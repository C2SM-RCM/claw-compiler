/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xnode;

/**
 * XcodeML/F element code from the specification 0.91J
 *
 * @author clementval
 */
public enum Xcode {
  ALLOC(Xname.ALLOC),                                        // XcodeML/F 6.30
  ARGUMENTS(Xname.ARGUMENTS),                                // XcodeML/F 7.5.2
  ARRAYINDEX(Xname.ARRAY_INDEX),                             // XcodeML/F 8.10
  FBASICTYPE(Xname.BASIC_TYPE),                              // XcodeML/F 3.3
  BODY(Xname.BODY),                                          // XcodeML/F 8.7
  CONDITION(Xname.CONDITION),                                // XcodeML/F 6.27
  CONTINUESTATEMENT(Xname.CONTINUE_STATEMENT),               // XcodeML/F 6.7
  COSHAPE(Xname.CO_SHAPE),                                   // XcodeML/F 3.2
  DECLARATIONS(Xname.DECLARATIONS),                          // XcodeML/F 5.2
  DEPENDS(Xname.DEPENDS),                                    // Xmod file
  ELSE(Xname.ELSE),                                          // XcodeML/F 6.29
  EXPRSTATEMENT(Xname.EXPR_STMT),                            // XcodeML/F 6.2
  EXTERNDECL(Xname.EXTERN_DECL),                             // XcodeML/F 5.6
  FUNCTIONCALL(Xname.FUNCTION_CALL),                         // XcodeML/F 7.5.1
  FALLOCATESTATEMENT(Xname.F_ALLOCATE_STATEMENT),            // XcodeML/F 6.24.1
  FARRAYCONSTRUCTOR(Xname.F_ARRAY_CONSTRUCTOR),              // XcodeML/F 7.2.1
  FARRAYREF(Xname.F_ARRAY_REF),                              // XcodeML/F 7.4.4
  FASSIGNSTATEMENT(Xname.F_ASSIGN_STMT),                     // XcodeML/F 6.1
  FBACKSPACESTATEMENT(Xname.F_BACKSPACE_STATEMENT),          // XcodeML/F 6.17.3
  FCASELABEL(Xname.F_CASE_LABEL),                            // XcodeML/F 6.14
  FCHARACTERREF(Xname.F_CHAR_REF),                           // XcodeML/F 7.4.5
  FCLOSESTATEMENT(Xname.F_CLOSE_STATEMENT),                  // XcodeML/F 6.17.5
  FCOARRAYREF(Xname.F_COARRAY_REF),                          // XcodeML/F 7.4.3
  FCOMMONDECL(Xname.F_COMMON_DECL),                          // XcodeML/F 6.22
  FCONTAINSSTATEMENT(Xname.F_CONTAINS_STATEMENT),            // XcodeML/F 6.26
  FCYCLESTATEMENT(Xname.F_CYCLE_STATEMENT),                  // XcodeML/F 6.8
  FDATADECL(Xname.F_DATA_DECL),                              // XcodeML/F 6.19
  FDEALLOCATESTATEMENT(Xname.F_DEALLOCATE_STATEMENT),        // XcodeML/F 6.24.2
  FDOLOOP(Xname.F_DO_LOOP),                                  // XcodeML/F 8.15
  FDOSTATEMENT(Xname.F_DO_STATEMENT),                        // XcodeML/F 6.5
  FDOWHILESTATEMENT(Xname.F_DO_WHILE_STATEMENT),             // XcodeML/F 6.6
  FENDFILESTATEMENT(Xname.F_ENDFILE_STATEMENT),              // XcodeML/F 6.17.3
  FENTRYDECL(Xname.F_ENTRY_DECL),                            // XcodeML/F 6.23
  FEQUIVALENCEDECL(Xname.F_EQUIVALENCE_DECL),                // XcodeML/F 6.21
  FEXITSTATEMENT(Xname.F_EXIT_STATEMENT),                    // XcodeML/F 6.9
  FFORMATDECL(Xname.F_FORMAT_DECL),                          // XcodeML/F 6.18
  FFUNCTIONDEFINITION(Xname.F_FUNCTION_DEFINITION),          // XcodeML/F 5.3
  FFUNCTIONTYPE(Xname.F_FUNCTION_TYPE),                      // XcodeML/F 3.4
  FIFSTATEMENT(Xname.F_IF_STMT),                             // XcodeML/F 6.4
  FINALPROCEDURE(Xname.FINAL_PROCEDURE),                     // XcodeML/F 3.12
  FINTCONSTANT(Xname.F_INT_CONST),                           // XcodeML/F 7.1.1
  FINTERFACEDECL(Xname.F_INTERFACE_DECL),                    // XcodeML/F 5.10
  FINQUIRESTATEMENT(Xname.F_INQUIRE_STATEMENT),              // XcodeML/F 6.17.6
  FREALCONSTANT(Xname.F_REAL_CONST),                         // XcodeML/F 7.1.2
  FCOMPLEXCONSTANT(Xname.F_COMPLEX_CONST),                   // XcodeML/F 7.1.1
  FCHARACTERCONSTANT(Xname.F_CHAR_CONST),                    // XcodeML/F 7.1.1
  FFUNCTIONDECL(Xname.F_FUNCTION_DECL),                      // XcodeML/F 5.12
  FLOGICALCONSTANT(Xname.F_LOGICAL_CONST),                   // XcodeML/F 7.1.1
  FMEMBERREF(Xname.F_MEMBER_REF),                            // XcodeML/F 7.4.2
  FMODULEDEFINITION(Xname.F_MODULE_DEFINITION),              // XcodeML/F 5.7
  FMODULEPROCEDUREDECL(Xname.F_MODULE_PROCEDURE_DECL),       // XcodeML/F 5.11
  FNAMELISTDECL(Xname.F_NAMELIST_DECL),                      // XcodeML/F 6.20
  FNULLIFYSTATEMENT(Xname.F_NULLIFY_STATEMENT),              // XcodeML/F 6.24.3
  FOPENSTATEMENT(Xname.F_OPEN_STATEMENT),                    // XcodeML/F 6.17.4
  FPOINTERASSIGNSTATEMENT(Xname.F_POINTER_ASSIGN_STATEMENT), // XcodeML/F 6.3
  FPRINTSTATEMENT(Xname.F_PRINT_STATEMENT),                  // XcodeML/F 6.17.2
  FREADSTATEMENT(Xname.F_READ_STATEMENT),                    // XcodeML/F 6.17.1
  FRETURNSTATEMENT(Xname.F_RETURN_STATEMENT),                // XcodeML/F 6.10
  FREWINDSTATEMENT(Xname.F_REWIND_STATEMENT),                // XcodeML/F 6.17.3
  FSELECTCASESTATEMENT(Xname.F_SELECT_CASE_STATEMENT),       // XcodeML/F 6.13
  FSTOPSTATEMENT(Xname.F_STOP_STATEMENT),                    // XcoceML 6.16
  FSTRUCTCONSTRUCTOR(Xname.F_STRUCT_CONSTRUCTOR),            // XcodeML/F 7.3.1
  FSTRUCTDECL(Xname.F_STRUCT_DECL),                          // XcodeML/F 5.5
  FSTRUCTTYPE(Xname.F_STRUCT_TYPE),                          // XcodeML/F 3.5
  FUSEDECL(Xname.F_USE_DECL),                                // XcodeML/F 5.8
  FUSEONLYDECL(Xname.F_USE_ONLY_DECL),                       // XcodeML/F 5.9
  FWHERESTATEMENT(Xname.F_WHERE_STATEMENT),                  // XcodeML/F 6.15
  FWRITESTATEMENT(Xname.F_WRITE_STATEMENT),                  // XcodeML/F 6.17.1
  GLOBALDECLARATIONS(Xname.GLOBAL_DECLARATIONS),             // XcodeML/F 5.1
  GLOBALSYMBOLS(Xname.GLOBAL_SYMBOLS),                       // XcodeML/F 4.1
  GOTOSTATEMENT(Xname.GOTO_STATEMENT),                       // XcodeML/F 6.11
  ID(Xname.ID),                                              // XcodeML/F 8.2
  IDENTIFIERS(Xname.IDENTIFIERS),                            // Xmod file
  INDEXRANGE(Xname.INDEX_RANGE),                             // XcodeML/F 8.11
  KIND(Xname.KIND),                                          // XcodeML/F 8.1
  LEN(Xname.LENGTH),                                         // XcodeML/F 8.6
  LOWERBOUND(Xname.LOWER_BOUND),                             // XcodeML/F 8.12
  NAME(Xname.NAME),                                          // XcodeML/F 8.3
  NAMEDVALUE(Xname.NAMED_VALUE),                             // XcodeML/F 8.16
  NAMEDVALUELIST(Xname.NAMED_VALUE_LIST),                    // XcodeML/F 8.17
  PARAMS(Xname.PARAMS),                                      // XcodeML/F 8.5
  FPRAGMASTATEMENT(Xname.PRAGMA_STMT),                       // XcodeML/F 6.25
  RENAME(Xname.RENAME),                                      // XcodeML/F 8.8
  RENAMABLE(Xname.RENAMABLE),                                // XcodeML/F 8.9
  SELECTTYPESTATEMENT(Xname.SELECT_TYPE_STATEMENT),          // XcodeML/F 6.34
  STATEMENTLABEL(Xname.STATEMENT_LABEL),                     // XcodeML/F 6.12
  STEP(Xname.STEP),                                          // XcodeML/F 8.14
  SYMBOLS(Xname.SYMBOLS),                                    // XcodeML/F 4.2
  THEN(Xname.THEN),                                          // XcodeML/F 6.28
  TYPEBOUNDGENERICPROCEDURE(Xname.TYPE_BOUND_GENERIC_PROCEDURE), // XcodeML 3.11
  TYPEBOUNDPROCEDURES(Xname.TYPE_BOUND_PROCEDURES),          // XcodeML/F 3.9
  TYPEBOUNDPROCEDURE(Xname.TYPE_BOUND_PROCEDURE),            // XcodeML/F 3.10
  TYPEGUARD(Xname.TYPE_GUARD),                               // XcodeML/F 6.53
  TYPETABLE(Xname.TYPE_TABLE),                               // XcodeML/F 3.1
  UPPERBOUND(Xname.UPPER_BOUND),                             // XcodeML/F 8.13
  VALUE(Xname.VALUE),                                        // XcodeML/F 8.4
  VALUELIST(Xname.VALUE_LIST),                               // XcodeML/F 8.18
  VAR(Xname.VAR),                                            // XcodeML/F 7.4.1
  VARDECL(Xname.VAR_DECL),                                   // XcodeML/F 5.4
  VARLIST(Xname.VAR_LIST),                                   // XcodeML/F 8.19
  VARREF(Xname.VAR_REF),                                     // XcodeML/F 7.4.6
  XCODEPROGRAM(Xname.X_CODE_PROGRAM),                        // XcodeML/F 2

