package com.studyhub.backend.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String email) {
        super("An account with the email " + email + " already exists. Please sign in instead.");
    }
}
