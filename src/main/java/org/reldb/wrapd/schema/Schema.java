package org.reldb.wrapd.schema;

import java.util.HashMap;

public class Schema {
    public static void main() {
        var data = new HashMap<String, String>();
        data.put("aaa", "Dave");
        data.put("aab", "Bob");
        data.put("aac", "Jane");

        var found = data.get("aab");
        System.out.println("Found: " + found);
    }
}
