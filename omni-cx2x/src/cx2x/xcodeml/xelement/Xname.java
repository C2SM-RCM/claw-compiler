/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

import org.w3c.dom.Element;
import cx2x.xcodeml.helper.*;

/**
 * The Xname represents the name (8.3) element in XcodeML intermediate
 * representation.
 * Elements: the base element can contains text data
 * Attributes:
 * - Requited: type (text)
 *
 * @author clementval
 */

public class Xname extends XbaseElement {
  private String _nameValue = null;
  private String _type = null;

  /**
   * Xelement standard ctor. Pass the base element to the base class and read
   * inner information (elements and attributes).
   * @param baseElement The root element of the Xelement
   */
  public Xname(Element baseElement){
    super(baseElement);
    readElementInformation();
  }

  /**
   * Set name value.
   * @param value New name value.
   */
  public void setName(String value){
    if(baseElement != null){
      baseElement.setTextContent(value);
      _nameValue = value;
    }
  }

  /**
   * Get the type attribute value.
   * @param value Type value.
   */
  public void setType(String value){
    if(baseElement != null){
      baseElement.setAttribute(XelementName.ATTR_TYPE, value);
      _type = value;
    }
  }

  /**
   * Read inner element information.
   */
  private void readElementInformation(){
    _type = XelementHelper.getAttributeValue(this, XelementName.ATTR_TYPE);
    _nameValue = baseElement.getTextContent();
  }

  /**
   * Get the name value.
   * @return Name value.
   */
  public String getValue(){
    return _nameValue;
  }

  /**
   * Get the type attribute value.
   * @return Type value.
   */
  public String getType(){
    return _type;
  }

  /**
   * Check whether a given Xname object is identical with the current one.
   * @param other The other object to compare with.
   * @return True if the two objects are identical. False otherwise.
   */
  public boolean isIdentical(Xname other){
    // TODO override equals ?
    return isIdentical(other.getValue(), other.getType());
  }

  /**
   * Check whether a given name and type combination is identical with the
   * current one.
   * @param name The name to compare with.
   * @param type The type to compare with.
   * @return True if the two objects are identical. False otherwise.
   */
  public boolean isIdentical(String name, String type){
    return _nameValue.equals(name); //&& _type.equals(type);
    //TODO analyze why type if not identical in fcall and fdef
  }

  /**
   * Create a name element with value and type in the given program.
   * @param xcodeml The XcodeProg object in which the empty element is created.
   * @param value   Value of the element.
   * @param type    Type of the name element.
   * @return The empty element created.
   */
  public static Xname createEmpty(XcodeProg xcodeml, String value,
    String type)
  {
    Element nameElement = xcodeml.getDocument().
      createElement(XelementName.NAME);
    Xname name = new Xname(nameElement);
    name.setName(value);
    name.setType(type);
    return name;
  }

}
