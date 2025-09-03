package com.project.financeapi.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
