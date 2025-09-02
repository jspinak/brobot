package io.github.jspinak.brobot.config;

import org.sikuli.basics.Settings;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${brobot.dpi.disable-scaling:false}")
    private boolean disableScaling;
    
    @Value("${brobot.dpi.resize-factor:0}")
    private float configuredResizeFactor;
    
    @PostConstruct
    public void configureDPIScaling() {
        System.out.println("=== Brobot DPI Configuration ===");
        
        // Check if scaling has been disabled in early initialization
        if (disableScaling || "true".equals(System.getProperty("brobot.dpi.scaling.disabled"))) {
            System.out.println("DPI scaling is disabled - patterns will match at 1:1");
            System.out.println("Settings.AlwaysResize: " + Settings.AlwaysResize);
            return;
        }
        
        // Check Java version for cross-version compatibility
        configureJavaVersionCompatibility();
        
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
            
            // Check if a resize factor was explicitly configured
            if (configuredResizeFactor > 0) {
                Settings.AlwaysResize = configuredResizeFactor;
                System.out.println("Using configured resize factor: " + configuredResizeFactor);
            }
            // Check if DPI scaling is active
            else if (physicalWidth > 0 && logicalWidth > 0 && physicalWidth != logicalWidth) {
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
                    
                    System.out.println("Running on Windows/WSL");
                    System.out.println("If pattern matching is poor, try setting brobot.dpi.resize-factor:");
                    System.out.println("  125% display scaling: brobot.dpi.resize-factor=0.8");
                    System.out.println("  150% display scaling: brobot.dpi.resize-factor=0.667");
                    System.out.println("  175% display scaling: brobot.dpi.resize-factor=0.571");
                    System.out.println("  Or disable scaling: brobot.dpi.disable-scaling=true");
                    
                    // Don't apply default scaling - let user configure if needed
                    Settings.AlwaysResize = 1.0f;
                    System.out.println("No automatic scaling applied: Settings.AlwaysResize = 1.0f");
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
            
            // Don't apply automatic scaling on error
            System.out.println("Applying fallback configuration...");
            Settings.AlwaysResize = 1.0f;
            System.out.println("Fallback: Settings.AlwaysResize = 1.0f (no scaling)");
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
    
    /**
     * Configure cross-version compatibility settings.
     * Handles differences between Java 8 (often used in IDE) and Java 9+ (used in runtime).
     * This ensures pattern matching works consistently across different Java versions.
     */
    private void configureJavaVersionCompatibility() {
        String javaVersion = System.getProperty("java.version");
        System.out.println("Running on Java: " + javaVersion);
        
        // Check if we're running on Java 9 or later
        boolean isJava9Plus = false;
        try {
            String[] versionParts = javaVersion.split("\\.");
            int majorVersion = Integer.parseInt(versionParts[0]);
            
            // Java 9+ uses version numbering like "11.0.1", "17.0.2", "21.0.1"
            // Java 8 uses "1.8.0_xxx"
            if (majorVersion >= 9 || (majorVersion == 1 && versionParts.length > 1 && 
                Integer.parseInt(versionParts[1]) >= 9)) {
                isJava9Plus = true;
            }
        } catch (Exception e) {
            // Simple fallback check
            isJava9Plus = javaVersion.startsWith("21") || javaVersion.startsWith("17") || 
                         javaVersion.startsWith("11") || javaVersion.startsWith("9");
        }
        
        if (isJava9Plus) {
            System.out.println("Detected Java 9+ - Applying cross-version compatibility settings");
            
            // Lower similarity threshold slightly to account for Java version rendering differences
            // (antialiasing, color management, font rendering)
            Settings.MinSimilarity = 0.68;  // Slightly lower than default 0.7
            
            // Keep optimization for performance
            Settings.CheckLastSeen = true;
            
            // Disable some Java 9+ specific image optimizations that might
            // interfere with pattern matching
            System.setProperty("sun.java2d.opengl", "false");
            System.setProperty("sun.java2d.d3d", "false");
            
            // Use consistent color rendering across versions
            System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            
            System.out.println("  Adjusted MinSimilarity to: " + Settings.MinSimilarity);
            System.out.println("  Disabled hardware acceleration for consistent rendering");
        } else {
            System.out.println("Running on Java 8 - Using standard settings");
            Settings.MinSimilarity = 0.7;
            Settings.CheckLastSeen = true;
        }
    }
}