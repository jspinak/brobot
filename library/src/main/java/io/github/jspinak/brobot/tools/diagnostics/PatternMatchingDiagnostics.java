package io.github.jspinak.brobot.tools.diagnostics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/**
 * Comprehensive diagnostics for pattern matching issues with DPI scaling. Provides detailed logging
 * and visual debugging for pattern matching problems.
 */
@Slf4j
@Component
public class PatternMatchingDiagnostics {

    private final Action action;

    private static final String DEBUG_DIR = "pattern-matching-debug";
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Autowired
    public PatternMatchingDiagnostics(Action action) {
        this.action = action;
        // Create debug directory if it doesn't exist
        new File(DEBUG_DIR).mkdirs();
    }

    /** Perform comprehensive diagnostics on a StateImage with all its patterns. */
    public void diagnoseStateImage(StateImage stateImage, String context) {
        log.info("╔════════════════════════════════════════════════════════════════════════════╗");
        log.info(
                "║           PATTERN MATCHING DIAGNOSTICS - {}                                ",
                context);
        log.info("╚════════════════════════════════════════════════════════════════════════════╝");

        // Log current DPI and scaling settings
        logDPISettings();

        // Log StateImage details
        logStateImageDetails(stateImage);

        // Test each pattern
        List<PatternTestResult> results = new ArrayList<>();
        for (int i = 0; i < stateImage.getPatterns().size(); i++) {
            Pattern pattern = stateImage.getPatterns().get(i);
            PatternTestResult result = testPattern(pattern, i, stateImage);
            results.add(result);
        }

        // Generate visual comparison
        generateVisualComparison(stateImage, results, context);

        // Log summary
        logSummary(results);

        // Provide recommendations
        provideRecommendations(results);

        log.info("════════════════════════════════════════════════════════════════════════════");
    }

