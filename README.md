# Campus IoT Sensor & Room Management API

A RESTful web service built with JAX-RS and Jersey 2.32 for managing rooms and IoT sensors across the University of Westminster campus. All data is stored in-memory using ConcurrentHashMap — no database is used. The server runs on Apache Tomcat 9.

---

## API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery – API metadata and resource links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Register a new room |
| GET | `/api/v1/rooms/{roomId}` | Fetch a single room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors are assigned) |
| GET | `/api/v1/sensors` | List all sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Fetch a single sensor by ID |
| DELETE | `/api/v1/sensors/{sensorId}` | Remove a sensor |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Submit a new reading for a sensor |

---

## How to Build and Run

### Prerequisites
- NetBeans IDE 12 or above
- JDK 8
- Apache Tomcat 9 added to NetBeans via **Tools → Servers → Add Server → Apache Tomcat 9**

### Steps

1. Clone or download this repository
2. Open NetBeans → **File → Open Project** → select the `CampusIoTAPI` folder
3. Right-click the project → **Properties → Run** → make sure the Server is set to **Apache Tomcat 9**
4. Right-click the project → **Clean and Build** → wait for `BUILD SUCCESS` in the Output window
5. Right-click the project → **Run** → Tomcat will start and deploy the application automatically
6. Open your browser or Postman once the Output window confirms Tomcat has started

> ⚠️ **Note:** Going to `http://localhost:8080/CampusIoTAPI/` will return a 404 — this is completely normal. There is no content at the root path. The API starts at:

```
http://localhost:8080/CampusIoTAPI/api/v1
```

---

## Troubleshooting

**404 on `http://localhost:8080/CampusIoTAPI/`**
This is expected behaviour. The application only serves requests under `/api/v1`. Always use `http://localhost:8080/CampusIoTAPI/api/v1` as your base URL.

**404 even on `/api/v1`**
- Make sure you ran **Clean and Build** before **Run**
- Double-check that Tomcat 9 is the selected server, not GlassFish or TomEE
- Review the NetBeans Output window for any errors during deployment

**Port 8080 already in use**
Go to **Tools → Servers → Apache Tomcat 9** and change the HTTP port to something free like 8084, then use `http://localhost:8084/CampusIoTAPI/api/v1`

**BUILD FAILURE**
- Check that JDK 8 is configured under **Tools → Java Platforms**
- Right-click project → **Properties → Libraries → Java Platform** → set to JDK 8

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

### 5. Try to delete a room that still has sensors (expect 409 Conflict)
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

### 8. Submit a reading to an active sensor
```bash
curl -X POST http://localhost:8080/CampusIoTAPI/api/v1/sensors/TEMP-ENG01/readings \
     -H "Content-Type: application/json" \
     -d '{"value":23.1}'
```

### 9. Get reading history for a sensor
```bash
curl -X GET http://localhost:8080/CampusIoTAPI/api/v1/sensors/TEMP-ENG01/readings
```

### 10. Try to register a sensor with a non-existent roomId (expect 422)
```bash
curl -X POST http://localhost:8080/CampusIoTAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"CO2-FAKE","type":"CO2","status":"ACTIVE","currentValue":400,"unit":"ppm","roomId":"GHOST-999"}'
```

### 11. Try to submit a reading to a sensor in MAINTENANCE (expect 403)
```bash
curl -X POST http://localhost:8080/CampusIoTAPI/api/v1/sensors/MOT-CAV01/readings \
     -H "Content-Type: application/json" \
     -d '{"value":1}'
```

### 12. Remove a sensor then delete the now-empty room
```bash
curl -X DELETE http://localhost:8080/CampusIoTAPI/api/v1/sensors/CO2-SCI01
curl -X DELETE http://localhost:8080/CampusIoTAPI/api/v1/rooms/SCI-B104
```

---

## Report: Answers to Coursework Questions

### Part 1.1 – JAX-RS Resource Lifecycle

By default, JAX-RS creates a brand new instance of each resource class for every incoming HTTP request. This is the per-request lifecycle — the constructor runs fresh, all instance fields are initialised from scratch, and the object is thrown away once the response is sent.

This has an important consequence for shared data. Because every request gets its own resource instance, any data stored as an instance field would disappear after that request finishes. To keep data alive across multiple requests it has to live somewhere outside the resource class entirely.

In this project I used a class called `CampusRegistry` to hold all the application data. Its maps are declared as `static final ConcurrentHashMap` fields, so they are created once when the class loads and stay alive for the entire lifetime of the running application. I chose `ConcurrentHashMap` over a plain `HashMap` because the server handles multiple HTTP requests on different threads at the same time. A regular `HashMap` under concurrent reads and writes would cause data corruption or throw a `ConcurrentModificationException`. Using `ConcurrentHashMap` prevents those race conditions without needing to manually synchronise every method.

---

### Part 1.2 – HATEOAS

HATEOAS stands for Hypermedia as the Engine of Application State. The idea is that instead of making clients rely on separate documentation or hard-coded URLs, the API embeds navigational links directly in its responses. A client only needs to know one entry point — the root URL — and can discover everything else by following the links returned in each response.

