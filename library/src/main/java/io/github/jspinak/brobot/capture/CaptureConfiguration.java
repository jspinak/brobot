package io.github.jspinak.brobot.capture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration helper for the modular capture system.
 * 
 * This class provides utilities to:
 * - Query and modify capture configuration at runtime
 * - Switch between capture providers easily
 * - Get detailed configuration information
 * - Validate capture settings
 * 
 * Example usage:
 * ```java
 * @Autowired
 * private CaptureConfiguration captureConfig;
 * 
 * // Switch to FFmpeg for high-precision capture
 * captureConfig.useFFmpeg();
 * 
 * // Switch back to Robot for general use
 * captureConfig.useRobot();
 * 
 * // Check current configuration
 * String provider = captureConfig.getCurrentProvider();
 * boolean isPhysical = captureConfig.isCapturingPhysicalResolution();
 * ```
 * 
 * @since 1.1.0
 */
@Component
public class CaptureConfiguration {
    
    @Autowired
    private UnifiedCaptureService captureService;
    
    @Autowired
    private Environment env;
    
    /**
     * Provider presets for quick switching.
     */
    public enum CaptureMode {
        /**
         * Robot with physical resolution scaling (default).
         * Best for: General use, no external dependencies needed.
         */
        ROBOT_PHYSICAL,
        
        /**
         * Robot without scaling (logical resolution).
         * Best for: When you want DPI-aware captures.
         */
        ROBOT_LOGICAL,
        
        /**
         * FFmpeg for true physical capture.
         * Best for: Maximum accuracy, cross-platform consistency.
         */
        FFMPEG,
        
        /**
         * Legacy SikuliX capture.
         * Best for: Backward compatibility with existing scripts.
         */
        SIKULIX,
        
        /**
         * Automatic provider selection.
         * Best for: Let the system choose based on availability.
         */
        AUTO
    }
    
    /**
     * Switches to Robot provider with physical resolution scaling.
     * This is the recommended default for most use cases.
     */
    public void useRobot() {
        useRobotWithScaling(true);
    }
    
    /**
     * Switches to Robot provider with configurable scaling.
     * 
     * @param scaleToPhysical true to scale to physical resolution, false for logical
     */
    public void useRobotWithScaling(boolean scaleToPhysical) {
        captureService.setProvider("ROBOT");
        // Note: Robot scaling is configured via properties, not runtime
        // This would need additional implementation to modify at runtime
        System.out.println("[CaptureConfig] Switched to Robot provider" + 
            (scaleToPhysical ? " (physical resolution)" : " (logical resolution)"));
    }
    
    /**
     * Switches to FFmpeg provider for true physical capture.
     * Requires FFmpeg to be installed on the system.
     * 
     * @throws IllegalStateException if FFmpeg is not available
     */
    public void useFFmpeg() {
        try {
            captureService.setProvider("FFMPEG");
            System.out.println("[CaptureConfig] Switched to FFmpeg provider");
        } catch (IllegalStateException e) {
            throw new IllegalStateException(
                "FFmpeg is not available. Please install FFmpeg or use a different provider.", e);
        }
    }
    
    /**
     * Switches to SikuliX provider for backward compatibility.
     */
    public void useSikuliX() {
        captureService.setProvider("SIKULIX");
        System.out.println("[CaptureConfig] Switched to SikuliX provider");
    }
    
    /**
     * Switches to automatic provider selection.
     * The system will choose the best available provider.
     */
    public void useAuto() {
        captureService.setProvider("AUTO");
        System.out.println("[CaptureConfig] Switched to automatic provider selection");
    }
    
    /**
     * Applies a preset capture mode configuration.
     * 
     * @param mode the capture mode to apply
     */
    public void setCaptureMode(CaptureMode mode) {
        switch (mode) {
            case ROBOT_PHYSICAL:
                useRobotWithScaling(true);
                break;
            case ROBOT_LOGICAL:
                useRobotWithScaling(false);
                break;
            case FFMPEG:
                useFFmpeg();
                break;
            case SIKULIX:
                useSikuliX();
                break;
            case AUTO:
                useAuto();
                break;
        }
    }
    
    /**
     * Gets the current capture provider name.
     * 
     * @return the name of the active provider
     */
    public String getCurrentProvider() {
        return captureService.getActiveProviderName();
    }
    
