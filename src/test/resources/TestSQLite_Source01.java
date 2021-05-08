package org.reldb.wrapd.tuples.generated;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reldb.wrapd.sqldb.*;
import org.reldb.wrapd.sqldb.sqlite.TestQueries;

import java.io.IOException;
import java.sql.SQLException;

public class TestSQLite_Source01 {
    private static final String prompt = "[TEST]";

    private static void resetDatabase(Database database) throws SQLException {
        database.updateAll("DELETE FROM $$tester");
        for (int i = 0; i < 20; i++) {
            database.update("INSERT INTO $$tester VALUES (?, ?);", i, i * 10);
        }
    }

    @BeforeAll
    public static void setup() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        resetDatabase(database);
    }

    @Test
    public void testQueryToStream01() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testQueryToStream01");
        database.queryAll("SELECT * FROM $$tester", TestSQLiteTuple.class)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream02() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testQueryToStream02");
        database.query("SELECT * FROM $$tester", TestSQLiteTuple.class)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream03() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testQueryToStream03");
        database.query("SELECT * FROM $$tester WHERE x > ? AND x < ?", TestSQLiteTuple.class, 3, 7)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testInsert01() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testInsert01");
        for (int x = 1000; x < 1010; x++) {
            var tuple = new TestSQLiteTuple();
            tuple.x = x;
            tuple.y = x * 2;
            tuple.insert(database, "$$tester");
        }
        database.query("SELECT * FROM $$tester WHERE x >= ? AND x < ?", TestSQLiteTuple.class, 1000, 1010)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.toString()));
    }

    @Test
    public void testUpdate01() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testUpdate01");
        database.queryAllForUpdate("SELECT * FROM $$tester WHERE x > 3 AND x < 7", TestSQLiteTuple.class)
                .forEach(tuple -> {
                    tuple.x *= 100;
                    tuple.y *= 100;
                    try {
                        tuple.update(database, "$$tester");
                    } catch (SQLException e) {
                        System.out.println("Update failed.");
                    }
                });
        database.query("SELECT * FROM $$tester WHERE x >= ? AND x <= ?", TestSQLiteTuple.class, 300, 700)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.toString()));
    }

    @Test
    public void testUpdate02() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testUpdate02");
        resetDatabase(database);
        database.queryForUpdate("SELECT * FROM $$tester WHERE x >= ?", TestSQLiteTuple.class, 10)
                .forEach(tuple -> {
                    if (tuple.x >= 12 && tuple.x <= 13) {
                        tuple.x *= 100;
                        tuple.y *= 100;
                        try {
                            tuple.update(database, "$$tester");
                        } catch (SQLException e) {
                            System.out.println("Update failed.");
                        }
                    }
                });
        database.query("SELECT * FROM $$tester WHERE x >= ? AND x <= ?", TestSQLiteTuple.class, 1200, 1300)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.toString()));
    }

    @Test
    public void testQueryToStream04() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testQueryToStream04");
        TestSQLiteQueryQuery02.query(database)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream05() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testQueryToStream05");
        TestSQLiteQueryQuery01.query(database, 3, 7)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream06() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testQueryToStream06");
        TestSQLiteQueryQuery03.query(database, 3)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream07() throws SQLException {
        var database = TestQueries.getDatabase(prompt);
        System.out.println(prompt + " testQueryToStream07");
        TestSQLiteQueryQuery04.query(database)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x));
    }

}