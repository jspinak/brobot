package io.github.jspinak.brobot.runner.testutil;

import lombok.Data;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base class for tests that require JavaFX platform initialization.
 * This class ensures JavaFX is properly initialized before tests run.
 */
public abstract class JavaFXTestBase {
    
    private static boolean javafxInitialized = false;
    
    @BeforeAll
    public static void initializeJavaFX() {
        if (javafxInitialized) {
            return;
        }
        
        // Set headless properties
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
        
        try {
            Platform.startup(() -> {});
            javafxInitialized = true;
        } catch (IllegalStateException e) {
            // Platform already initialized
            javafxInitialized = true;
        }
        
        // Wait for toolkit to be ready
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("JavaFX initialization timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("JavaFX initialization interrupted", e);
        }
    }
}