package org.reldb.wrapd.db.postgresql;

import org.reldb.toolbox.configuration.ConfigurationSettings;
import org.reldb.toolbox.configuration.RandomString;

import org.reldb.wrapd.version.VersionProxy;

public class WrapDBConfiguration extends ConfigurationSettings {

	public static final String INSTALLER_ADMIN_NAME = "installer_admin_name";
	public static final String INSTALLER_ADMIN_PASSWORD = "installer_admin_password";
	public static final String DATABASE_NAME = "database_name";
	public static final String DATABASE_USER = "database_user";
    public static final String DATABASE_PASSWORD = "database_password";
    public static final String DATABASE_TABLENAME_PREFIX = "database_tablename_prefix";
	public static final String DATABASE_SERVER = "database_server";
    public static final String DATABASE_NONSTANDARD_PORT = "database_nonstandard_port";
    public static final String DATABASE_DEFINITION = "database_definition";
    public static final String SUPPORT_CONTACT = "support_contact";
    
	/** Define configuration settings here. */
	public void registration() {
		add(INSTALLER_ADMIN_NAME, "admin", "admin account for initial installation and updates");
		add(INSTALLER_ADMIN_PASSWORD, (new RandomString(10).nextString()), "admin password for initial installation and updates");
		add(DATABASE_NAME, "mydatabase", "PostgreSQL database");
		add(DATABASE_USER, "dbuser", "PostgreSQL database user");
		add(DATABASE_PASSWORD, "dbpass", "PostgreSQL database password");
		add(DATABASE_TABLENAME_PREFIX, VersionProxy.getVersion().getInternalProductName().toLowerCase() + "_", "optional - table name prefix");
		add(DATABASE_SERVER, "localhost", "PostgreSQL DBMS host");
		add(DATABASE_NONSTANDARD_PORT, " ", "optional - PostgreSQL DBMS port");
		add(DATABASE_DEFINITION, org.reldb.wrapd.db.postgresql.WrapdDB.class.getPackageName(), "database definition class");
		add(SUPPORT_CONTACT, "dave@armchair.mb.ca", "support contact address");
	}
	
}
