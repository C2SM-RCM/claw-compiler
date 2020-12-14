/*
 * @author Mikhail Zhigun
 * @copyright Copyright 2020, MeteoSwiss
 */
package clawfc.ut;

import clawfc.tests.utils.TestsRunner;

public class Main
{
    public static void main(String[] args)
    {
        TestsRunner.main(args, UtilsTest.class, FortranCLAWScannerTest.class, FortranCLAWDetectorTest.class,
                FortranCommentsFilterTest.class, FortranDepStatementsRecognizerTest.class,
                FortranLineBreaksFilterTest.class, PreprocessorOutputScannerTest.class, PreprocessorTest.class,
                FortranDepScannerTest.class, FortranBuildInfoTest.class, BuildTest.class, FortranIncludeTest.class,
                FilterUtilsTest.class, FortranFrontEndTest.class, FortranFileBuildInfoDataTest.class);
    }
}
