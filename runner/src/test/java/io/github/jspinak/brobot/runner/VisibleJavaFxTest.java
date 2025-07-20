package io.github.jspinak.brobot.runner;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JavaFX test that creates a visible window for manual verification
 */
public class VisibleJavaFxTest extends ApplicationTest {

    private Stage stage;
    private Label statusLabel;
    private volatile int clickCount = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        
        // Create UI elements
        statusLabel = new Label("JavaFX Test Window - Click the button!");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");
        
        Button testButton = new Button("Click Me!");
        testButton.setStyle("-fx-font-size: 20px; -fx-padding: 10px;");
        testButton.setOnAction(e -> {
            clickCount++;
            statusLabel.setText("Button clicked " + clickCount + " times!");
            System.out.println("Button clicked! Count: " + clickCount);
        });
        
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20px; -fx-alignment: center;");
        root.getChildren().addAll(statusLabel, testButton);
        
        Scene scene = new Scene(root, 400, 200);
        
        // Set window properties
        primaryStage.setTitle("JavaFX UI Test - Brobot Runner");
        primaryStage.setScene(scene);
        
        // Position window at a visible location
        primaryStage.setX(100);
        primaryStage.setY(100);
        
        // Make sure window is on top
        primaryStage.setAlwaysOnTop(true);
        
        primaryStage.show();
        primaryStage.toFront();
        
        // Request focus
        primaryStage.requestFocus();
        
        System.out.println("JavaFX Window created at: X=" + primaryStage.getX() + ", Y=" + primaryStage.getY());
        System.out.println("Window size: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testJavaFxWindowIsVisibleAndInteractive() throws InterruptedException {
        // Keep window visible for 3 seconds
        System.out.println("Test window should be visible now!");
        
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            assertNotNull(stage, "Stage should be created");
            assertTrue(stage.isShowing(), "Stage should be showing");
            assertTrue(stage.isAlwaysOnTop(), "Stage should be on top");
            
            // Simulate clicking the button
            stage.getScene().getRoot().lookup("Button").fireEvent(
                new javafx.scene.input.MouseEvent(
                    javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                    0, 0, 0, 0, javafx.scene.input.MouseButton.PRIMARY, 1,
                    true, true, true, true, true, true, true, true, true, true, null
                )
            );
            
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS), "UI operations should complete");
        
        // Keep window visible for observation
        Thread.sleep(3000);
        
        assertTrue(clickCount > 0, "Button should have been clicked at least once");
        System.out.println("Test completed successfully! Click count: " + clickCount);
    }
}