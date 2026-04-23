package com.example.controller;

import com.example.exception.DeviceOfflineException;
import com.example.model.Sensor;
import com.example.model.SensorReading;
import com.example.storage.CampusRegistry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Part 4.2 – Historical Data Management (Sub-Resource).
 *
 * This class has NO @Path annotation at the class level.
 * It is instantiated by SensorController's sub-resource locator method,
 * which already carries the /sensors/{sensorId}/readings path context.
 *
 * GET  /api/v1/sensors/{sensorId}/readings   – retrieve full reading history
 * POST /api/v1/sensors/{sensorId}/readings   – append a new reading
 *
 * Side-effect on POST: the parent Sensor's currentValue field is updated
 * to reflect the latest measurement, keeping the sensor object consistent
 * with its most recent reading.
 *
 * State constraint (Part 5.3):
 * Sensors in MAINTENANCE status are physically disconnected and cannot
 * accept new readings → DeviceOfflineException → HTTP 403 Forbidden.
 */
public class ReadingController {

    private final String sensorId;

    public ReadingController(String sensorId) {
        this.sensorId = sensorId;
    }

    // ---------------------------------------------------------------- GET history
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> getHistory() {
        List<SensorReading> history = CampusRegistry.getReadings().get(sensorId);
        if (history == null) {
            return Collections.emptyList();
        }
        return history;
    }

    // ---------------------------------------------------------------- POST new reading
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitReading(SensorReading reading) {

        Sensor sensor = CampusRegistry.getSensors().get(sensorId);

        // Part 5.3 – sensors under MAINTENANCE cannot log readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new DeviceOfflineException(
                    "Sensor '" + sensorId + "' is in MAINTENANCE mode and cannot " +
                    "accept readings until it is returned to ACTIVE status.");
        }

        // Auto-assign UUID if the client did not supply one
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        // Auto-assign epoch timestamp if missing
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist reading
        List<SensorReading> history = CampusRegistry.getReadings()
                .computeIfAbsent(sensorId, k ->
                        Collections.synchronizedList(new ArrayList<>()));
        history.add(reading);

        // Side-effect: synchronise currentValue on the parent sensor
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
