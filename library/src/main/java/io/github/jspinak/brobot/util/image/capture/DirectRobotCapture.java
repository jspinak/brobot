package io.github.jspinak.brobot.util.image.capture;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.core.services.SikuliScreenCapture;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import lombok.extern.slf4j.Slf4j;

/**
 * Direct screen capture using SikuliX Screen API. This ensures proper Brobot-compliant screen
 * capture without using Robot.
 *
 * <p>This class wraps SikuliX screen capture functionality to provide direct screen capture without
 * additional transformations.
 *
 * @since 1.1.0
 */
@Slf4j
@Component
public class DirectRobotCapture {

    private final SikuliScreenCapture screenCapture;

    @Autowired
    public DirectRobotCapture(SikuliScreenCapture screenCapture) {
        this.screenCapture = screenCapture;
        ConsoleReporter.println("[DIRECT CAPTURE] SikuliX screen capture initialized successfully");
    }

    // For backward compatibility with tests that use no-arg constructor
    public DirectRobotCapture() {
        this.screenCapture = new SikuliScreenCapture();
        ConsoleReporter.println("[DIRECT CAPTURE] SikuliX screen capture initialized successfully");
    }

    /**
     * Captures a screen region using SikuliX Screen API. NO additional scaling, NO DPI adjustments,
     * NO transformations.
     *
     * @param x X coordinate in actual screen pixels
     * @param y Y coordinate in actual screen pixels
     * @param width Width in actual screen pixels
     * @param height Height in actual screen pixels
     * @return Raw captured image at actual pixel resolution
     */
    public BufferedImage captureRegion(int x, int y, int width, int height) {
        if (screenCapture == null) {
            ConsoleReporter.println("[DIRECT CAPTURE] Screen capture not initialized!");
            return null;
        }

        // Validate dimensions
        if (width <= 0 || height <= 0) {
            ConsoleReporter.println("[DIRECT CAPTURE] Invalid dimensions: " + width + "x" + height);
            return null;
        }

        try {
            ConsoleReporter.println(
                    "[DIRECT CAPTURE] Capturing region: "
                            + x
                            + ","
                            + y
                            + " "
                            + width
                            + "x"
                            + height);

            BufferedImage captured = screenCapture.captureRegion(x, y, width, height);

            if (captured != null) {
                ConsoleReporter.println(
                        "[DIRECT CAPTURE] Success: "
                                + captured.getWidth()
                                + "x"
                                + captured.getHeight()
                                + " type="
                                + getImageType(captured.getType()));

                // Log pixel sample to verify content
                if (captured.getWidth() > 0 && captured.getHeight() > 0) {
                    int centerX = captured.getWidth() / 2;
                    int centerY = captured.getHeight() / 2;
                    int rgb = captured.getRGB(centerX, centerY);
                    ConsoleReporter.println(
                            "[DIRECT CAPTURE] Center pixel RGB: "
                                    + String.format("#%06X", rgb & 0xFFFFFF));
                }
            } else {
                ConsoleReporter.println("[DIRECT CAPTURE] Capture returned null");
            }

            return captured;

        } catch (Exception e) {
            ConsoleReporter.println("[DIRECT CAPTURE] ERROR: " + e.getMessage());
            log.error("Direct capture failed", e);
            return null;
        }
    }

    /**
     * Captures the full screen using SikuliX Screen API.
     *
     * @return Full screen capture at actual resolution
     */
    public BufferedImage captureFullScreen() {
        if (screenCapture == null) {
            ConsoleReporter.println("[DIRECT CAPTURE] Screen capture not initialized!");
            return null;
        }

        try {
            ConsoleReporter.println("[DIRECT CAPTURE] Capturing full screen");

            BufferedImage captured = screenCapture.captureScreen();

            if (captured != null) {
                ConsoleReporter.println(
                        "[DIRECT CAPTURE] Full screen captured: "
                                + captured.getWidth()
                                + "x"
                                + captured.getHeight());
            } else {
                ConsoleReporter.println("[DIRECT CAPTURE] Full screen capture returned null");
            }

            return captured;

        } catch (Exception e) {
            log.error("Full screen capture failed", e);
            return null;
        }
    }

    /**
     * Compares direct Robot capture with SikuliX capture. Useful for debugging scaling issues.
     *
     * @param sikuliCapture Image captured by SikuliX
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width
     * @param height Height
     */
    public void compareWithSikuliCapture(
            BufferedImage sikuliCapture, int x, int y, int width, int height) {
        BufferedImage directCapture = captureRegion(x, y, width, height);

        if (directCapture == null || sikuliCapture == null) {
            ConsoleReporter.println("[DIRECT CAPTURE] Cannot compare - null capture");
            return;
        }

        ConsoleReporter.println("[DIRECT CAPTURE] Comparison:");
        ConsoleReporter.println(
                "  Direct Robot: " + directCapture.getWidth() + "x" + directCapture.getHeight());
        ConsoleReporter.println(
                "  SikuliX:      " + sikuliCapture.getWidth() + "x" + sikuliCapture.getHeight());

        if (directCapture.getWidth() != sikuliCapture.getWidth()
                || directCapture.getHeight() != sikuliCapture.getHeight()) {
            ConsoleReporter.println("  WARNING: Size mismatch! SikuliX might be scaling!");
        }

        // Sample pixel comparison
        int sampleX =
                Math.min(10, Math.min(directCapture.getWidth() - 1, sikuliCapture.getWidth() - 1));
        int sampleY =
                Math.min(
                        10, Math.min(directCapture.getHeight() - 1, sikuliCapture.getHeight() - 1));

        int directRGB = directCapture.getRGB(sampleX, sampleY);
        int sikuliRGB = sikuliCapture.getRGB(sampleX, sampleY);

        if (directRGB != sikuliRGB) {
            ConsoleReporter.println("  Pixel difference at (" + sampleX + "," + sampleY + "):");
            ConsoleReporter.println("    Direct: " + String.format("#%06X", directRGB & 0xFFFFFF));
            ConsoleReporter.println("    Sikuli: " + String.format("#%06X", sikuliRGB & 0xFFFFFF));
        }
    }

    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "BGR";
            default:
                return "Type" + type;
        }
    }
}
