package io.github.jspinak.brobot.action.internal.find.scene;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.sikuli.script.Finder;
import org.sikuli.script.OCR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.logging.ConciseFindLogger;
import io.github.jspinak.brobot.logging.DiagnosticLogger;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Performs image pattern matching and OCR text detection within captured scenes.
 *
 * <p>This wrapper class provides low-level search functionality for finding visual patterns and
 * text within {@link Scene} objects. It leverages Sikuli's {@link Finder} for pattern matching and
 * {@link OCR} for text recognition. Scene objects represent screenshots or loaded images that serve
 * as the search space.
 *
 * <p>This class is designed to be used by higher-level action classes that handle mock/live
 * execution modes. Direct usage is discouraged as it bypasses the framework's execution control
 * mechanisms.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Pattern matching with similarity scoring
 *   <li>OCR-based word detection and extraction
 *   <li>Automatic management of fixed region updates for patterns
 *   <li>Resource cleanup for Finder instances
 * </ul>
 *
 * @see Scene
 * @see Pattern
 * @see Match
 * @see io.github.jspinak.brobot.action.internal.factory.ActionResultFactory
 */
@Component
public class ScenePatternMatcher {

    @Autowired(required = false)
    private DiagnosticLogger diagnosticLogger;

    @Autowired(required = false)
    private ConciseFindLogger conciseFindLogger;

    @Autowired(required = false)
    private LoggingVerbosityConfig verbosityConfig;

    @Autowired(required = false)
    private BestMatchCapture bestMatchCapture;

    /**
     * Creates a Sikuli Finder instance for the given scene image.
     *
     * <p>The method attempts to create the Finder using the scene's BufferedImage if available
     * (preferred for in-memory operations). If the image is empty, it falls back to using the
     * filename, which causes Sikuli to load the image from disk.
     *
     * @param scene The image to use as the search space for find operations. Must contain either a
     *     valid BufferedImage or a valid filename.
     * @return A new Finder instance configured with the scene's image data. The caller is
     *     responsible for calling {@code destroy()} on the returned Finder.
     */
    private Finder getFinder(Image scene) {
        if (!scene.isEmpty()) return new Finder(scene.getBufferedImage());
        return new Finder(scene.getName());
    }

    /**
     * Finds all occurrences of a pattern within a scene using image matching.
     *
     * <p>This method performs template matching to locate all instances of the given pattern within
     * the scene. It enforces a size constraint where the pattern must be smaller than the scene in
     * both dimensions. If the pattern has a fixed region setting, the method automatically updates
     * the pattern's fixed region to the location of the best match (highest similarity score).
     *
     * <p>The method properly manages Finder resources by calling {@code destroy()} after use to
     * prevent memory leaks.
     *
     * @param pattern The image pattern to search for. Must be smaller than the scene in both width
     *     and height dimensions.
     * @param scene The scene image to search within. Serves as the search space.
     * @return A list of all matches found, sorted by their appearance in the scene. Returns an
     *     empty list if the pattern is larger than the scene or no matches are found. Each match
     *     includes similarity score and location.
     * @implNote If the pattern is marked as fixed ({@code pattern.isFixed() == true}), this method
     *     has the side effect of updating the pattern's fixed region to the location of the best
     *     match found.
     */
    public List<Match> findAllInScene(Pattern pattern, Scene scene) {
        // Add null checks
        if (pattern == null || scene == null || scene.getPattern() == null) {
            return new ArrayList<>();
        }

        // Check if pattern has valid image data
        if (pattern.getImage() == null || pattern.getImage().isEmpty()) {
            return new ArrayList<>();
        }

        // Use concise logger if available for initial pattern info
        if (conciseFindLogger != null) {
            conciseFindLogger.logPatternSearch(
                    pattern, scene, org.sikuli.basics.Settings.MinSimilarity);
        } else if (verbosityConfig != null
                && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
            List<io.github.jspinak.brobot.model.element.Region> regions = pattern.getRegions();
            String regionInfo =
                    regions.size() == 1 ? regions.get(0).toString() : regions.size() + " regions";
        }

        // Check size constraints first
        if (pattern.w() > scene.getPattern().w() || pattern.h() > scene.getPattern().h()) {
            return new ArrayList<>();
        }

        // Get the SikuliX pattern once and cache it
        org.sikuli.script.Pattern sikuliPattern = pattern.sikuli();

        // Ensure the pattern has the correct similarity threshold
        double globalSimilarity = org.sikuli.basics.Settings.MinSimilarity;
        if (Math.abs(sikuliPattern.getSimilar() - globalSimilarity) > 0.01) {
            sikuliPattern = sikuliPattern.similar(globalSimilarity);
            // Similarity update is handled by concise logger
        }

        // Already logged by conciseFindLogger above if available
        if (conciseFindLogger == null && diagnosticLogger != null) {
            diagnosticLogger.logPatternSearch(pattern, scene, sikuliPattern.getSimilar());
        } else if (conciseFindLogger == null) {
        }

        // Get search regions for filtering (NOT for cropping)
        List<io.github.jspinak.brobot.model.element.Region> searchRegions =
                pattern.getRegionsForSearch();

        // ALWAYS search the entire image - NO CROPPING
        Finder f = getFinder(scene.getPattern().getImage());

        if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
            if (!searchRegions.isEmpty()) {
                for (io.github.jspinak.brobot.model.element.Region region : searchRegions) {}
            } else {
            }
        }

