package com.harro.goaltracker.exceptions;

public class DuplicateDataException extends RuntimeException {
    final String field;

    public DuplicateDataException(String field) {
        super("Duplicate value for " + field);
        this.field = field;
    }
}
