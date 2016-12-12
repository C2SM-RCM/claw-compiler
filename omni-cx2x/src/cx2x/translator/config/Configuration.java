/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */


package cx2x.translator.config;

import cx2x.translator.language.helper.accelerator.AcceleratorDirective;
import cx2x.translator.language.helper.target.Target;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class is used to read the configuration file and expose its
 * information to the translator.
 *
 * @author clementval
 */
public class Configuration {

  // Specific keys
  public static final String DEFAULT_TARGET = "default_target";
  public static final String DEFAULT_DIRECTIVE = "default_directive";
  // Element and attribute names
  private static final String GLOBAL_ELEMENT = "global";
  private static final String GROUPS_ELEMENT = "groups";
  private static final String GROUP_ELEMENT = "group";
  private static final String PARAMETER_ELEMENT = "parameter";
  private static final String CLASS_ATTR = "class";
  private static final String NAME_ATTR = "name";
  private static final String TYPE_ATTR = "type";
  private static final String KEY_ATTR = "key";
  private static final String VALUE_ATTR = "value";
  // Specific values
  private static final String DEPENDENT_GR_TYPE = "dependent";
  private static final String INDEPENDENT_GR_TYPE = "independent";
  private final Document _document;
  private final Map<String, String> _parameters;
  private final List<GroupConfiguration> _groups;

  /**
   * Contructs a new configuration object from the give configuration file.
   *
   * @param path Path to the configuration file.
   */
  public Configuration(String path) throws Exception {
    _parameters = new HashMap<>();
    _groups = new ArrayList<>();
    DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    _document = builder.parse(path);
    // TODO XSD validation

    Element root = _document.getDocumentElement();
    Element global =
        (Element) root.getElementsByTagName(GLOBAL_ELEMENT).item(0);
    Element groups =
        (Element) root.getElementsByTagName(GROUPS_ELEMENT).item(0);

    readParameters(global);
    readGroups(groups);
  }

  /**
   * Get value of a parameter.
   *
   * @param key Key of the parameter.
   * @return Value of the parameter. Null if parameter doesn't exists.
   */
  public String getParameter(String key) {
    return (_parameters.containsKey(key)) ? _parameters.get(key) : null;
  }

  /**
   * Get all the group configuration information.
   *
   * @return List of group configuration.
   */
  public List<GroupConfiguration> getGroups() {
    return _groups;
  }

  public AcceleratorDirective getDefaultDirective(){
    return AcceleratorDirective.fromString(getParameter(DEFAULT_DIRECTIVE));
  }

  public Target getDefaultTarget(){
    return Target.fromString(getParameter(DEFAULT_TARGET));
  }

  /**
   * Read all the parameter element and store their key/value pair in the map.
   *
   * @param globalElement Parent element "global" for the parameters.
   */
  private void readParameters(Element globalElement) {
    NodeList parameters = globalElement.getElementsByTagName(PARAMETER_ELEMENT);
    for(int i = 0; i < parameters.getLength(); ++i) {
      Element e = (Element) parameters.item(i);
      _parameters.put(e.getAttribute(KEY_ATTR), e.getAttribute(VALUE_ATTR));
    }
  }

  /**
   * Read all the group element and store theie information in a list of
   * GroupConfiguration objects.
   *
   * @param groupsNode Parent element "groups" for the group elements.
   * @throws Exception Group information not valid.
   */
  private void readGroups(Element groupsNode) throws Exception {
    NodeList groupElements = groupsNode.getElementsByTagName(GROUP_ELEMENT);
    for(int i = 0; i < groupElements.getLength(); ++i) {
      if(groupElements.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element g = (Element) groupElements.item(i);
        String name = g.getAttribute(NAME_ATTR);
        String type = g.getAttribute(TYPE_ATTR);
        GroupConfiguration.GroupType gType;
        switch(type) {
          case DEPENDENT_GR_TYPE:
            gType = GroupConfiguration.GroupType.DEPENDENT;
            break;
          case INDEPENDENT_GR_TYPE:
            gType = GroupConfiguration.GroupType.INDEPENDENT;
            break;
          default:
            throw new Exception("Invalid group type specified.");
        }
        String cPath = g.getAttribute(CLASS_ATTR);
        if(cPath == null || cPath.isEmpty()) {
          throw new Exception("Invalid group class transformation definition.");
        }
        Class transClass;
        try {
          // Check if class is there
          transClass = Class.forName(cPath);
        } catch(ClassNotFoundException e) {
          throw new Exception("Transformation class " + cPath +
              " not available");
        }
        _groups.add(new GroupConfiguration(name, gType, cPath, transClass));
      }
    }
  }
}
