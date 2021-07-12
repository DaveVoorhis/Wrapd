package org.reldb.wrapd.schema;

import org.reldb.wrapd.response.Result;

public interface UpdateTransaction {
    Result run(ResultAction action);
}
