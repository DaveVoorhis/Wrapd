package org.reldb.wrapd.response;

/**
 * A Response that wraps a Boolean.
 */
public class Result extends Response<Boolean> {
    /**
     * A 'true' valid response.
     */
    public static final Result OK = new Result(true);

    /**
     * A 'false' valid response.
     */
    public static final Result FAIL = new Result(false);

    /**
     * Create an invalid, error response.
     *
     * @param error The Throwable error.
     * @return A Result instance.
     */
    public static Result ERROR(Throwable error) {
        return new Result(error);
    }

    /**
     * Create a valid response.
     *
     * @param value boolean true/false.
     * @return A Result instance.
     */
    public static Result BOOLEAN(boolean value) {
        return new Result(value);
    }

    /**
     * Create a Result from a boolean value.
     *
     * @param value boolean true/false.
     */
    protected Result(boolean value) {
        super(value);
    }

    /**
     * Create a Result from a Throwable error.
     *
     * @param error
     */
    protected Result(Throwable error) {
        super(error);
    }

    /**
     * Return true if result is valid and true.
     *
     * @return true if result is valid and true.
     */
    public final boolean isOk() {
        return isValid() && value == true;
    }
}
