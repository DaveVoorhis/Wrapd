package org.reldb.toolbox.mail;

import org.reldb.toolbox.configuration.ConfigurationSettings;

public class EmailConfiguration extends ConfigurationSettings {

    public static final String SMTP_SERVER = "server";
    public static final String SMTP_SERVER_PORT = "port";
    public static final String SMTP_SERVER_AUTHNAME = "authname";
    public static final String SMTP_SERVER_AUTHPASS = "authpass";
    public static final String SMTP_SERVER_FROM_EMAIL = "from_email";
    public static final String SMTP_SERVER_ENABLED = "enabled";

    @Override
    public void registration() {
        add(SMTP_SERVER, "localhost");
        add(SMTP_SERVER_PORT, " ", "optional - defaults to 25");
        add(SMTP_SERVER_AUTHNAME, " ", "optional - account ID for SMTP authorisation");
        add(SMTP_SERVER_AUTHPASS, " ", "optional - account password for SMTP authorisation");
        add(SMTP_SERVER_FROM_EMAIL, " ", "optional - all email will come from this address");
        add(SMTP_SERVER_ENABLED, "false", "default is false and email system bypassed; email will not be sent! Set to 'true' to enable email sending.");
    }

}
