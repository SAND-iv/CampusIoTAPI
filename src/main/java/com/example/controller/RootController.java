package com.example.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1.2 – Discovery Endpoint.
 *
 * GET /api/v1
 *
 * Returns a JSON object describing the API: version, contact, and a
 * _links map of all primary resource collections (HATEOAS).
 *
 * HATEOAS report question (Part 1.2):
 * Hypermedia as the Engine of Application State means embedding navigational
 * links directly inside API responses so that clients can explore the entire
 * API starting from a single well-known root URL, without needing a separate
 * documentation page.  If URL structures change in future, clients that
 * follow the links from this discovery response adapt automatically rather
 * than breaking.  It reduces coupling between the client and the server,
 * and makes integration far more resilient.
 */
@Path("/")
public class RootController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> discover() {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service",     "Campus IoT Sensor & Room Management API");
        response.put("version",     "1.0");
        response.put("description", "RESTful service for managing rooms and IoT sensors across Westminster campus");
        response.put("contact",     "facilities@westminster.ac.uk");
        response.put("status",      "operational");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",     "/api/v1");
        links.put("rooms",    "/api/v1/rooms");
        links.put("sensors",  "/api/v1/sensors");
        response.put("_links", links);

        return response;
    }
}