  // Binary expression element
  DIVEXPR(Xname.DIV_EXPR),                                   // XcodeML/F 7.6
  FCONCATEXPR(Xname.F_CONCAT_EXPR),                          // XcodeML/F 7.6
  FPOWEREXPR(Xname.F_POWER_EXPR),                            // XcodeML/F 7.6
  LOGANDEXPR(Xname.LOG_AND_EXPR),                            // XcodeML/F 7.6
  LOGEQEXPR(Xname.LOG_EQ_EXPR),                              // XcodeML/F 7.6
  LOGEQVEXPR(Xname.LOG_EQV_EXPR),                            // XcodeML/F 7.6
  LOGGEEXPR(Xname.LOG_GE_EXPR),                              // XcodeML/F 7.6
  LOGGTEXPR(Xname.LOG_GT_EXPR),                              // XcodeML/F 7.6
  LOGLEEXPR(Xname.LOG_LE_EXPR),                              // XcodeML/F 7.6
  LOGLTEXPR(Xname.LOG_LT_EXPR),                              // XcodeML/F 7.6
  LOGNEQEXPR(Xname.LOG_NEQ_EXPR),                            // XcodeML/F 7.6
  LOGNEWVEXPR(Xname.LOG_NEWV_EXPR),                          // XcodeML/F 7.6
  LOGOREXPR(Xname.LOG_OR_EXPR),                              // XcodeML/F 7.6
  MINUSEXPR(Xname.MINUS_EXPR),                               // XcodeML/F 7.6
  MULEXPR(Xname.MUL_EXPR),                                   // XcodeML/F 7.6
  PLUSEXPR(Xname.PLUS_EXPR),                                 // XcodeML/F 7.6
  USERBINARYEXPR(Xname.USER_BINARY_EXPR),                    // XcodeML/F 7.6

  // Unary expression element
  LOGNOTEXPR(Xname.LOG_NOT_EXPR),                            // XcodeML/F 7.7
  UNARYMINUSEXPR(Xname.UNARY_MINUS_EXPR),                    // XcodeML/F 7.7
  USERUNARYEXPR(Xname.USER_UNARY_EXPR);                      // XcodeML/F 7.7

  private final String name;

  Xcode(String s) {
    name = s;
  }

  public static Xcode fromString(String value) {
    return Xcode.valueOf(value.toUpperCase());
  }

  public String toString() {
    return this.name;
  }

  /**
   * Get the XcodeML original code.
   *
   * @return XcodeML code.
   */
  public String code() {
    return name;
  }
}
