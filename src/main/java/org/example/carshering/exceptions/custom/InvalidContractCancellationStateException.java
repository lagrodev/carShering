package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidContractCancellationStateException extends ApplicationException {
    public InvalidContractCancellationStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
