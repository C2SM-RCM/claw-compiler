/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.language;

import cx2x.xcodeml.xnode.*;

/**
 * Class holding information about defined dimension.
 *
 * @author clementval
 */
public class DimensionDefinition {

  public static final String BASE_DIM = ":";

  private final BoundDefinition _lowerBound;
  private final BoundDefinition _upperBound;
  private final String _identifier;

  /**
   * Constructs a new dimension object from the extracted information.
   *
   * @param id         Identifier of the defined dimension.
   * @param lowerBound Lower bound of the dimension.
   * @param upperBound Upper bound of the dimension.
   *                   TODO maybe add step information (in the grammar as well)
   */
  public DimensionDefinition(String id, String lowerBound, String upperBound) {
    _identifier = id;
    _lowerBound = new BoundDefinition(lowerBound);
    _upperBound = new BoundDefinition(upperBound);
  }

  public BoundDefinition getLowerBound() {
    return _lowerBound;
  }

  public BoundDefinition getUpperBound() {
    return _upperBound;
  }


  /**
   * Get the identifier for the current dimension.
   *
   * @return The identifier of the current dimension.
   */
  public String getIdentifier() {
    return _identifier;
  }


  /**
   * Generate the correct indexRange element with lowerBound, upperBound and
   * step from the current dimension.
   *
   * @param xcodeml  Current XcodeML program unit in which elements will be
   *                 created.
   * @param withStep IF true, step element is created.
   * @return A new indexRange elements.
   */
  public Xnode generateIndexRange(XcodeML xcodeml, boolean withStep) {
    Xnode range = xcodeml.createNode(Xcode.INDEXRANGE);
    Xnode lower = xcodeml.createNode(Xcode.LOWERBOUND);
    Xnode upper = xcodeml.createNode(Xcode.UPPERBOUND);
    range.append(lower);
    range.append(upper);

    if(withStep) {
      Xnode step = xcodeml.createNode(Xcode.STEP);
      Xnode stepValue = xcodeml.createNode(Xcode.FINTCONSTANT);
      stepValue.setAttribute(Xattr.TYPE, Xname.TYPE_F_INT);
      step.append(stepValue);
      stepValue.setValue(Xname.DEFAULT_STEP_VALUE);
      range.append(step);
    }

    lower.append(_lowerBound.generate(xcodeml));
    upper.append(_upperBound.generate(xcodeml));
    return range;
  }

  /**
   * Generate the array index that will be placed in the array reference for
   * this additional dimension.
   *
   * @param xcodeml Current XcodeML program unit.
   * @return A new arrayIndex element including a var element with the dimension
   * identifier.
   */
  public Xnode generateArrayIndex(XcodeProgram xcodeml) {
    Xnode aIdx = xcodeml.createNode(Xcode.ARRAYINDEX);
    Xnode var = xcodeml.createVar(Xname.TYPE_F_INT, _identifier, Xscope.LOCAL);
    aIdx.append(var);
    return aIdx;
  }

  /**
   * Generate additional node to be inserted in the allocate statement
   * arrayIndex based on the dimension definition.
   *
   * @param xcodeml Current XcodeML program unit.
   * @return New node to be inserted in arrayIndex of allocate statement.
   */
  public Xnode generateAllocateNode(XcodeProgram xcodeml) {
    // TODO handle special size with lowerBound != 1
    Xnode arrayIndex = xcodeml.createNode(Xcode.ARRAYINDEX);
    arrayIndex.append(_upperBound.generate(xcodeml));
    return arrayIndex;
  }

}
