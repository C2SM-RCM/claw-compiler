/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator;

// Cx2x import
import cx2x.translator.common.Constant;
import cx2x.translator.language.ClawLanguage;
import cx2x.translator.language.helper.accelerator.AcceleratorDirective;
import cx2x.translator.transformation.claw.Kcaching;
import cx2x.translator.transformation.loop.*;
import cx2x.translator.transformation.openacc.OpenAccContinuation;
import cx2x.translator.transformation.utility.UtilityRemove;
import cx2x.xcodeml.error.*;
import cx2x.xcodeml.helper.*;
import cx2x.xcodeml.language.AnalyzedPragma;
import cx2x.xcodeml.xelement.*;
import cx2x.xcodeml.exception.*;
import cx2x.xcodeml.transformation.*;
import cx2x.translator.transformer.*;

// OMNI import
import xcodeml.util.XmOption;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;


/**
 * ClawXcodeMlTranslator is the class driving the translation. It analyzes the
 * CLAW directives and applies the corresponding transformation to the XcodeML/F
 * intermediate representation.
 *
 * @author clementval
 */

public class ClawXcodeMlTranslator {
  private static final String ERROR_PREFIX = "claw-error: ";
  private final Map<ClawDirectiveKey, ClawLanguage> _blockDirectives;
  private String _xcodemlInputFile = null;
  private String _xcodemlOutputFile = null;
  private boolean _canTransform = false;

  private ClawTransformer _transformer = null;
  private XcodeProgram _program = null;

  private static final int INDENT_OUTPUT = 2; // Number of spaces for indent

  /**
   * ClawXcodeMlTranslator ctor.
   * @param xcodemlInputFile  The XcodeML input file path.
   * @param xcodemlOutputFile The XcodeML output file path.
   */
  public ClawXcodeMlTranslator(String xcodemlInputFile,
    String xcodemlOutputFile)
  {
    _xcodemlInputFile = xcodemlInputFile;
    _xcodemlOutputFile = xcodemlOutputFile;
    _transformer = new ClawTransformer();
    _blockDirectives = new Hashtable<>();
  }

  /**
   * Analysis the XcodeML code and produce a list of applicable transformation.
   * @throws Exception if analysis fails.
   */
  public void analyze() throws Exception {
    _program = new XcodeProgram(_xcodemlInputFile);
    if(!_program.load()){
      abort();
    }

    // Check all pragma found in the program
    for (Xpragma pragma :  XelementHelper.findAllPragmas(_program)){

      // pragma does not start with the CLAW prefix
      if(!ClawLanguage.startsWithClaw(pragma)){
        // Handle special transformation of OpenACC line continuation
        if(pragma.getValue().toLowerCase().startsWith(Constant.OPENACC_PREFIX)){
          OpenAccContinuation t =
              new OpenAccContinuation(new AnalyzedPragma(pragma));
          addOrAbort(t);
        }

        // Not CLAW pragma, we do nothing
        continue;
      }

      // Analyze the raw pragma with the CLAW language parser
      // TODO give correct accelerator definition when option is available #17
      ClawLanguage analyzedPragma = ClawLanguage.analyze(pragma,
          AcceleratorDirective.OPENACC);

      // Create transformation object based on the directive
      switch (analyzedPragma.getDirective()){
        case KCACHE:
          addOrAbort(new Kcaching(analyzedPragma));
          break;
        case LOOP_FUSION:
          addOrAbort(new LoopFusion(analyzedPragma));
          break;
        case LOOP_INTERCHANGE:
          addOrAbort(new LoopInterchange(analyzedPragma));
          break;
        case LOOP_EXTRACT:
          addOrAbort(new LoopExtraction(analyzedPragma));
          break;
        case LOOP_HOIST:
          HandleBlockDirective(analyzedPragma);
          break;
        case ARRAY_TRANSFORM:
          HandleBlockDirective(analyzedPragma);
          break;
        case REMOVE:
          HandleBlockDirective(analyzedPragma);
          break;
        default:
          _program.addError("Unrecognized CLAW directive", pragma.getLineNo());
          abort();
      }
    }

    // Clean up block transformation map
    for(Map.Entry<ClawDirectiveKey, ClawLanguage> entry :
        _blockDirectives.entrySet())
    {
      createBlockDirectiveTransformation(entry.getValue(), null);
    }

    // Analysis done, the transformation can be performed.
    _canTransform = true;
  }


