package com.hl7client.client;

public class AuthExpiredException extends RuntimeException {

    public AuthExpiredException(String message) {
        super(message);
    }

    public AuthExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
