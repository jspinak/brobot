package io.github.jspinak.brobot.action.basic.find;

import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for all standard pattern-matching Find actions.
 * <p>
 * This class encapsulates parameters specific to finding objects via image or text 
 * pattern matching. It extends {@link BaseFindOptions} to inherit common find 
 * functionality while adding pattern-specific settings.
 * <p>
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 * <p>
 * By providing a specialized configuration class, the Brobot API ensures that only 
 * relevant options are available for pattern matching, enhancing type safety and ease of use.
 *
 * @see BaseFindOptions
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
@Getter
@JsonDeserialize(builder = PatternFindOptions.Builder.class)
public final class PatternFindOptions extends BaseFindOptions {

    /**
     * The pattern matching strategy.
     */
    public enum Strategy {
        /**
         * Returns the first match found. Stops searching once any Pattern
         * finds a match, making it efficient for existence checks.
         */
        FIRST,
        /**
         * Returns all matches for all Patterns across all Images. Useful for
         * counting or processing multiple instances of an element.
         */
        ALL,
        /**
         * Returns one match per Image object. The {@link DoOnEach} option
         * determines whether to return the first or best match per Image.
         */
        EACH,
        /**
         * Performs an ALL search then returns only the match with the
         * highest similarity score.
         */
        BEST
    }

    /**
     * Controls match selection strategy when using {@link Strategy#EACH}.
     */
    public enum DoOnEach {
        /**
         * Returns the first match found for each Image (fastest).
         */
        FIRST,
        /**
         * Returns the match with the highest similarity for each Image.
         */
        BEST
    }


    private final Strategy strategy;
    private final DoOnEach doOnEach;
    private final MatchFusionOptions matchFusionOptions;

    private PatternFindOptions(Builder builder) {
        super(builder);
        this.strategy = builder.strategy;
        this.doOnEach = builder.doOnEach;
        this.matchFusionOptions = builder.matchFusionOptions;
    }

    @Override
    public FindStrategy getFindStrategy() {
        switch (strategy) {
            case FIRST:
                return FindStrategy.FIRST;
            case ALL:
                return FindStrategy.ALL;
            case EACH:
                return FindStrategy.EACH;
            case BEST:
                return FindStrategy.BEST;
            default:
                return FindStrategy.FIRST;
        }
    }
    
    /**
     * Creates a configuration optimized for quick pattern matching.
     * 
     * <p>This factory method provides a preset configuration for scenarios where
     * speed is more important than precision. It uses:
     * <ul>
     *   <li>FIRST strategy (stops after finding one match)</li>
     *   <li>Lower similarity threshold (0.7)</li>
     *   <li>Disabled image capture for performance</li>
     * </ul>
     * </p>
     * 
     * @return A PatternFindOptions configured for quick searches
     * @see io.github.jspinak.brobot.action.basic.find.presets.QuickFindOptions
     */
    public static PatternFindOptions forQuickSearch() {
        return new Builder()
            .setStrategy(Strategy.FIRST)
            .setSimilarity(0.7)
            .setCaptureImage(false)
            .setMaxMatchesToActOn(1)
            .build();
    }
    
    /**
     * Creates a configuration optimized for precise pattern matching.
     * 
     * <p>This factory method provides a preset configuration for scenarios where
     * accuracy is more important than speed. It uses:
     * <ul>
     *   <li>BEST strategy (finds all matches and returns highest scoring)</li>
     *   <li>High similarity threshold (0.9)</li>
     *   <li>Enabled image capture for debugging</li>
     *   <li>Conservative match fusion settings</li>
     * </ul>
     * </p>
     * 
     * @return A PatternFindOptions configured for precise searches
     * @see io.github.jspinak.brobot.action.basic.find.presets.PreciseFindOptions
     */
    public static PatternFindOptions forPreciseSearch() {
        return new Builder()
            .setStrategy(Strategy.BEST)
            .setSimilarity(0.9)
            .setCaptureImage(true)
            .setMatchFusion(MatchFusionOptions.builder()
                .fusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .maxFusionDistanceX(10)
                .maxFusionDistanceY(10)
                .build())
            .build();
    }
    
    /**
     * Creates a configuration for finding all occurrences of a pattern.
     * 
     * <p>This factory method provides a preset configuration for scenarios where
     * you need to find multiple instances of an element. It uses:
     * <ul>
     *   <li>ALL strategy (finds all matches)</li>
     *   <li>Balanced similarity threshold (0.8)</li>
     *   <li>Match fusion to combine adjacent matches</li>
     *   <li>No limit on match count</li>
     * </ul>
     * </p>
     * 
     * @return A PatternFindOptions configured for finding all matches
     * @see io.github.jspinak.brobot.action.basic.find.presets.AllMatchesFindOptions
     */
    public static PatternFindOptions forAllMatches() {
        return new Builder()
            .setStrategy(Strategy.ALL)
            .setSimilarity(0.8)
            .setCaptureImage(false)
            .setMaxMatchesToActOn(-1)
            .setMatchFusion(MatchFusionOptions.builder()
                .fusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .maxFusionDistanceX(20)
                .maxFusionDistanceY(20)
                .build())
            .build();
    }

    /**
     * Builder for constructing {@link PatternFindOptions} with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends BaseFindOptions.Builder<Builder> {

        @JsonProperty("strategy")
        private Strategy strategy = Strategy.FIRST;
        @JsonProperty("doOnEach")
        private DoOnEach doOnEach = DoOnEach.FIRST;
        @JsonProperty("matchFusionOptions")
        private MatchFusionOptions matchFusionOptions = MatchFusionOptions.builder().build();

        /**
         * Default constructor for creating a new PatternFindOptions configuration.
         */
        @JsonCreator
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * PatternFindOptions object, allowing for easy modification or templating.
         *
         * @param original The PatternFindOptions instance to copy.
         */
        public Builder(PatternFindOptions original) {
            super(original);
            this.strategy = original.strategy;
            this.doOnEach = original.doOnEach;
            this.matchFusionOptions = original.matchFusionOptions.toBuilder().build();
        }

        /**
         * Sets the pattern matching strategy.
         * @param strategy The strategy to use (e.g., FIRST, ALL).
         * @return this Builder instance for chaining.
         */
        public Builder setStrategy(Strategy strategy) {
            this.strategy = strategy;
            return self();
        }


        /**
         * Sets the strategy for selecting one match per image when using Find.EACH.
         * @param doOnEach The strategy to use (e.g., FIRST, BEST).
         * @return this Builder instance for chaining.
         */
        public Builder setDoOnEach(DoOnEach doOnEach) {
            this.doOnEach = doOnEach;
            return self();
        }


        /**
         * Sets the match fusion options for combining adjacent matches.
         * @param matchFusionBuilder A builder for MatchFusionOptions.
         * @return this Builder instance for chaining.
         */
        public Builder setMatchFusion(MatchFusionOptions matchFusionOptions) {
            this.matchFusionOptions = matchFusionOptions;
            return self();
        }

        /**
         * Builds the immutable {@link PatternFindOptions} object.
         *
         * @return A new instance of PatternFindOptions.
         */
        public PatternFindOptions build() {
            return new PatternFindOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}