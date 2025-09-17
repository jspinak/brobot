package io.github.jspinak.brobot.dpi;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;
import io.github.jspinak.brobot.tools.diagnostics.DPIScalingDiagnostic;

/**
 * Tests DPI auto-detection and verifies that pattern matching works correctly with different
 * pattern sources and DPI configurations.
 *
 * <p>This test uses actual claude-automator images to verify the theory that screen capture and
 * pattern capture must use compatible methods.
 */
@SpringBootTest
@TestPropertySource(
        properties = {
            "brobot.dpi.resize.factor=auto",
            "brobot.dpi.debug=true",
            "brobot.action.similarity=0.65"
        })
@DisabledInCI
public class DPIAutoDetectionTest extends BrobotTestBase {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final String CLAUDE_AUTOMATOR_PATH =
            "/home/jspinak/brobot_parent/claude-automator";

    @Autowired(required = false)
    private Action action;

    private Screen screen;
    private double displayScale;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        screen = new Screen();
        displayScale = DPIScalingStrategy.detectDisplayScaling();

        System.out.println("\n=== DPI AUTO-DETECTION TEST ===");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("Display scaling detected: " + (int) (displayScale * 100) + "%");
        System.out.println("Current Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("================================\n");
    }

    @Test
    public void testScreenCaptureVsPatternCaptureDimensions() {
        System.out.println("TEST 1: Comparing Screen Capture vs Pattern Dimensions");
        System.out.println("-".repeat(60));

        // Capture the screen using SikuliX
        ScreenImage screenImage = screen.capture();
        BufferedImage screenCapture = screenImage.getImage();

        System.out.println("Screen Capture Analysis:");
        System.out.println(
                "  Capture dimensions: "
                        + screenCapture.getWidth()
                        + "x"
                        + screenCapture.getHeight());
        System.out.println("  Image type: " + getImageTypeString(screenCapture.getType()));

        // Check screen resolution from GraphicsEnvironment
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();

        System.out.println("\nDisplay Configuration:");
        System.out.println("  Display mode: " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println("  Display scaling: " + (int) (displayScale * 100) + "%");

        // Calculate expected physical dimensions
        int expectedPhysicalWidth = (int) (dm.getWidth() * displayScale);
        int expectedPhysicalHeight = (int) (dm.getHeight() * displayScale);

        System.out.println("\nDimension Analysis:");
        System.out.println("  Logical resolution: " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println(
                "  Expected physical: " + expectedPhysicalWidth + "x" + expectedPhysicalHeight);
        System.out.println(
                "  Actual capture: " + screenCapture.getWidth() + "x" + screenCapture.getHeight());

        // Determine if capture is in logical or physical pixels
        boolean captureIsLogical = Math.abs(screenCapture.getWidth() - dm.getWidth()) < 10;
        boolean captureIsPhysical = Math.abs(screenCapture.getWidth() - expectedPhysicalWidth) < 10;

        System.out.println("\nCapture Type:");
        if (captureIsLogical) {
            System.out.println("  ⚠ Screen capture is in LOGICAL pixels!");
            System.out.println("  This means patterns in physical pixels won't match correctly.");
        } else if (captureIsPhysical) {
            System.out.println("  ✓ Screen capture is in PHYSICAL pixels");
            System.out.println("  This is expected for SikuliX.");
        } else {
            System.out.println(
                    "  ❓ Screen capture dimensions don't match expected logical or physical!");
            System.out.println("  There may be additional scaling factors.");
        }

        // Save the capture for analysis
        try {
            File debugDir = new File("debug_captures");
            if (!debugDir.exists()) debugDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File captureFile = new File(debugDir, "screen_capture_" + timestamp + ".png");
            ImageIO.write(screenCapture, "png", captureFile);
            System.out.println("\nScreen capture saved to: " + captureFile.getName());
        } catch (IOException e) {
            System.err.println("Failed to save capture: " + e.getMessage());
        }
    }

    @Test
    public void testClaudeAutomatorPatterns() {
        System.out.println("\nTEST 2: Testing Claude Automator Patterns");
        System.out.println("-".repeat(60));

        // Test patterns from claude-automator
        String[] testPatterns = {
            CLAUDE_AUTOMATOR_PATH + "/images/prompt/claude-prompt-3.png", // SikuliX IDE capture
            CLAUDE_AUTOMATOR_PATH + "/images/prompt/claude-prompt-3-80.png", // 80% scaled
            CLAUDE_AUTOMATOR_PATH + "/images/prompt/claude-prompt-win.png" // Windows capture
        };

        for (String patternPath : testPatterns) {
            File patternFile = new File(patternPath);
            if (!patternFile.exists()) {
                System.out.println("\nPattern not found: " + patternPath);
                continue;
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("Testing: " + patternFile.getName());

            try {
                BufferedImage patternImage = ImageIO.read(patternFile);
                System.out.println(
                        "  Dimensions: "
                                + patternImage.getWidth()
                                + "x"
                                + patternImage.getHeight());

                // Analyze pattern source
                DPIScalingStrategy.PatternSource source = analyzePatternSource(patternImage);
                System.out.println("  Likely source: " + source);

                // Test with different DPI settings
                testPatternWithVariousDPI(patternPath);

            } catch (IOException e) {
                System.err.println("  Error reading pattern: " + e.getMessage());
            }
        }
    }

    @Test
    public void testAutoDetectionEffectiveness() {
        System.out.println("\nTEST 3: Auto-Detection Effectiveness");
        System.out.println("-".repeat(60));

        // Get current auto-detected settings
        float autoDetectedResize = Settings.AlwaysResize;
        System.out.println("Auto-detected Settings.AlwaysResize: " + autoDetectedResize);

        // Test with auto-detected value
        String testPattern = CLAUDE_AUTOMATOR_PATH + "/images/prompt/claude-prompt-3.png";
        File patternFile = new File(testPattern);

        if (!patternFile.exists()) {
            System.out.println("Test pattern not found: " + testPattern);
            return;
        }

        System.out.println("\nTesting with auto-detected DPI: " + autoDetectedResize);
        double autoScore = testPatternSimilarity(testPattern, autoDetectedResize);

        // Test with other common values
        float[] testValues = {1.0f, 0.8f, 0.67f, 0.5f};
        double bestScore = autoScore;
        float bestValue = autoDetectedResize;

        System.out.println("\nComparing with other DPI values:");
        for (float testValue : testValues) {
            if (Math.abs(testValue - autoDetectedResize) < 0.01) continue;

            double score = testPatternSimilarity(testPattern, testValue);
            System.out.printf("  DPI %.2f: %.1f%%\n", testValue, score * 100);

            if (score > bestScore) {
                bestScore = score;
                bestValue = testValue;
            }
        }

        System.out.println("\nResults:");
        System.out.println(
                "  Auto-detected DPI "
                        + autoDetectedResize
                        + ": "
                        + String.format("%.1f%%", autoScore * 100));
        System.out.println(
                "  Best DPI " + bestValue + ": " + String.format("%.1f%%", bestScore * 100));

        if (Math.abs(bestValue - autoDetectedResize) < 0.01) {
            System.out.println("  ✅ Auto-detection is OPTIMAL!");
        } else {
            System.out.println("  ⚠ Auto-detection may need adjustment");
            System.out.println("  Consider setting: brobot.dpi.resize.factor=" + bestValue);
        }
    }

    @Test
    public void testCaptureAndPatternCompatibility() {
        System.out.println("\nTEST 4: Capture and Pattern Compatibility");
        System.out.println("-".repeat(60));

        // This test captures a region and then tries to find it again
        // to verify that capture and search use compatible methods

        try {
            // First, find a pattern on screen
            String testPattern = CLAUDE_AUTOMATOR_PATH + "/images/prompt/claude-prompt-3.png";
            File patternFile = new File(testPattern);

            if (!patternFile.exists()) {
                System.out.println("Test pattern not found, skipping test");
                return;
            }

            Pattern pattern = new Pattern(testPattern).similar(0.3);
            Match match = screen.exists(pattern, 0.5);

            if (match == null) {
                System.out.println("Pattern not found on screen, skipping test");
                return;
            }

            System.out.println("Found pattern at: " + match.getTarget());
            System.out.println("Similarity: " + String.format("%.1f%%", match.getScore() * 100));

            // Capture the matched region
            Region matchRegion = new Region(match);
            BufferedImage capturedRegion = screen.capture(matchRegion).getImage();

            // Save the captured region
            File debugDir = new File("debug_captures");
            if (!debugDir.exists()) debugDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File captureFile = new File(debugDir, "compatibility_test_" + timestamp + ".png");
            ImageIO.write(capturedRegion, "png", captureFile);

            System.out.println("\nCaptured region analysis:");
            System.out.println(
                    "  Captured size: "
                            + capturedRegion.getWidth()
                            + "x"
                            + capturedRegion.getHeight());

            BufferedImage originalPattern = ImageIO.read(patternFile);
            System.out.println(
                    "  Pattern size: "
                            + originalPattern.getWidth()
                            + "x"
                            + originalPattern.getHeight());

            // Calculate size ratio
            double widthRatio = (double) capturedRegion.getWidth() / originalPattern.getWidth();
            double heightRatio = (double) capturedRegion.getHeight() / originalPattern.getHeight();

            System.out.println("  Width ratio: " + String.format("%.2f", widthRatio));
            System.out.println("  Height ratio: " + String.format("%.2f", heightRatio));

            // Now try to find the captured region on screen
            System.out.println("\nReverse matching test:");
            Pattern capturedPattern = new Pattern(captureFile.getAbsolutePath()).similar(0.3);
            Match reverseMatch = screen.exists(capturedPattern, 0.5);

            if (reverseMatch != null) {
                System.out.println("  ✅ Captured region found on screen!");
                System.out.println(
                        "  Similarity: " + String.format("%.1f%%", reverseMatch.getScore() * 100));

                if (reverseMatch.getScore() > 0.95) {
                    System.out.println("  Capture and search methods are COMPATIBLE");
                } else if (reverseMatch.getScore() > 0.80) {
                    System.out.println("  Capture and search methods are mostly compatible");
                } else {
                    System.out.println(
                            "  ⚠ Capture and search methods may have compatibility issues");
                }
            } else {
                System.out.println("  ❌ Captured region NOT found on screen!");
                System.out.println("  This indicates capture/search incompatibility");
            }

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private DPIScalingStrategy.PatternSource analyzePatternSource(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Known dimensions from claude-automator
        if (width == 195 && height == 80) {
            return DPIScalingStrategy.PatternSource.SIKULI_IDE;
        } else if (width == 103 && height == 60) {
            return DPIScalingStrategy.PatternSource.WINDOWS_TOOL;
        } else if (width == 156 && height == 64) {
            // This is the 80% scaled version
            return DPIScalingStrategy.PatternSource.SIKULI_IDE;
        }

        // Use ratio analysis
        double ratio = (double) width / height;
        if (Math.abs(ratio - 2.4375) < 0.01) { // 195/80 ratio
            return DPIScalingStrategy.PatternSource.SIKULI_IDE;
        } else if (Math.abs(ratio - 1.7167) < 0.01) { // 103/60 ratio
            return DPIScalingStrategy.PatternSource.WINDOWS_TOOL;
        }

        return DPIScalingStrategy.PatternSource.UNKNOWN;
    }

    private void testPatternWithVariousDPI(String patternPath) {
        float[] testValues = {1.0f, 0.9f, 0.8f, 0.75f, 0.67f, 0.6f, 0.5f};

        System.out.println("\n  Testing with different DPI settings:");
        double bestScore = 0;
        float bestDPI = 1.0f;

        for (float dpi : testValues) {
            Settings.AlwaysResize = dpi;
            try {
                Pattern pattern = new Pattern(patternPath).similar(0.3);
                Match match = screen.exists(pattern, 0.1);

                if (match != null) {
                    double score = match.getScore();
                    System.out.printf("    DPI %.2f: %.1f%%", dpi, score * 100);

                    if (score > bestScore) {
                        bestScore = score;
                        bestDPI = dpi;
                        System.out.print(" ← BEST");
                    }
                    System.out.println();
                } else {
                    System.out.printf("    DPI %.2f: No match\n", dpi);
                }
            } catch (Exception e) {
                System.out.printf("    DPI %.2f: Error - %s\n", dpi, e.getMessage());
            }
        }

        System.out.println(
                "  Best DPI: " + bestDPI + " (" + String.format("%.1f%%", bestScore * 100) + ")");
    }

    private double testPatternSimilarity(String patternPath, float dpiSetting) {
        Settings.AlwaysResize = dpiSetting;
        try {
            Pattern pattern = new Pattern(patternPath).similar(0.3);
            Match match = screen.exists(pattern, 0.5);
            return match != null ? match.getScore() : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB (no alpha)";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB (with alpha)";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "ABGR (with alpha)";
            default:
                return "Type " + type;
        }
    }

    @Test
    public void testDPIScalingDiagnostic() {
        System.out.println("\nTEST 5: Running DPIScalingDiagnostic");
        System.out.println("-".repeat(60));

        List<String> patterns =
                Arrays.asList(
                        CLAUDE_AUTOMATOR_PATH + "/images/prompt/claude-prompt-3.png",
                        CLAUDE_AUTOMATOR_PATH + "/images/prompt/claude-prompt-win.png");

        for (String pattern : patterns) {
            File f = new File(pattern);
            if (f.exists()) {
                DPIScalingDiagnostic.DiagnosticResult result =
                        DPIScalingDiagnostic.analyzePattern(pattern);

                if (result != null) {
                    System.out.println("\nDiagnostic Summary for " + f.getName() + ":");
                    System.out.println("  Best DPI: " + result.bestDPISetting);
                    System.out.println(
                            "  Best similarity: "
                                    + String.format("%.1f%%", result.bestSimilarity * 100));
                    System.out.println("  Recommendation: " + result.recommendation);
                }
            }
        }
    }
}
