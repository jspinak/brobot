package io.github.jspinak.brobot.runner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;

/** Simple test that shows a JavaFX window for manual verification */
public class SimpleVisibleWindowTest {

    public static class TestApp extends Application {
        private static CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void start(Stage primaryStage) {
            Label label = new Label("JavaFX is Working!\nThis window will close in 5 seconds...");
            label.setStyle("-fx-font-size: 24px; -fx-text-fill: green; -fx-font-weight: bold;");

            StackPane root = new StackPane();
            root.setStyle("-fx-background-color: white;");
            root.getChildren().add(label);

            Scene scene = new Scene(root, 500, 300);

            primaryStage.setTitle("JavaFX Test - Brobot Runner");
            primaryStage.setScene(scene);
            primaryStage.setX(200);
            primaryStage.setY(200);
            primaryStage.setAlwaysOnTop(true);
            primaryStage.show();
            primaryStage.toFront();
            primaryStage.requestFocus();

            System.out.println(
                    "JavaFX Window is now visible at position: "
                            + primaryStage.getX()
                            + ", "
                            + primaryStage.getY());

            // Auto-close after 5 seconds
            new Thread(
                            () -> {
                                try {
                                    Thread.sleep(5000);
                                    Platform.runLater(
                                            () -> {
                                                primaryStage.close();
                                                latch.countDown();
                                            });
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            })
                    .start();
        }
    }

    @Test
    public void testShowJavaFxWindow() throws InterruptedException {
        System.out.println("Launching JavaFX window...");

        // Launch JavaFX in a separate thread
        Thread appThread =
                new Thread(
                        () -> {
                            Application.launch(TestApp.class);
                        });
        appThread.setDaemon(true);
        appThread.start();

        // Wait for the window to close
        boolean windowShown = TestApp.latch.await(10, TimeUnit.SECONDS);

        if (windowShown) {
            System.out.println("JavaFX window was displayed successfully!");
        } else {
            System.out.println("JavaFX window display timed out");
        }
    }
}
