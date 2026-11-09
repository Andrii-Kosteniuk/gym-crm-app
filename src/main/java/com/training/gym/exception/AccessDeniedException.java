package com.training.gym.exception;

public class AccessDeniedException extends RuntimeException{

    public AccessDeniedException(String message) {
        super("Access denied: " + message);
    }
}
