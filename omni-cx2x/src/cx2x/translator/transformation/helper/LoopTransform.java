/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */
package cx2x.translator.transformation.helper;

import cx2x.translator.common.ClawConstant;
import cx2x.translator.common.Utility;
import cx2x.xcodeml.exception.IllegalTransformationException;
import cx2x.xcodeml.helper.NestedDoStatement;
import cx2x.xcodeml.helper.XnodeUtil;
import cx2x.xcodeml.xnode.Xcode;
import cx2x.xcodeml.xnode.Xnode;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Low-level transformation applied on do statements. This included:
 * - loop fusion (merge)
 *
 * @author clementval
 */
public class LoopTransform {

  private static final String[] prevToDelete = {"acc loop", "omp do"};
  private static final String[] nextToDelete = {"omp end do"};

  /**
   * Merge two do statements together. Body of the slave do statement will be
   * append to the body of the master do statement.
   *
   * @param masterDoStmt Master do statement for the merge operation. Stay in
   *                     place. Other do statement will be merged into it.
   * @param slaveDoStmt  Slave do statement for the merge operation. Will be
   *                     merged into the master do statement.
   * @throws IllegalTransformationException If given node are null or not
   *                                        FdoStatement nodes.
   */
  private static void merge(Xnode masterDoStmt, Xnode slaveDoStmt)
      throws IllegalTransformationException
  {
    if(masterDoStmt == null || slaveDoStmt == null
        || masterDoStmt.opcode() != Xcode.FDOSTATEMENT
        || slaveDoStmt.opcode() != Xcode.FDOSTATEMENT)
    {
      throw new IllegalTransformationException(
          "Incompatible node to perform a merge");
    }

    // Merge slave body into the master body
    appendBody(masterDoStmt.body(), slaveDoStmt.body());

    // Delete any acc loop / omp do pragma before/after the do statements.
    cleanPragmas(slaveDoStmt, prevToDelete, nextToDelete);
    slaveDoStmt.delete();
  }

  /**
   * Merge two nested do statements group together. Inner most body of the slave
   * group is merged into the inner most body of the master group. Slave group
   * is then deleted.
   *
   * @param master Master nested do statements group for the merge operation.
   * @param slave  Slave nested do statements group for the merge operation.
   * @throws IllegalTransformationException If given node are null or not
   *                                        FdoStatement nodes.
   */
  public static void merge(NestedDoStatement master, NestedDoStatement slave)
      throws IllegalTransformationException
  {
    if(master == null || master.size() == 0 || slave == null
        || slave.size() == 0)
    {
      throw new IllegalTransformationException(
          "Incompatible node to perform a merge");
    }
    merge(master.getInnerStatement(), slave.getInnerStatement());
    slave.getOuterStatement().delete();
  }

  /**
   * Perform a do statements reordering based on the new order specified by
   * induction variables.
   * <p>
   * i,j,k -> k,j,i
   *
   * @param nestedGroup          The nested group of do statements to be
   *                             reordered.
   * @param newInductionVarOrder New order of the induction variables.
   *                             E.g. k,j,i.
   */
  public static void reorder(NestedDoStatement nestedGroup,
                             List<String> newInductionVarOrder)
      throws IllegalTransformationException
  {
    // Check that new order is possible
    if(nestedGroup.size() == 2) { // simple swap
      swapIterationRange(nestedGroup.getOuterStatement(),
          nestedGroup.getInnerStatement());
      Utility.debug("Loop reordering: single swap operation");
    } else if(nestedGroup.size() == 3) {
      int newPosition =
          nestedGroup.computeSwappingIndices(newInductionVarOrder);
      Utility.debug("Loop reordering: potential double swap operation " +
          newPosition);
      switch(newPosition) {
        case 201: // Double swap: i,j,k -> j,k,i
          swapIterationRange(nestedGroup.get(0), nestedGroup.get(1));
          swapIterationRange(nestedGroup.get(1), nestedGroup.get(2));
          break;
        case 120: // Double swap: i,j,k -> k,i,j
          swapIterationRange(nestedGroup.get(0), nestedGroup.get(2));
          swapIterationRange(nestedGroup.get(2), nestedGroup.get(1));
          break;
        case 21: // Single swap: i,j,k -> i,k,j
          swapIterationRange(nestedGroup.get(1), nestedGroup.get(2));
          break;
        case 210: // Single swap: i,j,k -> k,j,i
          swapIterationRange(nestedGroup.get(0), nestedGroup.get(2));
          break;
        case 102: // Single swap: i,j,k -> j,i,k
          swapIterationRange(nestedGroup.get(0), nestedGroup.get(1));
          break;
      }
    } else {
      throw new IllegalTransformationException("Currently unsupported " +
          "reorder operation.");
    }
  }

