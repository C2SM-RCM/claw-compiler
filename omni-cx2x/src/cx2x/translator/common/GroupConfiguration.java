/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.common;

/**
 * Class holding group information from configuration.
 *
 * @author clementval
 */
public class GroupConfiguration {
  public enum GroupType { DEPENDENT, INDEPENDENT }

  private final String _name;
  private final String _cPath;
  private final GroupType _type;

  /**
   * Constructs a new GroupConfiguration element with all mandatory information.
   * @param name  User friendly name of the group.
   * @param type  Type of the group.
   * @param cPath Path to the transformation class.
   */
  public GroupConfiguration(String name, GroupType type, String cPath){
    _name = name;
    _cPath = cPath;
    _type = type;
  }

  /**
   * Get the name value.
   * @return Name value.
   */
  public String getName(){
    return _name;
  }

  /**
   * Get the class path value.
   * @return Class path value.
   */
  public String getTransformationClass(){
    return _cPath;
  }

  /**
   * Get the type value.
   * @return Type value.
   */
  public GroupType getType(){
    return _type;
  }


}
