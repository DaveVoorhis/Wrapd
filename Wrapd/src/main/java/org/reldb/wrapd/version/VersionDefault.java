package org.reldb.wrapd.version;

import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class VersionDefault implements Version {
	
	private String cachedVersion = null;
	
	public String getVersionString() {
		if (cachedVersion == null) {
	        var reader = new MavenXpp3Reader();
	        Model model;
			try {
				model = reader.read(new FileReader("pom.xml"));
				var parent = model.getParent();
				cachedVersion = parent.getVersion();
			} catch (IOException | XmlPullParserException e) {
				cachedVersion = "Dev";
			}
		}
		return cachedVersion;
	}

	/** Product name for display. */
	public String getProductName() {
		return "Wrapd";
	}
	
	/** Product name for constructing identifiers. Must NOT contain spaces! */
	public String getInternalProductName() {
		return "Wrapd";
	}

	public String getProductShortDescription() {
		return "Wrapd Database Wrapper";
	}

	public String getPageTitle() {
		return getProductName() + " " + getVersionString();
	}

	public String getEntryPoint() {
		return "/";
	}

	public String getDeveloperEmail() {
		return "dave@armchair.mb.ca";
	}

}
