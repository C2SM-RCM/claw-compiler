/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.transformation.loop;

import cx2x.translator.language.ClawLanguage;
import cx2x.xcodeml.exception.IllegalTransformationException;
import cx2x.xcodeml.helper.XelementHelper;
import cx2x.xcodeml.transformation.BlockTransformation;
import cx2x.xcodeml.transformation.Transformation;
import cx2x.xcodeml.transformation.Transformer;
import cx2x.xcodeml.xelement.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A LoopHoist transformation is an independent transformation over a
 * structured block. It performs loop fusion in the given block as well as do
 * start statement hoisting.
 *
 * @author clementval
 */
public class LoopHoist extends BlockTransformation {

  private final List<LoopHoistDoStmtGroup> _doGroup;
  private final ClawLanguage _startClaw, _endClaw;
  private int _nestedLevel;
  private int _pragmaDepthLevel;

  /**
   * Constructs a new LoopHoist triggered from a specific directive.
   * @param startDirective The directive that triggered the loop hoist
   *                       transformation.
   * @param endDirective   The directive that end the structured block.
   */
  public LoopHoist(ClawLanguage startDirective, ClawLanguage endDirective) {
    super(startDirective, endDirective);
    _startClaw = startDirective;
    _endClaw = endDirective;
    _doGroup = new ArrayList<>();
  }

  /**
   * @see Transformation#analyze(XcodeProgram, Transformer)
   */
  @Override
  public boolean analyze(XcodeProgram xcodeml, Transformer transformer) {
    _pragmaDepthLevel = XelementHelper.getDepth(_startClaw.getPragma());
    _nestedLevel = _startClaw.getHoistInductionVars().size();

    // Find all the group of nested loops that can be part of the hoisting
    List<XdoStatement> statements =
        XelementHelper.findDoStatement(_startClaw.getPragma(),
            _endClaw.getPragma(), _startClaw.getHoistInductionVars());

    if(statements.size() == 0){
      xcodeml.addError("No do statement group meets the criteria of hoisting.",
          _startClaw.getPragma().getLineNo());
      return false;
    }

    for(int i = 0; i < statements.size(); i++){
      XdoStatement[] group = new XdoStatement[_nestedLevel];
      LoopHoistDoStmtGroup g = new LoopHoistDoStmtGroup(group);
      try {
        reloadDoStmts(g, statements.get(i));
      } catch (IllegalTransformationException e) {
        xcodeml.addError("Group " + i + " of do statements do not meet the" +
                " criteria of loop hoisting.",
            _startClaw.getPragma().getLineNo());
        return false;
      }

      LoopHoistDoStmtGroup crtGroup = new LoopHoistDoStmtGroup(group);
      int depth = XelementHelper.getDepth(group[0]);
      if(depth != _pragmaDepthLevel){
        XifStatement tmpIf = XelementHelper.findParentIfStatement(group[0]);
        if(tmpIf == null){
          xcodeml.addError("Group " + i + " is nested in an unsupported " +
              "statement for loop hoisting",
              _startClaw.getPragma().getLineNo());
        }
        int ifDepth = XelementHelper.getDepth(tmpIf);
        if (_pragmaDepthLevel <= ifDepth && ifDepth < depth){
          crtGroup.setExtraction();
        } else {
          xcodeml.addError("Group " + i + " is nested in an unsupported " +
              "statement for loop hoisting or depth is too high.",
              _startClaw.getPragma().getLineNo());
        }
      }
      _doGroup.add(crtGroup);
    }

    LoopHoistDoStmtGroup master = _doGroup.get(0);
    for (int i = 1; i < _doGroup.size(); ++i){
      LoopHoistDoStmtGroup next = _doGroup.get(i);
      for(int j = 0; j < master.getDoStmts().length; ++j){
        // Iteration range are identical, just merge
        if(j == 0
            && (!master.getDoStmts()[j].getIterationRange().
            isFullyIdentical(next.getDoStmts()[j].getIterationRange())
            && master.getDoStmts()[j].getIterationRange().
            isIdenticalBesidesLowerBound(next.getDoStmts()[j].
                getIterationRange())))
        { // Iteration range are identical besides lower-bound, if creation
          next.setIfStatement();
        } else if(!master.getDoStmts()[j].getIterationRange().
            isFullyIdentical(next.getDoStmts()[j].getIterationRange()))
        { // Iteration range are too different, stop analysis
          xcodeml.addError("Iteration range of do statements group " + i +
                  " differs from group 0. Loop hoisting aborted.",
              _startClaw.getPragma().getLineNo());
          return false;
        }
      }
    }
    return true;
  }




