package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class BusinessConflictException extends ApplicationException {
    public BusinessConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
