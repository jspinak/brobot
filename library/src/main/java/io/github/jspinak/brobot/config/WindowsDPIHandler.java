package io.github.jspinak.brobot.config;

import org.sikuli.basics.Settings;
import java.awt.*;

/**
 * Handles Windows-specific DPI scaling issues for pattern matching.
 * 
 * On Windows with DPI scaling, even when capturing at physical resolution,
 * the SikuliX Finder returns match coordinates in logical space.
 * This handler detects and compensates for this issue.
 */
public class WindowsDPIHandler {
    
    private static boolean initialized = false;
    private static float detectedScale = 1.0f;
    private static boolean isWindowsWithScaling = false;
    
    /**
     * Initialize DPI handling for Windows.
     * This detects if we're on Windows with DPI scaling and configures
     * Settings.AlwaysResize to compensate.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        System.out.println("\n=== Windows DPI Handler ===");
        
        // Check if we're on Windows
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("windows")) {
            System.out.println("Not Windows - no DPI compensation needed");
            initialized = true;
            return;
        }
        
        // Detect DPI scaling
        try {
            GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            
            DisplayMode mode = device.getDisplayMode();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            
            int physicalWidth = mode.getWidth();
            int physicalHeight = mode.getHeight();
            int logicalWidth = (int) screenSize.getWidth();
            int logicalHeight = (int) screenSize.getHeight();
            
            System.out.println("Physical: " + physicalWidth + "x" + physicalHeight);
            System.out.println("Logical: " + logicalWidth + "x" + logicalHeight);
            
            if (physicalWidth != logicalWidth || physicalHeight != logicalHeight) {
                // DPI scaling is active
                float scaleX = (float) logicalWidth / physicalWidth;
                float scaleY = (float) logicalHeight / physicalHeight;
                
                // Use the average scale (they should be the same)
                detectedScale = (scaleX + scaleY) / 2;
                isWindowsWithScaling = true;
                
                System.out.println("DPI scaling detected: " + (int)(100 / detectedScale) + "%");
                System.out.println("Scale factor: " + detectedScale);
                
                // CRITICAL: Set Settings.AlwaysResize to compensate
                // This makes the Finder scale patterns to match logical coordinates
                Settings.AlwaysResize = detectedScale;
                
                System.out.println("Settings.AlwaysResize set to: " + Settings.AlwaysResize);
                System.out.println("This ensures match dimensions equal search image dimensions");
                
            } else {
                System.out.println("No DPI scaling detected");
                Settings.AlwaysResize = 0;
            }
            
        } catch (Exception e) {
            System.err.println("Error detecting DPI scaling: " + e.getMessage());
            // Default to no scaling
            Settings.AlwaysResize = 0;
        }
        
        initialized = true;
        System.out.println("===========================\n");
    }
    
    /**
     * Get the detected scale factor.
     */
    public static float getScaleFactor() {
        return detectedScale;
    }
    
    /**
     * Check if Windows DPI scaling is active.
     */
    public static boolean isScalingActive() {
        return isWindowsWithScaling;
    }
    
    /**
     * Reset for testing purposes.
     */
    public static void reset() {
        initialized = false;
        detectedScale = 1.0f;
        isWindowsWithScaling = false;
    }
}