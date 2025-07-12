package io.github.jspinak.brobot.runner.testutils;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base class for tests that require JavaFX runtime.
 * Ensures JavaFX Platform is initialized before tests run.
 */
public abstract class JavaFXTestBase {
    
    private static boolean javaFxInitialized = false;
    
    @BeforeAll
    public static void initializeJavaFX() throws InterruptedException {
        if (javaFxInitialized) {
            return;
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        
        // Initialize JavaFX Platform
        Platform.startup(() -> {
            // JavaFX is now initialized
            latch.countDown();
        });
        
        // Wait for initialization
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX initialization timeout");
        }
        
        javaFxInitialized = true;
    }
    
    /**
     * Runs the given runnable on the JavaFX Application Thread and waits for completion.
     */
    protected void runAndWait(Runnable action) throws InterruptedException {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    action.run();
                } finally {
                    latch.countDown();
                }
            });
            latch.await(5, TimeUnit.SECONDS);
        }
    }
}