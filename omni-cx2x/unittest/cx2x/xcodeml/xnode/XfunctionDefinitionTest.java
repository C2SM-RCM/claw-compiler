/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xnode;

import helper.TestConstant;
import helper.XmlHelper;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test features of the XfunctionDefinition class.
 *
 * @author clementval
 */
public class XfunctionDefinitionTest {

  private static final String basicFDef = "<FfunctionDefinition lineno=\"1\" " +
      "file=\"original_code.f90\">" +
      "<name type=\"F7ff951406df0\">force_dummy</name>" +
      "<symbols>" +
      "</symbols>" +
      "<declarations>" +
      "</declarations>" +
      "<body>" +
      "<FprintStatement format=\"*\" lineno=\"3\" file=\"original_code.f90\">" +
      "<valueList>" +
      "<value>" +
      "<FcharacterConstant type=\"C7ff9514071e0\">Dummy program for force " +
      "option</FcharacterConstant>" +
      "</value>" +
      "</valueList>" +
      "</FprintStatement>" +
      "</body>" +
      "</FfunctionDefinition>";

  @Test
  public void basicFunctionDefinitionTest() {
    XfunctionDefinition fctDef =
        XmlHelper.createXfunctionDefinitionFromString(basicFDef);
    assertNotNull(fctDef);
    assertEquals("force_dummy", fctDef.getName());
    assertEquals("F7ff951406df0", fctDef.getType());
    assertEquals(0, fctDef.getSymbolTable().size());
    assertEquals(0, fctDef.getDeclarationTable().count());
    assertNull(fctDef.getParams());
    assertNotNull(fctDef.body());

    assertEquals(1, fctDef.lineNo());
    assertEquals("original_code.f90", fctDef.filename());

    assertNull(fctDef.findContainingXmod());

    XfunctionDefinition clone = fctDef.cloneNode();
    assertNotNull(clone);
    assertEquals("force_dummy", fctDef.getName());
  }

  @Test
  public void findContainingXmodTest() {
    File f = new File(TestConstant.TEST_DECLARATIONS);
    assertTrue(f.exists());
    XcodeProgram xcodeml =
        XcodeProgram.createFromFile(TestConstant.TEST_DECLARATIONS);
    List<XfunctionDefinition> fctDefs = xcodeml.getAllFctDef();
    for(XfunctionDefinition fctDef : fctDefs) {
      // Search paths is not set so module cannot be found.
      assertNull(fctDef.findContainingXmod());
    }
  }
}
