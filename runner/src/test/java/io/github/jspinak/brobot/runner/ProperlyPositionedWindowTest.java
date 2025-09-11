package io.github.jspinak.brobot.runner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;

/** Test that creates a JavaFX window positioned correctly on the visible screen */
public class ProperlyPositionedWindowTest {

    public static class TestApp extends Application {
        private static CountDownLatch latch = new CountDownLatch(1);
        private static volatile boolean buttonClicked = false;

        @Override
        public void start(Stage primaryStage) {
            // Get screen bounds
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            System.out.println("Screen bounds: " + screenBounds);

            Label label = new Label("JavaFX Window is Visible!");
            label.setStyle("-fx-font-size: 24px; -fx-text-fill: green; -fx-font-weight: bold;");

            Label infoLabel =
                    new Label(
                            "Screen: "
                                    + screenBounds.getWidth()
                                    + "x"
                                    + screenBounds.getHeight()
                                    + " at ("
                                    + screenBounds.getMinX()
                                    + ", "
                                    + screenBounds.getMinY()
                                    + ")");
            infoLabel.setStyle("-fx-font-size: 14px;");

            Button testButton = new Button("Click Me to Verify Interaction!");
            testButton.setStyle("-fx-font-size: 18px; -fx-padding: 10px;");
            testButton.setOnAction(
                    e -> {
                        buttonClicked = true;
                        label.setText("Button Clicked! JavaFX is fully functional!");
                        label.setStyle(
                                "-fx-font-size: 24px; -fx-text-fill: blue; -fx-font-weight: bold;");
                        System.out.println("Button clicked!");
                    });

            Label timerLabel = new Label("Window will close in 10 seconds...");
            timerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

            VBox root = new VBox(20);
            root.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-alignment: center;");
            root.getChildren().addAll(label, infoLabel, testButton, timerLabel);

            Scene scene = new Scene(root, 600, 400);

            primaryStage.setTitle("JavaFX Test - Brobot Runner (Click the button!)");
            primaryStage.setScene(scene);

            // Position window in the center of the actual visible screen
            double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - 600) / 2;
            double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - 400) / 2;

            primaryStage.setX(centerX);
            primaryStage.setY(centerY);

            // Try multiple approaches to ensure visibility
            primaryStage.setAlwaysOnTop(true);
            primaryStage.show();
            primaryStage.toFront();
            primaryStage.requestFocus();

            System.out.println(
                    "Window positioned at: ("
                            + primaryStage.getX()
                            + ", "
                            + primaryStage.getY()
                            + ")");
            System.out.println(
                    "Window size: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());

            // Update timer
            new Thread(
                            () -> {
                                try {
                                    for (int i = 10; i > 0; i--) {
                                        final int count = i;
                                        Platform.runLater(
                                                () ->
                                                        timerLabel.setText(
                                                                "Window will close in "
                                                                        + count
                                                                        + " seconds..."));
                                        Thread.sleep(1000);
                                    }
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

        public static boolean wasButtonClicked() {
            return buttonClicked;
        }
    }

    @Test
    public void testJavaFxWindowAppearsOnScreen() throws InterruptedException {
        System.out.println("Launching JavaFX window with proper positioning...");
        System.out.println(
                "Please look for the window on your screen and click the button if you see it!");

        // Launch JavaFX in a separate thread
        Thread appThread =
                new Thread(
                        () -> {
                            Application.launch(TestApp.class);
                        });
        appThread.setDaemon(true);
        appThread.start();

        // Wait for the window to close
        boolean windowShown = TestApp.latch.await(15, TimeUnit.SECONDS);

        if (windowShown) {
            System.out.println("JavaFX window test completed!");
            if (TestApp.wasButtonClicked()) {
                System.out.println("✓ Button was clicked - UI interaction verified!");
            } else {
                System.out.println(
                        "ℹ Button was not clicked - window may not have been visible or"
                                + " interactive");
            }
        } else {
            System.out.println("JavaFX window display timed out");
        }
    }
}
