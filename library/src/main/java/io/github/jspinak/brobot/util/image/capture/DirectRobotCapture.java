package io.github.jspinak.brobot.util.image.capture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Direct screen capture using Java Robot, bypassing SikuliX.
 * This ensures NO scaling or transformations are applied.
 * 
 * <p>This class is for testing whether SikuliX is applying
 * unwanted scaling during screen capture.</p>
 * 
 * @since 1.1.0
 */
@Slf4j
@Component
public class DirectRobotCapture {
    
    private Robot robot;
    
    public DirectRobotCapture() {
        try {
            this.robot = new Robot();
            ConsoleReporter.println("[DIRECT CAPTURE] Robot initialized successfully");
        } catch (AWTException e) {
            log.error("Failed to initialize Robot for direct capture", e);
            ConsoleReporter.println("[DIRECT CAPTURE] ERROR: Failed to initialize Robot: " + e.getMessage());
        }
    }
    
    /**
     * Captures a screen region using Java Robot directly.
     * NO scaling, NO DPI adjustments, NO transformations.
     * 
     * @param x X coordinate in actual screen pixels
     * @param y Y coordinate in actual screen pixels
     * @param width Width in actual screen pixels
     * @param height Height in actual screen pixels
     * @return Raw captured image at actual pixel resolution
     */
    public BufferedImage captureRegion(int x, int y, int width, int height) {
        if (robot == null) {
            ConsoleReporter.println("[DIRECT CAPTURE] Robot not initialized!");
            return null;
        }
        
        try {
            ConsoleReporter.println("[DIRECT CAPTURE] Capturing region: " + 
                x + "," + y + " " + width + "x" + height);
            
            Rectangle captureRect = new Rectangle(x, y, width, height);
            BufferedImage captured = robot.createScreenCapture(captureRect);
            
            ConsoleReporter.println("[DIRECT CAPTURE] Success: " + 
                captured.getWidth() + "x" + captured.getHeight() + 
                " type=" + getImageType(captured.getType()));
            
            // Log pixel sample to verify content
            if (captured.getWidth() > 0 && captured.getHeight() > 0) {
                int centerX = captured.getWidth() / 2;
                int centerY = captured.getHeight() / 2;
                int rgb = captured.getRGB(centerX, centerY);
                ConsoleReporter.println("[DIRECT CAPTURE] Center pixel RGB: " + 
                    String.format("#%06X", rgb & 0xFFFFFF));
            }
            
            return captured;
            
        } catch (Exception e) {
            ConsoleReporter.println("[DIRECT CAPTURE] ERROR: " + e.getMessage());
            log.error("Direct capture failed", e);
            return null;
        }
    }
    
    /**
     * Captures the full screen using Java Robot.
     * 
     * @return Full screen capture at actual resolution
     */
    public BufferedImage captureFullScreen() {
        if (robot == null) {
            return null;
        }
        
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            ConsoleReporter.println("[DIRECT CAPTURE] Screen size: " + 
                screenSize.width + "x" + screenSize.height);
            
            return captureRegion(0, 0, screenSize.width, screenSize.height);
            
        } catch (Exception e) {
            log.error("Full screen capture failed", e);
            return null;
        }
    }
    
    /**
     * Compares direct Robot capture with SikuliX capture.
     * Useful for debugging scaling issues.
     * 
     * @param sikuliCapture Image captured by SikuliX
     * @param x X coordinate
     * @param y Y coordinate  
     * @param width Width
     * @param height Height
     */
    public void compareWithSikuliCapture(BufferedImage sikuliCapture, 
                                         int x, int y, int width, int height) {
        BufferedImage directCapture = captureRegion(x, y, width, height);
        
        if (directCapture == null || sikuliCapture == null) {
            ConsoleReporter.println("[DIRECT CAPTURE] Cannot compare - null capture");
            return;
        }
        
        ConsoleReporter.println("[DIRECT CAPTURE] Comparison:");
        ConsoleReporter.println("  Direct Robot: " + directCapture.getWidth() + "x" + directCapture.getHeight());
        ConsoleReporter.println("  SikuliX:      " + sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());
        
        if (directCapture.getWidth() != sikuliCapture.getWidth() ||
            directCapture.getHeight() != sikuliCapture.getHeight()) {
            ConsoleReporter.println("  WARNING: Size mismatch! SikuliX might be scaling!");
        }
        
        // Sample pixel comparison
        int sampleX = Math.min(10, Math.min(directCapture.getWidth() - 1, sikuliCapture.getWidth() - 1));
        int sampleY = Math.min(10, Math.min(directCapture.getHeight() - 1, sikuliCapture.getHeight() - 1));
        
        int directRGB = directCapture.getRGB(sampleX, sampleY);
        int sikuliRGB = sikuliCapture.getRGB(sampleX, sampleY);
        
        if (directRGB != sikuliRGB) {
            ConsoleReporter.println("  Pixel difference at (" + sampleX + "," + sampleY + "):");
            ConsoleReporter.println("    Direct: " + String.format("#%06X", directRGB & 0xFFFFFF));
            ConsoleReporter.println("    Sikuli: " + String.format("#%06X", sikuliRGB & 0xFFFFFF));
        }
    }
    
    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB";
            case BufferedImage.TYPE_3BYTE_BGR: return "BGR";
            default: return "Type" + type;
        }
    }
}