/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */
package claw.tatsu.primitive;

import claw.tatsu.xcodeml.abstraction.DimensionDefinition;
import claw.tatsu.xcodeml.abstraction.FunctionCall;
import claw.tatsu.xcodeml.abstraction.InsertionPosition;
import claw.tatsu.xcodeml.abstraction.PromotionInfo;
import claw.tatsu.xcodeml.xnode.common.*;
import claw.tatsu.xcodeml.xnode.fortran.FfunctionDefinition;
import claw.tatsu.xcodeml.xnode.fortran.FfunctionType;
import claw.tatsu.xcodeml.xnode.fortran.Xintrinsic;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Primitive transformation, test and utility for Function related action.
 * This includes:
 * - Find arguments in function call.
 * - Find id in function definition or in ancestor.
 *
 * @author clementval
 */
public final class Function {

  // Avoid instantiation of this class
  private Function() {
  }

  /**
   * Find the id element in the current function definition or in parent
   * function definition if nested.
   *
   * @param fctDef Function definition.
   * @param name   Id name to be searched for.
   * @return The id if found. Null otherwise.
   */
  public static Xid findId(FfunctionDefinition fctDef, String name) {
    if(fctDef == null) {
      return null;
    }

    if(fctDef.getSymbolTable().contains(name)) {
      return fctDef.getSymbolTable().get(name);
    }
    FfunctionDefinition upperDef = fctDef.findParentFunction();
    if(upperDef == null) {
      return null;
    }
    return findId(upperDef, name);
  }

  /**
   * Find the declaration element in the current function definition or in
   * parent if nested.
   *
   * @param fctDef Current function definition.
   * @param name   Declaration name to be searched for.
   * @return The element if found. Null otherwise.
   */
  public static Xnode findDecl(FfunctionDefinition fctDef, String name) {
    if(fctDef.getSymbolTable().contains(name)) {
      return fctDef.getDeclarationTable().get(name);
    }
    FfunctionDefinition upperDef = fctDef.findParentFunction();
    if(upperDef == null) {
      return null;
    }
    return findDecl(upperDef, name);
  }

  /**
   * Read the promotion information stored in function type.
   *
   * @param fctType           Function type to read from.
   * @param insertionPosition Insertion position to be applied. Null to keep
   *                          original insertion position.
   * @return Promotion information object with read information.
   */
  public static PromotionInfo readPromotionInfo(FfunctionType fctType,
                                                InsertionPosition
                                                    insertionPosition)
  {
    PromotionInfo defaultInfo = new PromotionInfo();
    for(Xnode param : fctType.getParameters()) {
      if(param.hasAttribute(Xattr.PROMOTION_INFO)) {
        defaultInfo.
            readDimensionsFromString(param.getAttribute(Xattr.PROMOTION_INFO));
        break;
      }
    }
    if(insertionPosition != null && defaultInfo.getDimensions() != null) {
      for(DimensionDefinition dim : defaultInfo.getDimensions()) {
        dim.setInsertionPosition(insertionPosition);
      }
    }
    return defaultInfo;
  }

  /**
   * Check if the given element is argument of a function call for a given
   * intrinsic function name.
   *
   * @param var  Element to be checked.
   * @param name Intrinsic function name as Xintrinsic.
   * @return True if the element is an argument. False otherwise.
   */
  public static boolean isArgOfFunction(Xnode var, Xintrinsic name) {
    if(var == null) {
      return false;
    }

    Xnode args = var.matchAncestor(Xcode.ARGUMENTS);
    return (args != null && args.prevSibling() != null
        && Xnode.isOfCode(args.prevSibling(), Xcode.NAME)
        && args.prevSibling().value().equalsIgnoreCase(name.toString()));
  }

  /**
   * Check if the function definition is a module procedure.
   *
   * @param fctDef  Function definition to check.
   * @param xcodeml Current XcodeML translation unit.
   * @return True if it is a module procedure. False otherwise.
   */
  public static boolean isModuleProcedure(FfunctionDefinition fctDef,
                                          XcodeProgram xcodeml)
  {
    if(fctDef == null || xcodeml == null) {
      return false;
    }

    return xcodeml.matchAll(Xcode.F_MODULE_PROCEDURE_DECL).stream()
        .filter(x -> x.matchSeq(Xcode.NAME) != null)
        .map(x -> x.matchSeq(Xcode.NAME))
        .map(Xnode::value).anyMatch(fctDef.getName()::equalsIgnoreCase);
  }

  /**
   * Find function definition node from a function call node.
   *
   * @param xcodeml Current XcodeML translation unit.
   * @param fctDef  Function definition encapsulating the function call.
   * @param fctCall Function call to find the definition.
   * @return The function definition if found. Null otherwise.
   */
  public static Optional<FfunctionDefinition> findFunctionDefinitionFromFctCall(
      XcodeProgram xcodeml, FfunctionDefinition fctDef, FunctionCall fctCall)
  {
    FfunctionDefinition calledFctDef = xcodeml.getGlobalDeclarationsTable()
        .getFunctionDefinition(fctCall.getFctName());
    if(calledFctDef == null) {
      Xnode meaningfulParentNode = fctDef.findParentModule();
      if(meaningfulParentNode == null) { // fct is not a module child
        meaningfulParentNode = fctDef.matchAncestor(Xcode.GLOBAL_DECLARATIONS);
      }

      return meaningfulParentNode.matchAll(Xcode.F_FUNCTION_DEFINITION).stream()
          .map(FfunctionDefinition::new)
          .filter(x -> x.getName().equalsIgnoreCase(fctCall.getFctName()))
          .findFirst();
    }
    return Optional.empty();
  }

}
