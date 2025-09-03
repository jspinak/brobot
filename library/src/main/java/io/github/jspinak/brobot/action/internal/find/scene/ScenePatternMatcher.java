package io.github.jspinak.brobot.action.internal.find.scene;

import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.logging.DiagnosticLogger;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.logging.ConciseFindLogger;

import org.sikuli.script.Finder;
import org.sikuli.script.OCR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs image pattern matching and OCR text detection within captured scenes.
 * <p>
 * This wrapper class provides low-level search functionality for finding visual patterns
 * and text within {@link Scene} objects. It leverages Sikuli's {@link Finder} for
 * pattern matching and {@link OCR} for text recognition. Scene objects represent
 * screenshots or loaded images that serve as the search space.
 * <p>
 * This class is designed to be used by higher-level action classes that handle
 * mock/live execution modes. Direct usage is discouraged as it bypasses the
 * framework's execution control mechanisms.
 * <p>
 * Key responsibilities:
 * <ul>
 * <li>Pattern matching with similarity scoring</li>
 * <li>OCR-based word detection and extraction</li>
 * <li>Automatic management of fixed region updates for patterns</li>
 * <li>Resource cleanup for Finder instances</li>
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
     * <p>
     * The method attempts to create the Finder using the scene's BufferedImage
     * if available (preferred for in-memory operations). If the image is empty,
     * it falls back to using the filename, which causes Sikuli to load the
     * image from disk.
     * 
     * @param scene The image to use as the search space for find operations.
     *              Must contain either a valid BufferedImage or a valid filename.
     * @return A new Finder instance configured with the scene's image data.
     *         The caller is responsible for calling {@code destroy()} on the returned Finder.
     */
    private Finder getFinder(Image scene) {
        if (!scene.isEmpty()) return new Finder(scene.getBufferedImage());
        return new Finder(scene.getName());
    }

    /**
     * Finds all occurrences of a pattern within a scene using image matching.
     * <p>
     * This method performs template matching to locate all instances of the given
     * pattern within the scene. It enforces a size constraint where the pattern
     * must be smaller than the scene in both dimensions. If the pattern has a
     * fixed region setting, the method automatically updates the pattern's fixed
     * region to the location of the best match (highest similarity score).
     * <p>
     * The method properly manages Finder resources by calling {@code destroy()}
     * after use to prevent memory leaks.
     * 
     * @param pattern The image pattern to search for. Must be smaller than the scene
     *                in both width and height dimensions.
     * @param scene The scene image to search within. Serves as the search space.
     * @return A list of all matches found, sorted by their appearance in the scene.
     *         Returns an empty list if the pattern is larger than the scene or
     *         no matches are found. Each match includes similarity score and location.
     *         
     * @implNote If the pattern is marked as fixed ({@code pattern.isFixed() == true}),
     *           this method has the side effect of updating the pattern's fixed region
     *           to the location of the best match found.
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
            conciseFindLogger.logPatternSearch(pattern, scene, org.sikuli.basics.Settings.MinSimilarity);
        } else if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
            List<io.github.jspinak.brobot.model.element.Region> regions = pattern.getRegions();
            String regionInfo = regions.size() == 1 ? regions.get(0).toString() : regions.size() + " regions";
            ConsoleReporter.println("[REGION] '" + pattern.getName() + "': " + 
                (pattern.isFixed() ? "fixed @ " : "search in ") + regionInfo);
        }
        
        // Check size constraints first
        if (pattern.w()>scene.getPattern().w() || pattern.h()>scene.getPattern().h()) {
            ConsoleReporter.println("[SKIP] Pattern '" + pattern.getName() + "' (" + pattern.w() + "x" + pattern.h() + 
                ") larger than scene (" + scene.getPattern().w() + "x" + scene.getPattern().h() + ")");
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
            // Fallback to ConsoleReporter
            ConsoleReporter.println("[SEARCH] Pattern: '" + pattern.getName() + "' (" + pattern.w() + "x" + pattern.h() + 
                ") | Similarity: " + sikuliPattern.getSimilar() + " | Scene: " + 
                scene.getPattern().w() + "x" + scene.getPattern().h());
        }
        
        // OPTIMIZATION: If pattern has a single constrained search region, search only that region
        List<io.github.jspinak.brobot.model.element.Region> searchRegions = pattern.getRegionsForSearch();
        boolean hasConstrainedRegion = false;
        io.github.jspinak.brobot.model.element.Region constrainedRegion = null;
        
        // Check if we have exactly one search region that's not full screen
        if (searchRegions.size() == 1) {
            constrainedRegion = searchRegions.get(0);
            // Check if this is NOT a full-screen region
            if (!(constrainedRegion.x() == 0 && constrainedRegion.y() == 0 && 
                  constrainedRegion.w() >= scene.getPattern().w() && 
                  constrainedRegion.h() >= scene.getPattern().h())) {
                hasConstrainedRegion = true;
                // Region info will be logged when sub-image is created
            }
        }
        
        Finder f;
        int regionOffsetX = 0;
        int regionOffsetY = 0;
        
        if (hasConstrainedRegion && scene.getPattern().getBImage() != null) {
            // Extract sub-image for the constrained region
            BufferedImage sceneImage = scene.getPattern().getBImage();
            int x = Math.max(0, constrainedRegion.x());
            int y = Math.max(0, constrainedRegion.y());
            int w = Math.min(constrainedRegion.w(), sceneImage.getWidth() - x);
            int h = Math.min(constrainedRegion.h(), sceneImage.getHeight() - y);
            
            if (w > 0 && h > 0 && w >= pattern.w() && h >= pattern.h()) {
                // Region is valid and large enough for the pattern
                BufferedImage subImage = sceneImage.getSubimage(x, y, w, h);
                f = new Finder(subImage);
                regionOffsetX = x;
                regionOffsetY = y;
                if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
                    ConsoleReporter.println("[SEARCH REGION] Constrained to: " + x + "," + y + " " + w + "x" + h);
                }
            } else {
                // Region too small for pattern - impossible to find a match
                ConsoleReporter.println("[SKIP] Search region (" + w + "x" + h + 
                    ") is smaller than pattern (" + pattern.w() + "x" + pattern.h() + ") - no matches possible");
                return new ArrayList<>();  // Return empty list immediately
            }
        } else {
            // No constrained region or no BufferedImage, search entire scene
            f = getFinder(scene.getPattern().getImage());
        }
        
        if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
            ConsoleReporter.println("[FINDER] Similarity: " + sikuliPattern.getSimilar());
        }
        f.findAll(sikuliPattern);
        if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
            ConsoleReporter.println("[FINDER] Search complete");
        }
        
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
            System.err.println("[WARNING] MinSimilarity was unexpectedly low (" + minSimilarity + ")! Resetting to 0.7");
            org.sikuli.basics.Settings.MinSimilarity = 0.7;
            minSimilarity = 0.7;
        }
        
        // Track accepted and rejected matches for summary logging
        int acceptedMatches = 0;
        int rejectedMatches = 0;
        
        // For tracking top matches to show in verbose mode
        List<Match> topMatches = new ArrayList<>();
        final int MAX_VERBOSE_MATCHES = 3;
        
        while (f.hasNext()) {
            org.sikuli.script.Match sikuliMatch = f.next();
            
            // If we searched a sub-region, adjust coordinates back to scene coordinates
            if (hasConstrainedRegion) {
                sikuliMatch.x += regionOffsetX;
                sikuliMatch.y += regionOffsetY;
            }
            
            // FILTER: Only process matches that meet the minimum similarity threshold
            if (sikuliMatch.getScore() >= minSimilarity) {
                Match nextMatch = new Match.Builder()
                        .setSikuliMatch(sikuliMatch)
                        .setName(pattern.getName())
                        .build();
                
                acceptedMatches++;
                matchList.add(nextMatch);
                
                // Track top matches for verbose summary (but don't log them individually)
                if (topMatches.size() < MAX_VERBOSE_MATCHES || 
                    sikuliMatch.getScore() > topMatches.get(topMatches.size() - 1).getScore()) {
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
            } else {
                rejectedMatches++;
                // Only log rejected matches in verbose mode
                if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
                    ConsoleReporter.println("[MATCH] Rejected at (" + sikuliMatch.x + ", " + sikuliMatch.y + 
                        ") score=" + String.format("%.3f", sikuliMatch.getScore()) + " < " + 
                        String.format("%.3f", minSimilarity));
                }
            }
            
            matchCount++;
            
            // Log match details using available logger
            if (conciseFindLogger == null) {
                if (diagnosticLogger != null) {
                    diagnosticLogger.logFoundMatch(matchCount, sikuliMatch.getScore(), sikuliMatch.x, sikuliMatch.y);
                } else if (matchCount <= 3 && sikuliMatch.getScore() >= 0.50) {
                    // Fallback to ConsoleReporter for first 3 high-score matches only
                    ConsoleReporter.println("  [FOUND #" + matchCount + "] Score: " + 
                        String.format("%.3f", sikuliMatch.getScore()) + " at (" + 
                        sikuliMatch.x + ", " + sikuliMatch.y + ")");
                }
            }
            // ConciseFindLogger will log results summary later
        }
        
        f.destroy();
        
        // Log match summary
        if (conciseFindLogger == null) {
            // Use legacy verbose logging
            if (verbosityConfig != null && verbosityConfig.getVerbosity() == VerbosityLevel.VERBOSE) {
                if (acceptedMatches > 0) {
                    ConsoleReporter.println("  [MATCH SUMMARY] " + acceptedMatches + " matches found" + 
                        (rejectedMatches > 0 ? ", " + rejectedMatches + " rejected" : "") + 
                        " (threshold=" + String.format("%.3f", minSimilarity) + ")");
                    
                    // Show only top 3 matches
                    ConsoleReporter.println("  [TOP MATCHES]");
                    for (int i = 0; i < Math.min(topMatches.size(), MAX_VERBOSE_MATCHES); i++) {
                        Match m = topMatches.get(i);
                        ConsoleReporter.println(String.format("    #%d: Score %.3f at %s", 
                            i + 1, m.getScore(), m.getRegion()));
                    }
                    
                    if (acceptedMatches > MAX_VERBOSE_MATCHES) {
                        ConsoleReporter.println("    ... and " + (acceptedMatches - MAX_VERBOSE_MATCHES) + 
                            " more matches");
                    }
                }
            } else if (matchCount > 1 || rejectedMatches > 0) {
                // Normal mode - simpler summary
                if (acceptedMatches > 0) {
                    ConsoleReporter.println("  [MATCHES] " + acceptedMatches + " accepted, " + 
                        rejectedMatches + " rejected (threshold=" + String.format("%.2f", minSimilarity) + ")");
                }
            }
        }
        
        // Log low-score summary if applicable
        if (diagnosticLogger != null) {
            diagnosticLogger.logLowScoreSummary();
        }
        
        // Log results using appropriate logger
        if (conciseFindLogger != null) {
            // Use concise logger
            boolean foundAtLowerThreshold = (matchList.isEmpty() && matchCount > 0 && bestScore > 0);
            conciseFindLogger.logPatternResult(pattern, matchList.size(), bestScore, foundAtLowerThreshold);
        } else if (diagnosticLogger != null) {
            diagnosticLogger.logPatternResult(pattern, matchList.size(), bestScore);
        } else {
            // Fallback to ConsoleReporter
            if (matchList.isEmpty()) {
                ConsoleReporter.println("  [RESULT] NO MATCHES for '" + pattern.getName() + "'");
            } else {
                ConsoleReporter.println("  [RESULT] " + matchList.size() + " matches for '" + pattern.getName() + 
                    "' | Best: " + String.format("%.3f", bestScore));
            }
        }
        
        // Handle failed matches
        if (matchList.isEmpty()) {
            // Save debug images if enabled and pattern name contains "prompt"
            if (pattern.getName() != null && pattern.getName().toLowerCase().contains("prompt")) {
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
     * <p>
     * This method performs optical character recognition on the scene to identify
     * individual words and their locations. Each detected word is wrapped in a
     * {@link Match} object that includes the word's bounding region and text content.
     * The Match objects are automatically associated with the source scene, enabling
     * subsequent image extraction if needed.
     * <p>
     * Each word match is assigned a unique name based on the scene's name (if available)
     * with a "-word{index}" suffix for identification purposes.
     * <p>
     * Technical note: This method uses {@link OCR#readWords} instead of Finder.findWords()
     * because the latter doesn't support file-based operations, while readWords works
     * with BufferedImage objects directly.
     * 
     * @param scene The scene to analyze for text content. Must contain a valid
     *              BufferedImage accessible via {@code scene.getPattern().getBImage()}.
     * @return A list of Match objects, one for each word detected in the scene.
     *         Each match contains the word's location and can access the word's
     *         text through its Sikuli match. Returns an empty list if no words
     *         are detected.
     *         
     * @see OCR#readWords(java.awt.image.BufferedImage)
     * @see Match.Builder#setScene(Scene)
     */
    public List<Match> getWordMatches(Scene scene) {
        List<Match> wordMatches = new ArrayList<>();
        if (scene == null || scene.getPattern() == null || scene.getPattern().getBImage() == null) {
            return wordMatches;
        }
        List<org.sikuli.script.Match> sikuliMatches = OCR.readWords(scene.getPattern().getBImage());
        String baseName = scene.getPattern().getName() == null ? "" : scene.getPattern().getName();
        int i=0;
        for (org.sikuli.script.Match match : sikuliMatches) {
            Match m = new Match.Builder()
                    .setName(baseName+"-word"+i)
                    .setSikuliMatch(match)
                    .setScene(scene)
                    .build();
            wordMatches.add(m);
            i++;
        }
        return wordMatches;
    }
    
    /**
     * Saves pattern and scene images for debugging when matches aren't found.
     */
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
                diagnosticLogger.logImageAnalysis(patternImg, sceneImg, pattern.getName());
            } else {
                // Fallback to ConsoleReporter
                ConsoleReporter.println("    [IMAGE ANALYSIS]");
                if (patternImg != null) {
                    ConsoleReporter.println("      Pattern: " + patternImg.getWidth() + "x" + patternImg.getHeight() + 
                        " type=" + getImageType(patternImg.getType()) + " bytes=" + estimateImageSize(patternImg));
                    analyzeImageContent(patternImg, "Pattern");
                } else {
                    ConsoleReporter.println("      Pattern image is NULL!");
                }
                
                if (sceneImg != null) {
                    ConsoleReporter.println("      Scene: " + sceneImg.getWidth() + "x" + sceneImg.getHeight() + 
                        " type=" + getImageType(sceneImg.getType()) + " bytes=" + estimateImageSize(sceneImg));
                    analyzeImageContent(sceneImg, "Scene");
                } else {
                    ConsoleReporter.println("      Scene image is NULL!");
                }
            }
            
            // Save pattern image
            String patternFile = debugDir + "/pattern_" + pattern.getName() + ".png";
            if (patternImg != null) {
                ImageIO.write(patternImg, "png", new File(patternFile));
                ConsoleReporter.println("    [DEBUG] Saved pattern image to: " + patternFile);
            }
            
            // Save scene image (only first time for each pattern set)
            String sceneFile = debugDir + "/scene_current.png";
            File sceneFileObj = new File(sceneFile);
            if (!sceneFileObj.exists() && sceneImg != null) {
                ImageIO.write(sceneImg, "png", sceneFileObj);
                ConsoleReporter.println("    [DEBUG] Saved scene image to: " + sceneFile);
            }
            
            // Progressive similarity testing
            double originalSimilarity = org.sikuli.basics.Settings.MinSimilarity;
            double[] testThresholds = {0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3};
            Double foundThreshold = null;
            Double foundScore = null;
            
            ConsoleReporter.println("    [SIMILARITY DEBUG] Testing pattern '" + pattern.getName() + "'");
            ConsoleReporter.println("    [SIMILARITY DEBUG] Original MinSimilarity: " + originalSimilarity);
            ConsoleReporter.println("    [SIMILARITY DEBUG] Pattern type before conversion: Type" + patternImg.getType());
            ConsoleReporter.println("    [SIMILARITY DEBUG] Scene type: Type" + sceneImg.getType());
            
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
                diagnosticLogger.logSimilarityAnalysis(pattern.getName(), testThresholds, foundThreshold, foundScore);
            } else {
                // Fallback to ConsoleReporter
                ConsoleReporter.println("    [SIMILARITY ANALYSIS]");
                if (foundThreshold != null && foundScore != null) {
                    ConsoleReporter.println("      Threshold " + String.format("%.1f", foundThreshold) + 
                        ": FOUND with score " + String.format("%.3f", foundScore));
                } else {
                    ConsoleReporter.println("      No match found at any threshold tested");
                }
            }
            
        } catch (Exception e) {
            ConsoleReporter.println("    [DEBUG] Error in analysis: " + e.getMessage());
        }
    }
    
    /**
     * Analyzes image content to detect common issues
     */
    private void analyzeImageContent(BufferedImage img, String label) {
        int width = img.getWidth();
        int height = img.getHeight();
        int sampleSize = Math.min(100, width * height);
        
        // Sample pixels to check for uniformity
        int blackCount = 0;
        int whiteCount = 0;
        long totalR = 0, totalG = 0, totalB = 0;
        
        for (int i = 0; i < sampleSize; i++) {
            int x = (i * 7) % width;  // Pseudo-random sampling
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
        int avgR = (int)(totalR / sampleSize);
        int avgG = (int)(totalG / sampleSize);
        int avgB = (int)(totalB / sampleSize);
        
        ConsoleReporter.println("      " + label + " content: " + 
            String.format("%.1f%%", blackPercent) + " black, " +
            String.format("%.1f%%", whitePercent) + " white, " +
            "avg RGB=(" + avgR + "," + avgG + "," + avgB + ")");
        
        if (blackPercent > 90) {
            ConsoleReporter.println("      WARNING: " + label + " is mostly BLACK - possible capture failure!");
        } else if (whitePercent > 90) {
            ConsoleReporter.println("      WARNING: " + label + " is mostly WHITE - possible capture issue!");
        }
    }
    
    /**
     * Get human-readable image type
     */
    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB";
            case BufferedImage.TYPE_3BYTE_BGR: return "BGR";
            case BufferedImage.TYPE_BYTE_GRAY: return "GRAY";
            default: return "Type" + type;
        }
    }
    
    /**
     * Estimate image size in memory
     */
    private String estimateImageSize(BufferedImage img) {
        long bytes = (long)img.getWidth() * img.getHeight() * 4; // Assume 4 bytes per pixel
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + "KB";
        return (bytes / (1024 * 1024)) + "MB";
    }

}
