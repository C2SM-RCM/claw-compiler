/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.transformation.claw.parallelize;

import cx2x.translator.language.common.OverPosition;

/**
 * Hold various information about the promotion of a variable.
 *
 * @author clementval
 */
public class PromotionInfo {

  private final String _identifier;
  private final int _baseDimension;
  private final int _targetDimension;
  private final String _targetType;
  private OverPosition _overPosition = OverPosition.BEFORE; // Default
  private PromotionType _promotionType = PromotionType.ARRAY_TO_ARRAY; //Default

  /**
   * Constructs a new PromotionInfo object with all its information.
   *
   * @param id              Identifier of the promoted variable.
   * @param baseDimension   Number of dimensions before the promotion.
   * @param targetDimension Number of dimensions after the promotion.
   * @param targetType      Type if after the promotion.
   */
  public PromotionInfo(String id, int baseDimension, int targetDimension,
                       String targetType)
  {
    _identifier = id;
    _baseDimension = baseDimension;
    _targetDimension = targetDimension;
    _targetType = targetType;
  }

  /**
   * Get the associated identifier.
   *
   * @return Identifier.
   */
  public String getIdentifier() {
    return _identifier;
  }

  /**
   * Get associated OverPosition.
   *
   * @return OverPosition value.
   */
  public OverPosition getOverPosition() {
    return _overPosition;
  }

  /**
   * Set the OverPosition value.
   *
   * @param overPosition New OverPosition value.
   */
  public void setOverPosition(OverPosition overPosition) {
    _overPosition = overPosition;
  }

  /**
   * Check whether the variable was a scalar before the promotion.
   *
   * @return True if the variable was a scalar. False otherwise.
   */
  public boolean wasScalar() {
    return _baseDimension == 0;
  }

  /**
   * Get the type id after the promotion.
   *
   * @return Type id.
   */
  public String getTargetType() {
    return _targetType;
  }

  /**
   * Get the number of dimension between the base and the target.
   *
   * @return Number of dimension.
   */
  public int diffDimension() {
    return _targetDimension - _baseDimension;
  }

  /**
   * Get the promotion type.
   *
   * @return Current promotion type value.
   */
  public PromotionType getPromotionType() {
    return _promotionType;
  }

  /**
   * Set the promotion type value.
   *
   * @param promotionType New promotion type value.
   */
  public void setPromotionType(PromotionType promotionType) {
    _promotionType = promotionType;
  }


  public enum PromotionType {SCALAR_TO_ARRAY, ARRAY_TO_ARRAY}

}
