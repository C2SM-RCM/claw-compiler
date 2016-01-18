/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

import org.w3c.dom.Element;


/**
 * The Xcondition represents the condition (6.27) element in XcodeML
 * intermediate representation.
 *
 * Elements:
 * - Required:
 *   - exprModel (XexprModel)
 *
 * @author clementval
 */

public class Xcondition extends XbaseElement {
  private XexprModel _exprModel;

  /**
   * Xelement standard ctor. Pass the base element to the base class and read
   * inner information (elements and attributes).
   * @param baseElement The root element of the Xelement
   */
  public Xcondition(Element baseElement){
    super(baseElement);
    _exprModel = XelementHelper.findExprModel(this);
  }

  /**
   * @return The inner exprModel element.
   */
  public XexprModel getExprModel(){
    return _exprModel;
  }
}
