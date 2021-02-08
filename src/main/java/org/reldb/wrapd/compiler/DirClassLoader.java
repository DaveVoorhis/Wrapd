/*
 * DirClassLoader.java
 *
 * Created on 21 August 2004, 21:02
 */

package org.reldb.wrapd.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * A class loader to load named classes from a specified directory.  With
 * class unload and class caching.
 *
 * @author dave
 */
public class DirClassLoader extends ClassLoader {

    private static final HashMap<String, Class<?>> classCache = new HashMap<>();

    private final String dir;
    private final String packageName;

    public DirClassLoader(String dir, String packageName) {
        this.dir = dir;
        this.packageName = packageName;
    }

    /**
     * Unload a given Class.
     */
    public void unload(String name) {
        classCache.remove(name);
    }

    public Class<?> findClass(String name) {
        if (!name.startsWith(packageName))
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e1) {
                return null;
            }
        Class<?> clazz = classCache.get(name);
        if (clazz == null) {
            byte[] bytes;
            try {
                bytes = loadClassData(name);
            } catch (IOException e) {
                return null;
            }
            try {
                clazz = defineClass(name, bytes, 0, bytes.length);
            } catch (LinkageError le) {
                var loader = new DirClassLoader(dir, packageName);
                clazz = loader.defineClass(name, bytes, 0, bytes.length);
            }
            classCache.put(name, clazz);
        }
        return clazz;
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

    /**
     * Get Class for given name.  Will check the system loader first, then the specified directory.
     */
    public Class<?> forName(final String name) throws ClassNotFoundException {
        return loadClass(name);
    }

}
