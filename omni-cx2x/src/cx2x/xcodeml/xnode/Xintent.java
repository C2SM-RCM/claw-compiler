/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xnode;

import java.util.HashMap;
import java.util.Map;

/**
 * The Xintent represents the possible value for the intent attribute in XcodeML
 * intermediate representation.
 * <p>
 * Possible value are: in, out, inout
 *
 * @author clementval
 */
public enum Xintent {
  NONE,
  IN,
  OUT,
  INOUT,
  ANY; // Represents any intent as well as no intent.

  private static final Map<String, Xintent> _stringToEnum = new HashMap<>();

  static {
    for(Xintent intent : values()) {
      if(intent != ANY && intent != NONE) {
        _stringToEnum.put(intent.toString().toLowerCase(), intent);
      }
    }
  }

  /**
   * Convert string value to enum.
   *
   * @param value String value.
   * @return Corresponding enum value.
   */
  public static Xintent fromString(String value) {
    return (value == null || !_stringToEnum.containsKey(value.toLowerCase())) ?
        NONE : _stringToEnum.get(value.toLowerCase());
  }

  /**
   * Convert current enum to String value.
   *
   * @return Corresponding String value.
   */
  @Override
  public String toString() {
    switch(this) {
      case IN:
        return Xname.INTENT_IN;
      case OUT:
        return Xname.INTENT_OUT;
      case INOUT:
        return Xname.INTENT_INOUT;
      default:
        return "";
    }
  }

  /**
   * Check if it is an in intent.
   *
   * @return True if IN or INOUT. False otherwise.
   */
  public boolean isIntentIn() {
    return this == IN || this == INOUT;
  }

  /**
   * Check if it is an out intent.
   *
   * @return True if OUT or INOUT. False otherwise.
   */
  public boolean isIntentOut() {
    return this == OUT || this == INOUT;
  }

  /**
   * Check whether an intent is specified
   *
   * @return True if IN, OUT or INOUT. False otherwise.
   */
  public boolean isIntent() {
    return isIntentIn() || isIntentOut();
  }

  /**
   * Check if intent is compatible with the given one.
   *
   * @param intent Intent to check with.
   * @return True if intents are compatible.
   */
  public boolean isCompatible(Xintent intent) {
    return intent != null && (this == ANY || (intent == ANY)
        || (isIntentIn() && intent.isIntentIn())
        || (isIntentOut() && intent.isIntentOut())
        || (this == NONE && intent == NONE));

  }
}
