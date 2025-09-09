package com.project.financeapi.exception;

import org.springframework.http.HttpStatus;

public class InvalidEmailOrPasswordException extends BusinessException {

    public InvalidEmailOrPasswordException( String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
