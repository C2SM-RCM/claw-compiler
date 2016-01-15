/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

import org.w3c.dom.Element;

/**
 * The XarrayConstructor represents the FarrayConstructor (7.2.1) element in
 * XcodeML intermediate representation.
 *
 * Elements:
 * - Optional:
 *   - exprModel* (XexprModel) TODO
 *
 * Attributes:
 * - Optional:
 *   - type (text)
 *
 * @author clementval
 */

public class XarrayConstructor extends XbaseElement {

  private String _type = null;

  public XarrayConstructor(Element baseElement){
    super(baseElement);
    _type = XelementHelper.getAttributeValue(this,
      XelementName.ATTR_TYPE);
  }

}
