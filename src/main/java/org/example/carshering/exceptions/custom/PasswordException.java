package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class PasswordException extends ApplicationException {
    public PasswordException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
