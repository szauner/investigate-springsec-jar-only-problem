package com.company.service.security;

public class NotAuthenticatedException extends Exception {
    private static final long serialVersionUID = 1L;

    public NotAuthenticatedException() {
        super();
    }

    public NotAuthenticatedException(String message) {
        super(message);
    }
}
