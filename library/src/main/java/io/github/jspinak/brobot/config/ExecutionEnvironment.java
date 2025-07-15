package io.github.jspinak.brobot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.awt.GraphicsEnvironment;

/**
 * Manages Brobot's runtime environment configuration.
 * 
 * <p>This class provides a clear separation between different environmental concerns:
 * <ul>
 *   <li>Mock mode - Use fake data for testing workflows</li>
 *   <li>Display availability - Whether screen capture operations are possible</li>
 *   <li>Headless mode - Running without a GUI (can still process files)</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * // For integration tests in CI/CD
 * ExecutionEnvironment env = ExecutionEnvironment.builder()
 *     .mockMode(false)  // Use real data
 *     .forceHeadless(true)  // But no screen capture
 *     .build();
 * 
 * // For unit tests
 * ExecutionEnvironment env = ExecutionEnvironment.builder()
 *     .mockMode(true)  // Use mock data
 *     .build();
 * 
 * // For production with auto-detection
 * ExecutionEnvironment env = ExecutionEnvironment.builder()
 *     .build();  // Auto-detects display availability
 * }</pre>
 */
@Slf4j
@Component
public class ExecutionEnvironment {
    
    private static ExecutionEnvironment instance = new ExecutionEnvironment();
    
    private boolean mockMode = false;
    private Boolean forceHeadless = null; // null means auto-detect
    private boolean allowScreenCapture = true;
    private boolean verboseLogging = false;
    
    private ExecutionEnvironment() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of ExecutionEnvironment.
     * 
     * @return the ExecutionEnvironment instance
     */
    public static ExecutionEnvironment getInstance() {
        return instance;
    }
    
    /**
     * Resets the environment to a new configuration.
     * 
     * @param newInstance the new configuration
     */
    public static void setInstance(ExecutionEnvironment newInstance) {
        instance = newInstance;
    }
    
    /**
     * Checks if Brobot is running in mock mode.
     * In mock mode, operations return predefined fake data instead of
     * performing real actions.
     * 
     * @return true if in mock mode
     */
    public boolean isMockMode() {
        return mockMode;
    }
    
