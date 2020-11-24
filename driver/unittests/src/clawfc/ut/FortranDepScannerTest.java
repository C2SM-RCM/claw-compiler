/*
 * @author Mikhail Zhigun
 * @copyright Copyright 2020, MeteoSwiss
 */
package clawfc.ut;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import clawfc.depscan.FortranDepParser;
import clawfc.depscan.FortranDepScanner;
import clawfc.depscan.FortranException;
import clawfc.depscan.FortranFileBasicSummary;
import clawfc.depscan.FortranFileSummary;
import clawfc.depscan.FortranFileSummaryDeserializer;
import clawfc.depscan.FortranFileSummarySerializer;
import clawfc.depscan.FortranModuleBasicInfo;
import clawfc.depscan.FortranModuleInfo;
import clawfc.depscan.FortranSemanticException;
import clawfc.depscan.FortranSyntaxException;
import clawfc.depscan.Utils;
import clawfc.depscan.parser.FortranDepScannerBaseListener;
import clawfc.depscan.parser.FortranDepScannerLexer;
import clawfc.depscan.parser.FortranDepScannerParser;
import clawfc.utils.ByteArrayIOStream;
import junit.framework.TestCase;

class FortranDepScannerListener extends FortranDepScannerBaseListener
{
    public ArrayList<String> programOpen = new ArrayList<String>();
    public ArrayList<String> programClose = new ArrayList<String>();
    public ArrayList<String> moduleOpen = new ArrayList<String>();
    public ArrayList<String> moduleClose = new ArrayList<String>();
    public ArrayList<String> use = new ArrayList<String>();
    public ArrayList<String> other = new ArrayList<String>();

    @Override
    public void exitModule_open_stmt(FortranDepScannerParser.Module_open_stmtContext ctx)
    {
        moduleOpen.add(ctx.getText());
    }

    @Override
    public void exitModule_close_stmt(FortranDepScannerParser.Module_close_stmtContext ctx)
    {
        moduleClose.add(ctx.getText());
    }

    @Override
    public void exitProgram_open_stmt(FortranDepScannerParser.Program_open_stmtContext ctx)
    {
        programOpen.add(ctx.getText());
    }

    @Override
    public void exitProgram_close_stmt(FortranDepScannerParser.Program_close_stmtContext ctx)
    {
        programClose.add(ctx.getText());
    }

    @Override
    public void exitUse_stmt(FortranDepScannerParser.Use_stmtContext ctx)
    {
        use.add(ctx.getText());
    }
}

public class FortranDepScannerTest extends TestCase
{
    FortranDepScannerParser parser;
    FortranDepScannerLexer lexer;
    FortranDepParser depParser;
    FortranDepScanner depScanner;

    @Override
    protected void setUp() throws Exception
    {
        lexer = new FortranDepScannerLexer(toCharStream(""));
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser = new FortranDepScannerParser(toTokenStream(""));
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        depParser = new FortranDepParser();
        depScanner = new FortranDepScanner();
    }

    private static CharStream toCharStream(String str) throws IOException
    {
        InputStream inStrm = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        CharStream chrStrm = CharStreams.fromStream(inStrm, StandardCharsets.UTF_8);
        return chrStrm;
    }

    private CommonTokenStream toTokenStream(String str) throws IOException
    {
        CharStream chrStrm = toCharStream(str);
        lexer.reset();
        lexer.setInputStream(chrStrm);
        CommonTokenStream tokStrm = new CommonTokenStream(lexer);
        return tokStrm;
    }

    protected void acceptString(String str) throws IOException
    {
        parser.reset();
        parser.setInputStream(toTokenStream(str));
        parser.root();
        assertTrue(String.format("Failed to accept string \"%s\"", str),
                parser.isMatchedEOF() && parser.getNumberOfSyntaxErrors() == 0);
    }

    protected void rejectString(String str) throws IOException
    {
        parser.reset();
        parser.setInputStream(toTokenStream(str));
        parser.root();
        assertTrue(String.format("Failed to reject string \"%s\"", str), parser.getNumberOfSyntaxErrors() > 0);
    }

