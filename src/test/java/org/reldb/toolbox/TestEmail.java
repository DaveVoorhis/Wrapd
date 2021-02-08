package org.reldb.toolbox;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reldb.toolbox.configuration.Configuration;
import org.reldb.toolbox.mail.Email;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.ExceptionFatal;

public class TestEmail {

	private static final String baseDir = "./_TestData";

	@BeforeAll
	public static void setup() {
		if (!Directory.chkmkdir(baseDir))
			throw new ExceptionFatal("TestEmail: Unable to create directory for test: " + baseDir);
	}

	@Test
	public void testEmail01() {
		var to = "Dave Voorhis <dave@armchair.mb.ca>";
		var subject = "Mail test";
		var message = "Sure, spam me. Everyone else does.";
		try {
			Configuration.setLocation(baseDir);
			Email.send(to, subject, message);
			assertTrue(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
