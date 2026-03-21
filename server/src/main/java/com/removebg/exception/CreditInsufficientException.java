package com.removebg.exception;

public class CreditInsufficientException extends RuntimeException {
    public CreditInsufficientException(String message) {
        super(message);
    }
}