    protected String flipChrCase(String str, int idx)
    {
        char c = str.charAt(idx);
        if (c >= 'a' && c <= 'z')
        {
            Character.toUpperCase(c);
        } else
        {
            Character.toLowerCase(c);
        }
        str = str.substring(0, idx) + c + str.substring(idx + 1);
        return str;
    }

    void acceptIdentifierString(String str) throws IOException
    {
        acceptString("module " + str + "\n end module x\n");
    }

    void rejectIdentifierString(String str) throws IOException
    {
        rejectString("module " + str + "\n end module x\n");
    }

    public void testIdentifier() throws Exception
    {

        for (char l = 'a'; l < 'z'; ++l)
        {
            acceptIdentifierString("" + l);
            acceptIdentifierString(l + "_");
            for (char l2 = '0'; l2 < '9'; ++l2)
            {
                acceptIdentifierString(("" + l) + l2);
            }
        }
        for (char l = 'A'; l < 'Z'; ++l)
        {
            acceptIdentifierString("" + l);
            acceptIdentifierString(l + "_");
            for (char l2 = '0'; l2 < '9'; ++l2)
            {
                acceptIdentifierString(("" + l) + l2);
            }
        }
        rejectIdentifierString("_");
        for (char l2 = '0'; l2 < '9'; ++l2)
        {
            rejectIdentifierString("" + l2);
        }
    }

    void acceptModuleOpenString(String str) throws IOException
    {
        acceptString(str + "\n end module x\n");
    }

    void rejectModuleOpenString(String str) throws IOException
    {
        rejectString(str + "\n end module x\n");
    }

    public void testModuleOpenStatement() throws Exception
    {
        acceptModuleOpenString("module module");
        acceptModuleOpenString("module x");
        acceptModuleOpenString(" module x");
        acceptModuleOpenString("\tmodule x");
        acceptModuleOpenString("\rmodule x");
        acceptModuleOpenString("module x ");
        acceptModuleOpenString("module x\r");
        acceptModuleOpenString("module x\t");
        for (int i = 0, n = "module".length(); i < n; ++i)
        {
            String str = flipChrCase("module", i) + " x";
            acceptModuleOpenString(str);
        }
        rejectModuleOpenString("module");
        rejectModuleOpenString("modulex");
        rejectModuleOpenString("module x !");
        rejectModuleOpenString("module x y");
    }

    void acceptModuleCloseString(String str) throws IOException
    {
        acceptString("module x\n" + str);
    }

    void rejectModuleCloseString(String str) throws IOException
    {
        rejectString("module x\n" + str);
    }

    public void testModuleCloseStatement() throws Exception
    {
        acceptModuleCloseString("end module x");
        acceptModuleCloseString(" end module x");
        acceptModuleCloseString("\rend module x");
        acceptModuleCloseString("\tend module x");
        acceptModuleCloseString("\tend\r\rmodule  x ");
        for (int i = 0, n = "end".length(); i < n; ++i)
        {
            String str = flipChrCase("end", i) + " module x";
            acceptModuleCloseString(str);
        }
        for (int i = 0, n = "module".length(); i < n; ++i)
        {
            String str = "end " + flipChrCase("module", i) + " x";
            acceptModuleCloseString(str);
        }
        rejectModuleCloseString("\tend\r\rmodule  x z");
    }

    void acceptProgramOpenString(String str) throws IOException
    {
        acceptString(str + "\n end program x\n");
    }

    void rejectProgramOpenString(String str) throws IOException
    {
        rejectString(str + "\n end program x\n");
    }

