package io.github.jspinak.brobot.config;

import org.sikuli.script.Screen;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/**
 * Forces Java to capture screenshots at physical resolution instead of logical (DPI-scaled) resolution.
 * This makes Brobot behave like the SikuliX IDE, capturing at full resolution regardless of Windows DPI scaling.
 * 
 * This solves the pattern matching issue where:
 * - IDE captures at 1920x1080 (physical)
 * - Brobot captures at 1536x864 (logical with 125% scaling)
 * - Patterns don't match well due to resolution difference
 */
@Configuration
public class PhysicalResolutionCapture {
    
    @PostConstruct
    public void configurePhysicalResolutionCapture() {
        System.out.println("=== Configuring Physical Resolution Capture ===");
        
        try {
            // Method 1: Disable DPI awareness for the entire JVM
            // This should be set as JVM arguments, but we can try setting them at runtime
            System.setProperty("sun.java2d.dpiaware", "false");
            System.setProperty("sun.java2d.uiScale", "1.0");
            System.setProperty("sun.java2d.win.uiScaleX", "1.0");
            System.setProperty("sun.java2d.win.uiScaleY", "1.0");
            
            // Method 2: Force AWT to recalculate screen dimensions
            // This tricks Java into using physical resolution
            forcePhysicalResolution();
            
            // Report the resolution we're capturing at
            GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            
            DisplayMode mode = device.getDisplayMode();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            
            System.out.println("Display Mode: " + mode.getWidth() + "x" + mode.getHeight());
            System.out.println("Toolkit Size: " + screenSize.getWidth() + "x" + screenSize.getHeight());
            
            if (mode.getWidth() == screenSize.getWidth()) {
                System.out.println("✓ Capturing at PHYSICAL resolution!");
            } else {
                System.out.println("⚠ Still capturing at LOGICAL resolution");
                System.out.println("  Consider using JVM flags: -Dsun.java2d.dpiaware=false");
            }
            
        } catch (Exception e) {
            System.err.println("Error configuring physical resolution: " + e.getMessage());
        }
    }
    
    /**
     * Forces AWT to use physical resolution by creating a temporary undecorated window.
     * This can trick the system into reporting physical dimensions.
     */
    private void forcePhysicalResolution() {
        try {
            // Create a temporary frame to force initialization
            JFrame tempFrame = new JFrame();
            tempFrame.setUndecorated(true);
            tempFrame.setSize(1, 1);
            tempFrame.setLocation(-100, -100);
            
            // Set the frame to ignore DPI scaling
            tempFrame.setPreferredSize(new Dimension(1, 1));
            
            // Show and immediately hide to force AWT initialization
            tempFrame.setVisible(true);
            tempFrame.setVisible(false);
            tempFrame.dispose();
            
        } catch (Exception e) {
            // Ignore errors - this is a best-effort attempt
        }
    }
    
    /**
     * Alternative method: Use SikuliX Screen to capture at physical resolution.
     * This can be used if the global configuration doesn't work.
     */
    public static BufferedImage capturePhysicalResolution() throws Exception {
        // Get the physical screen bounds
        GraphicsDevice device = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();
        
        DisplayMode mode = device.getDisplayMode();
        int physicalWidth = mode.getWidth();
        int physicalHeight = mode.getHeight();
        
        // Create screen and capture
        Screen screen = new Screen();
        BufferedImage capture = screen.capture().getImage();
        
        // Verify we got the right resolution
        if (capture.getWidth() != physicalWidth || capture.getHeight() != physicalHeight) {
            System.out.println("Warning: Capture resolution doesn't match physical resolution");
            System.out.println("  Expected: " + physicalWidth + "x" + physicalHeight);
            System.out.println("  Got: " + capture.getWidth() + "x" + capture.getHeight());
        }
        
        return capture;
    }
}