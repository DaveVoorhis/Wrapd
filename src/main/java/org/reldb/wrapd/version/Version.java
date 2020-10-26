package org.reldb.wrapd.version;

import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class Version {
	
	// No instances
	private Version() {}
	
	private static String cachedVersion = null;
	
	public static String getVersionString() {
		if (cachedVersion == null) {
	        var reader = new MavenXpp3Reader();
	        Model model;
			try {
				model = reader.read(new FileReader("../pom.xml"));
				var parent = model.getParent();
				cachedVersion = parent.getVersion();
			} catch (IOException | XmlPullParserException e) {
				cachedVersion = "Dev";
			}
		}
		return cachedVersion;
	}

}
