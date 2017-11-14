/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */
package cx2x.translator.transformation.primitive;

import cx2x.translator.common.ClawConstant;
import cx2x.translator.common.Utility;
import cx2x.translator.config.Configuration;
import cx2x.xcodeml.exception.IllegalTransformationException;
import cx2x.xcodeml.xnode.Xcode;
import cx2x.xcodeml.xnode.XcodeProgram;
import cx2x.xcodeml.xnode.Xnode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Primitive transformation and test applied to FpragmaStatement. This included:
 * - Extract directive prefix of a FpragmaStatement.
 * -
 *
 * @author clementval
 */
public final class Pragma {

  // Avoid instantiation of this class
  private Pragma() {
  }

  /**
   * Return the pragma prefix.
   *
   * @param pragma The pragma node.
   * @return Directive prefix if any. Empty string otherwise.
   */
  public static String getPrefix(Xnode pragma) {
    if(pragma == null || pragma.opcode() != Xcode.FPRAGMASTATEMENT
        || pragma.value().isEmpty())
    {
      return "";
    }

    if(!pragma.value().contains(" ")) {
      return pragma.value();
    }

    return pragma.value().substring(0, pragma.value().indexOf(" "));
  }

  /**
   * Split a long pragma into chunk depending on the maxColumns specified.
   * The split is done on spaces or commas.
   *
   * @param fullPragma   The original full pragma value.
   * @param maxColumns   Maximum number of columns. This length take into
   *                     account the added prefix and continuation symbol.
   * @param pragmaPrefix Prefix used by the pragma.
   * @return A list of chunks from the original pragma.
   */
  public static List<String> split(String fullPragma, int maxColumns,
                                   String pragmaPrefix)
  {
    List<String> splittedPragmas = new ArrayList<>();
    fullPragma = fullPragma.toLowerCase();
    if(fullPragma.length() > maxColumns) {
      fullPragma = Pragma.dropEndingComment(fullPragma);
      int addLength = pragmaPrefix.length() + 5; // "!$<prefix> PRAGMA &"
      while(fullPragma.length() > (maxColumns - addLength)) {
        int splitIndex =
            fullPragma.substring(0,
                maxColumns - addLength).lastIndexOf(" ");
        // Cannot cut as it should. Take first possible cutting point.
        if(splitIndex == -1) {
          splitIndex = fullPragma.substring(0,
              maxColumns - addLength).lastIndexOf(",");
          if(splitIndex == -1) {
            splitIndex =
                (fullPragma.contains(" ")) ? fullPragma.lastIndexOf(" ") :
                    (fullPragma.contains(",")) ? fullPragma.lastIndexOf(",") :
                        fullPragma.length();
          }
        }
        String splittedPragma = fullPragma.substring(0, splitIndex);
        fullPragma =
            fullPragma.substring(splitIndex, fullPragma.length()).trim();
        splittedPragmas.add(splittedPragma);
      }
    }
    if(fullPragma.length() > 0) {
      splittedPragmas.add(fullPragma);
    }
    return splittedPragmas;
  }

  /**
   * Split the line by its length and add continuation symbols.
   *
   * @param xcodeml The XcodeML on which the transformations are
   *                applied.
   */
  public static void splitByLength(Xnode pragma, XcodeProgram xcodeml,
                                   String prefix)
      throws IllegalTransformationException
  {
    if(pragma == null || pragma.opcode() != Xcode.FPRAGMASTATEMENT) {
      throw new IllegalTransformationException(ClawConstant.ERROR_INCOMPATIBLE);
    }
    String allPragma = pragma.value().toLowerCase();
    if(allPragma.length() > Configuration.get().getMaxColumns()) {
      allPragma = Pragma.dropEndingComment(allPragma);
      Xnode newlyInserted = pragma;
      List<String> splittedPragmas = Pragma.split(allPragma,
          Configuration.get().getMaxColumns(), prefix);

      for(int i = 0; i < splittedPragmas.size(); ++i) {
        // Create pragma with continuation symbol unless for the last item.
        newlyInserted = createAndInsertPragma(xcodeml, newlyInserted,
            pragma.filename(), pragma.lineNo(),
            splittedPragmas.get(i), i != splittedPragmas.size() - 1);
      }
      // Delete original not splitted pragma.
      pragma.delete();
    }
  }

