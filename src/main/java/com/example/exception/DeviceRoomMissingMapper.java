package com.example.exception;

import com.example.model.ApiError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.2 – Maps DeviceRoomMissingException → HTTP 422 Unprocessable Entity.
 */
@Provider
public class DeviceRoomMissingMapper implements ExceptionMapper<DeviceRoomMissingException> {

    @Override
    public Response toResponse(DeviceRoomMissingException ex) {
        return Response
                .status(422)
                .entity(new ApiError(422, ex.getMessage()))
                .build();
    }
}
