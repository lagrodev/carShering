package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidContractStateException extends ApplicationException {
    public InvalidContractStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
