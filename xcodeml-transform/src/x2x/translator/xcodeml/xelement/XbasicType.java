package x2x.translator.xcodeml.xelement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The XbasicType represents the basicType (3.3) element in XcodeML intermediate
 * representation.
 *
 * Elements:
 * - Optional:
 *   - kind TODO
 *   - len TODO
 *   - arrayIndex TODO
 *   - indexRange (XindexRange)
 *   - coShape TODO not needed for the moment
 * Attributes:
 * - Requited: type (text), ref (text)
 * - Optional: is_public (bool), is_private (bool), is_pointer (bool),
 *             is_target (bool), is_external (bool),is_intrinsic (bool),
 *             is_optional (bool), is_save (bool), is_parameter (bool),
 *             is_allocatable (bool), intent (text: in, out, inout)
 *   TODO all attributes
 */

public class XbasicType extends Xtype {

  private String _ref;
  private int _dimension = 0;
  private int _length = 0;
  private boolean _isArray = false;
  private boolean _hasLength = false;
  private XindexRange[] _dimensionRanges = null;

  public XbasicType(Element element){
    super(element);
    readBasicTypeInformation();
  }

  private void readBasicTypeInformation(){
    _ref = XelementHelper.getAttributeValue(_element,
      XelementName.ATTR_REF);

    // is array ?
    _dimension = XelementHelper.findNumberOfRange(_element);
    if (_dimension > 0){
      _isArray = true;
      _dimensionRanges = new XindexRange[_dimension];
      NodeList ranges = XelementHelper.findIndexRanges(_element);
      for(int i = 0; i < _dimension; ++i){
        _dimensionRanges[i] = new XindexRange((Element)ranges.item(i));
      }
    }

    // has length ?
    Element length = XelementHelper.findLen(_element);
    if(length != null){
      _hasLength = true;
      // TODO have a length object with information
    }
  }

  public XindexRange getDimensions(int index){
    if(index >= _dimensionRanges.length || index < 0){
      return null;
    }
    return _dimensionRanges[index];
  }

  public boolean isArray(){
    return _isArray;
  }

  public boolean hasLength(){
    return _hasLength;
  }

  public int getDimensions(){
    return _dimension;
  }

  public String getRef(){
    return _ref;
  }

}
