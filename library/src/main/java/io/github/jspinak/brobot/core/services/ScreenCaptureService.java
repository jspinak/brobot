package io.github.jspinak.brobot.core.services;

import io.github.jspinak.brobot.model.element.Region;

import java.awt.image.BufferedImage;

/**
 * Core interface for screen capture operations.
 * 
 * <p>This interface defines the contract for screen capture implementations,
 * completely decoupled from any higher-level components. It represents pure
 * screen capture functionality with no dependencies on the Brobot action
 * framework, Find, or any other high-level components.</p>
 * 
 * <p>Implementations can use various technologies for screen capture:
 * <ul>
 *   <li>AWT Robot for Java-based capture</li>
 *   <li>Sikuli's screen capture capabilities</li>
 *   <li>Platform-specific APIs</li>
 *   <li>Mock implementations for testing</li>
 * </ul>
 * </p>
 * 
 * <p>Key design principles:
 * <ul>
 *   <li>No dependencies on Find, Actions, or any higher-level components</li>
 *   <li>Pure screen capture - no pattern matching or analysis</li>
 *   <li>Support for multi-monitor environments</li>
 *   <li>Thread-safe operations</li>
 * </ul>
 * </p>
 * 
 * @since 2.0.0
 */
public interface ScreenCaptureService {
    
    /**
     * Captures the entire primary screen.
     * 
     * <p>This method captures a screenshot of the primary monitor.
     * In multi-monitor setups, this typically refers to the main
     * display as determined by the operating system.</p>
     * 
     * @return BufferedImage containing the screen contents,
     *         or null if capture fails
     */
    BufferedImage captureScreen();
    
    /**
     * Captures a specific region of the screen.
     * 
     * <p>This method captures only the specified rectangular area
     * of the screen, which can improve performance when only a
     * portion of the screen is needed.</p>
     * 
     * @param x X coordinate of the region's top-left corner
     * @param y Y coordinate of the region's top-left corner
     * @param width Width of the region to capture
     * @param height Height of the region to capture
     * @return BufferedImage containing the region contents,
     *         or null if capture fails or region is invalid
     */
    BufferedImage captureRegion(int x, int y, int width, int height);
    
    /**
     * Captures a specific region of the screen using a Region object.
     * 
     * <p>Convenience method that accepts a Brobot Region object
     * for specifying the capture area.</p>
     * 
     * @param region The region to capture
     * @return BufferedImage containing the region contents,
     *         or null if capture fails or region is invalid
     */
    default BufferedImage captureRegion(Region region) {
        if (region == null) {
            return null;
        }
        return captureRegion(region.x(), region.y(), region.w(), region.h());
    }
    
    /**
     * Captures the screen containing the mouse cursor.
     * 
     * <p>In multi-monitor setups, this captures the screen where
     * the mouse cursor is currently located, which may not be the
     * primary screen.</p>
     * 
     * @return BufferedImage containing the active screen contents,
     *         or null if capture fails
     */
    BufferedImage captureActiveScreen();
    
    /**
     * Captures a specific monitor by index.
     * 
     * <p>Allows capturing from a specific monitor in multi-monitor
     * setups. Monitor indices typically start at 0 for the primary
     * monitor.</p>
     * 
     * @param monitorIndex Index of the monitor to capture (0-based)
     * @return BufferedImage containing the monitor contents,
     *         or null if capture fails or monitor doesn't exist
     */
    BufferedImage captureMonitor(int monitorIndex);
    
    /**
     * Gets the number of available monitors.
     * 
     * <p>Returns the count of monitors available for capture
     * in the current system configuration.</p>
     * 
     * @return Number of monitors available (minimum 1)
     */
    int getMonitorCount();
    
    /**
     * Gets the bounds of a specific monitor.
     * 
     * <p>Returns the screen bounds (position and size) of the
     * specified monitor, useful for understanding the layout
     * in multi-monitor setups.</p>
     * 
     * @param monitorIndex Index of the monitor (0-based)
     * @return Region representing the monitor bounds,
     *         or null if monitor doesn't exist
     */
    Region getMonitorBounds(int monitorIndex);
    
    /**
     * Gets the bounds of all monitors combined.
     * 
     * <p>Returns the virtual desktop bounds that encompass all
     * monitors in a multi-monitor setup.</p>
     * 
     * @return Region representing the combined bounds of all monitors
     */
    Region getVirtualDesktopBounds();
    
    /**
     * Checks if the service is available and functional.
     * 
     * <p>This method can be used to verify that screen capture
     * is possible in the current environment (e.g., not headless).</p>
     * 
     * @return true if screen capture is available, false otherwise
     */
    boolean isAvailable();
    
    /**
     * Gets the name of this screen capture implementation.
     * 
     * <p>Used for logging and debugging to identify which
     * capture method is being used.</p>
     * 
     * @return Implementation name (e.g., "AWT Robot", "Sikuli", "Mock")
     */
    String getImplementationName();
}