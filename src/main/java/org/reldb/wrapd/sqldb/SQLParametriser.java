package org.reldb.wrapd.sqldb;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;

import javax.lang.model.SourceVersion;
import java.util.List;
import java.util.Vector;

/**
 * Convert SQL text with optional parameters specified as ? or {name} to
 * SQL text with only ? parameters, and obtain a list of parameter names,
 * where a generated parameter name will correspond to each ?, and the specified
 * name will be for each {name}.
 */
public class SQLParametriser {
    private static final Msg ErrMissingEndBrace = new Msg("Missing end ''}'' in parameter def started at position {0} in {1}", SQLParametriser.class);
    private static final Msg ErrDuplicateParameterName = new Msg("Attempt to define duplicate parameter name {0}.", SQLParametriser.class);
    private static final Msg ErrInvalidIdentifier = new Msg("Parameter name {0} is not a valid Java identifier.", SQLParametriser.class);
    private static final Msg ErrInvalidIdentifierCharacter = new Msg("Parameter name {0} must not contain ''.''.", SQLParametriser.class);

    private static final char ParmChar = '?';
    private static final char ParmNameLeftDelimiter = '{';
    private static final char ParmNameRightDelimiter = '}';

    private String sqlText;
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

    /**
     * Create a SQL parameter converter.
     *
     * @param sqlText SQL query text. Parameters may be specified as ? or {name}. If {name} is used, it will
     *                appear as a corresponding Java method name. If ? is used, it will be named pn, where n
     *                is a unique number in the given definition. Use getSQLText() after generate() to obtain final
     *                SQL text with all {name} converted to ? for subsequent evaluation.
     */
    public SQLParametriser(String sqlText) {
        this.sqlText = sqlText;
    }

    /**
     * Generate the revised SQL and parameter name list.
     *
     * @throws IllegalArgumentException is thrown if the parameter specification is invalid.
     */
    public void process() {
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
     * Get SQL text. May be modified by process().
     *
     * @return SQL text.
     */
    public String getSQLText() {
        return sqlText;
    }

    /**
     * Get the parameter names obtained by process().
     *
     * @return List of parameter names, in the order of appearance.
     */
    public List<String> getParameterNames() {
        return parameterNames;
    }

}
