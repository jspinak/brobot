package io.github.jspinak.brobot.debug;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.capture.BrobotScreenCapture;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static io.github.jspinak.brobot.debug.AnsiColor.*;

/**
 * Central debugging orchestrator for image finding operations.
 * Provides comprehensive debugging output including visual annotations,
 * detailed logging, and performance metrics.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "brobot.debug.image.enabled", havingValue = "true")
public class ImageFindDebugger {
    
    @Autowired(required = false)
    private ImageDebugConfig config;
    
    @Autowired(required = false)
    private BrobotScreenCapture screenCapture;
    
    @Autowired(required = false)
    private VisualDebugRenderer visualRenderer;
    
    @Autowired(required = false)
    private DebugReportGenerator reportGenerator;
    
    private final AtomicInteger operationCounter = new AtomicInteger(0);
    private String sessionId;
    private Path debugOutputPath;
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    @Data
    public static class FindDebugInfo {
        private int operationId;
        private String patternName;
        private String patternPath;
        private int patternWidth;
        private int patternHeight;
        private Region searchRegion;
        private double similarityThreshold;
        private long searchDuration;
        private boolean found;
        private int matchCount;
        private double bestScore;
        private List<Match> matches;
        private String screenshotPath;
        private String patternImagePath;
        private String comparisonPath;
        private LocalDateTime timestamp;
        private String failureReason;
    }
    
    @PostConstruct
    public void init() {
        if (config != null && config.isEnabled()) {
            initializeSession();
        }
    }
    
    /**
     * Initialize debugging session.
     */
    public void initializeSession() {
        if (config == null || !config.isEnabled()) return;
        
        sessionId = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        debugOutputPath = Paths.get(config.getOutputDir(), sessionId);
        
        try {
            Files.createDirectories(debugOutputPath);
            Files.createDirectories(debugOutputPath.resolve("screenshots"));
            Files.createDirectories(debugOutputPath.resolve("patterns"));
            Files.createDirectories(debugOutputPath.resolve("comparisons"));
            Files.createDirectories(debugOutputPath.resolve("logs"));
            
            printSessionHeader();
        } catch (IOException e) {
            log.error("Failed to create debug directories", e);
        }
    }
    
    /**
     * Debug a find operation with comprehensive output.
     */
    public FindDebugInfo debugFindOperation(
            ObjectCollection objectCollection,
            PatternFindOptions options,
            ActionResult result) {
        
        if (config == null || !config.isEnabled()) return null;
        
        if (sessionId == null) {
            initializeSession();
        }
        
        FindDebugInfo debugInfo = new FindDebugInfo();
        debugInfo.operationId = operationCounter.incrementAndGet();
        debugInfo.timestamp = LocalDateTime.now();
        
        try {
            // Capture pattern information
            if (!objectCollection.getStateImages().isEmpty()) {
                StateImage stateImage = objectCollection.getStateImages().get(0);
                capturePatternInfo(stateImage, debugInfo);
            }
            
            // Capture search parameters
            captureSearchParameters(options, debugInfo);
            
            // Capture results
            captureResults(result, debugInfo);
            
            // Save debug files if configured
            if (config.shouldSaveFiles()) {
                saveDebugFiles(debugInfo, objectCollection);
            }
            
            // Print colorful console output
            printDebugOutput(debugInfo);
            
            // Generate visual debugging if enabled
            if (config.isVisualEnabled() && visualRenderer != null) {
                BufferedImage annotated = visualRenderer.createAnnotatedScreenshot(
                    objectCollection, result, debugInfo.searchRegion);
                if (annotated != null) {
                    visualRenderer.saveVisualDebug(annotated, "annotated", sessionId, debugInfo.operationId);
                }
                
                if (config.getVisual().isCreateComparisonGrid() && !result.getMatchList().isEmpty()) {
                    BufferedImage comparison = visualRenderer.createComparisonGrid(
                        objectCollection.getStateImages().get(0),
                        result.getMatchList(),
                        screenCapture != null ? screenCapture.capture() : null);
                    if (comparison != null) {
                        visualRenderer.saveVisualDebug(comparison, "comparison", sessionId, debugInfo.operationId);
                    }
                }
            }
            
            // Add to report generator
            if (reportGenerator != null) {
                reportGenerator.addOperation(sessionId, debugInfo);
            }
            
        } catch (Exception e) {
            log.error("Error during debug operation", e);
        }
        
        return debugInfo;
    }
    
    private void capturePatternInfo(StateImage stateImage, FindDebugInfo debugInfo) {
        debugInfo.patternName = stateImage.getName();
        
        if (!stateImage.getPatterns().isEmpty()) {
            Pattern pattern = stateImage.getPatterns().get(0);
            if (pattern.getBImage() != null) {
                debugInfo.patternWidth = pattern.getBImage().getWidth();
                debugInfo.patternHeight = pattern.getBImage().getHeight();
            }
            debugInfo.patternPath = pattern.getUrl();
        }
    }
    
    private void captureSearchParameters(PatternFindOptions options, FindDebugInfo debugInfo) {
        if (options != null) {
            debugInfo.similarityThreshold = options.getSimilarity();
            // Note: options.getSearchRegions() returns SearchRegions, not Region
            // For now, we'll leave searchRegion as null
        } else {
            debugInfo.similarityThreshold = 0.8; // Default
        }
    }
    
    private void captureResults(ActionResult result, FindDebugInfo debugInfo) {
        debugInfo.found = result.isSuccess();
        debugInfo.matchCount = result.size();
        debugInfo.matches = new ArrayList<>(result.getMatchList());
        
        if (!result.getMatchList().isEmpty()) {
            debugInfo.bestScore = result.getMatchList().stream()
                .mapToDouble(Match::getScore)
                .max()
                .orElse(0.0);
        } else {
            debugInfo.bestScore = 0.0;
            debugInfo.failureReason = determineFailureReason(debugInfo);
        }
        
        debugInfo.searchDuration = result.getDuration().toMillis();
    }
    
    private String determineFailureReason(FindDebugInfo debugInfo) {
        if (debugInfo.bestScore > 0 && debugInfo.bestScore < debugInfo.similarityThreshold) {
            return String.format("Best match (%.2f) below threshold (%.2f)", 
                debugInfo.bestScore, debugInfo.similarityThreshold);
        } else if (debugInfo.bestScore == 0) {
            return "No similar regions found";
        } else {
            return "Unknown failure reason";
        }
    }
    
    private void saveDebugFiles(FindDebugInfo debugInfo, ObjectCollection objectCollection) {
        try {
            // Save screenshot
            if (config.isSaveScreenshots() && screenCapture != null) {
                BufferedImage screenshot = screenCapture.capture();
                if (screenshot != null) {
                    String filename = String.format("screen_%03d.png", debugInfo.operationId);
                    Path screenshotPath = debugOutputPath.resolve("screenshots").resolve(filename);
                    ImageIO.write(screenshot, "png", screenshotPath.toFile());
                    debugInfo.screenshotPath = screenshotPath.toString();
                }
            }
            
            // Save pattern image
            if (config.isSavePatterns() && !objectCollection.getStateImages().isEmpty()) {
                StateImage stateImage = objectCollection.getStateImages().get(0);
                if (!stateImage.getPatterns().isEmpty()) {
                    Pattern pattern = stateImage.getPatterns().get(0);
                    if (pattern.getBImage() != null) {
                        String filename = String.format("pattern_%03d_%s.png", 
                            debugInfo.operationId, 
                            sanitizeFilename(debugInfo.patternName));
                        Path patternPath = debugOutputPath.resolve("patterns").resolve(filename);
                        ImageIO.write(pattern.getBImage(), "png", patternPath.toFile());
                        debugInfo.patternImagePath = patternPath.toString();
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to save debug files", e);
        }
    }
    
    private void printSessionHeader() {
        if (!config.getConsole().isShowBox()) return;
        
        String header = String.format(
            "%s IMAGE FIND DEBUG SESSION STARTED %s\n" +
            "%s Session ID: %s\n" +
            "%s Output Dir: %s\n" +
            "%s Debug Level: %s",
            colorize(STAR, BRIGHT_YELLOW),
            colorize(STAR, BRIGHT_YELLOW),
            colorize(ARROW_RIGHT, BRIGHT_CYAN),
            colorize(sessionId, BRIGHT_WHITE),
            colorize(ARROW_RIGHT, BRIGHT_CYAN),
            colorize(debugOutputPath.toString(), BRIGHT_WHITE),
            colorize(ARROW_RIGHT, BRIGHT_CYAN),
            colorize(config.getLevel().toString(), BRIGHT_MAGENTA)
        );
        
        System.out.println("\n" + box("IMAGE DEBUG", header, BRIGHT_BLUE));
    }
    
    private void printDebugOutput(FindDebugInfo debugInfo) {
        if (!config.getConsole().isUseColors()) {
            printPlainOutput(debugInfo);
            return;
        }
        
        StringBuilder output = new StringBuilder();
        
        // Timestamp
        if (config.getConsole().isShowTimestamp()) {
            output.append(dim("[" + debugInfo.timestamp.format(LOG_TIME_FORMAT) + "] "));
        }
        
        // Operation ID
        output.append(info("FIND #" + debugInfo.operationId + " "));
        
        // Pattern name
        output.append(header(debugInfo.patternName != null ? debugInfo.patternName : "unknown"));
        output.append(" ");
        
        // Dimensions
        if (debugInfo.patternWidth > 0) {
            output.append(dim(String.format("(%dx%d)", debugInfo.patternWidth, debugInfo.patternHeight)));
            output.append(" ");
        }
        
        // Result
        if (debugInfo.found) {
            output.append(success(CHECK + " FOUND"));
            output.append(" ");
            output.append(success(String.format("[%.2f%%]", debugInfo.bestScore * 100)));
            if (debugInfo.matchCount > 1) {
                output.append(" ");
                output.append(info("(" + debugInfo.matchCount + " matches)"));
            }
        } else {
            output.append(error(CROSS + " NOT FOUND"));
            if (debugInfo.bestScore > 0) {
                output.append(" ");
                output.append(warning(String.format("best: %.2f%%", debugInfo.bestScore * 100)));
            }
            if (debugInfo.failureReason != null) {
                output.append(" ");
                output.append(dim("- " + debugInfo.failureReason));
            }
        }
        
        // Duration
        output.append(" ");
        output.append(dim(HOURGLASS + " " + debugInfo.searchDuration + "ms"));
        
        System.out.println(output.toString());
        
        // Additional details for higher debug levels
        if (config.isLevelEnabled(ImageDebugConfig.DebugLevel.DETAILED)) {
            printDetailedInfo(debugInfo);
        }
    }
    
    private void printDetailedInfo(FindDebugInfo debugInfo) {
        StringBuilder details = new StringBuilder();
        
        // Search parameters
        details.append("  ").append(dim("Search: "));
        details.append(info(String.format("threshold=%.2f", debugInfo.similarityThreshold)));
        
        if (debugInfo.searchRegion != null) {
            details.append(" ");
            details.append(info(String.format("region=%s", debugInfo.searchRegion)));
        }
        
        System.out.println(details.toString());
        
        // Match locations
        if (debugInfo.found && !debugInfo.matches.isEmpty()) {
            System.out.println("  " + dim("Matches:"));
            int count = 0;
            for (Match match : debugInfo.matches) {
                if (++count > 3 && !config.isLevelEnabled(ImageDebugConfig.DebugLevel.FULL)) {
                    System.out.println("    " + dim("... and " + (debugInfo.matches.size() - 3) + " more"));
                    break;
                }
                System.out.println(String.format("    %s %s at (%d,%d) score=%.3f",
                    colorize(BULLET, BRIGHT_GREEN),
                    dim("Match"),
                    match.getTarget().getX(), match.getTarget().getY(),
                    match.getScore()
                ));
            }
        }
        
        // File paths
        if (config.shouldSaveFiles()) {
            if (debugInfo.screenshotPath != null) {
                System.out.println("  " + dim("Screenshot: ") + dim(debugInfo.screenshotPath));
            }
            if (debugInfo.patternImagePath != null) {
                System.out.println("  " + dim("Pattern: ") + dim(debugInfo.patternImagePath));
            }
        }
    }
    
    private void printPlainOutput(FindDebugInfo debugInfo) {
        String result = debugInfo.found ? "FOUND" : "NOT FOUND";
        System.out.printf("[%s] FIND #%d %s %s (%.2f%%) %dms%n",
            debugInfo.timestamp.format(LOG_TIME_FORMAT),
            debugInfo.operationId,
            debugInfo.patternName != null ? debugInfo.patternName : "unknown",
            result,
            debugInfo.bestScore * 100,
            debugInfo.searchDuration
        );
    }
    
    private String sanitizeFilename(String name) {
        if (name == null) return "unknown";
        return name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
    
    /**
     * Finalize the session and generate reports.
     */
    public void finalizeSession() {
        if (config == null || !config.isEnabled()) return;
        
        // Generate reports
        if (reportGenerator != null) {
            reportGenerator.generateHtmlReport(sessionId);
            reportGenerator.generateJsonReport(sessionId);
        }
        
        // Print summary
        printSessionSummary();
    }
    
    /**
     * Print a summary of the debug session.
     */
    public void printSessionSummary() {
        if (config == null || !config.isEnabled()) return;
        
        String summary = String.format(
            "%s Total operations: %d\n" +
            "%s Session duration: %s\n" +
            "%s Output saved to: %s",
            colorize(BULLET, BRIGHT_CYAN),
            operationCounter.get(),
            colorize(BULLET, BRIGHT_CYAN),
            "N/A", // Would need to track start time
            colorize(BULLET, BRIGHT_CYAN),
            debugOutputPath != null ? debugOutputPath.toString() : "N/A"
        );
        
        System.out.println("\n" + box("SESSION SUMMARY", summary, BRIGHT_GREEN));
    }
}