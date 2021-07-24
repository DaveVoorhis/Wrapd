package org.reldb.toolbox.il8n;

import java.lang.reflect.Modifier;
import java.util.Vector;

/**
 * Message registry. Later, this will be used to facilitate localisation/internationalisation of error messages.
 */
public class Str {

    private static final Vector<String> strings = new Vector<>();

    private static final int errorText = register("ERROR");
    private static final int warningText = register("WARNING");
    private static final int noteText = register("NOTE");
    private static final int msgPreambleText = register("MSG");
    private static final int fromText = register("from");

    /**
     * Register a message string.
     *
     * @param string The string.
     * @return The message index.
     */
    public static int register(String string) {
        strings.add(string);
        return strings.size() - 1;
    }

    /**
     * Register a message string.
     *
     * @param format Standard Java format string.
     * @param module Module name.
     * @param msgType Type of message.
     * @return Message index.
     */
    public static int register(String format, String module, String msgType) {
        return register(msgType + " " + strings.get(fromText) + " " + module + ": " + format);
    }

    /**
     * Register an Error format message in a given module.
     *
     * @param format Standard Java format string.
     * @param module Module name.
     * @return Message index.
     */
    public static int E(String format, String module) {
        return register(format, module, strings.get(errorText));
    }

    /**
     * Register a Warning format message in a given module.
     *
     * @param format Standard Java format string.
     * @param module Module name.
     * @return Message index.
     */
    public static int W(String format, String module) {
        return register(format, module, strings.get(warningText));
    }

    /**
     * Register a Note format message in a given module.
     *
     * @param format Standard Java format string.
     * @param module Module name.
     * @return Message index.
     */
    public static int N(String format, String module) {
        return register(format, module, strings.get(noteText));
    }

    /**
     * Get the string at a given message index.
     *
     * @param msgIdx Message index.
     * @return A string.
     */
    public static String getString(int msgIdx) {
        if (msgIdx < 0 || msgIdx >= strings.size())
            return null;
        return strings.get(msgIdx);
    }

    /**
     * Given a message index and zero or more arguments, return a string.
     *
     * @param msgIdx Message index.
     * @param objects Parameter arguments.
     * @return Generated string.
     */
    public static String ing(int msgIdx, Object... objects) {
        String formatString = getString(msgIdx);
        if (formatString == null)
            return strings.get(errorText) + ": " + msgIdx;
        return String.format(strings.get(msgPreambleText) + String.format("%05d: ", msgIdx) + formatString, objects);
    }

    /**
     * Given a class containing internationalisable strings, dump them to console.
     *
     * @param strings A class containing internationalisable strings.
     */
    public static void dump(Class<?> strings) {
        var fields = strings.getFields();
        for (java.lang.reflect.Field field : fields) {
            if (Modifier.isPublic(field.getModifiers()))
                try {
                    int value = field.getInt(field);
                    String name = field.getName();
                    System.out.format("MSG%05d  %-40s  %s", value, name, getString(value));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
        }
    }
}
