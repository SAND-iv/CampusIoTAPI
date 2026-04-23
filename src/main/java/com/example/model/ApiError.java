package com.example.model;

/**
 * Standardised JSON error envelope returned by all exception mappers.
 *
 * Pattern taken directly from Tutorial Week 09 (ErrorMessage class).
 * Renamed ApiError and extended with a timestamp field for uniqueness.
 *
 * Example JSON:
 * {
 *   "status"    : 404,
 *   "error"     : "Room 'ENG-F999' not found.",
 *   "timestamp" : 1714000000000,
 *   "docs"      : "https://westminster.ac.uk/campusiot/api/docs"
 * }
 */
public class ApiError {

    private int    status;
    private String error;
    private long   timestamp;
    private String docs;

    public ApiError() {}

    public ApiError(int status, String error) {
        this.status    = status;
        this.error     = error;
        this.timestamp = System.currentTimeMillis();
        this.docs      = "https://westminster.ac.uk/campusiot/api/docs";
    }

    // ---------------------------------------------------------------- getters / setters

    public int    getStatus()              { return status; }
    public void   setStatus(int status)    { this.status = status; }

    public String getError()               { return error; }
    public void   setError(String error)   { this.error = error; }

    public long   getTimestamp()                   { return timestamp; }
    public void   setTimestamp(long timestamp)     { this.timestamp = timestamp; }

    public String getDocs()                { return docs; }
    public void   setDocs(String docs)     { this.docs = docs; }
}
