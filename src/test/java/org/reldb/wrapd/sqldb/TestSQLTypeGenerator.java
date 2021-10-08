package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.Test;
import org.reldb.wrapd.TestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSQLTypeGenerator {

	private static final String baseDir = TestConfiguration.Directory + "SQL";
	private static final String tuplePackage = "org.reldb.wrapd.test.tuples.sqltest.generated";

	private static String getCodeDirectory() {
		return baseDir + "/code";
	}

	@Test 
	public void testSQLGenerator01() {
		var generator = new QueryTypeGenerator(getCodeDirectory(), tuplePackage,"TestTuple01", "TestQuery01",
				"select * from sometable where x = {testparm}", 3);
		generator.generate();
		assertEquals("select * from sometable where x = ?", generator.getSQLText());
	}

	@Test
	public void testSQLGenerator02() {
		var generator = new QueryTypeGenerator(getCodeDirectory(), tuplePackage,"TestTuple02", "TestQuery02",
				"select * from sometable where x = {testparm} and y = {testparm2}", 3, 5);
		generator.generate();
		assertEquals("select * from sometable where x = ? and y = ?", generator.getSQLText());
	}

	@Test
	public void testSQLGenerator03() {
		var generator = new QueryTypeGenerator(getCodeDirectory(), tuplePackage, "TestTuple03", "TestQuery03",
				"select * from sometable where x = {testparm} and y = {testparm2} and z = {testparm3}", 3, 5, 7);
		generator.generate();
		assertEquals("select * from sometable where x = ? and y = ? and z = ?", generator.getSQLText());
	}

	@Test
	public void testSQLGenerator04() {
		var generator = new QueryTypeGenerator(getCodeDirectory(), tuplePackage, "TestTuple04", "TestQuery04",
				"select * from sometable where x = {testparm} and y = ? and z = {testparm3}", 3, 5, 7);
		generator.generate();
		assertEquals("select * from sometable where x = ? and y = ? and z = ?", generator.getSQLText());
	}

	@Test
	public void testSQLGenerator05() {
		var generator = new QueryTypeGenerator(getCodeDirectory(), tuplePackage, "TestTuple05", "TestQuery05",
				"select * from sometable where x = {testparm", 7);
		Exception exception = assertThrows(IllegalArgumentException.class, generator::generate);

		String expectedMessage = "class org.reldb.wrapd.sqldb.SQLParametriser: Missing end '}' in parameter def started at position 34 in select * from sometable where x = {testparm";
		String actualMessage = exception.getMessage();

		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	public void testSQLGenerator06() {
		var generator = new QueryTypeGenerator(getCodeDirectory(), tuplePackage, "TestTuple06", "TestQuery06",
				"select * from sometable where x = {testparm} and y = {testparm}", 5, 6);
		Exception exception = assertThrows(IllegalArgumentException.class, generator::generate);

		String expectedMessage = "class org.reldb.wrapd.sqldb.SQLParametriser: Attempt to define duplicate parameter name testparm.";
		String actualMessage = exception.getMessage();

		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	public void testSQLGenerator07() {
		var generator = new QueryTypeGenerator(getCodeDirectory(), tuplePackage, "TestTuple07", "TestQuery07",
				"select * from sometable where x = {123testparm}", 6);
		Exception exception = assertThrows(IllegalArgumentException.class, generator::generate);

		String expectedMessage = "class org.reldb.wrapd.sqldb.SQLParametriser: Parameter name 123testparm is not a valid Java identifier.";
		String actualMessage = exception.getMessage();

		assertEquals(expectedMessage, actualMessage);
	}

	@Test
	public void testSQLGenerator08() {
		var generator = new QueryTypeGenerator(getCodeDirectory(), tuplePackage, "TestTuple08", "TestQuery08",
				"select * from sometable where x = {test.parm}", 6);
		Exception exception = assertThrows(IllegalArgumentException.class, generator::generate);

		String expectedMessage = "class org.reldb.wrapd.sqldb.SQLParametriser: Parameter name test.parm must not contain '.'.";
		String actualMessage = exception.getMessage();

		assertEquals(expectedMessage, actualMessage);
	}

}
