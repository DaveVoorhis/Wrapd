package org.reldb.wrapd.tests.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class TestJUnit {

	// Verify JUnit operation
	
	@Test 
	public void testOneIsOne() {
		assertEquals(1, 1);
	}
	
	@Test 
	public void testOneIsNotTwo() {
		assertNotEquals(1, 2);
	}

}
