/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.language.helper;

import cx2x.translator.language.ClawLanguage;
import cx2x.translator.language.ClawReshapeInfo;
import cx2x.translator.transformation.loop.LoopFusion;
import cx2x.translator.transformation.loop.LoopInterchange;
import cx2x.xcodeml.exception.IllegalTransformationException;
import cx2x.xcodeml.helper.XnodeUtil;
import cx2x.xcodeml.transformation.Transformer;
import cx2x.xcodeml.xnode.*;
import xcodeml.util.XmOption;

import java.util.List;

/**
 * The class TransformationHelper contains only static method to help the
 * generation of additional transformation described as clause in main
 * directive.
 *
 * @author clementval
 */
public class TransformationHelper {


  /**
   * Generate corresponding additional transformation according to optional
   * clauses given to the directive.
   * @param claw        ClawLanguage object that tells encapsulates all
   *                    information about the current directives and its
   *                    clauses.
   * @param xcodeml     Current XcodeML program.
   * @param transformer Transformer object in which new transformation are
   *                    added.
   * @param stmt        Statement on which the transformation is attached.
   */
  public static void generateAdditionalTransformation(ClawLanguage claw,
                                                      XcodeProgram xcodeml,
                                                      Transformer transformer,
                                                      Xnode stmt)
  {
    // Order doesn't matter
    applyFusionClause(claw, transformer, stmt);
    applyInterchangeClause(claw, xcodeml, transformer, stmt);
  }


  /**
   * Generate loop fusion transformation if the clause is present in the
   * directive.
   * @param claw        ClawLanguage object that tells encapsulates all
   *                    information about the current directives and its
   *                    clauses.
   * @param transformer Transformer object in which new transformation are
   *                    added.
   * @param stmt        Statement on which the transformation is attached. Must
   *                    be a FdoStatement for the loop fusion transformation.
   */
  private static void applyFusionClause(ClawLanguage claw,
                                        Transformer transformer,
                                        Xnode stmt)
  {
    if(claw.hasFusionClause() && stmt.Opcode() == Xcode.FDOSTATEMENT){
      ClawLanguage l = ClawLanguage.createLoopFusionLanguage(claw);
      // TODO XNODE stmt should be directily an Xnode
      LoopFusion fusion = new LoopFusion(stmt, l);
      // TODO maybe run analysis
      transformer.addTransformation(fusion);

      if(XmOption.isDebugOutput()){
        System.out.println("Loop fusion added: " + claw.getGroupValue());
      }
    }
  }

  /**
   * Generate loop interchange transformation if the clause is present in the
   * directive.
   * @param claw        ClawLanguage object that tells encapsulates all
   *                    information about the current directives and its
   *                    clauses.
   * @param xcodeml     Current XcodeML program.
   * @param transformer Transformer object in which new transformation are
   *                    added.
   * @param stmt        Statement on which the transformation is attached. Must
   *                    be a FdoStatement for the loop interchange
   *                    transformation.
   */
  private static void applyInterchangeClause(ClawLanguage claw,
                                             XcodeProgram xcodeml,
                                             Transformer transformer,
                                             Xnode stmt)
  {
    if(claw.hasInterchangeClause() && stmt.Opcode() == Xcode.FDOSTATEMENT){
      Xnode p = new Xnode(Xcode.FPRAGMASTATEMENT, xcodeml);
      XnodeUtil.insertBefore(stmt, p);
      ClawLanguage l = ClawLanguage.createLoopInterchangeLanguage(claw, p);
      LoopInterchange interchange = new LoopInterchange(l);
      transformer.addTransformation(interchange);
      if(XmOption.isDebugOutput()){
        System.out.println("Loop interchange added: " + claw.getIndexes());
      }
    }
  }


  /**
   * Apply the reshape clause transformation.
   * @param claw    The claw language object holding the reshape information.
   * @param xcodeml The current XcodeML program.
   * @throws IllegalTransformationException when reshape information are not
   * valid or an new element cannot be created.
   */
  public static void applyReshapeClause(ClawLanguage claw,
                                        XcodeProgram xcodeml)
      throws IllegalTransformationException
  {
    if(!claw.hasReshapeClause()){
      return;
    }
    XfunctionDefinition fctDef =
        XnodeUtil.findParentFunction(claw.getPragma());
    if(fctDef == null){
      throw new IllegalTransformationException("Cannot apply reshape clause." +
          "Parent function definition not found.",claw.getPragma().getLineNo());
    }

    for(ClawReshapeInfo reshapeInfo : claw.getReshapeClauseValues()){
      Xid id = fctDef.getSymbolTable().get(reshapeInfo.getArrayName());
      XvarDecl decl =
          fctDef.getDeclarationTable().get(reshapeInfo.getArrayName());

      String crtTypeHash = id.getType();

      Xtype rawType = xcodeml.getTypeTable().get(crtTypeHash);
      if(!(rawType instanceof XbasicType)){
        throw new IllegalTransformationException(
            String.format("Reshape variable %s is not a basic type.",
                reshapeInfo.getArrayName()),
            claw.getPragma().getLineNo()
        );
      }
      XbasicType crtType = (XbasicType)rawType;

      // Check dimension
      if(crtType.getDimensions() < reshapeInfo.getTargetDimension()){
        throw new IllegalTransformationException(
            String.format(
                "Reshape variable %s has smaller dimension than requested.",
                reshapeInfo.getArrayName()
            ), claw.getPragma().getLineNo()
        );
      }

      // Create new type
      XbasicType newType = crtType.cloneObject();
      newType.setType(xcodeml.getTypeTable().generateRealTypeHash());
      if(reshapeInfo.getTargetDimension() == 0){ // Demote to scalar
        newType.resetDimension();
      } else { // Demote to smaller dimension array

        if(crtType.getDimensions() - reshapeInfo.getKeptDimensions().size() !=
            reshapeInfo.getTargetDimension()){
          throw new IllegalTransformationException(
              String.format("Reshape information for %s not valid. " +
                      "Target dimension and kept dimension mismatch.",
                  reshapeInfo.getArrayName())
          );
        }
        newType.removeDimension(reshapeInfo.getKeptDimensions());
      }
      xcodeml.getTypeTable().add(newType);

      // Update symbol & declaration
      id.setType(newType.getType());
      decl.getName().setAttribute(Xattr.TYPE, newType.getType());

      // Update array references
      List<Xnode> refs =
          XnodeUtil.getAllArrayReferences(fctDef.getBody(),
              reshapeInfo.getArrayName());

      for(Xnode ref : refs){
        if(reshapeInfo.getTargetDimension() == 0){
          XnodeUtil.demoteToScalar(ref);
        } else {
          XnodeUtil.demote(ref, reshapeInfo.getKeptDimensions());
        }
      }
    }
  }

}
