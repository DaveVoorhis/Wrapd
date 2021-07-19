package org.reldb.wrapd.sqldb;

import java.sql.SQLException;

public class TestDbHelper {

    public static void clearDb(Database db, String[] tableNames) {
        System.out.println("Clearing database " + db.getClass().getName());
        for (String tableName: tableNames)
            try {
                System.out.println("Dropping table " + tableName);
                db.updateAll("DROP TABLE " + tableName);
            } catch (SQLException sqe) {
                System.out.println(" Oops dropping table: " + sqe);
            }
    }

}
