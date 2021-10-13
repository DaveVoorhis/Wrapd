package <tuplepackage>;

/* NOTE: This is a template. Content will be replaced at runtime. See <db>, <dbpackage>, and <tuplepackage>. */

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.reldb.wrapd.sqldb.Database;
import org.reldb.wrapd.sqldb.<dbpackage>.GetDatabase;

public class Test<db>_Source01 {

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

    @Test
    public void testValueOf01() throws Exception {
        System.out.println("== ValueOfABCb ==");
        var database = GetDatabase.getDatabase();
        ClearABC.update(database);
        populateABC(database);
        var result = ValueOfABCb.valueOf(database);
        System.out.println("Result is " + result.get());
        assertEquals(2000, result.get());
    }

    @Test
    public void testValueOf02() throws Exception {
        System.out.println("== ValueOfXYZz ==");
        var database = GetDatabase.getDatabase();
        ClearXYZ.update(database);
        populateXYZ(database);
        var result = ValueOfXYZz.valueOf(database, 1007);
        System.out.println("Result is " + result.get());
        assertEquals("100700", result.get());
    }

    @Test
    public void testValueOf03() throws Exception {
        System.out.println("== ValueOfXYZz (no rows) ==");
        var database = GetDatabase.getDatabase();
        ClearXYZ.update(database);
        populateXYZ(database);
        var result = ValueOfXYZz.valueOf(database, 99999);
        assertEquals(true, !result.isPresent());
    }

}