package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Response;

/**
 * For defining lambdas that return a Response.
 */
public interface ResultAction<T> {
    /**
     * Run something.
     *
     * @return A Result.
     */
    Response<T> run();
}
