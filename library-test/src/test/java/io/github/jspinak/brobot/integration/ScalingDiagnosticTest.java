package io.github.jspinak.brobot.integration;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import io.github.jspinak.brobot.startup.orchestration.ApplicationContextInitializer;
import io.github.jspinak.brobot.test.BrobotTestBase;

@Disabled("CI failure - needs investigation")
public class ScalingDiagnosticTest extends BrobotTestBase {

    @BeforeAll
    public static void setup() {
        // Manually initialize Brobot settings
        MockEnvironment env = new MockEnvironment();
        env.setProperty("brobot.dpi.disable-scaling", "true");
        env.setProperty("brobot.dpi.resize-factor", "0.8");
        env.setProperty("brobot.core.image-path", "images");

        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(env);

        ApplicationContextInitializer initializer = new ApplicationContextInitializer();
        initializer.initialize(context);
    }

    @Test
    public void diagnoseScaling() throws Exception {
        System.out.println("=== SCALING DIAGNOSTIC TEST ===\n");

        // 1. Check current settings
        System.out.println("1. CURRENT SETTINGS:");
        System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("   Settings.MinSimilarity: " + Settings.MinSimilarity);
        System.out.println(
                "   System property 'brobot.dpi.scaling.disabled': "
                        + System.getProperty("brobot.dpi.scaling.disabled"));
        System.out.println(
                "   System property 'sun.java2d.dpiaware': "
                        + System.getProperty("sun.java2d.dpiaware"));

        // 2. Check screen resolution
        System.out.println("\n2. SCREEN RESOLUTION:");
        GraphicsDevice device =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode mode = device.getDisplayMode();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        System.out.println(
                "   Physical (DisplayMode): " + mode.getWidth() + "x" + mode.getHeight());
        System.out.println(
                "   Logical (Toolkit): " + screenSize.getWidth() + "x" + screenSize.getHeight());

        float scaleFactor = (float) screenSize.getWidth() / mode.getWidth();
        System.out.println("   Calculated scale factor: " + scaleFactor);

        // 3. Load a pattern and check its size
        System.out.println("\n3. PATTERN ANALYSIS:");
        ImagePath.setBundlePath("images");
        try {
            Pattern pattern = new Pattern("working/claude-icon-1.png");
            BufferedImage patternImage = pattern.getBImage();
            System.out.println("   Pattern 'working/claude-icon-1.png':");
            System.out.println(
                    "     Original size: "
                            + patternImage.getWidth()
                            + "x"
                            + patternImage.getHeight());

            // Check if pattern is being resized
            if (Settings.AlwaysResize != 1.0f) {
                int resizedWidth = (int) (patternImage.getWidth() * Settings.AlwaysResize);
                int resizedHeight = (int) (patternImage.getHeight() * Settings.AlwaysResize);
                System.out.println(
                        "     Would be resized to: " + resizedWidth + "x" + resizedHeight);
            }
        } catch (Exception e) {
            System.out.println("   Error loading pattern: " + e.getMessage());
        }

        // 4. Capture screen and check resolution
        System.out.println("\n4. SCREEN CAPTURE TEST:");
        try {
            Screen screen = new Screen();
            ScreenImage capture = screen.capture();
            BufferedImage captureImage = capture.getImage();

            System.out.println(
                    "   Capture size: " + captureImage.getWidth() + "x" + captureImage.getHeight());
            System.out.println(
                    "   Matches physical resolution: "
                            + (captureImage.getWidth() == mode.getWidth()));

            // Save a small region for analysis
            Rectangle testRegion = new Rectangle(100, 100, 50, 50);
            ScreenImage regionCapture = screen.capture(testRegion);
            BufferedImage regionImage = regionCapture.getImage();
            System.out.println(
                    "   50x50 region actual size: "
                            + regionImage.getWidth()
                            + "x"
                            + regionImage.getHeight());

        } catch (Exception e) {
            System.out.println("   Error capturing screen: " + e.getMessage());
        }

        // 5. Test pattern matching with different resize factors
        System.out.println("\n5. PATTERN MATCHING TEST:");
        try {
            Pattern testPattern = new Pattern("working/claude-icon-1.png");
            Screen screen = new Screen();
            screen.setAutoWaitTimeout(0.5);

            float[] testFactors = {Settings.AlwaysResize, 1.0f, 0.8f, 1.25f};
            for (float factor : testFactors) {
                Settings.AlwaysResize = factor;
                try {
                    Match match = screen.find(testPattern);
                    System.out.println(
                            "   AlwaysResize="
                                    + factor
                                    + ": FOUND (score="
                                    + String.format("%.3f", match.getScore())
                                    + ")");
                } catch (FindFailed e) {
                    System.out.println("   AlwaysResize=" + factor + ": NOT FOUND");
                }
            }

            // Restore original setting
            Settings.AlwaysResize = testFactors[0];

        } catch (Exception e) {
            System.out.println("   Error in matching test: " + e.getMessage());
        }

        // 6. Recommendations
        System.out.println("\n6. ANALYSIS:");
        if (Math.abs(scaleFactor - 1.0f) > 0.01f) {
            System.out.println("   ⚠ DPI scaling detected!");
            System.out.println("   Windows is scaling at: " + (int) (100 / scaleFactor) + "%");
            System.out.println("   Recommended Settings.AlwaysResize: " + scaleFactor);
        } else {
            System.out.println("   ✓ No DPI scaling detected");
        }

        if (Settings.AlwaysResize != 1.0f) {
            System.out.println(
                    "   ⚠ Patterns are being resized by factor: " + Settings.AlwaysResize);
        } else {
            System.out.println("   ✓ Patterns are not being resized");
        }

        System.out.println("\n=== END DIAGNOSTIC ===");
    }
}
