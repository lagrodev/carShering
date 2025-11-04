package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class CarNotFoundException extends ApplicationException {
    public CarNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
