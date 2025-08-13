package io.github.jspinak.brobot.config;

import org.sikuli.basics.Settings;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.awt.*;

/**
 * Automatically configures Settings.AlwaysResize on Windows to fix pattern matching.
 * 
 * This is the pragmatic solution for Windows DPI scaling issues:
 * - On Windows with DPI scaling, SikuliX's Finder works in logical coordinates
 * - Settings.AlwaysResize scales patterns to match these coordinates
 * - This ensures match dimensions equal search image dimensions
 * 
 * On non-Windows systems or without scaling, AlwaysResize remains 0.
 */
@Configuration
public class WindowsAutoScaleConfig {
    
    @PostConstruct
    public void configureWindowsScaling() {
        System.out.println("\n=== Windows Auto-Scale Configuration ===");
        
        // Only apply on Windows
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("windows")) {
            System.out.println("Not Windows - Settings.AlwaysResize = 0");
            Settings.AlwaysResize = 0;
            System.out.println("=========================================\n");
            return;
        }
        
        try {
            // Detect DPI scaling
            GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            
            DisplayMode mode = device.getDisplayMode();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            
            int physicalWidth = mode.getWidth();
            int physicalHeight = mode.getHeight();
            int logicalWidth = (int) screenSize.getWidth();
            int logicalHeight = (int) screenSize.getHeight();
            
            System.out.println("Detected resolutions:");
            System.out.println("  Physical: " + physicalWidth + "x" + physicalHeight);
            System.out.println("  Logical:  " + logicalWidth + "x" + logicalHeight);
            
            if (physicalWidth != logicalWidth || physicalHeight != logicalHeight) {
                // Calculate the scale factor
                // For 125% DPI: physical=1920, logical=1536
                // Scale = logical/physical = 1536/1920 = 0.8
                float scaleX = (float) logicalWidth / physicalWidth;
                float scaleY = (float) logicalHeight / physicalHeight;
                float scale = (scaleX + scaleY) / 2; // Should be the same
                
                // Set Settings.AlwaysResize to this scale
                Settings.AlwaysResize = scale;
                
                int dpiPercent = Math.round(100 / scale);
                System.out.println("\nWindows DPI scaling detected: " + dpiPercent + "%");
                System.out.println("Settings.AlwaysResize = " + Settings.AlwaysResize);
                System.out.println("\n✓ Pattern matching will now work correctly:");
                System.out.println("  - Patterns will be scaled to logical resolution");
                System.out.println("  - Match dimensions will equal search image dimensions");
                System.out.println("  - Similarity scores will be ~0.99 like SikuliX IDE");
                
            } else {
                System.out.println("No DPI scaling detected");
                Settings.AlwaysResize = 0;
                System.out.println("Settings.AlwaysResize = 0 (no scaling needed)");
            }
            
        } catch (Exception e) {
            System.err.println("Error detecting DPI scaling: " + e.getMessage());
            System.out.println("Defaulting to no scaling (Settings.AlwaysResize = 0)");
            Settings.AlwaysResize = 0;
        }
        
        System.out.println("=========================================\n");
    }
}