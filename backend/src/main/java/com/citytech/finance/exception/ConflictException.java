package com.citytech.finance.exception;

/** Thrown when a request clashes with existing state (e.g. email already registered). Maps to 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