        if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {}
        f.findAll(sikuliPattern);
        if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {}

        // Process results
        List<Match> matchList = new ArrayList<>();
        int matchCount = 0;
        double bestScore = 0;
        Match bestMatch = null;

        // Reset match tracking for this search
        if (diagnosticLogger != null) {
            diagnosticLogger.resetMatchTracking();
        } else if (conciseFindLogger != null) {
            // ConciseFindLogger handles tracking internally
        }

        // Get the minimum similarity threshold
        double minSimilarity = org.sikuli.basics.Settings.MinSimilarity;

        // Safeguard: If MinSimilarity is unexpectedly low, reset it to default
        if (minSimilarity < 0.5) {
            System.err.println(
                    "[WARNING] MinSimilarity was unexpectedly low ("
                            + minSimilarity
                            + ")! Resetting to 0.7");
            org.sikuli.basics.Settings.MinSimilarity = 0.7;
            minSimilarity = 0.7;
        }

        // Track accepted and rejected matches for summary logging
        int acceptedMatches = 0;
        int rejectedMatches = 0;

        // For tracking top matches to show in verbose mode
        List<Match> topMatches = new ArrayList<>();
        final int MAX_VERBOSE_MATCHES = 3;

        // Track filtered matches for combined logging
        List<String> filteredOutMatches = new ArrayList<>();
        List<String> acceptedRegionMatches = new ArrayList<>();

        while (f.hasNext()) {
            org.sikuli.script.Match sikuliMatch = f.next();

            // FILTER 1: Check if match falls within any search region (if regions are specified)
            boolean withinSearchRegion = true;
            if (!searchRegions.isEmpty()) {
                withinSearchRegion = false;
                int matchX = sikuliMatch.x;
                int matchY = sikuliMatch.y;
                int matchW = sikuliMatch.w;
                int matchH = sikuliMatch.h;

                for (io.github.jspinak.brobot.model.element.Region region : searchRegions) {
                    // Regions are now defined in the same coordinate space as captures
                    // No scaling needed - ScreenResolutionManager ensures consistency
                    int regionX = region.x();
                    int regionY = region.y();
                    int regionW = region.w();
                    int regionH = region.h();

                    // Check if entire match is within this region (not just center)
                    // This ensures consistency with RegionBasedProofer filtering
                    boolean matchFullyContained =
                            matchX >= regionX
                                    && matchY >= regionY
                                    && (matchX + matchW) <= (regionX + regionW)
                                    && (matchY + matchH) <= (regionY + regionH);

                    if (matchFullyContained) {
                        withinSearchRegion = true;
                        if (verbosityConfig != null
                                && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
                            acceptedRegionMatches.add("(" + matchX + ", " + matchY + ")");
                        }
                        break;
                    }
                }

                if (!withinSearchRegion
                        && verbosityConfig != null
                        && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
                    filteredOutMatches.add("(" + matchX + ", " + matchY + ")");
                }
            }

            // FILTER 2: Only process matches that meet the minimum similarity threshold AND are
            // within search regions
            if (withinSearchRegion && sikuliMatch.getScore() >= minSimilarity) {
                Match nextMatch =
                        new Match.Builder()
                                .setSikuliMatch(sikuliMatch)
                                .setName(pattern.getNameWithoutExtension())
                                .build();

                acceptedMatches++;
                matchList.add(nextMatch);

                // Track top matches for verbose summary (but don't log them individually)
                if (topMatches.size() < MAX_VERBOSE_MATCHES
                        || sikuliMatch.getScore()
                                > topMatches.get(topMatches.size() - 1).getScore()) {
                    topMatches.add(nextMatch);
                    // Keep only top N matches sorted by score
                    topMatches.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
                    if (topMatches.size() > MAX_VERBOSE_MATCHES) {
                        topMatches = new ArrayList<>(topMatches.subList(0, MAX_VERBOSE_MATCHES));
                    }
                }

                if (sikuliMatch.getScore() > bestScore) {
                    bestScore = sikuliMatch.getScore();
                    bestMatch = nextMatch;
                }
            } else if (withinSearchRegion) {
                // Match was within search region but didn't meet similarity threshold
                rejectedMatches++;
                if (verbosityConfig != null
                        && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {}
            }
            // else: Match was filtered out by region filter (already logged above)

            matchCount++;

            // Log match details using available logger
            if (conciseFindLogger == null) {
                if (diagnosticLogger != null) {
                    diagnosticLogger.logFoundMatch(
                            matchCount, sikuliMatch.getScore(), sikuliMatch.x, sikuliMatch.y);
                } else if (matchCount <= 3 && sikuliMatch.getScore() >= 0.50) {
                }
            }
            // ConciseFindLogger will log results summary later
        }

        f.destroy();

        // Log combined filter messages if verbose mode is enabled
        if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
            // Log all filtered out matches in one message
            if (!filteredOutMatches.isEmpty()) {
                String filteredList = String.join(", ", filteredOutMatches);
            }

            // Log all accepted matches in one message if needed
            if (!acceptedRegionMatches.isEmpty() && acceptedRegionMatches.size() > 3) {
                // Only log if there are many accepted matches to avoid clutter
            }
        }

