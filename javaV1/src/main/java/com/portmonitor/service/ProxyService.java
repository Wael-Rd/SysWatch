package com.portmonitor.service;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyService {
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final AtomicBoolean isRunning;
    private int proxyPort = 8080;
    private String proxyHost = "localhost";
    private final List<ProxyHandler> activeHandlers;
    
    public ProxyService() {
        this.isRunning = new AtomicBoolean(false);
        this.activeHandlers = new CopyOnWriteArrayList<>();
    }

    public void start() throws IOException {
        if (isRunning.get()) {
            return;
        }

        serverSocket = new ServerSocket(proxyPort);
        executorService = Executors.newCachedThreadPool();
        isRunning.set(true);

        // Start accepting connections in a separate thread
        new Thread(() -> {
            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ProxyHandler handler = new ProxyHandler(clientSocket);
                    activeHandlers.add(handler);
                    executorService.execute(() -> {
                        handler.run();
                        activeHandlers.remove(handler);
                    });
                } catch (IOException e) {
                    if (isRunning.get()) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stop() {
        isRunning.set(false);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (executorService != null) {
            executorService.shutdown();
        }
        
        activeHandlers.clear();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void setProxyPort(int port) {
        if (!isRunning.get()) {
            this.proxyPort = port;
        }
    }

    public void setProxyHost(String host) {
        if (!isRunning.get()) {
            this.proxyHost = host;
        }
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyHost() {
        return proxyHost;
    }
    
    public int getActiveConnections() {
        return activeHandlers.size();
    }
    
    public long getTotalBytesSent() {
        return activeHandlers.stream()
                .mapToLong(ProxyHandler::getBytesSent)
                .sum();
    }
    
    public long getTotalBytesReceived() {
        return activeHandlers.stream()
                .mapToLong(ProxyHandler::getBytesReceived)
                .sum();
    }
    
    public List<ConnectionStats> getConnectionStats() {
        List<ConnectionStats> stats = new ArrayList<>();
        for (ProxyHandler handler : activeHandlers) {
            stats.add(new ConnectionStats(
                handler.getBytesSent(),
                handler.getBytesReceived()
            ));
        }
        return stats;
    }
    
    public static class ConnectionStats {
        private final long bytesSent;
        private final long bytesReceived;
        
        public ConnectionStats(long bytesSent, long bytesReceived) {
            this.bytesSent = bytesSent;
            this.bytesReceived = bytesReceived;
        }
        
        public long getBytesSent() {
            return bytesSent;
        }
        
        public long getBytesReceived() {
            return bytesReceived;
        }
    }
}
