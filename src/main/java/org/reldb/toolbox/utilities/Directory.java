package org.reldb.toolbox.utilities;

import java.io.File;

/**
 * Directory-handling utilities.
 */
public class Directory {

    /**
     * Return true if specified directory exists. Otherwise, attempt to create it and return true if successful. Return false if unable to create the directory.
     *
     * @param dir Specified directory.
     * @return True if specified directory exists after running this method, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean chkmkdir(String dir) {
        File dirf = new File(dir);
        if (dirf.exists())
            return true;
        return dirf.mkdirs();
    }

    /**
     * Remove the specified file or directory. If it's a directory, remove all files in the specified directory and the directory itself.
     *
     * @param dir Directory.
     * @return True if successful; false otherwise.
     */
    public static boolean rmAll(String dir) {
        File dirf = new File(dir);
        if (dirf.isDirectory()) {
            File[] files = dirf.listFiles();
            boolean success = true;
            if (files != null) {
                for (File file : files)
                    if (!rmAll(file.getAbsolutePath()))
                        success = false;
            }
            return success;
        }
        return dirf.delete();
    }

    /**
     * Return true if a given path specification exists.
     *
     * @param fspec Path specification.
     * @return True if it exists.
     */
    public static boolean exists(String fspec) {
        return (new File(fspec)).exists();
    }

}
