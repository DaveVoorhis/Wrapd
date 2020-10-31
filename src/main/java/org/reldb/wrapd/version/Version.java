package org.reldb.wrapd.version;

//import org.apache.maven.model.Model;
//import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
//import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Version {

    // No instances
    private Version() {
    }

    private static String cachedVersion = null;

    public static String getVersionString() {
        if (cachedVersion == null) {
            var versionPropertiesStream = Version.class.getResourceAsStream("/version.properties");
            if (versionPropertiesStream == null) {
                cachedVersion = "?";
            } else {
                var versionProperties = new Properties();
                try {
                    versionProperties.load(versionPropertiesStream);
                    cachedVersion = versionProperties.getProperty("version", "??");
                } catch (IOException ioe) {
                    cachedVersion = "???";
                }
            }
        }
        return cachedVersion;
    }

}
