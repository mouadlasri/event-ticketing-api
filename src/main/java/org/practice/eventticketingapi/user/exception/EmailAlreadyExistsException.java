package org.practice.eventticketingapi.user.exception;

import org.practice.eventticketingapi.exception.ResourceAlreadyExistsException;

public class EmailAlreadyExistsException extends ResourceAlreadyExistsException {
    public EmailAlreadyExistsException() {
        super("Email already exists");
    }
}
