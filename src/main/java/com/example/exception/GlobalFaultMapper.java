package com.example.exception;

import com.example.model.ApiError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Part 5.4 – Global Safety Net (HTTP 500).
 *
 * Catches any Throwable not handled by a more specific mapper and returns
 * a clean, generic HTTP 500 JSON response.  The full stack trace is logged
 * server-side (visible in the NetBeans / Tomcat output window) but is
 * NEVER sent to the client.
 *
 * Report question Part 5.4 – Security risks of exposing stack traces:
 * Raw stack traces reveal the internal package structure and class names of
 * the application, helping an attacker map the codebase.  They expose which
 * third-party libraries and framework versions are in use, allowing targeted
 * exploitation of known CVEs.  They can leak file paths, SQL fragments, and
 * configuration values that appear in exception messages.  They also expose
 * the server-side logic flow, making it easier to craft inputs that trigger
 * specific error paths.  This mapper ensures that none of that information
 * ever leaves the server boundary.
 */
@Provider
public class GlobalFaultMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG =
            Logger.getLogger(GlobalFaultMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log full detail server-side only
        LOG.log(Level.SEVERE, "Unhandled exception intercepted by GlobalFaultMapper", ex);

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ApiError(500,
                        "An unexpected internal error occurred. " +
                        "Please contact the system administrator."))
                .build();
    }
}
