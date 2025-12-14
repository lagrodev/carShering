package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class PasswordException extends ApplicationException {
    public PasswordException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
