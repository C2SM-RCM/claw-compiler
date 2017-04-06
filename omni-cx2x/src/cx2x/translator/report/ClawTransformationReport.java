/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */
package cx2x.translator.report;

import cx2x.ClawVersion;
import cx2x.translator.common.Utility;
import cx2x.translator.config.Configuration;
import cx2x.translator.config.GroupConfiguration;

import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generation of the transformation report. Report includes information about
 * the configuration used for the transformation phase as well as information
 * about applied transformation.
 *
 * @author clementval
 */
public class ClawTransformationReport {

  private static final int MAX_COL = 80;
  private FileWriter _report;

  /**
   * Contructs a transformation report object.
   *
   * @throws Exception If file cannot be created or cannot be written.
   */
  public ClawTransformationReport() throws Exception {
    _report = new FileWriter("report.lst");
  }

  /**
   * Generate the report to file.
   *
   * @param config Current configuration used during the transformation.
   * @param args   Arguments passed to the translator.
   * @throws Exception If file cannot be created or cannot be written.
   */
  public void generate(Configuration config, String[] args) throws Exception {
    printHeader("CLAW Transformation Report");
    printMainInfo(config, args);
    printTransformationOrderInfo(config);
    _report.flush();
  }

  /**
   * Print a header surrounded by dashed lines.
   *
   * @param headerTitle Title to be included in the header.
   * @throws Exception If file cannot be created or cannot be written.
   */
  private void printHeader(String headerTitle) throws Exception {
    printDashedLine();
    printLine(generateStr((MAX_COL / 2) - (headerTitle.length() / 2), ' ') +
        headerTitle);
    printDashedLine();
  }

  /**
   * Write the header of the report. Contains information driving the
   * transformation.
   *
   * @param config Used configuration for the transformation.
   * @param args   Arguments passed to the translator.
   * @throws Exception If file cannot be created or cannot be written.
   */
  private void printMainInfo(Configuration config, String[] args)
      throws Exception
  {
    printTitle("Information");

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date = new Date();

    List<String[]> infos = new ArrayList<>();

    infos.add(new String[]{"File", ""});
    infos.add(new String[]{"Transformed", dateFormat.format(date)});
    infos.add(new String[]{"Compiler", ClawVersion.getVersion()});
    infos.add(new String[]{"Target", config.getCurrentTarget().toString()});
    infos.add(new String[]{"Directive", config.getCurrentDirective().toString()});
    infos.add(new String[]{"Driver command", ""});
    infos.add(new String[]{"Translator command", Utility.join(" ", args)});


    int indentCol = 0;
    for(String[] info : infos) {
      if(info[0].length() > indentCol) {
        indentCol = info[0].length();
      }
    }

    for(String[] info : infos) {
      printInfo(info[0], info[1], indentCol + 1);
    }
  }

  /**
   * Print information about the transformations group and their application
   * order.
   *
   * @param config Current configuration used during the transformation.
   * @throws Exception If file cannot be created or cannot be written.
   */
  private void printTransformationOrderInfo(Configuration config)
      throws Exception
  {
    printTitle("Transformation information");

    int index = 1;
    for(GroupConfiguration group : config.getGroups()) {
      printLine(String.format("%d) %s\t %d", index++, group.getName(), 10));
    }
  }


  /**
   * Write information in a formatted way: title : value
   *
   * @param title Title for the information.
   * @param value Value of the information.
   * @throws Exception If file cannot be created or cannot be written.
   */
  private void printInfo(String title, String value, int indentCol)
      throws Exception
  {
    printLine(String.format("%s%s: %s", title,
        generateStr(indentCol - title.length(), ' '), value));

  }

  /**
   * Write value and add new line at the end.
   *
   * @param value Value to be written.
   * @throws Exception If file cannot be created or cannot be written.
   */
  private void printLine(String value) throws Exception {
    _report.write(value + "\n");
  }

  /**
   * Print a dashed line of the width of the report.
   *
   * @throws Exception If file cannot be created or cannot be written.
   */
  private void printDashedLine() throws Exception {
    printLine(getDashedLine(MAX_COL));
  }

  /**
   * Print the given text as a section title and underlined with dashes.
   * Title is preceded and followed by an empty line.
   *
   * @param title Text to be written as a section title.
   * @throws Exception If file cannot be created or cannot be written.
   */
  private void printTitle(String title) throws Exception {
    printLine("");
    printLine(title);
    printLine(getDashedLine(title.length()));
    printLine("");
  }

  /**
   * Generate a string with dash of the given size.
   *
   * @param size Length of the dashed line.
   * @return A dashed line string of the given size.
   */
  private String getDashedLine(int size) {
    return generateStr(size, '-');
  }

  /**
   * Generate a string with the given character and size.
   *
   * @param size Length of the string to be generated.
   * @param c    Character used during the generation.
   * @return A string of the given length filled with the given character.
   */
  private String generateStr(int size, char c) {
    StringBuilder str = new StringBuilder();
    for(int i = 0; i < size; ++i) {
      str.append(c);
    }
    return str.toString();
  }
}
