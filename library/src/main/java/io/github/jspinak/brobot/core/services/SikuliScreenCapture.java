package io.github.jspinak.brobot.core.services;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.sikuli.script.Mouse;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Region;

/**
 * Sikuli-based implementation of the ScreenCaptureService interface.
 *
 * <p>This implementation uses Sikuli's screen capture capabilities, which internally use
 * platform-specific methods for efficient capture. It is completely independent of the Find action
 * and other high-level Brobot components.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li>No dependencies on Find or Action classes
 *   <li>Thread-safe through stateless operations
 *   <li>Multi-monitor support
 *   <li>Efficient region capture
 * </ul>
 *
 * @since 2.0.0
 */
@Component
public class SikuliScreenCapture implements ScreenCaptureService {

    private final List<Screen> screens;

    public SikuliScreenCapture() {
        this.screens = initializeScreens();
    }

    private List<Screen> initializeScreens() {
        List<Screen> screenList = new ArrayList<>();
        int numScreens = Screen.getNumberScreens();
        for (int i = 0; i < numScreens; i++) {
            screenList.add(new Screen(i));
        }
        return screenList;
    }

    @Override
    public BufferedImage captureScreen() {
        try {
            Screen primaryScreen = getPrimaryScreen();
            org.sikuli.script.ScreenImage screenImage = primaryScreen.capture();
            return screenImage.getImage();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public BufferedImage captureRegion(int x, int y, int width, int height) {
        try {
            // Find which screen contains this region
            Screen targetScreen = findScreenForRegion(x, y, width, height);
            if (targetScreen == null) {
                return null;
            }

            org.sikuli.script.Region sikuliRegion =
                    new org.sikuli.script.Region(x, y, width, height);
            org.sikuli.script.ScreenImage screenImage = targetScreen.capture(sikuliRegion);
            return screenImage.getImage();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public BufferedImage captureActiveScreen() {
        try {
            // Get the screen containing the mouse cursor
            Screen activeScreen = Mouse.at().getMonitor();
            if (activeScreen == null) {
                // Fallback to primary screen
                return captureScreen();
            }

            org.sikuli.script.ScreenImage screenImage = activeScreen.capture();
            return screenImage.getImage();
        } catch (Exception e) {
            return captureScreen(); // Fallback to primary screen
        }
    }

    @Override
    public BufferedImage captureMonitor(int monitorIndex) {
        if (monitorIndex < 0 || monitorIndex >= screens.size()) {
            return null;
        }

        try {
            Screen screen = screens.get(monitorIndex);
            org.sikuli.script.ScreenImage screenImage = screen.capture();
            return screenImage.getImage();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getMonitorCount() {
        return screens.size();
    }

    @Override
    public Region getMonitorBounds(int monitorIndex) {
        if (monitorIndex < 0 || monitorIndex >= screens.size()) {
            return null;
        }

        try {
            Screen screen = screens.get(monitorIndex);
            Rectangle bounds = screen.getBounds();
            return new Region(bounds.x, bounds.y, bounds.width, bounds.height);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Region getVirtualDesktopBounds() {
        if (screens.isEmpty()) {
            return null;
        }

        // Calculate the bounding box of all screens
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Screen screen : screens) {
            Rectangle bounds = screen.getBounds();
            minX = Math.min(minX, bounds.x);
            minY = Math.min(minY, bounds.y);
            maxX = Math.max(maxX, bounds.x + bounds.width);
            maxY = Math.max(maxY, bounds.y + bounds.height);
        }

        return new Region(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Check if we're in a headless environment
            if (GraphicsEnvironment.isHeadless()) {
                return false;
            }

            // Try to get screen information
            return Screen.getNumberScreens() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getImplementationName() {
        return "Sikuli";
    }

    private Screen getPrimaryScreen() {
        // Screen 0 is typically the primary screen
        return screens.isEmpty() ? new Screen() : screens.get(0);
    }

    private Screen findScreenForRegion(int x, int y, int width, int height) {
        // Find which screen best contains this region
        for (Screen screen : screens) {
            Rectangle bounds = screen.getBounds();
            // Check if the region's top-left corner is within this screen
            if (bounds.contains(x, y)) {
                return screen;
            }
        }

        // If no screen contains the top-left corner, use primary screen
        return getPrimaryScreen();
    }
}
