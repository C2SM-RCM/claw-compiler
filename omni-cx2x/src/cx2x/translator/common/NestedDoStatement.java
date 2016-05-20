package cx2x.translator.common;

import cx2x.translator.language.ClawDimension;
import cx2x.xcodeml.exception.IllegalTransformationException;
import cx2x.xcodeml.xelement.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding information about nested do statements. 
 *
 * Created by clementval on 20/05/16.
 */
public class NestedDoStatement {

  private final List<XdoStatement> _statements;

  /**
   * Constructs a group of nested do statements from a list of dimension
   * objects. The outer statement represents the first element of the list and
   * the inner statement represents the last element of the list.
   * @param dimensions A list of dimension objects.
   * @param xcodeml    The current XcodeML program unit in which the elements
   *                   will be created.
   */
  public NestedDoStatement(List<ClawDimension> dimensions,
                           XcodeProgram xcodeml)
  {
    _statements = new ArrayList<>();
    try {
      for (ClawDimension dim : dimensions) {
        Xvar induction = Xvar.create(XelementName.TYPE_F_INT,
            dim.getIdentifier(), Xscope.LOCAL, xcodeml);
        XindexRange range = dim.generateIndexRange(xcodeml);
        XdoStatement doSt =
            XdoStatement.create(induction, range, false, xcodeml);
        if (_statements.size() != 0) {
          _statements.get(_statements.size() - 1).getBody().
              appendToChildren(doSt, false);
        }
        _statements.add(doSt);
      }
    } catch (IllegalTransformationException ex){
      _statements.clear();
    }
  }

  /**
   * Get the outer do statements. First do statement in the nested group.
   * @return XdoStatement holding information about the outer do statement.
   */
  public XdoStatement getOuterStatement(){
    return _statements.isEmpty() ? null : _statements.get(0);
  }

  /**
   * Get the inner do statements. Last do statement in the nested group.
   * @return XdoStatement holding information about the inner do statement.
   */
  public XdoStatement getInnerStatement(){
    return _statements.isEmpty() ? null : _statements.get(_statements.size()-1);
  }

}
