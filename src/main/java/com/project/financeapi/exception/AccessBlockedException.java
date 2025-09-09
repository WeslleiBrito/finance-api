package com.project.financeapi.exception;

import org.springframework.http.HttpStatus;

public class AccessBlockedException extends BusinessException {

    public AccessBlockedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
