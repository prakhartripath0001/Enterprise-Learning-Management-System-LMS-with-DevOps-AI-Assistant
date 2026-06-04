package com.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PermissionNotFoundException extends RuntimeException {
    public PermissionNotFoundException(String id) {
        super("Permission not found with id: " + id);
    }
}
