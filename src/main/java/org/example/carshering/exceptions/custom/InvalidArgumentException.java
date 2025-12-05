package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidArgumentException extends ApplicationException {
    public InvalidArgumentException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
