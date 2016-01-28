/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.pragma;

import static org.junit.Assert.*;

import cx2x.translator.common.Constant;
import cx2x.translator.pragma.ClawMapping;
import cx2x.xcodeml.exception.IllegalDirectiveException;
import org.junit.Test;

import cx2x.translator.pragma.ClawPragma;

import java.util.ArrayList;
import java.util.List;

/**
 * Test the features of the ClawPragma enum.
 *
 * @author clementval
 */

public class ClawPragmaTest {

  @Test
  public void isValidTest() {
    // loop-fusion
    assertTrue(ClawPragma.isValid("claw loop-fusion"));
    assertTrue(ClawPragma.isValid("claw loop-fusion group(g1)"));
    assertTrue(ClawPragma.isValid("claw loop-fusion group( g1 )"));
    assertTrue(ClawPragma.isValid("claw loop-fusion group ( g1   ) "));
    assertFalse(ClawPragma.isValid("claw loop-fusiongroup(g1)"));
    assertFalse(ClawPragma.isValid("claw loop-fusion group"));
    assertFalse(ClawPragma.isValid("claw loop-fusion (i,j,k)"));
    assertFalse(ClawPragma.isValid("claw loop-fusion group()"));
    assertFalse(ClawPragma.isValid("claw loop-fusion group(   )"));

    // loop-interchange
    assertTrue(ClawPragma.isValid("claw loop-interchange"));
    assertTrue(ClawPragma.isValid("claw loop-interchange (i,j,k)"));
    assertTrue(ClawPragma.isValid("claw loop-interchange (  i,j,k  ) "));
    assertFalse(ClawPragma.isValid("claw loop-interchange ()"));
    assertFalse(ClawPragma.isValid("claw loop-interchange (  )"));

    // loop-extract
    assertTrue(ClawPragma.isValid("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i)"));
    assertTrue(ClawPragma.isValid("claw loop-extract range(i=istart,iend) map(value1,value2:i)"));
    assertTrue(ClawPragma.isValid("claw loop-extract range(i=istart,iend) map(value1, value2:i)"));
    assertFalse(ClawPragma.isValid("claw loop-extract range(i=istart,iend) map(value1, value2)"));
    assertFalse(ClawPragma.isValid("claw loop-extract range(i=istart,iend) map(:i)"));
    assertFalse(ClawPragma.isValid("claw loop-extract range(i=istart,iend)"));
    assertFalse(ClawPragma.isValid("claw loop-extract map(value1:i)"));
    assertFalse(ClawPragma.isValid("claw loop-extract range() map(value1:i)"));
    assertFalse(ClawPragma.isValid("claw loop-extract range(i=istart,iend) map()"));
    assertFalse(ClawPragma.isValid("claw loop-extract range() map()"));
    assertFalse(ClawPragma.isValid("claw loop-extract range map"));


    assertTrue(ClawPragma.isValid("claw loop-extract range(j1=ki1sc,ki1ec) "
        + " map(pduh2oc,pduh2of:j1,ki3sc/j3) "
        + " map(pduco2,pduo3,palogp,palogt,podsc,podsf,podac,podaf:j1,ki3sc/j3)"
        + " map(pbsff,pbsfc:j1,ki3sc/j3) map(pa1c,pa1f,pa2c,pa2f,pa3c,pa3f:j1) "
        + " fusion group(j1)"));


    // remove
    assertTrue(ClawPragma.isValid("claw remove"));
    assertTrue(ClawPragma.isValid("claw end remove"));

    // invalid dummy directives
    assertFalse(ClawPragma.isValid("claw"));
    assertFalse(ClawPragma.isValid("claw dummy-directive"));


  }

  @Test
  public void parallelOptionTest(){
    assertTrue(ClawPragma.hasParallelOption("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i) parallel "));
    assertFalse(ClawPragma.hasParallelOption("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i)"));
  }

  @Test
  public void accOptionTest(){
    assertEquals("loop gang vector", ClawPragma.getAccOptionValue("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i) parallel acc(loop gang vector)"));
    assertEquals("loop seq", ClawPragma.getAccOptionValue("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i) parallel acc(loop seq)"));
    assertNull(ClawPragma.getAccOptionValue("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i) parallel acc()"));
    assertNull(ClawPragma.getAccOptionValue("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i)"));
  }

