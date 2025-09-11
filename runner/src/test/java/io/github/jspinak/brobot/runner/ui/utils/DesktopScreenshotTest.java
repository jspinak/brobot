package io.github.jspinak.brobot.runner.ui.utils;

import java.io.File;

/**
 * Simple test to verify desktop screenshot functionality. Run this with: java -cp
 * "runner/build/classes/java/main" io.github.jspinak.brobot.runner.ui.utils.DesktopScreenshotTest
 */
public class DesktopScreenshotTest {

    public static void main(String[] args) {
        System.out.println("Testing Desktop Screenshot Utility...");

        // List available monitors
        System.out.println("\n1. Listing available monitors:");
        DesktopScreenshotUtil.listMonitors();

        // Test primary screen capture
        System.out.println("\n2. Capturing primary screen...");
        String primaryPath = DesktopScreenshotUtil.capturePrimaryScreen("test-primary-screen");
        if (primaryPath != null && new File(primaryPath).exists()) {
            System.out.println("✓ Primary screen captured: " + primaryPath);
        } else {
            System.out.println("✗ Failed to capture primary screen");
        }

        // Test all screens capture
        System.out.println("\n3. Capturing all screens...");
        String allScreensPath = DesktopScreenshotUtil.captureAllScreens("test-all-screens");
        if (allScreensPath != null && new File(allScreensPath).exists()) {
            System.out.println("✓ All screens captured: " + allScreensPath);
            System.out.println("  File size: " + new File(allScreensPath).length() + " bytes");
        } else {
            System.out.println("✗ Failed to capture all screens");
        }

        // Test timestamp capture
        System.out.println("\n4. Capturing with timestamp...");
        String timestampPath = DesktopScreenshotUtil.captureWithTimestamp();
        if (timestampPath != null && new File(timestampPath).exists()) {
            System.out.println("✓ Timestamp capture successful: " + timestampPath);
        } else {
            System.out.println("✗ Failed to capture with timestamp");
        }

        System.out.println("\nTest complete!");
    }
}
