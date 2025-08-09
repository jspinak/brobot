package io.github.jspinak.brobot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;

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
 * <p><strong>Default Headless Configuration:</strong><br>
 * Brobot automatically sets {@code java.awt.headless=false} during class initialization
 * to enable GUI automation by default. This overrides any existing headless setting
 * (including those set by build tools like Gradle). Applications that specifically need 
 * to preserve the original headless setting can use:
 * <pre>{@code
 * // Preserve original headless setting (e.g., for CI/CD)
 * System.setProperty("brobot.preserve.headless.setting", "true");
 * // Then initialize Brobot...
 * }</pre>
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
    
    static {
        // Set java.awt.headless=false as the default for GUI automation
        // Override even if already set, unless explicitly preserved via system property
        String currentHeadless = System.getProperty("java.awt.headless");
        String preserveHeadless = System.getProperty("brobot.preserve.headless.setting");
        
        if ("true".equals(preserveHeadless)) {
            log.debug("Preserving existing java.awt.headless setting: {}", currentHeadless);
        } else {
            // Always set to false for GUI automation, unless explicitly preserved
            System.setProperty("java.awt.headless", "false");
            if (currentHeadless != null && !"false".equals(currentHeadless)) {
                log.info("Overrode java.awt.headless from '{}' to 'false' for GUI automation. " +
                        "Set -Dbrobot.preserve.headless.setting=true to preserve original setting.", currentHeadless);
            } else {
                log.debug("Set java.awt.headless=false for GUI automation");
            }
        }
    }
    
    private static ExecutionEnvironment instance = new ExecutionEnvironment();
    
    private boolean mockMode = false;
    private Boolean forceHeadless = null; // null means auto-detect
    private boolean allowScreenCapture = true;
    private boolean verboseLogging = false;
    
    // Cache for display check result to avoid repeated checks
    private Boolean cachedHasDisplay = null;
    private long lastDisplayCheckTime = 0;
    private static final long DISPLAY_CHECK_CACHE_DURATION = 60000; // Cache for 1 minute
    
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
     * Results are cached to avoid repeated expensive checks during automation.
     * 
     * @return true if display is available
     */
    public boolean hasDisplay() {
        // Return cached result if still valid
        long currentTime = System.currentTimeMillis();
        if (cachedHasDisplay != null && (currentTime - lastDisplayCheckTime) < DISPLAY_CHECK_CACHE_DURATION) {
            return cachedHasDisplay;
        }
        
        // Perform the actual display check
        boolean result = performDisplayCheck();
        
        // Cache the result
        cachedHasDisplay = result;
        lastDisplayCheckTime = currentTime;
        
        // Log only on first check or when cache expires
        log.debug("[DISPLAY_CHECK] Display availability: {} (cached for {} ms)", 
                result, DISPLAY_CHECK_CACHE_DURATION);
        
        return result;
    }
    
    /**
     * Forces a refresh of the display check cache.
     * Useful when the environment might have changed.
     */
    public void refreshDisplayCheck() {
        cachedHasDisplay = null;
        lastDisplayCheckTime = 0;
        log.debug("[DISPLAY_CHECK] Cache cleared, will perform fresh check on next call");
    }
    
    /**
     * Performs the actual display check without caching.
     * This method contains the original display checking logic.
     * 
     * @return true if display is available
     */
    private boolean performDisplayCheck() {
        // Check OS type first
        String os = System.getProperty("os.name").toLowerCase();
        boolean isMac = os.contains("mac");
        
        // Only log detailed checks on first run or after cache expiry
        if (cachedHasDisplay == null) {
            log.debug("[DISPLAY_CHECK] Performing initial display check - OS: {}, isMac: {}", os, isMac);
        }
        
        // If explicitly set to headless, return false
        if (forceHeadless != null) {
            if (cachedHasDisplay == null) {
                log.debug("[DISPLAY_CHECK] forceHeadless is set to: {}", forceHeadless);
            }
            return !forceHeadless;
        }
        
        // Check if java.awt.headless is explicitly set to false - this overrides detection
        String headlessProp = System.getProperty("java.awt.headless");
        if ("false".equalsIgnoreCase(headlessProp)) {
            if (cachedHasDisplay == null) {
                log.debug("[DISPLAY_CHECK] java.awt.headless explicitly set to false, assuming display available");
            }
            return true;
        }
        
        // Special handling for macOS - check actual GraphicsEnvironment state
        if (isMac) {
            boolean notInCI = !isRunningInCI();
            
            // Even on macOS not in CI, we need to verify actual display state
            // Check if GraphicsEnvironment is actually headless
            try {
                boolean javaHeadless = GraphicsEnvironment.isHeadless();
                
                if (javaHeadless) {
                    if (cachedHasDisplay == null) {
                        log.debug("[DISPLAY_CHECK] macOS: Java reports headless, cannot use display");
                    }
                    return false;
                }
                
                if (notInCI) {
                    if (cachedHasDisplay == null) {
                        log.debug("[DISPLAY_CHECK] macOS: Not in CI and not headless, assuming display available");
                    }
                    return true;
                }
            } catch (Exception e) {
                if (cachedHasDisplay == null) {
                    log.debug("[DISPLAY_CHECK] macOS: Error checking GraphicsEnvironment: {}", e.getMessage());
                }
                return false;
            }
            
            // If in CI, try to check screen devices but don't fail if headless
            try {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                boolean hasScreens = ge.getScreenDevices().length > 0;
                if (cachedHasDisplay == null) {
                    log.debug("[DISPLAY_CHECK] macOS CI: hasScreens={}", hasScreens);
                }
                return hasScreens;
            } catch (HeadlessException e) {
                if (cachedHasDisplay == null) {
                    log.debug("[DISPLAY_CHECK] macOS CI: HeadlessException caught, assuming no display in CI");
                }
                return false;
            } catch (Exception e) {
                log.error("[DISPLAY_CHECK] macOS CI: Unexpected error checking display: ", e);
                return false;
            }
        }
        
        // For non-Mac systems, check GraphicsEnvironment
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        
        if (isHeadless) {
            if (cachedHasDisplay == null) {
                log.debug("[DISPLAY_CHECK] GraphicsEnvironment.isHeadless(): true");
            }
            return false;
        }
        
        // Check other OS types
        boolean isWindows = os.contains("windows");
        boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null || 
                       System.getenv("WSL_INTEROP") != null;
        
        // For Windows (not WSL), display is generally available
        if (isWindows && !isWSL) {
            // Windows has display unless running in CI
            return !isRunningInCI();
        }
        
        
        // Check for DISPLAY variable on Unix-like systems (including WSL)
        if (os.contains("nix") || os.contains("nux") || isWSL) {
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
        boolean hasDisp = hasDisplay();
        boolean result = hasDisp && allowScreenCapture && !mockMode;
        
        if (!result) {
            log.debug("[CAPTURE_CHECK] Cannot capture screen - hasDisplay: {}, allowScreenCapture: {}, mockMode: {}", 
                    hasDisp, allowScreenCapture, mockMode);
        }
        
        return result;
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
            
            // Reset cache for new environment
            env.cachedHasDisplay = null;
            env.lastDisplayCheckTime = 0;
            
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