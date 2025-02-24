package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        // Обработчик для всех статических файлов
        server.addHandler("GET", "/", Main::handleStaticFiles);
        server.addHandler("GET", "/*", Main::handleStaticFiles); // Обрабатываем все пути
        // Добавляем API-хендлеры
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            String lastParam = request.getQueryParam("last");
            String responseText;

            if (lastParam == null) {
                responseText = "This is a GET response";
            } else {
                responseText = "Last messages count: " + lastParam;
            }

            sendResponse(responseStream, "200 OK", "text/plain", responseText);
        });


        server.addHandler("POST", "/messages", (request, responseStream) -> {
            String responseText = "This is a POST response";
            sendResponse(responseStream, "200 OK", "text/plain", responseText);
        });

        server.addHandler("POST", "/submit", (request, responseStream) -> {
            String name = request.getPostParam("name"); // "Alice"
            List<String> allMessages = request.getPostParams().get("message"); // ["Hello World", "How are you"]

            StringBuilder responseText = new StringBuilder("Name: " + name + ", Messages: ");
            for (String message : allMessages) {
                responseText.append(message).append(" ");
            }

            // Убираем лишний пробел в конце строки
            String finalResponseText = responseText.toString().trim();

            sendResponse(responseStream, "200 OK", "text/plain", finalResponseText);
        });




        // Запускаем сервер
        server.listen(9999);
    }

    // Метод для обработки статических файлов
    private static void handleStaticFiles(Request request, BufferedOutputStream responseStream) {
        String path = request.getPath();

        // Логируем запрос
        System.out.println("Запрос на файл: " + path);

        // Если путь корневой, заменяем на index.html
        if (path.equals("/")) {
            path = "/index.html";
        }

        // Удаляем начальный "/" для корректного пути
        Path filePath = Path.of("public", path.substring(1));

        // Проверяем, существует ли файл
        if (!Files.exists(filePath)) {
            send404(responseStream);
            return;
        }

        // Определяем MIME-тип
        String mimeType;
        try {
            mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
        } catch (IOException e) {
            mimeType = "application/octet-stream";
        }

        // Отправляем файл клиенту
        try {
            byte[] fileContent = Files.readAllBytes(filePath);
            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + fileContent.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            responseStream.write(fileContent);
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            send500(responseStream);
        }
    }

    // Метод для отправки 404 Not Found
    private static void send404(BufferedOutputStream responseStream) {
        try {
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            responseStream.write(response.getBytes());
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для отправки 500 Internal Server Error
    private static void send500(BufferedOutputStream responseStream) {
        try {
            String response = "HTTP/1.1 500 Internal Server Error\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
            responseStream.write(response.getBytes());
            responseStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Универсальный метод для отправки HTTP-ответов
    private static void sendResponse(BufferedOutputStream responseStream, String status, String contentType, String content) throws IOException {
        responseStream.write((
                "HTTP/1.1 " + status + "\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + content.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        content
        ).getBytes());
        responseStream.flush();
    }
}
