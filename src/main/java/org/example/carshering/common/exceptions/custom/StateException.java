package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class StateException extends ApplicationException {
    public StateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
