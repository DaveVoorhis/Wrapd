package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.generator.JavaGenerator;

import javax.lang.model.SourceVersion;
import java.io.File;
import java.util.Vector;

/**
 * Generates Java code to represent a SQL construct.
 */
public abstract class SQLTypeGenerator {
    private static final Msg ErrUnableToCreateOrOpenCodeDirectory = new Msg("Unable to create or open code directory {0}.", SQLTypeGenerator.class);
    private static final Msg ErrMissingEndBrace = new Msg("Missing end ''}'' in parameter def started at position {0} in {1}", SQLTypeGenerator.class);
    private static final Msg ErrDuplicateParameterName = new Msg("Attempt to define duplicate parameter name {0}.", SQLTypeGenerator.class);
    private static final Msg ErrInvalidIdentifier = new Msg("Parameter name {0} is not a valid Java identifier.", SQLTypeGenerator.class);
    private static final Msg ErrInvalidIdentifierCharacter = new Msg("Parameter name {0} must not contain ''.''.", SQLTypeGenerator.class);

    private static final char ParmChar = '?';
    private static final char ParmNameLeftDelimiter = '{';
    private static final char ParmNameRightDelimiter = '}';

    /**
     * Delete this SQL construct type.
     *
     * @param dir The directory containing the query definition.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param className The class name of the query definition.
     * @return boolean true if definition successfully deleted.
     */
    public static boolean destroy(String dir, String packageSpec, String className) {
        var pathName = dir + File.separator + packageSpec.replace('.', File.separatorChar) + File.separator + className;
        var fJava = new File(pathName + ".java");
        return fJava.delete();
    }

    private String sqlText;

    private final String dir;
    private final String packageSpec;
    private final String queryName;
    private final Object[] args;

    private final Vector<String> parameterNames = new Vector<>();

    private void addParameterName(String rawName) {
        var name = rawName.trim();
        if (name.contains("."))
            throw new IllegalArgumentException(Str.ing(ErrInvalidIdentifierCharacter, name));
        if (!SourceVersion.isName(name))
            throw new IllegalArgumentException(Str.ing(ErrInvalidIdentifier, name));
        if (parameterNames.contains(name))
            throw new IllegalArgumentException(Str.ing(ErrDuplicateParameterName, name));
        parameterNames.add(name);
    }

    private void addAutomaticParameterName() {
        addParameterName("p" + parameterNames.size());
    }

    private void processParameterNames() {
        StringBuffer sql = new StringBuffer(sqlText);
        for (int start = 0; start < sql.length(); start++) {
            if (sql.charAt(start) == ParmChar)
                addAutomaticParameterName();
            else if (sql.charAt(start) == ParmNameLeftDelimiter) {
                var startNamePos = start + 1;
                var endBracePos = sql.indexOf(String.valueOf(ParmNameRightDelimiter), startNamePos);
                if (endBracePos == -1)
                    throw new IllegalArgumentException(Str.ing(ErrMissingEndBrace, start, sql));
                addParameterName(sql.substring(startNamePos, endBracePos));
                sql.replace(start, endBracePos + 1, String.valueOf(ParmChar));
                start = start + 1;
            }
        }
        sqlText = sql.toString();
    }

    /**
     * Create a generator of compiled update invokers.
     *
     * @param dir Directory into which generated class(es) will be put.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param queryName Name of generated query class.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL text with all {name} converted to ? for subsequent evaluation.
     * @param args Sample arguments.
     */
    public SQLTypeGenerator(String dir, String packageSpec, String queryName, String sqlText, Object... args) {
        this.packageSpec = packageSpec;
        if (queryName.startsWith(packageSpec))
            queryName = queryName.substring(packageSpec.length() + 1);
        this.dir = dir;
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
    }

    /**
     * Get SQL text. May be modified by generate().
     *
     * @return SQL text.
     */
    public String getSQLText() {
        return sqlText;
    }

    /**
     * Get query name.
     *
     * @return Query name.
     */
    protected String getQueryName() {
        return queryName;
    }

    /**
     * Get package specification.
     *
     * @return Package specification.
     */
    protected String getPackageSpec() {
        return packageSpec;
    }

    /**
     * Return true if arguments have been specified.
     *
     * @return True if arguments have been specified.
     */
    protected boolean hasArgs() {
        return args != null && args.length > 0;
    }

    /**
     * Return a parameter list specification.
     *
     * @param withConnection True to include 'Connection connection' parameter.
     * @return Parameter list definition string.
     */
    protected String getParms(boolean withConnection) {
        String parmConnection = withConnection
                ? ", Connection connection"
                : "";
        StringBuilder out = new StringBuilder("Database db" + parmConnection);
        int parameterNumber = 0;
        if (hasArgs()) {
            for (Object arg: args) {
                out.append(", ").append(arg.getClass().getCanonicalName()).append(" ").append(parameterNames.get(parameterNumber));
                parameterNumber++;
            }
        }
        return out.toString();
    }

    /**
     * Return a parameter list.
     *
     * @return Parameter list reference string.
     */
    protected String getArgs() {
        if (!hasArgs())
            return "null";
        var out = new StringBuilder();
        for (var parameterNumber = 0; parameterNumber < args.length; parameterNumber++) {
            if (out.length() > 0)
                out.append(", ");
            out.append(parameterNames.get(parameterNumber));
        }
        return out.toString();
    }

    /**
     * Obtain the Java text for this definition.
     *
     * @return Query text.
     */
    protected abstract String getDefinitionSourceCode();

    /**
     * Generate this query type as a Java class definition.
     *
     * @return The generated Java source file.
     */
    public File generate() {
        if (!Directory.chkmkdir(dir))
            throw new FatalException(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
        processParameterNames();
        return new JavaGenerator(dir).generateJavaCode(queryName, packageSpec, getDefinitionSourceCode());
    }

}
