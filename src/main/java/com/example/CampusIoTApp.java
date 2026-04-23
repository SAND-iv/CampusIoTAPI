package com.example;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Part 1.1 – Application configuration.
 *
 * @ApplicationPath("/api/v1") establishes the versioned root for every
 * endpoint in this service.  Jersey discovers all resource classes,
 * exception mappers and filters by scanning the com.example package
 * tree (configured in web.xml).
 *
 * JAX-RS Lifecycle note (Part 1.1 report question):
 * By default JAX-RS instantiates a brand-new resource object for every
 * incoming HTTP request (per-request scope).  This means instance fields
 * are NOT shared between calls, so all mutable, shared state must live
 * outside the resource class – in this project inside CampusRegistry,
 * which uses static ConcurrentHashMap collections that survive the
 * lifetime of the whole application.
 */
@ApplicationPath("/api/v1")
public class CampusIoTApp extends Application {
    // Jersey auto-discovers providers via package scanning in web.xml,
    // so no manual class registration is needed here.
}
