package org.reldb.legacy.wrapd.sqldb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reldb.toolbox.utilities.ProgressIndicator;
import org.reldb.wrapd.sqldb.Database;

import java.sql.SQLException;

/*
 * Base or generic database definitions specific to the Wrapd framework: user management, etc., and wrapper around the Database database abstraction.
 */
public abstract class WrapdDatabaseBase implements WrapdDatabase {

    protected static Database database;

    private static final Logger log = LogManager.getLogger(WrapdDatabaseBase.class.toString());

    private boolean databaseChecked = false;
    private Integer loggedInUserID = null;

    public Database getDatabase() {
        return database;
    }

    @Override
    public CheckDatabaseStatus checkDatabase() {
        if (databaseChecked)
            return CheckDatabaseStatus.OK;
        try {
            DBVersion version = getDBVersion();
            if (version == null)
                return CheckDatabaseStatus.ERROR;
            if (version.getFrameworkDBVersion() < getFrameworkDatabaseUpdates().length || version.getUserDBVersion() < getUserDatabaseUpdates().length)
                return CheckDatabaseStatus.UPGRADE_DATABASE_NEEDED;
        } catch (SQLException e) {
            log.warn("WrapdDatabaseBase: Error: " + e);
            return CheckDatabaseStatus.CREATE_DATABASE_NEEDED;
        }
        databaseChecked = true;
        return CheckDatabaseStatus.OK;
    }

    @Override
    public void upgrade(ProgressIndicator progress) throws SQLException {
        DBVersion version = getDBVersion();
        int steps = getFrameworkDatabaseUpdates().length - version.getFrameworkDBVersion() + getUserDatabaseUpdates().length - version.getUserDBVersion();
        progress.initialise(steps + 1);
        int step = 0;
        for (int versionNumber = version.getFrameworkDBVersion(); versionNumber < getFrameworkDatabaseUpdates().length; versionNumber++) {
            progress.move(++step, "Update framework database to version number: " + (versionNumber + 1));
            getFrameworkDatabaseUpdates()[versionNumber].go(this);
            putFrameworkDBVersion(versionNumber + 1);
        }
        for (int versionNumber = version.getUserDBVersion(); versionNumber < getUserDatabaseUpdates().length; versionNumber++) {
            progress.move(++step, "Update user database to version number: " + (versionNumber + 1));
            getUserDatabaseUpdates()[versionNumber].go(this);
            putUserDBVersion(versionNumber + 1);
        }
        progress.move(++step, "Done.");
    }

    protected void setLoggedInUserID(Integer id) {
        loggedInUserID = id;
    }

    @Override
    public Integer getLoggedInUserID() {
        return loggedInUserID;
    }

    @Override
    public void logout() {
        setLoggedInUserID(null);
    }

}
