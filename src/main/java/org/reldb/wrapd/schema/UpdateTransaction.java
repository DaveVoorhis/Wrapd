package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Result;

/**
 * Interface to define lamdbas that return a Result and require a ResultAction parameter.
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