  @Test
  public void extractMappingTest(){
    List<ClawMapping> mappings = null;
    try {
       mappings = ClawPragma.extractMappingInformation(
          "claw loop-extract range(j1=ki1sc,ki1ec) "
              + " map(pduh2oc,pduh2of:j1,ki3sc/j3) "
              + " map(pduco2,pduo3,palogp,palogt,podsc,podsf,podac,podaf:j1,ki3sc/j3)"
              + " map(pbsff,pbsfc:j1,ki3sc/j3) map(pa1c,pa1f,pa2c,pa2f,pa3c,pa3f:j1) "
              + " fusion group(j1)");
    } catch (IllegalDirectiveException ide){
      fail();
    }

    assertNotNull(mappings);
    assertEquals(4, mappings.size());

    assertEquals(2, mappings.get(0).getMappedDimensions());
    assertEquals(2, mappings.get(0).getMappedVariables().size());
    assertEquals(2, mappings.get(0).getMappingVariables().size());


    assertEquals(2, mappings.get(1).getMappedDimensions());
    assertEquals(8, mappings.get(1).getMappedVariables().size());
    assertEquals(2, mappings.get(1).getMappingVariables().size());


    assertEquals(2, mappings.get(2).getMappedDimensions());
    assertEquals(2, mappings.get(2).getMappedVariables().size());
    assertEquals(2, mappings.get(2).getMappingVariables().size());


    assertEquals(1, mappings.get(3).getMappedDimensions());
    assertEquals(6, mappings.get(3).getMappedVariables().size());
    assertEquals(1, mappings.get(3).getMappingVariables().size());

  }

  @Test
  public void getGroupOptionTest(){
    assertEquals("g1", ClawPragma.getGroupOptionValue("claw loop-fusion group (g1)"));
    assertEquals("g1", ClawPragma.getGroupOptionValue("claw loop-fusion group (g1) "));
    assertEquals("g1", ClawPragma.getGroupOptionValue("claw loop-fusion group ( g1  )  "));
    assertNull(ClawPragma.getGroupOptionValue("claw loop-fusion group()"));
    assertNull(ClawPragma.getGroupOptionValue("claw loop-fusion group(  )"));
    assertNull(ClawPragma.getGroupOptionValue("claw loop-fusion"));
  }

  @Test
  public void getExtractFusionOptionTest(){
    assertEquals("j1", ClawPragma.getExtractFusionOption("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i) fusion group(j1) parallel acc(loop seq)"));
    assertEquals("j1", ClawPragma.getExtractFusionOption("claw loop-extract range(i=istart,iend) map(value1:i) map(value2:i) parallel acc(loop seq) fusion group(j1)"));
  }

  @Test
  public void extractRangeInformationTest(){
    ClawRange r1 = ClawPragma.extractRangeInformation("claw loop-extract range(i=istart,iend)");
    assertNotNull(r1);
    assertEquals("i", r1.getInductionVar());
    assertEquals("istart", r1.getLowerBound());
    assertEquals("iend", r1.getUpperBound());
    assertEquals(Constant.DEFAULT_STEP_VALUE, r1.getStep());

    ClawRange r2 = ClawPragma.extractRangeInformation("claw loop-extract range(i=1,10)");
    assertNotNull(r2);
    assertEquals("i", r2.getInductionVar());
    assertEquals("1", r2.getLowerBound());
    assertEquals("10", r2.getUpperBound());
    assertEquals(Constant.DEFAULT_STEP_VALUE, r2.getStep());

    ClawRange r3 = ClawPragma.extractRangeInformation("claw loop-extract range(i=1,10,2 )");
    assertNotNull(r3);
    assertEquals("i", r3.getInductionVar());
    assertEquals("1", r3.getLowerBound());
    assertEquals("10", r3.getUpperBound());
    assertEquals("2", r3.getStep());


    ClawRange r4 = ClawPragma.extractRangeInformation("claw loop-extract range(i=istart, iend , 10)");
    assertNotNull(r4);
    assertEquals("i", r4.getInductionVar());
    assertEquals("istart", r4.getLowerBound());
    assertEquals("iend", r4.getUpperBound());
    assertEquals("10", r4.getStep());

    ClawRange r5 = ClawPragma.extractRangeInformation("claw loop-extract range()");
    assertNull(r5);

    ClawRange r6 = ClawPragma.extractRangeInformation("claw loop-extract range(i=istart,iend) map(a:j1)");
    assertNotNull(r6);
    assertEquals("i", r6.getInductionVar());
    assertEquals("istart", r6.getLowerBound());
    assertEquals("iend", r6.getUpperBound());
    assertEquals(Constant.DEFAULT_STEP_VALUE, r1.getStep());
  }
}
