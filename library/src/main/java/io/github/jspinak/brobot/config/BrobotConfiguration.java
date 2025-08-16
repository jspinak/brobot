package io.github.jspinak.brobot.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified configuration system for Brobot with validation and environment-specific profiles.
 * This replaces scattered configuration with a cohesive, validated system.
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "brobot")
public class BrobotConfiguration {
    
    /**
     * Core configuration settings
     */
    @Data
    public static class CoreConfig {
        
        /**
         * Primary image path for pattern matching
         */
        private String imagePath = "images";
        
        /**
         * Additional image search paths
         */
        private List<String> additionalImagePaths = new ArrayList<>();
        
        /**
         * Enable mock mode for testing without real GUI operations
         */
        private boolean mockMode = false;
        
        /**
         * Force headless mode regardless of display availability
         */
        private Boolean forceHeadless = null;
        
        /**
         * Allow screen capture operations
         */
        private boolean allowScreenCapture = true;
        
        /**
         * Enable verbose logging for debugging
         */
        private boolean verboseLogging = false;
        
        /**
         * Timeout for finding elements (seconds)
         */
        private double findTimeout = 3.0;
        
        /**
         * Default pause between actions (seconds)
         */
        private double actionPause = 0.3;
        
        /**
         * Enable image caching for performance
         */
        private boolean enableImageCache = true;
        
        /**
         * Maximum cache size in MB
         */
        private int maxCacheSizeMB = 100;
    }
    
    /**
     * SikuliX-specific configuration
     */
    @Data
    public static class SikuliConfig {
        
        /**
         * Minimum similarity for pattern matching (0.0 to 1.0)
         */
        private double minSimilarity = 0.7;
        
        /**
         * Wait time for elements to appear (seconds)
         */
        private double waitTime = 3.0;
        
        /**
         * Highlight duration for found elements (seconds)
         */
        private double highlightTime = 2.0;
        
        /**
         * Enable visual debugging features
         */
        private boolean visualDebugging = false;
        
        /**
         * Save screenshots on failures
         */
        private boolean saveFailureScreenshots = true;
        
        /**
         * Screenshot directory
         */
        private String screenshotDirectory = "screenshots";
    }
    
    /**
     * Environment-specific configuration
     */
    @Data
    public static class EnvironmentConfig {
        
        /**
         * Current environment profile
         */
        private String profile = "development";
        
        /**
         * Track if profile was explicitly set
         */
        private boolean profileExplicitlySet = false;
        
        /**
         * CI/CD specific settings
         */
        private boolean ciMode = false;
        
        /**
         * Docker container mode
         */
        private boolean dockerMode = false;
        
        /**
         * Remote automation settings
         */
        private boolean remoteMode = false;
        
        /**
         * Remote server URL
         */
        private String remoteServerUrl;
        
        /**
         * Override setter to track explicit profile setting
         */
        public void setProfile(String profile) {
            this.profile = profile;
            this.profileExplicitlySet = true;
        }
    }
    
    /**
     * Performance tuning configuration
     */
    @Data
    public static class PerformanceConfig {
        
        /**
         * Thread pool size for parallel operations
         */
        private int threadPoolSize = 4;
        
        /**
         * Enable parallel execution
         */
        private boolean enableParallelExecution = false;
        
        /**
         * Maximum retry attempts for failed operations
         */
        private int maxRetryAttempts = 3;
        
        /**
         * Delay between retries (seconds)
         */
        private double retryDelay = 1.0;
        
        /**
         * Enable performance metrics collection
         */
        private boolean collectMetrics = false;
    }
    
    /**
     * Migration configuration for backwards compatibility
     */
    @Data
    public static class MigrationConfig {
        
        /**
         * Enable legacy property support
         */
        private boolean enableLegacySupport = true;
        
        /**
         * Warn about deprecated properties
         */
        private boolean warnDeprecated = true;
        
        /**
         * Property mappings from old to new
         */
        private Map<String, String> propertyMappings = new HashMap<>();
    }
    
    // Main configuration sections
    private CoreConfig core = new CoreConfig();
    private SikuliConfig sikuli = new SikuliConfig();
    private EnvironmentConfig environment = new EnvironmentConfig();
    private PerformanceConfig performance = new PerformanceConfig();
    private MigrationConfig migration = new MigrationConfig();
    
    /**
     * Validate configuration after properties are bound
     */
    public void validate() {
        log.info("Validating Brobot configuration...");
        
        // Auto-detect environment if not explicitly set
        if (!environment.isProfileExplicitlySet()) {
            detectEnvironmentProfile();
        }
        
        // Apply environment-specific defaults
        applyEnvironmentDefaults();
        
        // Validate configuration consistency
        validateConsistency();
        
        // Log final configuration
        if (core.isVerboseLogging()) {
            logConfiguration();
        }
    }
    
