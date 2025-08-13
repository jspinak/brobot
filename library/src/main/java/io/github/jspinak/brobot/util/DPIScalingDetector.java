package io.github.jspinak.brobot.util;

import org.sikuli.script.Screen;
import java.awt.*;

/**
 * Detects DPI scaling mismatches between pattern capture and runtime environment.
 * This solves the common problem where patterns captured at one DPI/scaling level
 * fail to match when the system is running at a different scaling level.
 */
public class DPIScalingDetector {
    
    private static Float cachedScaleFactor = null;
    
    /**
     * Detects the scaling factor needed to compensate for DPI differences.
     * 
     * For example:
     * - If patterns were captured at 100% and system is at 125%, returns 0.8 (1/1.25)
     * - If patterns were captured at 125% and system is at 100%, returns 1.25
     * 
     * @return The scaling factor to apply via Settings.AlwaysResize
     */
    public static float detectScalingFactor() {
        if (cachedScaleFactor != null) {
            return cachedScaleFactor;
        }
        
        try {
            // Get logical (scaled) resolution
            Screen sikuliScreen = new Screen();
            Rectangle logicalBounds = sikuliScreen.getBounds();
            int logicalWidth = logicalBounds.width;
            int logicalHeight = logicalBounds.height;
            
            // Get physical (native) resolution
            GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            DisplayMode displayMode = device.getDisplayMode();
            int physicalWidth = displayMode.getWidth();
            int physicalHeight = displayMode.getHeight();
            
            // Calculate scaling factor
            float widthScale = (float) logicalWidth / physicalWidth;
            float heightScale = (float) logicalHeight / physicalHeight;
            
            // They should be the same, but use width as primary
            float systemScale = widthScale;
            
            System.out.println("[DPI DETECTOR] Display Information:");
            System.out.println("  Logical resolution: " + logicalWidth + "x" + logicalHeight);
            System.out.println("  Physical resolution: " + physicalWidth + "x" + physicalHeight);
            System.out.println("  System scaling: " + (1/systemScale) * 100 + "%");
            
            // Common Windows scaling levels and their factors
            // 100% = 1.0, 125% = 0.8, 150% = 0.67, 175% = 0.57
            float detectedFactor = 1.0f;
            
            if (Math.abs(systemScale - 1.0f) < 0.01) {
                // No scaling (100%)
                detectedFactor = 1.0f;
            } else if (Math.abs(systemScale - 0.8f) < 0.01) {
                // 125% scaling - patterns need to be scaled down
                detectedFactor = 0.8f;
            } else if (Math.abs(systemScale - 0.67f) < 0.01) {
                // 150% scaling
                detectedFactor = 0.67f;
            } else if (Math.abs(systemScale - 0.57f) < 0.01) {
                // 175% scaling
                detectedFactor = 0.57f;
            } else {
                // Custom scaling - use exact calculation
                detectedFactor = systemScale;
            }
            
            System.out.println("  Recommended AlwaysResize: " + detectedFactor);
            
            cachedScaleFactor = detectedFactor;
            return detectedFactor;
            
        } catch (Exception e) {
            System.err.println("[DPI DETECTOR] Error detecting scaling: " + e.getMessage());
            return 1.0f; // Default to no scaling
        }
    }
    
    /**
     * Checks if the system has DPI scaling enabled.
     * @return true if scaling is not 100%
     */
    public static boolean hasScaling() {
        float factor = detectScalingFactor();
        return Math.abs(factor - 1.0f) > 0.01;
    }
    
    /**
     * Clears the cached scaling factor, forcing re-detection on next call.
     */
    public static void clearCache() {
        cachedScaleFactor = null;
    }
    
    /**
     * Gets a human-readable description of the current scaling situation.
     * @return Description of the scaling configuration
     */
    public static String getScalingDescription() {
        float factor = detectScalingFactor();
        
        if (Math.abs(factor - 1.0f) < 0.01) {
            return "No scaling detected (100%)";
        } else if (Math.abs(factor - 0.8f) < 0.01) {
            return "125% Windows scaling detected - patterns will be scaled to 80%";
        } else if (Math.abs(factor - 0.67f) < 0.01) {
            return "150% Windows scaling detected - patterns will be scaled to 67%";
        } else if (Math.abs(factor - 0.57f) < 0.01) {
            return "175% Windows scaling detected - patterns will be scaled to 57%";
        } else {
            int percentage = Math.round((1/factor) * 100);
            return percentage + "% scaling detected - patterns will be scaled to " + 
                   Math.round(factor * 100) + "%";
        }
    }
}