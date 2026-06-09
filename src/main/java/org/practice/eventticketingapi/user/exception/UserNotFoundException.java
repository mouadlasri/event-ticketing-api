package org.practice.eventticketingapi.user.exception;

import org.practice.eventticketingapi.exception.ResourceNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(UUID id) {
        super("User not found with id " + id);
    }
}
