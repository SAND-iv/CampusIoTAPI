# Campus IoT Sensor & Room Management API

A JAX-RS RESTful web service for managing rooms and IoT sensors across the University of Westminster campus. Built with Jersey 2.32 on Apache Tomcat 9, using ConcurrentHashMap-based in-memory storage.

---

## API Endpoint Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery – API metadata and resource links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Register a new room |
| GET | `/api/v1/rooms/{roomId}` | Fetch a single room |
| DELETE | `/api/v1/rooms/{roomId}` | Decommission a room (blocked if sensors present) |
| GET | `/api/v1/sensors` | List all sensors (optional `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Fetch a single sensor |
| DELETE | `/api/v1/sensors/{sensorId}` | Remove a sensor |
| GET | `/api/v1/sensors/{sensorId}/readings` | Retrieve reading history |
| POST | `/api/v1/sensors/{sensorId}/readings` | Submit a new reading |

---

## How to Build and Run

### Prerequisites
- NetBeans IDE 12+
- JDK 8
- Apache Tomcat 9 (added to NetBeans via Tools → Servers → Add Server → Apache Tomcat or TomEE)

### Steps
1. Extract `CampusIoTAPI.zip` anywhere on your machine
2. Open NetBeans → **File → Open Project** → select the `CampusIoTAPI` folder
3. Right-click the project → **Properties → Run** → confirm Server is set to Apache Tomcat 9
4. Right-click the project → **Clean and Build** — wait for `BUILD SUCCESS`
5. Right-click the project → **Run** — Tomcat starts and deploys the WAR automatically
6. Base URL: `http://localhost:8080/CampusIoTAPI/api/v1`

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl -X GET http://localhost:8080/CampusIoTAPI/api/v1
```

### 2. List all rooms
```bash
curl -X GET http://localhost:8080/CampusIoTAPI/api/v1/rooms
```

### 3. Create a new room
```bash
curl -X POST http://localhost:8080/CampusIoTAPI/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"MED-101","name":"Media Suite 101","capacity":20,"building":"Media Block"}'
```

### 4. Get a single room
```bash
curl -X GET http://localhost:8080/CampusIoTAPI/api/v1/rooms/ENG-F101
```

### 5. Attempt to delete a room with sensors (expect 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/CampusIoTAPI/api/v1/rooms/ENG-F101
```

### 6. List only Temperature sensors
```bash
curl -X GET "http://localhost:8080/CampusIoTAPI/api/v1/sensors?type=Temperature"
```

### 7. Register a new sensor
```bash
curl -X POST http://localhost:8080/CampusIoTAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"HUM-LRC01","type":"Humidity","status":"ACTIVE","currentValue":48.0,"unit":"%RH","roomId":"LRC-201"}'
```

### 8. Post a reading to an ACTIVE sensor
```bash
curl -X POST http://localhost:8080/CampusIoTAPI/api/v1/sensors/TEMP-ENG01/readings \
     -H "Content-Type: application/json" \
     -d '{"value":23.1,"recordedBy":"BMS-AutoAgent"}'
```

### 9. Get reading history for a sensor
```bash
curl -X GET http://localhost:8080/CampusIoTAPI/api/v1/sensors/TEMP-ENG01/readings
```

### 10. Register a sensor with a non-existent roomId (expect 422)
```bash
curl -X POST http://localhost:8080/CampusIoTAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"CO2-FAKE","type":"CO2","status":"ACTIVE","currentValue":400,"unit":"ppm","roomId":"GHOST-999"}'
```

### 11. Post a reading to a MAINTENANCE sensor (expect 403)
```bash
curl -X POST http://localhost:8080/CampusIoTAPI/api/v1/sensors/MOT-CAV01/readings \
     -H "Content-Type: application/json" \
     -d '{"value":1,"recordedBy":"TechnicianApp"}'
```

### 12. Delete a sensor then delete its now-empty room
```bash
curl -X DELETE http://localhost:8080/CampusIoTAPI/api/v1/sensors/CO2-SCI01
curl -X DELETE http://localhost:8080/CampusIoTAPI/api/v1/rooms/SCI-B104
```

---

## Report: Answers to Coursework Questions

### Part 1.1 – JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance** of each resource class for every incoming HTTP request. This is known as the per-request lifecycle: the constructor is called afresh, all instance fields are initialised from scratch, and the object is discarded once the response is sent.

This design has a direct consequence for state management. Because each request receives its own controller instance, any data stored as an instance field would be lost immediately after the request ends — making it impossible to share state between calls. To preserve data across requests, all mutable shared state must live outside the controller lifecycle entirely.

In this implementation, `CampusRegistry` acts as the application-scoped data store. Its collections are declared as `static final ConcurrentHashMap` fields, meaning they are created once when the class is first loaded and remain alive for the entire lifetime of the Tomcat application context. `ConcurrentHashMap` is chosen over a plain `HashMap` because Tomcat dispatches concurrent HTTP requests on separate threads. A regular `HashMap` subject to simultaneous reads and writes from multiple threads would produce data corruption or throw `ConcurrentModificationException`. The `ConcurrentHashMap` design eliminates this race condition without requiring explicit synchronisation in every controller method.

---

### Part 1.2 – HATEOAS

HATEOAS (Hypermedia as the Engine of Application State) is the principle of embedding navigational links directly inside API responses so that clients can discover all available operations starting from a single well-known root URL, without relying on hard-coded paths or external documentation.

The discovery endpoint at `GET /api/v1` returns a `_links` map containing the URLs of every primary resource collection. A client can start at the root and navigate to rooms, sensors, and readings from there. If the URL structure changes in a future version, clients that follow these links adapt automatically rather than breaking silently. This decouples the client from specific URL patterns and dramatically reduces the integration burden for developers building against the API. Without HATEOAS, every client must read separate documentation, manually hard-code paths, and update those paths every time the API evolves — a fragile and maintenance-heavy approach.

---

### Part 2.1 – Full Objects vs IDs in List Responses

This implementation returns complete `Room` objects in `GET /rooms`. The trade-off is a larger response payload per call, but it eliminates the N+1 request problem entirely. If only IDs were returned, the client would need to fire a separate `GET /rooms/{id}` call for each room to retrieve its name, capacity, and sensor list. For a campus facilities dashboard rendering a table of all rooms simultaneously, that would mean dozens of sequential network requests. Returning full objects in a single call is far more efficient in practice, and for a campus-scale dataset the payload size is well within acceptable limits.

---

### Part 2.2 – Idempotency of DELETE

The DELETE operation in this implementation is idempotent at the **resource-state level**: after both the first and any subsequent calls, the server is in the same state — the room is absent. However, the HTTP response codes differ: the first call returns `204 No Content`, while subsequent calls return `404 Not Found` because the room no longer exists. This is by design — returning `404` on the second call gives the client accurate feedback that the resource was not found, rather than silently pretending the operation succeeded. Some implementations return `204` for all calls (true response idempotency), but the `404` approach is more informative and is widely used in production APIs.

---

### Part 3.1 – @Consumes and Content-Type Mismatches

`@Consumes(MediaType.APPLICATION_JSON)` declares that the POST method only accepts requests carrying a `Content-Type: application/json` header. If a client sends `text/plain` or `application/xml` instead, Jersey intercepts the request at the routing layer, compares the declared media type against the request header, and rejects the call with HTTP `415 Unsupported Media Type` — before the method body is even executed. This protects the Jackson deserialiser from receiving data in a format it cannot parse, and keeps media-type validation at the framework boundary rather than buried inside business logic.

---

### Part 3.2 – @QueryParam vs Path Parameter for Filtering

`GET /api/v1/sensors?type=CO2` uses `@QueryParam` because query parameters are the standard HTTP mechanism for filtering, searching, or sorting a collection. The base resource `/sensors` always means "all sensors"; adding `?type=CO2` narrows the result set without changing the resource's identity.

An alternative path-based design such as `GET /api/v1/sensors/type/CO2` embeds the filter value in the URI, implying it identifies a unique sub-resource rather than a filtered view of an existing one. This creates several problems: it is impossible to combine multiple filters naturally (e.g. `?type=CO2&status=ACTIVE`); REST caching infrastructure treats the filtered path as an entirely separate resource from the base collection, breaking cache invalidation; and the URL structure becomes harder to document and understand. Query parameters communicate "optional modifier on a collection", which is precisely what filtering is.

---

### Part 4.1 – Sub-Resource Locator Pattern

The sub-resource locator in `SensorController.getReadingController()` returns a `ReadingController` instance without carrying an HTTP method annotation. JAX-RS does not invoke this method directly; instead, it uses the returned object to resolve the actual `GET` or `POST` on `/sensors/{sensorId}/readings`.

This approach delivers several architectural benefits. First, it enforces **separation of concerns**: `SensorController` owns sensor CRUD, and `ReadingController` owns reading history — neither class contains the other's logic. Second, each class remains small, focused, and independently testable. Third, `ReadingController` receives the `sensorId` as a constructor argument, giving it the correct sensor context without resorting to global state or thread-locals. In a large-scale API with many nested resource types, defining every path variant in a single controller would produce an unmaintainable monolith. The sub-resource locator pattern keeps complexity proportional to the feature set.

---

### Part 5.2 – Why 422 Over 404 for Missing Room Reference

When a sensor POST body references a `roomId` that does not exist, returning `404 Not Found` would be misleading because there is nothing wrong with the request URI — `/api/v1/sensors` is perfectly valid and reachable. The problem lies inside the request body: a foreign-key reference points to a non-existent entity. HTTP `422 Unprocessable Entity` precisely describes this situation: the server understood the content type and the syntactic structure of the request but was unable to process the semantic content because it contained an invalid reference. A `404` would tell the client they navigated to the wrong URL, which is incorrect. A `422` tells them to fix their payload — a far more actionable error for the developer.

---

### Part 5.4 – Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers creates several concrete security risks. Stack traces reveal the internal package structure and class names of the application, helping an attacker map the codebase before attempting exploitation. They expose which third-party libraries and framework versions are in use, allowing targeted attacks against known CVEs in those dependencies. Exception messages often contain file paths, SQL query fragments, or configuration values, all of which provide reconnaissance data. They also reveal the server-side execution flow, making it easier to craft inputs that trigger specific vulnerable code paths.

The `GlobalFaultMapper` in this implementation intercepts all unhandled `Throwable` instances, logs the full stack trace server-side only (visible in the Tomcat output window in NetBeans), and returns a generic `500` JSON body to the client. Internal details never leave the server boundary.

---

### Part 5.5 – JAX-RS Filters vs Manual Logging

A JAX-RS filter (`TrafficMonitor`) handles logging as a **cross-cutting concern** — a behaviour that applies uniformly to all operations regardless of business logic. If logging were added manually by inserting `Logger.info()` calls into every controller method, every new endpoint would require the developer to remember to add logging, and any change to the log format would require editing every controller class across the entire project. A single filter intercepts all requests and responses automatically, providing complete and consistent coverage with zero per-method overhead and no risk of omission. This is the same principle that motivates middleware pipelines in enterprise frameworks and aspect-oriented programming.

---

## Project Structure

```
CampusIoTAPI/
├── pom.xml
├── nb-configuration.xml
└── src/main/
    ├── java/com/example/
    │   ├── CampusIoTApp.java              ← @ApplicationPath("/api/v1")
    │   ├── model/
    │   │   ├── Room.java
    │   │   ├── Sensor.java
    │   │   ├── SensorReading.java
    │   │   └── ApiError.java
    │   ├── storage/
    │   │   └── CampusRegistry.java        ← Singleton ConcurrentHashMap store
    │   ├── controller/
    │   │   ├── RootController.java        ← GET /api/v1 (discovery)
    │   │   ├── RoomController.java        ← /rooms CRUD
    │   │   ├── SensorController.java      ← /sensors CRUD + sub-resource locator
    │   │   └── ReadingController.java     ← /sensors/{id}/readings
    │   ├── exception/
    │   │   ├── RoomNotFoundException.java       → 404
    │   │   ├── RoomNotFoundMapper.java
    │   │   ├── SensorNotFoundException.java     → 404
    │   │   ├── SensorNotFoundMapper.java
    │   │   ├── RoomHasDevicesException.java     → 409
    │   │   ├── RoomHasDevicesMapper.java
    │   │   ├── DeviceRoomMissingException.java  → 422
    │   │   ├── DeviceRoomMissingMapper.java
    │   │   ├── DeviceOfflineException.java      → 403
    │   │   ├── DeviceOfflineMapper.java
    │   │   └── GlobalFaultMapper.java           → 500 catch-all
    │   └── logging/
    │       └── TrafficMonitor.java              ← request + response logging filter
    └── webapp/WEB-INF/
        └── web.xml
```

## Technology Stack

- **Language**: Java 8
- **Framework**: JAX-RS via Jersey 2.32 (as used in Tutorial Weeks 07–09)
- **JSON**: Jackson (via `jersey-media-json-jackson`)
- **Server**: Apache Tomcat 9
- **Build**: Maven
- **Storage**: `ConcurrentHashMap` + `Collections.synchronizedList` — no database
