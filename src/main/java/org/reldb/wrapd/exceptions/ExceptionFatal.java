package org.reldb.wrapd.exceptions;

/**
 * This exception is thrown when fatal errors are encountered.
 */
public class ExceptionFatal extends Error {

    static final long serialVersionUID = 0;

    /**
     * Create an ExceptionFatal.
     *
     * @param message Message associated with this error.
     */
    public ExceptionFatal(String message) {
        super(message);
    }

    /**
     * Create an ExceptionFatal.
     *
     * @param message Message associated with this error.
     * @param cause Throwable cause of this error.
     */
    public ExceptionFatal(String message, Throwable cause) {
        super(message, cause);
    }

}
