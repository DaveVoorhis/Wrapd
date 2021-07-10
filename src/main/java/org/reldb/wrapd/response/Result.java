package org.reldb.wrapd.response;

import java.sql.SQLException;

public class Result extends Response<Boolean> {
    public static final Result OK = new Result(true);
    public static final Result FAIL = new Result(false);

    public static Result ERROR(Throwable error) {
        return new Result(error);
    }

    protected Result(boolean value) {
        super(value);
    }

    protected Result(Throwable error) {
        super(error);
    }

    public final boolean isOk() {
        return isValid();
    }
}
