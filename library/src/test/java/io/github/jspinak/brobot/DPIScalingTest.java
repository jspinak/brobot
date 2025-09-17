package io.github.jspinak.brobot;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.*;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

@DisabledInCI
public class DPIScalingTest extends BrobotTestBase {

    @Test
    public void testScalingSettings() throws Exception {
        System.out.println("=== DPI Scaling Test ===");

        // Skip this test in mock/headless mode as it requires real display
        assumeFalse(
                true /* mock mode enabled in tests */,
                "Skipping DPI scaling test in mock mode - requires real display");
        assumeFalse(
                GraphicsEnvironment.isHeadless(),
                "Skipping DPI scaling test in headless environment");

        // Check current display settings
        GraphicsDevice device =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        DisplayMode displayMode = device.getDisplayMode();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        System.out.println(
                "Physical resolution: " + displayMode.getWidth() + "x" + displayMode.getHeight());
        System.out.println(
                "Logical resolution: " + screenSize.getWidth() + "x" + screenSize.getHeight());

        // Check SikuliX settings
        System.out.println("\n=== SikuliX Settings ===");
        System.out.println("Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("Settings.MinSimilarity: " + Settings.MinSimilarity);

        // Set up ImagePath
        ImagePath.setBundlePath("images");

        // Try to load and match a pattern
        System.out.println("\n=== Pattern Test ===");
        try {
            Pattern pattern = new Pattern("working/claude-icon-1.png");
            System.out.println("Pattern loaded successfully");
            System.out.println(
                    "Pattern size: "
                            + pattern.getBImage().getWidth()
                            + "x"
                            + pattern.getBImage().getHeight());

            // Try different resize factors
            float[] resizeFactors = {1.0f, 0.8f, 0.667f, 0.571f};

            for (float factor : resizeFactors) {
                Settings.AlwaysResize = factor;
                System.out.println("\nTesting with AlwaysResize = " + factor);

                // Create a resized pattern
                Pattern resizedPattern = pattern.resize(factor);
                System.out.println(
                        "  Resized pattern size: "
                                + (int) (pattern.getBImage().getWidth() * factor)
                                + "x"
                                + (int) (pattern.getBImage().getHeight() * factor));

                // Try to find it on screen
                Screen screen = new Screen();
                screen.setAutoWaitTimeout(0.1); // Quick timeout for testing

                try {
                    Match match = screen.find(resizedPattern);
                    if (match != null) {
                        System.out.println("  ✓ Found match with score: " + match.getScore());
                        System.out.println("  Match size: " + match.w + "x" + match.h);
                    }
                } catch (Exception e) {
                    System.out.println("  ✗ No match found");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\n=== Recommendations ===");
        float scaleFactor = (float) screenSize.getWidth() / displayMode.getWidth();
        if (scaleFactor < 1.0f) {
            System.out.println("DPI scaling detected: " + (int) ((1 / scaleFactor) * 100) + "%");
            System.out.println("Recommended Settings.AlwaysResize = " + scaleFactor);
        } else {
            System.out.println("No DPI scaling detected or unable to determine");
        }
    }
}
