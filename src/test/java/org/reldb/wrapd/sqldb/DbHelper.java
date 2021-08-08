package org.reldb.wrapd.sqldb;

import org.reldb.wrapd.TestConfiguration;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.FatalException;

import java.sql.SQLException;

public class DbHelper {

    private final String baseDir;

    public DbHelper(String dbName) {
        baseDir = TestConfiguration.Directory + dbName;
        ensureTestDirectoryExists();
    }

    private void ensureTestDirectoryExists() {
        if (!Directory.chkmkdir(baseDir))
            throw new FatalException("DbHelper: Unable to create directory for test: " + baseDir);
    }

    public String getBaseDir() {
        return baseDir;
    }

    public static void clearDb(Database db, String[] tableNames) {
        System.out.println("Clearing database " + db.toString());
        for (var tableName: tableNames)
            try {
                System.out.println("Dropping table " + tableName);
                db.updateAll("DROP TABLE " + tableName);
            } catch (SQLException sqe) {
                System.out.println(" Oops dropping table: " + sqe);
            }
    }

}
