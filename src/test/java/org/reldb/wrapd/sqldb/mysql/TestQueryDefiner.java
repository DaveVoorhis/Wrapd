package org.reldb.wrapd.sqldb.mysql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.QueryDefiner;
import org.reldb.wrapd.sqldb.QueryDefinition;

import java.io.IOException;
import java.sql.SQLException;

public class TestQueryDefiner extends QueryDefiner {
    private static final String prompt = "[TEST]";
    private static final String testPackage = "org.reldb.wrapd.tuples.generated";

    public TestQueryDefiner() throws SQLException {
        super(getDatabase(prompt), Configuration.codeDir);
    }

    public static Database getDatabase(String prompt) throws SQLException {
        try {
            return new Database(
                    Configuration.dbURL,
                    Configuration.dbUser,
                    Configuration.dbPassword,
                    Configuration.dbTablenamePrefix,
                    null);
        } catch (IOException e) {
            throw new SQLException(prompt + " Database connection failed. Error is: " + e);
        }
    }

    private static void resetDatabase(Database database) throws SQLException {
        database.updateAll("DELETE FROM $$tester");
        for (int i = 0; i < 20; i++) {
            database.update("INSERT INTO $$tester VALUES (?, ?);", i, i * 10);
        }
    }

    @BeforeAll
    public static void setup() throws SQLException, IOException {
        var database = TestQueries.getDatabase(prompt);
        resetDatabase(database);
    }

    @Test
    public void run() throws QueryDefinerException {
        generate();
    }

    public QueryDefinition QueryDefinition01() {
        return new QueryDefinition("Query01", "SELECT * FROM $$tester WHERE x > ? AND x < ?", 3, 7);
    }

    public QueryDefinition QueryDefinition02() {
        return new QueryDefinition("Query02", "SELECT * FROM $$tester");
    }

}