package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class CarNotFoundException extends ApplicationException {
    public CarNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
