/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.xcodeml.transformation;

import cx2x.xcodeml.xelement.XcodeProgram;
import cx2x.xcodeml.exception.*;

import java.util.ArrayList;

/**
 * A TransformationGroup holds transformation units and can apply. Normally,
 * only derived classes of TransformationGroup should be used as they
 * implement applyTranslations.
 *
 * @author clementval
 */

public abstract class TransformationGroup<T extends Transformation> {
  private String _name = null;
  protected ArrayList<T> _translations = null;

  /**
   * TransformationGroup ctor.
   * @param name A friendly name to describe the transformation group.
   */
  public TransformationGroup(String name){
    _name = name;
    _translations = new ArrayList<>();
  }

  /**
   * Get the number of transformation stored in the group.
   * @return The number of transformation in the group.
   */
  public int count(){
    return _translations.size();
  }

  /**
   * Add a new transformation in the group.
   * @param translation The transformation to be added.
   */
  public void add(T translation){
    _translations.add(translation);
  }

  /**
   * Get the name of transformation stored in this group.
   * @return A string representing the name of the transformations.
   */
  public String transformationName(){
    return _name;
  }

  /**
   * Apply all transformation stored in this group. Method transform from each
   * transformation is called.
   * @see Transformation#transform(XcodeProgram, Transformer, Object)
   * @param xcodeml     The XcodeML on which the transformations are applied.
   * @param transformer The transformer used to applied the transformations.
   * @throws IllegalTransformationException if transformation cannot be applied.
   */
  public abstract void applyTranslations(XcodeProgram xcodeml,
    Transformer transformer) throws Exception;

}
