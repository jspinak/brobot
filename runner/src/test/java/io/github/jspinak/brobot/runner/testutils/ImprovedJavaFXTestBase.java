package io.github.jspinak.brobot.runner.testutils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

/**
 * Improved base class for JavaFX tests with better headless support. This class provides robust
 * JavaFX initialization that works in various environments.
 */
public abstract class ImprovedJavaFXTestBase {

    private static final AtomicBoolean javaFxInitialized = new AtomicBoolean(false);
    private static final Object initLock = new Object();

    @BeforeAll
    public static void initializeJavaFX() throws InterruptedException {
        synchronized (initLock) {
            if (javaFxInitialized.get()) {
                return;
            }

            // Set system properties for headless testing
            System.setProperty("java.awt.headless", "false");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("testfx.headless", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            System.setProperty("embedded", "monocle");

            try {
                // Try to initialize JavaFX
                if (!tryPlatformStartup()) {
                    // Platform might already be initialized
                    System.out.println(
                            "JavaFX initialization skipped - assuming already initialized");
                }
                javaFxInitialized.set(true);
            } catch (Exception e) {
                System.err.println("JavaFX initialization failed: " + e.getMessage());
                // Continue anyway - some tests might still work
                javaFxInitialized.set(true);
            }
        }
    }

    private static boolean tryPlatformStartup() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(() -> latch.countDown());
            return latch.await(5, TimeUnit.SECONDS);
        } catch (IllegalStateException e) {
            // Platform already initialized
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Runs the given runnable on the JavaFX Application Thread and waits for completion. If JavaFX
     * is not available, runs directly.
     */
    protected void runAndWait(Runnable action) throws InterruptedException {
        if (!javaFxInitialized.get()) {
            // If JavaFX not initialized, run directly
            action.run();
            return;
        }

        try {
            if (Platform.isFxApplicationThread()) {
                action.run();
            } else {
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<Exception> exception = new AtomicReference<>();

                Platform.runLater(
                        () -> {
                            try {
                                action.run();
                            } catch (Exception e) {
                                exception.set(e);
                            } finally {
                                latch.countDown();
                            }
                        });

                if (!latch.await(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("JavaFX operation timeout");
                }

                if (exception.get() != null) {
                    throw new RuntimeException("JavaFX operation failed", exception.get());
                }
            }
        } catch (IllegalStateException e) {
            // JavaFX not available, run directly
            action.run();
        }
    }

    /** Waits for JavaFX animations and pending updates to complete. */
    protected void waitForFxEvents() throws InterruptedException {
        Thread.sleep(50);
        if (javaFxInitialized.get()) {
            try {
                runAndWait(() -> {});
            } catch (Exception e) {
                // Ignore - JavaFX might not be available
            }
        }
    }

    /** Check if running in a CI/headless environment */
    protected boolean isHeadlessEnvironment() {
        return System.getenv("CI") != null
                || System.getProperty("java.awt.headless", "false").equals("true")
                || java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadless();
    }

    /** Skip test if in headless environment and JavaFX is required */
    protected void skipIfHeadless(TestInfo testInfo) {
        if (isHeadlessEnvironment()) {
            System.out.println(
                    "Skipping test in headless environment: " + testInfo.getDisplayName());
            org.junit.jupiter.api.Assumptions.assumeFalse(true, "Test requires display");
        }
    }
}
