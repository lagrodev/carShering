package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class CarUnavailableOnDatesException extends ApplicationException {
    public CarUnavailableOnDatesException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
