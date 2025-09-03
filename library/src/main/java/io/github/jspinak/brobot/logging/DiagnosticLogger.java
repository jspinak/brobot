package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Diagnostic logger for pattern matching and image analysis.
 * Provides verbosity-aware logging that integrates with both ConsoleReporter
 * and BrobotLogger to ensure diagnostic information is available at all verbosity levels.
 * 
 * <p>In NORMAL mode: Uses ConsoleReporter for concise output
 * <p>In VERBOSE mode: Adds detailed information through BrobotLogger
 * <p>In QUIET mode: Minimal output only
 */
@Component
public class DiagnosticLogger {
    
    @Autowired(required = false)
    private BrobotLogger brobotLogger;
    
    @Autowired(required = false)
    private LoggingVerbosityConfig verbosityConfig;
    
    // Track low-score matches for summarization
    private int lowScoreMatchCount = 0;
    private double lowestScore = 1.0;
    private double highestLowScore = 0.0;
    
    @Value("${brobot.logging.low-score-threshold:0.50}")
    private double lowScoreThreshold = 0.50; // Matches below this are considered low-score
    
    @Value("${brobot.logging.max-detailed-matches:10}")
    private int maxDetailedMatches = 10; // Show details for first N high-score matches
    
    private VerbosityLevel getVerbosity() {
        if (verbosityConfig != null) {
            return verbosityConfig.getVerbosity();
        }
        return VerbosityLevel.NORMAL;
    }
    
