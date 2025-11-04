package org.example.carshering.exceptions.custom;


import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApplicationException {
    public AlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}
