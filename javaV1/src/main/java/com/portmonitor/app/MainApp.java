package com.portmonitor.app;

import com.portmonitor.app.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 初始化数据库
            DatabaseManager.getInstance().initDatabase();
            
            // 加载主界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/portmonitor/app/view/MainView.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/portmonitor/app/css/style.css").toExternalForm());
            
            primaryStage.setTitle("SysWatch");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // 关闭数据库连接
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
