package com.example.accounts.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(long id) {
        super("User %d not found".formatted(id));
    }
}
