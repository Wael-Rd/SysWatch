package com.portmonitor.controller;

import com.portmonitor.dao.MonitoredEntityDao;
import com.portmonitor.model.MonitoredEntity;
// Change this import
// import com.portmonitor.model.Port;
// To this:
import com.portmonitor.app.model.Port;
// Change this import:
import com.portmonitor.app.model.ProcessInfo;
// Instead of:
// import com.portmonitor.model.ProcessInfo;
import com.portmonitor.service.ExportService;
import com.portmonitor.service.PortService;
import com.portmonitor.service.ProcessService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList; // Add this import
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the main application window.
 */
public class MainController implements Initializable {

    // Services
    private final PortService portService = new PortService();
    private final ProcessService processService = new ProcessService();
    private final ExportService exportService = new ExportService();
    private final MonitoredEntityDao monitoredEntityDao = new MonitoredEntityDao();

    // Observable lists for TableViews
    private final ObservableList<Port> portsList = FXCollections.observableArrayList();
    private final ObservableList<ProcessInfo> processesList = FXCollections.observableArrayList();
    private final ObservableList<MonitoredEntity> monitoredEntitiesList = FXCollections.observableArrayList();

    // Filtered lists for search functionality
    private FilteredList<Port> filteredPorts;
    private FilteredList<ProcessInfo> filteredProcesses;
    private FilteredList<MonitoredEntity> filteredEntities;

    // Ports Tab
    @FXML private TableView<Port> portsTableView;
    @FXML private TableColumn<Port, Integer> portNumberColumn;
    @FXML private TableColumn<Port, String> protocolColumn;
    @FXML private TableColumn<Port, String> stateColumn;
    @FXML private TableColumn<Port, String> processNameColumn;
    @FXML private TableColumn<Port, Integer> pidColumn;
    @FXML private TextField portSearchField;
    @FXML private Button refreshPortsButton;
    @FXML private Button exportPortsButton;
    @FXML private Button monitorPortButton;
    @FXML private ProgressBar portScanProgressBar;
    
    // Processes Tab
    @FXML private TableView<ProcessInfo> processesTableView;
    @FXML private TableColumn<ProcessInfo, Integer> processPidColumn;
    @FXML private TableColumn<ProcessInfo, String> processNameInfoColumn; // Changed from processNameColumn
    @FXML private TableColumn<ProcessInfo, String> cpuUsageColumn;
    @FXML private TableColumn<ProcessInfo, String> memoryUsageColumn;
    @FXML private TableColumn<ProcessInfo, String> userColumn;
    @FXML private TextField processSearchField;
    @FXML private Button refreshProcessesButton;
    @FXML private Button exportProcessesButton;
    @FXML private Button monitorProcessButton;
    @FXML private ProgressBar processLoadProgressBar;
    
    // Monitored Entities Tab
    @FXML private TableView<MonitoredEntity> monitoredEntitiesTableView;
    @FXML private TableColumn<MonitoredEntity, Integer> entityIdColumn;
    @FXML private TableColumn<MonitoredEntity, String> entityTypeColumn;
    @FXML private TableColumn<MonitoredEntity, String> entityNameColumn;
    @FXML private TableColumn<MonitoredEntity, String> entityTimestampColumn;
    @FXML private TextField entitySearchField;
    @FXML private Button deleteEntityButton;
    @FXML private Button exportEntitiesButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize tables and load data
        setupPortsTable();
        setupProcessesTable();
        setupMonitoredEntitiesTable();
        
        // Load initial data
        loadPorts();
        loadProcesses();
        loadMonitoredEntities();
        
