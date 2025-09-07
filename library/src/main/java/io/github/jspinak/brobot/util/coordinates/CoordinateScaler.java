package io.github.jspinak.brobot.util.coordinates;

import io.github.jspinak.brobot.capture.ScreenDimensions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * Utility class for scaling coordinates between physical and logical resolution.
 * 
 * <p>When captures are done at physical resolution (e.g., 1920x1080 with FFmpeg)
 * but SikuliX operations work in logical resolution (e.g., 1536x864 with 125% DPI),
 * coordinates need to be scaled appropriately.</p>
 * 
 * <p>This centralizes the coordinate scaling logic to ensure consistency across
 * all mouse operations, highlighting, and other screen-coordinate dependent features.</p>
 * 
 * @since 1.0
 */
@Slf4j
@Component
public class CoordinateScaler {
    
    /**
     * Scales a location from capture coordinates to logical coordinates for SikuliX operations.
     * 
     * @param location The location in capture coordinates
     * @return A new SikuliX Location in logical coordinates
     */
    public org.sikuli.script.Location scaleLocationToLogical(Location location) {
        // Get the coordinates from the location
        int x = location.getCalculatedX();
        int y = location.getCalculatedY();
        
        return scaleToLogical(x, y);
    }
    
    /**
     * Scales x,y coordinates from capture resolution to logical resolution.
     * 
     * @param x The x coordinate in capture resolution
     * @param y The y coordinate in capture resolution
     * @return A new SikuliX Location in logical coordinates
     */
    public org.sikuli.script.Location scaleToLogical(int x, int y) {
        // Get the capture dimensions (may be physical resolution)
        int captureWidth = ScreenDimensions.getWidth();
        int captureHeight = ScreenDimensions.getHeight();
        
        // Get the logical screen dimensions (what SikuliX uses)
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int logicalWidth = screenSize.width;
        int logicalHeight = screenSize.height;
        
        // If capture and logical dimensions match, no scaling needed
        if (captureWidth == logicalWidth && captureHeight == logicalHeight) {
            log.trace("No coordinate scaling needed: capture {}x{} matches logical {}x{}", 
                     captureWidth, captureHeight, logicalWidth, logicalHeight);
            return new org.sikuli.script.Location(x, y);
        }
        
        // Calculate scale factors (from physical to logical)
        double scaleX = (double) logicalWidth / captureWidth;
        double scaleY = (double) logicalHeight / captureHeight;
        
        // Scale the coordinates
        int scaledX = (int) Math.round(x * scaleX);
        int scaledY = (int) Math.round(y * scaleY);
        
        log.debug("Scaled coordinates from ({},{}) to ({},{}) - scale factors: {}x{}",
                 x, y, scaledX, scaledY, 
                 String.format("%.3f", scaleX), String.format("%.3f", scaleY));
        
        return new org.sikuli.script.Location(scaledX, scaledY);
    }
    
    /**
     * Scales a region from capture coordinates to logical coordinates.
     * Used for highlighting and other display operations.
     * 
     * @param region The region in capture coordinates
     * @return A new Region in logical coordinates
     */
    public Region scaleRegionToLogical(Region region) {
        // Get capture and logical dimensions
        int captureWidth = ScreenDimensions.getWidth();
        int captureHeight = ScreenDimensions.getHeight();
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int logicalWidth = screenSize.width;
        int logicalHeight = screenSize.height;
        
        // If dimensions match, no scaling needed
        if (captureWidth == logicalWidth && captureHeight == logicalHeight) {
            return region;
        }
        
        // Calculate scale factors
        double scaleX = (double) logicalWidth / captureWidth;
        double scaleY = (double) logicalHeight / captureHeight;
        
        // Scale the region coordinates
        int scaledX = (int) Math.round(region.x() * scaleX);
        int scaledY = (int) Math.round(region.y() * scaleY);
        int scaledW = (int) Math.round(region.w() * scaleX);
        int scaledH = (int) Math.round(region.h() * scaleY);
        
        // Ensure the scaled region fits within logical screen bounds
        if (scaledY + scaledH > logicalHeight) {
            scaledH = logicalHeight - scaledY;
        }
        if (scaledX + scaledW > logicalWidth) {
            scaledW = logicalWidth - scaledX;
        }
        
        log.debug("Scaled region from [{},{},{},{}] to [{},{},{},{}]",
                 region.x(), region.y(), region.w(), region.h(),
                 scaledX, scaledY, scaledW, scaledH);
        
        return new Region(scaledX, scaledY, scaledW, scaledH);
    }
    
    /**
     * Checks if coordinate scaling is needed based on current configuration.
     * 
     * @return true if capture and logical resolutions differ
     */
    public boolean isScalingNeeded() {
        int captureWidth = ScreenDimensions.getWidth();
        int captureHeight = ScreenDimensions.getHeight();
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        return captureWidth != screenSize.width || captureHeight != screenSize.height;
    }
    
    /**
     * Gets the scale factors for debugging and logging.
     * 
     * @return An array with [scaleX, scaleY]
     */
    public double[] getScaleFactors() {
        int captureWidth = ScreenDimensions.getWidth();
        int captureHeight = ScreenDimensions.getHeight();
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        
        double scaleX = (double) screenSize.width / captureWidth;
        double scaleY = (double) screenSize.height / captureHeight;
        
        return new double[] { scaleX, scaleY };
    }
}