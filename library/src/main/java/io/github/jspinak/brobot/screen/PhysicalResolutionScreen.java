package io.github.jspinak.brobot.screen;

import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A simpler Screen implementation that forces physical resolution capture.
 * 
 * This implementation doesn't scale coordinates - it simply ensures that
 * all captures happen at physical resolution by disabling DPI awareness
 * at the JVM level (done in BrobotStartup).
 * 
 * The key insight: We don't need to scale coordinates if Java itself
 * is configured to work in physical pixels (like Java 8 does by default).
 */
public class PhysicalResolutionScreen extends Screen {
    
    private final boolean dpiAwarenessDisabled;
    
    public PhysicalResolutionScreen() {
        super();
        
        // Check if DPI awareness has been successfully disabled
        String dpiAware = System.getProperty("sun.java2d.dpiaware");
        this.dpiAwarenessDisabled = "false".equals(dpiAware);
        
        if (dpiAwarenessDisabled) {
            verifyPhysicalResolution();
        } else {
            System.out.println("WARNING: DPI awareness is not disabled. Pattern matching may not work correctly.");
            System.out.println("Run with: java -Dsun.java2d.dpiaware=false");
        }
    }
    
    private void verifyPhysicalResolution() {
        try {
            GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            
            DisplayMode mode = device.getDisplayMode();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            
            int physicalWidth = mode.getWidth();
            int physicalHeight = mode.getHeight();
            int toolkitWidth = (int) screenSize.getWidth();
            int toolkitHeight = (int) screenSize.getHeight();
            
            if (physicalWidth == toolkitWidth && physicalHeight == toolkitHeight) {
                System.out.println("✓ PhysicalResolutionScreen: Capturing at physical resolution (" + 
                    physicalWidth + "x" + physicalHeight + ")");
            } else {
                System.out.println("⚠ PhysicalResolutionScreen: Resolution mismatch detected");
                System.out.println("  Physical: " + physicalWidth + "x" + physicalHeight);
                System.out.println("  Toolkit: " + toolkitWidth + "x" + toolkitHeight);
                System.out.println("  This may cause pattern matching issues.");
            }
        } catch (Exception e) {
            System.err.println("Error verifying resolution: " + e.getMessage());
        }
    }
    
    @Override
    public ScreenImage capture() {
        // Use the standard capture - if DPI awareness is disabled, 
        // this will naturally capture at physical resolution
        return super.capture();
    }
    
    @Override
    public ScreenImage capture(Rectangle rect) {
        // Use the standard capture - coordinates should already be in physical pixels
        // if DPI awareness is properly disabled
        return super.capture(rect);
    }
    
    @Override
    public ScreenImage capture(int x, int y, int w, int h) {
        // Use the standard capture - coordinates should already be in physical pixels
        return super.capture(x, y, w, h);
    }
    
    /**
     * Check if the screen is properly configured for physical resolution capture.
     */
    public boolean isPhysicalResolutionEnabled() {
        return dpiAwarenessDisabled;
    }
}