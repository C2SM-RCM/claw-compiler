/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */
package cx2x.translator.transformation.primitive;

import cx2x.xcodeml.xnode.Xattr;
import cx2x.xcodeml.xnode.Xcode;
import cx2x.xcodeml.xnode.Xnode;

import java.util.List;

/**
 * Primitive transformation and test on Ranges.
 * - comparison of indexRange and list of indexRanges.
 *
 * @author clementval
 */
public final class Range {

  // Avoid instantiation of this class
  private Range() {
  }

  /**
   * Compare two list of indexRange.
   *
   * @param list1 First list of indexRange.
   * @param list2 Second list of indexRange.
   * @return True if the indexRange at the same position in the two list are all
   * identical. False otherwise.
   */
  public static boolean compare(List<Xnode> list1, List<Xnode> list2)
  {
    if(list1.size() != list2.size()) {
      return false;
    }

    for(int i = 0; i < list1.size(); ++i) {
      if(!compare(list1.get(i), list2.get(i), true)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compare if two indexRange nodes are identical.
   *
   * @param idx1           First indexRange node.
   * @param idx2           Second indexRange node.
   * @param withLowerBound If true, compare lower bound. If false, lower bound
   *                       is not compared.
   * @return True if the index range are identical.
   */
  public static boolean compare(Xnode idx1, Xnode idx2, boolean withLowerBound)
  {
    if(idx1 == null || idx2 == null || idx1.opcode() != Xcode.INDEXRANGE
        || idx2.opcode() != Xcode.INDEXRANGE)
    {
      return false;
    }

    if(idx1.getBooleanAttribute(Xattr.IS_ASSUMED_SHAPE) &&
        idx2.getBooleanAttribute(Xattr.IS_ASSUMED_SHAPE))
    {
      return true;
    }

    Xnode low1 = idx1.matchSeq(Xcode.LOWERBOUND);
    Xnode up1 = idx1.matchSeq(Xcode.UPPERBOUND);
    Xnode low2 = idx2.matchSeq(Xcode.LOWERBOUND);
    Xnode up2 = idx2.matchSeq(Xcode.UPPERBOUND);
    Xnode s1 = idx1.matchSeq(Xcode.STEP);
    Xnode s2 = idx2.matchSeq(Xcode.STEP);

    if(s1 != null) {
      s1 = s1.firstChild();
    }
    if(s2 != null) {
      s2 = s2.firstChild();
    }

    if(withLowerBound) {
      return low1.compareFirstChildValues(low2) &&
          up1.compareFirstChildValues(up2)
          && (s1 == null || s1.compareOptionalValues(s2));
    } else {
      return up1.compareFirstChildValues(up2)
          && (s1 == null || s1.compareOptionalValues(s2));
    }
  }

}
