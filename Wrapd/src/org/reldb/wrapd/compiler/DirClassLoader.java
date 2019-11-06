/*
 * DirClassLoader.java
 *
 * Created on 21 August 2004, 21:02
 */

package org.reldb.wrapd.compiler;

import static org.reldb.wrapd.strings.Strings.*;

import java.io.*;
import java.util.*;

import org.reldb.wrapd.strings.Str;

/**
 * A class loader to load named classes from a specified directory.  With
 * class unload and class caching.
 *
 * @author  dave
 */
public class DirClassLoader extends ClassLoader {

	private static HashMap<String, Class<?>> classCache = new HashMap<String, Class<?>>();

	private String dir;

	public DirClassLoader(String dir) {
		this.dir = dir;
	}
	
	/** Unload a given Class. */
	public void unload(String name) {
		classCache.remove(name);
	}

	public Class<?> findClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException cnfe) {
			Class<?> clazz = (Class<?>) classCache.get(name);
			if (clazz == null) {
				byte[] bytes;
				try {
					bytes = loadClassData(name);
				} catch (ClassNotFoundException e) {
					return null;
				}
				clazz = defineClass(name, bytes, 0, bytes.length);
				classCache.put(name, clazz);
			}
			return clazz;
		}
	}

	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = findClass(name);
		if (clazz == null)
			throw new ClassNotFoundException();
		if (resolve)
			resolveClass(clazz);
		return clazz;
	}

	private File getClassFileName(String name) {
		name = name.replace('.', File.separatorChar);
		if (dir.endsWith(File.separator))
			return new File(dir + name + ".class");
		else
			return new File(dir + File.separator + name + ".class");
	}
	
	private byte[] loadClassData(String name) throws ClassNotFoundException {
		File f = getClassFileName(name);
		BytestreamOutputArray byteStream = new BytestreamOutputArray();
		try {
			FileInputStream reader = new FileInputStream(f);
			byte[] bytes = new byte[65535];
			while (true) {
				int read = reader.read(bytes);
				if (read < 0)
					break;
				byteStream.put(bytes, 0, read);
			}
			reader.close();
		} catch (FileNotFoundException fnfe) {
			throw new ClassNotFoundException(Str.ing(ErrFileNotFound1, f.toString(), name));
		} catch (IOException ioe) {
			throw new ClassNotFoundException(Str.ing(ErrReading, f.toString(), ioe.toString()));
		}
		return byteStream.getBytes();
	}
	
	/** Get Class for given name.  Will check the system loader first, then the specified directory. */
	public Class<?> forName(final String name) throws ClassNotFoundException {
		// Creation of new ClassLoader allows same class name to be reloaded, as when user
		// drops and then re-creates a given user-defined Java-based type.
		return new DirClassLoader(dir).loadClass(name);
	}

}
