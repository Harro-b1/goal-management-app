package com.harro.goaltracker.exceptions;

import com.harro.goaltracker.util.StringUtil;

public class NullAssignmentException extends RuntimeException {
    public NullAssignmentException(String field) {
        super(StringUtil.snakeToCamel(field) + " cannot be null");
    }
}
