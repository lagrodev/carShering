package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class DocumentTypeException extends ApplicationException {
    public DocumentTypeException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
