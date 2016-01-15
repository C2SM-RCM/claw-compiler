/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

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
 *
 * @author clementval
 */

public class XtypeTable extends XbaseElement {

  private static final int HASH_LENGTH = 12;
  private static final String FCT_HASH_PREFIX = "F";

  private Hashtable<String, Xtype> _table;

  public XtypeTable(Element typeTableElement){
    super(typeTableElement);
    _table = new Hashtable<String, Xtype>();
    readTable();
  }

  private void readTable(){
    // Read basic type
    NodeList basicTypes = baseElement.getElementsByTagName(XelementName.BASIC_TYPE);
    for (int i = 0; i < basicTypes.getLength(); i++) {
      Node n = basicTypes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        XbasicType bt = new XbasicType(el);
        _table.put(bt.getType(), bt);
      }
    }

    // Read fct type
    NodeList fctTypes = baseElement.getElementsByTagName(XelementName.FCT_TYPE);
    for (int i = 0; i < fctTypes.getLength(); i++) {
      Node n = fctTypes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        XfctType ft = new XfctType(el);
        _table.put(ft.getType(), ft);
      }
    }

    // Read struct type
    NodeList structTypes =
      baseElement.getElementsByTagName(XelementName.F_STRUCT_TYPE);
    for (int i = 0; i < structTypes.getLength(); i++) {
      Node n = structTypes.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        // TODO create XstructType object and insert it in the table
      }
    }
  }

  public int count(){
    return _table.size();
  }

  public void add(Xtype type){
    baseElement.appendChild(type.clone());
    _table.put(type.getType(), type);
  }

  public Xtype get(String key){
    return _table.get(key);
  }

  public String generateFctTypeHash(){
    String hash;
    do {
      hash = FCT_HASH_PREFIX + generateHash(HASH_LENGTH);
    } while(_table.containsKey(hash));
    return hash;
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
