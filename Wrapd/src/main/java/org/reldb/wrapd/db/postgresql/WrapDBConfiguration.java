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
    public static final String SMTP_SERVER = "smtp_server";
    public static final String SMTP_SERVER_PORT = "smtp_server_port";	
    public static final String SMTP_SERVER_AUTHNAME = "smtp_server_authname";
    public static final String SMTP_SERVER_AUTHPASS = "smtp_server_authpass";
    public static final String SMTP_SERVER_FROM_EMAIL = "smtp_server_from_email";
    public static final String SUPPORT_CONTACT = "support_contact";
    public static final String AUTH_ALLOW_USER_REGISTRATION = "auth_allow_user_registration";
    public static final String AUTH_ACTIVATE_ACCOUNT_GROUP = "auth_activate_account_group";
    public static final String URL_BASE = "url_base";
    
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
		add(AUTH_ALLOW_USER_REGISTRATION, "yes", "no = users cannot register accounts; yes = (default) users can register & self-activate; admin = users can register, admins activate accounts.");
		add(AUTH_ACTIVATE_ACCOUNT_GROUP, "Administrator", "optional - group that can authorise account requests");
		add(URL_BASE, " ", "optional - base URL for this application. We'll try to determine it if we can, but it might not be correct.");
	}
	
}