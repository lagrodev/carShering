package org.example.carshering.exceptions.custom;

import org.example.carshering.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class CarUnavailableOnDatesException extends ApplicationException {
    public CarUnavailableOnDatesException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
