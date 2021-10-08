package org.reldb.wrapd.response;

/** Union of a boolean and a Throwable */
public class Result extends Response<Boolean> {
    public final static Result True = new Result(true);
    public final static Result False = new Result(false);

    public static Result Ok(boolean flag) {
        return flag ? True : False;
    }

    public static Result Error(Throwable t) {
        return new Result(t);
    }

    protected Result(boolean flag) {
        super(flag);
    }

    protected Result(Throwable t) {
        super(t);
    }
}
