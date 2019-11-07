package org.reldb.wrapd.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.reldb.wrapd.version.VersionProxy;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Configuration {

	private static final String RootNodeName = "configuration";

	public static final String INSTALLER_ADMIN_NAME = "installer_admin_name";
	public static final String INSTALLER_ADMIN_PASSWORD = "installer_admin_password";
	public static final String DATABASE_NAME = "database_name";
	public static final String DATABASE_USER = "database_user";
    public static final String DATABASE_PASSWORD = "database_password";
    public static final String DATABASE_TABLENAME_PREFIX = "database_tablename_prefix";
	public static final String DATABASE_SERVER = "database_server";
    public static final String DATABASE_NONSTANDARD_PORT = "database_nonstandard_port";
    public static final String DATABASE_DEFINITION = "database_definition";
    public static final String SMTP_SERVER = "smtp_server";
    public static final String SMTP_SERVER_PORT = "smtp_server_port";	
    public static final String SMTP_SERVER_AUTHNAME = "smtp_server_authname";
    public static final String SMTP_SERVER_AUTHPASS = "smtp_server_authpass";
    public static final String SMTP_SERVER_FROM_EMAIL = "smtp_server_from_email";
    public static final String SUPPORT_CONTACT = "support_contact";
    public static final String AUTH_ALLOW_USER_REGISTRATION = "auth_allow_user_registration";
    public static final String AUTH_ACTIVATE_ACCOUNT_GROUP = "auth_activate_account_group";
    public static final String URL_BASE = "url_base";
	
	/** Define default configuration file here. */
	private static void buildDefaultConfiguration(Document doc, Element parent) {
		addItem(doc, parent, INSTALLER_ADMIN_NAME, "admin", "needed for initial installation and updates");
		addItem(doc, parent, INSTALLER_ADMIN_PASSWORD, (new RandomString(10).nextString()), "needed for initial installation and updates");
		addItem(doc, parent, DATABASE_NAME, "mydatabase");
		addItem(doc, parent, DATABASE_USER, "dbuser");
		addItem(doc, parent, DATABASE_PASSWORD, "dbpass");
		addItem(doc, parent, DATABASE_TABLENAME_PREFIX, VersionProxy.getVersion().getInternalProductName().toLowerCase() + "_", "optional");
		addItem(doc, parent, DATABASE_SERVER, "localhost");
		addItem(doc, parent, DATABASE_NONSTANDARD_PORT, " ", "optional");
		addItem(doc, parent, DATABASE_DEFINITION, org.reldb.wrapd.db.postgresql.WrapdDB.class.getPackageName());
		addItem(doc, parent, SMTP_SERVER, "localhost");
		addItem(doc, parent, SMTP_SERVER_PORT, " ", "optional - defaults to 25");
		addItem(doc, parent, SMTP_SERVER_AUTHNAME, " ", "optional - account ID for SMTP authorisation");
		addItem(doc, parent, SMTP_SERVER_AUTHPASS, " ", "optional - account password for SMTP authorisation");
		addItem(doc, parent, SMTP_SERVER_FROM_EMAIL, " ", "optional - all email will come from this address");
		addItem(doc, parent, SUPPORT_CONTACT, "dave@armchair.mb.ca");
		addItem(doc, parent, AUTH_ALLOW_USER_REGISTRATION, "yes", "no = users cannot register accounts; yes = (default) users can register & self-activate; admin = users can register, admins activate accounts.");
		addItem(doc, parent, AUTH_ACTIVATE_ACCOUNT_GROUP, "Administrator", "optional - group that can authorise account requests");
		addItem(doc, parent, URL_BASE, " ", "optional - base URL for this application. We'll try to determine it if we can, but it might not be correct.");
	}
	
	private static void addItem(Document doc, Element parent, String elementName, String text, String comment) {
		parent.appendChild(doc.createComment(comment));
		Element element = doc.createElement(elementName);
		element.appendChild(doc.createTextNode(text));
		parent.appendChild(element);	
	}
	
	private static void addItem(Document doc, Element parent, String elementName, String text) {
		Element element = doc.createElement(elementName);
		element.appendChild(doc.createTextNode(text));
		parent.appendChild(element);
	}
	
	private static void writeDefaultConfiguration() throws ParserConfigurationException, TransformerException, IOException {
		// Create empty .dtd file
		String dtdFileName = RootNodeName + ".dtd";
		File dtdFile = new File(dtdFileName);
		dtdFile.createNewFile();
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		// root
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(RootNodeName);
		doc.appendChild(rootElement);

		// children
		buildDefaultConfiguration(doc, rootElement);

		// write the content to an xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		
		// DOCTYPE
		DOMImplementation domImpl = doc.getImplementation();
		DocumentType doctype = domImpl.createDocumentType("doctype", RootNodeName, dtdFileName);
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
		
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(getConfigurationFileName()));
		transformer.transform(source, result);

		System.out.println("New configuration file " + getConfigurationFileName() + " written.");
	}

	private static Map<String, String> readConfiguration() throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(getConfigurationFileName());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		Map<String, String> configuration = new HashMap<String, String>();

		NodeList nList = doc.getElementsByTagName(RootNodeName);
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				NodeList cNodes = ((Element) nNode).getChildNodes();
				for (int nodeNum = 0; nodeNum < cNodes.getLength(); nodeNum++) {
					Node cNode = cNodes.item(nodeNum);
					if (cNode.getNodeType() == Node.ELEMENT_NODE) {
						Element cElement = (Element) cNode;
						String key = cElement.getTagName();
						String value = cElement.getTextContent().trim();
						configuration.put(key, value);						
					}
				}
			}
		}
		
		return configuration;
	}
	
	private static Map<String, String> cachedConfiguration = null;
	
	private static Map<String, String> getConfiguration() throws ParserConfigurationException, TransformerException, SAXException, IOException {
		if (cachedConfiguration != null)
			return cachedConfiguration;
		if (!(new File(getConfigurationFileName())).exists())
			writeDefaultConfiguration();
		cachedConfiguration = readConfiguration();
		return cachedConfiguration;
	}
		
	public static String getConfigurationFileName() {
		return VersionProxy.getVersion().getInternalProductName() + "Configuration.xml";
	}
	
	public static void checkConfiguration() throws IOException {
        try {
			Configuration.getConfiguration();
		} catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
			throw new IOException("Unable to obtain configuration from " + getConfigurationFileName() + ": " + e.getMessage());
		}
	}
	
	public static String getValue(String key) {
		try {
			return getConfiguration().get(key);
		} catch (ParserConfigurationException | TransformerException | SAXException | IOException e) {
			return null;
		}
	}
	
	public static String getTechnicalContactEmail() {
		String contact = Configuration.getValue(Configuration.SUPPORT_CONTACT);
		if (contact == null)
			contact = VersionProxy.getVersion().getDeveloperEmail();
		return contact;
	}

	public static String getTechnicalContactEmailHTML() {
		String email = Configuration.getTechnicalContactEmail();
		return "<a href=\"mailto:" + email + "?subject=" + VersionProxy.getVersion().getPageTitle() + "\">" + email + "</a>";
	}
	
}
