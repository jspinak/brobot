package io.github.jspinak.brobot.logging.events;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.logging.LogLevel;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

import lombok.Builder;
import lombok.Value;

/**
 * Event representing pattern matching operations in the Brobot framework.
 *
 * <p>Captures detailed information about find operations including the search strategy used,
 * matches found, timing, and search regions. This is essential for debugging pattern matching
 * issues and performance optimization.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * MatchEvent event = MatchEvent.builder()
 *     .pattern("submitButton")
 *     .matches(foundMatches)
 *     .searchTime(Duration.ofMillis(150))
 *     .strategy(FindStrategy.FIRST)
 *     .searchRegion(searchArea)
 *     .build();
 * }</pre>
 */
@Value
@Builder(toBuilder = true)
public class MatchEvent {

    /** Timestamp when the match operation was initiated */
    @Builder.Default Instant timestamp = Instant.now();

    /** Pattern description (e.g., image name, text content) */
    String pattern;

    /** List of matches found (empty if no matches) */
    @Builder.Default List<Match> matches = java.util.Collections.emptyList();

    /** Time taken to complete the search */
    Duration searchTime;

    /** Strategy used for matching */
    FindStrategy strategy;

    /** Region where the search was performed */
    Region searchRegion;

    /** Similarity threshold used */
    double similarityThreshold;

    /** Maximum number of matches requested */
    int maxMatches;

    /** Whether the search was successful (found at least one match) */
    boolean success;

    /** Error message if search failed */
    String errorMessage;

    /** Additional metadata about the search */
    @Builder.Default Map<String, Object> metadata = java.util.Collections.emptyMap();

    /** Correlation ID for tracking related actions */
    String correlationId;

    /** Current state when search was performed */
    String currentState;

    /** Number of images searched */
    int imagesSearched;

    /** Total search area in pixels */
    long searchAreaPixels;

    /**
     * Create a MatchEvent for a successful search.
     *
     * @param pattern The pattern that was searched
     * @param matches The matches found
     * @param searchTime How long the search took
     * @param strategy The strategy used
     * @return A new MatchEvent for a successful search
     */
    public static MatchEvent success(
            String pattern, List<Match> matches, Duration searchTime, FindStrategy strategy) {
        return MatchEvent.builder()
                .pattern(pattern)
                .matches(matches)
                .searchTime(searchTime)
                .strategy(strategy)
                .success(true)
                .build();
    }

    /**
     * Create a MatchEvent for a failed search.
     *
     * @param pattern The pattern that was searched
     * @param searchTime How long the search took
     * @param strategy The strategy used
     * @param errorMessage The error message
     * @return A new MatchEvent for a failed search
     */
    public static MatchEvent failure(
            String pattern, Duration searchTime, FindStrategy strategy, String errorMessage) {
        return MatchEvent.builder()
                .pattern(pattern)
                .searchTime(searchTime)
                .strategy(strategy)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Get the number of matches found.
     *
     * @return The number of matches found
     */
    public int getMatchCount() {
        return matches.size();
    }

    /**
     * Get the best (highest similarity) match if any.
     *
     * @return The best match, or null if no matches
     */
    public Match getBestMatch() {
        return matches.stream()
                .max((m1, m2) -> Double.compare(m1.getScore(), m2.getScore()))
                .orElse(null);
    }

    /**
     * Get the average similarity score of all matches.
     *
     * @return The average similarity score, or 0.0 if no matches
     */
    public double getAverageScore() {
        return matches.stream().mapToDouble(Match::getScore).average().orElse(0.0);
    }

    /**
     * Get a human-readable description of this match event.
     *
     * @return A formatted description
     */
    public String getDescription() {
        if (success && !matches.isEmpty()) {
            Match best = getBestMatch();
            double bestScore = best != null ? best.getScore() : 0.0;

            return String.format(
                    "MATCH %s → %d matches [%dms] %s best:%.2f avg:%.2f",
                    pattern,
                    getMatchCount(),
                    searchTime.toMillis(),
                    strategy,
                    bestScore,
                    getAverageScore());
        } else if (success) {
            return String.format(
                    "MATCH %s → 0 matches [%dms] %s", pattern, searchTime.toMillis(), strategy);
        } else {
            return String.format(
                    "MATCH %s → FAILED [%dms] %s%s",
                    pattern,
                    searchTime.toMillis(),
                    strategy,
                    errorMessage != null ? " " + errorMessage : "");
        }
    }

    /**
     * Check if this was a fast search (completed quickly).
     *
     * @param thresholdMs The threshold in milliseconds
     * @return true if the search was faster than the threshold
     */
    public boolean isFastSearch(long thresholdMs) {
        return searchTime.toMillis() < thresholdMs;
    }

    /**
     * Check if this was a high-quality match (good similarity scores).
     *
     * @param threshold The similarity threshold
     * @return true if the average score is above the threshold
     */
    public boolean isHighQuality(double threshold) {
        return getAverageScore() >= threshold;
    }

    /**
     * Get the log level for this match event. Successful searches log at DEBUG level, failures at
     * WARN level.
     *
     * @return The appropriate log level
     */
    public LogLevel getLevel() {
        return success ? LogLevel.DEBUG : LogLevel.WARN;
    }

    /**
     * Get a message describing this match event.
     *
     * @return A formatted message
     */
    public String getMessage() {
        if (success && !matches.isEmpty()) {
            return String.format("Found %d matches for %s", getMatchCount(), pattern);
        } else if (success) {
            return String.format("No matches found for %s", pattern);
        } else {
            return String.format(
                    "Search for %s failed%s",
                    pattern, errorMessage != null ? ": " + errorMessage : "");
        }
    }

    /**
     * Check if matches were found.
     *
     * @return true if at least one match was found
     */
    public boolean isFound() {
        return success && !matches.isEmpty();
    }

    /**
     * Get the duration of the search operation.
     *
     * @return The search duration
     */
    public Duration getDuration() {
        return searchTime;
    }

    /**
     * Get the similarity score of the best match.
     *
     * @return The best match similarity, or 0.0 if no matches
     */
    public Double getSimilarity() {
        Match best = getBestMatch();
        return best != null ? best.getScore() : 0.0;
    }

    /**
     * Get the location of the best match.
     *
     * @return The best match location, or null if no matches
     */
    public Location getLocation() {
        Match best = getBestMatch();
        return best != null ? best.getTarget() : null;
    }

    /**
     * Get any error associated with this match event.
     *
     * @return null since match events don't carry exception objects
     */
    public Throwable getError() {
        return null; // MatchEvent only has error messages, not exceptions
    }
}
