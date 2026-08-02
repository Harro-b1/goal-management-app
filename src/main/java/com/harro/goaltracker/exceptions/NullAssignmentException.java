package com.harro.goaltracker.exceptions;

public class NullAssignmentException extends RuntimeException {
    final String field;

    public NullAssignmentException(String field) {
        super(field + " cannot be null");
        this.field = field;
    }
}
