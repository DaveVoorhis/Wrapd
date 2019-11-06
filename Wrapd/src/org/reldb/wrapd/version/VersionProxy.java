package org.reldb.wrapd.version;

public class VersionProxy {

	private static Version delegate = new VersionDefault();
	
	public static void setProxy(Version version) {
		delegate = version;
	}

	public static Version getVersion() {
		return delegate;
	}
	
}
