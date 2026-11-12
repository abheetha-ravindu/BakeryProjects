package com.example.bakeryshop.exception;

// Handles resource not found exception exceptions.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}