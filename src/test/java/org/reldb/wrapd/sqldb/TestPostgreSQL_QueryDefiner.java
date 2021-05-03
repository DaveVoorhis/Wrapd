package org.reldb.wrapd.sqldb;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

public class TestPostgreSQL_QueryDefiner extends QueryDefiner {
    private static final String prompt = "[TEST]";
    private static final String testPackage = "org.reldb.wrapd.tuples.generated";

    public TestPostgreSQL_QueryDefiner() throws SQLException {
        super(getDatabase(prompt), PostgreSQL_Configuration.codeDir);
    }

    public static Database getDatabase(String prompt) throws SQLException {
        try {
            return new Database(
                    PostgreSQL_Configuration.dbURL,
                    PostgreSQL_Configuration.dbUser,
                    PostgreSQL_Configuration.dbPassword,
                    PostgreSQL_Configuration.dbTablenamePrefix,
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
        var database = TestMySQL_Queries.getDatabase(prompt);
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