  /**
   * Swap the iteration range information of two do statement.
   *
   * @param e1 First do statement.
   * @param e2 Second do statement.
   * @throws IllegalTransformationException if necessary elements are missing
   *                                        to apply the transformation.
   */
  private static void swapIterationRange(Xnode e1, Xnode e2)
      throws IllegalTransformationException
  {
    // The two nodes must be do statement
    if(e1.opcode() != Xcode.FDOSTATEMENT || e2.opcode() != Xcode.FDOSTATEMENT) {
      throw new IllegalTransformationException("Only two do statement can be " +
          "swap iteration ranges.");
    }

    Xnode inductionVar1 = e1.matchDirectDescendant(Xcode.VAR);
    Xnode inductionVar2 = e2.matchDirectDescendant(Xcode.VAR);
    Xnode indexRange1 = e1.matchDirectDescendant(Xcode.INDEXRANGE);
    Xnode indexRange2 = e2.matchDirectDescendant(Xcode.INDEXRANGE);
    if(inductionVar1 == null || inductionVar2 == null ||
        indexRange1 == null || indexRange2 == null)
    {
      throw new IllegalTransformationException("Induction variable or index " +
          "range missing.");
    }

    Xnode low1 = indexRange1.matchSeq(Xcode.LOWERBOUND).child(0);
    Xnode up1 = indexRange1.matchSeq(Xcode.UPPERBOUND).child(0);
    Xnode s1 = indexRange1.matchSeq(Xcode.STEP).child(0);

    Xnode low2 = indexRange2.matchSeq(Xcode.LOWERBOUND).child(0);
    Xnode up2 = indexRange2.matchSeq(Xcode.UPPERBOUND).child(0);
    Xnode s2 = indexRange2.matchSeq(Xcode.STEP).child(0);

    // Set the range of loop2 to loop1
    inductionVar2.insertAfter(inductionVar1.cloneNode());
    low2.insertAfter(low1.cloneNode());
    up2.insertAfter(up1.cloneNode());
    s2.insertAfter(s1.cloneNode());

    inductionVar1.insertAfter(inductionVar2.cloneNode());
    low1.insertAfter(low2.cloneNode());
    up1.insertAfter(up2.cloneNode());
    s1.insertAfter(s2.cloneNode());

    inductionVar1.delete();
    inductionVar2.delete();
    low1.delete();
    up1.delete();
    s1.delete();
    low2.delete();
    up2.delete();
    s2.delete();
  }

  /**
   * Append the slave body to the master body.
   *
   * @param masterBody Master body node.
   * @param slaveBody  Slave body bode.
   * @throws IllegalTransformationException If given nodes are null or not body
   *                                        nodes.
   */
  private static void appendBody(Xnode masterBody, Xnode slaveBody)
      throws IllegalTransformationException
  {
    if(masterBody == null || masterBody.element() == null
        || slaveBody == null || slaveBody.element() == null
        || masterBody.opcode() != Xcode.BODY
        || slaveBody.opcode() != Xcode.BODY)
    {
      throw new IllegalTransformationException("Unable to append body.");
    }

    // Append content of slave body master body
    Node childNode = slaveBody.element().getFirstChild();
    while(childNode != null) {
      Node nextChild = childNode.getNextSibling();
      // Do something with childNode, including move or delete...
      if(childNode.getNodeType() == Node.ELEMENT_NODE) {
        masterBody.element().appendChild(childNode);
      }
      childNode = nextChild;
    }
  }

  /**
   * Clean up extra pragma that have no more sense after transformation.
   *
   * @param node     Do statement that will be removed.
   * @param previous List of pragma to be removed before the do statement.
   * @param next     List of pragmas to be removed after the do statement.
   */
  private static void cleanPragmas(Xnode node, String[] previous, String[] next)
  {
    if(node.opcode() != Xcode.FDOSTATEMENT) {
      return;
    }

    Xnode doStatement = node;

    while(node.prevSibling() != null
        && node.prevSibling().opcode() == Xcode.FPRAGMASTATEMENT) {
      String pragma = node.prevSibling().value();
      Xnode toDelete = null;

      for(String p : previous) {
        if(!pragma.startsWith(ClawConstant.CLAW) && pragma.contains(p)) {
          toDelete = node.prevSibling();
          break;
        }
      }

      node = node.prevSibling();
      XnodeUtil.safeDelete(toDelete);
    }

    node = doStatement; // Reset node to the initial position.
    while(node.nextSibling() != null
        && node.nextSibling().opcode() == Xcode.FPRAGMASTATEMENT) {
      String pragma = node.nextSibling().value();
      Xnode toDelete = null;

      for(String n : next) {
        if(!pragma.startsWith(ClawConstant.CLAW) && pragma.contains(n)) {
          toDelete = node.nextSibling();
          break;
        }
      }

      node = node.nextSibling();
      XnodeUtil.safeDelete(toDelete);
    }
  }
}
