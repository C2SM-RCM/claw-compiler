package x2x.translator.xcodeml.xelement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Hashtable;
import java.util.Random;


/**
 * The XtypeTable represents the typeTable (3.1) element in XcodeML intermediate
 * representation.
 *
 * Elements:
 * - Optional:
 *   - FbasicType (XbasicType)
 *   - FfunctionType (XfctType)
 *   - FstructType (XstructType)
 */

public class XtypeTable {

  private Hashtable<String, Xtype> _table;
  private Element _baseElement = null;

  public XtypeTable(Element symbols){
    _baseElement = symbols;
    _table = new Hashtable<String, Xtype>();
    readTable();
  }

  private void readTable(){
    // Read basic type
    NodeList basicTypes = _baseElement.getElementsByTagName(XelementName.BASIC_TYPE);
    for (int i = 0; i < basicTypes.getLength(); i++) {
      Node n = basicTypes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        XbasicType bt = new XbasicType(el);
        _table.put(bt.getType(), bt);
      }
    }
    // Read fct type
    NodeList fctTypes = _baseElement.getElementsByTagName(XelementName.FCT_TYPE);
    for (int i = 0; i < fctTypes.getLength(); i++) {
      Node n = fctTypes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        XfctType ft = new XfctType(el);
        _table.put(ft.getType(), ft);
      }
    }

    // TODO read structtype object
  }

  public void add(Xtype type){
    _baseElement.appendChild(type.clone());
    _table.put(type.getType(), type);
  }

  public Xtype get(String key){
    return _table.get(key);
  }

  public String generateFctTypeHash(){
    // TODO check that hash is unique otherwise generate a new one
    return "F" + generateHash(12);
  }

  private String generateHash(int length){
    Random r = new Random();
    StringBuffer sb = new StringBuffer();
    while(sb.length() < length){
      sb.append(Integer.toHexString(r.nextInt()));
    }
    return sb.toString().substring(0, length);
  }
}
