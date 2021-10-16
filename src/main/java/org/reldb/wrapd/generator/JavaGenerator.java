package org.reldb.wrapd.generator;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.wrapd.exceptions.FatalException;

import java.io.*;

/**
 * Machinery for generating Java source code.
 */
public class JavaGenerator {
    private final static Msg ErrSavingJavaSource = new Msg("Unable to save Java source: {0}.", JavaGenerator.class);
    private final static Msg ErrUnableToCreateResourceDir = new Msg("Unable to create resource directory: {0}.", JavaGenerator.class);
    private final static Msg ErrUnableToCreatePackageDir = new Msg("Unable to create package directory: {0}.", JavaGenerator.class);

    private final String userSourcePath;

    /**
     * Constructor.
     *
     * @param userSourcePath Specifies path to source code.
     */
    public JavaGenerator(String userSourcePath) {
        this.userSourcePath = userSourcePath;
    }


    /**
     * Given a source code directory and a package specification, get the directory they specify.
     *
     * @param sourcePath Source path.
     * @param packageSpec Package specification.
     * @return Target directory specification.
     */
    public static String obtainDirectoryFromSourcePathAndPackage(String sourcePath, String packageSpec) {
        return sourcePath + "/" + packageSpec.replace('.', '/');
    }

    /**
     * Generate compilable Java code.
     *
     * @param className The class name to be generated.
     * @param packageSpec The package.
     * @param src The source code.
     * @return Generated source code file.
     */
    public File generateJavaCode(String className, String packageSpec, String src) {
        // If resource directory doesn't exist, create it.
        var resourceDir = new File(userSourcePath);
        if (!(resourceDir.exists()))
            if (!resourceDir.mkdirs())
                throw new FatalException(Str.ing(ErrUnableToCreateResourceDir, resourceDir.toString()));
        File sourcef;
        try {
            // Convert package to directories
            var packageDir = obtainDirectoryFromSourcePathAndPackage(userSourcePath, packageSpec);
            var packageDirFile = new File(packageDir);
            if (!packageDirFile.exists())
                if (!packageDirFile.mkdirs())
                    throw new FatalException(Str.ing(ErrUnableToCreatePackageDir, packageDirFile.toString()));
            // Write source to a Java source file
            sourcef = new File(packageDir + "/" + getStrippedClassname(className) + ".java");
            var sourcePS = new PrintStream(new FileOutputStream(sourcef));
            sourcePS.print(src);
            sourcePS.close();
        } catch (IOException ioe) {
            throw new FatalException(Str.ing(ErrSavingJavaSource, ioe.toString()));
        }
        return sourcef;
    }

    /**
     * Get a stripped name.  Only return text after the final '.'
     */
    private static String getStrippedName(String name) {
        var lastDot = name.lastIndexOf('.');
        return (lastDot >= 0)
            ? name.substring(lastDot + 1)
            : name;
    }

    /**
     * Get stripped Java Class name.
     */
    private static String getStrippedClassname(String name) {
        return getStrippedName(name);
    }

}
