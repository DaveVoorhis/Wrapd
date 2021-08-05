package org.reldb.wrapdemo.application;

import org.reldb.wrapdemo.database.mysql.GetDatabase;

import org.reldb.wrapdemo.generated.*;

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
            } catch (Throwable t) {
                System.out.println("ERROR populating db: " + t);
            }
            try {
                var db = GetDatabase.getDatabase();
                Query04.query(db)
                        .forEach(row -> System.out.println("Row: x = " + row.x));
            } catch (Throwable t) {
                System.out.println("ERROR querying db: " + t);
                Throwable inner = t.getCause();
                while (inner != null) {
                    System.out.println("Caused by..." + inner);
                    inner = inner.getCause();
                }
            }
    }

}