        // Log match summary
        if (conciseFindLogger == null) {
            // Use legacy verbose logging
            if (verbosityConfig != null
                    && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
                if (acceptedMatches > 0) {
                    // Show only top 3 matches
                    for (int i = 0; i < Math.min(topMatches.size(), MAX_VERBOSE_MATCHES); i++) {
                        Match m = topMatches.get(i);
                    }

                    if (acceptedMatches > MAX_VERBOSE_MATCHES) {}
                }
            } else if (matchCount > 1 || rejectedMatches > 0) {
                // Normal mode - simpler summary
                if (acceptedMatches > 0) {}
            }
        }

        // Log low-score summary if applicable
        if (diagnosticLogger != null) {
            diagnosticLogger.logLowScoreSummary();
        }

        // Log results using appropriate logger
        if (conciseFindLogger != null) {
            // Use concise logger
            boolean foundAtLowerThreshold =
                    (matchList.isEmpty() && matchCount > 0 && bestScore > 0);
            conciseFindLogger.logPatternResult(
                    pattern, matchList.size(), bestScore, foundAtLowerThreshold);
        } else if (diagnosticLogger != null) {
            diagnosticLogger.logPatternResult(pattern, matchList.size(), bestScore);
        } else {
            if (matchList.isEmpty()) {
            } else {
            }
        }

        // Handle failed matches
        if (matchList.isEmpty()) {
            // Save debug images if enabled and pattern name contains "prompt"
            if (pattern.getNameWithoutExtension() != null
                    && pattern.getNameWithoutExtension().toLowerCase().contains("prompt")) {
                saveDebugImages(pattern, scene);
            }
        }

        // Capture best match for debugging if enabled
        if (bestMatchCapture != null && bestMatchCapture.isCaptureEnabled()) {
            bestMatchCapture.captureBestMatch(pattern, scene, matchList);
        }

