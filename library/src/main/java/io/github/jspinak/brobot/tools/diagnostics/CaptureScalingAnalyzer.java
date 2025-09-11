package io.github.jspinak.brobot.tools.diagnostics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

/**
 * Analyzes why SikuliX captures at 1536x864 instead of 1920x1080. Tests various theories about the
 * 0.8 scaling factor.
 */
public class CaptureScalingAnalyzer {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void main(String[] args) throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CAPTURE SCALING ANALYZER");
        System.out.println("Understanding why SikuliX captures at 80% resolution");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=".repeat(80));

        // Get system information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        System.out.println("\n1. SYSTEM INFORMATION:");
        System.out.println("   Physical Resolution: 1920x1080 (confirmed by user)");
        System.out.println("   Display Mode: " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println("   Windows DPI: 125% (but physical = logical in this case)");

        double scaleX = gc.getDefaultTransform().getScaleX();
        double scaleY = gc.getDefaultTransform().getScaleY();
        System.out.println("   Java Transform Scale: " + scaleX + "x" + scaleY);

        // Initialize SikuliX
        Screen screen = new Screen();
        Rectangle sikuliBounds = screen.getBounds();

        System.out.println("\n2. SIKULIX CONFIGURATION:");
        System.out.println("   SikuliX Bounds: " + sikuliBounds);
        System.out.println("   Current Settings.AlwaysResize: " + Settings.AlwaysResize);

        // Capture and analyze
        System.out.println("\n3. CAPTURE ANALYSIS:");
        System.out.println("-".repeat(70));

        // Test with AlwaysResize = 0.0 (SikuliX default)
        Settings.AlwaysResize = 0.0f;
        BufferedImage capture0 = screen.capture().getImage();
        System.out.println("\n   With Settings.AlwaysResize = 0.0:");
        System.out.println("   Capture: " + capture0.getWidth() + "x" + capture0.getHeight());
        System.out.println(
                "   Ratio to 1920x1080: "
                        + String.format("%.3f", (double) capture0.getWidth() / 1920));

        // Test with AlwaysResize = 1.0 (no resize)
        Settings.AlwaysResize = 1.0f;
        BufferedImage capture1 = screen.capture().getImage();
        System.out.println("\n   With Settings.AlwaysResize = 1.0:");
        System.out.println("   Capture: " + capture1.getWidth() + "x" + capture1.getHeight());
        System.out.println(
                "   Ratio to 1920x1080: "
                        + String.format("%.3f", (double) capture1.getWidth() / 1920));

        // Test with AlwaysResize = 1.25 (inverse of 0.8)
        Settings.AlwaysResize = 1.25f;
        BufferedImage capture125 = screen.capture().getImage();
        System.out.println("\n   With Settings.AlwaysResize = 1.25:");
        System.out.println("   Capture: " + capture125.getWidth() + "x" + capture125.getHeight());
        System.out.println(
                "   Ratio to 1920x1080: "
                        + String.format("%.3f", (double) capture125.getWidth() / 1920));

        // Analyze the 1536x864 capture
        System.out.println("\n4. THE 1536x864 MYSTERY:");
        System.out.println("-".repeat(70));

        int actualWidth = 1536;
        int actualHeight = 864;

        System.out.println("   Observed capture: " + actualWidth + "x" + actualHeight);
        System.out.println("   This is exactly: 1920 × 0.8 = " + (1920 * 0.8));
        System.out.println("                    1080 × 0.8 = " + (1080 * 0.8));

        System.out.println("\n   POSSIBLE EXPLANATIONS:");
        System.out.println("   a) SikuliX detects 125% DPI and applies 1/1.25 = 0.8 scaling");
        System.out.println("   b) SikuliX has internal 0.8 default when AlwaysResize = 0");
        System.out.println("   c) There's a hidden display setting affecting capture");

        // Test pattern matching implications
        System.out.println("\n5. PATTERN MATCHING IMPLICATIONS:");
        System.out.println("-".repeat(70));

        testPatternMatching(screen);

        // Final recommendations
        System.out.println("\n6. RECOMMENDATIONS:");
        System.out.println("-".repeat(70));

        System.out.println("\n   GIVEN: SikuliX captures at 1536x864 (80% of screen)");
        System.out.println("\n   OPTION 1 - Use 80% pre-scaled patterns:");
        System.out.println("   - Scale all patterns to 80% size");
        System.out.println("   - Use Settings.AlwaysResize = 1.0");
        System.out.println("   - Example: 195x80 → 156x64");

        System.out.println("\n   OPTION 2 - Compensate with AlwaysResize:");
        System.out.println("   - Keep original patterns");
        System.out.println("   - Use Settings.AlwaysResize = 1.25");
        System.out.println("   - This scales patterns UP to match captures");

        System.out.println("\n   OPTION 3 - Force full resolution capture:");
        System.out.println("   - Try Settings.AlwaysResize = 1.25 for captures");
        System.out.println("   - May not work if SikuliX limits capture size");

        System.out.println("\n   RECOMMENDED: Option 1 (80% pre-scaled patterns)");
        System.out.println("   - Most reliable and predictable");
        System.out.println("   - Already working in your tests");
        System.out.println("   - Avoids runtime scaling issues");

        System.out.println("\n" + "=".repeat(80));
    }

    private static void testPatternMatching(Screen screen) {
        String basePath = "/home/jspinak/brobot_parent/claude-automator/images/prompt/";

        // Test patterns if they exist
        String[] patterns = {
            "claude-prompt-3.png", // 195x80 - original
            "claude-prompt-3-80.png", // 156x64 - 80% scaled
            "claude-prompt-win.png" // 103x60 - Windows capture
        };

        System.out.println("\n   Testing pattern dimensions vs capture scale:");

        for (String patternName : patterns) {
            File f = new File(basePath + patternName);
            if (!f.exists()) continue;

            try {
                BufferedImage img = ImageIO.read(f);
                int width = img.getWidth();
                int height = img.getHeight();

                System.out.println("\n   " + patternName + ": " + width + "x" + height);

                // Calculate what this would be at 80% scale
                int scaled80Width = (int) (width * 0.8);
                int scaled80Height = (int) (height * 0.8);

                // Calculate what this would be at 125% scale
                int scaled125Width = (int) (width * 1.25);
                int scaled125Height = (int) (height * 1.25);

                System.out.println("     At 80%: " + scaled80Width + "x" + scaled80Height);
                System.out.println("     At 125%: " + scaled125Width + "x" + scaled125Height);

                // Test actual matching
                System.out.println("     Testing match scores:");

                Settings.AlwaysResize = 1.0f;
                testMatch(screen, f.getAbsolutePath(), "1.0");

                Settings.AlwaysResize = 0.8f;
                testMatch(screen, f.getAbsolutePath(), "0.8");

                Settings.AlwaysResize = 1.25f;
                testMatch(screen, f.getAbsolutePath(), "1.25");

            } catch (Exception e) {
                System.err.println("   Error: " + e.getMessage());
            }
        }
    }

    private static void testMatch(Screen screen, String patternPath, String dpiLabel) {
        try {
            Pattern pattern = new Pattern(patternPath).similar(0.3);
            Match match = screen.exists(pattern, 0.1);

            if (match != null) {
                System.out.printf("       DPI %s: %.1f%%%n", dpiLabel, match.getScore() * 100);
            } else {
                System.out.printf("       DPI %s: No match%n", dpiLabel);
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
}