    public void testProgramOpenStatement() throws Exception
    {
        acceptProgramOpenString("program x");
        acceptProgramOpenString(" program x");
        acceptProgramOpenString("\tprogram x");
        acceptProgramOpenString("\rprogram x");
        acceptProgramOpenString("program x ");
        acceptProgramOpenString("program x\r");
        acceptProgramOpenString("program x\t");
        for (int i = 0, n = "program".length(); i < n; ++i)
        {
            String str = flipChrCase("program", i) + " x";
            acceptProgramOpenString(str);
        }
        rejectProgramOpenString("program");
        rejectProgramOpenString("programx");
        rejectProgramOpenString("program x !");
        rejectProgramOpenString("program x y");
    }

    void acceptProgramCloseString(String str) throws IOException
    {
        acceptString("program x\n" + str);
    }

    void rejectProgramCloseString(String str) throws IOException
    {
        rejectString("program x\n" + str);
    }

    public void testProgramCloseStatement() throws Exception
    {
        acceptProgramCloseString("end Program x");
        acceptProgramCloseString(" end Program x");
        acceptProgramCloseString("\rend Program x");
        acceptProgramCloseString("\tend Program x");
        acceptProgramCloseString("\tend\r\rProgram  xy_23");
        for (int i = 0, n = "end".length(); i < n; ++i)
        {
            String str = flipChrCase("end", i) + " Program x";
            acceptProgramCloseString(str);
        }
        for (int i = 0, n = "Program".length(); i < n; ++i)
        {
            String str = "end " + flipChrCase("Program", i) + " x";
            acceptProgramCloseString(str);
        }
        rejectProgramCloseString("\tend\r\rprogram x z");
    }

    void acceptUseString(String str) throws IOException
    {
        acceptString("program x\n" + str + "\nend program x\n");
    }

    void rejectUseString(String str) throws IOException
    {
        FortranDepScannerListener res = runParser("program x\n" + str + "\nend program x\n");
        assertEquals(1, res.programOpen.size());
        // "Unfit" use strings are classified as "other" and therefore skipped
        assertEquals(0, res.use.size());
        assertEquals(1, res.programClose.size());
        assertEquals("program x", res.programOpen.get(0));
        assertEquals("end program x", res.programClose.get(0));
    }

    public void testUseStatement() throws Exception
    {
        acceptUseString("use x");
        for (int i = 0, n = "use".length(); i < n; ++i)
        {
            String str = flipChrCase("use", i) + " x";
            acceptUseString(str);
        }
        acceptUseString(" use x");
        acceptUseString(" use x ");
        acceptUseString("use x,y1=>z1");
        acceptUseString("use x,  y1=>z1");
        acceptUseString("use x,  y1  =>z1");
        acceptUseString("use x,  y1  =>  z1");
        acceptUseString("use x,  y1  =>  z1  ");
        acceptUseString("use x,  y1  =>  z1, y2 => z2 ");
        acceptUseString("use x,  y1  =>  z1, y2 => z2, y3 => z3 ");
        acceptUseString("use x,  ONLY: y1");
        for (int i = 0, n = "only".length(); i < n; ++i)
        {
            String str = "use x,  " + flipChrCase("only", i) + ":x";
            acceptUseString(str);
        }
        acceptUseString("use x ,  ONLY : y1");
        acceptUseString("use x,  ONLY : y1");
        acceptUseString("use x,  ONLY: y1,y2");
        acceptUseString("use x,  ONLY: y1,y2,y3");
        acceptUseString("use x,  ONLY:y1=>z1");
        acceptUseString("use x,  ONLY: y1 =>z1");
        acceptUseString("use x,  ONLY: y1  =>z1");
        acceptUseString("use x,  ONLY: y1  =>  z1");
        acceptUseString("use x,  ONLY: y1  =>  z1  ");
        acceptUseString("use x,  ONLY: y1  =>  z1, y2 => z2, y3 => z3 ");
        rejectUseString("usex");
        rejectUseString("use x,");
        rejectUseString("use x y ");
        rejectUseString("use x, y,");
        rejectUseString("use x, ONLY ");
        rejectUseString("use x, ONLY: x y");
    }

