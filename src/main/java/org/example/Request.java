package org.example;

public class Request {
    private final String method;
    private final String path;
    private final String httpVersion;

    public Request(String requestLine) {
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid request format");
        }
        this.method = parts[0];
        this.path = parts[1];
        this.httpVersion = parts[2];
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}
