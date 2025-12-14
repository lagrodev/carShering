package org.example.carshering.common.exceptions.custom;


import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApplicationException {
    public AlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}