    FortranDepScannerListener runParser(String inputStr) throws IOException
    {
        FortranDepScannerListener listener = new FortranDepScannerListener();
        parser.reset();
        parser.setInputStream(toTokenStream(inputStr));
        parser.setBuildParseTree(true);
        ParseTree tree = parser.root();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener, tree);
        assertTrue(String.format("Failed to accept string \"%s\"", inputStr),
                parser.isMatchedEOF() && parser.getNumberOfSyntaxErrors() == 0);
        return listener;
    }

    public void testProgramStatement() throws Exception
    {
        FortranDepScannerListener res = runParser("program x\n" + "use y\n" + "end program x\n");
        assertEquals(1, res.programOpen.size());
        assertEquals(1, res.use.size());
        assertEquals(1, res.programClose.size());
        assertEquals("program x", res.programOpen.get(0));
        assertEquals("use y", res.use.get(0));
        assertEquals("end program x", res.programClose.get(0));
    }

    public void testModuleStatement() throws Exception
    {
        FortranDepScannerListener res = runParser("module x\n" + "use y\n" + "end module x\n");
        assertEquals(1, res.moduleOpen.size());
        assertEquals(1, res.use.size());
        assertEquals(1, res.moduleClose.size());
        assertEquals("module x", res.moduleOpen.get(0));
        assertEquals("use y", res.use.get(0));
        assertEquals("end module x", res.moduleClose.get(0));
    }

    FortranFileBasicSummary parse(String s) throws IOException, FortranException, Exception
    {
        return depParser.parse(Utils.toInputStream(s), null);
    }

    void verifyParse(String s, List<FortranModuleBasicInfo> expModules, FortranModuleBasicInfo expectedProgram)
            throws IOException, FortranException, Exception
    {
        FortranFileBasicSummary res = parse(s);
        FortranFileBasicSummary expRes = new FortranFileBasicSummary(expModules, expectedProgram);
        assertEquals(expRes, res);
    }

    public void testParsing() throws IOException, FortranException, Exception
    {
        verifyParse("", Arrays.asList(), (FortranModuleBasicInfo) null);
        verifyParse("module x\n" + "end module x\n",
                Arrays.asList(new FortranModuleBasicInfo("x", 0, 1, Arrays.asList())), (FortranModuleBasicInfo) null);
        verifyParse("module x\n" + "use y\n" + "end module x\n",
                Arrays.asList(new FortranModuleBasicInfo("x", 0, 2, Arrays.asList("y"))),
                (FortranModuleBasicInfo) null);
        verifyParse("module x\n" + "use y\n" + "bla\n" + "use z\n" + "end module x\n",
                Arrays.asList(new FortranModuleBasicInfo("x", 0, 4, Arrays.asList("y", "z"))),
                (FortranModuleBasicInfo) null);
        verifyParse(
                "module x\n" + "use y\n" + "bla\n" + "use z\n" + "end module x\n" + "module x1\n" + "use y1\n" + "bla\n"
                        + "use z1\n" + "end module x1\n",
                Arrays.asList(new FortranModuleBasicInfo("x", 0, 4, Arrays.asList("y", "z")),
                        new FortranModuleBasicInfo("x1", 5, 9, Arrays.asList("y1", "z1"))),
                (FortranModuleBasicInfo) null);
        verifyParse(
                "module x\n" + "use y\n" + "bla\n" + "use z\n" + "end module x\n" + "module x1\n" + "use y1\n" + "bla\n"
                        + "use z1\n" + "end module x1\n" + "program p1\n" + "use y1\n" + "bla\n" + "use z1\n"
                        + "end program p1\n",
                Arrays.asList(new FortranModuleBasicInfo("x", 0, 4, Arrays.asList("y", "z")),
                        new FortranModuleBasicInfo("x1", 5, 9, Arrays.asList("y1", "z1"))),
                new FortranModuleBasicInfo("p1", 10, 14, Arrays.asList("y1", "z1")));

    }

    public void testSyntaxError() throws IOException, FortranException, Exception
    {
        try
        {
            parse("end module x\n");
        } catch (FortranSyntaxException e)
        {
            assertTrue(e.getMessage().contains("extraneous input 'end module x'"));
            assertEquals(Integer.valueOf(1), e.line());
            return;
        }
        assertTrue(false);
    }

    public void testModuleNamesMismatchError() throws IOException, FortranException, Exception
    {
        try
        {
            parse("module x\n" + "end module y\n");
        } catch (FortranSemanticException e)
        {
            assertEquals("End module name \"y\" does not match current module name \"x\"", e.getMessage());
            assertEquals(Integer.valueOf(2), e.line());
            return;
        }
        assertTrue(false);
    }

    public void testDoubleDefError() throws IOException, FortranException, Exception
    {
        try
        {
            parse("module x\n" + "end module x\n" + "module x\n" + "end module x\n");
        } catch (FortranSemanticException e)
        {
            assertEquals("Double definition of module \"x\"", e.getMessage());
            assertEquals(Integer.valueOf(3), e.line());
            return;
        }
        assertTrue(false);
    }

    FortranFileBasicSummary basicScan(String s) throws IOException, FortranException, Exception
    {
        return depScanner.basicScan(Utils.toInputStream(s));
    }

    FortranFileSummary scan(String s) throws IOException, FortranException, Exception
    {
        return depScanner.scan(Utils.toInputStream(s));
    }

    void verifyBasicScan(String s, List<FortranModuleBasicInfo> expModules, FortranModuleBasicInfo expectedProgram)
            throws IOException, FortranException, Exception
    {
        FortranFileBasicSummary res = basicScan(s);
        FortranFileBasicSummary expRes = new FortranFileBasicSummary(expModules, expectedProgram);
        assertEquals(expRes, res);
    }

    void verifyScan(String s, List<FortranModuleInfo> expModules, FortranModuleInfo expectedProgram)
            throws IOException, FortranException, Exception
    {
        List<FortranModuleBasicInfo> basicExpModules = new ArrayList<FortranModuleBasicInfo>();
        for (FortranModuleInfo info : expModules)
        {
            basicExpModules.add(new FortranModuleBasicInfo(info.data()));
        }
        FortranFileBasicSummary basicRes = basicScan(s);
        FortranFileBasicSummary expBasicRes = new FortranFileBasicSummary(basicExpModules,
                expectedProgram != null ? new FortranModuleBasicInfo(expectedProgram.data()) : null);
        assertEquals(basicRes, expBasicRes);
        FortranFileSummary res = scan(s);
        FortranFileSummary expRes = new FortranFileSummary(expModules, expectedProgram);
        assertEquals(expRes, res);
    }

    public void testScanning() throws IOException, FortranException, Exception
    {
        verifyScan("", Arrays.asList(), (FortranModuleInfo) null);
        verifyScan("module x\n" + "end module x\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 1, Arrays.asList(), 0, 22, false)),
                (FortranModuleInfo) null);
        verifyScan("module x\n" + "use y\n" + "end module x\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 2, Arrays.asList("y"), 0, 28, false)),
                (FortranModuleInfo) null);
        verifyScan("module x\n" + "use y\n" + "bla\n" + "use z\n" + "end module x\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 4, Arrays.asList("y", "z"), 0, 38, false)),
                (FortranModuleInfo) null);
        verifyScan(
                "module x\n" + "use y\n" + "bla\n" + "use z\n" + "end module x\n" + "module x1\n" + "use y1\n" + "bla\n"
                        + "use z1\n" + "end module x1\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 4, Arrays.asList("y", "z"), 0, 38, false),
                        new FortranModuleInfo("x1", 5, 9, Arrays.asList("y1", "z1"), 38, 80, false)),
                (FortranModuleInfo) null);
        verifyScan(
                "module x\n" + "use y\n" + "bla\n" + "use z\n" + "end module x\n" + "module x1\n" + "use y1\n" + "bla\n"
                        + "use z1\n" + "end module x1\n" + "program p1\n" + "use y1\n" + "bla\n" + "use z1\n"
                        + "end program p1\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 4, Arrays.asList("y", "z"), 0, 38, false),
                        new FortranModuleInfo("x1", 5, 9, Arrays.asList("y1", "z1"), 38, 80, false)),
                new FortranModuleInfo("p1", 10, 14, Arrays.asList("y1", "z1"), 80, 124, false));

    }

    public void testScanWithComments() throws IOException, FortranException, Exception
    {
        verifyScan("module x  ! comment1 \n" + "end module x ! comment2\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 1, Arrays.asList(), 0, 46, false)),
                (FortranModuleInfo) null);
    }

    public void testScanWithLineBreaks() throws IOException, FortranException, Exception
    {
        verifyScan("m&   \n" + "&od&\n" + "    &ule&    \n" + "x  ! comment1 \n" + "end module x ! comment2\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 4, Arrays.asList(), 0, 64, false)),
                (FortranModuleInfo) null);
    }

    public void testWithClawDirectives() throws Exception
    {
        verifyScan("module x\n" + "!$claw\n" + "end module x\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 2, Arrays.asList(), 0, 29, true)),
                (FortranModuleInfo) null);
        verifyScan(
                "module x\n" + "use y\n" + "bla\n" + "use z\n" + "end module x\n" + "module x1\n" + "use y1\n" + "bla\n"
                        + "use z1\n" + "end module x1\n" + "program p1\n" + "use y1\n" + "bla\n" + "use z1\n"
                        + "!$omp claw\n" + "end program p1\n",
                Arrays.asList(new FortranModuleInfo("x", 0, 4, Arrays.asList("y", "z"), 0, 38, false),
                        new FortranModuleInfo("x1", 5, 9, Arrays.asList("y1", "z1"), 38, 80, false)),
                new FortranModuleInfo("p1", 10, 15, Arrays.asList("y1", "z1"), 80, 135, true));
    }

    void verifySerialization(FortranFileSummary obj) throws Exception
    {
        FortranFileSummarySerializer serializer = new FortranFileSummarySerializer();
        FortranFileSummaryDeserializer deserializer = new FortranFileSummaryDeserializer(true);
        ByteArrayIOStream buf = new ByteArrayIOStream();
        serializer.serialize(obj, buf);
        FortranFileSummary deObj = deserializer.deserialize(buf.getAsInputStreamUnsafe());
        assertEquals(obj, deObj);
    }

    public void testSerialization() throws Exception
    {
        verifySerialization(new FortranFileSummary(Arrays.asList(), null));
        verifySerialization(new FortranFileSummary(
                Arrays.asList(new FortranModuleInfo("x", 1, 4, Arrays.asList("y", "z"), 9, 38, false)), null));
        verifySerialization(new FortranFileSummary(
                Arrays.asList(new FortranModuleInfo("x", 1, 4, Arrays.asList("y", "z"), 9, 38, false),
                        new FortranModuleInfo("x1", 6, 9, Arrays.asList("y1", "z1"), 48, 80, false)),
                null));
        verifySerialization(new FortranFileSummary(
                Arrays.asList(new FortranModuleInfo("x", 1, 4, Arrays.asList("y", "z"), 9, 38, false),
                        new FortranModuleInfo("x1", 6, 9, Arrays.asList("y1", "z1"), 48, 80, false)),
                new FortranModuleInfo("p1", 11, 14, Arrays.asList("y1", "z1"), 91, 124, true)));
        verifySerialization(new FortranFileSummary(
                Arrays.asList(new FortranModuleInfo("x", 1, 4, Arrays.asList("y", "z"), 9, 38, false),
                        new FortranModuleInfo("x1", 6, 9, Arrays.asList("y1", "z1"), 48, 80, false)),
                new FortranModuleInfo("p1", 11, 14, Arrays.asList("y1", "z1"), 91, 124, true),
                Paths.get("/tmp/bla-dir/bla.file")));
    }
}
