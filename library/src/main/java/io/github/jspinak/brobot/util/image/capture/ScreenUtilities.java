package io.github.jspinak.brobot.util.image.capture;

import io.github.jspinak.brobot.model.element.Region;
import org.sikuli.script.Screen;

/**
 * Utility class for screen dimension operations and region creation.
 * <p>
 * Provides static methods and fields for accessing screen dimensions and creating
 * screen-sized regions. This class serves as a central point for screen-related
 * operations, caching dimensions for performance and providing convenient region
 * creation methods.
 * <p>
 * Key features:
 * <ul>
 * <li>Static screen dimension caching (w, h fields)</li>
 * <li>Dynamic screen dimension retrieval</li>
 * <li>Full-screen region creation</li>
 * <li>Support for both cached and fresh dimensions</li>
 * </ul>
 * <p>
 * Usage patterns:
 * <ul>
 * <li>Initialize w and h once at startup for consistent dimensions</li>
 * <li>Use getNewScreenWH() when screen resolution might have changed</li>
 * <li>Use getRegion() for quick access to cached full-screen region</li>
 * <li>Use getNewScreenRegion() when fresh dimensions are needed</li>
 * </ul>
 * <p>
 * Thread safety: This class uses static fields without synchronization.
 * The w and h fields should be initialized once at startup and treated
 * as read-only thereafter. For multi-monitor setups or dynamic resolution
 * changes, use the getNew* methods.
 * <p>
 * Note: This class assumes a single primary screen. For multi-monitor
 * support, additional methods would be needed.
 *
 * @see Region
 * @see Screen
 */
public class ScreenUtilities {

    /**
     * Cached screen width in pixels.
     * Should be initialized at application startup and treated as read-only.
     */
    public static int w;
    
    /**
     * Cached screen height in pixels.
     * Should be initialized at application startup and treated as read-only.
     */
    public static int h;

    /**
     * Retrieves current screen dimensions directly from the system.
     * <p>
     * Creates a new Screen instance to get fresh dimensions, useful when:
     * <ul>
     * <li>Screen resolution may have changed</li>
     * <li>Initial dimension retrieval at startup</li>
     * <li>Verifying cached dimensions are still accurate</li>
     * </ul>
     *
     * @return array containing [width, height] in pixels
     */
    public static int[] getNewScreenWH() {
        Screen screen = new Screen();
        int[] wh = new int[2];
        wh[0] = screen.w;
        wh[1] = screen.h;
        return wh;
    }

    /**
     * Creates a region covering the entire screen with fresh dimensions.
     * <p>
     * Queries current screen dimensions and returns a region starting at (0,0)
     * that covers the full screen. Use this method when you need to ensure
     * the region matches current screen dimensions, especially after resolution
     * changes.
     *
     * @return Region covering the entire screen with current dimensions
     */
    public static Region getNewScreenRegion() {
        int[] wh = getNewScreenWH();
        return new Region(0,0,wh[0],wh[1]);
    }

    /**
     * Creates a region using cached screen dimensions.
     * <p>
     * Returns a region covering the entire screen based on the cached
     * w and h values. This is more efficient than getNewScreenRegion()
     * but requires that w and h have been properly initialized.
     * <p>
     * Warning: Returns a region with dimensions (0,0,0,0) if w and h
     * have not been initialized.
     *
     * @return Region covering the entire screen using cached dimensions
     */
    public static Region getRegion() {
        return new Region(0,0,w,h);
    }
}
