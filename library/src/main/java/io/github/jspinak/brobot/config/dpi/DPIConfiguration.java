package io.github.jspinak.brobot.config.dpi;

import org.sikuli.basics.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * Configures DPI scaling for Brobot applications.
 * Automatically detects and applies appropriate pattern scaling based on monitor DPI.
 * 
 * This configuration is part of the Brobot library and applies to all Brobot applications.
 */
@Component
@Order(1) // Run early in startup sequence
public class DPIConfiguration {
    
    @Value("${brobot.dpi.resize-factor:auto}")
    private String resizeFactorConfig;
    
    @Value("${brobot.action.similarity:0.70}")
    private double similarityThreshold;
    
    private final DPIAutoDetector dpiDetector;
    
    public DPIConfiguration(DPIAutoDetector dpiDetector) {
        this.dpiDetector = dpiDetector;
    }
    
    @PostConstruct
    public void configureDPIScalingEarly() {
        // Configure DPI scaling IMMEDIATELY after bean construction
        // This ensures Settings.AlwaysResize is set before ANY patterns are loaded
        System.out.println("\n=== Brobot DPI Configuration ===");
        
        // Configure similarity threshold
        Settings.MinSimilarity = similarityThreshold;
        System.out.println("Similarity threshold: " + similarityThreshold);
        
        // Configure DPI scaling
        float resizeFactor = determineResizeFactor();
        Settings.AlwaysResize = resizeFactor;
        
        // Additional optimizations for DPI environments
        Settings.CheckLastSeen = true; // Performance optimization
        
        System.out.println("Pattern resize factor: " + resizeFactor);
        System.out.println("Configuration complete\n");
    }
    
    private float determineResizeFactor() {
        // Check if auto mode is enabled
        if ("auto".equalsIgnoreCase(resizeFactorConfig)) {
            System.out.println("DPI auto-detection enabled");
            float detectedFactor = dpiDetector.detectScalingFactor();
            System.out.println("Status: " + dpiDetector.getScalingDescription());
            return detectedFactor;
        }
        
        // Manual configuration - parse the float value
        try {
            float manualFactor = Float.parseFloat(resizeFactorConfig);
            System.out.println("Using manual resize factor: " + manualFactor);
            return manualFactor;
        } catch (NumberFormatException e) {
            System.err.println("[Brobot] Invalid resize factor: " + resizeFactorConfig + 
                             ", defaulting to 1.0 (no scaling)");
            return 1.0f;
        }
    }
    
    /**
     * Gets the current resize factor being used.
     */
    public float getCurrentResizeFactor() {
        return Settings.AlwaysResize;
    }
    
    /**
     * Manually set the resize factor (overrides auto-detection).
     */
    public void setResizeFactor(float factor) {
        Settings.AlwaysResize = factor;
        System.out.println("[Brobot] Manually set resize factor to: " + factor);
    }
    
    /**
     * Re-run auto-detection and apply the detected factor.
     */
    public void redetectAndApply() {
        float detectedFactor = dpiDetector.detectScalingFactor();
        Settings.AlwaysResize = detectedFactor;
        System.out.println("[Brobot] Re-detected and applied resize factor: " + detectedFactor);
    }
}