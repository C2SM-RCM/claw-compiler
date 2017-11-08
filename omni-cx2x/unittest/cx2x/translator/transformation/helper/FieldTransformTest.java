/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */
package cx2x.translator.transformation.helper;

import cx2x.translator.transformation.claw.parallelize.PromotionInfo;
import cx2x.xcodeml.exception.IllegalTransformationException;
import cx2x.xcodeml.language.DimensionDefinition;
import cx2x.xcodeml.language.InsertionPosition;
import cx2x.xcodeml.xnode.*;
import helper.TestConstant;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Test methods of FieldTransform class.
 *
 * @author clementval
 */
public class FieldTransformTest {

  @Test
  public void promoteTest() {
    DimensionDefinition dim1 = new DimensionDefinition("dim1", "1", "30");
    DimensionDefinition dim2 = new DimensionDefinition("dim2", "1", "40");

    List<DimensionDefinition> dimensions1 = Collections.singletonList(dim1);
    List<DimensionDefinition> dimensions2 = Arrays.asList(dim1, dim2);

    XcodeProgram xcodeml =
        XcodeProgram.createFromFile(TestConstant.TEST_PROMOTION);
    assertNotNull(xcodeml);

    List<XfunctionDefinition> fctDefs = xcodeml.getAllFctDef();
    assertEquals(1, fctDefs.size());

    XfunctionDefinition fctDef = fctDefs.get(0);
    assertEquals("sub1", fctDef.getName().value());

    // Scalar to array promotion with 1 additional dimension
    dim1.setInsertionPosition(InsertionPosition.BEFORE);
    performAndAssertPromotion("s1", dimensions1, fctDef, xcodeml, 0, 1,
        new int[]{1, 30});

    dim1.setInsertionPosition(InsertionPosition.IN_MIDDLE);
    performAndAssertPromotion("s2", dimensions1, fctDef, xcodeml, 0, 1,
        new int[]{1, 30});

    dim1.setInsertionPosition(InsertionPosition.AFTER);
    performAndAssertPromotion("s3", dimensions1, fctDef, xcodeml, 0, 1,
        new int[]{1, 30});

    // Scalar to array promotion with 2 additional dimension
    dim1.setInsertionPosition(InsertionPosition.BEFORE);
    dim2.setInsertionPosition(InsertionPosition.BEFORE);
    performAndAssertPromotion("s4", dimensions2, fctDef, xcodeml, 0, 2,
        new int[]{1, 30, 1, 40});

    dim1.setInsertionPosition(InsertionPosition.IN_MIDDLE);
    dim2.setInsertionPosition(InsertionPosition.IN_MIDDLE);
    performAndAssertPromotion("s5", dimensions2, fctDef, xcodeml, 0, 2,
        new int[]{1, 30, 1, 40});

    dim1.setInsertionPosition(InsertionPosition.AFTER);
    dim2.setInsertionPosition(InsertionPosition.AFTER);
    performAndAssertPromotion("s6", dimensions2, fctDef, xcodeml, 0, 2,
        new int[]{1, 30, 1, 40});

    // Promotion with 1 additional dimension
    dim1.setInsertionPosition(InsertionPosition.BEFORE);
    performAndAssertPromotion("a", dimensions1, fctDef, xcodeml, 2, 3,
        new int[]{1, 30, 1, 10, 1, 20});

    dim1.setInsertionPosition(InsertionPosition.AFTER);
    performAndAssertPromotion("b", dimensions1, fctDef, xcodeml, 2, 3,
        new int[]{1, 10, 1, 20, 1, 30});

    dim1.setInsertionPosition(InsertionPosition.IN_MIDDLE);
    performAndAssertPromotion("c", dimensions1, fctDef, xcodeml, 2, 3,
        new int[]{1, 10, 1, 30, 1, 20});

    // Promotion with 2 additional dimensions at the same position
    dim1.setInsertionPosition(InsertionPosition.BEFORE);
    dim2.setInsertionPosition(InsertionPosition.BEFORE);
    performAndAssertPromotion("d", dimensions2, fctDef, xcodeml, 2, 4,
        new int[]{1, 30, 1, 40, 1, 10, 1, 20});

    dim1.setInsertionPosition(InsertionPosition.AFTER);
    dim2.setInsertionPosition(InsertionPosition.AFTER);
    performAndAssertPromotion("e", dimensions2, fctDef, xcodeml, 2, 4,
        new int[]{1, 10, 1, 20, 1, 30, 1, 40});

    dim1.setInsertionPosition(InsertionPosition.IN_MIDDLE);
    dim2.setInsertionPosition(InsertionPosition.IN_MIDDLE);
    performAndAssertPromotion("f", dimensions2, fctDef, xcodeml, 2, 4,
        new int[]{1, 10, 1, 30, 1, 40, 1, 20});

    // Promotion with 2 additional dimensions at different position
    dim1.setInsertionPosition(InsertionPosition.IN_MIDDLE);
    dim2.setInsertionPosition(InsertionPosition.AFTER);
    performAndAssertPromotion("g", dimensions2, fctDef, xcodeml, 2, 4,
        new int[]{1, 10, 1, 30, 1, 20, 1, 40});

    dim1.setInsertionPosition(InsertionPosition.BEFORE);
    dim2.setInsertionPosition(InsertionPosition.IN_MIDDLE);
    performAndAssertPromotion("h", dimensions2, fctDef, xcodeml, 2, 4,
        new int[]{1, 30, 1, 10, 1, 40, 1, 20});

    dim1.setInsertionPosition(InsertionPosition.BEFORE);
    dim2.setInsertionPosition(InsertionPosition.AFTER);
    performAndAssertPromotion("i", dimensions2, fctDef, xcodeml, 2, 4,
        new int[]{1, 30, 1, 10, 1, 20, 1, 40});
  }

