package com.harro.goaltracker.exceptions;

import com.harro.goaltracker.util.StringUtil;

public class DuplicateDataException extends RuntimeException {
    public DuplicateDataException(String field) {
        super("Duplicate value for " + StringUtil.snakeToCamel(field));
    }
}
