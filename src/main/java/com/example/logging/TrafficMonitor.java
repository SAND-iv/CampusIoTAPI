package com.example.logging;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Part 5.5 – API Request and Response Logging Filter.
 *
 * Implements both ContainerRequestFilter and ContainerResponseFilter
 * exactly as taught in Tutorial Week 09 (LoggingFilter).
 *
 * Every incoming request and every outgoing response is logged to the
 * java.util.logging system, which writes to the Tomcat output window in
 * NetBeans – no code changes needed in any controller.
 *
 * Report question Part 5.5 – Filters vs manual Logger.info() calls:
 * Using a JAX-RS filter addresses logging as a cross-cutting concern in
 * one place.  If logging were added manually to every controller method,
 * any new endpoint would require the developer to remember to add log
 * statements, and changing the log format would require editing every
 * controller class.  A single filter intercepts all traffic automatically
 * regardless of which controller handles it, eliminating repetition and
 * ensuring consistent, complete coverage with zero per-method overhead.
 * This is the same principle behind aspect-oriented programming and
 * middleware pipelines.
 */
@Provider
public class TrafficMonitor implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG =
            Logger.getLogger(TrafficMonitor.class.getName());

    /** Logs every incoming HTTP request method and full URI. */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info(">>>  Incoming Request");
        LOG.info("     Method : " + requestContext.getMethod());
        LOG.info("     URI    : " + requestContext.getUriInfo().getAbsolutePath());
    }

    /** Logs the HTTP status code of every outgoing response. */
    @Override
    public void filter(ContainerRequestContext  requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOG.info("<<<  Outgoing Response");
        LOG.info("     Status : " + responseContext.getStatus());
    }
}
