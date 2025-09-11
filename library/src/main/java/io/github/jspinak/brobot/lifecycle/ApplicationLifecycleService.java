package io.github.jspinak.brobot.lifecycle;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing application lifecycle events in Brobot applications.
 *
 * <p>This service provides a clean API for applications to:
 *
 * <ul>
 *   <li>Request graceful shutdown
 *   <li>Perform cleanup operations
 *   <li>Handle lifecycle transitions
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Autowired
 * private ApplicationLifecycleService lifecycleService;
 *
 * // When application needs to shutdown
 * lifecycleService.requestShutdown();
 * }</pre>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationLifecycleService {

    private final BrobotShutdownHandler shutdownHandler;

    /**
     * Requests a graceful application shutdown with success exit code.
     *
     * <p>This method initiates the full shutdown sequence including:
     *
     * <ul>
     *   <li>Stopping all active operations
     *   <li>Cleaning up native resources
     *   <li>Closing Spring context
     *   <li>Exiting the JVM
     * </ul>
     */
    public void requestShutdown() {
        requestShutdown(0);
    }

    /**
     * Requests a graceful application shutdown with specified exit code.
     *
     * @param exitCode The exit code to use (0 for success, non-zero for error)
     */
    public void requestShutdown(int exitCode) {
        log.info("Application shutdown requested with exit code: {}", exitCode);
        shutdownHandler.initiateGracefulShutdown(exitCode);
    }

    /**
     * Performs cleanup operations without shutting down. Useful for resource cleanup during
     * application runtime.
     */
    public void performCleanup() {
        log.info("Performing resource cleanup");

        try {
            // Trigger garbage collection to clean up native resources
            System.gc();
            Thread.sleep(200);

            log.info("Resource cleanup completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Cleanup interrupted", e);
        }
    }

    /**
     * Checks if shutdown is currently in progress.
     *
     * @return true if shutdown has been initiated
     */
    public boolean isShutdownInProgress() {
        return shutdownHandler.isShutdownInProgress();
    }
}
