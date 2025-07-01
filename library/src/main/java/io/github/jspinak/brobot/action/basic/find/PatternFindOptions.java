package io.github.jspinak.brobot.action.basic.find;

import lombok.Getter;

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
        this.matchFusionOptions = builder.matchFusionOptions.build();
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
     * Builder for constructing {@link PatternFindOptions} with a fluent API.
     */
    public static class Builder extends BaseFindOptions.Builder<Builder> {

        private Strategy strategy = Strategy.FIRST;
        private DoOnEach doOnEach = DoOnEach.FIRST;
        private MatchFusionOptions.Builder matchFusionOptions = new MatchFusionOptions.Builder();

        /**
         * Default constructor for creating a new PatternFindOptions configuration.
         */
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
            this.matchFusionOptions = new MatchFusionOptions.Builder(original.matchFusionOptions);
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
        public Builder setMatchFusion(MatchFusionOptions.Builder matchFusionBuilder) {
            this.matchFusionOptions = matchFusionBuilder;
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