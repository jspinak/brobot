package io.github.jspinak.brobot.tools.testing.mock;

import java.util.Random;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Generates synthetic Match objects for probability-based mock testing scenarios.
 *
 * <p>MockMatchBuilder provides a flexible builder pattern for creating Match objects with
 * controlled randomness when testing without actual image recognition. Unlike snapshot-based
 * testing which uses historical match data, this class generates matches dynamically based on
 * probabilities and constraints, enabling more varied test scenarios.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Builder pattern for flexible match configuration
 *   <li>Random placement within search regions
 *   <li>Support for fixed or dynamic positioning
 *   <li>Pattern-based dimension matching
 *   <li>Constraint validation to ensure valid matches
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Testing find operations without actual image matching
 *   <li>Simulating successful matches in specific regions
 *   <li>Creating edge case scenarios (matches at boundaries)
 *   <li>Performance testing with controlled match generation
 *   <li>Testing match-dependent logic without GUI dependencies
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Create a match at specific location
 * Match fixedMatch = new MockMatchBuilder.Builder()
 *     .setImageXYWH(100, 200, 50, 30)
 *     .build();
 *
 * // Create a random match within search region
 * Match randomMatch = new MockMatchBuilder.Builder()
 *     .setSearchRegion(searchArea)
 *     .setPattern(targetPattern)
 *     .build();
 * }</pre>
 *
 * <p>The builder supports two modes:
 *
 * <ul>
 *   <li>Fixed position - When x,y coordinates are explicitly set
 *   <li>Random position - When only search region is provided
 * </ul>
 *
 * <p>Thread safety: The Builder is not thread-safe and should be used by a single thread. Multiple
 * builders can be used concurrently.
 *
 * @see Match
 * @see Pattern
 * @see Region
 * @see MockFind
 */
@Component
public class MockMatchBuilder {

    /**
     * Builder for creating customized Match objects with various configuration options.
     *
     * <p>The builder follows a fluent interface pattern, allowing method chaining for readable
     * match construction. It handles both deterministic and probabilistic match generation based on
     * the provided configuration.
     */
    public static class Builder {
        private int x = -1;
        private int y = -1;
        private int w = 200;
        private int h = 100;
        private Region searchRegion = new Region();

        /**
         * Sets the search region within which random matches will be generated.
         *
         * <p>When x,y coordinates are not explicitly set, the match will be randomly positioned
         * within this region, ensuring it fits completely inside the boundaries.
         *
         * @param searchRegion The region to constrain random match placement. Must not be null.
         * @return This builder instance for method chaining
         */
        public Builder setSearchRegion(Region searchRegion) {
            this.searchRegion = searchRegion;
            return this;
        }

        /**
         * Sets the match dimensions based on a Pattern object.
         *
         * <p>This method extracts width and height from the pattern to ensure the generated match
         * has the same dimensions as the pattern it supposedly found. Null patterns are safely
         * ignored.
         *
         * @param pattern The pattern whose dimensions will be used for the match. Can be null, in
         *     which case dimensions remain unchanged.
         * @return This builder instance for method chaining
         */
        public Builder setPattern(Pattern pattern) {
            if (pattern == null) return this;
            this.w = pattern.w();
            this.h = pattern.h();
            return this;
        }

        public Builder setImageWH(int w, int h) {
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder setImageXYWH(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder setImageXYWH(Region region) {
            this.x = region.x();
            this.y = region.y();
            this.w = region.w();
            this.h = region.h();
            return this;
        }

        /**
         * Builds the Match object based on the configured parameters.
         *
         * <p>The build process follows this logic:
         *
         * <ul>
         *   <li>If x,y coordinates are set (>= 0), creates a fixed-position match
         *   <li>Otherwise, creates a randomly positioned match within the search region
         * </ul>
         *
         * @return A new Match instance configured according to the builder settings
         */
        public Match build() {
            if (x >= 0 && y >= 0) return buildWithXYWH();
            return buildInSearchRegion();
        }

        private Match buildWithXYWH() {
            return new Match.Builder().setRegion(x, y, w, h).build();
        }

        /**
         * Builds a Match with random positioning within the search region.
         *
         * <p>The match is positioned randomly but constrained to fit completely within the search
         * region boundaries. Uses Math.max(1, ...) to prevent negative bounds in Random.nextInt()
         * when the match size equals or exceeds the search region size.
         *
         * @return A new Match randomly positioned within the search region
         */
        private Match buildInSearchRegion() {
            x = searchRegion.x() + new Random().nextInt(Math.max(1, searchRegion.w() - w));
            y = searchRegion.y() + new Random().nextInt(Math.max(1, searchRegion.h() - h));
            return new Match.Builder().setRegion(x, y, w, h).build();
        }
    }
}
