package io.github.jspinak.brobot.action.methods.find;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Automatically detects and applies the correct scaling for pattern matching.
 * This solves the DPI/scaling mismatch problem without manual configuration.
 */
@Component
public class AutoScalingFinder {
    
    private static final Logger log = LoggerFactory.getLogger(AutoScalingFinder.class);
    
    // Common DPI scaling factors
    private static final double[] COMMON_SCALES = {
        1.0,    // Original size (100% DPI)
        0.8,    // 125% DPI to 100% DPI conversion
        1.25,   // 100% DPI to 125% DPI conversion
        0.75,   // 133% DPI to 100% DPI conversion
        1.33,   // 100% DPI to 133% DPI conversion
        0.67,   // 150% DPI to 100% DPI conversion
        1.5,    // 100% DPI to 150% DPI conversion
        0.9,    // Minor scaling difference
        1.1     // Minor scaling difference
    };
    
    // Cache successful scales for patterns
    private final Map<String, Double> patternScaleCache = new ConcurrentHashMap<>();
    
    // Global scale factor (learned over time)
    private double globalScaleFactor = 1.0;
    private int globalScaleConfidence = 0;
    
    /**
     * Find pattern with automatic scale detection.
     * Tries multiple scales and caches the best one for future use.
     */
    public Match findWithAutoScale(BufferedImage searchImage, Pattern pattern, double minSimilarity) {
        String patternKey = getPatternKey(pattern);
        
        // Try cached scale first
        if (patternScaleCache.containsKey(patternKey)) {
            double cachedScale = patternScaleCache.get(patternKey);
            Match match = findWithScale(searchImage, pattern, cachedScale, minSimilarity);
            if (match != null && match.getScore() >= minSimilarity) {
                log.debug("Pattern {} found with cached scale {}", patternKey, cachedScale);
                return match;
            }
        }
        
        // Try global scale if we have high confidence
        if (globalScaleConfidence > 5 && globalScaleFactor != 1.0) {
            Match match = findWithScale(searchImage, pattern, globalScaleFactor, minSimilarity);
            if (match != null && match.getScore() >= minSimilarity) {
                log.debug("Pattern {} found with global scale {}", patternKey, globalScaleFactor);
                updatePatternCache(patternKey, globalScaleFactor);
                return match;
            }
        }
        
        // Try all scales to find the best match
        return findBestScale(searchImage, pattern, minSimilarity);
    }
    
    /**
     * Find the best scale for a pattern by trying multiple options.
     */
    private Match findBestScale(BufferedImage searchImage, Pattern pattern, double minSimilarity) {
        String patternKey = getPatternKey(pattern);
        Match bestMatch = null;
        double bestScore = 0;
        double bestScale = 1.0;
        
        log.debug("Searching for best scale for pattern {}", patternKey);
        
        for (double scale : COMMON_SCALES) {
            Match match = findWithScale(searchImage, pattern, scale, 0.5); // Lower threshold for testing
            
            if (match != null) {
                double score = match.getScore();
                log.trace("Scale {} achieved score {}", scale, score);
                
                if (score > bestScore) {
                    bestScore = score;
                    bestScale = scale;
                    bestMatch = match;
                    
                    // If we found an excellent match, stop searching
                    if (score >= 0.95) {
                        log.debug("Excellent match found at scale {} with score {}", scale, score);
                        break;
                    }
                }
            }
        }
        
        // Cache the best scale if we found a good match
        if (bestMatch != null && bestScore >= minSimilarity) {
            updatePatternCache(patternKey, bestScale);
            updateGlobalScale(bestScale);
            log.info("Pattern {} matches best at scale {} with score {}", 
                     patternKey, bestScale, bestScore);
        }
        
        return (bestMatch != null && bestScore >= minSimilarity) ? bestMatch : null;
    }
    
    /**
     * Find pattern with a specific scale applied.
     */
    private Match findWithScale(BufferedImage searchImage, Pattern pattern, 
                                double scale, double minSimilarity) {
        try {
            // Save current setting
            float originalResize = Settings.AlwaysResize;
            
            // Apply scale
            Settings.AlwaysResize = (float)scale;
            
            // Create finder and search
            Finder finder = new Finder(searchImage);
            // Get the SikuliX pattern from Brobot pattern - use sikuli() method which handles file path loading
            org.sikuli.script.Pattern sikuliPattern = pattern.sikuli();
            sikuliPattern = sikuliPattern.similar(minSimilarity);
            
            finder.find(sikuliPattern);
            
            Match match = null;
            if (finder.hasNext()) {
                match = finder.next();
            }
            
            // Restore original setting
            Settings.AlwaysResize = originalResize;
            
            finder.destroy();
            return match;
            
        } catch (Exception e) {
            log.error("Error finding pattern with scale {}: {}", scale, e.getMessage());
            return null;
        }
    }
    
    /**
     * Update pattern cache with successful scale.
     */
    private void updatePatternCache(String patternKey, double scale) {
        patternScaleCache.put(patternKey, scale);
        log.debug("Cached scale {} for pattern {}", scale, patternKey);
    }
    
    /**
     * Update global scale factor based on successful matches.
     */
    private void updateGlobalScale(double scale) {
        if (scale == globalScaleFactor) {
            globalScaleConfidence++;
        } else if (globalScaleConfidence < 3) {
            // Switch to new scale if we don't have high confidence in current one
            globalScaleFactor = scale;
            globalScaleConfidence = 1;
        }
        
        log.debug("Global scale factor: {} (confidence: {})", 
                  globalScaleFactor, globalScaleConfidence);
    }
    
    /**
     * Get unique key for pattern caching.
     */
    private String getPatternKey(Pattern pattern) {
        if (pattern.getName() != null && !pattern.getName().isEmpty()) {
            return pattern.getName();
        }
        // Use image properties as fallback
        return "pattern_" + pattern.hashCode();
    }
    
    /**
     * Clear all cached scales (useful when environment changes).
     */
    public void clearCache() {
        patternScaleCache.clear();
        globalScaleFactor = 1.0;
        globalScaleConfidence = 0;
        log.info("Cleared all cached scales");
    }
    
    /**
     * Get statistics about scaling performance.
     */
    public ScalingStatistics getStatistics() {
        return new ScalingStatistics(
            globalScaleFactor,
            globalScaleConfidence,
            patternScaleCache.size(),
            new ArrayList<>(patternScaleCache.values())
        );
    }
    
    /**
     * Statistics about auto-scaling performance.
     */
    public static class ScalingStatistics {
        public final double globalScale;
        public final int confidence;
        public final int cachedPatterns;
        public final List<Double> commonScales;
        
        public ScalingStatistics(double globalScale, int confidence, 
                                int cachedPatterns, List<Double> scales) {
            this.globalScale = globalScale;
            this.confidence = confidence;
            this.cachedPatterns = cachedPatterns;
            this.commonScales = scales;
        }
        
        @Override
        public String toString() {
            return String.format(
                "AutoScaling Stats: globalScale=%.2f (confidence=%d), cached=%d patterns", 
                globalScale, confidence, cachedPatterns
            );
        }
    }
}