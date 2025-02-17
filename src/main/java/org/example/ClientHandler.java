package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final List<String> VALID_PATHS = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
            "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js"
    );

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length != 3) {
                return;
            }

            String path = parts[1];
            if (!VALID_PATHS.contains(path)) {
                Response.send(out, "404 Not Found", "text/plain", new byte[0]);
                return;
            }

            Path filePath = Path.of(".", "public", path);
            String mimeType = Files.probeContentType(filePath);

            if ("/classic.html".equals(path)) {
                String template = Files.readString(filePath);
                byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
                Response.send(out, "200 OK", mimeType, content);
            } else {
                byte[] content = Files.readAllBytes(filePath);
                Response.send(out, "200 OK", mimeType, content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
