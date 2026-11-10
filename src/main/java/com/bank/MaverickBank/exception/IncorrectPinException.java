package com.bank.MaverickBank.exception;

public class IncorrectPinException extends RuntimeException {
    public IncorrectPinException(String message) {
        super(message);
    }
}
