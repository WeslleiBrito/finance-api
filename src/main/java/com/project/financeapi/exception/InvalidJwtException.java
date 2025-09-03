package com.project.financeapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidJwtException extends BusinessException {
    public InvalidJwtException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
