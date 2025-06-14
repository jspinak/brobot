package io.github.jspinak.brobot.runner.ui.log;

import javafx.application.Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Minimal JavaFX test to verify platform availability in a headless environment.
 */
public class SimpleJavaFXTest {

    @BeforeAll
    public static void setupJavaFX() {
        System.out.println("=== Simple JavaFX Setup Test (Standard Headless Mode) ===");

        // Print system properties
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("JavaFX version: " + System.getProperty("javafx.version", "not set"));
        System.out.println("Glass platform: " + System.getProperty("glass.platform", "not set"));
        System.out.println("Prism order: " + System.getProperty("prism.order", "not set"));
        System.out.println("TestFX headless: " + System.getProperty("testfx.headless", "not set"));
        System.out.println("AWT headless: " + System.getProperty("java.awt.headless", "not set"));

        // FIRST: Check basic JavaFX platform classes (these don't need toolkit)
        try {
            Class.forName("javafx.application.Platform");
            System.out.println("✓ JavaFX Platform class found on classpath");
        } catch (Exception e) {
            System.out.println("✗ JavaFX Platform class NOT found on classpath");
            e.printStackTrace();
            throw new RuntimeException("JavaFX Platform not available - check dependencies", e);
        }

        try {
            Class.forName("javafx.scene.Scene");
            System.out.println("✓ JavaFX Scene class found");
        } catch (Exception e) {
            System.out.println("✗ JavaFX Scene class NOT found");
            throw new RuntimeException("JavaFX Scene not available", e);
        }

        try {
            Class.forName("javafx.stage.Stage");
            System.out.println("✓ JavaFX Stage class found");
        } catch (Exception e) {
            System.out.println("✗ JavaFX Stage class NOT found");
            throw new RuntimeException("JavaFX Stage not available", e);
        }

        // SECOND: Initialize JavaFX toolkit in headless mode
        try {
            System.out.println("Attempting Platform.startup() in headless mode...");
            Platform.startup(() -> {
                System.out.println("✓ JavaFX Platform startup callback executed successfully");
                System.out.println("✓ Current thread: " + Thread.currentThread().getName());
            });
            System.out.println("✓ Platform.startup() completed without exception");
        } catch (IllegalStateException e) {
            System.out.println("ℹ Platform already initialized: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ Platform.startup() failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("JavaFX Platform startup failed", e);
        }

        // THIRD: Wait for toolkit to be fully initialized
        CountDownLatch initLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            System.out.println("✓ JavaFX toolkit is now initialized and ready (headless mode)");
            initLatch.countDown();
        });

        try {
            if (!initLatch.await(15, TimeUnit.SECONDS)) {
                throw new RuntimeException("JavaFX toolkit initialization timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted waiting for JavaFX toolkit", e);
        }

        // FOURTH: Now we can safely check control classes
        try {
            Class.forName("javafx.scene.control.Button");
            System.out.println("✓ JavaFX Controls (Button) class found and loaded");
        } catch (Exception e) {
            System.out.println("✗ JavaFX Controls (Button) class NOT found or failed to load");
            e.printStackTrace();
            throw new RuntimeException("JavaFX Controls not available", e);
        }

        System.out.println("✓ JavaFX setup completed successfully in headless mode!");
    }

