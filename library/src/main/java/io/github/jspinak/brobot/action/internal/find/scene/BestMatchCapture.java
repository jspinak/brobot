package io.github.jspinak.brobot.action.internal.find.scene;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;

import org.sikuli.script.Finder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
// Removed old logging import: 
import lombok.extern.slf4j.Slf4j;

/**
 * Captures and saves the best matching region when pattern searches fail or find low-similarity
 * matches. This is useful for debugging why patterns aren't matching as expected.
 *
 * <p>When enabled, this component will:
 *
 * <ul>
 *   <li>Find the best match regardless of similarity threshold
 *   <li>Capture the matching region from the scene
 *   <li>Save it with descriptive filename including similarity score
 *   <li>Optionally save the pattern image for comparison
 * </ul>
 *
 * @since 1.0
 */
@Slf4j
@Component
public class BestMatchCapture {

    @Value("${brobot.debug.capture-best-match:false}")
    private boolean captureEnabled;

    @Value("${brobot.debug.capture-threshold:0.95}")
    private double captureThreshold;

    @Value("${brobot.debug.capture-directory:history/best-matches}")
    private String captureDirectory;

    @Value("${brobot.debug.save-pattern-image:true}")
    private boolean savePatternImage;

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /**
     * Captures the best matching region for a pattern, regardless of similarity threshold.
     *
     * @param pattern The pattern being searched for
     * @param scene The scene being searched
     * @param actualMatches The matches found at the configured threshold (may be empty)
     */
    public void captureBestMatch(Pattern pattern, Scene scene, List<Match> actualMatches) {
        if (!captureEnabled) {
            return;
        }

        // Skip if we already have good matches
        if (!actualMatches.isEmpty() && actualMatches.get(0).getScore() >= captureThreshold) {
            return;
        }

        try {
            // Find best match at very low threshold
            BestMatchResult bestMatch = findBestMatchAtAnyThreshold(pattern, scene);

            if (bestMatch != null) {
                saveBestMatch(pattern, scene, bestMatch);
            }
        } catch (Exception e) {
            log.error(
                    "Error capturing best match for pattern '{}': {}",
                    pattern.getName(),
                    e.getMessage(),
                    e);
        }
    }

    /** Finds the best match for a pattern at any similarity level. */
    private BestMatchResult findBestMatchAtAnyThreshold(Pattern pattern, Scene scene) {
        // Check size constraints
        if (pattern.w() > scene.getPattern().w() || pattern.h() > scene.getPattern().h()) {
            return null;
        }

        // Save original threshold
        double originalSimilarity = org.sikuli.basics.Settings.MinSimilarity;
        log.debug("BestMatchCapture: Saving original MinSimilarity: {}", originalSimilarity);

        try {
            // Search at very low threshold to find any match
            org.sikuli.basics.Settings.MinSimilarity = 0.1;
            log.debug(
                    "BestMatchCapture: Temporarily setting MinSimilarity to 0.1 for best match"
                            + " search");

            Finder finder = new Finder(scene.getPattern().getBImage());
            org.sikuli.script.Pattern sikuliPattern = pattern.sikuli().similar(0.1);
            finder.findAll(sikuliPattern);

            org.sikuli.script.Match bestMatch = null;
            double bestScore = 0;

            while (finder.hasNext()) {
                org.sikuli.script.Match match = finder.next();
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                    bestMatch = match;
                }
            }

            finder.destroy();

            if (bestMatch != null) {
                return new BestMatchResult(bestMatch, bestScore);
            }

        } finally {
            // Restore original threshold
            org.sikuli.basics.Settings.MinSimilarity = originalSimilarity;
            log.debug("BestMatchCapture: Restored MinSimilarity to: {}", originalSimilarity);
        }

        return null;
    }

    /** Saves the best match image and optional pattern image. */
    private void saveBestMatch(Pattern pattern, Scene scene, BestMatchResult bestMatch) {
        try {
            // Create directory if it doesn't exist
            Path captureDir = Paths.get(captureDirectory);
            Files.createDirectories(captureDir);

            // Generate timestamp
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);

            // Create filename with pattern name, similarity, and timestamp
            String safePatternName =
                    pattern.getName().replaceAll("[^a-zA-Z0-9.-]", "_").replaceAll("_{2,}", "_");

            String similarityStr = String.format("%.3f", bestMatch.score).replace(".", "");

            // Extract the matching region from the scene
            BufferedImage sceneImage = scene.getPattern().getBImage();
            org.sikuli.script.Match match = bestMatch.match;

            // Ensure we don't go out of bounds
            int x = Math.max(0, match.x);
            int y = Math.max(0, match.y);
            int w = Math.min(match.w, sceneImage.getWidth() - x);
            int h = Math.min(match.h, sceneImage.getHeight() - y);

            BufferedImage matchRegion = sceneImage.getSubimage(x, y, w, h);

            // Save the match region
            String matchFilename =
                    String.format(
                            "%s_%s_sim%s_match.png", timestamp, safePatternName, similarityStr);
            File matchFile = captureDir.resolve(matchFilename).toFile();
            ImageIO.write(matchRegion, "png", matchFile);
            // Optionally save the pattern image for comparison
            if (savePatternImage && pattern.getBImage() != null) {
                String patternFilename =
                        String.format(
                                "%s_%s_sim%s_pattern.png",
                                timestamp, safePatternName, similarityStr);
                File patternFile = captureDir.resolve(patternFilename).toFile();
                ImageIO.write(pattern.getBImage(), "png", patternFile);
            }

            // Log additional debug information
            log.debug(
                    "Best match captured - Pattern: {}, Location: ({}, {}), Size: {}x{},"
                            + " Similarity: {}",
                    pattern.getName(),
                    x,
                    y,
                    w,
                    h,
                    bestMatch.score);

        } catch (IOException e) {
            log.error(
                    "Failed to save best match image for pattern '{}': {}",
                    pattern.getName(),
                    e.getMessage(),
                    e);
        }
    }

    /** Holder class for best match results. */
    private static class BestMatchResult {
        final org.sikuli.script.Match match;
        final double score;

        BestMatchResult(org.sikuli.script.Match match, double score) {
            this.match = match;
            this.score = score;
        }
    }

    /** Checks if capture is enabled. */
    public boolean isCaptureEnabled() {
        return captureEnabled;
    }

    /** Sets whether capture is enabled (useful for testing). */
    public void setCaptureEnabled(boolean enabled) {
        this.captureEnabled = enabled;
    }
}