    /**
     * Get effective ExecutionEnvironment based on configuration
     */
    public ExecutionEnvironment getExecutionEnvironment() {
        return ExecutionEnvironment.builder()
            .mockMode(core.isMockMode())
            .forceHeadless(core.getForceHeadless())
            .allowScreenCapture(core.isAllowScreenCapture())
            .verboseLogging(core.isVerboseLogging())
            .build();
    }
    
    /**
     * Check if running in a specific profile
     */
    public boolean isProfile(String profile) {
        return environment.getProfile().equalsIgnoreCase(profile);
    }
    
    /**
     * Get configuration for a specific profile
     */
    public void applyProfile(String profile) {
        log.info("Applying configuration profile: {}", profile);
        environment.setProfile(profile);
        environment.setProfileExplicitlySet(true);
        applyEnvironmentDefaults();
    }
    
    private void detectEnvironmentProfile() {
        // Check for CI environment (both env variable and system property)
        if (System.getenv("CI") != null || 
            System.getenv("CONTINUOUS_INTEGRATION") != null ||
            "true".equals(System.getProperty("CI"))) {
            environment.profile = "ci";  // Use field directly to avoid setting the flag
            environment.setCiMode(true);
            log.info("Detected CI environment");
            return;
        }
        
        // Check for Docker
        if (Files.exists(Paths.get("/.dockerenv"))) {
            environment.setDockerMode(true);
            log.info("Detected Docker environment");
        }
        
        // Check for common test indicators
        String classPath = System.getProperty("java.class.path", "");
        if (classPath.contains("test-classes") || classPath.contains("junit")) {
            environment.profile = "testing";  // Use field directly to avoid setting the flag
            log.info("Detected testing environment");
            return;
        }
        
        // Default remains as development
    }
    
    private void applyEnvironmentDefaults() {
        switch (environment.getProfile()) {
            case "ci":
                // CI defaults - no display, fast timeouts
                if (core.getForceHeadless() == null) {
                    core.setForceHeadless(true);
                }
                core.setFindTimeout(1.0);
                core.setActionPause(0.1);
                sikuli.setWaitTime(1.0);
                sikuli.setSaveFailureScreenshots(false);
                break;
                
            case "testing":
                // Testing defaults - mock mode, fast execution
                core.setMockMode(true);
                core.setFindTimeout(0.5);
                core.setActionPause(0.0);
                performance.setEnableParallelExecution(true);
                break;
                
            case "production":
                // Production defaults - conservative settings
                core.setVerboseLogging(false);
                sikuli.setSaveFailureScreenshots(true);
                performance.setMaxRetryAttempts(5);
                performance.setCollectMetrics(true);
                break;
                
            case "development":
                // Development defaults - debugging enabled
                core.setVerboseLogging(true);
                sikuli.setVisualDebugging(true);
                sikuli.setSaveFailureScreenshots(true);
                break;
        }
    }
    
    private void validateConsistency() {
        // Mock mode and screen capture are incompatible
        if (core.isMockMode() && core.isAllowScreenCapture()) {
            log.warn("Mock mode enabled but screen capture allowed - disabling screen capture");
            core.setAllowScreenCapture(false);
        }
        
        // Remote mode requires server URL
        if (environment.isRemoteMode() && 
            (environment.getRemoteServerUrl() == null || environment.getRemoteServerUrl().isEmpty())) {
            throw new IllegalStateException("Remote mode enabled but no server URL provided");
        }
        
        // Parallel execution requires adequate thread pool
        if (performance.isEnableParallelExecution() && performance.getThreadPoolSize() < 2) {
            log.warn("Parallel execution enabled but thread pool size is {}, increasing to 4", 
                    performance.getThreadPoolSize());
            performance.setThreadPoolSize(4);
        }
    }
    
    private void logConfiguration() {
        log.debug("=== Brobot Configuration ===");
        log.debug("Profile: {}", environment.getProfile());
        log.debug("Core: {}", core);
        log.debug("SikuliX: {}", sikuli);
        log.debug("Environment: {}", environment);
        log.debug("Performance: {}", performance);
        log.debug("==========================");
    }
    
    /**
     * Get a diagnostic report of the current configuration
     */
    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("profile", environment.getProfile());
        diagnostics.put("core", core);
        diagnostics.put("sikuli", sikuli);
        diagnostics.put("environment", environment);
        diagnostics.put("performance", performance);
        diagnostics.put("effectiveEnvironment", getExecutionEnvironment().getEnvironmentInfo());
        return diagnostics;
    }
}