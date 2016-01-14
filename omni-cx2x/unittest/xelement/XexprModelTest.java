package xelement;

import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import helper.XmlHelper;
import cx2x.xcodeml.xelement.*;

public class XexprModelTest {

  private String dummyRootOpen = "<exprModel>";
  private String dummyRootClose = "</exprModel>";

  private String exprModel_IntConst = dummyRootOpen +
    "<FintConstant type=\"Fint\">16</FintConstant>" + dummyRootClose;

  private String exprModel_RealConst = dummyRootOpen +
    "<FrealConstant type=\"Freal\">9.81</FrealConstant>" + dummyRootClose;

  private String exprModel_ComplexConst = dummyRootOpen +
    "<FcomplexConstant type=\"Fcomplex\">" +
    "  <FrealConstant type=\"Freal\">1.0</FrealConstant>" +
    "  <FrealConstant type=\"Freal\">2.0</FrealConstant>" +
    "</FcomplexConstant>" + dummyRootClose;

  private String exprModel_LogConst = dummyRootOpen +
    "<FlogicalConstant type=\"Flogical\">.TRUE.</FlogicalConstant>" +
    dummyRootClose;

  private String exprModel_CharConst = dummyRootOpen +
    "<FcharacterConstant type=\"C7fca03c0d3c0\">value1: </FcharacterConstant>"
    + dummyRootClose;

  private String exprModel_Var = dummyRootOpen +
    "<Var type=\"Fint\" scope=\"local\">k</Var>" + dummyRootClose;

  private String exprModel_fctCall = dummyRootOpen +
    "<functionCall type=\"Fvoid\">" +
    "  <name type=\"F7fca03c08d80\">clawloop</name>" +
    "  <arguments>" +
    "    <Var type=\"A7fca03c07980\" scope=\"local\">value1</Var>" +
    "    <Var type=\"A7fca03c08230\" scope=\"local\">value2</Var>" +
    "  </arguments>" +
    "</functionCall>" + dummyRootClose;

  @Test
  public void findExprModelTest() {
    // XintConstant object
    Document xml = XmlHelper.loadXMLFromString(exprModel_IntConst);
    XbaseElement element = new XbaseElement(xml.getDocumentElement());
    assertNotNull(element);
    XexprModel model = XelementHelper.findExprModel(element);
    assertNotNull(model);
    assertTrue(model.isIntConst());

    // XrealConstant object
    xml = XmlHelper.loadXMLFromString(exprModel_RealConst);
    element = new XbaseElement(xml.getDocumentElement());
    assertNotNull(element);
    model = XelementHelper.findExprModel(element);
    assertNotNull(model);
    assertTrue(model.isRealConst());

    // XcomplexConstant object
    xml = XmlHelper.loadXMLFromString(exprModel_ComplexConst);
    element = new XbaseElement(xml.getDocumentElement());
    assertNotNull(element);
    model = XelementHelper.findExprModel(element);
    assertNotNull(model);
    assertTrue(model.isComplexConst());

    // XlogicalConstant object
    xml = XmlHelper.loadXMLFromString(exprModel_LogConst);
    element = new XbaseElement(xml.getDocumentElement());
    assertNotNull(element);
    model = XelementHelper.findExprModel(element);
    assertNotNull(model);
    assertTrue(model.isLogicalConst());

    // XcharacterConstant object
    xml = XmlHelper.loadXMLFromString(exprModel_CharConst);
    element = new XbaseElement(xml.getDocumentElement());
    assertNotNull(element);
    model = XelementHelper.findExprModel(element);
    assertNotNull(model);
    assertTrue(model.isCharConst());

    // Xvar object
    xml = XmlHelper.loadXMLFromString(exprModel_Var);
    element = new XbaseElement(xml.getDocumentElement());
    assertNotNull(element);
    model = XelementHelper.findExprModel(element);
    assertNotNull(model);
    assertTrue(model.isVar());

    // XfctCall
    xml = XmlHelper.loadXMLFromString(exprModel_fctCall);
    element = new XbaseElement(xml.getDocumentElement());
    assertNotNull(element);
    model = XelementHelper.findExprModel(element);
    assertNotNull(model);
    assertTrue(model.isFctCall());

  }

}
