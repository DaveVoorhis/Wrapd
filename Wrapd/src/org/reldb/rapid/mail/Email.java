package org.reldb.rapid.mail;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.reldb.wrapd.configuration.Configuration;

public class Email {
	
	public static boolean send(String smtpServerHost, String smtpServerPort, String smtpServerAuthname, String smtpServerAuthpass, 
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
	
			String username = smtpServerAuthname;
			String password = smtpServerAuthpass;
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
	
	public static boolean send(String to, String subject, String message) {
		return send(
			Configuration.getValue(Configuration.SMTP_SERVER).trim(),
			Configuration.getValue(Configuration.SMTP_SERVER_PORT).trim(),
			Configuration.getValue(Configuration.SMTP_SERVER_AUTHNAME).trim(),
			Configuration.getValue(Configuration.SMTP_SERVER_AUTHPASS),
			Configuration.getValue(Configuration.SMTP_SERVER_FROM_EMAIL).trim(),
			to,
			subject,
			message
		);
	}

}