package <tuplepackage>;

/* NOTE: This is a template. Content will be replaced at runtime. See <db>, <dbpackage>, and <tuplepackage>. */

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.Query;
import org.reldb.wrapd.sqldb.<dbpackage>.GetDatabase;

import java.io.IOException;
import java.sql.SQLException;

public class Test<db>_Source01 {
    private static final String prompt = "[TEST]";

    private static void resetDatabase(Database database) throws SQLException {
        database.updateAll("DELETE FROM $$tester");
        for (var i = 0; i < 20; i++) {
            database.update("INSERT INTO $$tester VALUES (?, ?);", i, i * 10);
        }
    }

    @BeforeAll
    public static void setup() throws SQLException {
        var database = GetDatabase.getDatabase();
        resetDatabase(database);
    }

    @Test
    public void testQueryToStream01() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testQueryToStream01");
        database.queryAll("SELECT * FROM $$tester", Test<db>Tuple.class)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream02() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testQueryToStream02");
        database.query("SELECT * FROM $$tester", Test<db>Tuple.class)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream03() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testQueryToStream03");
        database.query("SELECT * FROM $$tester WHERE x > ? AND x < ?", Test<db>Tuple.class, 3, 7)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testInsert01() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testInsert01");
        for (var x = 1000; x < 1010; x++) {
            var tuple = new Test<db>Tuple(database);
            tuple.x = x;
            tuple.y = x * 2;
            tuple.insert("$$tester");
        }
        database.query("SELECT * FROM $$tester WHERE x >= ? AND x < ?", Test<db>Tuple.class, 1000, 1010)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.toString()));
    }

    @Test
    public void testUpdate01() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testUpdate01");
        database.queryAllForUpdate("SELECT * FROM $$tester WHERE x > 3 AND x < 7", Test<db>Tuple.class)
                .forEach(tuple -> {
                    tuple.x *= 100;
                    tuple.y *= 100;
                    try {
                        tuple.update("$$tester");
                    } catch (SQLException e) {
                        System.out.println("Update failed.");
                    }
                });
        database.query("SELECT * FROM $$tester WHERE x >= ? AND x <= ?", Test<db>Tuple.class, 300, 700)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.toString()));
    }

    @Test
    public void testUpdate02() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testUpdate02");
        resetDatabase(database);
        database.queryForUpdate("SELECT * FROM $$tester WHERE x >= ?", Test<db>Tuple.class, 10)
                .forEach(tuple -> {
                    if (tuple.x >= 12 && tuple.x <= 13) {
                        tuple.x *= 100;
                        tuple.y *= 100;
                        try {
                            tuple.update("$$tester");
                        } catch (SQLException e) {
                            System.out.println("Update failed.");
                        }
                    }
                });
        database.query("SELECT * FROM $$tester WHERE x >= ? AND x <= ?", Test<db>Tuple.class, 1200, 1300)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.toString()));
    }

    @Test
    public void testQueryToStream04() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testQueryToStream04");
        Query02.query(database)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream05() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testQueryToStream05");
        Query01.query(database, 3, 7)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream06() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testQueryToStream06");
        Query03.query(database, 3)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x + ", " + tuple.y));
    }

    @Test
    public void testQueryToStream07() throws SQLException {
        var database = GetDatabase.getDatabase();
        System.out.println(prompt + " testQueryToStream07");
        Query04.query(database)
                .forEach(tuple -> System.out.println(prompt + " " + tuple.x));
    }

    private static void populateABC(Database database) throws Exception {
        for (var i = 1000; i < 1010; i++) {
            var tuple = new abcTuple(database);
            tuple.a = i;
            tuple.b = i * 2;
            tuple.c = Integer.toString(i * 10);
            tuple.insert();
        }
    }

    private static void populateXYZ(Database database) throws Exception {
        for (var i = 1005; i < 1015; i++) {
            var tuple = new xyzTuple(database);
            tuple.x = i;
            tuple.y = i * 2;
            tuple.z = Integer.toString(i * 100);
            tuple.insert();
        }
    }

    @Test
    public void testABC01() throws Exception {
        var database = GetDatabase.getDatabase();
        ClearABC.update(database);
        populateABC(database);
        System.out.println("== ABC ==");
        abc.query(database)
                .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c));
    }

    @Test
    public void testXYZ01() throws Exception {
        var database = GetDatabase.getDatabase();
        ClearXYZ.update(database);
        populateXYZ(database);
        System.out.println("== XYZ (1007) ==");
        xyz.query(database, 1007)
                .forEach(row -> System.out.println("Row: x = " + row.x + " y = " + row.y + " z = " + row.z));
    }

    @Test
    public void testABC02() throws Exception {
        var database = GetDatabase.getDatabase();
        ClearABC.update(database);
        populateABC(database);
        ClearABCWhere.update(database, 1007);
        System.out.println("== ABC ==");
        abc.query(database)
                .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c));
    }

    @Test
    public void testABC03() throws Exception {
        var database = GetDatabase.getDatabase();
        ClearABC.update(database);
        populateABC(database);
        ClearABCWhere.update(database, 1007);
        System.out.println("== ABC with 1007 removed ==");
        abc.query(database)
                .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c));
        abc.queryForUpdate(database)
                .forEach(row -> {
                    row.b += 100;
                    try {
                        row.update();
                    } catch (SQLException e) {
                        System.out.println("Row update failed due to: " + e);
                    }
                });
        System.out.println("== ABC updated with b += 100 ==");
        abc.query(database)
                .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c));
    }

    @Test
    public void testJoin01() throws Exception {
        var database = GetDatabase.getDatabase();
        ClearABC.update(database);
        ClearXYZ.update(database);
        populateABC(database);
        populateXYZ(database);
        System.out.println("== ABCJoinXYZ ==");
        ABCJoinXYZ.query(database)
                .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c +
                        " x = " + row.x + " y = " + row.y + " z = " + row.z));
        System.out.println("== ABCJoinXYZWhere (1002, 1008) ==");
        ABCJoinXYZWhere.query(database, 1002, 1008)
                .forEach(row -> System.out.println("Row: a = " + row.a + " b = " + row.b + " c = " + row.c +
                        " x = " + row.x + " y = " + row.y + " z = " + row.z));
    }

}