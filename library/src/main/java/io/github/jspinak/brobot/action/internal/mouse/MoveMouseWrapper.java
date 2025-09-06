package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.action.internal.utility.DragCoordinateCalculator;
import io.github.jspinak.brobot.capture.ScreenDimensions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import org.springframework.stereotype.Component;

import java.awt.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles mouse movement operations to specific screen locations.
 * <p>
 * MoveMouseWrapper provides a reliable interface for moving the mouse cursor
 * to precise screen coordinates. It abstracts the underlying SikuliX mouse
 * movement functionality while adding error handling, logging, and mock mode
 * support for testing.
 * <p>
 * <strong>Key features:</strong>
 * <ul>
 * <li>Precise mouse positioning at specified coordinates</li>
 * <li>Error detection and reporting for failed movements</li>
 * <li>Mock mode for testing without actual mouse movement</li>
 * <li>Automatic logging of all movement operations</li>
 * <li>Uses hover() instead of mouseMove() to avoid freezing issues</li>
 * </ul>
 * <p>
 * <strong>Technical considerations:</strong>
 * <p>
 * The implementation uses SikuliX's hover() method instead of mouseMove() due
 * to
 * stability issues where mouseMove() can cause the script to freeze under
 * certain
 * conditions. The hover() method provides equivalent functionality with better
 * reliability.
 * <p>
 * <strong>Common usage scenarios:</strong>
 * <ul>
 * <li>Positioning mouse before click operations</li>
 * <li>Moving to hover-sensitive UI elements</li>
 * <li>Navigating between screen regions</li>
 * <li>Setting up for drag operations</li>
 * </ul>
 *
 * @see SingleClickExecutor
 * @see DragCoordinateCalculator
 * @see Location
 */
@Slf4j
@Component
public class MoveMouseWrapper {

    /**
     * Scales a location from physical to logical coordinates if needed.
     * When captures are done at physical resolution (e.g., 1920x1080 with FFmpeg)
     * but SikuliX mouse operations work in logical resolution (e.g., 1536x864 with 125% DPI),
     * we need to scale the coordinates.
     * 
     * @param location The location in capture coordinates
     * @return A new SikuliX Location in logical coordinates
     */
    private org.sikuli.script.Location scaleLocationForMouse(Location location) {
        // Get the capture dimensions (may be physical resolution)
        int captureWidth = ScreenDimensions.getWidth();
        int captureHeight = ScreenDimensions.getHeight();
        
        // Get the logical screen dimensions (what SikuliX uses for mouse operations)
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int logicalWidth = screenSize.width;
        int logicalHeight = screenSize.height;
        
        // Get the coordinates from the location
        int x = location.getCalculatedX();
        int y = location.getCalculatedY();
        
        // If capture and logical dimensions match, no scaling needed
        if (captureWidth == logicalWidth && captureHeight == logicalHeight) {
            log.debug("No coordinate scaling needed: capture {}x{} matches logical {}x{}", 
                     captureWidth, captureHeight, logicalWidth, logicalHeight);
            return new org.sikuli.script.Location(x, y);
        }
        
        // Calculate scale factors (from physical to logical)
        double scaleX = (double) logicalWidth / captureWidth;
        double scaleY = (double) logicalHeight / captureHeight;
        
        // Scale the coordinates
        int scaledX = (int) Math.round(x * scaleX);
        int scaledY = (int) Math.round(y * scaleY);
        
        log.debug("Scaling mouse location from physical ({},{}) to logical ({},{}) - scale factors: {}x{}",
                 x, y, scaledX, scaledY, 
                 String.format("%.3f", scaleX), String.format("%.3f", scaleY));
        
        return new org.sikuli.script.Location(scaledX, scaledY);
    }

    /**
     * Performs the actual mouse movement using SikuliX.
     * <p>
     * Uses the hover() method instead of mouseMove() to avoid potential
     * freezing issues that have been observed with mouseMove(). The hover()
     * method moves the mouse to the location and returns the location if
     * successful, or null if the movement failed.
     * <p>
     * Coordinates are automatically scaled from physical to logical resolution
     * if needed (e.g., when using FFmpeg capture with DPI scaling).
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>Moves the system mouse cursor to the specified location</li>
     * <li>Writes movement details to Report</li>
     * </ul>
     *
     * @param location Target location for mouse movement (in capture coordinates)
     * @return true if movement succeeded, false if hover() returned null
     */
    private boolean sikuliMove(Location location) {
        // Scale the location from capture coordinates to logical coordinates if needed
        org.sikuli.script.Location sikuliLocation = scaleLocationForMouse(location);
        ConsoleReporter.print("move mouse to " + sikuliLocation + " ");
        // return new Region().mouseMove(location.getSikuliLocation()) != 0; // this can
        // cause the script to freeze for unknown reasons
        // Directly use hover() which is more stable than mouseMove()
        return sikuliLocation.hover() != null;
    }

    /**
     * Moves the mouse cursor to the specified location.
     * <p>
     * In mock mode, simulates the movement by logging the target coordinates
     * without actual mouse interaction. In normal mode, performs the movement
     * and reports any failures.
     * <p>
     * <strong>Error handling:</strong>
     * <p>
     * If the movement fails (hover returns null), the method logs "move failed"
     * and returns false. This allows calling code to handle movement failures
     * appropriately, such as retrying or aborting dependent operations.
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>In real mode: moves the system mouse cursor</li>
     * <li>In mock mode: logs "mouseMove to: X.Y|" at HIGH output level</li>
     * <li>On failure: adds "move failed." to Report</li>
     * </ul>
     *
     * @param location Target screen location for the mouse cursor
     * @return true if movement succeeded or in mock mode, false if movement failed
     */
    public boolean move(Location location) {
        if (FrameworkSettings.mock) {
            ConsoleReporter.format(ConsoleReporter.OutputLevel.HIGH, "%s: %d.%d| ", "mouseMove to",
                    location.getCalculatedX(), location.getCalculatedY());
            return true;
        }
        boolean success = sikuliMove(location);
        if (!success)
            ConsoleReporter.print("move failed. ");
        // else ConsoleReporter.print("move succeeded. ");
        return success;
    }

}
