package org.reldb.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

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
