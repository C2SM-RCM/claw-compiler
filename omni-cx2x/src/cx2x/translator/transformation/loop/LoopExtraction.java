/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.transformation.loop;

// Cx2x import
import cx2x.translator.common.Constant;
import cx2x.translator.language.ClawLanguage;
import cx2x.translator.language.ClawMapping;
import cx2x.translator.language.ClawMappingVar;
import cx2x.translator.language.helper.TransformationHelper;
import cx2x.translator.language.helper.accelerator.AcceleratorHelper;
import cx2x.translator.misc.Utility;
import cx2x.xcodeml.helper.*;
import cx2x.xcodeml.xelement.*;
import cx2x.xcodeml.transformation.*;
import cx2x.xcodeml.exception.*;

// OMNI import
import cx2x.xcodeml.xnode.Xattr;
import cx2x.xcodeml.xnode.Xcode;
import cx2x.xcodeml.xnode.Xnode;
import xcodeml.util.XmOption;

// Java import
import java.util.*;

/**
 * A LoopExtraction transformation is an independent transformation. The
 * transformation consists of locating a loop in a function call and extract it.
 * This loop is then wrapped around the function call and the parameters are
 * demoted accordingly to the mapping options.
 *
 * @author clementval
 */

public class LoopExtraction extends Transformation {

  private final ClawLanguage _claw;
  private final Map<String, ClawMapping> _fctMappingMap;
  private final Map<String, ClawMapping> _argMappingMap;
  private Xnode _fctCall = null;
  private Xnode _extractedLoop = null;
  private XfunctionDefinition _fctDef = null; // Fct holding the fct call
  private XfunctionDefinition _fctDefToExtract = null;


  /**
   * Constructs a new LoopExtraction triggered from a specific pragma.
   * @param directive  The directive that triggered the loop extraction
   *                   transformation.
   * @throws IllegalDirectiveException if something is wrong in the directive's
   * options
   */
  public LoopExtraction(ClawLanguage directive) throws IllegalDirectiveException {
    super(directive);
    _claw = directive;
    _argMappingMap = new Hashtable<>();
    _fctMappingMap = new Hashtable<>();

    try {
      extractMappingInformation();
    } catch (IllegalDirectiveException ide){
      ide.setDirectiveLine(directive.getPragma().getLineNo());
      throw ide;
    }
  }

  /**
   * Extract all mapping information from the pragma data. Each
   * map(<mapped>:<mapping>) produces a ClawMapping object.
   */
  private void extractMappingInformation() throws IllegalDirectiveException {
    for(ClawMapping m : _claw.getMappings()){
      for(ClawMappingVar mappedVar : m.getMappedVariables()){
        if(_argMappingMap.containsKey(mappedVar.getArgMapping())){
          throw new IllegalDirectiveException(_claw.getPragma().getValue(),
              mappedVar + " appears more than once in the mapping");
        } else {
          _argMappingMap.put(mappedVar.getArgMapping(), m);
        }
        if(_fctMappingMap.containsKey(mappedVar.getFctMapping())){
          throw new IllegalDirectiveException(_claw.getPragma().getValue(),
              mappedVar + " appears more than once in the mapping");
        } else {
          _fctMappingMap.put(mappedVar.getFctMapping(), m);
        }
      }
    }
  }

  /**
   * Check whether the provided mapping information are correct or not. A
   * mapped variable should only appear once. Mapped variable must be parameters
   * in the function definition.
   * Mapping using the same mapping variables are merged together.
   * @return True if all the conditions are respected. False otherwise.
   */
  private boolean checkMappingInformation(XcodeProgram xcodeml){
    for(Map.Entry<String, ClawMapping> map : _argMappingMap.entrySet()){
      if(XelementHelper.findArg(map.getKey(), _fctCall) == null){
        xcodeml.addError("Mapped variable " + map.getKey() +
            " not found in function call arguments",
            _claw.getPragma().getLineNo());
        return false;
      }
    }

    return true;
  }

