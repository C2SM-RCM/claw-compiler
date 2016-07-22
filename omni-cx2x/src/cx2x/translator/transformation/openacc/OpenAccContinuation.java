/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.transformation.openacc;

import cx2x.translator.common.ClawConstant;
import cx2x.xcodeml.exception.IllegalTransformationException;
import cx2x.xcodeml.helper.XnodeUtil;
import cx2x.xcodeml.language.AnalyzedPragma;
import cx2x.xcodeml.transformation.Transformation;
import cx2x.xcodeml.transformation.Transformer;
import cx2x.xcodeml.xnode.XcodeProgram;
import cx2x.xcodeml.xnode.Xcode;
import cx2x.xcodeml.xnode.Xnode;

/**
 * <pre>
 * OpenACC line continuation transformation. The XcodeML/F pragma statement
 * representation is an aggregated version of the pragma with all its continuation
 * lines.
 * As those directives are not handled by the CLAW XcodeML to XcodeML
 * translator, they must be output in a correct way. This transformation divides
 * the XcodeML representation back to a multi-line pragma definition.
 *
 * Example:
 * The followings OpenACC directives in Fortran code:
 *
 *   !$acc data &amp;
 *   !$acc present (a,b,c,d,e,f,g)
 *
 * are represented in XcodeML with
 *
 * &lt;FpragmaStatement&gt;acc data present (a,b,c,d,e,f,g)&lt;/FpragmaStatement&gt;
 *
 * Based on the defined max columns, the pragma statement will be splitted.
 * </pre>
 *
 * @author clementval
 */
public class OpenAccContinuation extends Transformation {

  /**
   * Constructs a new LoopFusion triggered from a specific pragma.
   * @param directive The directive that triggered the loop fusion
   *                  transformation.
   */
  public OpenAccContinuation(AnalyzedPragma directive){
    super(directive);
  }


  /**
   * Loop fusion analysis: find whether the pragma statement is followed by a
   * do statement.
   * @param xcodeml      The XcodeML on which the transformations are applied.
   * @param transformer  The transformer used to applied the transformations.
   * @return True if a do statement is found. False otherwise.
   */
  public boolean analyze(XcodeProgram xcodeml, Transformer transformer) {
    return getDirective().getPragma().getValue().toLowerCase().
        startsWith(ClawConstant.OPENACC_PREFIX);
  }

  @Override
  public boolean canBeTransformedWith(Transformation other) {
    // independent transformation
    return false;
  }

  /**
   * Apply the loop fusion transformation.
   * @param xcodeml         The XcodeML on which the transformations are
   *                        applied.
   * @param transformer     The transformer used to applied the transformations.
   * @param transformation  Not used in this transformation
   * @throws IllegalTransformationException if the transformation cannot be
   * applied.
   */
  @Override
  public void transform(XcodeProgram xcodeml, Transformer transformer,
                        Transformation transformation)
      throws IllegalTransformationException
  {
    if(transformer.getMaxColumns() <= 0
        || getDirective().getPragma().isDeleted())
    {
      return;
    }

    String allPragma = getDirective().getPragma().getValue();

    if(allPragma.length() > transformer.getMaxColumns()){
      Xnode newlyInserted = getDirective().getPragma();
      int lineIndex = 0;
      while(allPragma.length() > transformer.getMaxColumns()){
        int splitIndex =
            allPragma.substring(0,
                transformer.getMaxColumns() - 2).lastIndexOf(" ");
        String splittedPragma = allPragma.substring(0, splitIndex);
        allPragma = allPragma.substring(splitIndex, allPragma.length());
        newlyInserted = createAndInsertPragma(xcodeml, newlyInserted, lineIndex,
            splittedPragma, true);
      }
      createAndInsertPragma(xcodeml, newlyInserted, lineIndex,
          allPragma, false);
      getDirective().getPragma().delete();
    }
  }

  /**
   * Create a new pragma node and insert it after the hook.
   * @param xcodeml   Current XcodeML file unit.
   * @param hook      Hook node. New node will be inserted after this one.
   * @param lineIndex Line index specify the offset of the line number for the
   *                  new node from the original pragma node.
   * @param value     Value of the pragma node.
   * @param continued If true, continuation symbol is added at the end of the
   *                  line.
   * @return The newly created node to be able to insert after it.
   */
  private Xnode createAndInsertPragma(XcodeProgram xcodeml, Xnode hook,
                                      int lineIndex, String value,
                                      boolean continued)
  {
    Xnode p = new Xnode(Xcode.FPRAGMASTATEMENT, xcodeml);
    p.setFile(getDirective().getPragma().getFile());
    p.setLine(getDirective().getPragma().getLineNo() + lineIndex);
    if(continued){
      p.setValue(ClawConstant.OPENACC_PREFIX + " " + value + " " +
          ClawConstant.CONTINUATION_LINE_SYMBOL);
    } else {
      p.setValue(ClawConstant.OPENACC_PREFIX + " " + value);
    }
    XnodeUtil.insertAfter(hook, p);
    getDirective().getPragma().delete();
    return p;
  }
}
