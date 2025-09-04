package io.github.jspinak.brobot.runner;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testfx.framework.junit5.ApplicationTest;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class JavaFxApplicationTest extends ApplicationTest {
    
    @BeforeAll
    public static void checkEnvironment() {
        assumeFalse(GraphicsEnvironment.isHeadless(), 
            "Skipping JavaFX test in headless environment");
    }

    private Stage stage;
    private Button testButton;
    private volatile boolean buttonClicked = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        
        // Create a simple UI with a button
        testButton = new Button("Test JavaFX");
        testButton.setOnAction(e -> {
            buttonClicked = true;
            System.out.println("JavaFX button clicked - UI is responsive!");
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(testButton);
        
        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("JavaFX Test Window");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testJavaFxApplicationLaunches() throws InterruptedException {
        // Verify stage is created and showing
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            assertNotNull(stage, "Stage should be created");
            assertTrue(stage.isShowing(), "Stage should be showing");
            assertEquals("JavaFX Test Window", stage.getTitle(), "Window title should match");
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX operations should complete within timeout");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testJavaFxUiIsResponsive() throws InterruptedException {
        // Test that we can interact with UI elements
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            assertNotNull(testButton, "Test button should exist");
            
            // Simulate button click
            testButton.fire();
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Button click should complete within timeout");
        
        // Give a moment for the click handler to execute
        Thread.sleep(100);
        
        assertTrue(buttonClicked, "Button click handler should have been called");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testSceneIsProperlyConfigured() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            Scene scene = stage.getScene();
            assertNotNull(scene, "Scene should be set");
            assertEquals(300, scene.getWidth(), "Scene width should be 300");
            assertEquals(200, scene.getHeight(), "Scene height should be 200");
            
            // Verify scene contains our root node
            assertNotNull(scene.getRoot(), "Scene should have a root node");
            assertTrue(scene.getRoot() instanceof StackPane, "Root should be a StackPane");
            
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Scene verification should complete within timeout");
    }
}