    /**
     * Checks if a display is available for GUI operations.
     * This determines whether screen capture and GUI automation are possible.
     * 
     * @return true if display is available
     */
    public boolean hasDisplay() {
        // If explicitly set to headless, return false
        if (forceHeadless != null) {
            return !forceHeadless;
        }
        
        // Auto-detect display availability
        if (GraphicsEnvironment.isHeadless()) {
            return false;
        }
        
        // Check OS type
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("windows");
        boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null || 
                       System.getenv("WSL_INTEROP") != null;
        
        // For Windows (not WSL), display is generally available
        if (isWindows && !isWSL) {
            // Windows has display unless running in CI
            return !isRunningInCI();
        }
        
        // Check for DISPLAY variable on Unix-like systems (including WSL)
        if (os.contains("nix") || os.contains("nux") || os.contains("mac") || isWSL) {
            String display = System.getenv("DISPLAY");
            if (display == null || display.isEmpty()) {
                return false;
            }
        }
        
        // Check for common CI environment variables
        if (isRunningInCI()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if screen capture operations are allowed.
     * This requires both display availability and permission to capture.
     * 
     * @return true if screen capture is allowed
     */
    public boolean canCaptureScreen() {
        return hasDisplay() && allowScreenCapture && !mockMode;
    }
    
    /**
     * Checks if image file operations should use real files.
     * File operations work in headless mode but not in mock mode.
     * 
     * @return true if real file operations should be used
     */
    public boolean useRealFiles() {
        return !mockMode;
    }
    
    /**
     * Checks if SikuliX operations should be skipped.
     * This is true when either in mock mode or when display is not available.
     * 
     * @return true if SikuliX operations should be skipped
     */
    public boolean shouldSkipSikuliX() {
        return mockMode || !hasDisplay();
    }
    
    /**
     * Gets a descriptive string of the current environment configuration.
     * 
     * @return environment description
     */
    public String getEnvironmentInfo() {
        return String.format(
            "ExecutionEnvironment[mockMode=%s, hasDisplay=%s, canCaptureScreen=%s, useRealFiles=%s]",
            mockMode, hasDisplay(), canCaptureScreen(), useRealFiles()
        );
    }
    
    /**
     * Checks common CI/CD environment variables.
     */
    private boolean isRunningInCI() {
        return System.getenv("CI") != null ||
               System.getenv("CONTINUOUS_INTEGRATION") != null ||
               System.getenv("JENKINS_URL") != null ||
               System.getenv("GITHUB_ACTIONS") != null ||
               System.getenv("GITLAB_CI") != null ||
               System.getenv("CIRCLECI") != null ||
               System.getenv("TRAVIS") != null ||
               System.getenv("APPVEYOR") != null ||
               System.getenv("DRONE") != null ||
               System.getenv("TEAMCITY_VERSION") != null;
    }
    
    /**
     * Builder for creating custom ExecutionEnvironment configurations.
     */
    public static class Builder {
        private boolean mockMode = false;
        private Boolean forceHeadless = null;
        private boolean allowScreenCapture = true;
        private boolean verboseLogging = false;
        
        /**
         * Sets mock mode. In mock mode, all operations return fake data.
         * 
         * @param mockMode true to enable mock mode
         * @return this builder
         */
        public Builder mockMode(boolean mockMode) {
            this.mockMode = mockMode;
            return this;
        }
        
        /**
         * Forces headless mode regardless of actual display availability.
         * Useful for testing headless behavior on systems with displays.
         * 
         * @param forceHeadless true to force headless, false to force display mode, 
         *                      null to auto-detect
         * @return this builder
         */
        public Builder forceHeadless(Boolean forceHeadless) {
            this.forceHeadless = forceHeadless;
            return this;
        }
        
        /**
         * Sets whether screen capture is allowed even when display is available.
         * 
         * @param allowScreenCapture true to allow screen capture
         * @return this builder
         */
        public Builder allowScreenCapture(boolean allowScreenCapture) {
            this.allowScreenCapture = allowScreenCapture;
            return this;
        }
        
        /**
         * Enables verbose logging of environment decisions.
         * 
         * @param verboseLogging true to enable verbose logging
         * @return this builder
         */
        public Builder verboseLogging(boolean verboseLogging) {
            this.verboseLogging = verboseLogging;
            return this;
        }
        
        /**
         * Creates a configuration from environment variables and system properties.
         * 
         * Environment variables:
         * - BROBOT_MOCK_MODE=true/false
         * - BROBOT_FORCE_HEADLESS=true/false
         * - BROBOT_ALLOW_SCREEN_CAPTURE=true/false
         * 
         * System properties take precedence over environment variables.
         * 
         * @return this builder
         */
        public Builder fromEnvironment() {
            // Check environment variables
            String mockEnv = System.getenv("BROBOT_MOCK_MODE");
            if (mockEnv != null) {
                this.mockMode = Boolean.parseBoolean(mockEnv);
            }
            
            String headlessEnv = System.getenv("BROBOT_FORCE_HEADLESS");
            if (headlessEnv != null) {
                this.forceHeadless = Boolean.parseBoolean(headlessEnv);
            }
            
            String captureEnv = System.getenv("BROBOT_ALLOW_SCREEN_CAPTURE");
            if (captureEnv != null) {
                this.allowScreenCapture = Boolean.parseBoolean(captureEnv);
            }
            
            // System properties override environment variables
            String mockProp = System.getProperty("brobot.mock.mode");
            if (mockProp != null) {
                this.mockMode = Boolean.parseBoolean(mockProp);
            }
            
            String headlessProp = System.getProperty("brobot.force.headless");
            if (headlessProp != null) {
                this.forceHeadless = Boolean.parseBoolean(headlessProp);
            }
            
            String captureProp = System.getProperty("brobot.allow.screen.capture");
            if (captureProp != null) {
                this.allowScreenCapture = Boolean.parseBoolean(captureProp);
            }
            
            return this;
        }
        
        /**
         * Builds the ExecutionEnvironment configuration.
         * 
         * @return configured ExecutionEnvironment
         */
        public ExecutionEnvironment build() {
            ExecutionEnvironment env = new ExecutionEnvironment();
            env.mockMode = this.mockMode;
            env.forceHeadless = this.forceHeadless;
            env.allowScreenCapture = this.allowScreenCapture;
            env.verboseLogging = this.verboseLogging;
            
            if (env.verboseLogging) {
                log.debug("ExecutionEnvironment configured: {}", env.getEnvironmentInfo());
            }
            
            return env;
        }
    }
    
    /**
     * Creates a default builder.
     * 
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}