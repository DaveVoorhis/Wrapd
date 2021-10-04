package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.generator.JavaGenerator;

import java.io.File;

/**
 * Generates Java code to represent a SQL construct.
 */
public abstract class SQLTypeGenerator {
    private static final Msg ErrUnableToCreateOrOpenCodeDirectory = new Msg("Unable to create or open code directory {0}.", SQLTypeGenerator.class);

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

    private final String dir;
    private final String packageSpec;
    private final String queryName;
    private final String sqlText;
    private final Object[] args;

    /**
     * Create a generator of compiled update invokers.
     *
     * @param dir Directory into which generated class(es) will be put.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param queryName Name of generated query class.
     * @param sqlText SQL query text.
     * @param args Sample arguments.
     */
    public SQLTypeGenerator(String dir, String packageSpec, String queryName, String sqlText, Object[] args) {
        this.packageSpec = packageSpec;
        if (queryName.startsWith(packageSpec))
            queryName = queryName.substring(packageSpec.length() + 1);
        this.dir = dir;
        this.queryName = queryName;
        this.sqlText = sqlText;
        this.args = args;
        if (!Directory.chkmkdir(dir))
            throw new FatalException(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
    }

    protected String getSQLText() {
        return sqlText;
    }

    protected String getQueryName() {
        return queryName;
    }

    protected Object[] getRawArgs() {
        return args;
    }

    protected String getPackageSpec() {
        return packageSpec;
    }

    protected boolean hasArgs() {
        return args != null && args.length > 0;
    }

    protected String getParms(boolean withConnection) {
        String parmConnection = withConnection
                ? ", Connection connection"
                : "";
        StringBuilder out = new StringBuilder("Database db" + parmConnection);
        int parameterNumber = 0;
        if (hasArgs()) {
            for (Object arg: args)
                out.append(", ").append(arg.getClass().getCanonicalName()).append(" p").append(parameterNumber++);
        }
        return out.toString();
    }

    protected String getArgs() {
        if (!hasArgs())
            return "null";
        var out = new StringBuilder();
        for (var parameterNumber = 0; parameterNumber < args.length; parameterNumber++) {
            if (out.length() > 0)
                out.append(", ");
            out.append("p").append(parameterNumber);
        }
        return out.toString();
    }

    /**
     * Obtain the Java text for this definition.
     *
     * @return Query text.
     */
    protected abstract String getQueryDef();

    /**
     * Generate this query type as a Java class definition.
     *
     * @return The generated Java source file.
     */
    public File generate() {
        return new JavaGenerator(dir).generateJavaCode(queryName, packageSpec, getQueryDef());
    }

}
