package io.github.jspinak.brobot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages Brobot's initialization lifecycle to ensure proper component startup order.
 * This replaces the need for multiple InitializingBean implementations and guarantees
 * that all components are initialized in the correct sequence.
 * 
 * <p>Initialization Order:
 * <ol>
 *   <li>Environment detection and configuration</li>
 *   <li>Image path resolution and SikuliX setup</li>
 *   <li>Resource verification and diagnostics</li>
 *   <li>Framework readiness check</li>
 * </ol>
 */
@Slf4j
@Component
public class BrobotLifecycleManager implements SmartLifecycle, Ordered {
    
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private final BrobotConfiguration configuration;
    private final ExecutionEnvironment environment;
    private final ImagePathManager pathManager;
    private final SmartImageLoader imageLoader;
    private final ConfigurationDiagnostics diagnostics;
    
    @Autowired
    public BrobotLifecycleManager(
            BrobotConfiguration configuration,
            ExecutionEnvironment environment,
            ImagePathManager pathManager,
            SmartImageLoader imageLoader,
            ConfigurationDiagnostics diagnostics) {
        this.configuration = configuration;
        this.environment = environment;
        this.pathManager = pathManager;
        this.imageLoader = imageLoader;
        this.diagnostics = diagnostics;
    }
    
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            log.info("Starting Brobot lifecycle initialization...");
            
