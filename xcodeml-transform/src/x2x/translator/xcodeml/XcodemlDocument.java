package x2x.translator.xcodeml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.File;
import java.util.Hashtable;

public class XcodemlDocument{
  private Document _xcodemlDoc = null;
  private String _xcodemlInputFile = null;
  private Element _typeTableElement = null;
  private Hashtable<String, CLAWbasicType> _typeTable = null;

  public XcodemlDocument(String inputFile){
    _xcodemlInputFile = inputFile;
    _typeTable = new Hashtable<String, CLAWbasicType>();
  }

  public Document getDocument(){
    return _xcodemlDoc;
  }

  public Hashtable<String, CLAWbasicType> getTypeTable(){
    return _typeTable;
  }

  public void readXcodeML(){
    try {
      File fXmlFile = new File(_xcodemlInputFile);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(fXmlFile);
      doc.getDocumentElement().normalize();
      _xcodemlDoc = doc;
    } catch(Exception ex){
      _xcodemlDoc = null;
    }
  }

  public boolean isXcodeMLvalid() throws Exception {
    if(_xcodemlDoc == null){
      return false;
    }

    Element root = _xcodemlDoc.getDocumentElement();
    if(!root.getNodeName().equals("XcodeProgram")){ // TODO const or enum
      return false;
    }

    if(!validateStringAttribute("1.0", "/XcodeProgram/@version")){
      System.err.println("Language is not set to fortran");
      return false;
    }

    if(!validateStringAttribute("Fortran", "/XcodeProgram/@language")){
      System.err.println("Language is not set to fortran");
      return false;
    }

    return true;
  }

  public void readTypeTable(){
    _typeTableElement = CLAWelementHelper.findTypeTable(_xcodemlDoc);
    NodeList nodeList = _typeTableElement.getElementsByTagName(XelementName.BASIC_TYPE);
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node n = nodeList.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        CLAWbasicType btype = new CLAWbasicType(el);
        _typeTable.put(btype.getType(), btype);
      }
    }
  }

  private boolean validateStringAttribute(String attrValue, String xpathQuery) throws Exception {
    XPathFactory xPathfactory = XPathFactory.newInstance();
    XPath xpath = xPathfactory.newXPath();
    XPathExpression getVersion = xpath.compile(xpathQuery);
    String outputValue = (String) getVersion.evaluate(_xcodemlDoc, XPathConstants.STRING);
    if(outputValue.equals(attrValue)){
      return true;
    }
    return false;
  }
}
