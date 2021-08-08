package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reldb.wrapd.TestConfiguration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TestQueryDefinitions {

    private static class TestParms {
        public final String pkg;
        public final String name;
        public final Database db;

        public TestParms(String pkg, String name, Database db) {
            this.pkg = pkg;
            this.name = name;
            this.db = db;
        }

        public String toString() {
            return "pkg=" + pkg + "; name=" + name + "; db=" + db;
        }
    }

    public static List<TestParms> dbProvider() throws SQLException {
        var parms = new TestParms[] {
                new TestParms(
                        org.reldb.wrapd.sqldb.mysql.Configuration.dbPackage,
                        org.reldb.wrapd.sqldb.mysql.Configuration.dbName,
                        org.reldb.wrapd.sqldb.mysql.GetDatabase.getDatabase()),
                new TestParms(
                        org.reldb.wrapd.sqldb.postgresql.Configuration.dbPackage,
                        org.reldb.wrapd.sqldb.postgresql.Configuration.dbName,
                        org.reldb.wrapd.sqldb.postgresql.GetDatabase.getDatabase()),
                new TestParms(
                        org.reldb.wrapd.sqldb.sqlite.Configuration.dbPackage,
                        org.reldb.wrapd.sqldb.sqlite.Configuration.dbName,
                        org.reldb.wrapd.sqldb.sqlite.GetDatabase.getDatabase())
        };
        return List.of(parms);
    }

    @BeforeAll
    public static void setupTestDirectories() {
        new DbHelper(org.reldb.wrapd.sqldb.mysql.Configuration.dbName);
        new DbHelper(org.reldb.wrapd.sqldb.postgresql.Configuration.dbName);
        new DbHelper(org.reldb.wrapd.sqldb.sqlite.Configuration.dbName);
    }

    @ParameterizedTest
    @MethodSource("dbProvider")
    public void testCodeThatUsesGeneratedTuple(TestParms parms) throws IOException, ClassNotFoundException, SQLException, QueryDefiner.QueryDefinerException {
        new QueriesHelper(parms.pkg, parms.name, TestConfiguration.Package).test(parms.db);
    }

}
