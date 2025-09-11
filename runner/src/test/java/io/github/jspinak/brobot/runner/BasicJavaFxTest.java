package io.github.jspinak.brobot.runner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import io.github.jspinak.brobot.runner.testutils.ImprovedJavaFXTestBase;

/** Basic test to verify JavaFX runtime is available and working */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
@Disabled("Temporarily disabled - hangs in CI environment")
public class BasicJavaFxTest extends ImprovedJavaFXTestBase {

    @Test
    public void testJavaFxPlatformIsAvailable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] success = {false};

        Platform.runLater(
                () -> {
                    // If we get here, JavaFX is working
                    success[0] = true;
                    System.out.println("JavaFX Platform is running!");
                    latch.countDown();
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX should initialize within 5 seconds");
        assertTrue(success[0], "JavaFX Platform.runLater should execute successfully");
    }

    @Test
    public void testJavaFxCanCreateBasicNodes() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] success = {false};

        Platform.runLater(
                () -> {
                    try {
                        // Try to create basic JavaFX objects
                        javafx.scene.control.Button button =
                                new javafx.scene.control.Button("Test");
                        javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane();
                        pane.getChildren().add(button);

                        // If we get here without exceptions, JavaFX UI components work
                        success[0] = true;
                        System.out.println("JavaFX UI components created successfully!");
                    } catch (Exception e) {
                        System.err.println("Failed to create JavaFX components: " + e.getMessage());
                        e.printStackTrace();
                    }
                    latch.countDown();
                });

        assertTrue(
                latch.await(5, TimeUnit.SECONDS),
                "JavaFX node creation should complete within 5 seconds");
        assertTrue(success[0], "JavaFX UI components should be created successfully");
    }
}
