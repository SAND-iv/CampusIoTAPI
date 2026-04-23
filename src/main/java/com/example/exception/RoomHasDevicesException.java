package com.example.exception;

/**
 * Part 5.1 – Resource Conflict (409).
 *
 * Thrown when a DELETE /rooms/{id} is attempted on a room that still has
 * sensors assigned.  Mapped to HTTP 409 Conflict by RoomHasDevicesMapper.
 */
public class RoomHasDevicesException extends RuntimeException {
    public RoomHasDevicesException(String message) {
        super(message);
    }
}
