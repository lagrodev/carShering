package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidQueryParameterException extends ApplicationException {
    public InvalidQueryParameterException(String message) {
        super("Invalid sort property: " + message, HttpStatus.BAD_REQUEST);
    }
}
