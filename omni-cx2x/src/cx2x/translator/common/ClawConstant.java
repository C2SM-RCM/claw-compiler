/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.common;

/**
 * Contains common constants values of the CLAW XcodeML to XcodeML translator
 *
 * @author clementval
 */
public class ClawConstant {

  public static final String EMPTY_STRING = "";
  public static final String OPENACC_PREFIX = "acc";
  public static final String OPENMP_PREFIX = "omp";
  public static final int OPENACC_PREFIX_LENGTH = 6; // "!$acc "
  public static final String CONTINUATION_LINE_SYMBOL = "&";
  public static final String DEFAULT_STEP_VALUE = "1";
  public static final String DEFUALT_LOWER_BOUND = "1";
  public static final String ITER_PREFIX = "iter_";
  public static final String CLAW = "claw";

  public static final String EXTRACTION_SUFFIX = "_extracted";

  public static final String CLAW_MOD_SUFFIX = ".claw";

  // CLAW atrtibute
  public static final String IS_CLAW = "is_claw";
  public static final String OVER = "over";

  // Over position constant value
  public static final String BEFORE = "before";
  public static final String MIDDLE = "middle";
  public static final String AFTER = "after";

}
