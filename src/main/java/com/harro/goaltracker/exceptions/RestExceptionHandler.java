package com.harro.goaltracker.exceptions;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    // MySQL (production) vendor error codes.
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;
    private static final int MYSQL_COLUMN_CANNOT_BE_NULL = 1048;
    // H2 (test datasource, see AbstractIntegrationTest) uses its own numeric codes for
    // the same two violations - org.h2.api.ErrorCode.DUPLICATE_KEY_1/NULL_NOT_ALLOWED.
    private static final int H2_DUPLICATE_ENTRY = 23505;
    private static final int H2_COLUMN_CANNOT_BE_NULL = 23502;

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleConflict(DataIntegrityViolationException e){
        if(e.getCause() instanceof ConstraintViolationException cve){
            String field = extractFieldName(cve.getConstraintName());

            if(cve.getErrorCode() == MYSQL_DUPLICATE_ENTRY || cve.getErrorCode() == H2_DUPLICATE_ENTRY){
                return handleDuplicateData(new DuplicateDataException(field));
            }
            if(cve.getErrorCode() == MYSQL_COLUMN_CANNOT_BE_NULL || cve.getErrorCode() == H2_COLUMN_CANNOT_BE_NULL){
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

    // The raw constraint name Hibernate hands back for a violated named UNIQUE
    // constraint (see schema.sql's `<table>__<column>` naming convention) differs by
    // database:
    //  - MySQL:  "categories.categories__name"                      (table.constraint)
    //  - H2:     "PUBLIC.categories__name INDEX PUBLIC.categories__name_INDEX_4"
    //            (schema.constraint, then a trailing " INDEX ..." for the backing index)
    // A NOT NULL violation's constraint name is just the bare column name ("name") on
    // both, so none of these strips apply there - each is a no-op if its marker isn't present.
    private static String extractFieldName(String constraintName){
        if(constraintName == null){
            return null;
        }

        int spaceIndex = constraintName.indexOf(' ');
        String name = spaceIndex == -1 ? constraintName : constraintName.substring(0, spaceIndex);

        int dotIndex = name.indexOf('.');
        name = dotIndex == -1 ? name : name.substring(dotIndex + 1);

        int tablePrefixIndex = name.lastIndexOf("__");
        return tablePrefixIndex == -1 ? name : name.substring(tablePrefixIndex + 2);
    }
}
