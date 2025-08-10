package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.springframework.beans.factory.annotation.Autowired;
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
        
        // Only log in NORMAL and VERBOSE modes
        if (level == VerbosityLevel.QUIET) {
            return;
        }
        
        ConsoleReporter.println("    [IMAGE ANALYSIS]");
        
        if (patternImg != null) {
            String imageInfo = String.format("      Pattern: %dx%d type=%s bytes=%s",
                patternImg.getWidth(), patternImg.getHeight(),
                getImageType(patternImg.getType()),
                estimateImageSize(patternImg));
            ConsoleReporter.println(imageInfo);
            
            // Analyze content
            analyzeAndLogImageContent(patternImg, "Pattern", level);
        } else {
            ConsoleReporter.println("      Pattern image is NULL!");
        }
        
        if (sceneImg != null) {
            String imageInfo = String.format("      Scene: %dx%d type=%s bytes=%s",
                sceneImg.getWidth(), sceneImg.getHeight(),
                getImageType(sceneImg.getType()),
                estimateImageSize(sceneImg));
            ConsoleReporter.println(imageInfo);
            
            // Analyze content
            analyzeAndLogImageContent(sceneImg, "Scene", level);
        } else {
            ConsoleReporter.println("      Scene image is NULL!");
        }
        
        // Verbose mode adds more details
        if (level == VerbosityLevel.VERBOSE && brobotLogger != null) {
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
     * Log similarity threshold analysis
     */
    public void logSimilarityAnalysis(String patternName, double[] thresholds, Double foundThreshold, Double foundScore) {
        VerbosityLevel level = getVerbosity();
        
        if (level == VerbosityLevel.QUIET) {
            return;
        }
        
        ConsoleReporter.println("    [SIMILARITY ANALYSIS]");
        
        if (foundThreshold != null && foundScore != null) {
            ConsoleReporter.println(String.format("      Threshold %.1f: FOUND with score %.3f", 
                foundThreshold, foundScore));
        } else {
            ConsoleReporter.println("      No match found at any threshold tested");
            if (level == VerbosityLevel.VERBOSE) {
                ConsoleReporter.println("      Tested thresholds: " + 
                    java.util.Arrays.toString(thresholds));
            }
        }
        
        // Verbose logging
        if (level == VerbosityLevel.VERBOSE && brobotLogger != null) {
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
     * Log found match details
     */
    public void logFoundMatch(int matchNumber, double score, int x, int y) {
        VerbosityLevel level = getVerbosity();
        
        if (level == VerbosityLevel.QUIET) {
            return;
        }
        
        // Log first 3 matches in NORMAL mode, all in VERBOSE
        if (matchNumber <= 3 || level == VerbosityLevel.VERBOSE) {
            ConsoleReporter.println(String.format("  [FOUND #%d] Score: %.3f at (%d, %d)",
                matchNumber, score, x, y));
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