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
     * Create a valid response.
     *
     * @param value boolean true/false.
     * @return A Result instance.
     */
    public static Result is(boolean value) {
        return value ? OK : FAIL;
    }

    /**
     * Create an invalid, error response.
     *
     * @param error The Throwable error.
     * @return A Result instance.
     */
    public static Result is(Throwable error) {
        return new Result(error);
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
     * @param error The Throwable error to be recorded in this Result.
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
        return isValid() && value != null && value;
    }
}
