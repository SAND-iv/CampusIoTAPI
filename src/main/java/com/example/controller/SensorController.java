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

@Path("/sensors")
public class SensorController {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor sensor) {

        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(422)
                    .entity(new ApiError(422, "Sensor 'id' field is required."))
                    .build();
        }

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

        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        CampusRegistry.getSensors().put(sensor.getId(), sensor);

        CampusRegistry.getRooms()
                .get(sensor.getRoomId())
                .getSensorIds()
                .add(sensor.getId());

        CampusRegistry.getReadings()
                .put(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));

        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .header("Location", "/api/v1/sensors/" + sensor.getId())
                .build();
    }

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

    @DELETE
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = CampusRegistry.getSensors().get(sensorId);
        if (sensor == null) {
            throw new SensorNotFoundException(
                    "Sensor '" + sensorId + "' is not registered in the campus system.");
        }
        String roomId = sensor.getRoomId();
        if (roomId != null && CampusRegistry.getRooms().containsKey(roomId)) {
            CampusRegistry.getRooms().get(roomId).getSensorIds().remove(sensorId);
        }
        CampusRegistry.getSensors().remove(sensorId);
        CampusRegistry.getReadings().remove(sensorId);
        return Response.noContent().build();
    }

    @Path("/{sensorId}/readings")
    public ReadingController getReadingController(@PathParam("sensorId") String sensorId) {
        if (!CampusRegistry.getSensors().containsKey(sensorId)) {
            throw new SensorNotFoundException(
                    "Sensor '" + sensorId + "' is not registered in the campus system.");
        }
        return new ReadingController(sensorId);
    }
}
