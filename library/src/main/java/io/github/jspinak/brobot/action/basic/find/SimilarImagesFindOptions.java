package io.github.jspinak.brobot.action.basic.find;

import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for find operations that compare images for similarity.
 * <p>
 * This class encapsulates parameters specific to finding similar images between
 * two ObjectCollections. The first ObjectCollection contains base images for 
 * comparison, and the second ObjectCollection contains images to compare against 
 * the base images. For each image in the second collection, it finds the best 
 * matching image from the first collection and returns a Match object with 
 * similarity scores.
 * <p>
 * This is particularly useful for:
 * <ul>
 *   <li>Screen state recognition - determining which known screen is currently displayed</li>
 *   <li>Image classification - categorizing images based on similarity to known templates</li>
 *   <li>Change detection - finding which images have changed between collections</li>
 *   <li>Duplicate detection - identifying similar or duplicate images across collections</li>
 * </ul>
 * <p>
 * The similarity comparison is performed at the Pattern level, where each StateImage's
 * patterns are compared to find the best match. The larger image automatically becomes
 * the scene, while the smaller becomes the search target.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * // Create options for finding similar images
 * SimilarImagesFindOptions options = new SimilarImagesFindOptions.Builder()
 *     .setSimilarity(0.85) // Minimum similarity threshold
 *     .setComparisonMethod(ComparisonMethod.BEST_MATCH)
 *     .build();
 * 
 * // First collection contains base images
 * ObjectCollection baseImages = new ObjectCollection.Builder()
 *     .withImages(knownScreens)
 *     .build();
 * 
 * // Second collection contains images to compare
 * ObjectCollection compareImages = new ObjectCollection.Builder()
 *     .withImages(capturedScreens)
 *     .build();
 * 
 * // Perform comparison
 * ActionResult result = action.perform(options, baseImages, compareImages);
 * // Result contains one Match per image in compareImages
 * }
 * </pre>
 *
 * @see BaseFindOptions
 * @see FindSimilarImages
 * @see io.github.jspinak.brobot.analysis.compare.ImageComparer
 * @since 2.0
 */
@Getter
@JsonDeserialize(builder = SimilarImagesFindOptions.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SimilarImagesFindOptions extends BaseFindOptions {

    /**
     * Defines how images are compared when multiple patterns exist.
     */
    public enum ComparisonMethod {
        /**
         * Returns the single best match across all pattern combinations.
         * This is the default and most common method.
         */
        BEST_MATCH,
        
        /**
         * Returns an average similarity score across all pattern combinations.
         * Useful when consistency across multiple patterns is important.
         */
        AVERAGE_SCORE,
        
        /**
         * Returns matches only if all patterns meet the similarity threshold.
         * Most restrictive method, ensuring high confidence matches.
         */
        ALL_PATTERNS_MATCH,
        
        /**
         * Returns matches if any pattern meets the similarity threshold.
         * Most permissive method, useful for finding partial matches.
         */
        ANY_PATTERN_MATCHES
    }

    private final ComparisonMethod comparisonMethod;
    private final boolean includeNoMatches;
    private final boolean returnAllScores;

    private SimilarImagesFindOptions(Builder builder) {
        super(builder);
        this.comparisonMethod = builder.comparisonMethod;
        this.includeNoMatches = builder.includeNoMatches;
        this.returnAllScores = builder.returnAllScores;
    }

    @Override
    public FindStrategy getFindStrategy() {
        return FindStrategy.SIMILAR_IMAGES;
    }
    
    /**
     * Creates a configuration optimized for screen state recognition.
     * <p>
     * This preset uses high similarity thresholds and strict comparison to
     * accurately identify which known screen state is currently displayed.
     * </p>
     * 
     * @return SimilarImagesFindOptions configured for screen recognition
     */
    public static SimilarImagesFindOptions forScreenRecognition() {
        return new Builder()
            .setSimilarity(0.9)
            .setComparisonMethod(ComparisonMethod.BEST_MATCH)
            .setIncludeNoMatches(false)
            .build();
    }
    
    /**
     * Creates a configuration optimized for finding duplicate images.
     * <p>
     * This preset uses very high similarity thresholds to identify
     * images that are nearly identical.
     * </p>
     * 
     * @return SimilarImagesFindOptions configured for duplicate detection
     */
    public static SimilarImagesFindOptions forDuplicateDetection() {
        return new Builder()
            .setSimilarity(0.95)
            .setComparisonMethod(ComparisonMethod.ALL_PATTERNS_MATCH)
            .setIncludeNoMatches(true)
            .build();
    }
    
    /**
     * Creates a configuration optimized for change detection.
     * <p>
     * This preset uses lower similarity thresholds and includes all results
     * to identify which images have changed between collections.
     * </p>
     * 
     * @return SimilarImagesFindOptions configured for change detection
     */
    public static SimilarImagesFindOptions forChangeDetection() {
        return new Builder()
            .setSimilarity(0.7)
            .setComparisonMethod(ComparisonMethod.ANY_PATTERN_MATCHES)
            .setIncludeNoMatches(true)
            .setReturnAllScores(true)
            .build();
    }

    /**
     * Builder for constructing {@link SimilarImagesFindOptions} with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder extends BaseFindOptions.Builder<Builder> {

        @JsonProperty("comparisonMethod")
        private ComparisonMethod comparisonMethod = ComparisonMethod.BEST_MATCH;
        
        @JsonProperty("includeNoMatches")
        private boolean includeNoMatches = false;
        
        @JsonProperty("returnAllScores")
        private boolean returnAllScores = false;

        /**
         * Default constructor for creating a new SimilarImagesFindOptions configuration.
         */
        @JsonCreator
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * SimilarImagesFindOptions object, allowing for easy modification or templating.
         *
         * @param original The SimilarImagesFindOptions instance to copy.
         */
        public Builder(SimilarImagesFindOptions original) {
            super(original);
            this.comparisonMethod = original.comparisonMethod;
            this.includeNoMatches = original.includeNoMatches;
            this.returnAllScores = original.returnAllScores;
        }

        /**
         * Sets the comparison method for matching patterns.
         * 
         * @param comparisonMethod The method to use for comparing patterns
         * @return this Builder instance for chaining
         */
        public Builder setComparisonMethod(ComparisonMethod comparisonMethod) {
            this.comparisonMethod = comparisonMethod;
            return self();
        }

        /**
         * Sets whether to include images with no matches in the results.
         * <p>
         * When true, images from the second collection that don't meet the
         * similarity threshold will still be included in the results as
         * EmptyMatch objects. This is useful for identifying which images
         * couldn't be matched.
         * </p>
         * 
         * @param includeNoMatches true to include non-matching images
         * @return this Builder instance for chaining
         */
        public Builder setIncludeNoMatches(boolean includeNoMatches) {
            this.includeNoMatches = includeNoMatches;
            return self();
        }

        /**
         * Sets whether to return similarity scores for all pattern combinations.
         * <p>
         * When true, the Match objects will include detailed information about
         * all pattern-to-pattern comparison scores, not just the best one.
         * This is useful for analysis and debugging.
         * </p>
         * 
         * @param returnAllScores true to return all comparison scores
         * @return this Builder instance for chaining
         */
        public Builder setReturnAllScores(boolean returnAllScores) {
            this.returnAllScores = returnAllScores;
            return self();
        }

        /**
         * Builds the immutable {@link SimilarImagesFindOptions} object.
         *
         * @return A new instance of SimilarImagesFindOptions.
         */
        public SimilarImagesFindOptions build() {
            return new SimilarImagesFindOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}