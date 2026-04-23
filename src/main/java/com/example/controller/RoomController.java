package com.example.controller;

import com.example.exception.RoomHasDevicesException;
import com.example.exception.RoomNotFoundException;
import com.example.model.ApiError;
import com.example.model.Room;
import com.example.storage.CampusRegistry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
public class RoomController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> listRooms() {
        return new ArrayList<>(CampusRegistry.getRooms().values());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(422)
                    .entity(new ApiError(422, "Room 'id' field is required."))
                    .build();
        }
        if (CampusRegistry.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ApiError(409,
                            "A room with ID '" + room.getId() + "' already exists."))
                    .build();
        }
        CampusRegistry.getRooms().put(room.getId(), room);
        return Response.status(Response.Status.CREATED)
                .entity(room)
                .header("Location", "/api/v1/rooms/" + room.getId())
                .build();
    }

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room room = CampusRegistry.getRooms().get(roomId);
        if (room == null) {
            throw new RoomNotFoundException(
                    "Room '" + roomId + "' does not exist in the campus registry.");
        }
        return room;
    }

    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeRoom(@PathParam("roomId") String roomId) {
        Room room = CampusRegistry.getRooms().get(roomId);
        if (room == null) {
            throw new RoomNotFoundException(
                    "Room '" + roomId + "' does not exist in the campus registry.");
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomHasDevicesException(
                    "Room '" + roomId + "' still has " + room.getSensorIds().size() +
                    " device(s) installed: " + room.getSensorIds() +
                    ". Decommission all sensors before deleting the room.");
        }
        CampusRegistry.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
