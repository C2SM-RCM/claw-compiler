/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.transformation.claw.utility;

import cx2x.translator.language.base.ClawPragma;
import cx2x.translator.transformation.ClawTransformation;
import cx2x.xcodeml.helper.XnodeUtil;
import cx2x.xcodeml.transformation.Transformation;
import cx2x.xcodeml.transformation.Translator;
import cx2x.xcodeml.xnode.*;

import java.util.List;

/**
 * An array access to function call transformation replace the access to an
 * array value by a function call.
 *
 * @author clementval
 */
public class ArrayToFctCall extends ClawTransformation {

  private XfunctionDefinition _replaceFct;

  /**
   * ArrayToFctCall ctor.
   *
   * @param directive The directive that triggered the transformation.
   */
  public ArrayToFctCall(ClawPragma directive) {
    super(directive);
  }

  @Override
  public boolean analyze(XcodeProgram xcodeml, Translator translator) {
    XfunctionDefinition _fctDef = _claw.getPragma().findParentFunction();
    if(_fctDef == null) {
      xcodeml.addError("Cannot locate function definition.",
          _claw.getPragma().lineNo());
      return false;
    }

    if(!_fctDef.getDeclarationTable().contains(_claw.getArrayName())) {
      xcodeml.addError(_claw.getArrayName() +
              " is not declared in current function/subroutine.",
          _claw.getPragma().lineNo());
      return false;
    }

    _replaceFct = xcodeml.getGlobalDeclarationsTable().
        getFunctionDefinition(_claw.getFctName());
    if(_replaceFct == null) {
      XmoduleDefinition parentModule = _claw.getPragma().findParentModule();
      _replaceFct = parentModule.getFunctionDefinition(_claw.getFctName());

      if(_replaceFct == null) {
        xcodeml.addError("Function " + _claw.getFctName() +
                " not found in current file.",
            _claw.getPragma().lineNo());
        return false;
      }
    }

    // TODO does it make sense ?
   /*if(_replaceFct.getParams().count() != _claw.getFctParams().size()){
      xcodeml.addError("Function " + _claw.getFctName() +
              " parameters mismatch.",
          _claw.getPragma().lineNo());
      return false;
    }*/

    return true; // skeleton
  }

  /**
   * @return Always false as independent transformation are applied one by one.
   * @see Transformation#canBeTransformedWith(XcodeProgram, Transformation)
   */
  @Override
  public boolean canBeTransformedWith(XcodeProgram xcodeml,
                                      Transformation other)
  {
    return false; // independent transformation
  }

  @Override
  public void transform(XcodeProgram xcodeml, Translator translator,
                        Transformation other) throws Exception
  {

    XfunctionType fctType = xcodeml.getTypeTable().getFunctionType(_replaceFct);

    // Prepare the function call
    Xnode fctCall = xcodeml.createFctCall(fctType.getReturnType(),
        _claw.getFctName(), _replaceFct.getType());
    Xnode args = fctCall.matchSeq(Xcode.ARGUMENTS);
    for(String arg : _claw.getFctParams()) {
      args.append(xcodeml.createVar(XcodeType.INTEGER, arg, Xscope.LOCAL));
    }

    List<Xnode> refs =
        XnodeUtil.getAllArrayReferencesInSiblings(_claw.getPragma(),
            _claw.getArrayName());

    for(Xnode ref : refs) {
      ref.insertAfter(fctCall.cloneNode());
      ref.delete();
    }

    fctCall.delete();
    removePragma();
    transformed();
  }
}
