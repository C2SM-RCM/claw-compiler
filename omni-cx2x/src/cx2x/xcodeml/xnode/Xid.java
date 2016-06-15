/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xnode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The Xid represents the id (8.2) element in XcodeML intermediate
 * representation.
 *
 * Elements:
 * - Required:
 *   - name (Xname)
 * Attributes:
 * - Required: type (text), sclass (text: auto, param, extern, extern_def,
 *             label, tagname) // TODO move to enum
 *
 * @author clementval
 */

public class Xid extends Xnode {
  private String _type = null;
  private String _sclass = null;
  private Xnode _xname;

  /**
   * Xelement standard ctor. Pass the base element to the base class and read
   * inner information (elements and attributes).
   * @param baseElement The root element of the Xelement
   */
  public Xid(Element baseElement){
    super(baseElement);
    readElementInformation();
  }

  /**
   * Read inner element information.
   */
  private void readElementInformation(){
    _type = getAttribute(Xattr.TYPE);
    _sclass = getAttribute(Xattr.SCLASS);
    _xname = find(Xcode.NAME);
  }

  /**
   * Set id name value.
   * @param value The new name value.
   */
  public void setName(String value){
    if(_xname != null && value != null){
      _xname.setValue(value);
    }
  }

  /**
   * Set id type value.
   * @param value The new type value.
   */
  public void setType(String value){
    if(_baseElement != null && value != null){
      _baseElement.setAttribute(XelementName.ATTR_TYPE, value);
      _type = value;
    }
  }

  /**
   * Set id sclass value.
   * @param value The new sclass value.
   */
  public void setSclass(String value){
    if(_baseElement != null && value != null){
      _baseElement.setAttribute(XelementName.ATTR_SCLASS, value);
      _sclass = value;
    }
  }

  /**
   * Get the id name value.
   * @return Name value.
   */
  public String getName(){
    return (_xname != null) ? _xname.getValue() : null;
  }

  /**
   * Get the id type value.
   * @return Type value.
   */
  public String getType() {
    return _type;
  }

  /**
   * Get the sclass value.
   * @return Sclass value.
   */
  public String getSclass(){
    return _sclass;
  }

  /**
   * @return A new object Xid that is the clone of the current object.
   */
  public Xid cloneObject(){
    Node clone = cloneNode();
    return new Xid((Element)clone);
  }

}
