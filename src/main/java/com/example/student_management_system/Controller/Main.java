package com.example.student_management_system.Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/student_management_system/View/Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);        // Auto full‑screen (maximised)
        // primaryStage.setFullScreen(true);    // Use this for true full‑screen
        primaryStage.setTitle("Student Management System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}