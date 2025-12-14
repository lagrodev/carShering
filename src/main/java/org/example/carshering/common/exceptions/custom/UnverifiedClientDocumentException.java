package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class UnverifiedClientDocumentException extends ApplicationException {
    public UnverifiedClientDocumentException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
