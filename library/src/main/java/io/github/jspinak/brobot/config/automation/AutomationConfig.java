package io.github.jspinak.brobot.config.automation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration for automation execution behavior.
 * 
 * <p>Controls how the framework handles automation failures and exceptions.
 * This allows applications to continue running even when automation sequences fail,
 * which is important for long-running services, monitoring applications, or
 * applications that need to perform cleanup after automation failures.
 * 
 * <p>Default behavior is to handle failures gracefully without terminating the application.
 * 
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "brobot.automation")
@Data
public class AutomationConfig {
    
    /**
     * Whether to exit the application when automation fails.
     * Default is false - application continues running after automation failure.
     */
    private boolean exitOnFailure = false;
    
    /**
     * Exit code to use when exitOnFailure is true and automation fails.
     * Default is 1 (standard error exit code).
     */
    private int failureExitCode = 1;
    
    /**
     * Whether to throw exceptions when automation fails.
     * Default is false - failures are logged but not thrown.
     */
    private boolean throwOnFailure = false;
    
    /**
     * Whether to log stack traces for automation failures.
     * Default is true for debugging purposes.
     */
    private boolean logStackTraces = true;
    
    /**
     * Maximum number of automation retry attempts.
     * Default is 0 (no retries).
     */
    private int maxRetries = 0;
    
    /**
     * Delay in milliseconds between retry attempts.
     * Default is 1000ms (1 second).
     */
    private long retryDelayMs = 1000;
    
    /**
     * Whether to continue with remaining automation steps after a failure.
     * Default is false - stop on first failure.
     */
    private boolean continueOnFailure = false;
    
    /**
     * Timeout in seconds for the entire automation sequence.
     * Default is 0 (no timeout).
     */
    private int timeoutSeconds = 0;
}