package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.generator.JavaGenerator;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
    private final Object[] args;

    private final SQLParameterConverter parametriser;

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
        this.args = args;
        parametriser = new SQLParameterConverter(sqlText);
    }

    /**
     * Get SQL text. May be modified by generate().
     *
     * @return SQL text.
     */
    public String getSQLText() {
        return parametriser.getSQLText();
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
                out.append(", ").append(arg.getClass().getCanonicalName()).append(" ").append(parametriser.getParameterNames().get(parameterNumber));
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
            out.append(parametriser.getParameterNames().get(parameterNumber));
        }
        return out.toString();
    }

    /**
     * Obtain the Java text for this definition.
     *
     * @return Query text.
     */
    protected abstract String getDefinitionSourceCode();

    /** A method definition for a generated method. */
    public static class Method {
        /** Method name. */
        public final String name;

        /** Method parameter list. */
        public final Attribute[] parameters;

        /** Return type. Null if void. */
        public final Class<?> returns;

        /** Constructor.
         *
         * @param name Method name.
         * @param parameters Method parameter list.
         * @param returns Return type. Null if void.
         */
        public Method(String name, Attribute[] parameters, Class<?> returns) {
            this.name = name;
            this.parameters = parameters;
            this.returns = returns;
        }
    }

    private Set<Method> methods = new HashSet<>();

    /**
     * Add a method to the list of methods defined in the generated Java and that may be
     * used by external mechanisms.
     *
     * @param method Method being added.
     */
    protected void addMethod(Method method) {
        methods.add(method);
    }

    /**
     * Get a collection of methods defined in the generated Java that may be used by external mechanisms.
     *
     * @return Collection of methods.
     */
    public Collection<Method> getMethods() {
        return methods;
    }

    /**
     * Generate this query type as a Java class definition.
     *
     * @return The generated Java source file.
     */
    public File generate() {
        if (!Directory.chkmkdir(dir))
            throw new FatalException(Str.ing(ErrUnableToCreateOrOpenCodeDirectory, dir));
        parametriser.process();
        return new JavaGenerator(dir).generateJavaCode(queryName, packageSpec, getDefinitionSourceCode());
    }
}
