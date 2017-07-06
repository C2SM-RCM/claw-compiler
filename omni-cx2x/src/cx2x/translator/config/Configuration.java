/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */

package cx2x.translator.config;

import cx2x.ClawVersion;
import cx2x.translator.language.helper.accelerator.AcceleratorDirective;
import cx2x.translator.language.helper.target.Target;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration class is used to read the configuration file and expose its
 * information to the translator.
 *
 * @author clementval
 */
public class Configuration {

  // Specific keys
  private static final String DEFAULT_TARGET = "default_target";
  private static final String DEFAULT_DIRECTIVE = "default_directive";
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
  private static final String VERSION_ATTR = "version";
  private static final String TRIGGER_ATTR = "trigger";
  // Specific values
  private static final String DEPENDENT_GR_TYPE = "dependent";
  private static final String INDEPENDENT_GR_TYPE = "independent";
  private static final String DIRECTIVE_TR_TYPE = "directive";
  private static final String TRANSLATION_UNIT_TR_TYPE = "translation_unit";
  private final Document _document;
  private final Map<String, String> _parameters;
  private final List<GroupConfiguration> _groups;
  private final OpenAccConfiguration _openacc;
  private boolean _forcePure = false;
  private int _maxColumns; // Max column for code formatting

  /**
   * Constructs a new configuration object from the give configuration file.
   *
   * @param configPath Path to the configuration file.
   * @param schemaPath Path to the XSD schema for validation.
   */
  public Configuration(String configPath, String schemaPath) throws Exception {
    _parameters = new HashMap<>();
    _groups = new ArrayList<>();
    DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    _document = builder.parse(configPath);

    try {
      validate(schemaPath);
    } catch(Exception e) {
      throw new Exception("Error: Configuration file is not well formatted: "
          + e.getMessage());
    }

    Element root = _document.getDocumentElement();


    String version = root.getAttribute(VERSION_ATTR);
    checkVersion(version);


    Element global =
        (Element) root.getElementsByTagName(GLOBAL_ELEMENT).item(0);
    Element groups =
        (Element) root.getElementsByTagName(GROUPS_ELEMENT).item(0);

    readParameters(global);
    readGroups(groups);

    _openacc = new OpenAccConfiguration(_parameters);
  }

  /**
   * Constructs basic configuration object.
   *
   * @param dir    Accelerator directive language.
   * @param target Target architecture.
   */
  public Configuration(AcceleratorDirective dir, Target target) {
    _parameters = new HashMap<>();
    _parameters.put(DEFAULT_DIRECTIVE, dir.toString());
    _parameters.put(DEFAULT_TARGET, target.toString());
    _document = null;
    _openacc = new OpenAccConfiguration(_parameters);
    _groups = new ArrayList<>();
  }

  /**
   * Validate the configuration file with the XSD schema.
   *
   * @param xsdPath Path to the XSD schema.
   * @throws Exception If configuration file is not valid.
   */
  private void validate(String xsdPath)
      throws Exception
  {
    SchemaFactory factory =
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Source schemaFile = new StreamSource(new File(xsdPath));
    Schema schema = factory.newSchema(schemaFile);
    Validator validator = schema.newValidator();
    validator.validate(new DOMSource(_document));
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
   * Get the OpenACC specific configuration information.
   *
   * @return The OpenACC configuration object.
   */
  public OpenAccConfiguration openACC() {
    return _openacc;
  }

  /**
   * Get all the group configuration information.
   *
   * @return List of group configuration.
   */
  public List<GroupConfiguration> getGroups() {
    return _groups;
  }

  /**
   * Get the current accelerator directive defined in the configuration.
   *
   * @return Current accelerator value.
   */
  public AcceleratorDirective getCurrentDirective() {
    return AcceleratorDirective.fromString(getParameter(DEFAULT_DIRECTIVE));
  }

  /**
   * Get the current target defined in the configuration or by the user on
   * the command line.
   *
   * @return Current target value.
   */
  public Target getCurrentTarget() {
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
   * Read all the group element and store their information in a list of
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
        String trigger_type = g.getAttribute(TRIGGER_ATTR);
        GroupConfiguration.TriggerType triggerType;
        switch(trigger_type) {
          case DIRECTIVE_TR_TYPE:
            triggerType = GroupConfiguration.TriggerType.DIRECTIVE;
            break;
          case TRANSLATION_UNIT_TR_TYPE:
            triggerType = GroupConfiguration.TriggerType.TRANSLATION_UNIT;
            break;
          default:
            throw new Exception("Invalid trigger type specified.");
        }
        _groups.add(new GroupConfiguration(name, gType, triggerType, cPath,
            transClass));
      }
    }
  }

  /**
   * Set the user defined target in the configuration.
   *
   * @param option Option passed as argument. Has priority over configuration
   *               file.
   */
  public void setUserDefinedTarget(String option) {
    if(option != null) {
      _parameters.put(DEFAULT_TARGET, option);
    }
  }

  /**
   * Set the user defined directive in the configuration.
   *
   * @param option Option passed as argument. Has priority over configuration
   *               file.
   */
  public void setUserDefineDirective(String option) {
    if(option != null) {
      _parameters.put(DEFAULT_DIRECTIVE, option);
    }
  }

  /**
   * Enable the force pure option.
   */
  public void setForcePure() {
    _forcePure = true;
  }

  /**
   * Check whether the force pure option is enabled or not.
   *
   * @return If true, the function is enabled. Disabled otherwise.
   */
  public boolean isForcePure() {
    return _forcePure;
  }


  /**
   * Check whether the configuration file version is high enough with the
   * compiler version.
   *
   * @param configVersion Version string from the configuration file.
   * @throws Exception If the configuration version is not high enough.
   */
  private void checkVersion(String configVersion) throws Exception {
    int[] configMajMin = getMajorMinor(configVersion);
    int[] compilerMajMin = getMajorMinor(ClawVersion.getVersion());

    if(configMajMin[0] < compilerMajMin[0]
        || (configMajMin[0] == compilerMajMin[0]
        && configMajMin[1] < compilerMajMin[1]))
    {
      throw new Exception("Configuration version is too small compared to " +
          "CLAW FORTRAN Compiler version: >= " + compilerMajMin[0] + "." +
          compilerMajMin[1]);
    }
  }

  /**
   * Extract major and minor version number from the full version String.
   *
   * @param version Full version String. <major>.<minor>.<fixes>
   * @return Two dimensional array with the major number at index 0 and the
   * minor at index 1.
   * @throws Exception If the version String is not of the correct format.
   */
  private int[] getMajorMinor(String version) throws Exception {
    Pattern p = Pattern.compile("^(\\d+)\\.(\\d+)\\.?(\\d+)?");
    Matcher m = p.matcher(version);
    if(!m.matches()) {
      throw new Exception("Configuration version not well formatted");
    }

    int major = Integer.parseInt(m.group(1));
    int minor = Integer.parseInt(m.group(2));
    return new int[]{major, minor};
  }

  /**
   * Get the defined max column parameter.
   *
   * @return Int value representing the max column.
   */
  public int getMaxColumns() {
    return _maxColumns;
  }

  /**
   * Set the max column value.
   *
   * @param value New value of the max column parameter.
   */
  public void setMaxColumns(int value) {
    _maxColumns = value;
  }

}
