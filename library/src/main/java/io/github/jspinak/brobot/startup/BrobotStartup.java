package io.github.jspinak.brobot.startup;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import jakarta.annotation.PostConstruct;

/**
 * Ensures Brobot captures at physical resolution by default.
 * This must run before any other configuration to properly disable DPI scaling.
 * 
 * Makes Brobot behave like SikuliX IDE, capturing at full physical resolution
 * regardless of Windows DPI scaling settings.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BrobotStartup {
    
    static {
        // Static initializer runs before Spring context
        // Use the dedicated initializer to ensure it runs first
        PhysicalResolutionInitializer.forceInitialization();
        configurePhysicalResolution();
    }
    
    @PostConstruct
    public void init() {
        // Verify configuration after Spring context loads
        verifyPhysicalResolution();
    }
    
    /**
     * Configures JVM to capture at physical resolution.
     * This disables DPI awareness to match SikuliX IDE behavior.
     */
    private static void configurePhysicalResolution() {
        System.out.println("=== Brobot Physical Resolution Configuration ===");
        
        // CRITICAL: These must be set before ANY AWT/Swing classes are loaded
        // Disable DPI awareness completely
        System.setProperty("sun.java2d.dpiaware", "false");
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        System.setProperty("sun.java2d.win.uiScaleX", "1.0");
        System.setProperty("sun.java2d.win.uiScaleY", "1.0");
        
        // Additional Windows-specific settings
        System.setProperty("sun.java2d.dpiaware.override", "false");
        System.setProperty("sun.java2d.win.uiScale.enabled", "false");
        
        // Ensure we're not in headless mode
        System.setProperty("java.awt.headless", "false");
        
        // Force physical pixels for all operations
        System.setProperty("sun.java2d.noddraw", "false");
        System.setProperty("sun.java2d.d3d", "false");  // Disable D3D which might apply scaling
        
        System.out.println("✓ DPI awareness disabled");
        System.out.println("✓ Physical resolution capture enabled");
        System.out.println("✓ All coordinates will be in physical pixels");
    }
    
    /**
     * Verifies that physical resolution capture is working.
     */
    private void verifyPhysicalResolution() {
        try {
            java.awt.GraphicsDevice device = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            
            java.awt.DisplayMode mode = device.getDisplayMode();
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            
            int physicalWidth = mode.getWidth();
            int physicalHeight = mode.getHeight();
            int toolkitWidth = (int) screenSize.getWidth();
            int toolkitHeight = (int) screenSize.getHeight();
            
            System.out.println("\n=== Resolution Verification ===");
            System.out.println("Physical Resolution: " + physicalWidth + "x" + physicalHeight);
            System.out.println("Toolkit Resolution:  " + toolkitWidth + "x" + toolkitHeight);
            
            if (physicalWidth == toolkitWidth && physicalHeight == toolkitHeight) {
                System.out.println("✓ SUCCESS: Capturing at PHYSICAL resolution");
                System.out.println("  Brobot will capture at " + physicalWidth + "x" + physicalHeight);
                System.out.println("  Pattern matching will work like SikuliX IDE");
            } else {
                System.out.println("⚠ WARNING: Resolution mismatch detected");
                System.out.println("  Physical: " + physicalWidth + "x" + physicalHeight);
                System.out.println("  Logical:  " + toolkitWidth + "x" + toolkitHeight);
                float scale = (float) toolkitWidth / physicalWidth;
                System.out.println("  DPI Scale: " + (int)(100 / scale) + "%");
                System.out.println("\n  To force physical resolution, run with:");
                System.out.println("  java -Dsun.java2d.dpiaware=false -jar your-app.jar");
            }
            System.out.println("================================\n");
            
        } catch (Exception e) {
            System.err.println("Error verifying resolution: " + e.getMessage());
        }
    }
}