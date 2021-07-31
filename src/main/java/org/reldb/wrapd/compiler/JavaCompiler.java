package org.reldb.wrapd.compiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.reldb.toolbox.il8n.Str;
import org.reldb.wrapd.exceptions.FatalException;

import java.io.*;

import static org.reldb.wrapd.il8n.Strings.ErrSavingJavaSource;
import static org.reldb.wrapd.il8n.Strings.ErrUnableToCreateResourceDir;
import static org.reldb.wrapd.il8n.Strings.ErrUnableToCreatePackageDir;

/**
 * Machinery for compiling Java code.
 */
public class JavaCompiler {
    private final static Logger log = LogManager.getLogger(JavaCompiler.class);

    private final String userSourcePath;

    /**
     * Constructor.
     *
     * @param userSourcePath Specifies path to source code.
     */
    public JavaCompiler(String userSourcePath) {
        this.userSourcePath = userSourcePath;
    }

    /**
     * Encapsulates results of compilation, including compiler messages.
     */
    public static class CompilationResults {
        /** True if compilation successful. */
        public final boolean compiled;

        /** The compiler messages. */
        public final String compilerMessages;

        /**
         * Constructor.
         *
         * @param compiled True if compilation successful.
         * @param compilerMessages Compiler-generated messages.
         */
        public CompilationResults(boolean compiled, String compilerMessages) {
            this.compiled = compiled;
            this.compilerMessages = compilerMessages;
        }

        /**
         * Stringify this.
         *
         * @return Compiler messages.
         */
        public String toString() {
            return "CompilationResults:\n" + compilerMessages;
        }
    }

    /**
     * Compile Java code.
     *
     * @param classpath The class path.
     * @param className The class name to be generated.
     * @param packageSpec The package.
     * @param src The source code.
     * @return CompilationResults.
     */
    public CompilationResults compileJavaCode(String classpath, String className, String packageSpec, String src) {
        ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
        ByteArrayOutputStream warningStream = new ByteArrayOutputStream();
        String warningSetting = "allDeprecation,"
                + "assertIdentifier," + "charConcat,"
                + "conditionAssign," + "constructorName," + "deprecation,"
                + "emptyBlock," + "fieldHiding," + "finalBound,"
                + "finally," + "indirectStatic," + "intfNonInherited,"
                + "javadoc," + "localHiding," + "maskedCatchBlocks,"
                + "noEffectAssign," + "pkgDefaultMethod," + "serial,"
                + "semicolon," + "specialParamHiding," + "staticReceiver,"
                + "syntheticAccess," + "unqualifiedField,"
                + "unnecessaryElse," + "uselessTypeCheck," + "unsafe,"
                + "unusedArgument," + "unusedImport," + "unusedLocal,"
                + "unusedPrivate," + "unusedThrown";

        // If resource directory doesn't exist, create it.
        File resourceDir = new File(userSourcePath);
        if (!(resourceDir.exists()))
            if (!resourceDir.mkdirs())
                throw new FatalException(Str.ing(ErrUnableToCreateResourceDir, resourceDir.toString()));
        File sourcef;
        try {
            // Convert package to directories
            var packageDir = userSourcePath + "/" + packageSpec.replace('.', '/');
            var packageDirFile = new File(packageDir);
            if (!packageDirFile.exists())
                if (!packageDirFile.mkdirs())
                    throw new FatalException(Str.ing(ErrUnableToCreatePackageDir, packageDirFile.toString()));
            // Write source to a Java source file
            sourcef = new File(packageDir + "/" + getStrippedClassname(className) + ".java");
            PrintStream sourcePS = new PrintStream(new FileOutputStream(sourcef));
            sourcePS.print(src);
            sourcePS.close();
        } catch (IOException ioe) {
            throw new FatalException(Str.ing(ErrSavingJavaSource, ioe.toString()));
        }

        log.debug(src);
        log.debug("\nCompile:\n" + sourcef);

        // Start compilation using JDT
        String commandLine = "-14 -source 14 -warn:" +
                warningSetting + " " +
                "-cp " + classpath + " \"" + sourcef + "\"";
        log.debug("Compile command: " + commandLine);
        boolean compiled = BatchCompiler.compile(
                commandLine,
                new PrintWriter(messageStream),
                new PrintWriter(warningStream),
                null);

        StringBuilder compilerMessages = new StringBuilder();
        // Parse the messages and the warnings.
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(messageStream.toByteArray())));
        while (true) {
            String str = null;
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str == null) {
                break;
            }
            compilerMessages.append(str).append('\n');
        }
        BufferedReader brWarnings = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(warningStream.toByteArray())));
        while (true) {
            String str = null;
            try {
                str = brWarnings.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str == null) {
                break;
            }
            compilerMessages.append(str).append('\n');
        }
        log.debug(compilerMessages.toString());
        return new CompilationResults(compiled, compilerMessages.toString());
    }

    /**
     * Compile Java code.
     *
     * @param className The class name to be generated.
     * @param packageSpec The package.
     * @param src The source code.
     * @return CompilationResults.
     */
    public CompilationResults compileJavaCode(String className, String packageSpec, String src) {
        String classpath = getDefaultClassPath();
        return compileJavaCode(classpath, className, packageSpec, src);
    }

    /**
     * Get the default class path.
     *
     * @return Class path.
     */
    public String getDefaultClassPath() {
        return cleanClassPath(System.getProperty("java.class.path")) +
                java.io.File.pathSeparatorChar +
                cleanClassPath(getLocalClasspath());
    }

    /**
     * Return a classpath cleaned of non-existent files and Web Start's deploy.jar.
     * Classpath elements with spaces are converted to quote-delimited strings.
     */
    private static String cleanClassPath(String s) {
        if (java.io.File.separatorChar == '/')
            s = s.replace('\\', '/');
        else
            s = s.replace('/', '\\');
        StringBuilder outstr = new StringBuilder();
        java.util.StringTokenizer st = new java.util.StringTokenizer(s, java.io.File.pathSeparator);
        while (st.hasMoreElements()) {
            String element = (String) st.nextElement();
            java.io.File f = new java.io.File(element);
            if (f.exists() && !element.contains("deploy.jar")) {
                String fname = f.toString();
                if (fname.indexOf(' ') >= 0)
                    fname = '"' + fname + '"';
                outstr.append((outstr.length() > 0) ? File.pathSeparator : "").append(fname);
            }
        }
        return outstr.toString();
    }

    /**
     * Return classpath to the core.
     */
    private String getLocalClasspath() {
        return System.getProperty("user.dir") + File.pathSeparatorChar + userSourcePath;
    }

    /**
     * Get a stripped name.  Only return text after the final '.'
     */
    private static String getStrippedName(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0)
            return name.substring(lastDot + 1);
        else
            return name;
    }

    /**
     * Get stripped Java Class name.
     */
    private static String getStrippedClassname(String name) {
        return getStrippedName(name);
    }

}
