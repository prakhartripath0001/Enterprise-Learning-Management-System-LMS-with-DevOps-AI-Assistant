package com.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super("Password and confirm password do not match");
    }
}
