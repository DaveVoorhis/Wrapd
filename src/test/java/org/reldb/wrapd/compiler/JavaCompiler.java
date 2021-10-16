package org.reldb.wrapd.compiler;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Machinery for compiling Java code.
 */
public class JavaCompiler {
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
     * Encapsulates parameters to and results of compilation, including compiler messages.
     */
    public static class CompilationResults {
        /** True if compilation successful. */
        public final boolean compiled;

        /** The compiler messages. */
        public final String compilerMessages;

        /** The compiler invocation commandline */
        public final String commandLine;

        /** The filespec of the file to be compiled. */
        public final File sourceFile;

        /**
         * Constructor.
         *
         * @param compiled True if compilation successful.
         * @param compilerMessages Compiler-generated messages.
         * @param commandLine Compiler-invocation command-line.
         * @param sourceFile Filespec of the file to be compiled.
         */
        public CompilationResults(boolean compiled, String compilerMessages, String commandLine, File sourceFile) {
            this.compiled = compiled;
            this.compilerMessages = compilerMessages;
            this.commandLine = commandLine;
            this.sourceFile = sourceFile;
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
     * @param sourcef The source file to compile.
     * @return CompilationResults.
     */
    public CompilationResults compileJavaCode(String classpath, File sourcef) {
        var messageStream = new ByteArrayOutputStream();
        var warningStream = new ByteArrayOutputStream();
        var warningSetting = "allDeprecation,"
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

        // Start compilation using JDT
        var commandLine = "-11 -source 11 -warn:" +
                warningSetting + " " +
                "-cp " + classpath + " \"" + sourcef + "\"";
        var compiled = BatchCompiler.compile(
                commandLine,
                new PrintWriter(messageStream),
                new PrintWriter(warningStream),
                null);

        var compilerMessages = new StringBuilder();
        // Parse the messages and the warnings.
        var br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(messageStream.toByteArray())));
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
        return new CompilationResults(compiled, compilerMessages.toString(), commandLine, sourcef);
    }

    /**
     * Get the default class path.
     *
     * @return Class path.
     */
    public String getDefaultClassPath() {
        return cleanClassPath(System.getProperty("java.class.path")) +
                File.pathSeparatorChar +
                cleanClassPath(getLocalClasspath());
    }

    /**
     * Return a classpath cleaned of non-existent files and Web Start's deploy.jar.
     * Classpath elements with spaces are converted to quote-delimited strings.
     */
    private static String cleanClassPath(String string) {
        string = (File.separatorChar == '/')
            ? string.replace('\\', '/')
            : string.replace('/', '\\');
        var outstr = new StringBuilder();
        var stringTokenizer = new StringTokenizer(string, File.pathSeparator);
        while (stringTokenizer.hasMoreElements()) {
            String element = (String) stringTokenizer.nextElement();
            var file = new File(element);
            if (file.exists() && !element.contains("deploy.jar")) {
                var fname = file.toString();
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

}
