package com.example.exception;

import com.example.model.ApiError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps RoomNotFoundException → HTTP 404 Not Found.
 * Pattern from Tutorial Week 09 (DataNotFoundExceptionMapper).
 */
@Provider
public class RoomNotFoundMapper implements ExceptionMapper<RoomNotFoundException> {

    @Override
    public Response toResponse(RoomNotFoundException ex) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new ApiError(404, ex.getMessage()))
                .build();
    }
}
