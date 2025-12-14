package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class CannotCancelCompletedContractException extends ApplicationException {
    public CannotCancelCompletedContractException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
