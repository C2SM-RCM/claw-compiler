package cx2x.xcodeml.xelement;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface Xclonable<T extends XbaseElement> {
  public T cloneObject();
}
