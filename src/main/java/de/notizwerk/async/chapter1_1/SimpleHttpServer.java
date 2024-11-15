package de.notizwerk.async.chapter1_1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            // Für jeden Client einen neuen Thread erstellen
            System.out.println("Creating new Client Thread");
            new Thread(() -> handleRequest(clientSocket)).start();

        }
    }

    private static void handleRequest(Socket clientSocket) {
        try {
            // Simulation einer zeitintensiven Operation (z.B. Datenbankzugriff)
            Thread.sleep(100);
            // Request Verarbeitung...
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}