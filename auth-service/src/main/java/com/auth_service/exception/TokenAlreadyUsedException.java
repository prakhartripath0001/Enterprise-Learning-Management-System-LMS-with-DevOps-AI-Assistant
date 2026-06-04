package com.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException() {
        super("This token has already been used");
    }
}
