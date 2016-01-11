package x2x.translator.xcodeml.xelement;

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
 */

public class Xid extends XbaseElement implements Xclonable<Xid> {
  private String _type = null;
  private String _sclass = null;

  private Xname _xname;

  public Xid(Element idElement){
    super(idElement);
    readElementInformation();
  }

  private void readElementInformation(){
    _type = XelementHelper.getAttributeValue(baseElement,
      XelementName.ATTR_TYPE);
    _sclass = XelementHelper.getAttributeValue(baseElement,
      XelementName.ATTR_SCLASS);
    _xname = XelementHelper.findName(this);
  }

  public void setName(String value){
    if(_xname != null){
      _xname.setName(value);
    }
  }

  public void setType(String value){
    if(baseElement != null){
      baseElement.setAttribute(XelementName.ATTR_TYPE, value);
      _type = value;
    }
  }

  public void setSclass(String value){
    if(baseElement != null){
      baseElement.setAttribute(XelementName.ATTR_SCLASS, value);
      _sclass = value;
    }
  }

  public String getName(){
    return (_xname != null) ? _xname.getValue() : null;
  }

  public String getType() {
    return _type;
  }

  public String getSclass(){
    return _sclass;
  }

  public Xid cloneObject(){
    Node clone = clone();
    return new Xid((Element)clone);
  }

}
