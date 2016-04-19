/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.language.helper.accelerator;

/**
 * OpenACC specific accelerator directive generator.
 *
 * @author clementval
 */
class OpenAcc extends AcceleratorGenerator {

  private static final String OPENACC_PREFIX = "acc";
  private static final String OPENACC_PARALLEL = "parallel";
  private static final String OPENACC_END = "end";
  private static final String OPENACC_PRIVATE = "private";

  @Override
  protected String getPrefix(){
    return OPENACC_PREFIX;
  }

  @Override
  protected String getStartParellelDirective() {
    return OPENACC_PREFIX + " " + OPENACC_PARALLEL;
  }

  @Override
  protected String getEndParellelDirective() {
    return OPENACC_PREFIX + " " + OPENACC_END + " " + OPENACC_PARALLEL;
  }

  @Override
  protected String getSingleDirective(String clause) {
    return OPENACC_PREFIX + " " + clause;
  }

  @Override
  protected String getParallelKeyword(){
    return OPENACC_PARALLEL;
  }

  @Override
  protected String getPrivateClause(String var) {
    return OPENACC_PRIVATE + "(" + var + ")";
  }
}
