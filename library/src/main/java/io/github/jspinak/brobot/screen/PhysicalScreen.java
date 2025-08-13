package io.github.jspinak.brobot.screen;

import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A Screen implementation that always captures at physical resolution,
 * regardless of DPI scaling settings.
 * 
 * This ensures Brobot captures screenshots at the same resolution as the SikuliX IDE,
 * solving pattern matching issues caused by DPI scaling.
 */
@Component
public class PhysicalScreen extends Screen {
    
    private final int physicalWidth;
    private final int physicalHeight;
    private final Robot physicalRobot;
    private final boolean needsScaling;
    private final float scaleFactor;
    
    public PhysicalScreen() {
        super();
        
        try {
            // Get physical resolution
            GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
            
            DisplayMode mode = device.getDisplayMode();
            this.physicalWidth = mode.getWidth();
            this.physicalHeight = mode.getHeight();
            
            // Create robot for physical device
            this.physicalRobot = new Robot(device);
            
            // Check if scaling is needed
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int toolkitWidth = (int) screenSize.getWidth();
            int toolkitHeight = (int) screenSize.getHeight();
            
            this.needsScaling = (physicalWidth != toolkitWidth || physicalHeight != toolkitHeight);
            this.scaleFactor = needsScaling ? (float) physicalWidth / toolkitWidth : 1.0f;
            
            if (needsScaling) {
                System.out.println("PhysicalScreen: Compensating for DPI scaling");
                System.out.println("  Physical: " + physicalWidth + "x" + physicalHeight);
                System.out.println("  Logical:  " + toolkitWidth + "x" + toolkitHeight);
                System.out.println("  Scale Factor: " + scaleFactor);
            }
            
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize PhysicalScreen", e);
        }
    }
    
    @Override
    public ScreenImage capture() {
        return capturePhysicalResolution(0, 0, physicalWidth, physicalHeight);
    }
    
    @Override
    public ScreenImage capture(Rectangle rect) {
        if (needsScaling) {
            // Scale the logical rectangle to physical coordinates
            int x = (int) (rect.x * scaleFactor);
            int y = (int) (rect.y * scaleFactor);
            int w = (int) (rect.width * scaleFactor);
            int h = (int) (rect.height * scaleFactor);
            return capturePhysicalResolution(x, y, w, h);
        } else {
            return capturePhysicalResolution(rect.x, rect.y, rect.width, rect.height);
        }
    }
    
    @Override
    public ScreenImage capture(int x, int y, int w, int h) {
        if (needsScaling) {
            // Scale logical coordinates to physical
            x = (int) (x * scaleFactor);
            y = (int) (y * scaleFactor);
            w = (int) (w * scaleFactor);
            h = (int) (h * scaleFactor);
        }
        return capturePhysicalResolution(x, y, w, h);
    }
    
    /**
     * Captures at physical resolution using the physical robot.
     */
    private ScreenImage capturePhysicalResolution(int x, int y, int w, int h) {
        try {
            // Ensure bounds are within screen
            x = Math.max(0, Math.min(x, physicalWidth - 1));
            y = Math.max(0, Math.min(y, physicalHeight - 1));
            w = Math.min(w, physicalWidth - x);
            h = Math.min(h, physicalHeight - y);
            
            Rectangle bounds = new Rectangle(x, y, w, h);
            BufferedImage capture = physicalRobot.createScreenCapture(bounds);
            
            // Return ScreenImage with physical resolution capture
            return new ScreenImage(bounds, capture);
            
        } catch (Exception e) {
            System.err.println("Error capturing at physical resolution: " + e.getMessage());
            // Fallback to default capture
            return super.capture(x, y, w, h);
        }
    }
    
    /**
     * Gets the physical screen resolution.
     */
    public Dimension getPhysicalResolution() {
        return new Dimension(physicalWidth, physicalHeight);
    }
    
    /**
     * Checks if DPI scaling compensation is active.
     */
    public boolean isScalingCompensated() {
        return needsScaling;
    }
    
    /**
     * Gets the DPI scale factor.
     */
    public float getScaleFactor() {
        return scaleFactor;
    }
}