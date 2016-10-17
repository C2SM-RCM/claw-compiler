/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xnode;

import helper.XmlHelper;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test the features of the XglobalDecl class.
 *
 * @author clementval
 */
public class XglobalDeclTest {

  private static final String simpleGlobDecl =
      "<globalDeclarations>" +
          "<FmoduleDefinition name=\"module\" lineno=\"4\" " +
          "file=\"./src/module.f90\">" +
          "</FmoduleDefinition>" +
          "<FfunctionDefinition lineno=\"917\" file=\"./src/module.f90\">\n" +
          "<name type=\"Fb39d60\">fct1</name>" +
          "<symbols></symbols>" +
          "<declarations></declarations>" +
          "<body></body>" +
          "</FfunctionDefinition>" +
          "</globalDeclarations>";


  @Test
  public void simpleGlobalDeclarationTest() {
    XglobalDeclTable gdTable = XmlHelper.createGlobalDeclTable(simpleGlobDecl);
    assertNotNull(gdTable);
    assertEquals(2, gdTable.count());
    assertTrue(gdTable.hasDefinition("fct1"));
    assertTrue(gdTable.hasFunctionDefinition("fct1"));
    assertFalse(gdTable.hasModuleDefinition("fct1"));
    XfunctionDefinition fDef = gdTable.getFctDefinition("fct1");
    assertNotNull(fDef);
    assertEquals("fct1", fDef.getName().getValue());
    assertNotNull(fDef.getBody());
    assertNotNull(fDef.getDeclarationTable());
    assertNull(fDef.getParams());
    assertNotNull(fDef.getSymbolTable());
    assertEquals(917, fDef.getLineNo());
    assertEquals("./src/module.f90", fDef.getFile());
    assertTrue(gdTable.hasDefinition("module"));
    assertFalse(gdTable.hasFunctionDefinition("module"));
    assertTrue(gdTable.hasModuleDefinition("module"));
    XmoduleDefinition mDef = gdTable.getModuleDefinition("module");
    assertNotNull(mDef);
    assertEquals("module", mDef.getName());
    assertEquals(4, mDef.getLineNo());
    assertEquals("./src/module.f90", mDef.getFile());
  }
}
