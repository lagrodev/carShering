package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class UnverifiedClientDocumentException extends ApplicationException {
    public UnverifiedClientDocumentException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
