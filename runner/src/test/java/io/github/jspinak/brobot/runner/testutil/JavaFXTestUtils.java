package io.github.jspinak.brobot.runner.testutil;

import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for JavaFX test initialization.
 * Provides a simple way to initialize JavaFX Platform for tests.
 */
public class JavaFXTestUtils {
    
    private static boolean initialized = false;
    
    /**
     * Initialize JavaFX Platform for testing.
     * Safe to call multiple times - will only initialize once.
     */
    public static void initJavaFX() throws InterruptedException {
        if (initialized) {
            return;
        }
        
        // Initialize JavaFX Platform if not already initialized
        try {
            Platform.startup(() -> {
                // Platform initialized
            });
        } catch (IllegalStateException e) {
            // Platform already initialized, which is fine
        }
        
        // Ensure we're ready by running a simple task
        CountDownLatch readyLatch = new CountDownLatch(1);
        Platform.runLater(readyLatch::countDown);
        if (!readyLatch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX Platform initialization timeout");
        }
        
        initialized = true;
    }
    
    /**
     * Run an action on the JavaFX Application Thread and wait for completion.
     */
    public static void runOnFXThread(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX action timeout");
        }
    }
    
    /**
     * Run an action on the JavaFX Application Thread and return a result.
     */
    public static <T> T runOnFXThreadAndWait(java.util.concurrent.Callable<T> action) throws Exception {
        final Object[] result = new Object[1];
        final Exception[] exception = new Exception[1];
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                result[0] = action.call();
            } catch (Exception e) {
                exception[0] = e;
            } finally {
                latch.countDown();
            }
        });
        
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("JavaFX action timeout");
        }
        
        if (exception[0] != null) {
            throw exception[0];
        }
        
        @SuppressWarnings("unchecked")
        T typedResult = (T) result[0];
        return typedResult;
    }
}