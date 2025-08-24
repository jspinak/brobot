package io.github.jspinak.brobot.config;

import org.sikuli.basics.Settings;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.awt.*;

/**
 * Configures DPI scaling compensation for pattern matching.
 * 
 * This solves the issue where SikuliX IDE achieves 0.99 similarity
 * but Brobot only achieves 0.70-0.71 due to DPI scaling mismatches.
 * 
 * The issue occurs when:
 * - Windows has DPI scaling enabled (e.g., 125%, 150%)
 * - Patterns were captured at a different resolution than runtime
 * - Java 21 captures at logical resolution while patterns are at physical resolution
 */
@Configuration
public class BrobotDPIConfiguration {
    
    @PostConstruct
    public void configureDPIScaling() {
        System.out.println("=== Brobot DPI Configuration ===");
        
        try {
            // Get the default screen device
            GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            
            // Get physical display mode
            DisplayMode displayMode = device.getDisplayMode();
            int physicalWidth = displayMode.getWidth();
            int physicalHeight = displayMode.getHeight();
            
            // Get logical screen size (what Java sees)
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int logicalWidth = (int) screenSize.getWidth();
            int logicalHeight = (int) screenSize.getHeight();
            
            System.out.println("Physical resolution: " + physicalWidth + "x" + physicalHeight);
            System.out.println("Logical resolution: " + logicalWidth + "x" + logicalHeight);
            
            // Check if DPI scaling is active
            if (physicalWidth > 0 && logicalWidth > 0 && physicalWidth != logicalWidth) {
                // Calculate the scaling factor
                float widthScale = (float) logicalWidth / physicalWidth;
                float heightScale = (float) logicalHeight / physicalHeight;
                
                // Use the average if they differ (shouldn't happen normally)
                float scaleFactor = (widthScale + heightScale) / 2;
                
                // Determine the compensation needed
                if (scaleFactor < 1.0f) {
                    // Screen is scaled up (e.g., 125% = 0.8 factor)
                    // We need to scale patterns DOWN to match
                    Settings.AlwaysResize = scaleFactor;
                    
                    System.out.println("DPI scaling detected:");
                    System.out.println("  Scaling factor: " + (int)((1/scaleFactor) * 100) + "%");
                    System.out.println("  Settings.AlwaysResize set to: " + scaleFactor);
                    System.out.println("  Pattern matching accuracy should now improve from ~0.70 to ~0.94");
                } else if (scaleFactor > 1.0f) {
                    // Screen is scaled down (rare)
                    Settings.AlwaysResize = scaleFactor;
                    System.out.println("Reverse DPI scaling detected:");
                    System.out.println("  Settings.AlwaysResize set to: " + scaleFactor);
                } else {
                    System.out.println("No DPI scaling detected (100% scale)");
                    Settings.AlwaysResize = 1.0f;
                }
            } else {
                // Cannot determine scaling or no scaling present
                System.out.println("DPI scaling detection inconclusive");
                
                // Check if we're in a known problematic environment
                String osName = System.getProperty("os.name").toLowerCase();
                boolean isWindows = osName.contains("win");
                boolean isWSL = System.getenv("WSL_DISTRO_NAME") != null;
                
                if (isWindows || isWSL) {
                    // Common Windows scaling factors and their compensations:
                    // 125% scaling -> 0.8 resize factor
                    // 150% scaling -> 0.667 resize factor
                    // 175% scaling -> 0.571 resize factor
                    
                    System.out.println("Running on Windows/WSL - applying default compensation");
                    System.out.println("If pattern matching is poor, try these values:");
                    System.out.println("  125% display scaling: Settings.AlwaysResize = 0.8f");
                    System.out.println("  150% display scaling: Settings.AlwaysResize = 0.667f");
                    System.out.println("  175% display scaling: Settings.AlwaysResize = 0.571f");
                    
                    // Default to 125% scaling compensation (most common)
                    Settings.AlwaysResize = 0.8f;
                    System.out.println("Applied default: Settings.AlwaysResize = 0.8f");
                }
            }
            
            // Log current settings
            System.out.println("=== Current SikuliX Settings ===");
            System.out.println("Settings.AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("Settings.MinSimilarity: " + Settings.MinSimilarity);
            System.out.println("=================================");
            
        } catch (Exception e) {
            System.err.println("Error configuring DPI scaling: " + e.getMessage());
            e.printStackTrace();
            
            // Apply safe default for Windows with common 125% scaling
            System.out.println("Applying fallback configuration...");
            Settings.AlwaysResize = 0.8f;
            System.out.println("Fallback: Settings.AlwaysResize = 0.8f");
        }
    }
    
    /**
     * Manually set the resize factor if auto-detection doesn't work.
     * 
     * @param scalingPercentage The Windows display scaling percentage (e.g., 125, 150, 175)
     */
    public static void setManualScaling(int scalingPercentage) {
        float resizeFactor = 100.0f / scalingPercentage;
        Settings.AlwaysResize = resizeFactor;
        System.out.println("Manual DPI scaling set:");
        System.out.println("  Windows scaling: " + scalingPercentage + "%");
        System.out.println("  Settings.AlwaysResize: " + resizeFactor);
    }
}