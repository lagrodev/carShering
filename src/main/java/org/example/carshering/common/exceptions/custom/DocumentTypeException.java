package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class DocumentTypeException extends ApplicationException {
    public DocumentTypeException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
