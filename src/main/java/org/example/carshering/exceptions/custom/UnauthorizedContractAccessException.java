package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class UnauthorizedContractAccessException extends ApplicationException {
    public UnauthorizedContractAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
