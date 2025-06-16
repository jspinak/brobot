package io.github.jspinak.brobot.runner.testutil;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Base class for JavaFX UI tests that properly extends ApplicationTest.
 * This ensures proper JavaFX platform initialization for headless testing.
 */
public abstract class BrobotJavaFXTestBase extends ApplicationTest {
    
    protected Stage testStage;
    protected StackPane testRoot;
    
    @BeforeAll
    public static void setupHeadless() {
        // These should already be set by gradle, but ensure they're set
        if (System.getProperty("testfx.headless") == null) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        this.testStage = stage;
        this.testRoot = new StackPane();
        
        // Create a minimal scene
        Scene scene = new Scene(testRoot, 800, 600);
        stage.setScene(scene);
        
        // Don't show the stage in headless mode
        if (!"true".equals(System.getProperty("testfx.headless"))) {
            stage.show();
        }
    }
    
    /**
     * Helper method to run code on JavaFX Application Thread
     */
    protected void runOnFxThread(Runnable action) {
        interact(action);
    }
}