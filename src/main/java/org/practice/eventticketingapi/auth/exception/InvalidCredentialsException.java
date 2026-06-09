package org.practice.eventticketingapi.auth.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credentials invalid.");
    }
}
