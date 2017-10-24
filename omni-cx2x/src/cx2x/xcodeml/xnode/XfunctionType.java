/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xnode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * The XfunctionType represents the FfunctionType (3.4) element in XcodeML
 * intermediate representation.
 * <p>
 * Elements: (params?)
 * - Optional:
 * - params (Xparams)
 * Attributes:
 * - Required: type (text), return_type (text)
 * - Optional: result_name (text), is_recursive (bool), is_program (bool),
 * is_internal (bool)
 *
 * @author clementval
 */

public class XfunctionType extends Xtype {

  private Xparams _params = null;

  /**
   * Basic ctor from Xnode.
   *
   * @param node Xnode object.
   */
  public XfunctionType(Xnode node) {
    this(node.element());
  }

  /**
   * Element standard ctor. Pass the base element to the base class and read
   * inner information (elements and attributes).
   *
   * @param baseElement The root of the element.
   */
  public XfunctionType(Element baseElement) {
    super(baseElement);
    readElementInformation();
  }

  /**
   * Read inner element information.
   */
  private void readElementInformation() {
    Xnode paramsNode = matchSeq(Xcode.PARAMS);
    if(paramsNode != null) {
      _params = new Xparams(paramsNode.element());
    }
  }

  /**
   * Get the function result name.
   *
   * @return Result name value.
   */
  public String getResultName() {
    return getAttribute(Xattr.RESULT_NAME);
  }

  /**
   * Check whether function is recursive.
   *
   * @return True if the function is recursive. False otherwise.
   */
  public boolean isRecursive() {
    return getBooleanAttribute(Xattr.IS_RECURSIVE);
  }

  /**
   * Check whether function is internal.
   *
   * @return True if the function is internal. False otherwise.
   */
  public boolean isInternal() {
    return getBooleanAttribute(Xattr.IS_INTERNAL);
  }

  /**
   * Get the function return type.
   *
   * @return The function's return type as String.
   */
  public String getReturnType() {
    return getAttribute(Xattr.RETURN_TYPE);
  }

  /**
   * Check whether function is the program function.
   *
   * @return True if the function is the program function. False otherwise.
   */
  public boolean isProgram() {
    return getBooleanAttribute(Xattr.IS_PROGRAM);
  }


  /**
   * Get the params element.
   *
   * @return Params element.
   */
  public Xparams getParams() {
    return _params;
  }

  /**
   * Get a list of string representing the function parameters.
   *
   * @return List of string.
   */
  public List<String> getParamsNames() {
    List<String> parameters = new ArrayList<>();
    for(Xnode n : _params.getAll()) {
      parameters.add(n.value());
    }
    return parameters;
  }

  /**
   * Check the number of parameters of the function.
   *
   * @return Number of parameters.
   */
  public int getParameterNb() {
    return (_params == null) ? 0 : _params.getAll().size();
  }

  /**
   * A new object XfunctionType that is the clone of the current object.
   *
   * @return A new XfunctionType that is a clone of the current one.
   */
  public XfunctionType cloneNode() {
    Node clone = cloneRawNode();
    return new XfunctionType((Element) clone);
  }


  /**
   * Check if a parameter is part of the function definition.
   *
   * @param paramName Parameter's name.
   * @return True if the parameter is found in the function definition. False
   * otherwise.
   */
  public boolean hasParam(String paramName) {
    if(_params == null) {
      return false;
    }
    for(Xnode param : _params.getAll()) {
      if(param.value().equals(paramName.toLowerCase())) {
        return true;
      }
    }
    return false;
  }
}
