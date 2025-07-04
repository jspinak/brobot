package io.github.jspinak.brobot.runner.ui.log;

import lombok.Data;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Diagnostic utility to test JavaFX environment setup
 * Run this before your tests to verify JavaFX is working correctly
 */
@Data
public class JavaFXDiagnostic extends Application {

    private static final CountDownLatch applicationStarted = new CountDownLatch(1);
    private static volatile boolean startupSuccessful = false;
    private static volatile Exception startupException = null;

    public static void main(String[] args) {
        System.out.println("=== JavaFX Environment Diagnostic Tool ===");

        // Check system properties
        checkSystemProperties();

        // Check class availability
        checkClassAvailability();

        // Try to launch JavaFX application
        try {
            System.out.println("\n--- Testing JavaFX Application Launch ---");

            // Launch in separate thread to avoid blocking
            Thread launchThread = new Thread(() -> {
                try {
                    Application.launch(JavaFXDiagnostic.class, args);
                } catch (Exception e) {
                    startupException = e;
                    System.err.println("ERROR: JavaFX launch failed: " + e.getMessage());
                    e.printStackTrace();
                    applicationStarted.countDown();
                }
            });

            launchThread.setDaemon(false);
            launchThread.start();

            // Wait for application to start
            boolean started = applicationStarted.await(30, TimeUnit.SECONDS);

            if (!started) {
                System.err.println("ERROR: JavaFX application failed to start within 30 seconds");
            } else if (startupException != null) {
                System.err.println("ERROR: JavaFX application startup failed with exception");
                startupException.printStackTrace();
            } else if (startupSuccessful) {
                System.out.println("✓ JavaFX application started successfully");

                // Keep running for a few seconds to test
                Thread.sleep(5000);

                System.out.println("✓ JavaFX application running normally");
                Platform.exit();
            }

        } catch (Exception e) {
            System.err.println("ERROR: Exception during JavaFX test: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Diagnostic Complete ===");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            System.out.println("JavaFX Application.start() called");
            System.out.println("Primary stage: " + (primaryStage != null));
            System.out.println("Current thread: " + Thread.currentThread().getName());
            System.out.println("Is FX Application Thread: " + Platform.isFxApplicationThread());

            // Create simple UI
            VBox root = new VBox(10);
            root.getChildren().addAll(
                    new Label("JavaFX is working!"),
                    new Label("Thread: " + Thread.currentThread().getName()),
                    new Label("Stage: " + primaryStage.getClass().getSimpleName()),
                    new Button("Test Button")
            );

            Scene scene = new Scene(root, 400, 300);
            primaryStage.setTitle("JavaFX Diagnostic");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✓ JavaFX stage shown successfully");
            startupSuccessful = true;

        } catch (Exception e) {
            System.err.println("ERROR: Exception in JavaFX start() method: " + e.getMessage());
            e.printStackTrace();
            startupException = e;
        } finally {
            applicationStarted.countDown();
        }
    }

    private static void checkSystemProperties() {
        System.out.println("\n--- System Properties ---");

        String[] importantProps = {
                "java.version",
                "java.runtime.version",
                "os.name",
                "os.version",
                "os.arch",
                "java.awt.headless",
                "javafx.version",
                "javafx.runtime.version",
                "glass.platform",
                "prism.order",
                "testfx.headless"
        };

        for (String prop : importantProps) {
            String value = System.getProperty(prop);
            System.out.println(prop + " = " + (value != null ? value : "<not set>"));
        }
    }

    private static void checkClassAvailability() {
        System.out.println("\n--- Class Availability Check ---");

        String[] importantClasses = {
                "javafx.application.Application",
                "javafx.application.Platform",
                "javafx.stage.Stage",
                "javafx.scene.Scene",
                "javafx.scene.control.Button",
                "com.sun.javafx.application.PlatformImpl",
                "org.testfx.api.FxToolkit",
                "org.testfx.framework.junit5.ApplicationExtension"
        };

        for (String className : importantClasses) {
            try {
                Class.forName(className);
                System.out.println("✓ " + className);
            } catch (ClassNotFoundException e) {
                System.out.println("✗ " + className + " - NOT FOUND");
            } catch (Exception e) {
                System.out.println("✗ " + className + " - ERROR: " + e.getMessage());
            }
        }
    }
}