  /**
   * Check whether the transformation can be applied.
   * @param xcodeml      The XcodeML on which the transformations are applied.
   * @param transformer  The transformer used to applied the transformations.
   * @return True if the transformation analysis succeeded. False otherwise.
   */
  @Override
  public boolean analyze(XcodeProgram xcodeml, Transformer transformer){
    Xnode _exprStmt =
        XelementHelper.findNext(Xcode.EXPRSTATEMENT, _claw.getPragma());
    if(_exprStmt == null){
      xcodeml.addError("No function call detected after loop-extract",
        _claw.getPragma().getLineNo());
      return false;
    }

    // Find function CALL
    _fctCall = XelementHelper.find(Xcode.FUNCTIONCALL, _exprStmt, true);
    if(_fctCall == null){
      xcodeml.addError("No function call detected after loop-extract",
        _claw.getPragma().getLineNo());
      return false;
    }

    Xnode fctDef = XelementHelper.findParent(Xcode.FFUNCTIONDEFINITION, _fctCall);
    if(fctDef == null){
      xcodeml.addError("No function around the fct call",
        _claw.getPragma().getLineNo());
      return false;
    }
    _fctDef = new XfunctionDefinition(fctDef.getElement());

    // Find function declaration
    _fctDefToExtract = XelementHelper.findFunctionDefinition(xcodeml, _fctCall);

    if(_fctDefToExtract == null){
      xcodeml.addError("Could not locate the function definition for: "
          + _fctCall.findNode(Xcode.NAME).getValue(),
          _claw.getPragma().getLineNo());
      return false;
    }

    // Find the loop to be extracted
    try {
      _extractedLoop = locateDoStatement(_fctDefToExtract);
    } catch (IllegalTransformationException itex){
      xcodeml.addError(itex.getMessage(),
          _claw.getPragma().getLineNo());
      return false;
    }

    return checkMappingInformation(xcodeml);
  }

