package org.reldb.wrapdemo.application;

import org.reldb.wrapdemo.database.mysql.GetDatabase;

import org.reldb.wrapd.tuples.generated.*;

import java.sql.SQLException;

public class Application {
    public static void populate() throws SQLException {
        var database = GetDatabase.getDatabase();
        for (int x = 1000; x < 1010; x++) {
            var tuple = new Query01Tuple();
            tuple.x = x;
            tuple.y = x * 2;
            tuple.insert(database, "$$tester01");
        }
    }

    public static void main(String args[]) {
        try {
            populate();
        } catch (SQLException e) {
            System.out.println("ERROR in Application: main: populate(): " + e);
        }
    }

}
