package com.citytech.finance.exception;

/** Thrown when a resource doesn't exist, or exists but the caller may not see it. Maps to 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
