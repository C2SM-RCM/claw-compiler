/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import cx2x.xcodeml.helper.*;

/**
 * The XargumentsTable represents the arguments (7.5.3) element in XcodeML
 * intermediate representation.
 *
 * Elements:
 * - Optional:
 *   - exprModel
 *
 * @author clementval
 */

public class XargumentsTable extends XbaseElement {
  private final Map<String, XexprModel> _table;

  /**
   * Xelement standard ctor. Pass the base element to the base class and read
   * inner information (elements and attributes).
   * @param baseElement The root element of the Xelement
   */
  public XargumentsTable(Element baseElement){
    super(baseElement);
    _table = new Hashtable<>();
    readTable();
  }

  /**
   * Read all the arguments present in the arguments table
   */
  private void readTable(){
    // Read Var element
    NodeList elements = baseElement.getChildNodes();
    for (int i = 0; i < elements.getLength(); i++) {
      Node n = elements.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        switch (el.getTagName()){
          case XelementName.VAR:
            Xvar var = new Xvar(el);
            _table.put(var.getValue(), new XexprModel(var));
            break;
          case XelementName.F_INT_CONST:
            XintConstant con = new XintConstant(el);
            _table.put(con.getValue(), new XexprModel(con));
            break;
        }
        // TODO read table differently to have all elements.
      }
    }
  }

  /**
   * Find a specific arguments.
   * @param name Name of the arguments.
   * @return The argument if found. Null otherwise.
   */
  public XexprModel findArgument(String name){
    if(_table.containsKey(name)) {
      return _table.get(name);
    }
    return null;
  }

  /**
   * Replace a var arguments by a array reference.
   * @param var       The var arguments to be replaced.
   * @param arrayRef  The new array reference arguments.
   */
  public void replace(Xvar var, XarrayRef arrayRef){
    if(var != null){
      XelementHelper.insertAfter(var, arrayRef);
      var.delete();
    } else {
      // TODO trigger a critical error
    }
  }

  /**
   * Add an arrayRef to the arguments table.
   * @param arrayRef The arrayRef to be added.
   */
  public void add(XarrayRef arrayRef){
    baseElement.appendChild(arrayRef.cloneNode());
    //_table.put(arrayRef.getVar().getValue(), arrayRef); TODO
  }

  /**
   * Add a var to the arguments table.
   * @param var The var to be added.
   */
  public void add(Xvar var){
    baseElement.appendChild(var.cloneNode());
    _table.put(var.getValue(), new XexprModel(var));
  }

  /**
   * Add a exprModel to the arguments table.
   * @param constant The exprModel to be added.
   */
  public void add(XintConstant constant){
    baseElement.appendChild(constant.cloneNode());
    _table.put(constant.getValue(), new XexprModel(constant));
  }




  /**
   * Get number of arguments presnet in the table.
   * @return Number of arguements in the table.
   */
  public int count(){
    return _table.size();
  }

  /**
   * Check if an argument is present in the argument table
   * @param name Name of the argument to be checked.
   * @return True if the argument is present. False otherwise.
   */
  public boolean hasArgument(String name){
    return _table.containsKey(name);
  }

  /**
   * Get an iterator on the arguments table
   * @return An iterator over the arguments table
   */
  public Iterator<Map.Entry<String, XexprModel>> iterator(){
    return _table.entrySet().iterator();
  }
}