  /**
   * Associate correctly the start and end directive to form blocks.
   * @param analyzedPragma Analyzed pragma object to be handle.
   */
  private void HandleBlockDirective(ClawLanguage analyzedPragma){
    int depth =
        XelementHelper.getDepth(analyzedPragma.getPragma().getBaseElement());
    ClawDirectiveKey crtRemoveKey =
        new ClawDirectiveKey(analyzedPragma.getDirective(), depth);
    if(analyzedPragma.isEndPragma()){ // start block directive
      if (!_blockDirectives.containsKey(crtRemoveKey)) {
        _program.addError("Invalid Claw directive (end with no start)",
            analyzedPragma.getPragma().getLineNo());
        abort();
      } else {
        createBlockDirectiveTransformation(_blockDirectives.get(crtRemoveKey),
            analyzedPragma);
        _blockDirectives.remove(crtRemoveKey);
      }
    } else { // end block directive
      if (_blockDirectives.containsKey(crtRemoveKey)) {
        createBlockDirectiveTransformation(_blockDirectives.get(crtRemoveKey),
            null);
      }
      _blockDirectives.remove(crtRemoveKey);
      _blockDirectives.put(crtRemoveKey, analyzedPragma);
    }
  }

  /**
   * Create a new block transformation object according to its start directive.
   * @param begin Begin directive which starts the block.
   * @param end   End directive which ends the block.
   */
  private void createBlockDirectiveTransformation(ClawLanguage begin,
                                                  ClawLanguage end)
  {
    switch (begin.getDirective()){
      case REMOVE:
        addOrAbort(new UtilityRemove(begin, end));
        break;
      case ARRAY_TRANSFORM:
        addOrAbort(new ArrayTransform(begin, end));
        break;
      case LOOP_HOIST:
        addOrAbort(new LoopHoist(begin, end));
    }
  }

  /**
   * Add a transformation in the pipeline if the analysis is succeded.
   * Otherwise, abort the translation.
   * @param t           The transformation to be analyzed and added.
   */
  private void addOrAbort(Transformation t)
  {
    if(t.analyze(_program, _transformer)){
      _transformer.addTransformation(t);
    } else {
      abort();
    }
  }

  /**
   * Apply all the transformation in the pipeline.
   */
  public void transform() {
    try {
      if(!_canTransform){
        if(!XelementHelper.writeXcodeML(_program,
            _xcodemlOutputFile, INDENT_OUTPUT))
        {
          abort();
        }
        return;
      }

      for (Map.Entry<Class, TransformationGroup> entry :
          _transformer.getGroups().entrySet())
      {
        if(XmOption.isDebugOutput()){
          System.out.println("Apply transfomation: " +
              entry.getValue().transformationName());
        }

        try {
          entry.getValue().applyTranslations(_program, _transformer);
        } catch (IllegalTransformationException itex) {
          _program.addError(itex.getMessage(), itex.getStartLine());
          abort();
        } catch (Exception ex){
          ex.printStackTrace();
          _program.addError("Unexpected error: " + ex.getMessage(), 0);
          if(XmOption.isDebugOutput()){
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            _program.addError(errors.toString(), 0);
          }
          abort();
        }
      }


      if(!XelementHelper.writeXcodeML(_program, _xcodemlOutputFile,
          INDENT_OUTPUT))
      {
        abort();
      }

    } catch (Exception ex) {
      System.err.println("Transformation exception: ");
      ex.printStackTrace();
    }
  }

  /**
   * Print all the errors stored in the XcodeML object and abort the program.
   */
  private void abort(){
    for(XanalysisError error : _program.getErrors()){
      if(error.getLine() == 0){
        System.err.println(ERROR_PREFIX + error.getMessage() + ", line: " +
            "undefined");
      } else {
        System.err.println(ERROR_PREFIX + error.getMessage() + ", line: " +
            error.getLine());
      }
    }
    System.exit(1);
  }

}