            try {
                // Phase 1: Environment Setup
                initializeEnvironment();
                
                // Phase 2: Path Configuration
                initializeImagePaths();
                
                // Phase 3: Verify Configuration
                verifyConfiguration();
                
                // Phase 4: Framework Ready
                logFrameworkReady();
                
            } catch (Exception e) {
                log.error("Failed to initialize Brobot framework", e);
                running.set(false);
                throw new IllegalStateException("Brobot initialization failed", e);
            }
        }
    }
    
    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping Brobot lifecycle...");
            
            // Clean up resources
            imageLoader.clearCache();
            
            log.info("Brobot lifecycle stopped");
        }
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    @Override
    public int getPhase() {
        // Run at the end of the Spring lifecycle
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean isAutoStartup() {
        return true;
    }
    
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
    
    @Override
    public int getOrder() {
        // Ensure we run after all other components
        return Ordered.LOWEST_PRECEDENCE;
    }
    
    /**
     * Phase 1: Initialize environment and apply configuration
     */
    private void initializeEnvironment() {
        log.info("=== Phase 1: Environment Initialization ===");
        
        // Apply configuration to environment
        ExecutionEnvironment configuredEnv = configuration.getExecutionEnvironment();
        ExecutionEnvironment.setInstance(configuredEnv);
        
        // Validate configuration
        configuration.validate();
        
        log.info("Environment configured: {}", configuredEnv.getEnvironmentInfo());
        log.info("Active profile: {}", configuration.getEnvironment().getProfile());
    }
    
    /**
     * Phase 2: Initialize image paths and SikuliX
     */
    private void initializeImagePaths() {
        log.info("=== Phase 2: Image Path Configuration ===");
        
        String primaryPath = configuration.getCore().getImagePath();
        pathManager.initialize(primaryPath);
        
        // Add additional paths
        for (String additionalPath : configuration.getCore().getAdditionalImagePaths()) {
            pathManager.addPath(additionalPath);
        }
        
        // Verify paths contain images
        if (!pathManager.validatePaths()) {
            log.warn("No images found in configured paths");
        }
        
        log.info("Image paths configured: {}", pathManager.getConfiguredPaths());
    }
    
    /**
     * Phase 3: Verify configuration and detect issues
     */
    private void verifyConfiguration() {
        log.info("=== Phase 3: Configuration Verification ===");
        
        if (!configuration.getCore().isVerboseLogging()) {
            // Run quick validation only
            boolean valid = diagnostics.isConfigurationValid();
            if (!valid) {
                log.warn("Configuration issues detected. Run with verbose logging for details.");
            }
        } else {
            // Run full diagnostics
            ConfigurationDiagnostics.DiagnosticReport report = diagnostics.runFullDiagnostics();
            
            if (report.hasErrors()) {
                log.warn("Configuration issues detected:");
                log.warn(report.toFormattedString());
            } else {
                log.info("Configuration validated successfully");
            }
        }
    }
    
    /**
     * Phase 4: Log framework readiness
     */
    private void logFrameworkReady() {
        log.info("=== Phase 4: Framework Ready ===");
        
        StringBuilder status = new StringBuilder();
        status.append("\nBrobot Framework Status:\n");
        status.append("  - Environment: ").append(configuration.getEnvironment().getProfile()).append("\n");
        status.append("  - Mock Mode: ").append(environment.isMockMode()).append("\n");
        status.append("  - Display Available: ").append(environment.hasDisplay()).append("\n");
        status.append("  - Screen Capture: ").append(environment.canCaptureScreen()).append("\n");
        status.append("  - Image Paths: ").append(pathManager.getConfiguredPaths().size()).append(" configured\n");
        status.append("  - SikuliX: ").append(!environment.shouldSkipSikuliX() ? "Active" : "Skipped").append("\n");
        
        log.info(status.toString());
        log.info("Brobot framework initialization complete");
    }
    
    /**
     * Check if the framework is fully initialized and ready for use
     */
    public boolean isFrameworkReady() {
        return running.get() && pathManager.getConfiguredPaths().size() > 0;
    }
    
    /**
     * Get a status report of the current framework state
     */
    public FrameworkStatus getStatus() {
        FrameworkStatus status = new FrameworkStatus();
        status.setRunning(running.get());
        status.setProfile(configuration.getEnvironment().getProfile());
        status.setMockMode(environment.isMockMode());
        status.setDisplayAvailable(environment.hasDisplay());
        status.setScreenCaptureAvailable(environment.canCaptureScreen());
        status.setImagePathsConfigured(pathManager.getConfiguredPaths().size());
        status.setSikulixActive(!environment.shouldSkipSikuliX());
        return status;
    }
    
    /**
     * Framework status information
     */
    public static class FrameworkStatus {
        private boolean running;
        private String profile;
        private boolean mockMode;
        private boolean displayAvailable;
        private boolean screenCaptureAvailable;
        private int imagePathsConfigured;
        private boolean sikulixActive;
        
        // Getters and setters
        public boolean isRunning() { return running; }
        public void setRunning(boolean running) { this.running = running; }
        
        public String getProfile() { return profile; }
        public void setProfile(String profile) { this.profile = profile; }
        
        public boolean isMockMode() { return mockMode; }
        public void setMockMode(boolean mockMode) { this.mockMode = mockMode; }
        
        public boolean isDisplayAvailable() { return displayAvailable; }
        public void setDisplayAvailable(boolean displayAvailable) { this.displayAvailable = displayAvailable; }
        
        public boolean isScreenCaptureAvailable() { return screenCaptureAvailable; }
        public void setScreenCaptureAvailable(boolean screenCaptureAvailable) { 
            this.screenCaptureAvailable = screenCaptureAvailable; 
        }
        
        public int getImagePathsConfigured() { return imagePathsConfigured; }
        public void setImagePathsConfigured(int imagePathsConfigured) { 
            this.imagePathsConfigured = imagePathsConfigured; 
        }
        
        public boolean isSikulixActive() { return sikulixActive; }
        public void setSikulixActive(boolean sikulixActive) { this.sikulixActive = sikulixActive; }
        
        @Override
        public String toString() {
            return String.format(
                "FrameworkStatus[running=%s, profile=%s, mockMode=%s, display=%s, capture=%s, paths=%d, sikuli=%s]",
                running, profile, mockMode, displayAvailable, screenCaptureAvailable, 
                imagePathsConfigured, sikulixActive
            );
        }
    }
}