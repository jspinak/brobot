package io.github.jspinak.brobot.util.image.capture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Handles DPI-aware screen capture to ensure consistent image dimensions
 * across different display scaling settings.
 * 
 * <p>Modern operating systems support display scaling (125%, 150%, 200% etc)
 * which can cause mismatches between logical and physical pixel dimensions.
 * This class detects and compensates for display scaling to ensure that
 * captured images match the expected dimensions for pattern matching.</p>
 * 
 * <p>The problem: When a display is scaled (e.g., 150%), a 100x100 logical
 * region actually corresponds to 150x150 physical pixels. If patterns are
 * saved at one scaling level and matched at another, the matching will fail.</p>
 * 
 * @since 1.1.0
 */
@Slf4j
@Component
public class DPIAwareCapture {
    
    private Double cachedScaleFactor = null;
    private long lastScaleCheck = 0;
    private static final long SCALE_CACHE_DURATION = 60000; // 1 minute
    
    /**
     * Gets the current display scaling factor.
     * 
     * @return The scaling factor (1.0 = 100%, 1.25 = 125%, 1.5 = 150%, 2.0 = 200%)
     */
    public double getDisplayScaleFactor() {
        // Cache the scale factor for performance
        long now = System.currentTimeMillis();
        if (cachedScaleFactor != null && (now - lastScaleCheck) < SCALE_CACHE_DURATION) {
            return cachedScaleFactor;
        }
        
        try {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            GraphicsConfiguration config = device.getDefaultConfiguration();
            AffineTransform transform = config.getDefaultTransform();
            
            double scaleX = transform.getScaleX();
            double scaleY = transform.getScaleY();
            
            // Use the maximum scale factor to be safe
            double scaleFactor = Math.max(scaleX, scaleY);
            
            // Log scale detection
            if (cachedScaleFactor == null || Math.abs(cachedScaleFactor - scaleFactor) > 0.01) {
                log.info("Display scale factor detected: {}% ({}x{} scaling)", 
                    (int)(scaleFactor * 100), scaleX, scaleY);
                    
                if (scaleFactor != 1.0) {
                    log.info("Display scaling is active. Adjusting capture dimensions for DPI awareness.");
                }
            }
            
            cachedScaleFactor = scaleFactor;
            lastScaleCheck = now;
            
            return scaleFactor;
            
        } catch (Exception e) {
            log.warn("Could not detect display scaling, assuming 100%: {}", e.getMessage());
            cachedScaleFactor = 1.0;
            lastScaleCheck = now;
            return 1.0;
        }
    }
    
    /**
     * Captures a screen region with DPI awareness, ensuring consistent dimensions.
     * 
     * @param x Logical X coordinate
     * @param y Logical Y coordinate
     * @param width Logical width
     * @param height Logical height
     * @return BufferedImage at logical resolution (scaled if necessary)
     */
    public BufferedImage captureDPIAware(int x, int y, int width, int height) {
        // Validate dimensions
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        
        // Check if in mock mode
        boolean mockMode = false;
        try {
            Class<?> frameworkSettingsClass = Class.forName("io.github.jspinak.brobot.config.FrameworkSettings");
            mockMode = frameworkSettingsClass.getField("mock").getBoolean(null);
        } catch (Exception e) {
            // FrameworkSettings not available or mock field not accessible
        }
        
        if (mockMode) {
            // In mock mode, return a dummy image
            log.debug("Mock mode: returning dummy image for capture [{},{}] {}x{}", x, y, width, height);
            BufferedImage mockImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = mockImage.createGraphics();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, width, height);
            g.dispose();
            return mockImage;
        }
        
        double scaleFactor = getDisplayScaleFactor();
        
