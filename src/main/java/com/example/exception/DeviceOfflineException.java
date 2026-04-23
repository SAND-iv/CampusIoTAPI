package com.example.exception;

/**
 * Part 5.3 – State Constraint (403 Forbidden).
 *
 * Thrown when a POST /sensors/{id}/readings is attempted on a sensor
 * whose status is "MAINTENANCE".  The device is physically disconnected
 * and cannot log readings.  Mapped to HTTP 403 Forbidden.
 */
public class DeviceOfflineException extends RuntimeException {
    public DeviceOfflineException(String message) {
        super(message);
    }
}
