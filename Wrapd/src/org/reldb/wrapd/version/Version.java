package org.reldb.wrapd.version;

public interface Version {
	public String getVersionString();
	
	public int getVersionNumber();

	/** Product name for display. */
	public String getProductName();
	
	/** Product name for constructing identifiers. Must NOT contain spaces! */
	public String getInternalProductName();

	public String getProductShortDescription();

	public String getPageTitle();

	public String getEntryPoint();

	public String getDeveloperEmail();

}