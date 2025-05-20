package com.portmonitor.controller;

import com.portmonitor.app.model.NetworkTraffic;
import com.portmonitor.app.service.NetworkTrafficService;
import com.portmonitor.service.ProxyService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetworkTrafficController implements Initializable {
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> filterValueComboBox;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button exportButton;
    @FXML private LineChart<Number, Number> trafficChart;
    @FXML private NumberAxis timeAxis;
    @FXML private NumberAxis bytesAxis;
    
    // Proxy controls
    @FXML private TextField proxyHostField;
    @FXML private TextField proxyPortField;
    @FXML private Button startProxyButton;
    @FXML private Button stopProxyButton;
    @FXML private Label proxyStatusLabel;
    
    // Statistics labels
    @FXML private Label totalTrafficLabel;
    @FXML private Label currentRateLabel;
    @FXML private Label activeConnectionsLabel;
    
    private final NetworkTrafficService networkTrafficService = new NetworkTrafficService();
    private final ProxyService proxyService = new ProxyService();
    private ScheduledExecutorService scheduler;
    private XYChart.Series<Number, Number> sentSeries;
    private XYChart.Series<Number, Number> receivedSeries;
    private int timeSeconds = 0;
    private long totalTraffic = 0;
    private long lastTraffic = 0;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChart();
        setupFilters();
        setupProxyControls();
        setupButtons();
        initializeStatistics();
    }
    
    private void setupChart() {
        timeAxis.setLabel("Time (seconds)");
        timeAxis.setAutoRanging(false);
        timeAxis.setLowerBound(0);
        timeAxis.setUpperBound(60);
        timeAxis.setTickUnit(5);
        
        bytesAxis.setLabel("Bytes");
        bytesAxis.setAutoRanging(true);
        
        sentSeries = new XYChart.Series<>();
        sentSeries.setName("Bytes Sent");
        
        receivedSeries = new XYChart.Series<>();
        receivedSeries.setName("Bytes Received");
        
        trafficChart.getData().addAll(sentSeries, receivedSeries);
        trafficChart.setCreateSymbols(false);
        
        // Style the series
        sentSeries.getNode().setStyle("-fx-stroke: #4682B4; -fx-stroke-width: 2px;");
        receivedSeries.getNode().setStyle("-fx-stroke: #32CD32; -fx-stroke-width: 2px;");
    }
    
    private void setupFilters() {
        filterTypeComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "Protocol", "Process", "Port"));
        filterTypeComboBox.getSelectionModel().select("ALL");
        
        filterTypeComboBox.setOnAction(e -> updateFilterValues());
    }
    
    private void setupProxyControls() {
        startProxyButton.setOnAction(e -> startProxy());
        stopProxyButton.setOnAction(e -> stopProxy());
        
        proxyHostField.textProperty().addListener((obs, oldVal, newVal) -> 
            proxyService.setProxyHost(newVal));
            
        proxyPortField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int port = Integer.parseInt(newVal);
                proxyService.setProxyPort(port);
            } catch (NumberFormatException ex) {
                proxyPortField.setText(oldVal);
            }
        });
    }
    
    private void setupButtons() {
        startButton.setOnAction(e -> startMonitoring());
        stopButton.setOnAction(e -> stopMonitoring());
        exportButton.setOnAction(e -> exportData());
        
        stopButton.setDisable(true);
        stopProxyButton.setDisable(true);
    }
    
    private void initializeStatistics() {
        totalTrafficLabel.setText("0 B");
        currentRateLabel.setText("0 B/s");
        activeConnectionsLabel.setText("0");
    }
    
    private void startProxy() {
        try {
            proxyService.start();
            updateProxyStatus(true);
            startProxyButton.setDisable(true);
            stopProxyButton.setDisable(false);
            proxyStatusLabel.setText("Proxy Status: Running");
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Proxy Error", 
                     "Failed to start proxy", ex.getMessage());
        }
    }
    
    private void stopProxy() {
        proxyService.stop();
        updateProxyStatus(false);
        startProxyButton.setDisable(false);
        stopProxyButton.setDisable(true);
        proxyStatusLabel.setText("Proxy Status: Stopped");
    }
    
    private void updateProxyStatus(boolean running) {
        Platform.runLater(() -> {
            proxyStatusLabel.setText("Proxy Status: " + (running ? "Running" : "Stopped"));
            proxyStatusLabel.setStyle("-fx-text-fill: " + (running ? "green" : "red"));
        });
    }
    
    private void startMonitoring() {
        resetChart();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateTrafficData, 0, 1, TimeUnit.SECONDS);
        
        startButton.setDisable(true);
        stopButton.setDisable(false);
    }
    
    private void stopMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        
        startButton.setDisable(false);
        stopButton.setDisable(true);
    }
    
    private void resetChart() {
        timeSeconds = 0;
        totalTraffic = 0;
        lastTraffic = 0;
        sentSeries.getData().clear();
        receivedSeries.getData().clear();
    }
    
    private void updateTrafficData() {
        Task<List<NetworkTraffic>> task = new Task<>() {
            @Override
            protected List<NetworkTraffic> call() {
                return networkTrafficService.collectNetworkTraffic();
            }
        };
        
        task.setOnSucceeded(e -> {
            List<NetworkTraffic> trafficData = task.getValue();
            updateChartAndStats(trafficData);
        });
        
        new Thread(task).start();
    }
    
    private void updateChartAndStats(List<NetworkTraffic> trafficData) {
        long currentTotal = 0;
        for (NetworkTraffic traffic : trafficData) {
            currentTotal += traffic.getBytesSent() + traffic.getBytesReceived();
        }
        
        final long rate = currentTotal - lastTraffic;
        totalTraffic += rate;
        lastTraffic = currentTotal;
        
        Platform.runLater(() -> {
            // Update chart
            timeSeconds++;
            if (timeSeconds > 60) {
                sentSeries.getData().remove(0);
                receivedSeries.getData().remove(0);
                timeAxis.setLowerBound(timeSeconds - 60);
                timeAxis.setUpperBound(timeSeconds);
            }
            
            // Update statistics
            totalTrafficLabel.setText(formatBytes(totalTraffic));
            currentRateLabel.setText(formatBytes(rate) + "/s");
            activeConnectionsLabel.setText(String.valueOf(trafficData.size()));
            
            // Update chart series
            long totalSent = trafficData.stream().mapToLong(NetworkTraffic::getBytesSent).sum();
            long totalReceived = trafficData.stream().mapToLong(NetworkTraffic::getBytesReceived).sum();
            
            sentSeries.getData().add(new XYChart.Data<>(timeSeconds, totalSent));
            receivedSeries.getData().add(new XYChart.Data<>(timeSeconds, totalReceived));
        });
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    private void updateFilterValues() {
        String selectedType = filterTypeComboBox.getValue();
        if (selectedType == null) return;
        
        List<String> values;
        switch (selectedType) {
            case "Protocol":
                values = List.of("ALL", "TCP", "UDP");
                break;
            case "Process":
                values = networkTrafficService.getActiveProcesses();
                values.add(0, "ALL");
                break;
            case "Port":
                values = networkTrafficService.getActivePorts();
                values.add(0, "ALL");
                break;
            default:
                values = List.of("ALL");
                break;
        }
        
        filterValueComboBox.setItems(FXCollections.observableArrayList(values));
        filterValueComboBox.getSelectionModel().select("ALL");
    }
    
    private void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Traffic Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(trafficChart.getScene().getWindow());
        if (file != null) {
            try {
                networkTrafficService.exportToCSV(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", 
                         "Data exported", "Traffic data has been exported to " + file.getAbsolutePath());
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Export Error", 
                         "Failed to export data", ex.getMessage());
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
