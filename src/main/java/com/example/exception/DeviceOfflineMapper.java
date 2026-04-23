package com.example.exception;

import com.example.model.ApiError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.3 – Maps DeviceOfflineException → HTTP 403 Forbidden.
 */
@Provider
public class DeviceOfflineMapper implements ExceptionMapper<DeviceOfflineException> {

    @Override
    public Response toResponse(DeviceOfflineException ex) {
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(new ApiError(403, ex.getMessage()))
                .build();
    }
}
