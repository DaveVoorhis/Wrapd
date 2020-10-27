package org.reldb.toolbox.mail;

import org.reldb.toolbox.configuration.Configuration;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Email {

    /**
     * Send email to specified host:port, using specified server authname and password, from specified sender, to specified recipient, with
     * specified subject and message.
     *
     * @param smtpServerHost - host SMTP server name or IP
     * @param smtpServerPort - host SMTP server port
     * @param username       - host SMTP server auth name; may be null
     * @param password       - host SMTP server auth password; may be null
     * @param from           - from name and email address in canonical form
     * @param to             - to name and email address in canonical form
     * @param subject        - email subject
     * @param message        - message
     * @return - boolean true if sent. Configuration, if any, is <b>ignored</b> including SMTP_SERVER_ENABLED setting.
     */
    public static boolean send(String smtpServerHost, String smtpServerPort, String username, String password,
                               String from, String to, String subject, String message) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", (smtpServerHost == null || smtpServerHost.length() == 0) ? "localhost" : smtpServerHost);
        if (smtpServerPort != null && smtpServerPort.length() > 0)
            properties.setProperty("mail.smtp.port", smtpServerPort);

        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setText(message);

            if (username == null || username.length() == 0)
                Transport.send(msg);
            else
                Transport.send(msg, username, password);
            return true;
        } catch (Exception me) {
            me.printStackTrace();
            return false;
        }
    }

    /**
     * Using configuration settings for SMTP host, send email from a given sender, to a given recipient with a specified subject and message.
     *
     * @param from    - from name and email address in canonical form
     * @param to      - to name and email address in canonical form
     * @param subject - email subject
     * @param message - message
     * @return - true if sent, or true if SMTP_SERVER_ENABLED set to 'false' in configuration but will not send email.
     */
    public static boolean send(String from, String to, String subject, String message) {
        Configuration.register(org.reldb.toolbox.mail.EmailConfiguration.class);

        // If SMTP_SERVER_ENABLED is not set, pretend to send email.
        if (!Configuration.getBooleanValue(org.reldb.toolbox.mail.EmailConfiguration.class.getName(), org.reldb.toolbox.mail.EmailConfiguration.SMTP_SERVER_ENABLED))
            return true;

        return send(
                Configuration.getValue(org.reldb.toolbox.mail.EmailConfiguration.class.getName(), org.reldb.toolbox.mail.EmailConfiguration.SMTP_SERVER),
                Configuration.getValue(org.reldb.toolbox.mail.EmailConfiguration.class.getName(), org.reldb.toolbox.mail.EmailConfiguration.SMTP_SERVER_PORT),
                Configuration.getValue(org.reldb.toolbox.mail.EmailConfiguration.class.getName(), org.reldb.toolbox.mail.EmailConfiguration.SMTP_SERVER_AUTHNAME),
                Configuration.getUntrimmedValue(org.reldb.toolbox.mail.EmailConfiguration.class.getName(), org.reldb.toolbox.mail.EmailConfiguration.SMTP_SERVER_AUTHPASS),
                Configuration.getValue(org.reldb.toolbox.mail.EmailConfiguration.class.getName(), org.reldb.toolbox.mail.EmailConfiguration.SMTP_SERVER_FROM_EMAIL),
                to,
                subject,
                message
        );
    }

    /**
     * Using configuration settings for SMTP host and from, send email to a given receipient with a specified subject and message.
     *
     * @param to      - to name and email address in canonical form
     * @param subject - email subject
     * @param message - message
     * @return - true if sent, or true if SMTP_SERVER_ENABLED set to 'false' in configuration but will not send email.
     */
    public static boolean send(String to, String subject, String message) {
        return send(Configuration.getValue(org.reldb.toolbox.mail.EmailConfiguration.class.getName(), EmailConfiguration.SMTP_SERVER_FROM_EMAIL), to, subject, message);
    }

}