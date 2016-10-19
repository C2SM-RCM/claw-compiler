/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.transformer;

import cx2x.translator.config.GroupConfiguration;
import cx2x.translator.transformation.openacc.OpenAccContinuation;
import cx2x.xcodeml.transformation.*;
import cx2x.xcodeml.xnode.Xnode;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ClawTransformer stores all transformation groups applied during the
 * translation.
 *
 * @author clementval
 */

public class ClawTransformer implements Transformer {

  // Hold all transformation groups
  private final Map<Class, TransformationGroup> _tGroups;
  // Hold cross-transformation elements
  private final Map<Element, Object> _crossTransformationTable;
  // Hold the module file cache
  private final ModuleCache _modCache;
  private int _transformationCounter = 0;
  private int _maxColumns;


  /**
   * ClawTransformer ctor. Creates the transformation groups needed for the CLAW
   * transformation and order the accordingly to their interpretation order.
   *
   * @param groups List of transformation groups that define the transformation
   *               order.
   * @param max    Maximum number of columns.
   */
  public ClawTransformer(List<GroupConfiguration> groups, int max) {
    /*
     * Use LinkedHashMap to be able to iterate through the map
     * entries with the insertion order.
     */
    _tGroups = new LinkedHashMap<>();
    for(GroupConfiguration g : groups) {
      switch(g.getType()) {
        case DEPENDENT:
          _tGroups.put(g.getTransformationClass(),
              new DependentTransformationGroup(g.getName()));
          break;
        case INDEPENDENT:
          _tGroups.put(g.getTransformationClass(),
              new IndependentTransformationGroup(g.getName()));
          break;
      }
    }

    // Internal transformations not specified by default configuration or user
    _tGroups.put(OpenAccContinuation.class,
        new IndependentTransformationGroup("internal-open-acc-continuation"));

    _crossTransformationTable = new HashMap<>();

    _modCache = new ModuleCache();

    _maxColumns = max;
  }

  /**
   * @see Transformer#addTransformation(Transformation)
   */
  public void addTransformation(Transformation t) {
    if(_tGroups.containsKey(t.getClass())) {
      _tGroups.get(t.getClass()).add(t);
    }
  }

  /**
   * @see Transformer#getGroups()
   */
  public Map<Class, TransformationGroup> getGroups() {
    return _tGroups;
  }

  /**
   * Get the next extraction counter value.
   *
   * @return Transformation counter value.
   */
  public int getNextTransformationCounter() {
    return _transformationCounter++;
  }

  /**
   * @see Transformer#getModCache()
   */
  @Override
  public ModuleCache getModCache() {
    return _modCache;
  }

  /**
   * @see Transformer#getMaxColumns()
   */
  @Override
  public int getMaxColumns() {
    return _maxColumns;
  }

  /**
   * @param max Max number of columns.
   * @see Transformer#setMaxColumns(int)
   */
  @Override
  public void setMaxColumns(int max) {
    _maxColumns = max;
  }

  /**
   * Get a stored element from a previous transformation.
   *
   * @param key Key to use to retrieve the element.
   * @return The stored element if present. Null otherwise.
   */
  public Object hasElement(Xnode key) {
    if(_crossTransformationTable.containsKey(key.getElement())) {
      return _crossTransformationTable.get(key.getElement());
    }
    return null;
  }

  /**
   * Store a Xnode from a transformation for a possible usage in another
   * transformation. If a key is already present, the element is overwritten.
   *
   * @param key   The element acting as a key.
   * @param value The element to be stored.
   */
  public void storeElement(Xnode key, Object value) {
    if(_crossTransformationTable.containsKey(key.getElement())) {
      _crossTransformationTable.remove(key.getElement());
    }
    _crossTransformationTable.put(key.getElement(), value);
  }
}
