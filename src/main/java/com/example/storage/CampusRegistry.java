package com.example.storage;

import com.example.model.Room;
import com.example.model.Sensor;
import com.example.model.SensorReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central in-memory data registry for the Campus IoT API.
 *
 * Why static ConcurrentHashMaps? (Part 1.1 lifecycle question)
 *
 * JAX-RS creates a fresh controller instance for every HTTP request, so
 * instance-level fields would be discarded after each call – useless for
 * persistence.  By making the collections static we ensure a single shared
 * instance lives for the entire life of the Tomcat application context.
 * ConcurrentHashMap is chosen over plain HashMap because Tomcat handles
 * concurrent HTTP requests on multiple threads; a regular HashMap under
 * concurrent write access would cause data corruption or ConcurrentModificationException.
 * The reading lists are additionally wrapped with Collections.synchronizedList
 * for the same thread-safety reason.
 *
 * Seed data uses Westminster Cavendish campus room identifiers for uniqueness.
 */
public class CampusRegistry {

    // ---------------------------------------------------- collection stores
    private static final Map<String, Room>   rooms   = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // ---------------------------------------------------- seed data
    static {

        // ---- Rooms (Westminster Cavendish campus themed) ----
        Room r1 = new Room("ENG-F101", "Engineering Lab Floor 1",    35, "Engineering Block");
        Room r2 = new Room("CAV-G02",  "Cavendish Ground Seminar",   60, "Cavendish Building");
        Room r3 = new Room("LRC-201",  "Learning Resource Centre 2", 80, "LRC Tower");
        Room r4 = new Room("ATR-301",  "Atrium Event Space",        150, "Main Atrium");
        Room r5 = new Room("SCI-B104", "Science Lab B",              28, "Science Wing");

        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);
        rooms.put(r4.getId(), r4);
        rooms.put(r5.getId(), r5);

        // ---- Sensors (variety of types + units) ----
        Sensor s1 = new Sensor("TEMP-ENG01",  "Temperature", "ACTIVE",      21.3, "°C",      "ENG-F101");
        Sensor s2 = new Sensor("HUM-ENG01",   "Humidity",    "ACTIVE",      55.0, "%RH",     "ENG-F101");
        Sensor s3 = new Sensor("CO2-CAV01",   "CO2",         "ACTIVE",     430.0, "ppm",     "CAV-G02");
        Sensor s4 = new Sensor("MOT-CAV01",   "Motion",      "MAINTENANCE", 0.0,  "boolean", "CAV-G02");
        Sensor s5 = new Sensor("TEMP-LRC01",  "Temperature", "ACTIVE",      19.8, "°C",      "LRC-201");
        Sensor s6 = new Sensor("NOI-ATR01",   "Noise",       "ACTIVE",      62.5, "dB",      "ATR-301");
        Sensor s7 = new Sensor("CO2-SCI01",   "CO2",         "OFFLINE",    500.0, "ppm",     "SCI-B104");

        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);
        sensors.put(s5.getId(), s5);
        sensors.put(s6.getId(), s6);
        sensors.put(s7.getId(), s7);

        // ---- Link sensors → rooms ----
        r1.getSensorIds().add("TEMP-ENG01");
        r1.getSensorIds().add("HUM-ENG01");
        r2.getSensorIds().add("CO2-CAV01");
        r2.getSensorIds().add("MOT-CAV01");
        r3.getSensorIds().add("TEMP-LRC01");
        r4.getSensorIds().add("NOI-ATR01");
        r5.getSensorIds().add("CO2-SCI01");
        // ATR-301 intentionally has NOI-ATR01 — use SCI-B104 → CO2-SCI01 for demo DELETE chain

        // ---- Initialise empty reading history for every seeded sensor ----
        for (String sid : sensors.keySet()) {
            readings.put(sid, Collections.synchronizedList(new ArrayList<>()));
        }
    }

    // ---------------------------------------------------- public accessors
    public static Map<String, Room>   getRooms()    { return rooms; }
    public static Map<String, Sensor> getSensors()  { return sensors; }
    public static Map<String, List<SensorReading>> getReadings() { return readings; }
}
