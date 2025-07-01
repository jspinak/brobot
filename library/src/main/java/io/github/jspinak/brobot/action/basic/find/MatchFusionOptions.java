package io.github.jspinak.brobot.action.basic.find;

import lombok.Getter;

/**
 * Configuration for match fusion operations in Find actions.
 * <p>
 * This class encapsulates all parameters related to fusing multiple adjacent matches
 * into single larger matches. Match fusion is useful when searching for UI elements
 * that may be detected as multiple separate matches but logically represent a single
 * element (e.g., text that's broken across multiple lines).
 * <p>
 * It is an immutable object designed to be composed within Find options classes
 * and should be constructed using its inner {@link Builder}.
 * <p>
 * Match fusion works by:
 * <ol>
 *   <li>Finding all individual matches using the specified search strategy</li>
 *   <li>Analyzing spatial relationships between matches</li>
 *   <li>Merging matches that are within the specified distance thresholds</li>
 *   <li>Creating new composite matches that encompass the merged regions</li>
 * </ol>
 *
 * @see BaseFindOptions
 * @see PatternFindOptions
 */
@Getter
public final class MatchFusionOptions {

    /**
     * Defines the method for fusing multiple matches into a single larger match.
     */
    public enum FusionMethod {
        /** No fusion is performed. Matches remain separate. */
        NONE,
        /** Fuses matches based on absolute pixel distance. */
        ABSOLUTE,
        /** Fuses matches based on distance relative to their size. */
        RELATIVE
    }

    private final FusionMethod fusionMethod;
    private final int maxFusionDistanceX;
    private final int maxFusionDistanceY;
    private final int sceneToUseForCaptureAfterFusingMatches;

    private MatchFusionOptions(Builder builder) {
        this.fusionMethod = builder.fusionMethod;
        this.maxFusionDistanceX = builder.maxFusionDistanceX;
        this.maxFusionDistanceY = builder.maxFusionDistanceY;
        this.sceneToUseForCaptureAfterFusingMatches = builder.sceneToUseForCaptureAfterFusingMatches;
    }

    /**
     * Builder for constructing {@link MatchFusionOptions} with a fluent API.
     */
    public static class Builder {

        private FusionMethod fusionMethod = FusionMethod.NONE;
        private int maxFusionDistanceX = 5;
        private int maxFusionDistanceY = 5;
        private int sceneToUseForCaptureAfterFusingMatches = 0;

        /**
         * Default constructor for creating a new MatchFusionOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * MatchFusionOptions object.
         *
         * @param original The MatchFusionOptions instance to copy.
         */
        public Builder(MatchFusionOptions original) {
            if (original != null) {
                this.fusionMethod = original.fusionMethod;
                this.maxFusionDistanceX = original.maxFusionDistanceX;
                this.maxFusionDistanceY = original.maxFusionDistanceY;
                this.sceneToUseForCaptureAfterFusingMatches = original.sceneToUseForCaptureAfterFusingMatches;
            }
        }

        /**
         * Sets the method for fusing multiple adjacent matches into a single larger match.
         * @param fusionMethod The fusion method to use.
         * @return this Builder instance for chaining.
         */
        public Builder setFusionMethod(FusionMethod fusionMethod) {
            this.fusionMethod = fusionMethod;
            return this;
        }

        /**
         * Sets the maximum horizontal distance in pixels for matches to be considered for fusion.
         * Matches separated by more than this distance will not be fused.
         * @param maxDistance The maximum horizontal distance.
         * @return this Builder instance for chaining.
         */
        public Builder setMaxFusionDistanceX(int maxDistance) {
            this.maxFusionDistanceX = maxDistance;
            return this;
        }

        /**
         * Sets the maximum vertical distance in pixels for matches to be considered for fusion.
         * Matches separated by more than this distance will not be fused.
         * @param maxDistance The maximum vertical distance.
         * @return this Builder instance for chaining.
         */
        public Builder setMaxFusionDistanceY(int maxDistance) {
            this.maxFusionDistanceY = maxDistance;
            return this;
        }

        /**
         * When fusing matches from multiple scenes, determines which scene's Mat and text 
         * to use for the new fused match. This is relevant when searching across multiple
         * screenshots or scenes.
         * @param sceneIndex The index of the scene to use (0-based).
         * @return this Builder instance for chaining.
         */
        public Builder setSceneToUseForCaptureAfterFusingMatches(int sceneIndex) {
            this.sceneToUseForCaptureAfterFusingMatches = sceneIndex;
            return this;
        }

        /**
         * Builds the immutable {@link MatchFusionOptions} object.
         * @return A new instance of MatchFusionOptions.
         */
        public MatchFusionOptions build() {
            return new MatchFusionOptions(this);
        }
    }
}