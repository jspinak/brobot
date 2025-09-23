package io.github.jspinak.brobot.action.basic.find;

import org.sikuli.basics.Settings;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.model.element.SearchRegions;

import lombok.Getter;

/**
 * Base configuration for all Find actions in the Brobot framework.
 *
 * <p>This abstract class encapsulates common parameters shared by all find operations, regardless
 * of whether they use pattern matching, color analysis, or other techniques. It extends {@link
 * ActionConfig} to inherit general action configuration while adding find-specific settings.
 *
 * <p>Specialized find configurations (e.g., {@link PatternFindOptions}, {@link
 * io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions}) should extend this class to
 * add their specific parameters while inheriting the common find functionality.
 *
 * <p>This design promotes code reuse and ensures consistency across different find implementations
 * while maintaining type safety and API clarity.
 *
 * @see ActionConfig
 * @see PatternFindOptions
 * @see io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions
 */
@Getter
public abstract class BaseFindOptions extends ActionConfig {

    private final double similarity;
    private final SearchRegions searchRegions;
    private final boolean captureImage;
    private final boolean useDefinedRegion;
    private final int maxMatchesToActOn;
    private final MatchAdjustmentOptions matchAdjustmentOptions;
    private final double searchDuration;

    /**
     * Protected constructor to be called by the builders of subclasses.
     *
     * @param builder The builder instance containing the configuration values.
     */
    protected BaseFindOptions(Builder<?> builder) {
        super(builder);
        this.similarity = builder.similarity;
        this.searchRegions = builder.searchRegions;
        this.captureImage = builder.captureImage;
        this.useDefinedRegion = builder.useDefinedRegion;
        this.maxMatchesToActOn = builder.maxMatchesToActOn;
        this.matchAdjustmentOptions = builder.matchAdjustmentOptions;
        this.searchDuration = builder.searchDuration;
    }

    /**
     * Gets the find strategy for this options instance.
     *
     * <p>Subclasses should override this method to return their specific strategy. For example,
     * PatternFindOptions would map its Strategy enum to FindStrategy, while ColorFindOptions would
     * return FindStrategy.COLOR.
     *
     * @return The find strategy to use for this find operation
     */
    public abstract FindStrategy getFindStrategy();

    /**
     * Abstract generic Builder for constructing BaseFindOptions and its subclasses. This pattern
     * allows for fluent, inheritable builder methods.
     *
     * @param <B> The type of the concrete builder subclass.
     */
    public abstract static class Builder<B extends Builder<B>> extends ActionConfig.Builder<B> {

        private double similarity = Settings.MinSimilarity;
        private SearchRegions searchRegions = new SearchRegions();
        private boolean captureImage = true;
        private boolean useDefinedRegion = false;
        private int maxMatchesToActOn = -1;
        private MatchAdjustmentOptions matchAdjustmentOptions =
                MatchAdjustmentOptions.builder().build();
        private double searchDuration = 3.0; // Default 3 seconds, same as SikuliX default

        /** Default constructor for the builder. */
        public Builder() {}

        /**
         * Copy constructor to initialize a builder from an existing BaseFindOptions instance.
         *
         * @param original The BaseFindOptions instance to copy values from.
         */
        public Builder(BaseFindOptions original) {
            super(original);
            this.similarity = original.similarity;
            this.searchRegions = new SearchRegions(original.searchRegions);
            this.captureImage = original.captureImage;
            this.useDefinedRegion = original.useDefinedRegion;
            this.maxMatchesToActOn = original.maxMatchesToActOn;
            this.matchAdjustmentOptions = original.matchAdjustmentOptions.toBuilder().build();
            this.searchDuration = original.searchDuration;
        }

        /**
         * Sets the minimum similarity score (0.0 to 1.0) for a match to be considered valid. This
         * threshold determines how closely a found element must match the search pattern. Lower
         * values allow for more variation but may produce false positives.
         *
         * @param similarity The minimum similarity threshold.
         * @return this Builder instance for chaining.
         */
        public B setSimilarity(double similarity) {
            this.similarity = similarity;
            return self();
        }

        /**
         * Sets the regions of the screen to search within. By default, the entire screen is
         * searched. This can be restricted to improve performance and accuracy by limiting the
         * search area.
         *
         * @param searchRegions The regions to search within.
         * @return this Builder instance for chaining.
         */
        public B setSearchRegions(SearchRegions searchRegions) {
            this.searchRegions = searchRegions;
            return self();
        }

        /**
         * Sets whether to capture an image of the match for logging and debugging. Captured images
         * can be useful for troubleshooting but may impact performance.
         *
         * @param captureImage true to capture match images, false otherwise.
         * @return this Builder instance for chaining.
         */
        public B setCaptureImage(boolean captureImage) {
            this.captureImage = captureImage;
            return self();
        }

        /**
         * If true, bypasses image search and creates Match objects directly from pre-defined
         * regions in the StateImage objects. This is useful when the location of elements is known
         * in advance.
         *
         * @param useDefinedRegion true to use defined regions instead of searching.
         * @return this Builder instance for chaining.
         */
        public B setUseDefinedRegion(boolean useDefinedRegion) {
            this.useDefinedRegion = useDefinedRegion;
            return self();
        }

        /**
         * Limits the number of matches to act on when using strategies that find multiple matches.
         * A value &lt;= 0 means no limit.
         *
         * @param maxMatchesToActOn The maximum number of matches to process.
         * @return this Builder instance for chaining.
         */
        public B setMaxMatchesToActOn(int maxMatchesToActOn) {
            this.maxMatchesToActOn = maxMatchesToActOn;
            return self();
        }

        /**
         * Sets the match adjustment options for post-processing found matches. This allows for
         * resizing match regions or targeting specific points within matches.
         *
         * @param matchAdjustmentOptions The match adjustment options.
         * @return this Builder instance for chaining.
         */
        public B setMatchAdjustment(MatchAdjustmentOptions matchAdjustmentOptions) {
            this.matchAdjustmentOptions = matchAdjustmentOptions;
            return self();
        }

        /**
         * Sets the search duration (in seconds) for finding a match. The search will continue until
         * a match is found or this duration is reached. This replaces the deprecated
         * ActionConfig.maxWait parameter.
         *
         * @param seconds The maximum duration to search for a match (default: 3.0 seconds)
         * @return this Builder instance for chaining.
         */
        public B setSearchDuration(double seconds) {
            this.searchDuration = seconds;
            return self();
        }
    }
}
