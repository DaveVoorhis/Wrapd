package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Result;

/**
 * Interface to define a lambda that returns a Result and requires a ResultAction parameter.
 */
public interface UpdateTransaction {
    /**
     * Perform an Update transaction.
     *
     * @param action ResultAction.
     * @return Result of running ResultAction.
     */
    Result run(ResultAction action);
}
