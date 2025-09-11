package io.github.jspinak.brobot.action.composite;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

/**
 * Composite action configuration that combines Find and Click operations.
 *
 * <p>This composite action provides a convenient way to configure the common pattern of finding an
 * element and then clicking on it. It's designed for users who prefer the simplicity of the old
 * embedded-find approach.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * // Using default options
 * FindAndClick findAndClick = new FindAndClick();
 *
 * // Using custom options
 * FindAndClick findAndClick = new FindAndClick(
 *     new PatternFindOptions.Builder()
 *         .setSimilarity(0.9)
 *         .build(),
 *     new ClickOptions.Builder()
 *         .setNumberOfClicks(2)
 *         .build()
 * );
 * }</pre>
 *
 * @since 2.0
 */
public class FindAndClick extends ActionConfig {

    private final PatternFindOptions findOptions;
    private final ClickOptions clickOptions;

    /**
     * Creates a FindAndClick action with default options. Uses default PatternFindOptions and
     * ClickOptions.
     */
    public FindAndClick() {
        this(new PatternFindOptions.Builder().build(), new ClickOptions.Builder().build());
    }

    /**
     * Creates a FindAndClick action with custom find options and default click options.
     *
     * @param findOptions the find configuration to use
     */
    public FindAndClick(PatternFindOptions findOptions) {
        this(findOptions, new ClickOptions.Builder().build());
    }

    /**
     * Creates a FindAndClick action with custom options.
     *
     * @param findOptions the find configuration to use
     * @param clickOptions the click configuration to use
     */
    public FindAndClick(PatternFindOptions findOptions, ClickOptions clickOptions) {
        super(new Builder());
        this.findOptions = findOptions;
        this.clickOptions = clickOptions;
    }

    /**
     * Gets the find options used by this composite action.
     *
     * @return the PatternFindOptions
     */
    public PatternFindOptions getFindOptions() {
        return findOptions;
    }

    /**
     * Gets the click options used by this composite action.
     *
     * @return the ClickOptions
     */
    public ClickOptions getClickOptions() {
        return clickOptions;
    }

    /** Builder for creating FindAndClick instances with a fluent API. */
    public static class Builder extends ActionConfig.Builder<Builder> {
        private PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        private ClickOptions clickOptions = new ClickOptions.Builder().build();

        /**
         * Sets the find options for this composite action.
         *
         * @param findOptions the find configuration
         * @return this builder for chaining
         */
        public Builder withFindOptions(PatternFindOptions findOptions) {
            this.findOptions = findOptions;
            return self();
        }

        /**
         * Sets the click options for this composite action.
         *
         * @param clickOptions the click configuration
         * @return this builder for chaining
         */
        public Builder withClickOptions(ClickOptions clickOptions) {
            this.clickOptions = clickOptions;
            return self();
        }

        /**
         * Sets the similarity threshold for finding. Convenience method that modifies the find
         * options.
         *
         * @param similarity the minimum similarity score (0.0 to 1.0)
         * @return this builder for chaining
         */
        public Builder withSimilarity(double similarity) {
            this.findOptions =
                    new PatternFindOptions.Builder(findOptions).setSimilarity(similarity).build();
            return self();
        }

        /**
         * Sets the number of clicks. Convenience method that modifies the click options.
         *
         * @param numberOfClicks the number of clicks to perform
         * @return this builder for chaining
         */
        public Builder withNumberOfClicks(int numberOfClicks) {
            this.clickOptions =
                    new ClickOptions.Builder(clickOptions)
                            .setNumberOfClicks(numberOfClicks)
                            .build();
            return self();
        }

        public FindAndClick build() {
            return new FindAndClick(findOptions, clickOptions);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
