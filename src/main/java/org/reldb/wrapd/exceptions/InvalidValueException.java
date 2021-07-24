package org.reldb.wrapd.exceptions;

/**
 * Exception for invalid values.
 */
public class InvalidValueException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidValueException(String msg) {
        super(msg);
    }

}
