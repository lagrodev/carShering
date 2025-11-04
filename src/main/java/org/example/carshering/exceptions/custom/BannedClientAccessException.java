package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class BannedClientAccessException extends ApplicationException {
    public BannedClientAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
