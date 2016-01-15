package cx2x.translator.transformation;

import cx2x.xcodeml.xelement.XcodeProg;
import cx2x.translator.exception.*;
import cx2x.translator.transformer.Transformer;

/**
 * An independent transformation group applies each transformation without
 * checking with any other transformation in the pipeline.
 *
 * @author Valentin Clement
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
   * @see TransformationGroup#applyTranslations(XcodeProg, Transformer)
   */
  public void applyTranslations(XcodeProg xcodeml, Transformer transformer)
    throws IllegalTransformationException
  {
    for(T translation : _translations){

      try {
        translation.transform(xcodeml, transformer, null);
      } catch (IllegalTransformationException itex) {
        // Catch the exception to add line information and rethrow it
        if(itex.getStartLine() == 0){
          itex.setStartLine(translation.getStartLine());
          throw itex;
        }
      }

    }
  }
}
