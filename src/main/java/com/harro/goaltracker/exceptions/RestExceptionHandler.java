package com.harro.goaltracker.exceptions;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;
    private static final int MYSQL_COLUMN_CANNOT_BE_NULL = 1048;

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleConflict(DataIntegrityViolationException e){
        if(e.getCause() instanceof ConstraintViolationException cve){
            String field = stripTablePrefix(cve.getConstraintName());

            if(cve.getErrorCode() == MYSQL_DUPLICATE_ENTRY){
                return handleDuplicateData(new DuplicateDataException(field));
            }
            if(cve.getErrorCode() == MYSQL_COLUMN_CANNOT_BE_NULL){
                return handleNullAssignment(new NullAssignmentException(field));
            }
        }

        return ResponseEntity.status(409).body(null);
    }

    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<?> handleDuplicateData(DuplicateDataException e){
        return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(409));
    }

    @ExceptionHandler(NullAssignmentException.class)
    public ResponseEntity<?> handleNullAssignment(NullAssignmentException e){
        return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(400));
    }

    @ExceptionHandler(InvalidReferenceException.class)
    public ResponseEntity<?> handleInvalidReference(InvalidReferenceException e){
        return new ResponseEntity<String>(e.getMessage(),HttpStatusCode.valueOf(422));
    }

    private static String stripTablePrefix(String constraintName){
        if(constraintName == null){
            return null;
        }
        int dotIndex = constraintName.indexOf('.');
        return dotIndex == -1 ? constraintName : constraintName.substring(dotIndex + 1);
    }
}
