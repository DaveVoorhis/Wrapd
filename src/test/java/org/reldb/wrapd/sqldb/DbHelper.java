package org.reldb.wrapd.sqldb;

import org.reldb.TestDirectory;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.ExceptionFatal;

import java.sql.SQLException;

public class DbHelper {

    private String baseDir;

    public DbHelper(String dbName) {
        baseDir = TestDirectory.Is + dbName;
        ensureTestDirectoryExists();
    }

    private void ensureTestDirectoryExists() {
        if (!Directory.chkmkdir(baseDir))
            throw new ExceptionFatal("DbHelper: Unable to create directory for test: " + baseDir);
    }

    public String getBaseDir() {
        return baseDir;
    }

    public static void clearDb(Database db, String[] tableNames) {
        System.out.println("Clearing database " + db.toString());
        for (String tableName: tableNames)
            try {
                System.out.println("Dropping table " + tableName);
                db.updateAll("DROP TABLE " + tableName);
            } catch (SQLException sqe) {
                System.out.println(" Oops dropping table: " + sqe);
            }
    }

}
