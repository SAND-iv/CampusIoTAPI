package com.example.controller;

import com.example.exception.DeviceRoomMissingException;
import com.example.exception.SensorNotFoundException;
import com.example.model.ApiError;
import com.example.model.Sensor;
import com.example.storage.CampusRegistry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Part 3 – Sensor Operations.
 *
 * POST   /api/v1/sensors               – register a sensor (validates roomId)
 * GET    /api/v1/sensors               – list all sensors; supports ?type= filter
 * GET    /api/v1/sensors/{sensorId}    – fetch a single sensor
 * DELETE /api/v1/sensors/{sensorId}    – remove a sensor
 *
 * Part 4 – Sub-Resource Locator
 * ANY    /api/v1/sensors/{sensorId}/readings  – delegated to ReadingController
 *
 * ---
 * Report question Part 3.1 – @Consumes and Content-Type mismatches:
 * The @Consumes(APPLICATION_JSON) annotation tells Jersey that this POST
 * method only accepts requests whose Content-Type header is application/json.
 * If a client sends text/plain or application/xml, Jersey compares the
 * declared media type against the request header before the method body
 * even executes, and automatically rejects the request with HTTP 415
 * Unsupported Media Type.  This protects the Jackson deserialiser from
 * receiving data it cannot parse.
 *
 * Report question Part 3.2 – @QueryParam vs path parameter for filtering:
 * GET /api/v1/sensors?type=CO2 uses @QueryParam because query parameters
 * are the HTTP convention for optional filtering of a collection.  The base
 * resource /sensors means "all sensors"; the query parameter narrows that
 * view without altering the resource's identity.  An alternative path design
 * like /sensors/type/CO2 would imply a distinct resource rather than a
 * filtered view, making it impossible to compose multiple filters cleanly
 * (e.g. ?type=CO2&status=ACTIVE) and breaking REST cache semantics.
 */
@Path("/sensors")
public class SensorController {

    // ---------------------------------------------------------------- POST
    /**
     * Validates that the referenced roomId exists before persisting the sensor.
     * If the room is absent → DeviceRoomMissingException → HTTP 422.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor sensor) {

        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(422)
                    .entity(new ApiError(422, "Sensor 'id' field is required."))
                    .build();
        }

        // Referential integrity: the referenced room must exist
        if (sensor.getRoomId() == null ||
                !CampusRegistry.getRooms().containsKey(sensor.getRoomId())) {
            throw new DeviceRoomMissingException(
                    "Cannot register sensor '" + sensor.getId() +
                    "': room '" + sensor.getRoomId() + "' does not exist in the registry.");
        }

        if (CampusRegistry.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ApiError(409,
                            "Sensor '" + sensor.getId() + "' is already registered."))
                    .build();
        }

        // Default status to ACTIVE when the client omits it
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        CampusRegistry.getSensors().put(sensor.getId(), sensor);

        // Keep parent room's sensorIds list synchronised
        CampusRegistry.getRooms()
                .get(sensor.getRoomId())
                .getSensorIds()
                .add(sensor.getId());

        // Initialise an empty reading history list for this sensor
        CampusRegistry.getReadings()
                .put(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // ---------------------------------------------------------------- GET ALL (+ filter)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> listSensors(@QueryParam("type") String type) {
        List<Sensor> all = new ArrayList<>(CampusRegistry.getSensors().values());
        if (type == null || type.trim().isEmpty()) {
            return all;
        }
        List<Sensor> filtered = new ArrayList<>();
        for (Sensor s : all) {
            if (type.equalsIgnoreCase(s.getType())) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    // ---------------------------------------------------------------- GET BY ID
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Sensor getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = CampusRegistry.getSensors().get(sensorId);
        if (sensor == null) {
            throw new SensorNotFoundException(
                    "Sensor '" + sensorId + "' is not registered in the campus system.");
        }
        return sensor;
    }

    // ---------------------------------------------------------------- DELETE
    @DELETE
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = CampusRegistry.getSensors().get(sensorId);
        if (sensor == null) {
            throw new SensorNotFoundException(
                    "Sensor '" + sensorId + "' is not registered in the campus system.");
        }
        // Remove this sensor ID from its parent room's list
        String roomId = sensor.getRoomId();
        if (roomId != null && CampusRegistry.getRooms().containsKey(roomId)) {
            CampusRegistry.getRooms().get(roomId).getSensorIds().remove(sensorId);
        }
        CampusRegistry.getSensors().remove(sensorId);
        CampusRegistry.getReadings().remove(sensorId);
        return Response.noContent().build(); // 204
    }

    // ---------------------------------------------------------------- SUB-RESOURCE LOCATOR (Part 4)
    /**
     * Sub-resource locator: no HTTP method annotation, only @Path.
     * JAX-RS does NOT invoke this method directly – instead it uses the
     * returned ReadingController instance to resolve the final GET or POST
     * on /sensors/{sensorId}/readings.
     *
     * Report question Part 4.1 – Sub-Resource Locator benefits:
     * Delegating reading logic to a dedicated ReadingController class enforces
     * separation of concerns: SensorController manages sensor CRUD, and
     * ReadingController manages historical data.  This keeps each class focused
     * and independently testable.  In a large API with many nested resource
     * types, placing every path variant in one class would create an
     * unmaintainable monolith.  The sub-resource pattern scales complexity
     * proportionally by keeping class size proportional to feature set.
     */
    @Path("/{sensorId}/readings")
    public ReadingController getReadingController(@PathParam("sensorId") String sensorId) {
        // Validate the parent sensor exists before delegating
        if (!CampusRegistry.getSensors().containsKey(sensorId)) {
            throw new SensorNotFoundException(
                    "Sensor '" + sensorId + "' is not registered in the campus system.");
        }
        return new ReadingController(sensorId);
    }
}