  /**
   * Apply the transformation. A loop extraction is applied in the following
   * steps:
   *  1) Duplicate the function targeted by the transformation
   *  2) Extract the loop body in the duplicated function and remove the loop.
   *  3) Adapt function call and demote array references in the duplicated
   *     function body.
   *  4) Optional: Add a LoopFusion transformation to the transformaions' queue.
   *
   * @param xcodeml        The XcodeML on which the transformations are applied.
   * @param transformer    The transformer used to applied the transformations.
   * @param transformation Only for dependent transformation. The other
   *                       transformation part of the transformation.
   * @throws IllegalTransformationException if the transformation cannot be
   * applied.
   */
  @Override
  public void transform(XcodeProgram xcodeml, Transformer transformer,
                        Transformation transformation) throws Exception
  {

    /*
     * DUPLICATE THE FUNCTION
     */

    // Duplicate function definition
    XfunctionDefinition clonedFctDef = _fctDefToExtract.cloneObject();
    String newFctTypeHash = xcodeml.getTypeTable().generateFctTypeHash();
    String newFctName = clonedFctDef.getName().getValue() + Constant.EXTRACTION_SUFFIX +
        transformer.getNextTransformationCounter();
    clonedFctDef.getName().setValue(newFctName);
    clonedFctDef.getName().setAttribute(Xattr.TYPE, newFctTypeHash);
    // Update the symbol table in the fct definition
    Xid fctId = clonedFctDef.getSymbolTable()
        .get(_fctDefToExtract.getName().getValue());
    fctId.setType(newFctTypeHash);
    fctId.setName(newFctName);

    // Get the fctType in typeTable
    XfunctionType fctType = (XfunctionType)xcodeml
      .getTypeTable().get(_fctDefToExtract.getName().getAttribute(Xattr.TYPE));
    XfunctionType newFctType = fctType.cloneObject();
    newFctType.setType(newFctTypeHash);
    xcodeml.getTypeTable().add(newFctType);

    // Get the id from the global symbols table
    Xid globalFctId = xcodeml.getGlobalSymbolsTable()
      .get(_fctDefToExtract.getName().getValue());

    // If the fct is define in the global symbol table, duplicate it
    if(globalFctId != null){
      Xid newFctId = globalFctId.cloneObject();
      newFctId.setType(newFctTypeHash);
      newFctId.setName(newFctName);
      xcodeml.getGlobalSymbolsTable().add(newFctId);
    }

    // Insert the duplicated function declaration
    XelementHelper.insertAfter(_fctDefToExtract, clonedFctDef);

    // Find the loop that will be extracted
    Xnode loopInClonedFct = locateDoStatement(clonedFctDef);

    if(XmOption.isDebugOutput()){
      System.out.println("loop-extract transformation: " +
          _claw.getPragma().getValue());
      System.out.println("  created subroutine: " +
          clonedFctDef.getName().getValue());
    }

    /*
     * REMOVE BODY FROM THE LOOP AND DELETE THE LOOP
     */

    // 1. append body into fct body after loop
    XelementHelper.extractBody(loopInClonedFct);
    // 2. delete loop
    loopInClonedFct.delete();


    /*
     * ADAPT FUNCTION CALL AND DEMOTE ARRAY REFERENCES IN THE BODY
     * OF THE FUNCTION
     */

    // Wrap function call with loop
    Xnode extractedLoop = wrapCallWithLoop(xcodeml, _extractedLoop);

    if(XmOption.isDebugOutput()){
      System.out.println("  call wrapped with loop: " +
          _fctCall.findNode(Xcode.NAME).getValue() + " --> " +
          clonedFctDef.getName().getValue());
    }

    // Change called fct name
    _fctCall.findNode(Xcode.NAME).setValue(newFctName);
    _fctCall.findNode(Xcode.NAME).setAttribute(Xattr.TYPE, newFctTypeHash);


    // Adapt function call parameters and function declaration
    XdeclTable fctDeclarations = clonedFctDef.getDeclarationTable();
    XsymbolTable fctSymbols = clonedFctDef.getSymbolTable();

    Utility.debug("  Start to apply mapping: " + _claw.getMappings().size());

    for(ClawMapping mapping : _claw.getMappings()){
      Utility.debug("Apply mapping (" + mapping.getMappedDimensions() + ") ");
      for(ClawMappingVar var : mapping.getMappedVariables()){
        Utility.debug("  Var: " + var);
        Xnode argument = XelementHelper.findArg(var.getArgMapping(), _fctCall);
        if(argument == null) {
          continue;
        }

        /* Case 1: Var --> ArrayRef
         * Var --> ArrayRef transformation
         * 1. Check that the variable used as array index exists in the
         *    current scope (XdeclTable). If so, get its type value. Create a
         *    Var element for the arrayIndex. Create the arrayIndex element
         *    with Var as child.
         *
         * 2. Get the reference type of the base variable.
         *    2.1 Create the varRef element with the type of base variable
         *    2.2 insert clone of base variable in varRef
         * 3. Create arrayRef element with varRef + arrayIndex
         */
        if(argument.Opcode() == Xcode.VAR){
          XbasicType type = (XbasicType)xcodeml.getTypeTable().
              get(argument.getAttribute(Xattr.TYPE));

          // Demotion cannot be applied as type dimension is smaller
          if(type.getDimensions() < mapping.getMappedDimensions()){
            throw new IllegalTransformationException(
                "mapping dimensions too big. Mapping " + mapping.toString() +
                    " is wrong ...", _claw.getPragma().getLineNo());
          }

          Xnode newArg = new Xnode(Xcode.FARRAYREF, xcodeml);
          newArg.setAttribute(Xattr.TYPE, type.getRef());

          Xnode varRef = new Xnode(Xcode.VARREF, xcodeml);
          varRef.setAttribute(Xattr.TYPE, argument.getAttribute(Xattr.TYPE));

          varRef.appendToChildren(argument, true);
          newArg.appendToChildren(varRef, false);

          //  create arrayIndex
          for(ClawMappingVar mappingVar : mapping.getMappingVariables()){
            Xnode arrayIndex = new Xnode(Xcode.ARRAYINDEX, xcodeml);
            // Find the mapping var in the local table (fct scope)
            XvarDecl mappingVarDecl =
                _fctDef.getDeclarationTable().get(mappingVar.getArgMapping());

            // Add to arrayIndex
            Xnode newMappingVar = new Xnode(Xcode.VAR,xcodeml);
            newMappingVar.setAttribute(Xattr.SCLASS, Xscope.LOCAL.toString());
            newMappingVar.setAttribute(Xattr.TYPE,
                mappingVarDecl.getName().getAttribute(Xattr.TYPE));
            newMappingVar.setValue(mappingVarDecl.getName().getValue());
            arrayIndex.appendToChildren(newMappingVar, false);
            newArg.appendToChildren(arrayIndex, false);
          }

          XelementHelper.insertAfter(argument, newArg);
          argument.delete();
        }
        // Case 2: ArrayRef (n arrayIndex) --> ArrayRef (n+m arrayIndex)
        else if (argument.Opcode() == Xcode.FARRAYREF){
          // TODO
        }

        // Change variable declaration in extracted fct
        XvarDecl varDecl = fctDeclarations.get(var.getFctMapping());
        Xid id = fctSymbols.get(var.getFctMapping());
        XbasicType varDeclType =
            (XbasicType)xcodeml.getTypeTable().get(varDecl.getName().getAttribute(Xattr.TYPE));

        // Case 1: variable is demoted to scalar then take the ref type
        if(varDeclType.getDimensions() == mapping.getMappedDimensions()){
          Xnode tempName = new Xnode(Xcode.NAME, xcodeml);
          tempName.setValue(var.getFctMapping());
          tempName.setAttribute(Xattr.TYPE, varDeclType.getRef());
          XvarDecl newVarDecl =
              new XvarDecl(new Xnode(Xcode.VARDECL, xcodeml).getElement());
          newVarDecl.append(tempName);

          fctDeclarations.replace(newVarDecl);
          id.setType(varDeclType.getRef());
        } else {
          // Case 2: variable is not totally demoted then create new type
          // TODO

        }
      } // Loop mapped variables
    } // Loop over mapping clauses


    // Adapt array reference in function body
    List<Xnode> arrayReferences =
        XelementHelper.findAll(Xcode.FARRAYREF, clonedFctDef.getBody());
    for(Xnode ref : arrayReferences){
      if(!(ref.find(Xcode.VARREF).getChild(0).Opcode() == Xcode.VAR)){
        continue;
      }
      String mappedVar = ref.find(Xcode.VARREF, Xcode.VAR).getValue();
      if(_fctMappingMap.containsKey(mappedVar)){
        ClawMapping mapping = _fctMappingMap.get(mappedVar);

        boolean changeRef = true;

        int mappingIndex = 0;
        for(Xnode e : ref.getChildren()){
          if(e.Opcode() == Xcode.ARRAYINDEX){
            List<Xnode> children = e.getChildren();
            if(children.size() > 0 && children.get(0).Opcode() == Xcode.VAR){
              String varName = e.find(Xcode.VAR).getValue();
              if(varName.equals(mapping.getMappingVariables().get(mappingIndex).getFctMapping())){
                ++mappingIndex;
              } else {
                changeRef = false;
              }
            }
          }
        }
        if(changeRef){
          // TODO Var ref should be extracted only if the reference can be
          // totally demoted
          XelementHelper.insertBefore(ref, ref.find(Xcode.VARREF, Xcode.VAR).cloneObject());
          ref.delete();
        }
      }
    }

    // Generate accelerator pragmas if needed
    // TODO XNODE activate once refactoring finished
    AcceleratorHelper.
        generateAdditionalDirectives(_claw, xcodeml, extractedLoop,
            extractedLoop);
    // TODO must be triggered by a clause
    //AcceleratorHelper.generateRoutineDirectives(_claw, xcodeml, clonedFctDef);

    // Add any additional transformation defined in the directive clauses
    TransformationHelper.generateAdditionalTransformation(_claw, xcodeml,
        transformer, extractedLoop);

    _claw.getPragma().delete();
    this.transformed();
  }

