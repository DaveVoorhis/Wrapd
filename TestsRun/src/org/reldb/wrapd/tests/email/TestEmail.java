package org.reldb.wrapd.tests.email;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.reldb.wrapd.mail.Email;

public class TestEmail {
	
	@Test
	public void testEmail01() {
		var smtpServerHost = "mail.armchair.mb.ca";
		var smtpServerPort = "25";
		String smtpServerAuthname = null;
		String smtpServerAuthpass = null;
		var from = "noreply@somebody.somewhere";
		var to = "Dave Voorhis <dave@armchair.mb.ca>";
		var subject = "Mail test";
		var message = "Sure, spam me. Everyone else does.";
		assertTrue(Email.send(smtpServerHost, smtpServerPort, smtpServerAuthname, smtpServerAuthpass, from, to, subject, message));
	}

}
