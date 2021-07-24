package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Result;

/**
 * For defining lambdas that return a Result.
 */
public interface ResultAction {
    Result run();
}
