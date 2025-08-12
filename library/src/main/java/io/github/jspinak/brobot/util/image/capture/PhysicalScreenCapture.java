package io.github.jspinak.brobot.util.image.capture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Captures screen at physical resolution, bypassing Windows DPI scaling.
 * This ensures captures are always at 1920x1080 instead of scaled 1536x864.
 */
@Slf4j
@Component
public class PhysicalScreenCapture {
    
    private static final boolean FORCE_PHYSICAL_RESOLUTION = true;
    
    /**
     * Get the physical screen dimensions, accounting for DPI scaling.
     * @return Rectangle with physical dimensions (e.g., 1920x1080)
     */
    public Rectangle getPhysicalScreenBounds() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        
        // Get logical bounds
        Rectangle logicalBounds = config.getBounds();
        
        // Get scale factor
        AffineTransform transform = config.getDefaultTransform();
        double scaleX = transform.getScaleX();
        double scaleY = transform.getScaleY();
        
        if (FORCE_PHYSICAL_RESOLUTION && (scaleX > 1.0 || scaleY > 1.0)) {
            // Calculate physical dimensions
            int physicalWidth = (int) Math.round(logicalBounds.width * scaleX);
            int physicalHeight = (int) Math.round(logicalBounds.height * scaleY);
            
            log.debug("Converting logical {}x{} to physical {}x{} (scale: {}%)", 
                logicalBounds.width, logicalBounds.height,
                physicalWidth, physicalHeight,
                (int)(scaleX * 100));
            
            return new Rectangle(0, 0, physicalWidth, physicalHeight);
        }
        
        return logicalBounds;
    }
    
    /**
     * Capture screen at physical resolution.
     * @param region The region to capture (in logical coordinates)
     * @return BufferedImage at physical resolution
     */
    public BufferedImage capturePhysicalScreen(Rectangle region) throws AWTException {
        Robot robot = new Robot();
        
        if (!FORCE_PHYSICAL_RESOLUTION) {
            return robot.createScreenCapture(region);
        }
        
        // Get scale factor
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device.getDefaultConfiguration();
        AffineTransform transform = config.getDefaultTransform();
        double scale = transform.getScaleX();
        
        if (scale > 1.0) {
            // Scale up the region to physical coordinates
            Rectangle physicalRegion = new Rectangle(
                (int)(region.x * scale),
                (int)(region.y * scale),
                (int)(region.width * scale),
                (int)(region.height * scale)
            );
            
            log.debug("Capturing physical region {}x{} (scaled from {}x{})",
                physicalRegion.width, physicalRegion.height,
                region.width, region.height);
            
            // Capture at physical resolution
            BufferedImage physicalCapture = robot.createScreenCapture(physicalRegion);
            
            // Return the physical resolution image (no downscaling)
            return physicalCapture;
        }
        
        return robot.createScreenCapture(region);
    }
}