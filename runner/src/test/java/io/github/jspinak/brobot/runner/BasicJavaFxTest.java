package io.github.jspinak.brobot.runner;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic test to verify JavaFX runtime is available and working
 */
public class BasicJavaFxTest {

    @BeforeAll
    public static void initJavaFX() {
        // Initialize JavaFX toolkit
        new JFXPanel();
    }

    @Test
    public void testJavaFxPlatformIsAvailable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] success = {false};
        
        Platform.runLater(() -> {
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
        
        Platform.runLater(() -> {
            try {
                // Try to create basic JavaFX objects
                javafx.scene.control.Button button = new javafx.scene.control.Button("Test");
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
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX node creation should complete within 5 seconds");
        assertTrue(success[0], "JavaFX UI components should be created successfully");
    }
}