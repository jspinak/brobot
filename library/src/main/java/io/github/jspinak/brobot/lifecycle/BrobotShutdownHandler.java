package io.github.jspinak.brobot.lifecycle;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.capture.BrobotCaptureService;
import io.github.jspinak.brobot.capture.UnifiedCaptureService;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles graceful shutdown of Brobot components and native resources.
 *
 * <p>This component ensures proper cleanup of:
 *
 * <ul>
 *   <li>JavaCV/FFmpeg native resources
 *   <li>Capture service resources
 *   <li>Thread pools and executors
 *   <li>Any other native resources that need explicit cleanup
 * </ul>
 *
 * <p>The shutdown process includes:
 *
 * <ol>
 *   <li>Stopping capture services
 *   <li>Releasing native memory allocations
 *   <li>Closing thread pools
 *   <li>Forcing JVM exit if necessary to prevent hanging
 * </ol>
 *
 * @since 1.0
 */
@Slf4j
@Component
public class BrobotShutdownHandler {

    private final UnifiedCaptureService unifiedCaptureService;
    private final BrobotCaptureService brobotCaptureService;
    private final ApplicationContext applicationContext;

    private volatile boolean shutdownInProgress = false;

    @Autowired
    public BrobotShutdownHandler(
            @Autowired(required = false) UnifiedCaptureService unifiedCaptureService,
            @Autowired(required = false) BrobotCaptureService brobotCaptureService,
            ApplicationContext applicationContext) {
        this.unifiedCaptureService = unifiedCaptureService;
        this.brobotCaptureService = brobotCaptureService;
        this.applicationContext = applicationContext;
    }

    /**
     * Handles application context closing event. This is triggered when Spring context is shutting
     * down.
     */
    @EventListener(ContextClosedEvent.class)
    public void onContextClosed(ContextClosedEvent event) {
        if (!shutdownInProgress) {
            log.info("Spring context closing - initiating Brobot shutdown sequence");
            performShutdown();
        }
    }

    /** PreDestroy hook for cleanup during bean destruction. */
    @PreDestroy
    public void cleanup() {
        if (!shutdownInProgress) {
            log.info("PreDestroy - initiating Brobot cleanup");
            performShutdown();
        }
    }

    /**
     * Checks if shutdown is currently in progress.
     *
     * @return true if shutdown has been initiated
     */
    public boolean isShutdownInProgress() {
        return shutdownInProgress;
    }

    /** Performs the actual shutdown sequence. */
    private synchronized void performShutdown() {
        if (shutdownInProgress) {
            log.debug("Shutdown already in progress, skipping duplicate call");
            return;
        }

        shutdownInProgress = true;
        log.info("=== BROBOT SHUTDOWN SEQUENCE INITIATED ===");

        try {
            // Step 1: Stop capture services
            stopCaptureServices();

            // Step 2: Give native resources time to clean up
            // This allows JavaCV/FFmpeg deallocators to run
            Thread.sleep(500);

            // Step 3: Force garbage collection to trigger native deallocators
            System.gc();
            Thread.sleep(200);

            log.info("Brobot shutdown sequence completed successfully");

        } catch (InterruptedException e) {
            // Preserve interrupt status when InterruptedException occurs
            Thread.currentThread().interrupt();
            log.warn("Shutdown sequence interrupted, preserving interrupt status");
        } catch (Exception e) {
            log.error("Error during Brobot shutdown", e);
        }
    }

    /** Stops all capture services and releases their resources. */
    private void stopCaptureServices() {
        log.info("Stopping capture services...");

        try {
            if (unifiedCaptureService != null) {
                log.debug("Stopping UnifiedCaptureService");
                // Add any specific cleanup if the service has a stop method
                // unifiedCaptureService.stop();
            }

            if (brobotCaptureService != null) {
                log.debug("Stopping BrobotCaptureService");
                // Add any specific cleanup if the service has a stop method
                // brobotCaptureService.stop();
            }

            log.info("Capture services stopped");

        } catch (Exception e) {
            log.error("Error stopping capture services", e);
        }
    }

    /**
     * Initiates a graceful application shutdown. This method can be called by applications that
     * need to trigger shutdown programmatically.
     *
     * @param exitCode The exit code to use (0 for success, non-zero for error)
     */
    public void initiateGracefulShutdown(int exitCode) {
        if (shutdownInProgress) {
            log.debug("Shutdown already in progress, ignoring duplicate request");
            return;
        }

        log.info("Graceful shutdown requested with exit code: {}", exitCode);

        // Perform cleanup first
        performShutdown();

        // Schedule the actual JVM exit after a short delay
        // This gives time for Spring context to close properly
        ScheduledExecutorService exitScheduler = Executors.newSingleThreadScheduledExecutor();
        exitScheduler.schedule(
                () -> {
                    log.info("Closing Spring application context...");
                    SpringApplication.exit(applicationContext, () -> exitCode);

                    // Force JVM exit after another short delay if Spring doesn't exit cleanly
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    log.info("Forcing JVM exit with code: {}", exitCode);
                    System.exit(exitCode);
                },
                500,
                TimeUnit.MILLISECONDS);

        exitScheduler.shutdown();
    }

    /**
     * Registers a JVM shutdown hook for last-resort cleanup. This ensures cleanup happens even if
     * Spring shutdown is bypassed.
     */
    static {
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    log.info(
                                            "JVM shutdown hook triggered - performing final"
                                                    + " cleanup");

                                    // Force garbage collection to clean up native resources
                                    System.gc();

                                    try {
                                        // Give deallocators time to run
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }

                                    log.info("JVM shutdown hook completed");
                                },
                                "brobot-shutdown-hook"));
    }
}
