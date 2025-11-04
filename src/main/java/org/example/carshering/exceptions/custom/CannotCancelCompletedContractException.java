package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class CannotCancelCompletedContractException extends ApplicationException {
    public CannotCancelCompletedContractException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