        // Setup search functionality
        setupSearch();
    }
    
    // Setup methods for tables
    private void setupPortsTable() {
        portNumberColumn.setCellValueFactory(new PropertyValueFactory<>("portNumber"));
        protocolColumn.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        processNameColumn.setCellValueFactory(new PropertyValueFactory<>("processName"));
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        
        portsTableView.setItems(portsList);
        filteredPorts = new FilteredList<>(portsList, p -> true);
        portsTableView.setItems(filteredPorts);
        
        // Setup button actions
        refreshPortsButton.setOnAction(e -> loadPorts());
        exportPortsButton.setOnAction(e -> exportPorts());
        monitorPortButton.setOnAction(e -> monitorSelectedPort());
    }
    
    private void setupProcessesTable() {
        processPidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        processNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cpuUsageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedCpuUsage()));
        // Change this line
        memoryUsageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedMemoryUsage()));
        // Instead of:
        // memoryUsageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedMemory()));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        
        processesTableView.setItems(processesList);
        filteredProcesses = new FilteredList<>(processesList, p -> true);
        processesTableView.setItems(filteredProcesses);
        
        // Setup button actions
        refreshProcessesButton.setOnAction(e -> loadProcesses());
        exportProcessesButton.setOnAction(e -> exportProcesses());
        monitorProcessButton.setOnAction(e -> monitorSelectedProcess());
    }
    
    private void setupMonitoredEntitiesTable() {
        entityIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        entityTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        entityNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        entityTimestampColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            return new SimpleStringProperty(timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
        
        monitoredEntitiesTableView.setItems(monitoredEntitiesList);
        filteredEntities = new FilteredList<>(monitoredEntitiesList, p -> true);
        monitoredEntitiesTableView.setItems(filteredEntities);
        
        // Setup button actions
        deleteEntityButton.setOnAction(e -> deleteSelectedEntity());
        exportEntitiesButton.setOnAction(e -> exportMonitoredEntities());
    }
    
    // Data loading methods
    private void loadPorts() {
        portScanProgressBar.setVisible(true);
        Task<List<Port>> task = new Task<>() {
            @Override
            protected List<Port> call() {
                return portService.getAllPorts();
            }
        };
        
        task.setOnSucceeded(e -> {
            portsList.clear();
            portsList.addAll(task.getValue());
            portScanProgressBar.setVisible(false);
        });
        
        task.setOnFailed(e -> {
            portScanProgressBar.setVisible(false);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load ports", task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    private void loadProcesses() {
        processLoadProgressBar.setVisible(true);
        Task<List<ProcessInfo>> task = new Task<>() {
            @Override
            protected List<ProcessInfo> call() {
                return processService.getAllProcesses();
            }
        };
        
        task.setOnSucceeded(e -> {
            processesList.clear();
            processesList.addAll(task.getValue());
            processLoadProgressBar.setVisible(false);
        });
        
        task.setOnFailed(e -> {
            processLoadProgressBar.setVisible(false);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load processes", task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    private void loadMonitoredEntities() {
        try {
            monitoredEntitiesList.clear();
            monitoredEntitiesList.addAll(monitoredEntityDao.getAllEntities());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load monitored entities", e.getMessage());
        }
    }
    
    // Search functionality
    private void setupSearch() {
        // Port search
        portSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPorts.setPredicate(port -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(port.getPortNumber()).contains(lowerCaseFilter) ||
                       port.getProtocol().toLowerCase().contains(lowerCaseFilter) ||
                       port.getState().toLowerCase().contains(lowerCaseFilter) ||
                       port.getProcessName().toLowerCase().contains(lowerCaseFilter) ||
                       String.valueOf(port.getPid()).contains(lowerCaseFilter);
            });
        });
        
        // Process search
        processSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredProcesses.setPredicate(process -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(process.getPid()).contains(lowerCaseFilter) ||
                       process.getName().toLowerCase().contains(lowerCaseFilter) ||
                       process.getUser().toLowerCase().contains(lowerCaseFilter);
            });
        });
        
        // Entity search
        entitySearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEntities.setPredicate(entity -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(entity.getId()).contains(lowerCaseFilter) ||
                       entity.getType().toString().toLowerCase().contains(lowerCaseFilter) ||
                       entity.getName().toLowerCase().contains(lowerCaseFilter) ||
                       entity.getDetails().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }
    
    // Export methods
    private void exportPorts() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Ports to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(portsTableView.getScene().getWindow());
        
        if (file != null) {
            try {
                exportService.exportPortsToCSV(new ArrayList<>(portsList), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Export Successful", "Ports exported to " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Export Failed", e.getMessage());
            }
        }
    }
    
    private void exportProcesses() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Processes to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(processesTableView.getScene().getWindow());
        
        if (file != null) {
            try {
                // Ensure this uses com.portmonitor.app.model.ProcessInfo
                exportService.exportProcessesToCSV(new ArrayList<com.portmonitor.app.model.ProcessInfo>(processesList), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Export Successful", "Processes exported to " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Export Failed", e.getMessage());
            }
        }
    }
    
    private void exportMonitoredEntities() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Monitored Entities to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(monitoredEntitiesTableView.getScene().getWindow());
        
        if (file != null) {
            try {
                exportService.exportMonitoredEntitiesToCSV(new ArrayList<>(monitoredEntitiesList), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Export Successful", "Monitored entities exported to " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Export Failed", e.getMessage());
            }
        }
    }
    
    // Monitor methods
    private void monitorSelectedPort() {
        Port selectedPort = portsTableView.getSelectionModel().getSelectedItem();
        if (selectedPort != null) {
            try {
                monitoredEntityDao.addPort(selectedPort);
                loadMonitoredEntities();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Port Monitored", "Port " + selectedPort.getPortNumber() + " added to monitored entities.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to monitor port", e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Port Selected", "Please select a port to monitor.");
        }
    }
    
    private void monitorSelectedProcess() {
        // This should be com.portmonitor.app.model.ProcessInfo due to processesList type
        com.portmonitor.app.model.ProcessInfo selectedProcess = processesTableView.getSelectionModel().getSelectedItem();
        if (selectedProcess != null) {
            try {
                monitoredEntityDao.addProcess(selectedProcess);
                loadMonitoredEntities();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Process Monitored", "Process " + selectedProcess.getName() + " added to monitored entities.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to monitor process", e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Process Selected", "Please select a process to monitor.");
        }
    }
    
    private void deleteSelectedEntity() {
        MonitoredEntity selectedEntity = monitoredEntitiesTableView.getSelectionModel().getSelectedItem();
        if (selectedEntity != null) {
            try {
                monitoredEntityDao.deleteEntity(selectedEntity.getId());
                loadMonitoredEntities();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Entity Deleted", "Entity removed from monitoring.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete entity", e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Entity Selected", "Please select an entity to delete.");
        }
    }
    
    // Utility methods
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}