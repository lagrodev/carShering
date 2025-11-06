package org.example.carshering.exceptions.custom;


import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidDataException extends ApplicationException {
    public InvalidDataException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}
