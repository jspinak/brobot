package io.github.jspinak.brobot.runner.ui.utils;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Screenshot utility for Wayland environments. Uses system screenshot tools (gnome-screenshot,
 * spectacle, etc.) as fallback.
 */
public class WaylandScreenshotUtil {
    private static final Logger logger = LoggerFactory.getLogger(WaylandScreenshotUtil.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /** Detects if running on Wayland. */
    public static boolean isWayland() {
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String sessionType = System.getenv("XDG_SESSION_TYPE");

        boolean isWayland = waylandDisplay != null || "wayland".equalsIgnoreCase(sessionType);
        logger.info(
                "Wayland detection - WAYLAND_DISPLAY: {}, XDG_SESSION_TYPE: {}, isWayland: {}",
                waylandDisplay,
                sessionType,
                isWayland);

        return isWayland;
    }

    /**
     * Takes a screenshot using system tools that work with Wayland.
     *
     * @param filename The base filename (without extension)
     * @return The file path where the screenshot was saved, or null if failed
     */
    public static CompletableFuture<String> captureWithSystemTool(String filename) {
        return CompletableFuture.supplyAsync(
                () -> {
                    String outputPath = getOutputPath(filename);

                    // Try different screenshot tools in order of preference
                    String[] commands = {
                        // GNOME Screenshot (most common on GNOME/Wayland)
                        String.format("gnome-screenshot -f '%s'", outputPath),

                        // Spectacle (KDE)
                        String.format("spectacle -b -n -o '%s'", outputPath),

                        // Import from ImageMagick (X11 compatibility layer)
                        String.format("import -window root '%s'", outputPath),

                        // grim (Wayland native)
                        String.format("grim '%s'", outputPath),

                        // Using dbus interface for GNOME Shell
                        String.format(
                                "dbus-send --session --type=method_call"
                                        + " --dest=org.gnome.Shell.Screenshot"
                                        + " /org/gnome/Shell/Screenshot"
                                        + " org.gnome.Shell.Screenshot.Screenshot boolean:false"
                                        + " boolean:false string:'%s'",
                                outputPath)
                    };

                    for (String command : commands) {
                        logger.info("Trying screenshot command: {}", command.split(" ")[0]);
                        if (executeScreenshotCommand(command, outputPath)) {
                            return outputPath;
                        }
                    }

                    logger.error("All screenshot methods failed");
                    return null;
                });
    }

    /** Executes a screenshot command and waits for completion. */
    private static boolean executeScreenshotCommand(String command, String expectedOutput) {
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            Process process = pb.start();

            boolean completed = process.waitFor(5, TimeUnit.SECONDS);
            int exitCode = completed ? process.exitValue() : -1;

            if (completed && exitCode == 0) {
                // Give it a moment for the file to be written
                Thread.sleep(500);

                File outputFile = new File(expectedOutput);
                if (outputFile.exists() && outputFile.length() > 0) {
                    logger.info(
                            "Screenshot captured successfully with command: {}",
                            command.split(" ")[0]);
                    return true;
                }
            }

            if (!completed) {
                process.destroyForcibly();
                logger.warn("Screenshot command timed out: {}", command.split(" ")[0]);
            } else {
                logger.warn(
                        "Screenshot command failed with exit code {}: {}",
                        exitCode,
                        command.split(" ")[0]);
            }

        } catch (Exception e) {
            logger.debug("Failed to execute screenshot command: {}", command.split(" ")[0], e);
        }

        return false;
    }

    /** Gets the output path for a screenshot. */
    private static String getOutputPath(String filename) {
        File screenshotDir = new File("runner/screenshots");
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }

        return new File(screenshotDir, filename + ".png").getAbsolutePath();
    }

    /** Takes a screenshot with automatic fallback between X11 and Wayland methods. */
    public static CompletableFuture<String> captureDesktop(String filename) {
        if (isWayland()) {
            logger.info("Wayland detected - using system screenshot tools");
            return captureWithSystemTool(filename);
        } else {
            logger.info("X11 environment - using AWT Robot");
            return DesktopScreenshotUtil.captureAllScreensAsync(filename);
        }
    }
}
