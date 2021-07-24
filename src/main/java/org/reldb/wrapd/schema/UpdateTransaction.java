package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Result;

/**
 * Interface to define lamdbas that return a Result and require a ResultAction parameter.
 */
public interface UpdateTransaction {
    Result run(ResultAction action);
}
