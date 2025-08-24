package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.util.DPIScalingDetector;
import org.sikuli.basics.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * Configures Brobot to handle DPI scaling issues automatically.
 * This component detects the system's display scaling and configures
 * SikuliX settings to compensate for any scaling mismatches.
 */
@Component
public class BrobotDPIConfig {
    
    @Value("${brobot.dpi.auto-detect:true}")
    private boolean autoDetectScaling;
    
    @Value("${brobot.dpi.override-scale:0}")
    private float overrideScale;
    
    @PostConstruct
    public void configureDPIScaling() {
        if (overrideScale > 0) {
            // User has specified a manual scaling factor
            Settings.AlwaysResize = overrideScale;
            System.out.println("[BROBOT DPI] Using manual scaling factor: " + overrideScale);
            return;
        }
        
        if (autoDetectScaling) {
            // Automatically detect and apply scaling
            float detectedScale = DPIScalingDetector.detectScalingFactor();
            
            if (DPIScalingDetector.hasScaling()) {
                Settings.AlwaysResize = detectedScale;
                System.out.println("[BROBOT DPI] " + DPIScalingDetector.getScalingDescription());
                System.out.println("[BROBOT DPI] Settings.AlwaysResize set to: " + detectedScale);
            } else {
                System.out.println("[BROBOT DPI] No display scaling detected - using default settings");
            }
        }
    }
    
    /**
     * Manually sets the DPI scaling factor.
     * Useful for testing or when auto-detection doesn't work correctly.
     * 
     * @param scaleFactor The scaling factor to use (e.g., 0.8 for 125% scaling)
     */
    public void setManualScaling(float scaleFactor) {
        Settings.AlwaysResize = scaleFactor;
        System.out.println("[BROBOT DPI] Manually set scaling to: " + scaleFactor);
    }
    
    /**
     * Resets to auto-detected scaling.
     */
    public void resetToAutoScaling() {
        float detectedScale = DPIScalingDetector.detectScalingFactor();
        Settings.AlwaysResize = detectedScale;
        System.out.println("[BROBOT DPI] Reset to auto-detected scaling: " + detectedScale);
    }
    
    /**
     * Gets the current scaling configuration.
     * @return Description of current scaling settings
     */
    public String getCurrentScalingInfo() {
        return String.format("AlwaysResize: %.2f, Auto-detect: %s, Override: %.2f",
                           Settings.AlwaysResize, autoDetectScaling, overrideScale);
    }
}