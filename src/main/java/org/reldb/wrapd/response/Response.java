package org.reldb.wrapd.response;

import org.reldb.toolbox.il8n.Msg;
import org.reldb.toolbox.il8n.Str;

/**
 * Effectively a union type of some value T or a (Throwable) error.
 *
 * @param <T> The type of the non-error value.
 */
public class Response<T> {
    private static final Msg MsgError = new Msg("Error:", Response.class);
    private static final Msg MsgCausedBy = new Msg("Caused by:", Response.class);

    /**
     * Create a valid, non-error response.
     *
     * @param <T> The value type.
     * @param value The non-error, valid value.
     * @return A Response.
     */
    public static <T> Response<T> set(T value) {
        return new Response<>(value);
    }

    /**
     * Create an error response.
     *
     * @param <T> The value type.
     * @param error The Throwable error.
     * @return A Response.
     */
    public static <T> Response<T> set(Throwable error) {
        return new Response<>(error);
    }

    /** The valid value of this Response. */
    public final T value;

    /** The Throwable error. */
    public final Throwable error;

    /**
     * Create a valid, non-error response.
     *
     * @param value The non-error, valid value.
     */
    protected Response(T value) {
        this.value = value;
        this.error = null;
    }

    /**
     * Create an error response.
     *
     * @param error The Throwable error.
     */
    protected Response(Throwable error) {
        this.value = null;
        this.error = error;
    }

    /**
     * True if the value is not null.
     *
     * @return boolean
     */
    public boolean isValid() {
        return value != null;
    }

    /**
     * True if error is null.
     *
     * @return boolean
     */
    public boolean isError() {
        return error != null;
    }

    /**
     * If this Response isError() == true, dump the error.
     */
    public void printIfError() {
        if (error != null)
            printError(Str.ing(MsgError), error);
    }

    /**
     * Dump an error's description, and its cause(s).
     *
     * @param prompt Text to precede error message.
     * @param error Throwable error.
     */
    public static void printError(String prompt, Throwable error) {
        System.out.println(
                (prompt != null
                    ? prompt + " "
                    : "")
                + error);
        if (error.getCause() != null)
            printError(Str.ing(MsgCausedBy), error.getCause());
    }

    /**
     * Dump an error's description, and its cause(s).
     *
     * @param error Throwable error.
     */
    public static void printError(Throwable error) {
        printError(null, error);
    }
}
