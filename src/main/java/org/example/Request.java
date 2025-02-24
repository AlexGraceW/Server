package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;

    public Request(String method, String path, Map<String, String> queryParams, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
    }

    public static Request parseRequest(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isBlank()) {
            throw new IOException("Invalid request format: empty request line");
        }

        // Разбиваем строку запроса
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request format: incorrect request line structure");
        }

        String method = parts[0];
        String fullPath = parts[1]; // Может содержать Query-параметры
        String version = parts[2];

        if (!version.equals("HTTP/1.1") && !version.equals("HTTP/1.0")) {
            throw new IOException("Unsupported HTTP version: " + version);
        }

        // Разбираем путь и Query String
        String path;
        Map<String, String> queryParams = new HashMap<>();
        int queryStart = fullPath.indexOf('?');
        if (queryStart != -1) {
            path = fullPath.substring(0, queryStart);
            String queryString = fullPath.substring(queryStart + 1);
            queryParams = parseQueryParams(queryString);
        } else {
            path = fullPath;
        }

        // Читаем заголовки
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isBlank()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        return new Request(method, path, queryParams, headers);
    }

    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ПАРСИНГА QUERY STRING
    private static Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                queryParams.put(keyValue[0], keyValue[1]);
            }
        }
        return queryParams;
    }

    // Методы доступа
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
