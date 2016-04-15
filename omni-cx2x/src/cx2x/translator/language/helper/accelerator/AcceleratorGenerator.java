/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.language.helper.accelerator;

/**
 * Basic accelerator directive generator that does nothing. Used when
 * AcceleratorDirective.NONE is set.
 *
 * TODO interface might need some refinments when we have a better idea of
 * TODO OpenACC vs OpenMP
 *
 * @author clementval
 */
abstract class AcceleratorGenerator {

  /**
   * Get the start pragma to define a parallel accelerated region.
   * @return String value that represents the pragma.
   */
  protected abstract String getStartParellelDirective();

  /**
   * Get the end pragma to define a parallel accelerated region.
   * @return String value that represents the pragma.
   */
  protected abstract String getEndParellelDirective();

  /**
   * Get formatted pragma defined by the accelerator directive prefix and the
   * given clauses.
   * @param clause Clauses to append to the accelerator directive prefix
   * @return String value that represents the pragma.
   */
  protected abstract String getSingleDirective(String clause);

  /**
   * Get the parallel keyword for a given accelerator language.
   * @return The corresponding parallel keyword.
   */
  protected abstract String getParallelKeyword();

  /**
   * Return contruction of the clause for a private variable in a
   * @param var Variable name that will be inserted in the generated clause.
   * @return An accelerator language specific private clause with the var.
   */
  protected abstract String getPrivateClause(String var);

}
