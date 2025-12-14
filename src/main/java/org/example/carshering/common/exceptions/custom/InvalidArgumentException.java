package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidArgumentException extends ApplicationException {
    public InvalidArgumentException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

}
