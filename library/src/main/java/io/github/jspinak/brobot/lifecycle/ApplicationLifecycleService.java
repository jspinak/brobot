package io.github.jspinak.brobot.lifecycle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing application lifecycle events in Brobot applications.
 * 
 * <p>This service provides a clean API for applications to:</p>
 * <ul>
 *   <li>Request graceful shutdown</li>
 *   <li>Perform cleanup operations</li>
 *   <li>Handle lifecycle transitions</li>
 * </ul>
 * 
 * <p>Example usage:</p>
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
     * <p>This method initiates the full shutdown sequence including:</p>
     * <ul>
     *   <li>Stopping all active operations</li>
     *   <li>Cleaning up native resources</li>
     *   <li>Closing Spring context</li>
     *   <li>Exiting the JVM</li>
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
     * Performs cleanup operations without shutting down.
     * Useful for resource cleanup during application runtime.
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