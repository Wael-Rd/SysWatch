package com.portmonitor.service;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

public class ProxyHandler implements Runnable {
    private final Socket clientSocket;
    private final AtomicLong bytesSent;
    private final AtomicLong bytesReceived;
    private static final int BUFFER_SIZE = 8192;

    public ProxyHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.bytesSent = new AtomicLong(0);
        this.bytesReceived = new AtomicLong(0);
    }

    @Override
    public void run() {
        try {
            // Read the initial request line
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String initialLine = reader.readLine();
            
            if (initialLine != null) {
                String[] parts = initialLine.split(" ");
                if (parts.length >= 3) {
                    String method = parts[0];
                    String url = parts[1];
                    
                    // Parse host and port from URL or Host header
                    String host = "";
                    int port = 80;
                    
                    // Read headers
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        if (line.toLowerCase().startsWith("host: ")) {
                            String[] hostParts = line.substring(6).split(":");
                            host = hostParts[0];
                            if (hostParts.length > 1) {
                                port = Integer.parseInt(hostParts[1]);
                            }
                            break;
                        }
                    }
                    
                    if (!host.isEmpty()) {
                        handleRequest(host, port);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(String host, int port) throws IOException {
        // Connect to target server
        Socket serverSocket = new Socket(host, port);
        
        // Create streams for both client and server
        InputStream clientIn = clientSocket.getInputStream();
        OutputStream clientOut = clientSocket.getOutputStream();
        InputStream serverIn = serverSocket.getInputStream();
        OutputStream serverOut = serverSocket.getOutputStream();
        
        // Create threads to handle bidirectional traffic
        Thread clientToServer = new Thread(() -> {
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = clientIn.read(buffer)) != -1) {
                    serverOut.write(buffer, 0, bytesRead);
                    serverOut.flush();
                    bytesSent.addAndGet(bytesRead);
                }
            } catch (IOException e) {
                // Connection closed
            }
        });
        
        Thread serverToClient = new Thread(() -> {
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = serverIn.read(buffer)) != -1) {
                    clientOut.write(buffer, 0, bytesRead);
                    clientOut.flush();
                    bytesReceived.addAndGet(bytesRead);
                }
            } catch (IOException e) {
                // Connection closed
            }
        });
        
        // Start the traffic forwarding threads
        clientToServer.start();
        serverToClient.start();
        
        // Wait for both threads to complete
        try {
            clientToServer.join();
            serverToClient.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Close server socket
        serverSocket.close();
    }

    public long getBytesSent() {
        return bytesSent.get();
    }

    public long getBytesReceived() {
        return bytesReceived.get();
    }
}
