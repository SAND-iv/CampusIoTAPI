package com.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Core data model for a campus room.
 *
 * Fields required by the coursework specification:
 *   id         – unique identifier  e.g. "ENG-F101"
 *   name       – human-readable label
 *   capacity   – maximum permitted occupancy
 *   sensorIds  – IDs of IoT devices deployed in this room
 *
 * Extra field added for uniqueness:
 *   building   – the campus building this room belongs to
 */
public class Room {

    private String       id;
    private String       name;
    private int          capacity;
    private String       building;
    private List<String> sensorIds = new ArrayList<>();

    // ---------------------------------------------------------------- constructors

    public Room() {}

    public Room(String id, String name, int capacity, String building) {
        this.id       = id;
        this.name     = name;
        this.capacity = capacity;
        this.building = building;
    }

    // ---------------------------------------------------------------- getters / setters

    public String getId()                    { return id; }
    public void   setId(String id)           { this.id = id; }

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }

    public int    getCapacity()              { return capacity; }
    public void   setCapacity(int capacity)  { this.capacity = capacity; }

    public String getBuilding()              { return building; }
    public void   setBuilding(String b)      { this.building = b; }

    public List<String> getSensorIds()       { return sensorIds; }
    public void setSensorIds(List<String> s) { this.sensorIds = s; }
}