    /**
     * Log pattern search attempt
     */
    public void logPatternSearch(Pattern pattern, Scene scene, double similarity) {
        VerbosityLevel level = getVerbosity();
        
        if (level == VerbosityLevel.QUIET) {
            return; // No search logging in quiet mode
        }
        
        // Always log concise version for NORMAL and VERBOSE
        ConsoleReporter.println("[SEARCH] Pattern: '" + pattern.getName() + 
            "' (" + pattern.w() + "x" + pattern.h() + 
            ") | Similarity: " + String.format("%.2f", similarity) + 
            " | Scene: " + scene.getPattern().w() + "x" + scene.getPattern().h());
        
        // Add detailed logging for VERBOSE mode
        if (level == VerbosityLevel.VERBOSE && brobotLogger != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("patternName", pattern.getName());
            metadata.put("patternSize", pattern.w() + "x" + pattern.h());
            metadata.put("sceneSize", scene.getPattern().w() + "x" + scene.getPattern().h());
            metadata.put("similarity", similarity);
            metadata.put("patternFixed", pattern.isFixed());
            metadata.put("patternDynamic", pattern.isDynamic());
            
            brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.DEBUG)
                .action("PATTERN_SEARCH")
                .metadata(metadata)
                .log();
        }
    }
    
    /**
     * Log pattern match result
     */
    public void logPatternResult(Pattern pattern, int matchCount, double bestScore) {
        VerbosityLevel level = getVerbosity();
        
        if (level == VerbosityLevel.QUIET) {
            // Minimal output in quiet mode
            if (matchCount == 0) {
                ConsoleReporter.print("✗");
            } else {
                ConsoleReporter.print("✓");
            }
            return;
        }
        
        // Normal/Verbose output
        if (matchCount == 0) {
            ConsoleReporter.println("  [RESULT] NO MATCHES for '" + pattern.getName() + "'");
        } else {
            ConsoleReporter.println("  [RESULT] " + matchCount + " matches for '" + 
                pattern.getName() + "' | Best score: " + String.format("%.3f", bestScore));
        }
        
        // Additional verbose logging
        if (level == VerbosityLevel.VERBOSE && brobotLogger != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("patternName", pattern.getName());
            metadata.put("matchCount", matchCount);
            metadata.put("bestScore", bestScore);
            metadata.put("success", matchCount > 0);
            
            brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(matchCount > 0 ? LogEvent.Level.DEBUG : LogEvent.Level.WARNING)
                .action("PATTERN_RESULT")
                .success(matchCount > 0)
                .metadata(metadata)
                .log();
        }
    }
    
    /**
     * Log image analysis for failed matches
     */
    public void logImageAnalysis(BufferedImage patternImg, BufferedImage sceneImg, String patternName) {
        VerbosityLevel level = getVerbosity();
        
        // Only log in VERBOSE mode to reduce clutter
        if (level != VerbosityLevel.VERBOSE) {
            return;
        }
        
        if (patternImg != null && sceneImg != null) {
            // Concise one-line comparison
            String typeComparison = getImageType(patternImg.getType()).equals(getImageType(sceneImg.getType())) ?
                getImageType(patternImg.getType()) :
                getImageType(patternImg.getType()) + " vs " + getImageType(sceneImg.getType());
            
            ConsoleReporter.println(String.format("    [IMG] Pattern %dx%d, Scene %dx%d, Types: %s",
                patternImg.getWidth(), patternImg.getHeight(),
                sceneImg.getWidth(), sceneImg.getHeight(),
                typeComparison));
            
            // Only analyze content if there might be an issue
            analyzeImageContentIfProblematic(patternImg, sceneImg);
        }
        
        // Still log to BrobotLogger in verbose mode
        if (brobotLogger != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("patternName", patternName);
            metadata.put("patternValid", patternImg != null);
            metadata.put("sceneValid", sceneImg != null);
            
            if (patternImg != null) {
                metadata.put("patternSize", patternImg.getWidth() + "x" + patternImg.getHeight());
                metadata.put("patternType", getImageType(patternImg.getType()));
            }
            
            if (sceneImg != null) {
                metadata.put("sceneSize", sceneImg.getWidth() + "x" + sceneImg.getHeight());
                metadata.put("sceneType", getImageType(sceneImg.getType()));
            }
            
            brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.WARNING)
                .action("IMAGE_ANALYSIS")
                .metadata(metadata)
                .log();
        }
    }
    
    /**
     * Only analyze and log image content if there's likely a problem
     */
    private void analyzeImageContentIfProblematic(BufferedImage patternImg, BufferedImage sceneImg) {
        // Sample a few pixels to check for obvious problems
        boolean patternIsBlack = isImageMostlyColor(patternImg, 0, 0, 0, 10);
        boolean patternIsWhite = isImageMostlyColor(patternImg, 255, 255, 255, 245);
        boolean sceneIsBlack = isImageMostlyColor(sceneImg, 0, 0, 0, 10);
        boolean sceneIsWhite = isImageMostlyColor(sceneImg, 255, 255, 255, 245);
        
        if (patternIsBlack || patternIsWhite || sceneIsBlack || sceneIsWhite) {
            String warning = "    [WARNING] ";
            if (patternIsBlack) warning += "Pattern is BLACK ";
            if (patternIsWhite) warning += "Pattern is WHITE ";
            if (sceneIsBlack) warning += "Scene is BLACK ";
            if (sceneIsWhite) warning += "Scene is WHITE ";
            ConsoleReporter.println(warning.trim());
        }
    }
    
    /**
     * Quick check if image is mostly a specific color
     */
    private boolean isImageMostlyColor(BufferedImage img, int targetR, int targetG, int targetB, int threshold) {
        int sampleSize = Math.min(20, img.getWidth() * img.getHeight());
        int matches = 0;
        
        for (int i = 0; i < sampleSize; i++) {
            int x = (i * 7) % img.getWidth();
            int y = ((i * 13) / img.getWidth()) % img.getHeight();
            int rgb = img.getRGB(x, y);
            
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            
            if (Math.abs(r - targetR) <= threshold && 
                Math.abs(g - targetG) <= threshold && 
                Math.abs(b - targetB) <= threshold) {
                matches++;
            }
        }
        
        return matches > (sampleSize * 0.8); // 80% threshold
    }
    
    /**
     * Log similarity threshold analysis
     */
    public void logSimilarityAnalysis(String patternName, double[] thresholds, Double foundThreshold, Double foundScore) {
        VerbosityLevel level = getVerbosity();
        
        // Only log in verbose mode
        if (level != VerbosityLevel.VERBOSE) {
            return;
        }
        
        if (foundThreshold != null && foundScore != null) {
            ConsoleReporter.println(String.format("    [SIM] Found at %.1f with score %.3f", 
                foundThreshold, foundScore));
        } else {
            ConsoleReporter.println("    [SIM] No match at tested thresholds");
        }
        
        // Still log to BrobotLogger
        if (brobotLogger != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("patternName", patternName);
            metadata.put("testedThresholds", thresholds);
            metadata.put("foundThreshold", foundThreshold);
            metadata.put("foundScore", foundScore);
            metadata.put("matchFound", foundThreshold != null);
            
            brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.INFO)
                .action("SIMILARITY_ANALYSIS")
                .metadata(metadata)
                .log();
        }
    }
    
    /**
     * Log Pattern.sikuli() calls with caching info
     */
    public void logPatternSikuliCall(String patternName, boolean cached) {
        VerbosityLevel level = getVerbosity();
        
        // Only log in VERBOSE mode
        if (level != VerbosityLevel.VERBOSE) {
            return;
        }
        
        if (cached) {
            ConsoleReporter.println("  [Pattern.sikuli()] Using CACHED SikuliX Pattern for: " + patternName);
        } else {
            ConsoleReporter.println("  [Pattern.sikuli()] Creating NEW SikuliX Pattern for: " + patternName);
        }
        
        if (brobotLogger != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("patternName", patternName);
            metadata.put("cached", cached);
            
            brobotLogger.log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.DEBUG)
                .action("PATTERN_SIKULI")
                .metadata(metadata)
                .log();
        }
    }
    
    /**
     * Log found match details with intelligent filtering and summarization
     */
    public void logFoundMatch(int matchNumber, double score, int x, int y) {
        VerbosityLevel level = getVerbosity();
        
        if (level == VerbosityLevel.QUIET) {
            return;
        }
        
        // Track low-score matches for summarization
        if (score < lowScoreThreshold) {
            lowScoreMatchCount++;
            lowestScore = Math.min(lowestScore, score);
            highestLowScore = Math.max(highestLowScore, score);
            
            // In VERBOSE mode, show first few low-score matches as examples
            if (level == VerbosityLevel.VERBOSE && lowScoreMatchCount <= 3) {
                ConsoleReporter.println(String.format("  [LOW-SCORE #%d] Score: %.3f at (%d, %d)",
                    matchNumber, score, x, y));
            }
            return;
        }
        
        // For high-score matches, show details based on verbosity level
        boolean showDetails = false;
        
        if (level == VerbosityLevel.NORMAL) {
            // In NORMAL mode, show top 3 high-score matches
            showDetails = (matchNumber <= 3 && score >= lowScoreThreshold);
        } else if (level == VerbosityLevel.VERBOSE) {
            // In VERBOSE mode, show more high-score matches but still limit
            showDetails = (matchNumber <= maxDetailedMatches && score >= lowScoreThreshold);
        }
        
        if (showDetails) {
            ConsoleReporter.println(String.format("  [FOUND #%d] Score: %.3f at (%d, %d)",
                matchNumber, score, x, y));
        }
    }
    
    /**
     * Reset match tracking for a new search
     */
    public void resetMatchTracking() {
        lowScoreMatchCount = 0;
        lowestScore = 1.0;
        highestLowScore = 0.0;
    }
    
    /**
     * Log summary of low-score matches if any were found
     */
    public void logLowScoreSummary() {
        if (lowScoreMatchCount > 0) {
            if (lowScoreMatchCount > 3) {
                ConsoleReporter.println(String.format("  [LOW-SCORE SUMMARY] %d matches below %.2f threshold (range: %.3f-%.3f)",
                    lowScoreMatchCount, lowScoreThreshold, lowestScore, highestLowScore));
            }
        }
    }
    
    // Helper methods
    
    private void analyzeAndLogImageContent(BufferedImage img, String label, VerbosityLevel level) {
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
        
        String analysis = String.format("      %s content: %.1f%% black, %.1f%% white, avg RGB=(%d,%d,%d)",
            label, blackPercent, whitePercent, avgR, avgG, avgB);
        ConsoleReporter.println(analysis);
        
        if (blackPercent > 90) {
            ConsoleReporter.println("      WARNING: " + label + " is mostly BLACK - possible capture failure!");
        } else if (whitePercent > 90) {
            ConsoleReporter.println("      WARNING: " + label + " is mostly WHITE - possible capture issue!");
        }
    }
    
    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB";
            case BufferedImage.TYPE_3BYTE_BGR: return "BGR";
            case BufferedImage.TYPE_BYTE_GRAY: return "GRAY";
            case BufferedImage.TYPE_BYTE_INDEXED: return "INDEXED";
            default: return "Type" + type;
        }
    }
    
    private String estimateImageSize(BufferedImage img) {
        long bytes = (long)img.getWidth() * img.getHeight() * 4; // Assume 4 bytes per pixel
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + "KB";
        return (bytes / (1024 * 1024)) + "MB";
    }
}