In this API, `GET /api/v1` returns a `_links` map that points to every major resource collection. If I were to change a URL in a future version, clients following those links would adapt automatically rather than breaking. This makes the API much easier to integrate with and far less fragile than one where clients have to maintain a separate list of hard-coded paths that need updating every time the API evolves.

---

### Part 2.1 – Full Objects vs IDs in List Responses

I chose to return complete `Room` objects from `GET /rooms` rather than just a list of IDs. The downside is a slightly larger response, but the upside is that the client gets everything it needs in a single request. If I only returned IDs, the client would have to send a separate `GET /rooms/{id}` request for every single room just to get basic details like the name and capacity. For a dashboard showing a full list of rooms, that could easily mean dozens of extra round trips. Returning full objects once is far more practical, and the payload size for a campus-scale system is not large enough to be a concern.

---

### Part 2.2 – Idempotency of DELETE

The DELETE operation is idempotent at the resource-state level. Whether you call it once or five times, the end result is the same — the room no longer exists. However the HTTP response code is different between calls: the first successful delete returns `204 No Content`, and any repeat call returns `404 Not Found` because the room is already gone. I deliberately return `404` on the second call rather than another `204` because it gives the client more accurate feedback — it signals clearly that the resource was not found rather than pretending the delete succeeded again. The server state is identical either way, so the core definition of idempotency is still satisfied.

---

### Part 3.1 – @Consumes and Content-Type Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells Jersey that the POST endpoint will only accept requests where the `Content-Type` header is `application/json`. If a client sends the request with `text/plain` or `application/xml` instead, Jersey rejects it automatically with HTTP `415 Unsupported Media Type` before the method body even runs. This keeps the validation at the framework level so I do not have to manually check the content type inside every method, and it prevents the JSON deserialiser from receiving data it cannot handle.

---

### Part 3.2 – @QueryParam vs Path Parameter for Filtering

I implemented the type filter using `@QueryParam` because query parameters are the standard HTTP way to filter or search a collection. The resource `/sensors` means "all sensors" — the query string `?type=CO2` just narrows that view without changing what resource you are addressing.

If I had used a path design like `/sensors/type/CO2` instead, it would imply that `type/CO2` is a separate distinct resource rather than a filtered view of the sensors collection. It would also make combining multiple filters very awkward — with query parameters you can naturally write `?type=CO2&status=ACTIVE`, but there is no clean way to express that with path segments. Query parameters are also much easier to make optional, which is exactly what I needed here since the filter should work without being required.

---

### Part 4.1 – Sub-Resource Locator Pattern

The sub-resource locator is a method in `SensorController` that carries a `@Path` annotation but no HTTP method annotation like `@GET` or `@POST`. Instead of handling the request itself, it returns an instance of `ReadingController` which then handles the actual GET or POST on `/sensors/{sensorId}/readings`.

The main benefit is separation of concerns. `SensorController` only has to deal with sensor management, and `ReadingController` only has to deal with reading history. Neither class leaks into the other's responsibility. Each class stays focused and small, which also makes them easier to test independently. If I had put every nested path into one giant controller, it would quickly become unmanageable as the API grows. The sub-resource locator pattern scales much more cleanly because each class only grows with the features it is responsible for.

---

### Part 5.2 – Why 422 Over 404 for Missing Room Reference

When a client POSTs a new sensor with a `roomId` that does not exist, returning `404` would be misleading. The URL `/api/v1/sensors` is perfectly valid — nothing is wrong with where the request was sent. The problem is inside the request body: the `roomId` field references a room that does not exist in the system. HTTP `422 Unprocessable Entity` is the right status here because it tells the client that the server understood the request structure but could not process it because of a bad reference in the payload. A `404` would make the client think they got the URL wrong, which is incorrect and unhelpful. A `422` points them directly at the real issue — fix the payload, not the URL.

---

### Part 5.4 – Security Risks of Exposing Stack Traces

Sending raw Java stack traces back to API clients is a serious security risk. They reveal the internal package and class structure of the application, which helps an attacker understand how the codebase is laid out before trying to exploit it. They also expose exactly which third-party libraries and versions are being used, making it easy to search for known vulnerabilities in those dependencies. Exception messages frequently contain file paths, configuration details, or fragments of internal logic that give away sensitive implementation information. On top of that, seeing the execution flow in a stack trace makes it easier to craft inputs that deliberately trigger specific error paths.

The `GlobalFaultMapper` in this project handles this by catching all unhandled `Throwable` instances, logging the full detail server-side only, and returning a plain generic `500` JSON response to the client. Nothing about the internal state of the application ever leaves the server.

---

### Part 5.5 – JAX-RS Filters vs Manual Logging

Using a JAX-RS filter for logging means the logic lives in exactly one place and covers every single endpoint automatically. If I had put log statements inside each controller method instead, I would have to remember to add them every time I created a new endpoint. If I ever wanted to change the log format, I would have to edit every controller class individually. The filter approach means any request or response passing through the API gets logged consistently, with no risk of missing an endpoint and no duplication of code across the project.

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
    │   │   └── CampusRegistry.java        ← shared in-memory data store
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
- **Framework**: JAX-RS with Jersey 2.32
- **JSON**: Jackson (via `jersey-media-json-jackson`)
- **Server**: Apache Tomcat 9
- **Build**: Maven
- **Storage**: `ConcurrentHashMap` + `Collections.synchronizedList` — no database
