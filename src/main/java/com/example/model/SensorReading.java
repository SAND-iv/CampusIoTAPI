package com.example.model;

/**
 * Represents a single historical measurement captured by a sensor.
 *
 * Fields required by the coursework specification:
 *   id          – unique event ID (UUID assigned automatically if omitted)
 *   timestamp   – epoch milliseconds when the reading was captured
 *   value       – the actual numeric measurement
 *
 * Extra field added for uniqueness:
 *   recordedBy  – identifier of the agent that submitted this reading
 *                 e.g. "BMS-AutoAgent", "TechnicianApp", "ScheduledJob"
 */
public class SensorReading {

    private String id;
    private long   timestamp;
    private double value;
    private String recordedBy;

    // ---------------------------------------------------------------- constructors

    public SensorReading() {}

    public SensorReading(String id, long timestamp, double value, String recordedBy) {
        this.id         = id;
        this.timestamp  = timestamp;
        this.value      = value;
        this.recordedBy = recordedBy;
    }

    // ---------------------------------------------------------------- getters / setters

    public String getId()                  { return id; }
    public void   setId(String id)         { this.id = id; }

    public long   getTimestamp()                   { return timestamp; }
    public void   setTimestamp(long timestamp)     { this.timestamp = timestamp; }

    public double getValue()               { return value; }
    public void   setValue(double value)   { this.value = value; }

    public String getRecordedBy()                  { return recordedBy; }
    public void   setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
