package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends ApplicationException {
    public InvalidPasswordException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
