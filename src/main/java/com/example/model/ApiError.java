package com.example.model;

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

    public int    getStatus()              { return status; }
    public void   setStatus(int status)    { this.status = status; }

    public String getError()               { return error; }
    public void   setError(String error)   { this.error = error; }

    public long   getTimestamp()                   { return timestamp; }
    public void   setTimestamp(long timestamp)     { this.timestamp = timestamp; }

    public String getDocs()                { return docs; }
    public void   setDocs(String docs)     { this.docs = docs; }
}
