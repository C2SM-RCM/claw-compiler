/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.transformation.loop;

import cx2x.translator.language.base.ClawLanguage;
import cx2x.translator.language.common.ClawReshapeInfo;
import cx2x.translator.language.helper.TransformationHelper;
import cx2x.translator.transformation.ClawBlockTransformation;
import cx2x.translator.transformation.helper.LoopTransform;
import cx2x.xcodeml.helper.HoistedNestedDoStatement;
import cx2x.xcodeml.helper.XnodeUtil;
import cx2x.xcodeml.transformation.Transformation;
import cx2x.xcodeml.transformation.Translator;
import cx2x.xcodeml.xnode.Xcode;
import cx2x.xcodeml.xnode.XcodeProgram;
import cx2x.xcodeml.xnode.XfunctionDefinition;
import cx2x.xcodeml.xnode.Xnode;

import java.util.ArrayList;
import java.util.List;

/**
 * A LoopHoist transformation is an independent transformation over a
 * structured block. It performs loop fusion in the given block as well as do
 * start statement hoisting.
 *
 * @author clementval
 */
public class LoopHoist extends ClawBlockTransformation {

  private final List<HoistedNestedDoStatement> _hoistedGroups;

  /**
   * Constructs a new LoopHoist triggered from a specific directive.
   *
   * @param startDirective The directive that triggered the loop hoist
   *                       transformation.
   * @param endDirective   The directive that end the structured block.
   */
  public LoopHoist(ClawLanguage startDirective, ClawLanguage endDirective) {
    super(startDirective, endDirective);
    _hoistedGroups = new ArrayList<>();
  }

  /**
   * @see Transformation#analyze(XcodeProgram, Translator)
   */
  @Override
  public boolean analyze(XcodeProgram xcodeml, Translator translator) {
    int _pragmaDepthLevel = _clawStart.getPragma().depth();

    // Find all the group of nested loops that can be part of the hoisting
    List<HoistedNestedDoStatement> statements =
        XnodeUtil.findDoStatementForHoisting(_clawStart.getPragma(),
            _clawEnd.getPragma(), _clawStart.getHoistInductionVars());

    if(statements.size() == 0) {
      xcodeml.addError("No do statement group meets the criteria of hoisting.",
          _clawStart.getPragma().lineNo());
      return false;
    }

    for(HoistedNestedDoStatement hoistedNestedDoStmt : statements) {
      int depth = hoistedNestedDoStmt.getOuterStatement().depth();
      if(depth != _pragmaDepthLevel) {
        Xnode tmpIf = hoistedNestedDoStmt.getOuterStatement().
            matchAncestor(Xcode.FIFSTATEMENT);
        Xnode tmpSelect = hoistedNestedDoStmt.getOuterStatement().
            matchAncestor(Xcode.FSELECTCASESTATEMENT);
        Xnode tmpDo = hoistedNestedDoStmt.getOuterStatement().
            matchAncestor(Xcode.FDOSTATEMENT);

        if(tmpIf == null && tmpSelect == null && tmpDo == null) {
          xcodeml.addError("A nested group is nested in an unsupported " +
                  "statement for loop hoisting.",
              _clawStart.getPragma().lineNo());
          return false;
        }

        int ifDepth =
            (tmpIf != null) ? tmpIf.depth() : Xnode.UNDEF_DEPTH;
        int selectDepth =
            (tmpSelect != null) ? tmpSelect.depth() : Xnode.UNDEF_DEPTH;
        int doDepth =
            (tmpDo != null) ? tmpDo.depth() : Xnode.UNDEF_DEPTH;

        if((_pragmaDepthLevel <= ifDepth || _pragmaDepthLevel <= selectDepth
            || _pragmaDepthLevel <= doDepth)
            && (ifDepth < depth || selectDepth < depth || doDepth < depth))
        {
          hoistedNestedDoStmt.setExtraction();
        } else {
          xcodeml.addError("Group is nested in an unsupported " +
                  "statement for loop hoisting or depth is too high " +
                  "(Group index starts at 0).",
              _clawStart.getPragma().lineNo());
          return false;
        }
      }
      _hoistedGroups.add(hoistedNestedDoStmt);
    }
    
    HoistedNestedDoStatement master = _hoistedGroups.get(0);
    for(int i = 1; i < _hoistedGroups.size(); ++i) {
      HoistedNestedDoStatement next = _hoistedGroups.get(i);
      for(int j = 0; j < master.size(); ++j) {
        // Iteration range are identical, just merge
        if(j == 0 && (!XnodeUtil.hasSameIndexRange(master.get(j), next.get(j))
            && XnodeUtil.hasSameIndexRangeBesidesLower(master.get(j),
            next.get(j))))
        {
          // Iteration range are identical besides lower-bound, if creation
          next.setIfStatement();
        } else if(!XnodeUtil.hasSameIndexRange(master.get(j), next.get(j))) {
          // Iteration range are too different, stop analysis
          xcodeml.addError("Iteration range of do statements group " + i +
                  " differs from group 0. Loop hoisting aborted.",
              _clawStart.getPragma().lineNo());
          return false;
        }
      }
    }

    // Check reshape mandatory points
    if(_clawStart.hasReshapeClause()) {
      XfunctionDefinition fctDef = _clawStart.getPragma().findParentFunction();
      if(fctDef == null) {
        xcodeml.addError("Unable to matchSeq the function/subroutine/module " +
                "definition including the current directive",
            _clawStart.getPragma().lineNo()
        );
        return false;
      }

      for(ClawReshapeInfo r : _clawStart.getReshapeClauseValues()) {
        if(!fctDef.getSymbolTable().contains(r.getArrayName()) ||
            !fctDef.getDeclarationTable().contains(r.getArrayName()))
        {
          // Check in the parent def if present
          if(!checkUpperDefinition(fctDef, r.getArrayName())) {
            xcodeml.addError(String.format("Reshape variable %s not found in " +
                    "the definition of %s", r.getArrayName(),
                fctDef.getName().value()), _clawStart.getPragma().lineNo()
            );
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Check whether the id is present in the parent function definition if the
   * current fct definition is nested.
   *
   * @param fctDef Current function definition.
   * @param name   Id to be looked for.
   * @return True if the id has been found. False otherwise.
   */
  private boolean checkUpperDefinition(XfunctionDefinition fctDef, String name)
  {
    XfunctionDefinition upperDef = fctDef.findParentFunction();
    return upperDef != null
        && (!(!upperDef.getSymbolTable().contains(name)
        || !upperDef.getDeclarationTable().contains(name))
        || checkUpperDefinition(upperDef, name)
    );
  }

  /**
   * @return Always false as independent transformation are applied one by one.
   * @see Transformation#canBeTransformedWith(XcodeProgram, Transformation)
   */
  @Override
  public boolean canBeTransformedWith(XcodeProgram xcodeml,
                                      Transformation transformation)
  {
    return false; // Independent transformation
  }

  /**
   * @see Transformation#transform(XcodeProgram, Translator, Transformation)
   */
  @Override
  public void transform(XcodeProgram xcodeml, Translator translator,
                        Transformation transformation) throws Exception
  {
    HoistedNestedDoStatement hoisted = LoopTransform.hoist(_hoistedGroups,
        _clawStart.getPragma(), _clawEnd.getPragma(), xcodeml);

    // Generate dynamic transformation (interchange)
    TransformationHelper.generateAdditionalTransformation(_clawStart,
        xcodeml, translator, hoisted.getOuterStatement());

    // Apply reshape clause
    TransformationHelper.applyReshapeClause(_clawStart, xcodeml);

    removePragma();
  }
}
