package io.github.jspinak.brobot.util.image.capture;

import org.sikuli.script.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.monitor.MonitorManager;

/**
 * Utility class for screen dimension operations and region creation.
 *
 * <p>Provides static methods and fields for accessing screen dimensions and creating screen-sized
 * regions. This class serves as a central point for screen-related operations, caching dimensions
 * for performance and providing convenient region creation methods.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Static screen dimension caching (w, h fields)
 *   <li>Dynamic screen dimension retrieval
 *   <li>Full-screen region creation
 *   <li>Support for both cached and fresh dimensions
 * </ul>
 *
 * <p>Usage patterns:
 *
 * <ul>
 *   <li>Initialize w and h once at startup for consistent dimensions
 *   <li>Use getNewScreenWH() when screen resolution might have changed
 *   <li>Use getRegion() for quick access to cached full-screen region
 *   <li>Use getNewScreenRegion() when fresh dimensions are needed
 * </ul>
 *
 * <p>Thread safety: This class uses static fields without synchronization. The w and h fields
 * should be initialized once at startup and treated as read-only thereafter. For multi-monitor
 * setups or dynamic resolution changes, use the getNew* methods.
 *
 * <p>Note: This class assumes a single primary screen. For multi-monitor support, additional
 * methods would be needed.
 *
 * @see Region
 * @see Screen
 */
@Component
public class ScreenUtilities {

    /**
     * Cached screen width in pixels. Should be initialized at application startup and treated as
     * read-only.
     */
    public static int w;

    /**
     * Cached screen height in pixels. Should be initialized at application startup and treated as
     * read-only.
     */
    public static int h;

    private static MonitorManager monitorManager;

    @Autowired
    public void setMonitorManager(MonitorManager monitorManager) {
        ScreenUtilities.monitorManager = monitorManager;
    }

    /**
     * Retrieves current screen dimensions directly from the system.
     *
     * <p>Creates a new Screen instance to get fresh dimensions, useful when:
     *
     * <ul>
     *   <li>Screen resolution may have changed
     *   <li>Initial dimension retrieval at startup
     *   <li>Verifying cached dimensions are still accurate
     * </ul>
     *
     * @return array containing [width, height] in pixels
     */
    public static int[] getNewScreenWH() {
        try {
            Screen screen = new Screen();
            int[] wh = new int[2];
            wh[0] = screen.w;
            wh[1] = screen.h;
            return wh;
        } catch (Exception e) {
            // In headless mode, return default dimensions
            return new int[] {1920, 1080};
        }
    }

    /**
     * Creates a region covering the entire screen with fresh dimensions.
     *
     * <p>Queries current screen dimensions and returns a region starting at (0,0) that covers the
     * full screen. Use this method when you need to ensure the region matches current screen
     * dimensions, especially after resolution changes.
     *
     * @return Region covering the entire screen with current dimensions
     */
    public static Region getNewScreenRegion() {
        int[] wh = getNewScreenWH();
        return new Region(0, 0, wh[0], wh[1]);
    }

    /**
     * Creates a region using cached screen dimensions.
     *
     * <p>Returns a region covering the entire screen based on the cached w and h values. This is
     * more efficient than getNewScreenRegion() but requires that w and h have been properly
     * initialized.
     *
     * <p>Warning: Returns a region with dimensions (0,0,0,0) if w and h have not been initialized.
     *
     * @return Region covering the entire screen using cached dimensions
     */
    public static Region getRegion() {
        return new Region(0, 0, w, h);
    }

    /**
     * Gets screen dimensions for a specific monitor.
     *
     * @param monitorIndex The monitor index (0-based)
     * @return array containing [width, height] in pixels
     */
    public static int[] getMonitorDimensions(int monitorIndex) {
        Screen screen =
                monitorManager != null
                        ? monitorManager.getScreen(monitorIndex)
                        : new Screen(monitorIndex);
        return new int[] {screen.w, screen.h};
    }

    /**
     * Creates a region covering the entire specified monitor.
     *
     * @param monitorIndex The monitor index (0-based)
     * @return Region covering the specified monitor
     */
    public static Region getMonitorRegion(int monitorIndex) {
        if (monitorManager != null && monitorManager.isValidMonitorIndex(monitorIndex)) {
            MonitorManager.MonitorInfo info = monitorManager.getMonitorInfo(monitorIndex);
            return new Region(info.getX(), info.getY(), info.getWidth(), info.getHeight());
        }
        // Fallback to Sikuli Screen
        Screen screen = new Screen(monitorIndex);
        return new Region(screen.x, screen.y, screen.w, screen.h);
    }

    /**
     * Gets the Screen object for a specific monitor.
     *
     * @param monitorIndex The monitor index (0-based)
     * @return Screen object for the specified monitor
     */
    public static Screen getScreen(int monitorIndex) {
        return monitorManager != null
                ? monitorManager.getScreen(monitorIndex)
                : new Screen(monitorIndex);
    }

    /**
     * Gets the Screen object based on operation context.
     *
     * @param operationName Operation name for monitor assignment
     * @return Appropriate Screen object
     */
    public static Screen getScreen(String operationName) {
        if (monitorManager != null) {
            return monitorManager.getScreen(operationName);
        }

        try {
            return new Screen();
        } catch (Exception e) {
            // In headless mode, return null
            return null;
        }
    }

    /**
     * Gets all available screens for multi-monitor operations.
     *
     * @return List of all Screen objects
     */
    public static java.util.List<Screen> getAllScreens() {
        if (monitorManager != null) {
            return monitorManager.getAllScreens();
        }
        // Fallback: create screens based on Sikuli's Screen.getNumberScreens()
        java.util.List<Screen> screens = new java.util.ArrayList<>();
        int numScreens = Screen.getNumberScreens();
        for (int i = 0; i < numScreens; i++) {
            screens.add(new Screen(i));
        }
        return screens;
    }
}
