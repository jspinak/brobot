package io.github.jspinak.brobot.runner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BrobotRunnerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label label = new Label("Brobot Runner - Java " + javaVersion + ", JavaFX " + javafxVersion);
        Scene scene = new Scene(new StackPane(label), 640, 480);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Brobot Runner");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
