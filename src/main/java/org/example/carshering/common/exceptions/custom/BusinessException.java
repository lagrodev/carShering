package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class BusinessException extends ApplicationException {
    public BusinessException(String message) {
        super(message,  HttpStatus.CONFLICT);
    }
}
