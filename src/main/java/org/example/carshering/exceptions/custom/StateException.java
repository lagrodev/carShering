package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class StateException extends ApplicationException {
    public StateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
