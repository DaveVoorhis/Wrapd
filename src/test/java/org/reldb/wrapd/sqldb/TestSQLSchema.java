package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reldb.toolbox.utilities.ConsoleProgressIndicator;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.schema.AbstractSchema;
import org.reldb.wrapd.schema.SQLSchema;
import org.reldb.wrapd.schema.VersionNumber;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.reldb.wrapd.sqldb.DbHelper.clearDb;

public class TestSQLSchema {

	public TestSQLSchema() {}

	private class TestSchema extends SQLSchema {

		public TestSchema(Database database) {
			super(database);
		}

		@Override
		protected Update[] getUpdates() {
			return new Update[0];
		}
	}

	public void canCreateMinimalSchema(final Database database) {
		clearDb(database, new String[] {"$$__version"});
		var testSchema = new TestSchema(database);
		var result = testSchema.setup(new ConsoleProgressIndicator());
		result.printIfError();
		assertEquals(true, result.isOk());
	}

	public void canCreateSimpleSchema(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema = new TestSchema(database) {
			protected AbstractSchema.Update[] getUpdates() {
				return new AbstractSchema.Update[] {
					schema -> {
						database.updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
						return Result.OK;
					},
					schema -> {
						database.updateAll("CREATE TABLE $$tester02 (a INT NOT NULL PRIMARY KEY, b INT NOT NULL)");
						return Result.OK;
					}
				};
			}
		};
		var result = testSchema.setup(new ConsoleProgressIndicator());
		result.printIfError();
		assertEquals(true, result.isOk());
		assertEquals(2, ((VersionNumber)testSchema.getVersion()).value);
	}

	public void stopsAtInvalidUpdate(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema = new TestSchema(database) {
			protected AbstractSchema.Update[] getUpdates() {
				return new AbstractSchema.Update[] {
						schema -> {
							database.updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
							return Result.OK;
						},
						// intentional fail
						schema -> {
							database.updateAll("CREATE TABLE $$tester02 (a INT NOT NULL PRIMARY KEY, deliberateNonsense");
							return Result.OK;
						}
				};
			}
		};
		var result = testSchema.setup(new ConsoleProgressIndicator());
		System.out.println("NOTE: The following should be an error.");
		result.printIfError();
		assertEquals(true, result.isError());
		assertEquals(1, ((VersionNumber)testSchema.getVersion()).value);
	}

	private String testStagePrompt = "[TSET]";

	// TODO parametrise the following tests

	@BeforeAll
	public static void setupTestDirectories() {
		new DbHelper(org.reldb.wrapd.sqldb.mysql.Configuration.dbName);
		new DbHelper(org.reldb.wrapd.sqldb.postgresql.Configuration.dbName);
		new DbHelper(org.reldb.wrapd.sqldb.sqlite.Configuration.dbName);
	}

	@Test
	public void testCreateMinimalSchemaMySQL() throws SQLException {
		var db = org.reldb.wrapd.sqldb.mysql.GetDatabase.getDatabase(testStagePrompt);
		canCreateMinimalSchema(db);
	}

	@Test
	public void testCreateSimpleSchemaMySQL() throws SQLException {
		var db = org.reldb.wrapd.sqldb.mysql.GetDatabase.getDatabase(testStagePrompt);
		canCreateSimpleSchema(db);
	}

	@Test
	public void testStopsAtInvalidUpdateMySQL() throws SQLException {
		var db = org.reldb.wrapd.sqldb.mysql.GetDatabase.getDatabase(testStagePrompt);
		stopsAtInvalidUpdate(db);
	}

	@Test
	public void testCreateMinimalSchemaPostgreSQL() throws SQLException {
		var db = org.reldb.wrapd.sqldb.postgresql.GetDatabase.getDatabase(testStagePrompt);
		canCreateMinimalSchema(db);
	}

	@Test
	public void testCreateSimpleSchemaPostgreSQL() throws SQLException {
		var db = org.reldb.wrapd.sqldb.postgresql.GetDatabase.getDatabase(testStagePrompt);
		canCreateSimpleSchema(db);
	}

	@Test
	public void testStopsAtInvalidUpdatePostgreSQL() throws SQLException {
		var db = org.reldb.wrapd.sqldb.postgresql.GetDatabase.getDatabase(testStagePrompt);
		stopsAtInvalidUpdate(db);
	}

	@Test
	public void testCreateMinimalSchemaSQLite() throws SQLException {
		var db = org.reldb.wrapd.sqldb.sqlite.GetDatabase.getDatabase(testStagePrompt);
		canCreateMinimalSchema(db);
	}

	@Test
	public void testCreateSimpleSchemaSQLite() throws SQLException {
		var db = org.reldb.wrapd.sqldb.sqlite.GetDatabase.getDatabase(testStagePrompt);
		canCreateSimpleSchema(db);
	}

	@Test
	public void testStopsAtInvalidUpdateSQLite() throws SQLException {
		var db = org.reldb.wrapd.sqldb.sqlite.GetDatabase.getDatabase(testStagePrompt);
		stopsAtInvalidUpdate(db);
	}

}
