/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

/**
 * The XtypeTable represents the typeTable (3.1) element in XcodeML intermediate
 * representation.
 *
 * Elements: ( FbasicType | FfunctionType | FstructType ) *
 * - Optional:
 *   - FbasicType (XbasicType)
 *   - FfunctionType (XfunctionType)
 *   - FstructType (XstructType)
 *
 * @author clementval
 */

public class XtypeTable extends XbaseElement implements Xclonable<XtypeTable> {

  private static final int HASH_LENGTH = 12;
  private static final String FCT_HASH_PREFIX = "F";
  private static final String INT_HASH_PREFIX = "I";
  private static final String REAL_HASH_PREFIX = "R";
  private static final String ARRAY_HASH_PREFIX = "A";

  private final Map<String, Xtype> _table;

  /**
   * Xelement standard ctor. Pass the base element to the base class and read
   * inner information (elements and attributes).
   * @param baseElement The root element of the Xelement
   */
  public XtypeTable(Element baseElement){
    super(baseElement);
    _table = new LinkedHashMap<>();
    readTable();
  }

  /**
   * Read the type table.
   */
  private void readTable(){
    Node crtNode = baseElement.getFirstChild();
    while(crtNode != null){
      if (crtNode.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element)crtNode;
        switch (el.getTagName()){
          case XelementName.BASIC_TYPE:
            XbasicType bt = new XbasicType(el);
            _table.put(bt.getType(), bt);
            break;
          case XelementName.FCT_TYPE:
            XfunctionType ft = new XfunctionType(el);
            _table.put(ft.getType(), ft);
            break;
          case XelementName.F_STRUCT_TYPE:
            // TODO create XstructType object and insert it in the table
            break;
        }
      }
      crtNode = crtNode.getNextSibling();
    }
  }

  /**
   * Get number of elements in the type table.
   * @return Number of elements in the table.
   */
  public int count(){
    return _table.size();
  }

  /**
   * Add a new element in the type table.
   * @param type The new type to be added.
   */
  public void add(Xtype type){
    baseElement.appendChild(type.cloneNode());
    _table.put(type.getType(), type);
  }

  /**
   * Get an element from the type table.
   * @param type Type of the element to be returned.
   * @return Xtype object if found in the table. Null otherwise.
   */
  public Xtype get(String type) {
    if(_table.containsKey(type)){
      return _table.get(type);
    }
    return null;
  }

  /**
   * Check if a type is present in the type table
   * @param type Type of the element to be checked.
   * @return True if the element is present. False otherwise.
   */
  public boolean hasType(String type){
    return _table.containsKey(type);
  }

  /**
   * Get a new unique function hash for the type table.
   * @return New unique fct type hash.
   */
  public String generateFctTypeHash(){
    return generateHash(FCT_HASH_PREFIX);
  }

  /**
   * Get a new unique integer hash for the type table.
   * @return New unique integer type hash.
   */
  public String generateIntegerTypeHash(){
    return generateHash(INT_HASH_PREFIX);
  }

  /**
   * Get a new unique real hash for the type table.
   * @return New unique real type hash.
   */
  public String generateRealTypeHash(){
    return generateHash(REAL_HASH_PREFIX);
  }

  /**
   * Get a new unique array hash for the type table.
   * @return New unique array type hash.
   */
  public String generateArrayTypeHash(){
    return generateHash(ARRAY_HASH_PREFIX);
  }

  /**
   * Get a new unique hash for the type table with the given prefix.
   * @param prefix Prefix added to the hash string.
   * @return New unique hash.
   */
  private String generateHash(String prefix){
    String hash;
    do {
      hash = prefix + generateHash(HASH_LENGTH);
    } while(_table.containsKey(hash));
    return hash;
  }

  /**
   * Generate a new unique type hash for the table.
   * @param length Length of the hash string to be generated.
   * @return The new unique hash.
   */
  private String generateHash(int length){
    Random r = new Random();
    StringBuilder sb = new StringBuilder();
    while(sb.length() < length){
      sb.append(Integer.toHexString(r.nextInt()));
    }
    return sb.toString().substring(0, length);
  }

  /**
   * Returns a Collection view of the values contained in this XtypeTable.
   * @return A view of the values contained in this map
   */
  public Collection<Xtype> values(){
    return _table.values();
  }

  @Override
  public XtypeTable cloneObject() {
    Element clone = (Element)cloneNode();
    return new XtypeTable(clone);
  }
}
