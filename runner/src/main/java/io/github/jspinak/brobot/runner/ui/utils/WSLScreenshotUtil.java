package io.github.jspinak.brobot.runner.ui.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Screenshot utility specifically for WSL2 environments. WSL2 with WSLg uses Wayland, which
 * prevents traditional Java screenshot methods. This utility provides workarounds and clear error
 * messages.
 */
public class WSLScreenshotUtil {
    private static final Logger logger = LoggerFactory.getLogger(WSLScreenshotUtil.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /** Detects if running in WSL. */
    public static boolean isWSL() {
        try {
            ProcessBuilder pb = new ProcessBuilder("uname", "-r");
            Process process = pb.start();

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                boolean isWSL =
                        line != null && (line.contains("microsoft") || line.contains("WSL"));

                if (isWSL) {
                    logger.info("WSL environment detected: {}", line);
                }

                return isWSL;
            }
        } catch (Exception e) {
            logger.debug("Failed to detect WSL", e);
            return false;
        }
    }

    /**
     * Attempts to take a screenshot using Windows tools through WSL interop. This requires
     * PowerShell to be accessible from WSL.
     */
    public static CompletableFuture<String> captureUsingWindowsTools(String filename) {
        return CompletableFuture.supplyAsync(
                () -> {
                    String outputPath = getOutputPath(filename);

                    // Convert WSL path to Windows path
                    String windowsPath = convertToWindowsPath(outputPath);
                    if (windowsPath == null) {
                        logger.error("Failed to convert WSL path to Windows path");
                        return null;
                    }

                    // PowerShell command to take screenshot
                    String psCommand =
                            String.format(
                                    "[System.Reflection.Assembly]::LoadWithPartialName('System.Windows.Forms');"
                                        + " [System.Windows.Forms.Screen]::PrimaryScreen |"
                                        + " ForEach-Object { $bounds = $_.Bounds; $bitmap ="
                                        + " New-Object System.Drawing.Bitmap($bounds.Width,"
                                        + " $bounds.Height); $graphics ="
                                        + " [System.Drawing.Graphics]::FromImage($bitmap);"
                                        + " $graphics.CopyFromScreen($bounds.Location,"
                                        + " [System.Drawing.Point]::Empty, $bounds.Size);"
                                        + " $bitmap.Save('%s',"
                                        + " [System.Drawing.Imaging.ImageFormat]::Png);"
                                        + " $bitmap.Dispose(); $graphics.Dispose(); }",
                                    windowsPath.replace("'", "''"));

                    try {
                        ProcessBuilder pb =
                                new ProcessBuilder(
                                        "powershell.exe", "-NoProfile", "-Command", psCommand);

                        Process process = pb.start();
                        boolean completed = process.waitFor(10, TimeUnit.SECONDS);

                        if (completed && process.exitValue() == 0) {
                            // Give it a moment for the file to be written
                            Thread.sleep(500);

                            File outputFile = new File(outputPath);
                            if (outputFile.exists() && outputFile.length() > 0) {
                                logger.info(
                                        "Screenshot captured successfully using Windows"
                                                + " PowerShell");
                                return outputPath;
                            }
                        }

                        // Read error output
                        try (BufferedReader errorReader =
                                new BufferedReader(
                                        new InputStreamReader(process.getErrorStream()))) {
                            String line;
                            while ((line = errorReader.readLine()) != null) {
                                logger.error("PowerShell error: {}", line);
                            }
                        }

                    } catch (Exception e) {
                        logger.error("Failed to execute Windows screenshot command", e);
                    }

                    return null;
                });
    }

    /** Converts a WSL path to Windows path. */
    private static String convertToWindowsPath(String wslPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("wslpath", "-w", wslPath);
            Process process = pb.start();

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String windowsPath = reader.readLine();
                logger.debug("Converted WSL path {} to Windows path {}", wslPath, windowsPath);
                return windowsPath;
            }
        } catch (Exception e) {
            logger.error("Failed to convert WSL path to Windows path", e);
            return null;
        }
    }

    /** Gets the output path for a screenshot. */
    private static String getOutputPath(String filename) {
        File screenshotDir = new File("runner/screenshots");
        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs();
        }

        return new File(screenshotDir, filename + ".png").getAbsolutePath();
    }

    /** Provides instructions for manual screenshot setup in WSL. */
    public static String getWSLScreenshotInstructions() {
        return """
            WSL2 Screenshot Limitations:

            WSL2 with WSLg uses Wayland, which prevents direct screen capture from Java.

            Options to capture desktop screenshots:

            1. Use Windows screenshot tools:
               - Press Windows+Shift+S to use Snipping Tool
               - Press PrtScn to capture full screen
               - Save the file manually to the runner/screenshots directory

            2. Install screenshot tools in WSL:
               sudo apt update
               sudo apt install gnome-screenshot

            3. Run the Brobot Runner directly on Windows instead of WSL

            4. Use X11 forwarding instead of WSLg:
               - Install an X server on Windows (e.g., VcXsrv, Xming)
               - Set DISPLAY environment variable
               - Disable WSLg in .wslconfig

            Note: The application can still capture screenshots of its own JavaFX windows.
            """;
    }

    /** Attempts to capture desktop screenshot with WSL-specific handling. */
    public static CompletableFuture<String> captureDesktop(String filename) {
        logger.warn("Desktop screenshot requested in WSL environment");

        // First try Windows PowerShell method
        return captureUsingWindowsTools(filename)
                .thenCompose(
                        result -> {
                            if (result != null) {
                                return CompletableFuture.completedFuture(result);
                            }

                            // If that fails, try Wayland method
                            logger.info("Windows screenshot failed, trying Wayland method");
                            return WaylandScreenshotUtil.captureWithSystemTool(filename);
                        })
                .thenApply(
                        result -> {
                            if (result == null) {
                                logger.error("All WSL screenshot methods failed");
                                logger.info("\n{}", getWSLScreenshotInstructions());
                            }
                            return result;
                        });
    }
}
