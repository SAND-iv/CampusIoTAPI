package com.example.exception;

/**
 * Thrown when a requested room ID does not exist in CampusRegistry.
 * Mapped to HTTP 404 Not Found by RoomNotFoundMapper.
 */
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }
}
