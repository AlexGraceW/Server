package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, List<String>> queryParams;
    private final Map<String, String> headers;
    private final Map<String, List<String>> postParams;

    public Request(String method, String path, Map<String, List<String>> queryParams,
                   Map<String, String> headers, Map<String, List<String>> postParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.postParams = postParams;
    }

    public static Request parseRequest(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isBlank()) {
            throw new IOException("Invalid request format: empty request line");
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IOException("Invalid request format: incorrect request line structure");
        }

        String method = parts[0];
        String fullPath = parts[1];
        String version = parts[2];

        if (!version.equals("HTTP/1.1") && !version.equals("HTTP/1.0")) {
            throw new IOException("Unsupported HTTP version: " + version);
        }

        // Разбираем путь и Query String
        String path;
        Map<String, List<String>> queryParams = new HashMap<>();
        int queryStart = fullPath.indexOf('?');
        if (queryStart != -1) {
            path = fullPath.substring(0, queryStart);
            queryParams = parseQueryParams(fullPath.substring(queryStart + 1));
        } else {
            path = fullPath;
        }

        // Читаем заголовки
        Map<String, String> headers = new HashMap<>();
        String line;
        int contentLength = 0;
        while ((line = reader.readLine()) != null && !line.isBlank()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }

            if (headerParts[0].equalsIgnoreCase("Content-Length")) {
                try {
                    contentLength = Integer.parseInt(headerParts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Обрабатываем тело запроса (POST)
        Map<String, List<String>> postParams = new HashMap<>();
        if ("POST".equalsIgnoreCase(method) && headers.getOrDefault("Content-Type", "").equals("application/x-www-form-urlencoded")) {
            char[] bodyChars = new char[contentLength];
            if (reader.read(bodyChars) != -1) {
                String requestBody = new String(bodyChars);
                postParams = parseQueryParams(requestBody);
            }
        }

        return new Request(method, path, queryParams, headers, postParams);
    }

    private static Map<String, List<String>> parseQueryParams(String queryString) {
        Map<String, List<String>> queryParams = new HashMap<>();
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                queryParams.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
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
        List<String> values = queryParams.get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    // Новые методы для работы с POST-данными
    public String getPostParam(String name) {
        List<String> values = postParams.get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    public Map<String, List<String>> getPostParams() {
        return postParams;
    }
}
