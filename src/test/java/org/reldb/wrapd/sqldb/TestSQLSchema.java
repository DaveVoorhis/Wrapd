package org.reldb.wrapd.sqldb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.reldb.toolbox.progress.ConsoleProgressIndicator;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.schema.SQLSchema;
import org.reldb.wrapd.schema.SQLSchemaYAML;
import org.reldb.wrapd.schema.VersionNumber;

import java.sql.SQLException;
import java.util.List;

import static org.reldb.wrapd.sqldb.DbHelper.clearDb;

public class TestSQLSchema {

	private static List<Database> dbProvider() throws SQLException {
		var db = new Database[] {
				org.reldb.wrapd.sqldb.mysql.GetDatabase.getDatabase(),
				org.reldb.wrapd.sqldb.mssql.GetDatabase.getDatabase(),
				org.reldb.wrapd.sqldb.postgresql.GetDatabase.getDatabase(),
				org.reldb.wrapd.sqldb.sqlite.GetDatabase.getDatabase()
		};
		return List.of(db);
	}

	@BeforeAll
	public static void setupTestDirectories() {
		new DbHelper(org.reldb.wrapd.sqldb.mysql.Configuration.dbName);
		new DbHelper(org.reldb.wrapd.sqldb.mssql.Configuration.dbName);
		new DbHelper(org.reldb.wrapd.sqldb.postgresql.Configuration.dbName);
		new DbHelper(org.reldb.wrapd.sqldb.sqlite.Configuration.dbName);
	}

	@DisplayName("Can we create a minimal schema?")
	@ParameterizedTest
	@MethodSource("dbProvider")
	public void canCreateMinimalSchema(final Database database) {
		clearDb(database, new String[] {"$$__version"});
		var testSchema = new SQLSchema(database) {
			@Override
			protected Update[] getUpdates() {
				return new Update[0];
			}
		};
		var result = testSchema.setup(new ConsoleProgressIndicator());
		result.printIfError();
		assertTrue(result.isOk());
	}

	@DisplayName("Can we create a simple schema?")
	@ParameterizedTest
	@MethodSource("dbProvider")
	public void canCreateSimpleSchema(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema = new SQLSchema(database) {
			protected Update[] getUpdates() {
				return new Update[] {
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
		assertTrue(result.isOk());
		assertEquals(2, ((VersionNumber)testSchema.getVersion()).value);
	}

	@DisplayName("Does schema migration stop at an invalid update?")
	@ParameterizedTest
	@MethodSource("dbProvider")
	public void stopsAtInvalidUpdate(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema = new SQLSchema(database) {
			protected Update[] getUpdates() {
				return new Update[] {
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
		assertTrue(result.isError());
		assertEquals(1, ((VersionNumber)testSchema.getVersion()).value);
	}

	@DisplayName("Do schema updates aka migrations work?")
	@ParameterizedTest
	@MethodSource("dbProvider")
	public void versionUpdatesWork(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema01 = new SQLSchema(database) {
			protected Update[] getUpdates() {
				return new Update[] {
						// version 1
						schema -> {
							database.updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
							return Result.OK;
						}
				};
			}
		};
		var testSchema02 = new SQLSchema(database) {
			protected Update[] getUpdates() {
				return new Update[] {
						// version 1
						schema -> {
							database.updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
							return Result.OK;
						},
						// migration to version 2
						schema -> {
							database.updateAll("CREATE TABLE $$tester02 (a INT NOT NULL PRIMARY KEY, b INT NOT NULL)");
							return Result.OK;
						}
				};
			}
		};
		var result1 = testSchema01.setup(new ConsoleProgressIndicator());
		result1.printIfError();
		assertTrue(result1.isOk());
		assertEquals(1, ((VersionNumber)testSchema01.getVersion()).value);
		var result2 = testSchema02.setup(new ConsoleProgressIndicator());
		result2.printIfError();
		assertTrue(result1.isOk());
		assertEquals(2, ((VersionNumber)testSchema01.getVersion()).value);
	}

	@DisplayName("Do YAML-based schema migration definitions work?")
	@ParameterizedTest
	@MethodSource("dbProvider")
	public void yamlSchemaWorks(final Database database) throws Throwable {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema = new SQLSchemaYAML(database, "testschema.yaml");
		var result = testSchema.setup(new ConsoleProgressIndicator());
		result.printIfError();
		assertTrue(result.isOk());
		assertEquals(2, ((VersionNumber)testSchema.getVersion()).value);
	}

	/**
	 * Test schema.
	 */
	public static class TestSchema1 extends SQLSchemaYAML {
		/**
		 * Create an instance of a schema for a specified Database.
		 *
		 * @param database     Database.
		 * @param yamlFileName YAML schema definition and migration file.
		 * @throws Throwable
		 */
		public TestSchema1(Database database, String yamlFileName) throws Throwable {
			super(database, yamlFileName);
		}

		// Demo method that can be invoked from the YAML database schema file.
		public Result doMyMethod(Integer x, String y) {
			System.out.println(">>>> doMyMethod invoked with " + x + ", " + y);
			return Result.OK;
		}
	}

	@DisplayName("Do YAML-based schema migration definitions with inline Java invocations work?")
	@ParameterizedTest
	@MethodSource("dbProvider")
	public void yamlSchemaWithInlineJavaWorks(final Database database) throws Throwable {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema = new TestSchema1(database, "testschema2.yaml");
		var result = testSchema.setup(new ConsoleProgressIndicator());
		result.printIfError();
		assertTrue(result.isOk());
		assertEquals(3, ((VersionNumber)testSchema.getVersion()).value);
	}

}
