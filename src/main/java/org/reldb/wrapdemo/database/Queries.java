package org.reldb.wrapdemo.database;

import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.QueryDefiner;
import org.reldb.wrapd.sqldb.QueryDefinition;
import org.reldb.wrapdemo.database.mysql.GetDatabase;

import java.sql.SQLException;

public class Queries extends QueryDefiner {
    /**
     * Create a QueryDefiner, given a Database and the directory where Tuple-derived classes will be stored.
     *
     * @param database      Database
     * @param codeDirectory Directory for Tuple-derived classes.
     */
    public Queries(Database database, String codeDirectory) {
        super(database, codeDirectory);
    }

    public QueryDefinition QueryDefinition01() {
        return new QueryDefinition("Query01", "SELECT * FROM $$tester01 WHERE x > ? AND x < ?", 3, 7);
    }

    public QueryDefinition QueryDefinition02() {
        return new QueryDefinition("Query02", "SELECT * FROM $$tester01");
    }

    public void QueryDefinition03() throws Exception {
        define("Query03", "SELECT * FROM $$tester01 WHERE x > ?", 3);
    }

    public void queryDefinition04() throws Exception {
        define("Query04", "SELECT x FROM $$tester01");
    }

    public static void main(String args[]) {
        Database db = null;
        try {
            db = GetDatabase.getDatabase();
        } catch (SQLException e) {
            System.out.println("ERROR in Queries: main: GetDatabase.getDatabase(): " + e);
            return;
        }
        var codeDirectory = "./_TestData/WrapDemo/code";
        if (!Directory.chkmkdir(codeDirectory)) {
            System.out.println("ERROR creating code directory " + codeDirectory);
            return;
        }
        var queryDefinitions = new Queries(db, codeDirectory);
        try {
            queryDefinitions.generate();
        } catch (QueryDefinerException e) {
            System.out.println("ERROR in Queries: main: queryDefinitions.generate(): " + e);
        }
        System.out.println("OK: Queries are ready.");
    }
}
