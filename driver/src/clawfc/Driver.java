/*
 * @author Mikhail Zhigun
 * @copyright Copyright 2020, MeteoSwiss
 */
package clawfc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import claw.ClawX2T;
import clawfc.utils.AsciiArrayIOStream;

public class Driver
{
    public static void main(String[] args) throws Exception
    {
        run(args);
        System.exit(0);
    }

    public static void run(String[] args) throws Exception
    {
        Utils.log.setLevel(java.util.logging.Level.INFO);
        Driver driver = new Driver();
        driver.verifyInstall();
        Options opts = Options.parseCmdlineArguments(args);
        if (!opts.verbose())
        {
            Utils.log.setLevel(java.util.logging.Level.WARNING);
        }
        if (opts != null)
        {
            verifyOptions(opts);
            driver.execute(opts);
        }
    }

    static Configuration _cfg;

    public static Configuration cfg()
    {
        return _cfg;
    }

    Driver() throws Exception
    {
        _cfg = new Configuration();
    }

    void verifyInstall()
    {
        if (!Files.isDirectory(cfg().installRoot()))
        {
            throw new RuntimeException((String.format(
                    "CLAW install directory \"%s\" does not exist or is not a directory", cfg().installRoot())));
        }
        if (!Files.isDirectory(cfg().omniInstallRoot()))
        {
            throw new RuntimeException(
                    String.format("OMNI XCodeML Tools install directory \"%s\" does not exist or is not a directory",
                            cfg().omniInstallRoot()));
        }
        if (!Files.isExecutable(cfg().omniFrontEnd()))
        {
            throw new RuntimeException(
                    String.format("OMNI XCodeML Tools Fortran Frontend \"%s\" does not exist or is not a directory",
                            cfg().omniFrontEnd()));
        }
        {
            String omniVersionTag = null;
            try
            {
                omniVersionTag = Utils.getCmdOutput(new String[] { cfg().omniFrontEnd().toString(), "--version-tag" });
            } catch (Exception e)
            {
                throw new RuntimeException("Failed to get OMNI XCodeML Tools version: " + e.getMessage());
            }
            if (!cfg().omniVersionTag().equals(omniVersionTag))
            {
                throw new RuntimeException(
                        String.format("OMNI XCodeML Tools version mismatch\n\texpected: \"%s\"\n\tgot: \"%s\"",
                                cfg().omniVersionTag(), omniVersionTag));
            }
        }
    }

    class InputFileData
    {
        public Path inDir;
        public Path inFilename;

        public Path inPath()
        {
            return inDir.resolve(inFilename);
        }

        public AsciiArrayIOStream pp;
        public Path tempDir;
        public Path ppFilename;

        public Path ppPath()
        {
            return tempDir.resolve(ppFilename);
        }

        public Path ppErrLogPath()
        {
            return tempDir.resolve(ppFilename + ".log");
        }

        public InputFileData(Path inPath)
        {
            inDir = Utils.dirPath(inPath);
            inFilename = inPath.getFileName();
            pp = null;
            tempDir = null;
            ppFilename = null;
        }
    }

    void execute(Options opts) throws Exception
    {
        if (opts.printInstallCfg())
        {
            print(cfg().toString());
        } else if (opts.printVersion())
        {
            printVersion();
        } else if (opts.printTargets())
        {
            ClawX2T.main(new String[] { "--target-list" });
        } else if (opts.printDirectives())
        {
            ClawX2T.main(new String[] { "--directive-list" });
        } else if (opts.printCfg())
        {
            ArrayList<String> args = new ArrayList<String>(
                    Arrays.asList("--show-config", "--config-path=" + cfg().configDir()));
            String cfg = opts.configFile();
            if (cfg != null)
            {
                args.add("--config=" + cfg);
            }
            ClawX2T.main(args.stream().toArray(String[]::new));
        } else if (opts.printOptions())
        {
            print(opts.toString());
        } else
        {
            Path tmpDir = null;
            try
            {
                info("Creating temp files directory...");
                tmpDir = createTempDir(opts);
                info(tmpDir.toString(), 1);
                info("Creating temp dirs for input files...");
                InputFileData[] inputFilesData = createInputData(opts.inputFiles());
                Path inputTmpDir = createInputFilesTempDir(inputFilesData, tmpDir);
                info("Preprocessing input files...");
                FortranPreprocessor pp = new FortranPreprocessor(cfg(), opts);
                preprocessInputFiles(inputFilesData, pp, opts.keepIntermediateFiles(), !opts.disableMultiprocessing(),
                        opts.skipPreprocessing());
                if (opts.stopAfterPreprocessing())
                {
                    return;
                }
            } finally
            {
                if (tmpDir != null && !opts.keepIntermediateFiles())
                {
                    Utils.removeDir(tmpDir);
                }
            }
        }
    }

