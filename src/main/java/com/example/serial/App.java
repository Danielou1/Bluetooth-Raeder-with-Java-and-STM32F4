package com.example.serial;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/serial/main-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Bluetooth Reader");
        stage.setScene(scene);
        stage.show();
    }
}
