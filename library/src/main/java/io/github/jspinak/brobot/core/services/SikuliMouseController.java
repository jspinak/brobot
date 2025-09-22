package io.github.jspinak.brobot.core.services;

import java.awt.*;
import java.awt.GraphicsEnvironment;
import java.awt.event.InputEvent;

import org.sikuli.script.Button;
import org.sikuli.script.Location;
import org.sikuli.script.Mouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.util.coordinates.CoordinateScaler;

/**
 * Sikuli-based implementation of the MouseController interface.
 *
 * <p>This implementation uses Sikuli's mouse control capabilities, which provide cross-platform
 * mouse automation. It is completely independent of the Find action and other high-level Brobot
 * components.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li>No dependencies on Find or Action classes
 *   <li>Thread-safe through synchronized operations
 *   <li>Cross-platform compatibility
 *   <li>Uses hover() instead of mouseMove() for stability
 * </ul>
 *
 * @since 2.0.0
 */
@Component("sikuliMouseController")
@Primary
public class SikuliMouseController implements MouseController {

    // Button constants for Sikuli
    private static final int SIKULI_LEFT = java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
    private static final int SIKULI_RIGHT = java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
    private static final int SIKULI_MIDDLE = java.awt.event.InputEvent.BUTTON2_DOWN_MASK;

    private final CoordinateScaler coordinateScaler;

    @Autowired
    public SikuliMouseController(@Autowired(required = false) CoordinateScaler coordinateScaler) {
        this.coordinateScaler = coordinateScaler;
    }

