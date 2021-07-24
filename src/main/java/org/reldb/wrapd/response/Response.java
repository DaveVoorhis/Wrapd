package org.reldb.wrapd.response;

/**
 * Effectively a union type of some value T or a (Throwable) error.
 *
 * @param <T> The type of the non-error value.
 */
public class Response<T> {
    /** The valid value of this Response. */
    public final T value;

    /** The Throwable error. */
    public final Throwable error;

    /**
     * Create a valid, non-error response.
     *
     * @param value The non-error, valid value.
     */
    public Response(T value) {
        this.value = value;
        this.error = null;
    }

    /**
     * Create an error response.
     *
     * @param error The Throwable error.
     */
    public Response(Throwable error) {
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
        if (error != null) {
            System.out.println("Error: " + error);
            Throwable cause = error.getCause();
            while (cause != null) {
                System.out.println("Caused by: " + cause);
                cause = cause.getCause();
            }
        }
    }
}
