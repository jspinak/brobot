package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.action.internal.utility.DragCoordinateCalculator;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.coordinates.CoordinateScaler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    
    @Autowired
    private CoordinateScaler coordinateScaler;


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
        try {
            // Scale the location from capture coordinates to logical coordinates if needed
            org.sikuli.script.Location sikuliLocation = coordinateScaler.scaleLocationToLogical(location);
            
            // Log both original and scaled coordinates for debugging
            log.debug("Mouse move: original={}, scaled={}", location, sikuliLocation);
            ConsoleReporter.print("move mouse to " + sikuliLocation + " ");
            
            // return new Region().mouseMove(location.getSikuliLocation()) != 0; // this can
            // cause the script to freeze for unknown reasons
            // Directly use hover() which is more stable than mouseMove()
            org.sikuli.script.Location result = sikuliLocation.hover();
            
            if (result == null) {
                log.error("SikuliX hover() returned null - mouse movement failed");
                return false;
            }
            
            log.debug("Mouse movement successful to {}", result);
            return true;
            
        } catch (Exception e) {
            log.error("Exception during mouse movement", e);
            return false;
        }
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
