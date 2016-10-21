/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.language.common;

import cx2x.translator.common.ClawConstant;
import cx2x.xcodeml.xnode.Xcode;
import cx2x.xcodeml.xnode.Xnode;

/**
 * Store the information from a range option in a CLAW directives.
 *
 * @author clementval
 */
public class ClawRange {

  private String _inductionVar = null;
  private String _lowerBound = null;
  private String _upperBound = null;
  private String _step = ClawConstant.DEFAULT_STEP_VALUE;

  /**
   * Constructs null initialized ClawRange object.
   */
  public ClawRange() {
  }

  /**
   * Constructs a new ClawRange object with all parameters initialization.
   *
   * @param inductionVar The induction variable value.
   * @param lowerBound   The lower bound value.
   * @param upperBound   The upper bound value.
   * @param step         The step value.
   */
  public ClawRange(String inductionVar, String lowerBound, String upperBound,
                   String step)
  {
    setInductionVar(inductionVar);
    setLowerBound(lowerBound);
    setUpperBound(upperBound);
    setStep(step);
  }

  /**
   * Get the induction variable value.
   *
   * @return The induction variable value. Null if not defined.
   */
  public String getInductionVar() {
    return _inductionVar;
  }

  /**
   * Set the induction variable value.
   *
   * @param inductionVar The induction variable value.
   */
  public void setInductionVar(String inductionVar) {
    if(inductionVar != null) {
      this._inductionVar = inductionVar.trim();
    }
  }

  /**
   * Get the lower bound value.
   *
   * @return The lower bound value. Null if not defined.
   */
  public String getLowerBound() {
    return _lowerBound;
  }

  /**
   * Set the lower bound value.
   *
   * @param lowerBound The lower bound value.
   */
  public void setLowerBound(String lowerBound) {
    if(lowerBound != null) {
      this._lowerBound = lowerBound.trim();
    }
  }

  /**
   * Get the upper bound value.
   *
   * @return The upper bound value. Null if not set.
   */
  public String getUpperBound() {
    return _upperBound;
  }

  /**
   * Set the upper bound value.
   *
   * @param upperBound The upper bound value.
   */
  public void setUpperBound(String upperBound) {
    if(upperBound != null) {
      this._upperBound = upperBound.trim();
    }
  }

  /**
   * Get the step value.
   *
   * @return The step value. Null if not defined.
   */
  public String getStep() {
    return _step;
  }

  /**
   * Set the step value.
   *
   * @param step The step value.
   */
  public void setStep(String step) {
    if(step != null) {
      this._step = step.trim();
    }
  }

  /**
   * Compare a ClawRange with a do statement.
   *
   * @param doStmt The do statement to compare iteration range.
   * @return True if the iteration range share the same property.
   */
  public boolean equals(Xnode doStmt) {
    if(doStmt.opcode() != Xcode.FDOSTATEMENT) {
      return false;
    }

    Xnode inductionVar = doStmt.matchExactNode(Xcode.VAR);
    Xnode indexRange = doStmt.matchExactNode(Xcode.INDEXRANGE);
    Xnode lower = indexRange.matchExactNode(Xcode.LOWERBOUND).getChild(0);
    Xnode upper = indexRange.matchExactNode(Xcode.UPPERBOUND).getChild(0);
    Xnode step = indexRange.matchExactNode(Xcode.STEP).getChild(0);

    return !(inductionVar == null || _inductionVar == null
        || !inductionVar.getValue().equals(_inductionVar))
        && !(lower == null || _lowerBound == null
        || !lower.getValue().equals(_lowerBound))
        && !(upper == null || _upperBound == null
        || !upper.getValue().equals(_upperBound))
        && (step == null && _step == null
        || !(step == null || _step == null || !_step.equals(step.getValue())));
  }

}
