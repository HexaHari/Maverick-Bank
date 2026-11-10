package com.bank.MaverickBank.exception;

public class UnchangedStateException extends RuntimeException {
    public UnchangedStateException(String message) {
        super(message);
    }
}
