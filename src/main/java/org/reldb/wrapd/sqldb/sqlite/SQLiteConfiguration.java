package org.reldb.wrapd.sqldb.sqlite;

import org.reldb.toolbox.configuration.ConfigurationSettings;

public class SQLiteConfiguration extends ConfigurationSettings {

	public static final String INSTALLER_ADMIN_NAME = "installer_admin_name";
	public static final String INSTALLER_ADMIN_PASSWORD = "installer_admin_password";
	public static final String DATABASE_NAME = "database_name";
	public static final String DATABASE_TABLENAME_PREFIX = "database_tablename_prefix";
    public static final String DATABASE_DEFINITION = "database_definition";
    public static final String SUPPORT_CONTACT = "support_contact";
    
	/** Define configuration settings here. */
	public void registration() {
		add(DATABASE_NAME, "sqlitedb.sqlite", "SQLite database");
		add(DATABASE_TABLENAME_PREFIX, "wrapd_", "optional - table name prefix");
	}
	
}
