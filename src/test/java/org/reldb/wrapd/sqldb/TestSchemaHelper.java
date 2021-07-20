package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.utilities.ConsoleProgressIndicator;
import org.reldb.wrapd.response.Result;
import org.reldb.wrapd.schema.AbstractSchema;
import org.reldb.wrapd.schema.SQLSchema;

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

	public void test01(Database database) {
		clearDb(database, new String[] {"$$__version"});
		var testSchema = new TestSchema(database);
		var result = testSchema.setup(new ConsoleProgressIndicator());
		if (result.isError())
			System.out.println("test01 error: " + result.error);
		assertEquals(true, result.isOk());
	}

	public void test02(final Database database) {
		clearDb(database, new String[] {
				"$$__version",
				"$$tester01",
				"$$tester02"
		});
		var version1 = new AbstractSchema.Update() {
			@Override
			public Result apply(AbstractSchema schema) throws SQLException {
				database.updateAll("CREATE TABLE $$tester01 (x INT NOT NULL PRIMARY KEY, y INT NOT NULL");
				return Result.OK;
			}
		};
		var version2 = new AbstractSchema.Update() {
			@Override
			public Result apply(AbstractSchema schema) throws SQLException {
				database.updateAll("CREATE TABLE $$tester02 (a INT NOT NULL PRIMARY KEY, b INT NOT NULL");
				return Result.OK;
			}
		};
		var testSchema = new TestSchema(database) {
			protected AbstractSchema.Update[] getUpdates() {
				return new AbstractSchema.Update[] {
					version1,
					version2
				};
			}
		};
		var result = testSchema.setup(new ConsoleProgressIndicator());
		if (result.isError())
			System.out.println("test02 error: " + result.error);
		assertEquals(true, result.isOk());
	}
}
