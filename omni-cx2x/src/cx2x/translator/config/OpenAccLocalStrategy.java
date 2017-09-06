/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.config;

/**
 * Define the enum values for the local strategy for OpenACC
 *
 * @author clementval
 */
public enum OpenAccLocalStrategy {
  PRIVATE,
  PROMOTE;

  /**
   * Get enum value from configuration string.
   *
   * @param value String value from configuration.
   * @return Enum value corresponding to the string. Default is PRIVATE.
   */
  public static OpenAccLocalStrategy fromString(String value) {
    if(value == null) {
      return PRIVATE;
    }
    switch(value) {
      case OpenAccConfiguration.LOCAL_STRATEGY_PRIVATE:
        return PRIVATE;
      case OpenAccConfiguration.LOCAL_STRATEGY_PROMOTE:
        return PROMOTE;
      default:
        return PRIVATE;
    }
  }
}
