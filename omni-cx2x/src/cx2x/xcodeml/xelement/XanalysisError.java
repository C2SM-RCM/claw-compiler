/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.xelement;

/**
 * This class hold information about error happening during transformation
 * analyzis.
 *
 * @author clementval
 */
 
public class XanalysisError {
  private String _errorMsg;
  private int _errorLineNumber = 0;

  /**
   * Default ctor.
   * @param msg     Error message
   * @param lineno  Line number that triggered the error.
   */
  public XanalysisError(String msg, int lineno){
    _errorMsg = msg;
    _errorLineNumber = lineno;
  }

  /**
   * @return The error message
   */
  public String getMessage(){
    return _errorMsg;
  }

  /**
   * @return The line number that triggered the error.
   */
  public int getLine(){
    return _errorLineNumber;
  }
}
