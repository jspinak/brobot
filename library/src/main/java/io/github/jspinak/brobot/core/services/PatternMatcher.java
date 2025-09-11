package io.github.jspinak.brobot.core.services;

import java.awt.image.BufferedImage;
import java.util.List;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Core interface for pattern matching operations.
 *
 * <p>This interface defines the contract for pattern matching implementations, completely decoupled
 * from any higher-level components like Find or Actions. It represents pure pattern matching
 * functionality with no dependencies on the Brobot action framework.
 *
 * <p>Implementations of this interface handle the low-level computer vision operations, whether
 * using OpenCV, Sikuli, or other pattern matching libraries. This separation allows for easy
 * swapping of pattern matching engines and testing with mock implementations.
 *
 * <p>Key design principles:
 *
 * <ul>
 *   <li>No dependencies on Find, Actions, or any higher-level components
 *   <li>Pure pattern matching - input images, output matches
 *   <li>Stateless operations for thread safety
 *   <li>Technology agnostic (OpenCV, Sikuli, etc.)
 * </ul>
 *
 * @since 2.0.0
 */
public interface PatternMatcher {

    /**
     * Configuration options for pattern matching operations. This class encapsulates all parameters
     * needed for matching, keeping the interface methods clean and extensible.
     */
    class MatchOptions {
        private double similarity = 0.7;
        private int maxMatches = Integer.MAX_VALUE;
        private boolean findAll = true;

        public double getSimilarity() {
            return similarity;
        }

        public void setSimilarity(double similarity) {
            this.similarity = similarity;
        }

        public int getMaxMatches() {
            return maxMatches;
        }

        public void setMaxMatches(int maxMatches) {
            this.maxMatches = maxMatches;
        }

        public boolean isFindAll() {
            return findAll;
        }

        public void setFindAll(boolean findAll) {
            this.findAll = findAll;
        }

        public static class Builder {
            private final MatchOptions options = new MatchOptions();

            public Builder withSimilarity(double similarity) {
                options.similarity = similarity;
                return this;
            }

            public Builder withMaxMatches(int maxMatches) {
                options.maxMatches = maxMatches;
                return this;
            }

            public Builder withFindAll(boolean findAll) {
                options.findAll = findAll;
                return this;
            }

            public MatchOptions build() {
                return options;
            }
        }
    }

    /**
     * Result of a single pattern match operation. This lightweight class contains only the
     * essential match information, without any dependencies on Brobot's Match class hierarchy.
     */
    class MatchResult {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final double confidence;

        public MatchResult(int x, int y, int width, int height, double confidence) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.confidence = confidence;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public double getConfidence() {
            return confidence;
        }

        /**
         * Converts this lightweight result to a Brobot Match object. This conversion happens at the
         * boundary between core services and the Brobot framework.
         */
        public Match toMatch() {
            Match match = new Match();
            // Create a Region with the match coordinates and size
            Region region = new Region(x, y, width, height);
            match.setTarget(new Location(region));
            match.setScore(confidence);
            return match;
        }
    }

    /**
     * Finds patterns within a screen image.
     *
     * <p>This method performs the core pattern matching operation, searching for occurrences of the
     * pattern within the screen image. The implementation may use any pattern matching algorithm
     * (template matching, feature matching, etc.).
     *
     * @param screen The screen image to search within
     * @param pattern The pattern to search for
     * @param options Configuration for the matching operation
     * @return List of match results, empty if no matches found
     */
    List<MatchResult> findPatterns(BufferedImage screen, Pattern pattern, MatchOptions options);

    /**
     * Finds patterns within a specific region of the screen.
     *
     * <p>This method optimizes pattern matching by searching only within a specified region of the
     * screen image, improving performance for targeted searches.
     *
     * @param screen The screen image to search within
     * @param pattern The pattern to search for
     * @param regionX X coordinate of the search region
     * @param regionY Y coordinate of the search region
     * @param regionWidth Width of the search region
     * @param regionHeight Height of the search region
     * @param options Configuration for the matching operation
     * @return List of match results, empty if no matches found
     */
    List<MatchResult> findPatternsInRegion(
            BufferedImage screen,
            Pattern pattern,
            int regionX,
            int regionY,
            int regionWidth,
            int regionHeight,
            MatchOptions options);

    /**
     * Checks if this matcher supports a specific pattern type.
     *
     * <p>This allows for specialized matchers that only handle certain types of patterns (e.g.,
     * text patterns, color patterns).
     *
     * @param pattern The pattern to check
     * @return true if this matcher can handle the pattern type
     */
    boolean supportsPattern(Pattern pattern);

    /**
     * Gets the name of this pattern matcher implementation.
     *
     * <p>Used for logging and debugging to identify which matcher is being used.
     *
     * @return Implementation name (e.g., "OpenCV", "Sikuli", "Mock")
     */
    String getImplementationName();
}
