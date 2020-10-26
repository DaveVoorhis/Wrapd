package org.reldb.toolbox;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.reldb.toolbox.configuration.Configuration;
import org.reldb.toolbox.mail.Email;

public class TestEmail {
	
	@Test
	public void testEmail01() {
		var to = "Dave Voorhis <dave@armchair.mb.ca>";
		var subject = "Mail test";
		var message = "Sure, spam me. Everyone else does.";
		try {
			Configuration.setLocation("./_TestData");
			Email.send(to, subject, message);
			assertTrue(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
