/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.transformation;

import cx2x.xcodeml.xelement.XcodeProgram;
import cx2x.xcodeml.exception.*;

/**
 * An independent transformation group applies each transformation without
 * checking with any other transformation in the pipeline.
 *
 * @author clementval
 */

public class IndependentTransformationGroup<T extends Transformation<? super T>>
    extends TransformationGroup<T>
{
  /**
   * IndependentTransformationGroup ctor
   * @param name A friendly name to describe the transformation group.
   */
  public IndependentTransformationGroup(String name) {
    super(name);
  }

  /**
   * @see TransformationGroup#applyTranslations(XcodeProgram, Transformer)
   */
  public void applyTranslations(XcodeProgram xcodeml, Transformer transformer)
    throws Exception
  {
    for(T translation : _translations){
      try {
        translation.transform(xcodeml, transformer, null);
      } catch (IllegalTransformationException itex) {
        // Catch the exception to add line information and rethrow it
        if(itex.getStartLine() == 0){
          itex.setStartLine(translation.getStartLine());
        }
        throw itex;
      } catch (Exception ex){
        throw ex;
      }
    }
  }
}
