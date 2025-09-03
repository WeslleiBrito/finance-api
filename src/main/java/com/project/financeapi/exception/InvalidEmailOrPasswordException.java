package com.project.financeapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidEmailOrPasswordException extends BusinessException {

    public InvalidEmailOrPasswordException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
