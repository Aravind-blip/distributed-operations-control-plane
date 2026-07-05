package com.opscontrolplane.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resourceType, Object id) {
        return new ResourceNotFoundException(resourceType + " with id " + id + " not found");
    }
}
