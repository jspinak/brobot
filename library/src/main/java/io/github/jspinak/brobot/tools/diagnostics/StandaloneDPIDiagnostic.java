package io.github.jspinak.brobot.tools.diagnostics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;
import org.sikuli.script.Region;

import io.github.jspinak.brobot.dpi.DPIScalingStrategy;

/**
 * Standalone diagnostic to verify DPI scaling assumptions and test claude-automator patterns. This
 * diagnostic answers the critical question: Do screen captures and patterns use the same pixel
 * dimensions?
 */
public class StandaloneDPIDiagnostic {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("STANDALONE DPI DIAGNOSTIC FOR BROBOT");
        System.out.println("Time: " + dateFormat.format(new Date()));
        System.out.println("=".repeat(80));

        // Run all diagnostics
        verifyCriticalAssumption();
        System.out.println("\n" + "=".repeat(80));
        testPhysicalResolutionCaptures();
        System.out.println("\n" + "=".repeat(80));
        testClaudeAutomatorPatterns();
        System.out.println("\n" + "=".repeat(80));
        verifyAutoDetectionLogic();
        System.out.println("\n" + "=".repeat(80));
        provideFinalRecommendations();
    }

    private static void verifyCriticalAssumption() {
        System.out.println("\nCRITICAL TEST: Screen Capture vs Display Resolution");
        System.out.println("-".repeat(70));

        // Get display information
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();

        // Get DPI scaling
        double displayScale = DPIScalingStrategy.detectDisplayScaling();

        System.out.println("\n1. DISPLAY CONFIGURATION:");
        System.out.println(
                "   Monitor resolution (logical): " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println("   Windows DPI scaling: " + (int) (displayScale * 100) + "%");

        // Calculate expected physical dimensions
        int expectedPhysicalWidth = (int) (dm.getWidth() * displayScale);
        int expectedPhysicalHeight = (int) (dm.getHeight() * displayScale);
        System.out.println(
                "   Expected physical pixels: "
                        + expectedPhysicalWidth
                        + "x"
                        + expectedPhysicalHeight);

        // Capture screen using SikuliX
        System.out.println("\n2. SIKULIX SCREEN CAPTURE:");
        Screen screen = new Screen();
        ScreenImage screenImage = screen.capture();
        BufferedImage capture = screenImage.getImage();

        System.out.println(
                "   Actual capture dimensions: " + capture.getWidth() + "x" + capture.getHeight());
        System.out.println("   Image type: " + getImageTypeString(capture.getType()));

        // Determine what type of pixels SikuliX is capturing
        boolean capturesLogical = Math.abs(capture.getWidth() - dm.getWidth()) < 10;
        boolean capturesPhysical = Math.abs(capture.getWidth() - expectedPhysicalWidth) < 10;

        System.out.println("\n3. 🔍 CRITICAL FINDING:");
        if (capturesLogical) {
            System.out.println("   ⚠️ SikuliX captures in LOGICAL pixels!");
            System.out.println(
                    "   - Screen captures: "
                            + capture.getWidth()
                            + "x"
                            + capture.getHeight()
                            + " (logical)");
            System.out.println("   - This matches the display resolution reported by Windows");
            System.out.println(
                    "   - Patterns captured by SikuliX IDE will also be in logical pixels");
            System.out.println("   \n   IMPLICATION: DPI scaling may NOT be the issue!");
        } else if (capturesPhysical) {
            System.out.println("   ✓ SikuliX captures in PHYSICAL pixels");
            System.out.println(
                    "   - Screen captures: "
                            + capture.getWidth()
                            + "x"
                            + capture.getHeight()
                            + " (physical)");
            System.out.println(
                    "   - This is " + (int) (displayScale * 100) + "% of logical resolution");
            System.out.println("   - Patterns must be scaled to match");
            System.out.println("   \n   IMPLICATION: DPI scaling IS necessary!");
        } else {
            System.out.println("   ❓ UNEXPECTED RESULT!");
            System.out.println("   - Capture doesn't match logical OR physical dimensions");
            System.out.println("   - There may be additional scaling factors");

            double actualScale = (double) capture.getWidth() / dm.getWidth();
            System.out.println("   - Actual scaling factor: " + String.format("%.3f", actualScale));
        }

        // Save the capture for analysis
        saveCapture(capture, "screen_analysis");
    }

    private static void testPhysicalResolutionCaptures() {
        System.out.println("\nTEST: Physical Resolution Screenshot Matching");
        System.out.println("-".repeat(70));
        System.out.println(
                "\nThis test captures screen regions at physical resolution and tests if");
        System.out.println("they match better than logical resolution patterns.\n");

        Screen screen = new Screen();
        double displayScale = DPIScalingStrategy.detectDisplayScaling();

        // First, try to find a pattern on screen to capture
        String basePath = "/home/jspinak/brobot_parent/claude-automator/images/prompt/";
        String[] testPatterns = {
            "claude-prompt-3.png", // 195x80 physical
            "claude-prompt-win.png", // 103x60 logical
            "claude-prompt-3-80.png" // 156x64 scaled
        };

        for (String patternName : testPatterns) {
            String fullPath = basePath + patternName;
            File patternFile = new File(fullPath);

            if (!patternFile.exists()) continue;

            System.out.println("\n🔬 Testing with base pattern: " + patternName);

            try {
                // Load the pattern to get its dimensions
                BufferedImage originalPattern = ImageIO.read(patternFile);
                int origWidth = originalPattern.getWidth();
                int origHeight = originalPattern.getHeight();

                System.out.println("   Original dimensions: " + origWidth + "x" + origHeight);

                // Try to find this pattern on screen with various DPI settings
                Match bestMatch = null;
                float bestDPI = 1.0f;
                double bestScore = 0;

                float[] dpiSettings = {1.0f, 0.8f, 0.67f};

                for (float dpi : dpiSettings) {
                    Settings.AlwaysResize = dpi;
                    Pattern pattern = new Pattern(fullPath).similar(0.3);
                    Match match = screen.exists(pattern, 0.1);

                    if (match != null && match.getScore() > bestScore) {
                        bestMatch = match;
                        bestDPI = dpi;
                        bestScore = match.getScore();
                    }
                }

                if (bestMatch == null) {
                    System.out.println("   ❌ Pattern not found on screen, skipping");
                    continue;
                }

                System.out.println(
                        "   ✓ Found with DPI "
                                + bestDPI
                                + ", score: "
                                + String.format("%.1f%%", bestScore * 100));

                // Now capture the matched region
                Region matchRegion = new Region(bestMatch);
                BufferedImage capturedRegion = screen.capture(matchRegion).getImage();

                System.out.println("\n   📸 CAPTURED REGION ANALYSIS:");
                System.out.println(
                        "   Captured dimensions: "
                                + capturedRegion.getWidth()
                                + "x"
                                + capturedRegion.getHeight());

                // Calculate the actual resolution this represents
                double widthRatio = (double) capturedRegion.getWidth() / origWidth;
                double heightRatio = (double) capturedRegion.getHeight() / origHeight;

                System.out.println(
                        "   Size ratio to original: " + String.format("%.3f", widthRatio));

                // Determine if captured region is logical or physical resolution
                if (Math.abs(widthRatio - 1.0) < 0.05) {
                    System.out.println("   → Captured at SAME resolution as pattern");
                } else if (Math.abs(widthRatio - 0.8) < 0.05) {
                    System.out.println("   → Captured at 80% of pattern (logical resolution)");
                } else if (Math.abs(widthRatio - 1.25) < 0.05) {
                    System.out.println("   → Captured at 125% of pattern (physical resolution)");
                } else if (Math.abs(widthRatio - (1.0 / displayScale)) < 0.05) {
                    System.out.println(
                            "   → Captured at logical resolution (1/"
                                    + String.format("%.2f", displayScale)
                                    + ")");
                } else {
                    System.out.println(
                            "   → Unexpected ratio: " + String.format("%.3f", widthRatio));
                }

                // Save the captured region
                File captureFile =
                        saveCapture(capturedRegion, "captured_" + patternName.replace(".png", ""));

                // Now test if this CAPTURED image works better as a pattern
                System.out.println("\n   🔄 REVERSE MATCHING (using capture as pattern):");

                Settings.AlwaysResize = 1.0f; // Reset to no scaling
                Pattern capturedPattern = new Pattern(captureFile.getAbsolutePath()).similar(0.3);
                Match reverseMatch = screen.exists(capturedPattern, 0.5);

                if (reverseMatch != null) {
                    System.out.println(
                            "   ✓ Captured pattern matches at: "
                                    + String.format("%.1f%%", reverseMatch.getScore() * 100));

                    if (reverseMatch.getScore() > 0.95) {
                        System.out.println(
                                "   🎯 PERFECT match! Screen capture = pattern capture method");
                    } else if (reverseMatch.getScore() > 0.90) {
                        System.out.println("   ✅ Excellent match - methods are compatible");
                    } else if (reverseMatch.getScore() > 0.80) {
                        System.out.println("   ✓ Good match - minor differences");
                    } else {
                        System.out.println("   ⚠ Moderate match - possible scaling issues");
                    }
                } else {
                    System.out.println("   ❌ Captured pattern NOT found!");
                    System.out.println("   This indicates capture/pattern incompatibility");
                }

                // Test if resizing the captured image helps
                System.out.println("\n   🔧 TESTING RESIZED CAPTURES:");
                testResizedCapture(captureFile, capturedRegion, displayScale, screen);

            } catch (Exception e) {
                System.err.println("   Error: " + e.getMessage());
            }
        }

        System.out.println("\n📊 CONCLUSION:");
        System.out.println("If captured regions match at >95% when used as patterns, then:");
        System.out.println("  - Screen capture and pattern search use the SAME resolution");
        System.out.println("  - DPI scaling may not be the root cause of low similarity");
        System.out.println("If captured regions DON'T match well, then:");
        System.out.println("  - There's a resolution mismatch between capture and search");
        System.out.println("  - DPI scaling configuration is critical");
    }

    private static void testResizedCapture(
            File captureFile, BufferedImage captured, double displayScale, Screen screen) {
        try {
            // Test 1: Resize captured to physical resolution (if not already)
            int physicalWidth = (int) (captured.getWidth() * displayScale);
            int physicalHeight = (int) (captured.getHeight() * displayScale);

            if (Math.abs(physicalWidth - captured.getWidth()) > 5) {
                System.out.println(
                        "   Testing capture resized to physical ("
                                + physicalWidth
                                + "x"
                                + physicalHeight
                                + "):");

                BufferedImage resizedPhysical =
                        resizeImage(captured, physicalWidth, physicalHeight);
                File physicalFile = saveCapture(resizedPhysical, "physical_resize");

                Pattern physicalPattern = new Pattern(physicalFile.getAbsolutePath()).similar(0.3);
                Match physicalMatch = screen.exists(physicalPattern, 0.1);

                if (physicalMatch != null) {
                    System.out.println(
                            "     Physical size: "
                                    + String.format("%.1f%%", physicalMatch.getScore() * 100));
                } else {
                    System.out.println("     Physical size: No match");
                }
            }

            // Test 2: Resize captured to logical resolution
            int logicalWidth = (int) (captured.getWidth() / displayScale);
            int logicalHeight = (int) (captured.getHeight() / displayScale);

            if (Math.abs(logicalWidth - captured.getWidth()) > 5) {
                System.out.println(
                        "   Testing capture resized to logical ("
                                + logicalWidth
                                + "x"
                                + logicalHeight
                                + "):");

                BufferedImage resizedLogical = resizeImage(captured, logicalWidth, logicalHeight);
                File logicalFile = saveCapture(resizedLogical, "logical_resize");

                Pattern logicalPattern = new Pattern(logicalFile.getAbsolutePath()).similar(0.3);
                Match logicalMatch = screen.exists(logicalPattern, 0.1);

                if (logicalMatch != null) {
                    System.out.println(
                            "     Logical size: "
                                    + String.format("%.1f%%", logicalMatch.getScore() * 100));
                } else {
                    System.out.println("     Logical size: No match");
                }
            }

            // Test 3: 80% scaled version
            int scaled80Width = (int) (captured.getWidth() * 0.8);
            int scaled80Height = (int) (captured.getHeight() * 0.8);

            System.out.println(
                    "   Testing capture at 80% size ("
                            + scaled80Width
                            + "x"
                            + scaled80Height
                            + "):");

            BufferedImage resized80 = resizeImage(captured, scaled80Width, scaled80Height);
            File scaled80File = saveCapture(resized80, "scaled_80");

            Pattern scaled80Pattern = new Pattern(scaled80File.getAbsolutePath()).similar(0.3);
            Match scaled80Match = screen.exists(scaled80Pattern, 0.1);

            if (scaled80Match != null) {
                System.out.println(
                        "     80% size: "
                                + String.format("%.1f%%", scaled80Match.getScore() * 100));
            } else {
                System.out.println("     80% size: No match");
            }

        } catch (Exception e) {
            System.err.println("   Error testing resized captures: " + e.getMessage());
        }
    }

    private static BufferedImage resizeImage(
            BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage =
                new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }

    private static void testClaudeAutomatorPatterns() {
        System.out.println("\nTEST: Claude Automator Pattern Analysis");
        System.out.println("-".repeat(70));

        String basePath = "/home/jspinak/brobot_parent/claude-automator/images/prompt/";
        Screen screen = new Screen();

        // Test patterns
        String[] patterns = {
            "claude-prompt-3.png", // Original SikuliX capture (195x80)
            "claude-prompt-3-80.png", // 80% scaled version (156x64)
            "claude-prompt-win.png" // Windows tool capture (103x60)
        };

        for (String patternName : patterns) {
            String fullPath = basePath + patternName;
            File patternFile = new File(fullPath);

            if (!patternFile.exists()) {
                System.out.println("\n❌ Pattern not found: " + patternName);
                continue;
            }

            System.out.println("\n📁 Testing: " + patternName);

            try {
                BufferedImage patternImage = ImageIO.read(patternFile);
                System.out.println(
                        "   Dimensions: "
                                + patternImage.getWidth()
                                + "x"
                                + patternImage.getHeight());
                System.out.println("   Type: " + getImageTypeString(patternImage.getType()));

                // Analyze pattern source
                DPIScalingStrategy.PatternSource source =
                        analyzePatternSource(patternImage.getWidth(), patternImage.getHeight());
                System.out.println("   Likely source: " + source);

                // Test with different DPI settings
                System.out.println("\n   Testing with different DPI settings:");

                float[] dpiSettings = {1.0f, 0.9f, 0.8f, 0.75f, 0.67f, 0.6f, 0.5f};
                double bestScore = 0;
                float bestDPI = 1.0f;

                for (float dpi : dpiSettings) {
                    Settings.AlwaysResize = dpi;

                    try {
                        Pattern pattern = new Pattern(fullPath).similar(0.3);
                        Match match = screen.exists(pattern, 0.1);

                        if (match != null) {
                            double score = match.getScore();
                            String marker = "";

                            if (score > bestScore) {
                                bestScore = score;
                                bestDPI = dpi;
                                marker = " ← BEST";
                            }

                            if (score > 0.90) {
                                marker += " ✅";
                            } else if (score > 0.80) {
                                marker += " ✓";
                            } else if (score > 0.70) {
                                marker += " ⚠";
                            }

                            System.out.printf(
                                    "     DPI %.2f: %.1f%%%s%n", dpi, score * 100, marker);
                        } else {
                            System.out.printf("     DPI %.2f: No match%n", dpi);
                        }
                    } catch (Exception e) {
                        System.out.printf("     DPI %.2f: Error%n", dpi);
                    }
                }

                System.out.println(
                        "\n   Best result: DPI "
                                + bestDPI
                                + " = "
                                + String.format("%.1f%%", bestScore * 100));

            } catch (IOException e) {
                System.err.println("   Error reading pattern: " + e.getMessage());
            }
        }
    }

    private static void verifyAutoDetectionLogic() {
        System.out.println("\nTEST: Auto-Detection Logic");
        System.out.println("-".repeat(70));

        double displayScale = DPIScalingStrategy.detectDisplayScaling();
        float calculatedFactor = DPIScalingStrategy.calculatePatternScaleFactor(displayScale);

        System.out.println("\n1. DPI Detection Results:");
        System.out.println("   Display scaling: " + (int) (displayScale * 100) + "%");
        System.out.println("   Calculated pattern factor: " + calculatedFactor);
        System.out.println("   Formula: 1.0 / " + displayScale + " = " + calculatedFactor);

        System.out.println("\n2. Current SikuliX Settings:");
        System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
        System.out.println("   Settings.MinSimilarity: " + Settings.MinSimilarity);

        System.out.println("\n3. Pattern Source Recommendations:");
        for (DPIScalingStrategy.PatternSource source : DPIScalingStrategy.PatternSource.values()) {
            float factor = DPIScalingStrategy.getOptimalResizeFactor(source);
            System.out.println("   " + source + ": Settings.AlwaysResize = " + factor);
        }
    }

    private static void provideFinalRecommendations() {
        System.out.println("\nFINAL RECOMMENDATIONS");
        System.out.println("-".repeat(70));

        double displayScale = DPIScalingStrategy.detectDisplayScaling();

        System.out.println("\n📋 Based on the diagnostic results:");

        if (Math.abs(displayScale - 1.0) < 0.01) {
            System.out.println("\n✓ No DPI scaling detected (100%)");
            System.out.println("  - Use Settings.AlwaysResize = 1.0");
            System.out.println("  - Patterns should match without scaling");
        } else {
            System.out.println("\n⚠ DPI scaling detected: " + (int) (displayScale * 100) + "%");
            System.out.println("\nRecommended configurations:");
            System.out.println("\n1. For SikuliX IDE patterns (physical pixels):");
            System.out.println("   brobot.dpi.resize.factor=" + (float) (1.0 / displayScale));
            System.out.println("   brobot.dpi.pattern.source=SIKULI_IDE");

            System.out.println("\n2. For Windows tool patterns (logical pixels):");
            System.out.println("   brobot.dpi.resize.factor=" + (float) (1.0 / displayScale));
            System.out.println("   brobot.dpi.pattern.source=WINDOWS_TOOL");

            System.out.println("\n3. For pre-scaled patterns (80% size):");
            System.out.println("   brobot.dpi.resize.factor=1.0");
            System.out.println("   (Patterns are already scaled)");
        }

        System.out.println("\n🔧 To improve pattern matching:");
        System.out.println("  1. Ensure all patterns use the same capture method");
        System.out.println("  2. Verify browser/application zoom is 100%");
        System.out.println("  3. Consider using pre-scaled pattern sets");
        System.out.println("  4. Lower similarity threshold if needed (0.65-0.70)");

        System.out.println("\n💡 Key insight:");
        System.out.println("  The 30% similarity loss (99% → 69%) may be due to:");
        System.out.println("  - Image type differences (ARGB vs RGB)");
        System.out.println("  - Anti-aliasing or rendering differences");
        System.out.println("  - Compound scaling (Windows + application)");
        System.out.println("  - Sub-pixel rendering variations");
    }

    private static DPIScalingStrategy.PatternSource analyzePatternSource(int width, int height) {
        // Known dimensions from claude-automator
        if (width == 195 && height == 80) {
            return DPIScalingStrategy.PatternSource.SIKULI_IDE;
        } else if (width == 103 && height == 60) {
            return DPIScalingStrategy.PatternSource.WINDOWS_TOOL;
        } else if (width == 156 && height == 64) {
            // 80% scaled version
            return DPIScalingStrategy.PatternSource.SIKULI_IDE;
        }
        return DPIScalingStrategy.PatternSource.UNKNOWN;
    }

    private static String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB (no alpha)";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB (with alpha)";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "ABGR (with alpha)";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "GRAYSCALE";
            default:
                return "Type " + type;
        }
    }

    private static File saveCapture(BufferedImage image, String prefix) {
        try {
            File debugDir = new File("debug_captures");
            if (!debugDir.exists()) debugDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File(debugDir, prefix + "_" + timestamp + ".png");
            ImageIO.write(image, "png", file);
            System.out.println("   💾 Saved to: " + file.getAbsolutePath());
            return file;
        } catch (IOException e) {
            System.err.println("   ❌ Failed to save: " + e.getMessage());
            return null;
        }
    }
}