    void executeUntilFirstError(List<Callable<Void>> tasks, boolean useMultiProcessing) throws Exception
    {
        List<Future> taskFutures = new ArrayList<Future>(tasks.size());
        ExecutorService es = createThreadPool(useMultiProcessing);
        try
        {
            for (Callable<Void> task : tasks)
            {
                taskFutures.add(es.submit(task));
            }
            for (Future taskFuture : taskFutures)
            {
                try
                {
                    taskFuture.get();
                } catch (InterruptedException e)
                {
                } catch (ExecutionException e)
                {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception)
                    {
                        throw (Exception) cause;
                    } else
                    {
                        throw e;
                    }
                }
            }
        } finally
        {
            es.shutdownNow();
        }
    }

    ExecutorService createThreadPool(boolean useMultiProcessing)
    {
        if (useMultiProcessing)
        {
            int maxNumThreads = Runtime.getRuntime().availableProcessors();
            return Executors.newFixedThreadPool(maxNumThreads);
        } else
        {
            return Executors.newSingleThreadExecutor();
        }
    }

    InputFileData[] createInputData(List<Path> inputFiles)
    {
        InputFileData[] inputFilesData = new InputFileData[inputFiles.size()];
        for (int i = 0; i < inputFilesData.length; ++i)
        {
            inputFilesData[i] = new InputFileData(inputFiles.get(i));
        }
        return inputFilesData;
    }

    void preprocessInputFiles(final InputFileData[] inputFilesData, final FortranPreprocessor pp,
            final boolean createFiles, boolean enableMultiprocessing, boolean skipPreprocessing) throws Exception
    {
        List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(inputFilesData.length);
        for (int i = 0; i < inputFilesData.length; ++i)
        {
            final InputFileData data = inputFilesData[i];
            tasks.add(new Callable<Void>()
            {
                public Void call() throws Exception
                {
                    try
                    {
                        if (createFiles)
                        {
                            data.ppFilename = Paths.get(FortranPreprocessor.outputFilename(data.inPath()));
                        }
                        if (!skipPreprocessing)
                        {
                            Utils.log.info(String.format("Preprocessing file \"%s\"...", data.inPath()));
                            data.pp = pp.apply(data.inPath(), createFiles ? data.ppPath() : null);
                            Utils.log.info(String.format("Finished preprocessing file \"%s\"...", data.inPath()));
                        } else
                        {
                            Utils.log.info(String.format("Copying file \"%s\"...", data.inPath()));
                            data.pp = new AsciiArrayIOStream();
                            try (InputStream inStrm = new FileInputStream(data.inPath().toString()))
                            {
                                Utils.copy(inStrm, data.pp);
                            }
                            if (createFiles)
                            {
                                try (OutputStream outStrm = new FileOutputStream(data.ppPath().toString()))
                                {
                                    Utils.copy(data.pp.getAsInputStreamUnsafe(), outStrm);
                                }
                            }
                            Utils.log.info(String.format("Finished copying file \"%s\"...", data.inPath()));
                        }
                    } catch (FortranPreprocessor.Failed e)
                    {
                        if (createFiles)
                        {
                            Utils.writeTextToFile(data.ppErrLogPath(), e.stderr);
                        }
                        throw e;
                    }
                    return null;
                }
            });
        }
        executeUntilFirstError(tasks, enableMultiprocessing);
    }

    Path createInputFilesTempDir(InputFileData[] inputFilesData, Path tmpDir) throws IOException
    {
        Path tmpInputFilesDir = tmpDir.resolve("input");
        for (InputFileData data : inputFilesData)
        {
            data.tempDir = tmpInputFilesDir;
        }
        Files.createDirectories(tmpInputFilesDir);
        return tmpInputFilesDir;
    }

    /*
     * Map<Path, Path> createInputFilesTempDirs(InputFileData[] inputFilesData, Path
     * tmpDir) throws IOException { Map<Path, Path> inputToTemp = new
     * LinkedHashMap<Path, Path>(); for (InputFileData data : inputFilesData) { Path
     * inputFileDir = data.inDir; Path tmpInputFileDir =
     * Paths.get(tmpDir.resolve("input").toString() + inputFileDir.toString());
     * inputToTemp.put(inputFileDir, tmpInputFileDir); data.tempDir =
     * tmpInputFileDir; } for (Map.Entry<Path, Path> entry : inputToTemp.entrySet())
     * { Path tmpInputFileDir = entry.getValue();
     * Files.createDirectories(tmpInputFileDir); } return
     * Collections.unmodifiableMap(inputToTemp); }
     */

    void print(String s)
    {
        System.out.println(s);
    }

    void info(String txt, int subLevel)
    {
        String prefix = String.join("", Collections.nCopies(subLevel, "\n"));
        Utils.log.info(prefix + txt);
    }

    void info(String txt)
    {
        info(txt, 0);
    }

    void error(String txt, int subLevel)
    {
        String prefix = String.join("", Collections.nCopies(subLevel, "\n"));
        Utils.log.severe(prefix + txt);
    }

    void error(String txt)
    {
        error(txt, 0);
    }

    Path createTempDir(Options opts) throws IOException
    {
        if (opts.intermediateFilesDir() != null)
        {
            final Path intDir = opts.intermediateFilesDir();
            if (Utils.dirExists(intDir))
            {
                Utils.removeDir(intDir);
            }
            Files.createDirectories(intDir);
            return intDir;
        } else
        {
            return Files.createTempDirectory(Paths.get(Utils.DEFAULT_TOP_TEMP_DIR), "clawfc");
        }
    }

    static void verifyOptions(Options opts)
    {
        {
            Map<Path, Path> fileNames = new HashMap<Path, Path>();
            for (Path inFilePath : opts.inputFiles())
            {
                if (!Utils.fileExists(inFilePath))
                {
                    throw new RuntimeException(
                            String.format("Input file \"%s\" does not exist or is a directory", inFilePath.toString()));
                }
                Path filename = inFilePath.getFileName();
                Path oldPath = fileNames.put(filename, inFilePath);
                if (oldPath != null)
                {
                    throw new RuntimeException(
                            String.format("Input files cannot have identical names: \n\"%s\"\n\"%s\"",
                                    oldPath.toString(), inFilePath.toString()));
                }
            }

        }
        if (opts.inputFiles().size() == 1)
        {
            if (opts.outputFile() == null && opts.outputDir() == null)
            {
                throw new RuntimeException(String.format("Either output file or output dir must be specified"));
            }
        } else if (opts.inputFiles().size() > 1)
        {
            if (opts.outputDir() == null)
            {
                throw new RuntimeException(
                        String.format("Output dir must be specified when multiple input files are used"));
            }
        }
        if (opts.fortranCompilerType() != null ^ opts.fortranCompilerCmd() != null)
        {
            throw new RuntimeException(String.format("Options --fc-type and --fc-cmd must be specified together"));
        }
        for (Path incPath : opts.includeDirs())
        {
            if (!Utils.dirExists(incPath))
            {
                throw new RuntimeException(
                        String.format("Include dir \"%s\" does not exist or is not a directory", incPath.toString()));
            }
        }
    }

    void printVersion()
    {
        String vStr = String.format("%s %s \"%s\" %s ", cfg().name(), cfg().version(), cfg().commit(),
                cfg().omniVersion());
        print(vStr);
    }
};