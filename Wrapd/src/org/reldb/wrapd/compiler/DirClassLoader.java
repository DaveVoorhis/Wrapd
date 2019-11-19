/*
 * DirClassLoader.java
 *
 * Created on 21 August 2004, 21:02
 */

package org.reldb.wrapd.compiler;

import java.io.*;
import java.util.*;

/**
 * A class loader to load named classes from a specified directory.  With
 * class unload and class caching.
 *
 * @author  dave
 */
public class DirClassLoader extends ClassLoader {

	private static HashMap<String, Class<?>> classCache = new HashMap<String, Class<?>>();

	private String dir;
	private String packageName;

	public DirClassLoader(String dir, String packageName) {
		this.dir = dir;
		this.packageName = packageName;
	}
	
	/** Unload a given Class. */
	public void unload(String name) {
		classCache.remove(name);
	}

	public Class<?> findClass(String name) {
		System.out.print("DirClassLoader: findClass " + name);
		if (!name.startsWith(packageName)) {
			System.out.print(" with system class loader.");
			try {
				System.out.println();
				return Class.forName(name);
			} catch (ClassNotFoundException e1) {
				System.out.println(" ...but not found.");
				return null;
			}
		}
		// TODO - optimise by checking that package is within dir. If not, use system classloader
//		Class<?> clazz = (Class<?>) classCache.get(name);
//		if (clazz == null) {
			byte[] bytes;
			try {
				System.out.print(" load data from " + name);
				bytes = loadClassData(name);
			} catch (IOException e) {
				System.out.println(" ...but load failed.");
				return null;
			}
			try {
				System.out.println(".");
				return defineClass(name, bytes, 0, bytes.length);
			} catch (LinkageError le) {
				System.out.println(" ...and reloaded via new DirClassLoader.");
				return new DirClassLoader(dir, packageName).defineClass(name,  bytes, 0, bytes.length);
			}
//			classCache.put(name, clazz);
//		}
//		System.out.println();
//		return clazz;
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
	
	private byte[] loadClassData(String name) throws IOException {
		File f = getClassFileName(name);
		BytestreamOutputArray byteStream = new BytestreamOutputArray();
		FileInputStream reader = new FileInputStream(f);
		byte[] bytes = new byte[65535];
		while (true) {
			int read = reader.read(bytes);
			if (read < 0)
				break;
			byteStream.put(bytes, 0, read);
		}
		reader.close();
		return byteStream.getBytes();
	}
	
	/** Get Class for given name.  Will check the system loader first, then the specified directory. */
	public Class<?> forName(final String name) throws ClassNotFoundException {
		// Creation of new ClassLoader allows same class name to be reloaded, as when user
		// drops and then re-creates a given user-defined Java-based type.
	//	return new DirClassLoader(dir).loadClass(name);
		return loadClass(name);
	}

}
