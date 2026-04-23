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

/**
 * Part 2 – Room Management.
 *
 * GET    /api/v1/rooms            – list all rooms
 * POST   /api/v1/rooms            – register a new room (201 Created)
 * GET    /api/v1/rooms/{roomId}   – fetch a single room by ID
 * DELETE /api/v1/rooms/{roomId}   – decommission a room (blocked if sensors exist)
 *
 * ---
 * Report question Part 2.1 – Full objects vs IDs in list responses:
 * Returning complete Room objects means a single GET /rooms call gives the
 * client everything it needs to render a full dashboard.  Returning only IDs
 * would force N additional requests (one per room ID) – the classic N+1
 * problem.  For a facilities-management UI that must display names, capacities
 * and sensor counts simultaneously, full objects are more efficient despite
 * the slightly larger payload.
 *
 * Report question Part 2.2 – Idempotency of DELETE:
 * The first DELETE /rooms/{id} removes the room and returns 204 No Content.
 * A second identical request returns 404 because the room is gone.  The
 * end state after both calls is identical (room absent), so the operation is
 * idempotent at the resource-state level.  However, the HTTP response code
 * differs between the first and second call (204 vs 404).  This implementation
 * returns 404 on the second call to give accurate feedback to the client.
 */
@Path("/rooms")
public class RoomController {

    // ---------------------------------------------------------------- GET ALL
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> listRooms() {
        return new ArrayList<>(CampusRegistry.getRooms().values());
    }

    // ---------------------------------------------------------------- POST
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
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // ---------------------------------------------------------------- GET BY ID
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

    // ---------------------------------------------------------------- DELETE
    /**
     * Business rule: a room cannot be decommissioned while IoT devices are
     * still assigned to it.  Attempting to do so raises RoomHasDevicesException
     * which the mapper converts to HTTP 409 Conflict.
     */
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
        return Response.noContent().build(); // 204
    }
}
