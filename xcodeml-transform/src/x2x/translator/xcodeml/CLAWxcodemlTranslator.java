package x2x.translator.xcodeml;

import x2x.translator.xcodeml.xelement.*;
import x2x.translator.xcodeml.transformation.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.xpath.*;

import java.util.*;

import exc.xcodeml.*;
import exc.object.Xcode;
import xcodeml.util.XmOption;
import x2x.translator.pragma.CLAWpragma;

public class CLAWxcodemlTranslator {
  private String _xcodemlInputFile = null;
  private String _xcodemlOutputFile = null;
  private boolean _canTransform = false;

  private DependentTransformationGroup<LoopFusion> _loopFusion = null;
  private IndependentTransformationGroup<LoopInterchange> _loopInterchange = null;
  private IndependentTransformationGroup<LoopExtraction> _loopExtract = null;
  private XcodeProg _program = null;

  private static final int INDENT_OUTPUT = 2; // Number of spaces for indent

  public CLAWxcodemlTranslator(String xcodemlInputFile,
    String xcodemlOutputFile)
  {
    _xcodemlInputFile = xcodemlInputFile;
    _xcodemlOutputFile = xcodemlOutputFile;
    _loopFusion = new DependentTransformationGroup<LoopFusion>();
    _loopInterchange = new IndependentTransformationGroup<LoopInterchange>();
    _loopExtract = new IndependentTransformationGroup<LoopExtraction>();

    // Add transformations
  }

  public void analyze() throws Exception {
    _program = new XcodeProg(_xcodemlInputFile);
    _program.load();

    if(!_program.isXcodeMLvalid()){
      System.err.println("XcodeML document is not valid");
      return;
    }

    // Read information from the type table
    _program.readTypeTable();
    _program.readGlobalSymbolsTable();

    NodeList pragmaList = XelementHelper.getPragmas(_program.getDocument());

    for (int i = 0; i < pragmaList.getLength(); i++) {
      Node pragmaNode = pragmaList.item(i);
      if (pragmaNode.getNodeType() == Node.ELEMENT_NODE) {
        Element pragmaElement = (Element) pragmaNode;
        String fullPragmaText = pragmaElement.getTextContent();

        if(!CLAWpragma.startsWithClaw(fullPragmaText)){
          continue; // Not CLAW pragma, we do nothing
        }

        if(CLAWpragma.isValid(fullPragmaText)){
          CLAWpragma clawDirective = CLAWpragma.getDirective(fullPragmaText);

          // loop-fusion directives
          if(clawDirective == CLAWpragma.LOOP_FUSION){
            Element loop = XelementHelper.findNextLoop(pragmaNode);
            if(loop == null){
              System.err
                .println("loop-fusion pragma is not followed by a loop");
              System.exit(1);
            } else {
              LoopFusion trans = new LoopFusion(pragmaElement, loop);
              if(trans.analyze(_program)){
                _loopFusion.add(trans);
              }
            }


          // loop-interchange directives
          } else if(clawDirective == CLAWpragma.LOOP_INTERCHANGE){
            Element loop = XelementHelper.findNextLoop(pragmaNode);
            if(loop == null){
              System.err
                .println("loop-interchange pragma is not followed by a loop");
              System.exit(1);
            } else {
              LoopInterchange trans = new LoopInterchange(pragmaElement, loop);
              if(trans.analyze(_program)){
                _loopInterchange.add(trans);
              }
            }

          // loop-extract directives
          } else if(clawDirective == CLAWpragma.LOOP_EXTRACT){
            Element expr = XelementHelper.findNextExprStatement(pragmaNode);
            if(expr == null){
              System.err.println("loop-extract pragma is not followed by an " +
                "expression statment");
            } else {
              LoopExtraction trans = new LoopExtraction(pragmaElement, expr);
              if(trans.analyze(_program)){
                _loopExtract.add(trans);
              }
            }
          }

        } else {
          System.out.println("INVALID PRAGMA: " + fullPragmaText);
          System.exit(1);
        }
      }
    }


    // Analysis done, the transformation can be performed.
    _canTransform = true;
  }

  public void transform() {
    try {
      if(!_canTransform){
        // TODO handle return value
        XelementHelper.writeXcodeML(_program, _xcodemlOutputFile, INDENT_OUTPUT);
        return;
      }

      // Do the transformation here

      if(XmOption.isDebugOutput()){
        System.out.println("transform loop-fusion: "+ _loopFusion.count());
        System.out.println("transform loop-interchange: "
          + _loopInterchange.count());
        System.out.println("transform loop-extract: "+ _loopExtract.count());
      }

      // Apply loop-extract transformation
      _loopExtract.applyTranslations(_program);

      // Apply loop-fusion transformation
      _loopFusion.applyTranslations(_program);

      // Apply loop-interchange transformation
      _loopInterchange.applyTranslations(_program);

      // TODO handle the return value
      XelementHelper.writeXcodeML(_program, _xcodemlOutputFile, INDENT_OUTPUT);

    } catch (Exception ex) {
      // TODO handle exception
      System.out.println("Transformation exception: ");
      ex.printStackTrace();
    }
  }

}