  /**
   * @see Transformation#canBeTransformedWith(Transformation)
   */
  @Override
  public boolean canBeTransformedWith(Transformation transformation) {
    return false;
  }

  /**
   * @see Transformation#transform(XcodeProgram, Transformer, Transformation)
   */
  @Override
  public void transform(XcodeProgram xcodeml, Transformer transformer,
                        Transformation transformation) throws Exception
  {
    for(LoopHoistDoStmtGroup g : _doGroup){
      if(g.needExtraction()){
        XifStatement ifStmt =
            XelementHelper.findParentIfStatement(g.getDoStmts()[0]);
        extractDoStmtFromIf(xcodeml, ifStmt, g);
      }
      if(g.needIfStatement()){
        createIfStatementForLowerBound(xcodeml, g);
      }
    }
  }

  /**
   *
   * @param xcodeml
   * @param ifStmt
   * @param g
   * @throws IllegalTransformationException
   */
  private void extractDoStmtFromIf(XcodeProgram xcodeml, XifStatement ifStmt,
                                   LoopHoistDoStmtGroup g)
      throws IllegalTransformationException
  {
    int nestedDepth = g.getDoStmts().length;
    XifStatement newIfStmt = ifStmt.cloneObject();
    XdoStatement newDoStmt = g.getDoStmts()[0].cloneObject();
    newIfStmt.getThen().getBody().delete();
    newIfStmt.getThen().
        appendToChildren(g.getDoStmts()[nestedDepth-1].getBody(), true);
    newDoStmt.getBody().delete();
    Xbody body = XelementHelper.createEmpty(Xbody.class, xcodeml);
    body.appendToChildren(newIfStmt, false);
    newDoStmt.appendToChildren(body, false);
    XelementHelper.insertAfter(ifStmt, newDoStmt);
    g.getDoStmts()[0].delete();
    ifStmt.delete();
    reloadDoStmts(g, newDoStmt);
  }

  private void createIfStatementForLowerBound(XcodeProgram xcodeml,
                                              LoopHoistDoStmtGroup g)
      throws IllegalTransformationException
  {
    int nestedDepth = g.getDoStmts().length;
    XifStatement ifStmt = XifStatement.create(xcodeml);
    XbinaryExpr cond =
        XelementHelper.createEmpty(XelementName.LOG_GE_EXPR, xcodeml);
    cond.appendToChildren(g.getDoStmts()[0].getIterationRange().getInductionVar(),
        true);
    cond.appendToChildren(g.getDoStmts()[0].getIterationRange().getIndexRange().
        getLowerBound().getExprModel().getElement(), true);

    ifStmt.getCondition().appendToChildren(cond, false);
    ifStmt.getThen().getBody().delete();
    ifStmt.getThen().
        appendToChildren(g.getDoStmts()[nestedDepth-1].getBody(), true);
    g.getDoStmts()[nestedDepth-1].getBody().delete();
    g.getDoStmts()[nestedDepth-1].appendToChildren(ifStmt, false);
  }


  private void reloadDoStmts(LoopHoistDoStmtGroup g, XdoStatement newStart)
      throws IllegalTransformationException
  {
    g.getDoStmts()[0] = newStart;
    for(int j = 1; j < g.getDoStmts().length; ++j){
      XdoStatement next = XelementHelper.findDoStatement(g.getDoStmts()[j-1].getBody(), false);
      if(next == null){
        throw new IllegalTransformationException("", _startClaw.getPragma().getLineNo());
      }
      g.getDoStmts()[j] = next;
    }
  }
}
