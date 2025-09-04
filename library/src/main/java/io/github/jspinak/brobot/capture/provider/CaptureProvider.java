package io.github.jspinak.brobot.capture.provider;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Interface for screen capture providers.
 * 
 * Allows different capture backends (SikuliX, FFmpeg, etc.) to be used interchangeably.
 * 
 * @since 1.1.0
 */
public interface CaptureProvider {
    
    /**
     * Captures the entire screen.
     * 
     * @return BufferedImage of the screen
     * @throws IOException if capture fails
     */
    BufferedImage captureScreen() throws IOException;
    
    /**
     * Captures a specific screen (for multi-monitor setups).
     * 
     * @param screenId the screen index
     * @return BufferedImage of the specified screen
     * @throws IOException if capture fails
     */
    BufferedImage captureScreen(int screenId) throws IOException;
    
    /**
     * Captures a specific region of the screen.
     * 
     * @param region the region to capture
     * @return BufferedImage of the region
     * @throws IOException if capture fails
     */
    BufferedImage captureRegion(Rectangle region) throws IOException;
    
    /**
     * Captures a region on a specific screen.
     * 
     * @param screenId the screen index
     * @param region the region to capture relative to that screen
     * @return BufferedImage of the region
     * @throws IOException if capture fails
     */
    BufferedImage captureRegion(int screenId, Rectangle region) throws IOException;
    
    /**
     * Checks if this provider is available on the current system.
     * 
     * @return true if the provider can be used
     */
    boolean isAvailable();
    
    /**
     * Gets the name of this provider.
     * 
     * @return provider name for logging/debugging
     */
    String getName();
    
    /**
     * Gets the capture resolution type.
     * 
     * @return PHYSICAL for physical pixels, LOGICAL for DPI-scaled pixels
     */
    ResolutionType getResolutionType();
    
    enum ResolutionType {
        PHYSICAL,  // Captures at physical resolution (e.g., 1920x1080)
        LOGICAL    // Captures at logical/DPI-scaled resolution (e.g., 1536x864)
    }
}