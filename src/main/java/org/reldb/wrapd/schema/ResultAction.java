package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Response;

/**
 * For defining lambdas that return a Result.
 */
public interface ResultAction {
    /**
     * Run something.
     *
     * @return A Result.
     */
    Response run();
}
