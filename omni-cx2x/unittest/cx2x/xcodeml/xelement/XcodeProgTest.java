/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

import java.io.File;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;
import helper.XmlHelper;
import cx2x.xcodeml.xelement.XcodeProg;

/**
 * Test the features of the XcodeProg class
 *
 * @author clementval
 */

public class XcodeProgTest {

  // Path is relative to the test directory
  private static final String TEST_DATA = "./data/basic.xcml";

  @Test
  public void basicXcodeProgTest() {
    File f = new File(TEST_DATA);
    System.out.println("Working Directory = " +
        System.getProperty("user.dir"));
    assertTrue(f.exists());
    XcodeProg xcodeml = new XcodeProg(TEST_DATA);
    boolean loaded = xcodeml.load();
    assertTrue(loaded);
    assertEquals(8, xcodeml.getTypeTable().count());
    assertEquals(2, xcodeml.getGlobalSymbolsTable().count());
  }

}