  /**
   * Perform the promotion transformation and assert its result.
   *
   * @param id         Identifier of the field to be promoted.
   * @param dims       Dimension definitions to use.
   * @param fctDef     Function definition in which the promotion is performed.
   * @param xcodeml    Current XcodeML translation unit.
   * @param base       Number of dimension before the promotion.
   * @param target     Number of dimension after the promotion.
   * @param dimensions Expected dimensions after promotion.
   */
  private void performAndAssertPromotion(String id,
                                         List<DimensionDefinition> dims,
                                         XfunctionDefinition fctDef,
                                         XcodeProgram xcodeml,
                                         int base, int target, int[] dimensions)
  {
    try {
      PromotionInfo promotionInfo = new PromotionInfo(id);
      promotionInfo.setDimensions(dims);

      Xnode decl = fctDef.getDeclarationTable().get(id);
      assertNotNull(decl);
      XbasicType bt;
      if(base != 0) {
        bt = xcodeml.getTypeTable().getBasicType(decl);
        assertNotNull(bt);
        assertTrue(bt.isArray());
        assertEquals(base, bt.getDimensions());
      } else {
        assertEquals(XbuiltInType.REAL,
            XbuiltInType.fromString(decl.getType()));
      }

      // Perform the promotion
      FieldTransform.promote2(promotionInfo, fctDef, xcodeml);
      decl = fctDef.getDeclarationTable().get(id);
      assertNotNull(decl);
      bt = xcodeml.getTypeTable().getBasicType(decl);
      assertNotNull(bt);
      assertTrue(bt.isArray());
      assertEquals(target, bt.getDimensions());
      assertEquals(target, dimensions.length / 2);
      assertEquals(target, promotionInfo.getTargetDimension());
      assertEquals(base, promotionInfo.getBaseDimension());
      assertEquals(target - base, promotionInfo.diffDimension());
      assertEquals(bt.getType(), promotionInfo.getTargetType());

      if(base > 0) {
        assertEquals(PromotionInfo.PromotionType.ARRAY_TO_ARRAY,
            promotionInfo.getPromotionType());
      } else {
        assertEquals(PromotionInfo.PromotionType.SCALAR_TO_ARRAY,
            promotionInfo.getPromotionType());
      }

      for(int i = 0; i < dimensions.length / 2; ++i) {
        assertDimension(bt.getDimensions(i), dimensions[i * 2],
            dimensions[(i * 2) + 1]);
      }
    } catch(IllegalTransformationException itex) {
      System.err.println(itex.getMessage());
      fail();
    }
  }

  /**
   * Assert information of a dimension.
   *
   * @param dimension  Node representing the dimension (indexRange node).
   * @param lowerBound Value of the lowerBound.
   * @param upperBound Value of the upperBound.
   */
  private void assertDimension(Xnode dimension, int lowerBound, int upperBound)
  {
    assertNotNull(dimension);
    assertEquals(Xcode.INDEXRANGE, dimension.opcode());
    Xnode lowerBoundNode = dimension.firstChild();
    assertNotNull(lowerBoundNode);
    Xnode upperBoundNode = dimension.lastChild();
    assertNotNull(upperBoundNode);
    assertEquals(Xcode.FINTCONSTANT, lowerBoundNode.firstChild().opcode());
    assertEquals(String.valueOf(lowerBound),
        lowerBoundNode.firstChild().value());
    assertEquals(Xcode.FINTCONSTANT, upperBoundNode.firstChild().opcode());
    assertEquals(String.valueOf(upperBound),
        upperBoundNode.firstChild().value());
  }

}
