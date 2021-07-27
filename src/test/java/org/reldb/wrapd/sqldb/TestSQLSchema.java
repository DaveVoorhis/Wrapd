package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reldb.toolbox.progress.ConsoleProgressIndicator;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.schema.AbstractSchema;
import org.reldb.wrapd.schema.SQLSchema;
import org.reldb.wrapd.schema.VersionNumber;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.reldb.wrapd.sqldb.DbHelper.clearDb;

public class TestSQLSchema {

	private static List<Database> dbProvider() throws SQLException {
		var testStagePrompt = "[TSET]";
		var db = new Database[] {
				org.reldb.wrapd.sqldb.mysql.GetDatabase.getDatabase(testStagePrompt),
				org.reldb.wrapd.sqldb.postgresql.GetDatabase.getDatabase(testStagePrompt),
				org.reldb.wrapd.sqldb.sqlite.GetDatabase.getDatabase(testStagePrompt)
		};
		return List.of(db);
	}

	@BeforeAll
	public static void setupTestDirectories() {
		new DbHelper(org.reldb.wrapd.sqldb.mysql.Configuration.dbName);
		new DbHelper(org.reldb.wrapd.sqldb.postgresql.Configuration.dbName);
		new DbHelper(org.reldb.wrapd.sqldb.sqlite.Configuration.dbName);
	}

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
		assertEquals(true, result.isOk());
	}

	@ParameterizedTest
	@MethodSource("dbProvider")
	public void canCreateSimpleSchema(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema = new SQLSchema(database) {
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

	@ParameterizedTest
	@MethodSource("dbProvider")
	public void stopsAtInvalidUpdate(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema = new SQLSchema(database) {
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


	@ParameterizedTest
	@MethodSource("dbProvider")
	public void versionUpdatesWork(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var testSchema01 = new SQLSchema(database) {
			protected AbstractSchema.Update[] getUpdates() {
				return new AbstractSchema.Update[] {
						// version 1
						schema -> {
							database.updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL)");
							return Result.OK;
						}
				};
			}
		};
		var testSchema02 = new SQLSchema(database) {
			protected AbstractSchema.Update[] getUpdates() {
				return new AbstractSchema.Update[] {
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
		assertEquals(true, result1.isOk());
		assertEquals(1, ((VersionNumber)testSchema01.getVersion()).value);
		var result2 = testSchema02.setup(new ConsoleProgressIndicator());
		result2.printIfError();
		assertEquals(true, result1.isOk());
		assertEquals(2, ((VersionNumber)testSchema01.getVersion()).value);
	}

}
