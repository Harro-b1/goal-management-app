package com.harro.goaltracker.exceptions;

public class InvalidReferenceException extends RuntimeException{
    final String field;

    public InvalidReferenceException(String field) {
        super("Invalid " + field + " reference id");
        this.field = field;
    }
}
