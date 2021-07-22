package org.reldb.wrapd.sqldb;

import org.reldb.TestDirectory;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.ExceptionFatal;

import java.sql.SQLException;

public class DbHelper {

    private String baseDir;
    private String codeDir;

    public DbHelper(String dbName) {
        baseDir = TestDirectory.Is + dbName;
        codeDir = baseDir + "/code";
        ensureTestDirectoryExists();
    }

    private void ensureTestDirectoryExists() {
        if (!Directory.chkmkdir(baseDir))
            throw new ExceptionFatal("Helper: Unable to create directory for test: " + baseDir);
        Directory.rmAll(codeDir);
    }

    public String getCodeDir() {
        return codeDir;
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