        try {
            Robot robot = new Robot();
            
            if (Math.abs(scaleFactor - 1.0) < 0.01) {
                // No scaling, capture directly
                return robot.createScreenCapture(new Rectangle(x, y, width, height));
            }
            
            // Scale coordinates for physical pixel capture
            int physicalX = (int)(x * scaleFactor);
            int physicalY = (int)(y * scaleFactor);
            int physicalWidth = (int)(width * scaleFactor);
            int physicalHeight = (int)(height * scaleFactor);
            
            log.debug("DPI-aware capture: logical [{}x{} at {},{}} -> physical [{}x{} at {},{}]",
                width, height, x, y, physicalWidth, physicalHeight, physicalX, physicalY);
            
            // Capture at physical resolution
            BufferedImage physicalCapture = robot.createScreenCapture(
                new Rectangle(physicalX, physicalY, physicalWidth, physicalHeight)
            );
            
            // Scale back to logical resolution for consistent matching
            return scaleToLogicalResolution(physicalCapture, width, height);
            
        } catch (AWTException e) {
            log.error("Failed to create Robot for screen capture", e);
            throw new RuntimeException("Screen capture failed", e);
        }
    }
    
    /**
     * Scales a captured image from physical to logical resolution.
     * 
     * @param physicalImage The image at physical pixel resolution
     * @param logicalWidth The desired logical width
     * @param logicalHeight The desired logical height
     * @return Scaled image at logical resolution
     */
    private BufferedImage scaleToLogicalResolution(BufferedImage physicalImage, 
                                                   int logicalWidth, int logicalHeight) {
        // If dimensions already match, no scaling needed
        if (physicalImage.getWidth() == logicalWidth && 
            physicalImage.getHeight() == logicalHeight) {
            return physicalImage;
        }
        
        // Create new image at logical resolution
        BufferedImage logicalImage = new BufferedImage(
            logicalWidth, logicalHeight, 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g = logicalImage.createGraphics();
        
        // Use high-quality scaling
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                          RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                          RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw scaled image
        g.drawImage(physicalImage, 0, 0, logicalWidth, logicalHeight, null);
        g.dispose();
        
        log.debug("Scaled image from {}x{} to {}x{} (logical resolution)",
            physicalImage.getWidth(), physicalImage.getHeight(),
            logicalWidth, logicalHeight);
        
        return logicalImage;
    }
    
    /**
     * Checks if display scaling is active.
     * 
     * @return true if display scaling is not 100%
     */
    public boolean isScalingActive() {
        double scale = getDisplayScaleFactor();
        return Math.abs(scale - 1.0) > 0.01;
    }
    
    /**
     * Converts logical coordinates to physical pixel coordinates.
     * 
     * @param logicalRect Rectangle in logical coordinates
     * @return Rectangle in physical pixel coordinates
     */
    public Rectangle toPhysicalCoordinates(Rectangle logicalRect) {
        double scale = getDisplayScaleFactor();
        return new Rectangle(
            (int)(logicalRect.x * scale),
            (int)(logicalRect.y * scale),
            (int)(logicalRect.width * scale),
            (int)(logicalRect.height * scale)
        );
    }
    
    /**
     * Converts physical pixel coordinates to logical coordinates.
     * 
     * @param physicalRect Rectangle in physical pixel coordinates
     * @return Rectangle in logical coordinates
     */
    public Rectangle toLogicalCoordinates(Rectangle physicalRect) {
        double scale = getDisplayScaleFactor();
        if (Math.abs(scale - 1.0) < 0.01) {
            return physicalRect;
        }
        
        return new Rectangle(
            (int)(physicalRect.x / scale),
            (int)(physicalRect.y / scale),
            (int)(physicalRect.width / scale),
            (int)(physicalRect.height / scale)
        );
    }
    
    /**
     * Normalizes an image to ensure it's at logical resolution.
     * This is useful when loading saved patterns that might have been
     * captured at different scaling levels.
     * 
     * @param image The image to normalize
     * @param expectedWidth Expected logical width
     * @param expectedHeight Expected logical height
     * @return Normalized image at logical resolution
     */
    public BufferedImage normalizeToLogicalResolution(BufferedImage image,
                                                      int expectedWidth,
                                                      int expectedHeight) {
        // If dimensions match, no normalization needed
        if (image.getWidth() == expectedWidth && 
            image.getHeight() == expectedHeight) {
            return image;
        }
        
        // Calculate the apparent scale factor from the image
        double apparentScaleX = (double) image.getWidth() / expectedWidth;
        double apparentScaleY = (double) image.getHeight() / expectedHeight;
        
        // Log if there's a significant difference
        if (Math.abs(apparentScaleX - apparentScaleY) > 0.1) {
            log.warn("Image has inconsistent scaling: X={}, Y={}", 
                apparentScaleX, apparentScaleY);
        }
        
        log.info("Normalizing image from {}x{} to {}x{} (apparent scale: {}%)",
            image.getWidth(), image.getHeight(),
            expectedWidth, expectedHeight,
            (int)(Math.max(apparentScaleX, apparentScaleY) * 100));
        
        return scaleToLogicalResolution(image, expectedWidth, expectedHeight);
    }
}