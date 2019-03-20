/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */
package claw.tatsu.primitive;

import claw.tatsu.xcodeml.abstraction.DimensionDefinition;
import claw.tatsu.xcodeml.abstraction.InsertionPosition;
import claw.tatsu.xcodeml.abstraction.PromotionInfo;
import claw.tatsu.xcodeml.xnode.common.*;
import claw.tatsu.xcodeml.xnode.fortran.FfunctionDefinition;
import claw.tatsu.xcodeml.xnode.fortran.FfunctionType;
import claw.tatsu.xcodeml.xnode.fortran.Xintrinsic;

import java.util.*;

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
   * Find specific argument in a function call.
   *
   * @param fctCall Function call node to search in.
   * @param argName Name of the argument to be found.
   * @return The argument if found. Null otherwise.
   */
  public static Xnode findArg(Xnode fctCall, String argName) {
    if(fctCall == null || fctCall.opcode() != Xcode.FUNCTION_CALL) {
      return null;
    }
    Xnode args = fctCall.matchSeq(Xcode.ARGUMENTS);
    if(args == null) {
      return null;
    }
    for(Xnode arg : args.children()) {
      if(argName.equalsIgnoreCase(arg.value())) {
        return arg;
      }
    }
    return null;
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
   * Detect all induction variables in the function body.
   *
   * @param fctDef Function definition to be checked.
   * @return Set of induction variables stored in a set.
   */
  public static Set<String> detectInductionVariables(FfunctionDefinition fctDef)
  {
    Set<String> inductionVariables = new HashSet<>();

    List<Xnode> doStatements = fctDef.body().matchAll(Xcode.F_DO_STATEMENT);

    for(Xnode doStatement : doStatements) {
      inductionVariables.add(Loop.extractInductionVariable(doStatement));
    }

    return inductionVariables;
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
        && args.prevSibling().opcode() == Xcode.NAME
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

    List<Xnode> nodes = xcodeml.matchAll(Xcode.F_MODULE_PROCEDURE_DECL);
    for(Xnode node : nodes) {
      Xnode nameNode = node.matchSeq(Xcode.NAME);
      if(nameNode != null
          && nameNode.value().equalsIgnoreCase(fctDef.getName()))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Extract the name of the function in a function call.
   *
   * @param fctCall Function call node.
   * @return Function name if can be extracted. Null otherwise.
   */
  public static String getFctNameFromFctCall(Xnode fctCall) {
    if(fctCall == null || fctCall.opcode() != Xcode.FUNCTION_CALL) {
      return null;
    }
    if(fctCall.firstChild().opcode() == Xcode.F_MEMBER_REF) {
      return fctCall.firstChild().getAttribute(Xattr.MEMBER);
    } else {
      return fctCall.matchSeq(Xcode.NAME).value();
    }
  }

  /**
   * Get the number of arguments in a function call.
   *
   * @param fctCall Function call to check.
   * @return Number of arguments in the function call. -1 if not a function
   * call.
   */
  public static int getNbOfArgsFromFctCall(Xnode fctCall) {
    if(fctCall == null || fctCall.opcode() != Xcode.FUNCTION_CALL) {
      return -1;
    }
    Xnode arguments = fctCall.matchDescendant(Xcode.ARGUMENTS);
    return arguments != null ? arguments.children().size() : 0;
  }

  /**
   * Check whether the function call is calling a type bound procedure.
   *
   * @param fctCall Function call node.
   * @return True if the function call is a type bound procedure call. False
   * otherwise.
   */
  public static boolean isCallToTypeBoundProcedure(Xnode fctCall) {
    if(fctCall == null || fctCall.opcode() != Xcode.FUNCTION_CALL) {
      return false;
    }
    return fctCall.firstChild().opcode() == Xcode.F_MEMBER_REF;
  }
}
