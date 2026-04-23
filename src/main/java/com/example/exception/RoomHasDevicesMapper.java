package com.example.exception;

import com.example.model.ApiError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.1 – Maps RoomHasDevicesException → HTTP 409 Conflict.
 *
 * Returns a JSON body explaining that the room still has active hardware
 * and cannot be decommissioned until all sensors are removed first.
 */
@Provider
public class RoomHasDevicesMapper implements ExceptionMapper<RoomHasDevicesException> {

    @Override
    public Response toResponse(RoomHasDevicesException ex) {
        return Response
                .status(Response.Status.CONFLICT)
                .entity(new ApiError(409, ex.getMessage()))
                .build();
    }
}