        return matchList;
    }

    /**
     * Extracts text regions from a scene using OCR and returns them as Match objects.
     *
     * <p>This method performs optical character recognition on the scene to identify individual
     * words and their locations. Each detected word is wrapped in a {@link Match} object that
     * includes the word's bounding region and text content. The Match objects are automatically
     * associated with the source scene, enabling subsequent image extraction if needed.
     *
     * <p>Each word match is assigned a unique name based on the scene's name (if available) with a
     * "-word{index}" suffix for identification purposes.
     *
     * <p>Technical note: This method uses {@link OCR#readWords} instead of Finder.findWords()
     * because the latter doesn't support file-based operations, while readWords works with
     * BufferedImage objects directly.
     *
     * @param scene The scene to analyze for text content. Must contain a valid BufferedImage
     *     accessible via {@code scene.getPattern().getBImage()}.
     * @return A list of Match objects, one for each word detected in the scene. Each match contains
     *     the word's location and can access the word's text through its Sikuli match. Returns an
     *     empty list if no words are detected.
     * @see OCR#readWords(java.awt.image.BufferedImage)
     * @see Match.Builder#setScene(Scene)
     */
    public List<Match> getWordMatches(Scene scene) {
        List<Match> wordMatches = new ArrayList<>();
        if (scene == null || scene.getPattern() == null || scene.getPattern().getBImage() == null) {
            return wordMatches;
        }
        List<org.sikuli.script.Match> sikuliMatches = OCR.readWords(scene.getPattern().getBImage());
        String baseName =
                scene.getPattern().getNameWithoutExtension() == null
                        ? ""
                        : scene.getPattern().getNameWithoutExtension();
        int i = 0;
        for (org.sikuli.script.Match match : sikuliMatches) {
            Match m =
                    new Match.Builder()
                            .setName(baseName + "-word" + i)
                            .setSikuliMatch(match)
                            .setScene(scene)
                            .build();
            wordMatches.add(m);
            i++;
        }
        return wordMatches;
    }

    /** Saves pattern and scene images for debugging when matches aren't found. */
    private void saveDebugImages(Pattern pattern, Scene scene) {
        try {
            String debugDir = "debug_images";
            File dir = new File(debugDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Image validation and properties
            BufferedImage patternImg = pattern.getBImage();
            BufferedImage sceneImg = scene.getPattern().getBImage();

            // Use DiagnosticLogger for image analysis
            if (diagnosticLogger != null) {
                diagnosticLogger.logImageAnalysis(
                        patternImg, sceneImg, pattern.getNameWithoutExtension());
            } else {
                if (patternImg != null) {
                    analyzeImageContent(patternImg, "Pattern");
                } else {
                }

                if (sceneImg != null) {
                    analyzeImageContent(sceneImg, "Scene");
                } else {
                }
            }

            // Save pattern image
            String patternFile =
                    debugDir + "/pattern_" + pattern.getNameWithoutExtension() + ".png";
            if (patternImg != null) {
                ImageIO.write(patternImg, "png", new File(patternFile));
            }

            // Save scene image (only first time for each pattern set)
            String sceneFile = debugDir + "/scene_current.png";
            File sceneFileObj = new File(sceneFile);
            if (!sceneFileObj.exists() && sceneImg != null) {
                ImageIO.write(sceneImg, "png", sceneFileObj);
            }

            // Progressive similarity testing
            double originalSimilarity = org.sikuli.basics.Settings.MinSimilarity;
            double[] testThresholds = {0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3};
            Double foundThreshold = null;
            Double foundScore = null;

            for (double threshold : testThresholds) {
                org.sikuli.basics.Settings.MinSimilarity = threshold;
                Finder testFinder = getFinder(scene.getPattern().getImage());
                org.sikuli.script.Pattern testPattern = pattern.sikuli().similar(threshold);
                testFinder.findAll(testPattern);

                if (testFinder.hasNext()) {
                    org.sikuli.script.Match firstMatch = testFinder.next();
                    foundThreshold = threshold;
                    foundScore = firstMatch.getScore();
                    testFinder.destroy();
                    break; // Found a match, no need to test lower thresholds
                }
                testFinder.destroy();
            }

            org.sikuli.basics.Settings.MinSimilarity = originalSimilarity;

            // Log similarity analysis using DiagnosticLogger
            if (diagnosticLogger != null) {
                diagnosticLogger.logSimilarityAnalysis(
                        pattern.getNameWithoutExtension(),
                        testThresholds,
                        foundThreshold,
                        foundScore);
            } else {
                if (foundThreshold != null && foundScore != null) {
                } else {
                }
            }

        } catch (Exception e) {
        }
    }

    /** Analyzes image content to detect common issues */
    private void analyzeImageContent(BufferedImage img, String label) {
        int width = img.getWidth();
        int height = img.getHeight();
        int sampleSize = Math.min(100, width * height);

        // Sample pixels to check for uniformity
        int blackCount = 0;
        int whiteCount = 0;
        long totalR = 0, totalG = 0, totalB = 0;

        for (int i = 0; i < sampleSize; i++) {
            int x = (i * 7) % width; // Pseudo-random sampling
            int y = ((i * 13) / width) % height;
            int rgb = img.getRGB(x, y);

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            totalR += r;
            totalG += g;
            totalB += b;

            if (r < 10 && g < 10 && b < 10) blackCount++;
            if (r > 245 && g > 245 && b > 245) whiteCount++;
        }

        double blackPercent = (blackCount * 100.0) / sampleSize;
        double whitePercent = (whiteCount * 100.0) / sampleSize;
        int avgR = (int) (totalR / sampleSize);
        int avgG = (int) (totalG / sampleSize);
        int avgB = (int) (totalB / sampleSize);
        if (blackPercent > 90) {
        } else if (whitePercent > 90) {
        }
    }

    /** Get human-readable image type */
    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "ARGB";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "BGR";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "GRAY";
            default:
                return "Type" + type;
        }
    }

    /** Estimate image size in memory */
    private String estimateImageSize(BufferedImage img) {
        long bytes = (long) img.getWidth() * img.getHeight() * 4; // Assume 4 bytes per pixel
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + "KB";
        return (bytes / (1024 * 1024)) + "MB";
    }
}