    private void logDPISettings() {
        log.info("");
        log.info("=== DPI AND SCALING SETTINGS ===");
        log.info("Settings.AlwaysResize: {}", Settings.AlwaysResize);
        log.info("Settings.MinSimilarity: {}", Settings.MinSimilarity);
        log.info(
                "Settings.AlwaysResize effective: {} (1.0 = no resize, < 1.0 = downscale, > 1.0 ="
                        + " upscale)",
                Settings.AlwaysResize);

        // Get screen info
        Screen screen = new Screen();
        log.info("Screen dimensions: {}x{}", screen.w, screen.h);
        log.info(
                "Screen DPI: {} (may be virtual/scaled)",
                java.awt.Toolkit.getDefaultToolkit().getScreenResolution());

        // Check Windows DPI
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                // Try to get Windows scaling
                double scaleX =
                        java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                                .getDefaultScreenDevice()
                                .getDefaultConfiguration()
                                .getDefaultTransform()
                                .getScaleX();
                double scaleY =
                        java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                                .getDefaultScreenDevice()
                                .getDefaultConfiguration()
                                .getDefaultTransform()
                                .getScaleY();
                log.info(
                        "Windows display scaling detected: {}% x {}%",
                        (int) (scaleX * 100), (int) (scaleY * 100));
            } catch (Exception e) {
                log.warn("Could not detect Windows display scaling: {}", e.getMessage());
            }
        }
    }

    private void logStateImageDetails(StateImage stateImage) {
        log.info("");
        log.info("=== STATE IMAGE DETAILS ===");
        log.info("StateImage: {}", stateImage.getName());
        log.info("Number of patterns: {}", stateImage.getPatterns().size());
        log.info("Is fixed: {}", stateImage.getPatterns().stream().anyMatch(Pattern::isFixed));
        log.info("Search regions defined: {}", stateImage.isDefined());

        // Log each pattern's details
        for (int i = 0; i < stateImage.getPatterns().size(); i++) {
            Pattern pattern = stateImage.getPatterns().get(i);
            log.info("");
            log.info("Pattern[{}]: {}", i, pattern.getNameWithoutExtension());
            if (pattern.getImage() != null && pattern.getImage().getBufferedImage() != null) {
                BufferedImage img = pattern.getImage().getBufferedImage();
                log.info("  - Dimensions: {}x{} pixels", img.getWidth(), img.getHeight());
                log.info(
                        "  - Expected scaled size at {}: {}x{} pixels",
                        Settings.AlwaysResize,
                        (int) (img.getWidth() * Settings.AlwaysResize),
                        (int) (img.getHeight() * Settings.AlwaysResize));
            } else {
                log.warn("  - Pattern has no image loaded!");
            }
            log.info("  - Fixed: {}", pattern.isFixed());
            log.info("  - Search regions: {}", pattern.getSearchRegions().getRegions());
        }
    }

    private PatternTestResult testPattern(Pattern pattern, int index, StateImage stateImage) {
        log.info("");
        log.info("=== TESTING PATTERN[{}]: {} ===", index, pattern.getNameWithoutExtension());

        PatternTestResult result = new PatternTestResult();
        result.patternName = pattern.getNameWithoutExtension();
        result.patternIndex = index;

        if (pattern.getImage() == null || pattern.getImage().getBufferedImage() == null) {
            log.error("Pattern has no image - cannot test!");
            result.error = "No image loaded";
            return result;
        }

        BufferedImage patternImage = pattern.getImage().getBufferedImage();
        result.patternWidth = patternImage.getWidth();
        result.patternHeight = patternImage.getHeight();

        try {
            // Capture current screen
            Screen screen = new Screen();
            BufferedImage screenshot = screen.capture().getImage();
            result.screenWidth = screenshot.getWidth();
            result.screenHeight = screenshot.getHeight();

            // Save screenshot for debugging
            String timestamp = LocalDateTime.now().format(TIMESTAMP);
            File screenshotFile =
                    new File(DEBUG_DIR, String.format("screen_%s_%d.png", timestamp, index));
            ImageIO.write(screenshot, "png", screenshotFile);
            log.info("Screenshot saved: {}", screenshotFile.getAbsolutePath());

            // Save pattern for comparison
            File patternFile =
                    new File(DEBUG_DIR, String.format("pattern_%s_%d.png", timestamp, index));
            ImageIO.write(patternImage, "png", patternFile);
            log.info("Pattern saved: {}", patternFile.getAbsolutePath());

            // Test with different similarity thresholds
            testWithMultipleSimilarities(pattern, screenshot, result);

            // Test with Brobot Action
            testWithBrobotAction(stateImage, index, result);

            // Test direct SikuliX matching
            testDirectSikuliX(pattern, screenshot, result);

        } catch (Exception e) {
            log.error("Error during pattern testing: ", e);
            result.error = e.getMessage();
        }

        return result;
    }

    private void testWithMultipleSimilarities(
            Pattern pattern, BufferedImage screenshot, PatternTestResult result) {
        log.info("");
        log.info("Testing with multiple similarity thresholds:");

        double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.70, 0.60, 0.50};

        for (double threshold : thresholds) {
            try {
                Finder finder = new Finder(screenshot);
                org.sikuli.script.Pattern sikuliPattern =
                        new org.sikuli.script.Pattern(pattern.getImage().getBufferedImage());
                sikuliPattern.similar(threshold);

                finder.find(sikuliPattern);

                if (finder.hasNext()) {
                    Match match = finder.next();
                    double score = match.getScore();
                    result.similarityScores.put(threshold, score);
                    log.info(
                            "  Threshold {}: FOUND with score {} at [{}, {}]",
                            threshold,
                            score,
                            match.x,
                            match.y);

                    if (result.bestMatch == null || score > result.bestScore) {
                        result.bestMatch = match;
                        result.bestScore = score;
                        result.bestThreshold = threshold;
                    }
                } else {
                    log.info("  Threshold {}: NOT FOUND", threshold);
                }
            } catch (Exception e) {
                log.error("  Threshold {} failed: {}", threshold, e.getMessage());
            }
        }
    }

    private void testWithBrobotAction(
            StateImage stateImage, int patternIndex, PatternTestResult result) {
        log.info("");
        log.info("Testing with Brobot Action API:");

        try {
            // Create a temporary StateImage with just this pattern
            StateImage testImage = new StateImage();
            testImage.setName(stateImage.getName() + "_pattern_" + patternIndex);
            testImage.setPatterns(List.of(stateImage.getPatterns().get(patternIndex)));

            // Test with current settings
            PatternFindOptions options =
                    new PatternFindOptions.Builder().setSimilarity(Settings.MinSimilarity).build();

            ObjectCollection collection =
                    new ObjectCollection.Builder().withImages(testImage).build();

            ActionResult actionResult = action.perform(ActionType.FIND, collection, options);

            result.brobotFound = actionResult.isSuccess();
            result.brobotMatches = actionResult.getMatchList().size();

            if (actionResult.isSuccess()) {
                log.info("  Brobot FOUND {} match(es)", result.brobotMatches);
                actionResult
                        .getMatchList()
                        .forEach(
                                match -> {
                                    log.info(
                                            "    - Match at [{}, {}] with score {}",
                                            match.x(),
                                            match.y(),
                                            match.getScore());
                                });
            } else {
                log.info("  Brobot NOT FOUND");
            }

        } catch (Exception e) {
            log.error("  Brobot test failed: {}", e.getMessage());
            result.error = "Brobot test failed: " + e.getMessage();
        }
    }

    private void testDirectSikuliX(
            Pattern pattern, BufferedImage screenshot, PatternTestResult result) {
        log.info("");
        log.info("Testing direct SikuliX matching:");

        try {
            // Test without AlwaysResize
            float originalResize = Settings.AlwaysResize;

            Settings.AlwaysResize = 0; // No resize
            log.info("  Testing with AlwaysResize = 0 (no scaling):");
            testSikuliXWithCurrentSettings(pattern, screenshot, "No scaling");

            Settings.AlwaysResize = 0.8f; // 125% DPI compensation
            log.info("  Testing with AlwaysResize = 0.8 (125% DPI compensation):");
            testSikuliXWithCurrentSettings(pattern, screenshot, "0.8 scaling");

            Settings.AlwaysResize = 1.0f; // Original size
            log.info("  Testing with AlwaysResize = 1.0 (original size):");
            testSikuliXWithCurrentSettings(pattern, screenshot, "Original size");

            // Restore original
            Settings.AlwaysResize = originalResize;

        } catch (Exception e) {
            log.error("  Direct SikuliX test failed: {}", e.getMessage());
        }
    }

    private void testSikuliXWithCurrentSettings(
            Pattern pattern, BufferedImage screenshot, String description) {
        try {
            Finder finder = new Finder(screenshot);
            org.sikuli.script.Pattern sikuliPattern =
                    new org.sikuli.script.Pattern(pattern.getImage().getBufferedImage());
            finder.find(sikuliPattern);

            if (finder.hasNext()) {
                Match match = finder.next();
                log.info(
                        "    {} - FOUND at [{}, {}] with score {}",
                        description,
                        match.x,
                        match.y,
                        match.getScore());
            } else {
                log.info("    {} - NOT FOUND", description);
            }
        } catch (Exception e) {
            log.error("    {} - ERROR: {}", description, e.getMessage());
        }
    }

    private void generateVisualComparison(
            StateImage stateImage, List<PatternTestResult> results, String context) {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP);

            // Capture current screen
            Screen screen = new Screen();
            BufferedImage screenshot = screen.capture().getImage();

            // Create annotated image
            BufferedImage annotated =
                    new BufferedImage(
                            screenshot.getWidth(),
                            screenshot.getHeight(),
                            BufferedImage.TYPE_INT_RGB);

            Graphics2D g = annotated.createGraphics();
            g.drawImage(screenshot, 0, 0, null);

            // Draw matches
            g.setStroke(new BasicStroke(3));
            g.setFont(new Font("Arial", Font.BOLD, 14));

            for (PatternTestResult result : results) {
                if (result.bestMatch != null) {
                    // Draw rectangle around match
                    g.setColor(Color.GREEN);
                    g.drawRect(
                            result.bestMatch.x,
                            result.bestMatch.y,
                            result.bestMatch.w,
                            result.bestMatch.h);

                    // Draw label
                    g.setColor(Color.YELLOW);
                    g.fillRect(result.bestMatch.x, result.bestMatch.y - 20, 150, 20);
                    g.setColor(Color.BLACK);
                    g.drawString(
                            String.format("%s: %.2f", result.patternName, result.bestScore),
                            result.bestMatch.x + 5,
                            result.bestMatch.y - 5);
                }
            }

            // Add diagnostic info
            g.setColor(Color.YELLOW);
            g.fillRect(10, 10, 400, 100);
            g.setColor(Color.BLACK);
            g.drawString("PATTERN MATCHING DIAGNOSTIC", 15, 30);
            g.drawString(String.format("Context: %s", context), 15, 50);
            g.drawString(String.format("AlwaysResize: %.2f", Settings.AlwaysResize), 15, 70);
            g.drawString(String.format("MinSimilarity: %.2f", Settings.MinSimilarity), 15, 90);
            g.drawString(timestamp, 15, 105);

            g.dispose();

            // Save annotated image
            File outputFile = new File(DEBUG_DIR, String.format("diagnostic_%s.png", timestamp));
            ImageIO.write(annotated, "png", outputFile);
            log.info("");
            log.info("Visual diagnostic saved: {}", outputFile.getAbsolutePath());

        } catch (Exception e) {
            log.error("Failed to generate visual comparison: ", e);
        }
    }

    private void logSummary(List<PatternTestResult> results) {
        log.info("");
        log.info("=== SUMMARY ===");

        int found = 0;
        int notFound = 0;

        for (PatternTestResult result : results) {
            if (result.bestMatch != null) {
                found++;
                log.info(
                        "✓ Pattern[{}] '{}': FOUND with score {} at threshold {}",
                        result.patternIndex,
                        result.patternName,
                        result.bestScore,
                        result.bestThreshold);
            } else {
                notFound++;
                log.info("✗ Pattern[{}] '{}': NOT FOUND", result.patternIndex, result.patternName);
            }

            if (result.brobotFound) {
                log.info("  - Brobot: FOUND ({} matches)", result.brobotMatches);
            } else {
                log.info("  - Brobot: NOT FOUND");
            }
        }

        log.info("");
        log.info("Total: {} found, {} not found", found, notFound);
    }

    private void provideRecommendations(List<PatternTestResult> results) {
        log.info("");
        log.info("=== RECOMMENDATIONS ===");

        boolean allFailed = results.stream().allMatch(r -> r.bestMatch == null);
        boolean someFailed = results.stream().anyMatch(r -> r.bestMatch == null);
        boolean lowScores = results.stream().anyMatch(r -> r.bestScore > 0 && r.bestScore < 0.8);

        if (allFailed) {
            log.warn("⚠ All patterns failed to match!");
            log.info("1. Check if the target UI is visible on screen");
            log.info("2. Verify pattern images match current UI appearance");
            log.info("3. Try adjusting Settings.AlwaysResize:");
            log.info("   - Current value: {}", Settings.AlwaysResize);
            log.info("   - For 125% DPI: try 0.8");
            log.info("   - For 150% DPI: try 0.67");
            log.info("   - For 200% DPI: try 0.5");
            log.info("4. Lower similarity threshold (current: {})", Settings.MinSimilarity);
            log.info("5. Re-capture patterns at current display scaling");
        } else if (someFailed) {
            log.warn("⚠ Some patterns failed to match");
            log.info("1. Failed patterns may need recapturing");
            log.info("2. Check if UI elements have changed");
            log.info("3. Consider using multiple pattern variations");
        }

        if (lowScores) {
            log.warn("⚠ Low similarity scores detected");
            log.info("1. Patterns may be slightly different from current UI");
            log.info("2. Consider re-capturing with current theme/appearance");
            log.info("3. UI may have minor updates or anti-aliasing differences");
        }

        // Check for DPI mismatch indicators
        for (PatternTestResult result : results) {
            if (result.bestMatch != null && result.patternWidth > 0) {
                double actualRatio = (double) result.bestMatch.w / result.patternWidth;
                if (Math.abs(actualRatio - Settings.AlwaysResize) > 0.1) {
                    log.warn(
                            "⚠ Possible DPI scaling mismatch for pattern '{}'", result.patternName);
                    log.info(
                            "  Expected ratio: {}, Actual ratio: {}",
                            Settings.AlwaysResize,
                            actualRatio);
                    log.info("  Consider adjusting Settings.AlwaysResize to {}", actualRatio);
                }
            }
        }
    }

    // Helper class to store test results
    private static class PatternTestResult {
        String patternName;
        int patternIndex;
        int patternWidth;
        int patternHeight;
        int screenWidth;
        int screenHeight;
        Match bestMatch;
        double bestScore;
        double bestThreshold;
        java.util.Map<Double, Double> similarityScores = new java.util.HashMap<>();
        boolean brobotFound;
        int brobotMatches;
        String error;
    }
}
