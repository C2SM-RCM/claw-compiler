package cx2x.translator.xcode;

import cx2x.translator.language.ClawDirective;
import cx2x.translator.language.ClawLanguage;
import cx2x.translator.language.helper.accelerator.AcceleratorGenerator;
import cx2x.translator.language.helper.target.Target;
import cx2x.xcodeml.exception.IllegalDirectiveException;
import exc.block.*;
import exc.object.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Xobject translator for the CLAW pragma statements.
 *
 * @author clementval
 */
class ClawTranslate implements XobjectDefVisitor {
  private XobjectFile env;
  private BlockPrintWriter debug_out;
  private final Target _crtTarget;
  private final AcceleratorGenerator _crtGenerator;


  /**
   * Constructs a new ClawTranslate object.
   * @param objectFile Original object file.
   */
  ClawTranslate(XobjectFile objectFile, Target target,
                AcceleratorGenerator generator)
  {
    this.env = objectFile;
    this.debug_out = new BlockPrintWriter(System.out);
    this._crtTarget = target;
    this._crtGenerator = generator;
  }

  /**
   * Action to be applied at the end of the translation.
   */
  void finish(){

  }

  @Override
  public void doDef(XobjectDef xobjectDef) {

    if(xobjectDef.getDef().Opcode() == Xcode.FUNCTION_DEFINITION){
      analyzeFunction(new FuncDefBlock(xobjectDef));
    }

    /*
    try {
      if (xobjectDef.getDef().Opcode() == Xcode.FUNCTION_DEFINITION) {
        analyzeFunctionBody(xobjectDef.getFuncBody());
      }
    } catch (IllegalDirectiveException idex){
      System.err.println(idex.getMessage());
    }*/
  }

  /**
   * Print list of identifier in the symbol table
   * @param ids Identifiers list
   */
  private void printIdList(XobjList ids){
    for(Xobject obj : ids){
      Ident id = (Ident)obj;
      System.out.println(String.format("      ID:%s (%s)", id.getSym(),
          id.getStorageClass()));
    }
  }

  /**
   * Test method to explore the structure of block and block manipulation.
   * @param cb    Compound block to be analyzed.
   * @param level Current level of recursion.
   */
  private void analyzeCompoundBlock(CompoundBlock cb, int level){
    System.out.println("-->    " + cb.Opcode() + " at level " + level);
    BlockList body = cb.getBody();
    if(body == null){
      return;
    }
    Block b = body.getHead();
    while(b != null){
      if(b instanceof CompoundBlock){
        analyzeCompoundBlock((CompoundBlock) b, level + 1);
      }

      switch (b.Opcode()){
        case PRAGMA_LINE:
          Xobject pragma = b.toXobject();
          System.out.println("    PRAGMA: " + pragma.getArg(0).getString());
          break;
        case F_DO_STATEMENT:
          FdoBlock fdb = (FdoBlock)b;
          analyzeDoBlock(fdb);
          break;
      }

      b = b.getNext();
    }
  }

  /**
   * Analyze a do block.
   * @param doBlock Do block to be analyzed.
   */
  private void analyzeDoBlock(FdoBlock doBlock){
    Xobject induction = doBlock.getInductionVar();

    String lb = getStringOrInt(doBlock.getLowerBound());
    String up = getStringOrInt(doBlock.getUpperBound());
    String s = getStringOrInt(doBlock.getStep());

    System.out.println(String.format("    DO %s = %s, %s, %s",
        induction.getString(), lb, up, s));
  }

  /**
   * Get the int value or string var as a String.
   * @param bound The bound object.
   * @return Int constant or var as a String.
   */
  private String getStringOrInt(Xobject bound){
    if(bound instanceof XobjInt){
      return String.valueOf(bound.getInt());
    } else {
      return bound.getString();
    }
  }



  /**
   * Traverse function definition with block api.
   * @param fb Function definition block.
   */
  private void analyzeFunction(FuncDefBlock fb){
    System.out.println("=== FUNCTION BLOCK ===");
    System.out.println("  = Function name: " + fb.getBlock().getName());
    BlockList body = fb.getBlock().getBody();
    XobjList idList = body.getIdentList();
    printIdList(idList);
    Block b = body.getHead();
    System.out.println("  = FUNCTION BODY");
    while(b != null) {
      if (b instanceof CompoundBlock){
        analyzeCompoundBlock((CompoundBlock) b, 0);
      } else {
        System.out.println("  -> " + b.Opcode());
      }
      b = b.getNext();
    }
  }


  /**
   * Analyze the statements list in a function body and trigger the correct
   * translations.
   * @param functionBody
   */
  private void analyzeFunctionBody(Xobject functionBody)
      throws IllegalDirectiveException
  {
    System.out.println("=== ANALYZE FUNCTION BODY ===");
    if(functionBody.Opcode() != Xcode.F_STATEMENT_LIST){
      // Add error
      System.err.print("Cannot analyze function body!");
    }


    topdownXobjectIterator iterator = new topdownXobjectIterator(functionBody);
    iterator.init();

    while(!iterator.end()) {
      Xobject crtObject = iterator.getXobject();
      if(crtObject != null)
        System.out.println(crtObject.Opcode());
      if(crtObject != null) {
        switch (crtObject.Opcode()){
          case PRAGMA_LINE:
            System.out.println(crtObject.getArg(0).getString());
            ClawLanguage l =
                ClawLanguage.analyze(crtObject, _crtGenerator, _crtTarget);
            switch (l.getDirective()){
              case LOOP_FUSION:
                translateLoopFusion(l, iterator, crtObject);
                break;
            }
            break;
          case F_ASSIGN_STATEMENT:
            System.out.println("=== ASSIGN ===");
            break;
        }
      }
      iterator.next();
    }
  }

  private void translateLoopFusion(ClawLanguage l, topdownXobjectIterator it,
                                   Xobject pragma)
    throws IllegalDirectiveException
  {
    List<XobjList> doStatements = new ArrayList<>();
    boolean findLoop = true;
    while(!it.end()){
      Xobject obj = it.getXobject();

      if(obj != null && obj.Opcode() == Xcode.F_DO_STATEMENT && findLoop){
        Xcode crtCode = obj.Opcode();
        while(crtCode != Xcode.F_STATEMENT_LIST){
          it.next();
          obj = it.getXobject();
          if(obj != null){
            crtCode = obj.Opcode();
          }
        }

        System.out.println(obj.Opcode());
        XobjList stmtList = (XobjList)obj;


        findLoop = false;
      } else if(obj != null && obj.Opcode() == Xcode.PRAGMA_LINE && !findLoop){
        ClawLanguage other = ClawLanguage.analyze(obj, null, null);
        if(other.getDirective() == ClawDirective.LOOP_FUSION){
          findLoop = true;
        }
      }
      it.next();
    }

    if(doStatements.size() <= 1){
      throw new IllegalDirectiveException("", "", 0); // TODO error
    }



  }
}
