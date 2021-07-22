package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.utilities.ConsoleProgressIndicator;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.schema.AbstractSchema;
import org.reldb.wrapd.schema.SQLSchema;
import org.reldb.wrapd.schema.VersionNumber;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.reldb.wrapd.sqldb.TestDbHelper.clearDb;

public class TestSchemaHelper {

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

}