    @Test
    public void testJavaFXPlatformAvailable() throws InterruptedException {
        System.out.println("\n=== Testing JavaFX Platform Functionality (Headless) ===");

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final Exception[] error = {null};

        try {
            System.out.println("Submitting task to JavaFX Application Thread...");
            Platform.runLater(() -> {
                try {
                    String threadName = Thread.currentThread().getName();
                    boolean isFxThread = Platform.isFxApplicationThread();

                    System.out.println("✓ Task executing on thread: " + threadName);
                    System.out.println("✓ Is FX Application Thread: " + isFxThread);

                    if (!isFxThread) {
                        throw new RuntimeException("Not running on JavaFX Application Thread!");
                    }

                    success[0] = true;
                } catch (Exception e) {
                    System.out.println("✗ Error in JavaFX runLater: " + e.getMessage());
                    error[0] = e;
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });

            System.out.println("Waiting for JavaFX task to complete...");
            boolean completed = latch.await(10, TimeUnit.SECONDS);

            if (!completed) {
                throw new RuntimeException("JavaFX Platform.runLater() timed out after 10 seconds");
            }

            if (error[0] != null) {
                throw new RuntimeException("JavaFX Platform.runLater() failed", error[0]);
            }

            if (!success[0]) {
                throw new RuntimeException("JavaFX Platform.runLater() did not execute successfully");
            }

            System.out.println("✓ JavaFX Platform test PASSED - JavaFX is working correctly in headless mode!");

        } catch (Exception e) {
            System.out.println("✗ JavaFX Platform test FAILED: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testJavaFXControlsCreation() throws InterruptedException {
        System.out.println("\n=== Testing JavaFX Controls Creation (Headless) ===");

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final Exception[] error = {null};

        Platform.runLater(() -> {
            try {
                // Test creating basic JavaFX controls on FX thread (toolkit is already initialized)
                javafx.scene.control.Label label = new javafx.scene.control.Label("Test Label");
                System.out.println("✓ Created Label: " + label.getText());

                javafx.scene.control.Button button = new javafx.scene.control.Button("Test Button");
                System.out.println("✓ Created Button: " + button.getText());

                javafx.scene.control.TextField textField = new javafx.scene.control.TextField("Test Text");
                System.out.println("✓ Created TextField: " + textField.getText());

                // Test TableView (important for your actual tests)
                javafx.scene.control.TableView<?> tableView = new javafx.scene.control.TableView<>();
                System.out.println("✓ Created TableView with " + tableView.getColumns().size() + " columns");

                // Test ComboBox
                javafx.scene.control.ComboBox<String> comboBox = new javafx.scene.control.ComboBox<>();
                comboBox.getItems().add("Test Item");
                System.out.println("✓ Created ComboBox with " + comboBox.getItems().size() + " items");

                // Test TextArea
                javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea("Test Content");
                System.out.println("✓ Created TextArea: " + textArea.getText());

                success[0] = true;

            } catch (Exception e) {
                System.out.println("✗ Cannot create JavaFX controls: " + e.getMessage());
                error[0] = e;
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);

        if (!completed) {
            throw new RuntimeException("JavaFX controls creation timed out");
        }

        if (error[0] != null) {
            throw new RuntimeException("JavaFX controls creation failed", error[0]);
        }

        if (!success[0]) {
            throw new RuntimeException("JavaFX controls creation did not succeed");
        }

        System.out.println("✓ JavaFX Controls test PASSED - can create all UI components in headless mode!");
    }

    @Test
    public void testBasicSceneCreation() throws InterruptedException {
        System.out.println("\n=== Testing JavaFX Scene Creation (Headless) ===");

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};
        final Exception[] error = {null};

        Platform.runLater(() -> {
            try {
                // Test creating a basic scene with controls in headless mode
                javafx.scene.layout.VBox root = new javafx.scene.layout.VBox();
                javafx.scene.control.Label label = new javafx.scene.control.Label("Test Scene");
                javafx.scene.control.Button button = new javafx.scene.control.Button("Test Button");

                root.getChildren().addAll(label, button);

                javafx.scene.Scene scene = new javafx.scene.Scene(root, 300, 200);
                System.out.println("✓ Created Scene with size: " + scene.getWidth() + "x" + scene.getHeight());
                System.out.println("✓ Scene root has " + root.getChildren().size() + " children");

                // Test that we can create a Stage (but don't show it in headless mode)
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setScene(scene);
                stage.setTitle("Test Stage");
                System.out.println("✓ Created Stage with title: " + stage.getTitle());
                System.out.println("✓ Stage scene set successfully");

                success[0] = true;

            } catch (Exception e) {
                System.out.println("✗ Cannot create JavaFX scene: " + e.getMessage());
                error[0] = e;
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);

        if (!completed) {
            throw new RuntimeException("JavaFX scene creation timed out");
        }

        if (error[0] != null) {
            throw new RuntimeException("JavaFX scene creation failed", error[0]);
        }

        if (!success[0]) {
            throw new RuntimeException("JavaFX scene creation did not succeed");
        }

        System.out.println("✓ JavaFX Scene test PASSED - can create complete scenes with stages in headless mode!");
    }
}