package org.reldb.toolbox.utilities;

import java.io.File;

public class Directory {

    /**
     * Return true if specified directory exists. Otherwise, attempt to create it and return true if successful. Return false if unable to create the directory.
     *
     * @param dir - specified directory
     */
    public static boolean chkmkdir(String dir) {
        File dirf = new File(dir);
        if (!dirf.exists())
            return dirf.mkdirs();
        return true;
    }

    /**
     * Remove the specified file or directory. If it's a directory, remove all files in the specified directory and the directory itself.
     *
     * @param dir - Directory
     */
    public static void rmAll(String dir) {
        File dirf = new File(dir);
        if (dirf.isDirectory()) {
            File[] files = dirf.listFiles();
            if (files != null) {
                for (File file : files)
                    rmAll(file.getAbsolutePath());
            }
        } else
            dirf.delete();
    }

    public static boolean exists(String fspec) {
        return (new File(fspec)).exists();
    }

}
