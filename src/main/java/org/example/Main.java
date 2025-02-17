package org.example;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(64); // Создаем сервер с 64 потоками
        server.start(); // Запускаем сервер
    }
}
