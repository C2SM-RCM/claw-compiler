package x2x.translator.xcodeml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * example of XcodeML representation
 * <Var type="Fint" scope="local">i</Var>
 */

public class CLAWvar {
  private Element _varElement = null;
  private String _identity = null;
  private String _type = null;
  private String _scope = null;

  public CLAWvar(Element var){
    _varElement = var;
    readElementInformation();
  }

  public String getValue(){
    return _identity;
  }

  public String getScope(){
    return _scope;
  }

  public String getType(){
    return _type;
  }

  private void readElementInformation(){
    _type = CLAWelementHelper.getAttributeValue(_varElement, XelementName.ATTR_TYPE);
    _scope = CLAWelementHelper.getAttributeValue(_varElement, XelementName.ATTR_SCOPE);
    _identity = _varElement.getTextContent();
  }

  @Override
  public boolean equals(Object ob) {
    if (ob == null) return false;
    if (ob.getClass() != getClass()) return false;
    CLAWvar other = (CLAWvar)ob;

    if(!_identity.toLowerCase().equals(other.getValue().toLowerCase())){
      return false;
    }

    if(!_type.equals(other.getType())){
      return false;
    }

    if(!_scope.equals(other.getScope())){
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return _identity.hashCode() ^ _scope.hashCode() ^ _type.hashCode();
  }
}
