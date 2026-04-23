package com.example.exception;

/**
 * Thrown when a requested sensor ID does not exist in CampusRegistry.
 * Mapped to HTTP 404 Not Found by SensorNotFoundMapper.
 */
public class SensorNotFoundException extends RuntimeException {
    public SensorNotFoundException(String message) {
        super(message);
    }
}
