package io.github.jspinak.brobot.runner.ui.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Desktop screenshot utility using SikuliX Screen API. Uses SikuliX Screen for cross-platform
 * screenshot capture. Runs captures in separate threads to avoid UI freezing.
 */
public class DesktopScreenshotUtil {
    private static final Logger logger = LoggerFactory.getLogger(DesktopScreenshotUtil.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final ExecutorService executor =
            Executors.newCachedThreadPool(
                    r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        t.setName("DesktopScreenshot-" + t.getId());
                        return t;
                    });

    /**
     * Captures the entire primary screen asynchronously.
     *
     * @param filename The base filename (without extension)
     * @return CompletableFuture with the file path where the screenshot was saved
     */
    public static CompletableFuture<String> capturePrimaryScreenAsync(String filename) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        // Small delay to ensure UI is not blocked
                        Thread.sleep(100);

                        // Get primary screen dimensions
                        Rectangle screenRect =
                                new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        logger.info(
                                "Capturing primary screen: {}x{}",
                                screenRect.width,
                                screenRect.height);
                        return captureRegionInternal(screenRect, filename);
                    } catch (Exception e) {
                        logger.error("Failed to capture primary screen", e);
                        return null;
                    }
                },
                executor);
    }

    /**
     * Captures the entire primary screen (blocking).
     *
     * @param filename The base filename (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static String capturePrimaryScreen(String filename) {
        try {
            return capturePrimaryScreenAsync(filename).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Failed to capture primary screen", e);
            return null;
        }
    }

    /**
     * Captures all screens in a multi-monitor setup asynchronously. Automatically detects and
     * handles WSL/Wayland environments.
     *
     * @param filename The base filename (without extension)
     * @return CompletableFuture with the file path where the screenshot was saved
     */
    public static CompletableFuture<String> captureAllScreensAsync(String filename) {
        // Check if we're in WSL first (most specific case)
        if (WSLScreenshotUtil.isWSL()) {
            logger.info("WSL environment detected - using WSL-specific screenshot methods");
            return WSLScreenshotUtil.captureDesktop(filename);
        }

        // Check if we're on Wayland
        if (WaylandScreenshotUtil.isWayland()) {
            logger.info("Wayland environment detected - using system screenshot tools");
            return WaylandScreenshotUtil.captureDesktop(filename);
        }

        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        // Small delay to ensure UI is not blocked
                        Thread.sleep(100);

                        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        GraphicsDevice[] screens = ge.getScreenDevices();

                        Rectangle allScreenBounds = new Rectangle();
                        for (GraphicsDevice screen : screens) {
                            Rectangle bounds = screen.getDefaultConfiguration().getBounds();
                            allScreenBounds = allScreenBounds.union(bounds);
                        }

                        logger.info(
                                "Capturing all screens: {}x{} at ({}, {})",
                                allScreenBounds.width,
                                allScreenBounds.height,
                                allScreenBounds.x,
                                allScreenBounds.y);

                        return captureRegionInternal(allScreenBounds, filename);
                    } catch (Exception e) {
                        logger.error("Failed to capture all screens", e);
                        return null;
                    }
                },
                executor);
    }

    /**
     * Captures all screens in a multi-monitor setup (blocking).
     *
     * @param filename The base filename (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static String captureAllScreens(String filename) {
        try {
            return captureAllScreensAsync(filename).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Failed to capture all screens", e);
            return null;
        }
    }

    /**
     * Captures a specific monitor by index.
     *
     * @param monitorIndex The monitor index (0 for primary)
     * @param filename The base filename (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static String captureMonitor(int monitorIndex, String filename) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            if (monitorIndex < 0 || monitorIndex >= screens.length) {
                logger.error(
                        "Invalid monitor index: {}. Available monitors: {}",
                        monitorIndex,
                        screens.length);
                return null;
            }

            Rectangle bounds = screens[monitorIndex].getDefaultConfiguration().getBounds();
            return captureRegion(bounds, filename);
        } catch (Exception e) {
            logger.error("Failed to capture monitor {}", monitorIndex, e);
            return null;
        }
    }

    /**
     * Captures a specific region of the screen (blocking).
     *
     * @param region The region to capture
     * @param filename The base filename (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static String captureRegion(Rectangle region, String filename) {
        try {
            return CompletableFuture.supplyAsync(
                            () -> {
                                try {
                                    Thread.sleep(100);
                                    return captureRegionInternal(region, filename);
                                } catch (Exception e) {
                                    logger.error("Failed to capture region", e);
                                    return null;
                                }
                            },
                            executor)
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Failed to capture region", e);
            return null;
        }
    }

    /** Internal method to capture a region - must be called from executor thread. */
    private static String captureRegionInternal(Rectangle region, String filename) {
        try {
            // Create SikuliX screen instance
            Screen screen = new Screen();

            // Wait to ensure screen content is rendered
            Thread.sleep(200);

            logger.info(
                    "Capturing region: x={}, y={}, width={}, height={}",
                    region.x,
                    region.y,
                    region.width,
                    region.height);

            BufferedImage capture;
            if (region.x == 0
                    && region.y == 0
                    && region.width == Toolkit.getDefaultToolkit().getScreenSize().width
                    && region.height == Toolkit.getDefaultToolkit().getScreenSize().height) {
                // Full screen capture
                ScreenImage screenImage = screen.capture();
                capture = screenImage.getImage();
            } else {
                // Region capture
                Region sikuliRegion = new Region(region.x, region.y, region.width, region.height);
                ScreenImage screenImage = screen.capture(sikuliRegion);
                capture = screenImage.getImage();
            }

            // Check if image is valid
            if (isImageBlack(capture)) {
                logger.warn(
                        "Captured image appears to be all black - retrying with different"
                                + " approach");

                // Try alternative capture method - full screen
                Thread.sleep(500); // Wait longer
                ScreenImage screenImage = screen.capture();
                capture = screenImage.getImage();

                // If we needed a region, crop the full screen capture
                if (!(region.x == 0
                        && region.y == 0
                        && region.width == Toolkit.getDefaultToolkit().getScreenSize().width
                        && region.height == Toolkit.getDefaultToolkit().getScreenSize().height)) {
                    capture = capture.getSubimage(region.x, region.y, region.width, region.height);
                }
            }

            return saveImage(capture, filename);
        } catch (Exception e) {
            logger.error("Failed to capture region internally", e);
            return null;
        }
    }

    /** Checks if an image is all black (common issue with screen capture). */
    private static boolean isImageBlack(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int sampleSize = Math.min(100, width * height);

        for (int i = 0; i < sampleSize; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            int rgb = image.getRGB(x, y);

            // If any pixel is not black, image is not all black
            if (rgb != 0xFF000000 && rgb != 0) {
                return false;
            }
        }

        logger.warn("Image appears to be all black");
        return true;
    }

    /**
     * Captures the desktop with automatic timestamp naming.
     *
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static String captureWithTimestamp() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = "desktop-screenshot-" + timestamp;
        return captureAllScreens(filename);
    }

    /**
     * Saves a BufferedImage to a PNG file.
     *
     * @param image The image to save
     * @param filename The base filename (without extension)
     * @return The file path where the image was saved, or null if failed
     */
    private static String saveImage(BufferedImage image, String filename) {
        try {
            // Create screenshots directory if it doesn't exist
            File screenshotDir = new File("runner/screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            // Save to file
            File outputFile = new File(screenshotDir, filename + ".png");
            ImageIO.write(image, "png", outputFile);

            String absolutePath = outputFile.getAbsolutePath();
            logger.info("Desktop screenshot saved to: {}", absolutePath);
            return absolutePath;

        } catch (IOException e) {
            logger.error("Failed to save screenshot", e);
            return null;
        }
    }

    /** Lists available monitors with their bounds. */
    public static void listMonitors() {
        // Check if running in headless mode
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        logger.info("Headless mode: {}", isHeadless);
        logger.info("java.awt.headless: {}", System.getProperty("java.awt.headless"));
        logger.info("DISPLAY env var: {}", System.getenv("DISPLAY"));

        if (isHeadless) {
            logger.error("Running in headless mode - screen capture not available");
            return;
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        logger.info("Available monitors: {}", screens.length);
        for (int i = 0; i < screens.length; i++) {
            GraphicsDevice device = screens[i];
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            logger.info(
                    "Monitor {}: x={}, y={}, width={}, height={}, ID: {}",
                    i,
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    device.getIDstring());

            // Log color model info
            logger.info("  Color model: {}", device.getDefaultConfiguration().getColorModel());
            logger.info("  Full screen supported: {}", device.isFullScreenSupported());
        }
    }

    /**
     * Initializes screenshot capability and checks environment. Call this before attempting
     * screenshots to diagnose issues.
     */
    public static boolean initialize() {
        try {
            listMonitors();

            // Try to create a screen instance to test permissions
            Screen testScreen = new Screen();
            logger.info("SikuliX Screen created successfully - screen capture should work");

            // Test a small capture
            Region testRegion = new Region(0, 0, 10, 10);
            ScreenImage testScreenImage = testScreen.capture(testRegion);
            BufferedImage testImage = testScreenImage.getImage();
            if (testImage != null) {
                logger.info("Test capture successful");
                return true;
            } else {
                logger.error("Test capture returned null");
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to initialize screenshot capability", e);
            return false;
        }
    }
}