  /**
   * Split the line by its previous continuation mark.
   *
   * @param pragma  Pragma node.
   * @param prefix  Pragma prefix.
   * @param xcodeml Current XcodeML translation unit.
   */
  public static void splitByCont(Xnode pragma, String prefix,
                                 XcodeProgram xcodeml)
      throws IllegalTransformationException
  {
    if(pragma == null || pragma.opcode() != Xcode.FPRAGMASTATEMENT) {
      throw new IllegalTransformationException(ClawConstant.ERROR_INCOMPATIBLE);
    }
    String allPragma = pragma.value();
    int lineIndex = pragma.lineNo();
    String splitter = prefix.trim();
    if(allPragma.contains(prefix.trim() +
        ClawConstant.CONTINUATION_LINE_SYMBOL))
    {
      splitter = prefix.trim() + ClawConstant.CONTINUATION_LINE_SYMBOL;
    }

    Xnode newlyInserted = pragma;
    String[] lines = allPragma.split(splitter);
    for(int i = 0; i < lines.length - 1; ++i) {
      newlyInserted = createAndInsertPragma(xcodeml, newlyInserted,
          pragma.filename(), lineIndex++, lines[i], true);
    }
    createAndInsertPragma(xcodeml, newlyInserted, pragma.filename(), lineIndex,
        lines[lines.length - 1], false);
    pragma.delete();
  }

  /**
   * Remove any trailing comment from a pragma string.
   *
   * @param pragma Original pragma string.
   * @return Pragma string without the trailing comment if any.
   */
  public static String dropEndingComment(String pragma) {
    if(pragma != null && pragma.indexOf("!") > 0) {
      return pragma.substring(0, pragma.indexOf("!")).trim();
    }
    return pragma;
  }

  /**
   * Check if the pragma was already continued. Can happen when using the !$claw
   * primitive directive
   *
   * @return True if the pragma was previously continued.
   */
  public static boolean fromClawPrimitive(Xnode pragma) {
    if(pragma == null || pragma.opcode() != Xcode.FPRAGMASTATEMENT) {
      return false;
    }
    String allPragma = pragma.value().toLowerCase();
    return allPragma.contains(ClawConstant.OPENACC_PREFIX_CONT) ||
        Utility.countOccurrences(allPragma,
            ClawConstant.OPENACC_PREFIX + " ") > 1;
  }

  /**
   * Create a new pragma node and insert it after the hook.
   *
   * @param xcodeml   Current XcodeML file unit.
   * @param hook      Hook node. New node will be inserted after this one.
   * @param filename  Filename set to the enhanced info.
   * @param lineNo    Line index specify the offset of the line number for the
   *                  new node from the original pragma node.
   * @param value     Value of the pragma node.
   * @param continued If true, continuation symbol is added at the end of the
   *                  line.
   * @return The newly created node to be able to insert after it.
   */
  private static Xnode createAndInsertPragma(XcodeProgram xcodeml, Xnode hook,
                                             String filename,
                                             int lineNo, String value,
                                             boolean continued)
  {
    Xnode p = xcodeml.createNode(Xcode.FPRAGMASTATEMENT);
    p.setFilename(filename);
    p.setLine(lineNo);
    if(continued) {
      if(!value.trim().toLowerCase().startsWith(ClawConstant.OPENACC_PREFIX)) {
        p.setValue(ClawConstant.OPENACC_PREFIX + " " + value.trim() + " " +
            ClawConstant.CONTINUATION_LINE_SYMBOL);
      } else {
        p.setValue(value.trim() + " " + ClawConstant.CONTINUATION_LINE_SYMBOL);
      }
    } else {
      if(!value.trim().toLowerCase().startsWith(ClawConstant.OPENACC_PREFIX)) {
        p.setValue(ClawConstant.OPENACC_PREFIX + " " + value.trim());
      }
    }
    hook.insertAfter(p);
    return p;
  }

  /**
   * Find a pragma element in the previous nodes containing a given keyword.
   *
   * @param from    Element to start from.
   * @param keyword Keyword to be found in the pragma.
   * @return The pragma if found. Null otherwise.
   */
  public static Xnode findPrevious(Xnode from, String keyword) {
    if(from == null || from.element() == null) {
      return null;
    }
    Node prev = from.element().getPreviousSibling();
    Node parent = from.element();
    do {
      while(prev != null) {
        if(prev.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) prev;
          if(element.getTagName().equals(Xcode.FPRAGMASTATEMENT.code())
              && element.getTextContent().toLowerCase().
              contains(keyword.toLowerCase()))
          {
            return new Xnode(element);
          }
        }
        prev = prev.getPreviousSibling();
      }
      parent = parent.getParentNode();
      prev = parent;
    } while(parent != null);
    return null;
  }
}
