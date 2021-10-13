package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;
import org.reldb.toolbox.utilities.Directory;
import org.reldb.wrapd.exceptions.FatalException;
import org.reldb.wrapd.generator.JavaGenerator;

import java.io.File;
import java.sql.Connection;
import java.util.*;

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

    private final SQLParameterConverter parameterConverter;
    private final Set<Method> methods = new HashSet<>();

    /**
     * Create a generator of compiled update invokers.
     *
     * @param dir Directory into which generated class(es) will be put.
     * @param packageSpec Package to which generated class(es) belong, in dotted notation.
     * @param queryName Name of generated query class.
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL query text with all {name} converted to ? for subsequent evaluation.
     * @param args Sample arguments.
     */
    public SQLTypeGenerator(String dir, String packageSpec, String queryName, String sqlText, Object... args) {
        this.packageSpec = packageSpec;
        if (queryName.startsWith(packageSpec))
            queryName = queryName.substring(packageSpec.length() + 1);
        this.dir = dir;
        this.queryName = queryName;
        this.args = args;
        parameterConverter = new SQLParameterConverter(sqlText);
    }

    /**
     * Get SQL text. May be modified by generate().
     *
     * @return SQL text.
     */
    public String getSQLText() {
        return parameterConverter.getSQLText();
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
     * Obtain the declared parameter list, with optional additional
     * Database and Connection parameters.
     *
     * @param withDatabase If true, include 'Database database' before the declared parameters.
     * @param withConnection If true, include 'Connection connection' before the declared parameters.
     * @return The parameters.
     */
    protected List<Attribute> getParameterList(boolean withDatabase, boolean withConnection) {
        var attributes = new LinkedList<Attribute>();
        if (withDatabase)
            attributes.add(new Attribute("db", Database.class));
        if (withConnection)
            attributes.add(new Attribute("connection", Connection.class));
        int parameterNumber = 0;
        if (hasArgs())
            for (Object arg: args) {
                var type = arg.getClass();
                var name = parameterConverter.getParameterNames().get(parameterNumber++);
                var attribute = new Attribute(name, type);
                attributes.add(attribute);
            }
        return attributes;
    }

    /**
     * Return a parameter list specification string.
     *
     * @param withConnection True to include "Connection connection" parameter.
     * @return Parameter list definition string.
     */
    protected String getParameterDefinitionListString(boolean withConnection) {
        var out = new StringBuffer();
        for (Attribute attribute: getParameterList(true, withConnection)) {
            if (out.length() > 0)
                out.append(", ");
            out.append(attribute.type.getCanonicalName()).append(" ").append(attribute.name);
        }
        return out.toString();
    }

    /**
     * Return a parameter list reference string.
     * E.g., if the parameter definition string is "int x, String y", the parameter list reference string
     * is "x, y".
     *
     * This is just the declared query parameters, <b>not</b> additional parameters like Database or
     * Connection.
     *
     * @return Parameter list reference string.
     */
    protected String getDeclaredQueryParameterNameListString() {
        if (!hasArgs())
            return "null";
        var out = new StringBuilder();
        for (Attribute attribute: getParameterList(false, false)) {
            if (out.length() > 0)
                out.append(", ");
            out.append(attribute.name);
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
        /** Base method name. */
        public final String name;

        /** Qualifier to method name. */
        public final String qualifier;

        /** Method parameter list. */
        public final List<Attribute> parameters;

        /** Return type name or specification. Null if void. */
        public final String returns;

        /** Constructor.
         *
         * @param name Base method name -- query, update, valueOf, etc.
         * @param qualifier Extension to method name -- ForUpdate, All, etc.
         * @param parameters Method parameter list.
         * @param returns Return type name or specification. Null if void.
         */
        public Method(String name, String qualifier, List<Attribute> parameters, String returns) {
            this.name = name;
            this.qualifier = qualifier;
            this.parameters = parameters;
            this.returns = returns;
        }

        public String toString() {
            var outBuffer = new StringBuilder();
            if (returns == null)
                outBuffer.append("void ");
            else
                outBuffer.append(returns).append(" ");
            outBuffer.append(name).append(qualifier).append("(");
            var parmList = new StringBuilder();
            for (Attribute parameter: parameters) {
                if (parmList.length() > 0)
                    parmList.append(", ");
                parmList.append(parameter.type.getCanonicalName()).append(" ").append(parameter.name);
            }
            outBuffer.append(parmList);
            outBuffer.append(")");
            return outBuffer.toString();
        }
    }

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
        parameterConverter.process();
        return new JavaGenerator(dir).generateJavaCode(queryName, packageSpec, getDefinitionSourceCode());
    }
}
