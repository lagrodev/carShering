package org.example.carshering.common.exceptions.custom;


import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidDataException extends ApplicationException {
    public InvalidDataException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}
