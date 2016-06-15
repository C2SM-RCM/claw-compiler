/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

import cx2x.xcodeml.xnode.Xattr;
import cx2x.xcodeml.xnode.Xcode;
import cx2x.xcodeml.xnode.Xnode;
import helper.XmlHelper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test features of basic elements
 *
 * @author clementval
 */
public class XbasicTest {

  private static final String name1 = "<name type=\"Fint\">a</name>";

  private static final String value1 =
      "<value>" +
      "<FintConstant type=\"Fint\">1</FintConstant>\n" +
      "</value>";


  @Test
  public void xValueTest(){
    Xnode val = XmlHelper.createXnode(value1);
    assertNotNull(val);
    assertTrue(val.getChild(0).Opcode() == Xcode.FINTCONSTANT);
    assertEquals(XelementName.TYPE_F_INT,
        val.getChild(0).getAttribute(Xattr.TYPE));
    assertEquals("1", val.getChild(0).getValue());
  }

  @Test
  public void xNameTest(){
    Xnode name = XmlHelper.createXnode(name1);
    assertNotNull(name);
    assertEquals(XelementName.TYPE_F_INT, name.getAttribute(Xattr.TYPE));
    assertEquals("a", name.getValue());
  }

}