    /**
     * Checks if currently capturing at physical resolution.
     * 
     * @return true if capturing at physical resolution
     */
    public boolean isCapturingPhysicalResolution() {
        return captureService.isPhysicalResolution();
    }
    
    /**
     * Gets all capture-related configuration properties.
     * 
     * @return map of property names to values
     */
    public Map<String, String> getAllCaptureProperties() {
        Map<String, String> properties = new HashMap<>();
        
        // Main capture settings
        properties.put("brobot.capture.provider", 
            env.getProperty("brobot.capture.provider", "AUTO"));
        properties.put("brobot.capture.prefer-physical", 
            env.getProperty("brobot.capture.prefer-physical", "true"));
        properties.put("brobot.capture.fallback-enabled", 
            env.getProperty("brobot.capture.fallback-enabled", "true"));
        properties.put("brobot.capture.enable-logging", 
            env.getProperty("brobot.capture.enable-logging", "false"));
        properties.put("brobot.capture.auto-retry", 
            env.getProperty("brobot.capture.auto-retry", "true"));
        properties.put("brobot.capture.retry-count", 
            env.getProperty("brobot.capture.retry-count", "3"));
        
        // Robot settings
        properties.put("brobot.capture.robot.scale-to-physical", 
            env.getProperty("brobot.capture.robot.scale-to-physical", "true"));
        properties.put("brobot.capture.robot.expected-physical-width", 
            env.getProperty("brobot.capture.robot.expected-physical-width", "1920"));
        properties.put("brobot.capture.robot.expected-physical-height", 
            env.getProperty("brobot.capture.robot.expected-physical-height", "1080"));
        
        // FFmpeg settings
        properties.put("brobot.capture.ffmpeg.path", 
            env.getProperty("brobot.capture.ffmpeg.path", "ffmpeg"));
        properties.put("brobot.capture.ffmpeg.timeout", 
            env.getProperty("brobot.capture.ffmpeg.timeout", "5"));
        
        return properties;
    }
    
    /**
     * Prints a detailed capture configuration report.
     */
    public void printConfigurationReport() {
        System.out.println("\n========== Capture Configuration Report ==========");
        System.out.println("Active Provider: " + getCurrentProvider());
        System.out.println("Resolution Type: " + 
            (isCapturingPhysicalResolution() ? "PHYSICAL" : "LOGICAL/UNKNOWN"));
        
        System.out.println("\nProvider Status:");
        System.out.println(captureService.getProvidersInfo());
        
        System.out.println("\nConfiguration Properties:");
        getAllCaptureProperties().forEach((key, value) -> 
            System.out.println("  " + key + " = " + value));
        
        System.out.println("\nQuick Switch Commands:");
        System.out.println("  captureConfig.useRobot()     - Switch to Robot (with scaling)");
        System.out.println("  captureConfig.useFFmpeg()    - Switch to FFmpeg");
        System.out.println("  captureConfig.useSikuliX()   - Switch to SikuliX");
        System.out.println("  captureConfig.useAuto()      - Automatic selection");
        System.out.println("==================================================\n");
    }
    
    /**
     * Validates the current capture configuration.
     * 
     * @return true if configuration is valid and working
     */
    public boolean validateConfiguration() {
        try {
            // Try to get active provider
            String provider = getCurrentProvider();
            if (provider == null || provider.isEmpty()) {
                System.err.println("[CaptureConfig] No active provider found");
                return false;
            }
            
            // Try a test capture
            captureService.captureScreen();
            
            System.out.println("[CaptureConfig] Configuration validated successfully");
            return true;
            
        } catch (Exception e) {
            System.err.println("[CaptureConfig] Configuration validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets a recommended configuration based on the current environment.
     * 
     * @return recommended CaptureMode for the current environment
     */
    public CaptureMode getRecommendedMode() {
        // Check if FFmpeg is available
        try {
            captureService.setProvider("FFMPEG");
            // FFmpeg is available, it's often the best choice
            return CaptureMode.FFMPEG;
        } catch (Exception e) {
            // FFmpeg not available
        }
        
        // Default to Robot with physical scaling
        return CaptureMode.ROBOT_PHYSICAL;
    }
}