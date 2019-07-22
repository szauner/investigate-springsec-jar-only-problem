package com.company.service.util;

public class OperationNotSuccessfulException extends Exception {
    private static final long serialVersionUID = 1L;

    public OperationNotSuccessfulException() {
        super();
    }

    public OperationNotSuccessfulException(String message) {
        super(message);
    }
}