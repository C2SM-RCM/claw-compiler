package x2x.translator.xcodeml.xelement;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Xbound represents the lowerBound and upperBound (8.12, 8.13) element in
 * XcodeML intermediate representation.
 *
 * Elements:
 * - exprModel TODO move to exprModel
 */


public class Xbound extends XbaseElement {
  private String _value = null;
  private boolean _constant = false;
  private boolean _isVar = false;
  private Xvar _var = null;

  public Xbound(Element boundElement){
    super(boundElement);
    readRangeValue();
  }

  public boolean isContant(){
    return _constant;
  }

  public boolean isVar(){
    return _isVar;
  }

  public String getValue(){
    return _value;
  }

  public String getType(){
    if(isVar() && _var != null) {
      return _var.getType();
    }
    return null;
  }

  private void readRangeValue(){
    XintConstant constant = XelementHelper.findIntConstant(this);
    Xvar var = XelementHelper.findVar(this);
    if(constant != null){
      _constant = true;
      _value = constant.getValue();
    } else if(var != null){
      _isVar = true;
      _var = var;
      _value = var.getValue();
    }
  }


  @Override
  public boolean equals(Object ob) {
    if (ob == null) return false;
    if (ob.getClass() != getClass()) return false;
    Xbound other = (Xbound)ob;

    if(isVar() != other.isVar()){
      return false;
    }

    if(isContant() != isContant()){
      return false;
    }

    if (!getValue().toLowerCase().equals(other.getValue())){
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return _value.hashCode();
  }

}
