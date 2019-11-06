package org.reldb.wrapd.utilities;

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
	 * @param dataDir
	 */
	public static void rmAll(String dir) {
		File dirf = new File(dir);
		if (dirf.isDirectory())
			for (File file: dirf.listFiles())
		    	rmAll(file.getAbsolutePath());
	    else
	        dirf.delete();
	}

	public static boolean exists(String fspec) {
		return (new File(fspec)).exists();
	}

}
