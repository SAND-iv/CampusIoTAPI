package com.example.model;

/**
 * Core data model for an IoT sensor deployed in a campus room.
 *
 * Fields required by the coursework specification:
 *   id           – unique identifier  e.g. "TEMP-001"
 *   type         – sensor category: "Temperature", "Humidity", "CO2", "Motion", "Noise"
 *   status       – operational state: "ACTIVE", "MAINTENANCE", or "OFFLINE"
 *   currentValue – most recent measurement recorded by the device
 *   roomId       – foreign key linking to the Room where this sensor is installed
 *
 * Extra field added for uniqueness:
 *   unit         – measurement unit string e.g. "°C", "%RH", "ppm", "boolean", "dB"
 */
public class Sensor {

    private String id;
    private String type;
    private String status;
    private double currentValue;
    private String unit;
    private String roomId;

    // ---------------------------------------------------------------- constructors

    public Sensor() {}

    public Sensor(String id, String type, String status,
                  double currentValue, String unit, String roomId) {
        this.id           = id;
        this.type         = type;
        this.status       = status;
        this.currentValue = currentValue;
        this.unit         = unit;
        this.roomId       = roomId;
    }

    // ---------------------------------------------------------------- getters / setters

    public String getId()                    { return id; }
    public void   setId(String id)           { this.id = id; }

    public String getType()                  { return type; }
    public void   setType(String type)       { this.type = type; }

    public String getStatus()                { return status; }
    public void   setStatus(String status)   { this.status = status; }

    public double getCurrentValue()                    { return currentValue; }
    public void   setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getUnit()                  { return unit; }
    public void   setUnit(String unit)       { this.unit = unit; }

    public String getRoomId()                { return roomId; }
    public void   setRoomId(String roomId)   { this.roomId = roomId; }
}
