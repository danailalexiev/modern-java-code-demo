package com.example.accounts.exception;

public class UserSuspendedException extends RuntimeException {

    public UserSuspendedException(long id) {
        super("User %d is suspended".formatted(id));
    }
}
