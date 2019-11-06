package org.reldb.wrapd.version;

public class VersionDefault implements Version {
	
	public String getVersionString() {
		return "" + getVersionNumber();
	}
	
	public int getVersionNumber() {
		return 1;
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
