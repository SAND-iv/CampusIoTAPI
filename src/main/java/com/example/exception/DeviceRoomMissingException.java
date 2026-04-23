package com.example.exception;

/**
 * Part 5.2 – Dependency Validation (422 Unprocessable Entity).
 *
 * Thrown when a POST /sensors body references a roomId that does not exist.
 * This is a semantic error inside a valid JSON payload, not a missing URI
 * resource – hence 422 rather than 404.
 *
 * Report question Part 5.2:
 * HTTP 422 is more accurate than 404 here because the request URI
 * /api/v1/sensors is perfectly valid and reachable.  The problem lies inside
 * the request body: a foreign-key reference points to a non-existent resource.
 * A 404 would mislead the client into thinking the wrong URL was used.
 * HTTP 422 ("the server understands the content type and syntax of the request
 * but was unable to process the contained instructions") precisely describes
 * the situation, giving client developers an actionable signal to fix their
 * payload rather than their URL.
 */
public class DeviceRoomMissingException extends RuntimeException {
    public DeviceRoomMissingException(String message) {
        super(message);
    }
}
