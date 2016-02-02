/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

import static org.junit.Assert.*;

import helper.XmlHelper;
import org.junit.Test;

/**
 *
 *
 * @author clementval
 */
public class XtypeTableTest {
  private static String basicTypeTable =
      "<typeTable>" +
      "<FbasicType type=\"C2307e50\" ref=\"Fcharacter\">" +
      "<len>" +
      "<FintConstant type=\"Fint\">30</FintConstant>" +
      "</len>" +
      "</FbasicType>" +
      "<FfunctionType type=\"F23079f0\" return_type=\"Fvoid\" is_program=\"true\"/>" +
      "</typeTable>";


  @Test
  public void basicTypeTableTest(){
    XtypeTable typeTable = XmlHelper.createXtypeTableFromString(basicTypeTable);
    assertNotNull(typeTable);
    assertEquals(2, typeTable.count());
    assertTrue(typeTable.hasType("C2307e50"));
    Xtype type1 = typeTable.get("C2307e50");
    assertNotNull(type1);
    assertTrue(type1 instanceof XbasicType);
    XbasicType bType1 = (XbasicType)type1;
    assertFalse(bType1.hasIntent());
    assertFalse(bType1.hasKind());
    assertTrue(bType1.hasLength());
    assertEquals(XelementName.TYPE_F_CHAR, bType1.getRef());
    assertTrue(bType1.getLength().getExprModel().isIntConst());
    assertEquals("30", bType1.getLength().getExprModel().getIntConstant().getValue());

    assertTrue(typeTable.hasType("F23079f0"));
    Xtype type2 = typeTable.get("F23079f0");
    assertNotNull(type2);
    assertTrue(type2 instanceof XfunctionType);
    XfunctionType fType2 = (XfunctionType)type2;
    assertEquals(XelementName.TYPE_F_VOID, fType2.getReturnType());
    assertTrue(fType2.isProgram());
    assertFalse(fType2.isInternal());
    assertFalse(fType2.isRecursive());
    assertNull(fType2.getResultName());
  }
}
