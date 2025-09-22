package io.github.jspinak.brobot.logging;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
// Removed old logging import:
/**
 * Provides concise logging for find operations with intelligent deduplication and summarization to
 * reduce repetitive log output.
 *
 * <p>This logger tracks patterns that have already been logged in a search session and provides
 * summarized output for repeated searches, significantly reducing log verbosity while maintaining
 * useful diagnostic information.
 */
@Component
public class ConciseFindLogger {

    @Autowired(required = false)
    private LoggingVerbosityConfig verbosityConfig;

    // Track patterns we've already logged details for in this search session
    private final Map<String, PatternSearchInfo> loggedPatterns = new ConcurrentHashMap<>();

    // Track the current search session
    private String currentSessionId = null;
    private long sessionStartTime = 0;
    private int totalSearchAttempts = 0;
    private Set<String> sessionPatterns = new HashSet<>();

    /** Start a new search session. This resets the deduplication tracking. */
    public void startSearchSession(String sessionId) {
        if (!isSameSession(sessionId)) {
            // New session - clear previous tracking
            loggedPatterns.clear();
            currentSessionId = sessionId;
            sessionStartTime = System.currentTimeMillis();
            totalSearchAttempts = 0;
            sessionPatterns.clear();

            if (isVerbose()) {
            }
        }
    }

    /** End the current search session and provide summary if needed. */
    public void endSearchSession(String sessionId, boolean success, int totalMatches) {
        if (isSameSession(sessionId)) {
            long duration = System.currentTimeMillis() - sessionStartTime;

            // Provide session summary
            if (totalSearchAttempts > 1) {
                String status = success ? "SUCCESS" : "FAILED";
                String summary =
                        String.format(
                                "[FIND %s] %d patterns searched %d times in %dms, %d matches found",
                                status,
                                sessionPatterns.size(),
                                totalSearchAttempts,
                                duration,
                                totalMatches);
            }

            // Clear session data
            currentSessionId = null;
            sessionStartTime = 0;
            totalSearchAttempts = 0;
            sessionPatterns.clear();
        }
    }

    /**
     * Log a pattern search with intelligent deduplication. First occurrence shows full details,
     * subsequent show minimal info.
     */
    public void logPatternSearch(Pattern pattern, Scene scene, double similarity) {
        if (pattern == null || scene == null) return;

        VerbosityLevel level = getVerbosity();
        if (level == VerbosityLevel.QUIET) return;

        String patternKey = pattern.getNameWithoutExtension();
        sessionPatterns.add(patternKey);
        totalSearchAttempts++;

        PatternSearchInfo info = loggedPatterns.get(patternKey);

        if (info == null) {
            // First time seeing this pattern - log full details
            info = new PatternSearchInfo(pattern, scene, similarity);
            loggedPatterns.put(patternKey, info);

            // Build concise log message
            StringBuilder msg = new StringBuilder();
            msg.append(
                    String.format(
                            "[SEARCH] %s (%dx%d) sim=%.2f",
                            pattern.getNameWithoutExtension(), pattern.w(), pattern.h(), similarity));

            // Add scene size only if not using a constrained region
            boolean hasConstrainedRegion = false;
            if (pattern.getRegions() != null && !pattern.getRegions().isEmpty()) {
                io.github.jspinak.brobot.model.element.Region region = pattern.getRegions().get(0);
                if (region.w() < scene.getPattern().w() || region.h() < scene.getPattern().h()) {
                    hasConstrainedRegion = true;
                    msg.append(
                            String.format(
                                    " in [%d,%d %dx%d]",
                                    region.x(), region.y(), region.w(), region.h()));
                }
            }

            if (!hasConstrainedRegion) {
                msg.append(
                        String.format(
                                " scene=%dx%d", scene.getPattern().w(), scene.getPattern().h()));
            }

            String logMessage = msg.toString();

        } else {
            // Already logged this pattern - increment counter
            info.incrementSearchCount();

            // Only log again if similarity changed or in verbose mode
            if (Math.abs(info.lastSimilarity - similarity) > 0.01) {
                info.lastSimilarity = similarity;
            } else if (isVerbose() && info.searchCount <= 3) {
                // In verbose mode, show first 3 repeats only
            }
        }
    }

    /** Log pattern results concisely. */
    public void logPatternResult(
            Pattern pattern, int matchCount, double bestScore, boolean foundAtLowerThreshold) {
        VerbosityLevel level = getVerbosity();
        if (level == VerbosityLevel.QUIET) {
            // Minimal output in quiet mode
            return;
        }

        PatternSearchInfo info = loggedPatterns.get(pattern.getNameWithoutExtension());

        if (matchCount == 0) {
            if (foundAtLowerThreshold && info != null && !info.hasLoggedLowerThreshold) {
                // Log that match exists at lower threshold (only once per pattern)
                info.hasLoggedLowerThreshold = true;
            } else if (info == null || info.searchCount == 1) {
                // First failure for this pattern
            }
        } else {
            // Found matches
        }
    }

    /** Log a batch of patterns being searched together. */
    public void logBatchSearch(List<Pattern> patterns, String targetName) {
        if (patterns.isEmpty()) return;

        VerbosityLevel level = getVerbosity();
        if (level == VerbosityLevel.QUIET) return;

        if (patterns.size() == 1) {
            // Single pattern, will be logged normally
            return;
        }

        // For multiple patterns, provide a header
    }

    /** Log image analysis only when it provides new information. */
    public void logImageAnalysis(String patternName, String analysisType, String details) {
        PatternSearchInfo info = loggedPatterns.get(patternName);

        if (info != null && info.hasLoggedAnalysis) {
            // Already logged analysis for this pattern in this session
            return;
        }

        if (info != null) {
            info.hasLoggedAnalysis = true;
        }

    }

    // Helper methods

    private boolean isSameSession(String sessionId) {
        return currentSessionId != null && currentSessionId.equals(sessionId);
    }

    private VerbosityLevel getVerbosity() {
        if (verbosityConfig != null) {
            return verbosityConfig.getVerbosity();
        }
        return VerbosityLevel.NORMAL;
    }

    private boolean isVerbose() {
        return getVerbosity() == VerbosityLevel.VERBOSE;
    }

    /** Internal class to track information about patterns we've logged. */
    private static class PatternSearchInfo {
        final String patternName;
        final int patternWidth;
        final int patternHeight;
        final int sceneWidth;
        final int sceneHeight;
        double lastSimilarity;
        int searchCount;
        boolean hasLoggedAnalysis;
        boolean hasLoggedLowerThreshold;

        PatternSearchInfo(Pattern pattern, Scene scene, double similarity) {
            this.patternName = pattern.getNameWithoutExtension();
            this.patternWidth = pattern.w();
            this.patternHeight = pattern.h();
            this.sceneWidth = scene.getPattern().w();
            this.sceneHeight = scene.getPattern().h();
            this.lastSimilarity = similarity;
            this.searchCount = 1;
            this.hasLoggedAnalysis = false;
            this.hasLoggedLowerThreshold = false;
        }

        void incrementSearchCount() {
            searchCount++;
        }
    }
}
