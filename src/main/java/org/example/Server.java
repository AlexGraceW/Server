package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int THREAD_POOL_SIZE = 64;
    private final ExecutorService threadPool;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    public void listen(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.execute(() -> handleRequest(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket socket) {
        try (
                InputStream inputStream = socket.getInputStream();
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = Request.parseRequest(inputStream);

            String cleanPath = request.getPath();

            Map<String, Handler> methodHandlers = handlers.getOrDefault(request.getMethod(), Map.of());
            Handler handler = methodHandlers.get(cleanPath);

            if (handler == null) {
                handler = methodHandlers.get("/*");
            }

            if (handler != null) {
                handler.handle(request, out);
            } else {
                sendNotFound(out);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(response.getBytes());
        out.flush();
    }
}
