package org.example.carshering.common.exceptions.custom;

import org.example.carshering.common.exceptions.ApplicationException;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {
  public NotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }
  public NotFoundException(String message, String... args) {
      this(String.format(message, args));
  }
}
