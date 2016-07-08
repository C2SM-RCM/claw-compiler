/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xnode;

import org.w3c.dom.Element;

/**
 * Xtype is the base class for element in the XtypeTable
 * (XbasicType, XfunctionType)
 *
 * @author clementval
 */

public class Xtype extends Xnode {

  /**
   * Element standard ctor. Pass the base element to the base class and read
   * inner information (elements and attributes).
   * @param baseElement The root of the element.
   */
  public Xtype(Element baseElement){
    super(baseElement);
  }

  /**
   * Set type value.
   * @param value New type value.
   */
  public void setType(String value){
    if(_baseElement != null){
      _baseElement.setAttribute(Xname.ATTR_TYPE, value);
    }
  }

  /**
   * Get type value.
   * @return Type value.
   */
  public String getType(){
    return getAttribute(Xattr.TYPE);
  }
}