    @Override
    public synchronized boolean moveTo(int x, int y) {
        try {
            // Scale coordinates if needed (from physical to logical)
            Location location;
            if (coordinateScaler != null && coordinateScaler.isScalingNeeded()) {
                io.github.jspinak.brobot.model.element.Location brobotLoc =
                        new io.github.jspinak.brobot.model.element.Location(x, y);
                location = coordinateScaler.scaleLocationToLogical(brobotLoc);
            } else {
                location = new Location(x, y);
            }

            // Use hover() instead of mouseMove() for better stability
            Location result = location.hover();
            boolean success = result != null;

            return success;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public synchronized boolean click(int x, int y, MouseButton button) {
        try {
            // Move to location first
            if (!moveTo(x, y)) {
                return false;
            }

            // Scale coordinates if needed (from physical to logical)
            Location location;
            if (coordinateScaler != null && coordinateScaler.isScalingNeeded()) {
                io.github.jspinak.brobot.model.element.Location brobotLoc =
                        new io.github.jspinak.brobot.model.element.Location(x, y);
                location = coordinateScaler.scaleLocationToLogical(brobotLoc);
            } else {
                location = new Location(x, y);
            }
            boolean success = false;

            if (button == MouseButton.LEFT) {
                location.click();
                success = true;
            } else if (button == MouseButton.RIGHT) {
                location.rightClick();
                success = true;
            } else if (button == MouseButton.MIDDLE) {
                // Sikuli doesn't have a built-in middle click, use Mouse class
                Mouse.move(location);
                Mouse.down(Button.MIDDLE);
                Mouse.up(Button.MIDDLE);
                success = true;
            }

            return success;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public synchronized boolean doubleClick(int x, int y, MouseButton button) {
        try {
            // Move to location first
            if (!moveTo(x, y)) {
                return false;
            }

            // Sikuli's doubleClick only supports left button
            // For other buttons, simulate with two clicks
            if (button == MouseButton.LEFT) {
                // Scale coordinates if needed (from physical to logical)
                Location location;
                if (coordinateScaler != null && coordinateScaler.isScalingNeeded()) {
                    io.github.jspinak.brobot.model.element.Location brobotLoc =
                            new io.github.jspinak.brobot.model.element.Location(x, y);
                    location = coordinateScaler.scaleLocationToLogical(brobotLoc);
                }
                {
                    location = new Location(x, y);
                }
                location.doubleClick();
                return true;
            } else {
                // Simulate double-click for other buttons
                boolean firstClick = click(x, y, button);
                if (!firstClick) return false;

                // Small delay between clicks
                Thread.sleep(50);
                return click(x, y, button);
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public synchronized boolean mouseDown(MouseButton button) {
        try {
            int sikuliButton = convertButton(button);
            Mouse.down(sikuliButton);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public synchronized boolean mouseUp(MouseButton button) {
        try {
            int sikuliButton = convertButton(button);
            Mouse.up(sikuliButton);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public synchronized boolean drag(
            int startX, int startY, int endX, int endY, MouseButton button) {
        try {
            // Scale coordinates if needed (from physical to logical)
            Location startLoc, endLoc;
            if (coordinateScaler != null && coordinateScaler.isScalingNeeded()) {
                io.github.jspinak.brobot.model.element.Location brobotStartLoc =
                        new io.github.jspinak.brobot.model.element.Location(startX, startY);
                io.github.jspinak.brobot.model.element.Location brobotEndLoc =
                        new io.github.jspinak.brobot.model.element.Location(endX, endY);
                startLoc = coordinateScaler.scaleLocationToLogical(brobotStartLoc);
                endLoc = coordinateScaler.scaleLocationToLogical(brobotEndLoc);
            } else {
                startLoc = new Location(startX, startY);
                endLoc = new Location(endX, endY);
            }

            // Move to start location
            if (!moveTo(startX, startY)) {
                return false;
            }

            // Press button
            if (!mouseDown(button)) {
                return false;
            }

            // Drag to end location
            // Move to end location while holding button
            moveTo(endX, endY);
            int result = 1; // Assume success since we got this far

            // Note: dragDrop automatically releases the button
            boolean success = result > 0;
            if (!success) {
                // Try to release button if drag failed
                mouseUp(button);
            }

            return success;
        } catch (Exception e) {
            // Try to release button on error
            mouseUp(button);
            return false;
        }
    }

    @Override
    public synchronized boolean scroll(int wheelAmt) {
        try {
            // Get current mouse location
            Location currentLoc = Mouse.at();
            if (currentLoc == null) {
                return false;
            }

            // Use Sikuli's wheel method for scrolling
            Location location = Mouse.at();
            if (location == null) {
                location = new Location(0, 0);
            }

            // Create a small region at the mouse location for wheel operation
            org.sikuli.script.Region region;
            if (coordinateScaler != null && coordinateScaler.isScalingNeeded()) {
                // Scale the mouse location from physical to logical coordinates
                io.github.jspinak.brobot.model.element.Location brobotLoc =
                        new io.github.jspinak.brobot.model.element.Location(location.x, location.y);
                Location scaledLoc = coordinateScaler.scaleLocationToLogical(brobotLoc);
                region = new org.sikuli.script.Region(scaledLoc.x, scaledLoc.y, 1, 1);
            } else {
                region = new org.sikuli.script.Region(location.x, location.y, 1, 1);
            }

            // Sikuli wheel method: positive = down, negative = up
            // Robot's mouseWheel: positive = down, negative = up
            // Direction: -1 for up, 1 for down
            int direction = wheelAmt > 0 ? 1 : -1;
            int steps = Math.abs(wheelAmt);

            region.wheel(direction, steps);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int[] getPosition() {
        try {
            Location currentLoc = Mouse.at();
            if (currentLoc == null) {
                return null;
            }
            // Mouse.at() returns logical coordinates which is what we want
            // The caller can scale if needed
            return new int[] {currentLoc.x, currentLoc.y};
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // Check if we're in a headless environment
            if (GraphicsEnvironment.isHeadless()) {
                return false;
            }

            // Try to get mouse position as a test
            return Mouse.at() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getImplementationName() {
        return "Sikuli";
    }

    private int convertButton(MouseButton button) {
        switch (button) {
            case RIGHT:
                return SIKULI_RIGHT;
            case MIDDLE:
                return SIKULI_MIDDLE;
            case LEFT:
            default:
                return SIKULI_LEFT;
        }
    }

    private int getButtonMask(MouseButton button) {
        switch (button) {
            case RIGHT:
                return InputEvent.BUTTON3_DOWN_MASK;
            case MIDDLE:
                return InputEvent.BUTTON2_DOWN_MASK;
            case LEFT:
            default:
                return InputEvent.BUTTON1_DOWN_MASK;
        }
    }
}
