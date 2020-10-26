package org.reldb.toolbox.configuration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Configuration {

	private static class ConfigurationNode {
		public final String comment;
		public final boolean fromFile;
		
		private String value;
		
		public ConfigurationNode(String value, String comment, boolean fromFile) {
			this.value = value;
			this.comment = comment;
			this.fromFile = fromFile;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	private static String baseDir = "./";
	
	private static final String RootNodeName = "configuration";

	private static Map<String, Map<String, ConfigurationNode>> configuration = null;

	private static HashSet<String> registrations = new HashSet<>();
	
	private static void initialise() {
		if (configuration != null)
			return;
		try {
			checkConfiguration();
		} catch (IOException e) {
			System.err.println("Configuration: problem reading or writing configuration: " + e);
		}
	}

	private static void put(String groupName, String elementName, String value, String comment, boolean fromFile) {
		var group = configuration.get(groupName);
		if (group == null)
			group = new HashMap<>();
		var element = group.get(elementName);
		group.put(elementName, new ConfigurationNode((element != null && element.fromFile) ? element.value : value, comment, fromFile));
		configuration.put(groupName, group);		
	}
	
	static void add(String groupName, String elementName, String value, String comment, boolean fromFile) {
		initialise();
		put(groupName, elementName, value, comment, fromFile);
	}
	
	private static ConfigurationNode getConfigurationNode(String groupName, String elementName) {
		var group = configuration.get(groupName);
		if (group == null)
			return null;
		return group.get(elementName);
	}

	/**
	 * Get an <b>un-trimmmed</b> configuration value. Null if it can't be found. Untrimmed values are appropriate for passwords and similar
	 * content that need to preserve leading/trailing spaces.
	 * 
	 * @param groupName - configuration group
	 * @param elementName - element name within group
	 * @return - String or null
	 */
	public static String getUntrimmedValue(String groupName, String elementName) {
		initialise();
		var node = getConfigurationNode(groupName, elementName);
		return node == null ? null : node.getValue();
	}
	
	/**
	 * Get a trimmed configuration value. Null if it can't be found.
	 * 
	 * @param groupName - configuration group
	 * @param elementName - element name within group
	 * @return - String or null
	 */
	public static String getValue(String groupName, String elementName) {
		initialise();
		var value = getUntrimmedValue(groupName, elementName);
		return (value == null) ? null : value.trim();
	}
	
	/**
	 * Get a configuration value that represents a boolean. Return true if it spells (case-insensitive) 'true', 'false' otherwise.
	 * 
	 * @param groupName - configuration group
	 * @param elementName - element name within group
	 * @return - boolean
	 */
	public static boolean getBooleanValue(String groupName, String elementName) {
		var value = getValue(groupName, elementName);
		return value != null && value.toLowerCase().equals("true");
	}
	
	static void writeConfiguration() throws TransformerException, ParserConfigurationException {		
		// write the content to an xml file
		var transformerFactory = TransformerFactory.newInstance();
		var transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		
		var docFactory = DocumentBuilderFactory.newInstance();
		var docBuilder = docFactory.newDocumentBuilder();
		
		var doc = docBuilder.newDocument();
		var rootElement = doc.createElement(RootNodeName);
		doc.appendChild(rootElement);
		
		configuration.forEach((groupName, group) -> {
			Element groupElement = doc.createElement(groupName);
			group.forEach((elementName, element) -> {
				if (element.comment != null)
					groupElement.appendChild(doc.createComment(element.comment));
				Element elementNode = doc.createElement(elementName);
				elementNode.appendChild(doc.createTextNode(element.value));
				groupElement.appendChild(elementNode);
			});
			rootElement.appendChild(groupElement);
		});
		
		// DOCTYPE
		var domImpl = doc.getImplementation();
		var doctype = domImpl.createDocumentType("doctype", RootNodeName, getDTDName());
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
		
		var source = new DOMSource(doc);
		var result = new StreamResult(new File(getConfigurationFileName()));
		transformer.transform(source, result);		
	}

	private static String getDTDName() {
		return RootNodeName + ".dtd";
	}

	private static String getDTDFileName() {
		return getLocation() + getDTDName();
	}
	
	private static void writeDefaultConfiguration() throws ParserConfigurationException, TransformerException, IOException {
		// Create empty .dtd file
		String dtdFileName = getDTDFileName();
		File dtdFile = new File(dtdFileName);
		if (!dtdFile.createNewFile()) {
			System.out.println("NOTE: Unable to create DTD file " + dtdFileName);
		}
		
		var docFactory = DocumentBuilderFactory.newInstance();
		var docBuilder = docFactory.newDocumentBuilder();
		
		var doc = docBuilder.newDocument();
		var rootElement = doc.createElement(RootNodeName);
		doc.appendChild(rootElement);

		writeConfiguration();
		
		System.out.println("New configuration file " + getConfigurationFileName() + " written.");
	}

	private static void readConfiguration() throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(getConfigurationFileName());
		var dbFactory = DocumentBuilderFactory.newInstance();
		var dBuilder = dbFactory.newDocumentBuilder();
		var doc = dBuilder.parse(fXmlFile);

		var nList = doc.getElementsByTagName(RootNodeName);
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node configuration = nList.item(temp);
			if (configuration.getNodeType() == Node.ELEMENT_NODE) {
				var groupNodes = configuration.getChildNodes();
				for (int nodeNum = 0; nodeNum < groupNodes.getLength(); nodeNum++) {
					Node groupNode = groupNodes.item(nodeNum);
					if (groupNode.getNodeType() == Node.ELEMENT_NODE) {
						var group = (Element)groupNode;
						var groupName = group.getTagName();
						var elementNodes = group.getChildNodes();
						for (int elementNum = 0; elementNum < elementNodes.getLength(); elementNum++) {
							var elementNode = elementNodes.item(elementNum);
							if (elementNode.getNodeType() == Node.ELEMENT_NODE) {
								var element = (Element)elementNode;
								String elementName = element.getTagName();
								String value = element.getTextContent().trim();
								var configurationNode = getConfigurationNode(groupName, elementName);
								if (configurationNode == null)
									put(groupName, elementName, value, null, true);
								else
									configurationNode.setValue(value);
							}
						}
					}
				}
			}
		}
	}

	private static void checkConfiguration() throws IOException {
		if (configuration == null)
			configuration = new HashMap<>();
        try {
    		if (!(new File(getConfigurationFileName())).exists())
    			writeDefaultConfiguration();
    		readConfiguration();
		} catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
			throw new IOException("Unable to obtain configuration from " + getConfigurationFileName() + ": " + e.getMessage());
		}
	}

	public static void setLocation(String baseDir) throws IOException {
		Configuration.baseDir = (baseDir == null) ? "./" : baseDir;
		configuration = null;
		checkConfiguration();
	}
	
	public static String getLocation() {
		return (Configuration.baseDir.endsWith("/")) ? Configuration.baseDir : Configuration.baseDir + "/";
	}
	
	public static String getConfigurationFileName() {
		return getLocation() + "Configuration.xml";
	}
	
	public static void register(Class<? extends org.reldb.toolbox.configuration.ConfigurationSettings> settingsClass) {
		if (registrations.contains(settingsClass.getCanonicalName()))
			return;
		try {
			var configurationSettings = settingsClass.getConstructor((Class<?>[])null).newInstance((Object[])null);
			var registration = settingsClass.getMethod("registration", (Class<?>[])null);
			registration.invoke(configurationSettings, (Object[])null);
			var getSettings = settingsClass.getMethod("getSettings", (Class<?>[])null);
			@SuppressWarnings("unchecked")
			var settings = (Map<String, ConfigurationSettings.ConfigurationSetting>)getSettings.invoke(configurationSettings, (Object[])null);
			settings.values().forEach(setting -> add(settingsClass.getCanonicalName(), setting.element, setting.value, setting.comment, false));
			writeConfiguration();
			registrations.add(settingsClass.getCanonicalName());
		} catch (Throwable t) {
			System.err.println("Configuration: unable to register configuration settings: " + settingsClass.getCanonicalName() + " due to: " + t);
			t.printStackTrace();
		}
	}
	
}
