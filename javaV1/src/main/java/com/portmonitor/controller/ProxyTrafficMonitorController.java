package com.portmonitor.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class ProxyTrafficMonitorController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private TableView<RequestEntry> requestsTable;
    @FXML private TableColumn<RequestEntry, String> methodColumn;
    @FXML private TableColumn<RequestEntry, String> urlColumn;
    @FXML private TableColumn<RequestEntry, String> statusColumn;
    @FXML private TableColumn<RequestEntry, String> timeColumn;
    @FXML private TableColumn<RequestEntry, String> detailsColumn;

    private ObservableList<RequestEntry> masterData = FXCollections.observableArrayList();
    private FilteredList<RequestEntry> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup columns
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("detailsButtonText"));

        // Add custom cell factory for status to show colored badges
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.setStyle("-fx-background-color: " + getStatusColor(status) + "; -fx-text-fill: white; -fx-padding: 3 6 3 6; -fx-background-radius: 4;");
                    setGraphic(label);
                }
            }
        });

        // Add custom cell factory for details column to show a button
        detailsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("View");

            {
                btn.setOnAction(event -> {
                    RequestEntry entry = getTableView().getItems().get(getIndex());
                    showDetailsDialog(entry);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // Setup filtered data
        filteredData = new FilteredList<>(masterData, p -> true);
        requestsTable.setItems(filteredData);

        // Setup search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(createPredicate(newVal));
        });

        // Load initial data
        loadData();

        // Setup refresh button
        refreshButton.setOnAction(e -> loadData());
    }

    private Predicate<RequestEntry> createPredicate(String filter) {
        if (filter == null || filter.isEmpty()) {
            return p -> true;
        }
        String lowerCaseFilter = filter.toLowerCase();
        return p -> p.getMethod().toLowerCase().contains(lowerCaseFilter)
                || p.getUrl().toLowerCase().contains(lowerCaseFilter)
                || p.getStatus().toLowerCase().contains(lowerCaseFilter)
                || p.getTime().toLowerCase().contains(lowerCaseFilter);
    }

    private void loadData() {
        // For demo, generate some dummy data
        masterData.clear();
        masterData.add(new RequestEntry("GET", "https://api.example.com/users", "200", LocalTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("hh:mm:ss a"))));
        masterData.add(new RequestEntry("POST", "https://api.example.com/data", "400", LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"))));
    }

    private String getStatusColor(String status) {
        switch (status) {
            case "200":
                return "#000000"; // black
            case "400":
                return "#ff0000"; // red
            default:
                return "#666666"; // gray
        }
    }

    private void showDetailsDialog(RequestEntry entry) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Request Details");

        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 10;");
        box.getChildren().addAll(
                new Label("Method: " + entry.getMethod()),
                new Label("URL: " + entry.getUrl()),
                new Label("Status: " + entry.getStatus()),
                new Label("Time: " + entry.getTime())
        );

        Scene scene = new Scene(box, 400, 200);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static class RequestEntry {
        private final SimpleStringProperty method;
        private final SimpleStringProperty url;
        private final SimpleStringProperty status;
        private final SimpleStringProperty time;
        private final SimpleStringProperty detailsButtonText = new SimpleStringProperty("View");

        public RequestEntry(String method, String url, String status, String time) {
            this.method = new SimpleStringProperty(method);
            this.url = new SimpleStringProperty(url);
            this.status = new SimpleStringProperty(status);
            this.time = new SimpleStringProperty(time);
        }

        public String getMethod() {
            return method.get();
        }

        public String getUrl() {
            return url.get();
        }

        public String getStatus() {
            return status.get();
        }

        public String getTime() {
            return time.get();
        }

        public String getDetailsButtonText() {
            return detailsButtonText.get();
        }
    }
}
