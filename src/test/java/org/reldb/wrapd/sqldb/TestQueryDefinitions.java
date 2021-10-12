package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reldb.wrapd.TestConfiguration;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestQueryDefinitions {

    private static class TestParms {
        private final String pkg;

        public final String name;
        public final Database db;
        public final String tuplePkg;
        public final String definerPkg;

        public TestParms(String pkg, String name, Database db) {
            this.pkg = pkg;
            this.name = name;
            this.db = db;
            this.tuplePkg = "org.reldb.wrapd.test.tuples." + pkg + ".generated";
            this.definerPkg = "org.reldb.wrapd.test.definer." + pkg + ".generated";
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
    public void testCodeThatUsesGeneratedTuple(TestParms parms) throws Throwable {
        new QueriesHelper(parms.pkg, parms.name, parms.tuplePkg).test(parms.db);
    }

    @ParameterizedTest
    @MethodSource("dbProvider")
    public void testQueryDefinerFailsWhenQueryReturnsNoRows(TestParms parms) {
        var definer = new Definer(parms.db, TestConfiguration.Directory, parms.definerPkg);
        assertThrows(SQLException.class,
                () -> definer.defineQuery("QueryOfTestUpdateABC",
                        "UPDATE $$abc SET c = 'blah' WHERE a = {aValue}", 2));
    }

    @ParameterizedTest
    @MethodSource("dbProvider")
    public void testQueryDefinerFailsWhenValueOfReturnsNoRows(TestParms parms) {
        var definer = new Definer(parms.db, TestConfiguration.Directory, parms.definerPkg);
        assertThrows(SQLException.class,
                () -> definer.defineValueOf("ValueOfTestUpdateABC",
                        "UPDATE $$abc SET c = 'blah' WHERE a = {aValue}", 2));
    }

}
