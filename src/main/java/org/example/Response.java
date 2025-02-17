package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Response {
    public static void send(BufferedOutputStream out, String status, String contentType, byte[] content) throws IOException {
        out.write(("HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n").getBytes());
        out.write(content);
        out.flush();
    }
}