  /**
   * Try to find a do statement matching the range of loop-extract.
   * @param from XbaseElement to search from. Search is performed in its
   *             children.
   * @return A XdoStatement object that match the range of loop-extract.
   * @throws IllegalTransformationException
   */
  private Xnode locateDoStatement(Xnode from)
      throws IllegalTransformationException
  {
    Xnode foundStatement = XelementHelper.find(Xcode.FDOSTATEMENT, from, true);
    if(foundStatement == null){
      throw new IllegalTransformationException("No loop found in function",
          _claw.getPragma().getLineNo());
    } else {
      if(!_claw.getRange().equals(foundStatement)) {
        // Try to find another loops that meet the criteria
        do {
          foundStatement =
              XelementHelper.findNext(Xcode.FDOSTATEMENT, foundStatement);
        } while (foundStatement != null &&
            !_claw.getRange().equals(foundStatement));
      }
    }

    if(foundStatement == null){
      throw new IllegalTransformationException("No loop found in function",
          _claw.getPragma().getLineNo());
    }

    if(!_claw.getRange().equals(foundStatement)) {
      throw new IllegalTransformationException(
          "Iteration range is different than the loop to be extracted",
          _claw.getPragma().getLineNo()
      );
    }
    return foundStatement;
  }

  /**
   * Wrap a function call with a do statement.
   * @param xcodeml The XcodeML representation.
   * @param doStmt  Iteration range to be applied to the do statement.
   * @return The created do statement.
   */
  private Xnode wrapCallWithLoop(XcodeProgram xcodeml, Xnode doStmt)
  {
    // Create a new empty loop
    Xnode loop = XelementHelper.createDoStmt(
        doStmt.findNode(Xcode.VAR).cloneObject(),
        doStmt.findNode(Xcode.INDEXRANGE).cloneObject(),
        xcodeml);

    // Insert the new empty loop just after the pragma
    XelementHelper.insertAfter(_claw.getPragma(), loop);

    // Move the call into the loop body
    loop.getBody().getElement().appendChild(_fctCall.getElement().getParentNode());

    insertDeclaration(doStmt.find(Xcode.VAR).getValue());
    if(doStmt.find(Xcode.INDEXRANGE, Xcode.LOWERBOUND, Xcode.VAR) != null) {
      insertDeclaration(doStmt.
          find(Xcode.INDEXRANGE, Xcode.LOWERBOUND, Xcode.VAR).getValue());
    }
    if(doStmt.find(Xcode.INDEXRANGE, Xcode.UPPERBOUND, Xcode.VAR) != null){
      insertDeclaration(doStmt.
          find(Xcode.INDEXRANGE, Xcode.UPPERBOUND, Xcode.VAR).getValue());
    }
    if(doStmt.find(Xcode.INDEXRANGE, Xcode.STEP, Xcode.VAR) != null){
      insertDeclaration(doStmt.
          find(Xcode.INDEXRANGE, Xcode.STEP, Xcode.VAR).getValue());
    }

    return loop;
  }

  /**
   * Insert new declaration in the function definition.
   * @param id The id used for insertion.
   */
  private void insertDeclaration(String id){
    Xid inductionVarId = _fctDef.getSymbolTable().get(id);
    if(inductionVarId == null){
      Xid copyId = _fctDefToExtract.getSymbolTable().get(id);
      _fctDef.getSymbolTable().add(copyId);
    }

    XvarDecl inductionVarDecl = _fctDef.getDeclarationTable().get(id);
    if(inductionVarDecl == null){
      XvarDecl copyDecl = _fctDefToExtract.getDeclarationTable().get(id);
      _fctDef.getDeclarationTable().add(copyDecl);
    }
  }

  /**
   * @see Transformation#canBeTransformedWith(Transformation)
   * @return Always false as independent transformation are applied one by one.
   */
  @Override
  public boolean canBeTransformedWith(Transformation other) {
    // independant transformation
    return false;
  }
}
