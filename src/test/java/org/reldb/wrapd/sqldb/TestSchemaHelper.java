package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.utilities.ConsoleProgressIndicator;
import org.reldb.wrapd.schema.SQLSchema;

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
		if (!result.isOk())
			System.out.println("test01 error: " + result.error);
		assertEquals(true, result.isOk());
	}
}
