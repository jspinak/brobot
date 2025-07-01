package io.github.jspinak.brobot.action.basic.region;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import lombok.Getter;

/**
 * Configuration for Define actions, which capture screen regions.
 * <p>
 * This class encapsulates all parameters for defining a new region on the screen based
 * on various strategies, such as using anchors, existing matches, or the focused window.
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 * <p>
 * By providing a specialized configuration class, the Brobot API ensures that only relevant
 * options are available for region definition, enhancing type safety and ease of use.
 *
 * @see ActionConfig
 * @see io.github.jspinak.brobot.action.basic.capture.DefineRegion
 */
@Getter
public final class DefineRegionOptions extends ActionConfig {

    /**
     * Defines the strategy for how a new region should be captured or calculated.
     */
    public enum DefineAs {
        /**
         * Defines the region as the smallest rectangle that encloses all found anchors.
         */
        INSIDE_ANCHORS,
        /**
         * Defines the region as the largest rectangle formed by the outermost anchors.
         */
        OUTSIDE_ANCHORS,
        /**
         * Defines the region directly from the bounds of a single match.
         */
        MATCH,
        /**
         * Defines a region immediately below a specified match.
         */
        BELOW_MATCH,
        /**
         * Defines a region immediately above a specified match.
         */
        ABOVE_MATCH,
        /**
         * Defines a region immediately to the left of a specified match.
         */
        LEFT_OF_MATCH,
        /**
         * Defines a region immediately to the right of a specified match.
         */
        RIGHT_OF_MATCH,
        /**
         * Defines the region using the boundaries of the currently focused window.
         */
        FOCUSED_WINDOW,
        /**
         * Defines the region as the smallest rectangle that encloses multiple specified matches.
         */
        INCLUDING_MATCHES
    }

    private final DefineAs defineAs;
    private final MatchAdjustmentOptions matchAdjustmentOptions;

    /**
     * Gets the match adjustment options for this define region configuration.
     * 
     * @return The match adjustment options, or null if not set.
     */
    public MatchAdjustmentOptions getMatchAdjustmentOptions() {
        return matchAdjustmentOptions;
    }

    private DefineRegionOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.defineAs = builder.defineAs;
        this.matchAdjustmentOptions = builder.matchAdjustmentOptions != null ? 
            builder.matchAdjustmentOptions.build() : null;
    }

    /**
     * Builder for constructing {@link DefineRegionOptions} with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {

        private DefineAs defineAs = DefineAs.MATCH;
        private MatchAdjustmentOptions.Builder matchAdjustmentOptions;

        /**
         * Default constructor for creating a new DefineRegionOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * DefineRegionOptions object, allowing for easy modification or templating.
         *
         * @param original The DefineRegionOptions instance to copy.
         */
        public Builder(DefineRegionOptions original) {
            super(original); // Call parent copy logic
            this.defineAs = original.defineAs;
            if (original.matchAdjustmentOptions != null) {
                this.matchAdjustmentOptions = new MatchAdjustmentOptions.Builder(original.matchAdjustmentOptions);
            }
        }

        /**
         * Sets the strategy for how the region should be defined.
         * @param defineAs The definition strategy to use (e.g., INSIDE_ANCHORS, FOCUSED_WINDOW).
         * @return this Builder instance for chaining.
         */
        public Builder setDefineAs(DefineAs defineAs) {
            this.defineAs = defineAs;
            return self();
        }

        /**
         * Sets the match adjustment options for post-processing matched regions.
         * This allows for resizing or repositioning the defined region based on matches.
         *
         * @param matchAdjustmentBuilder A builder for MatchAdjustmentOptions.
         * @return this Builder instance for chaining.
         */
        public Builder setMatchAdjustment(MatchAdjustmentOptions.Builder matchAdjustmentBuilder) {
            this.matchAdjustmentOptions = matchAdjustmentBuilder;
            return self();
        }

        /**
         * Builds the immutable {@link DefineRegionOptions} object.
         *
         * @return A new instance of DefineRegionOptions.
         */
        public DefineRegionOptions build() {
            return new DefineRegionOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
