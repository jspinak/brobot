package io.github.jspinak.brobot.action.basic.highlight;

import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;

/**
 * Configuration for Highlight actions.
 * <p>
 * This class encapsulates all parameters for highlighting regions on the screen,
 * including the duration, color, and whether to highlight all matches simultaneously.
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 * <p>
 * This specialized configuration enhances API clarity by only exposing options
 * relevant to highlighting operations.
 *
 * @see ActionConfig
 * @see io.github.jspinak.brobot.action.basic.highlight.Highlight
 */
@Getter
public final class HighlightOptions extends ActionConfig {

    private final boolean highlightAllAtOnce;
    private final double highlightSeconds;
    private final String highlightColor;

    private HighlightOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.highlightAllAtOnce = builder.highlightAllAtOnce;
        this.highlightSeconds = builder.highlightSeconds;
        this.highlightColor = builder.highlightColor;
    }

    /**
     * Builder for constructing {@link HighlightOptions} with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {

        private boolean highlightAllAtOnce = false;
        private double highlightSeconds = 1.0;
        private String highlightColor = "red";

        /**
         * Default constructor for creating a new HighlightOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * HighlightOptions object, allowing for easy modification or templating.
         *
         * @param original The HighlightOptions instance to copy.
         */
        public Builder(HighlightOptions original) {
            super(original); // Call parent copy logic
            this.highlightAllAtOnce = original.highlightAllAtOnce;
            this.highlightSeconds = original.highlightSeconds;
            this.highlightColor = original.highlightColor;
        }

        /**
         * If true, all found matches will be highlighted simultaneously. If false,
         * each match will be highlighted sequentially.
         *
         * @param highlightAllAtOnce true to highlight all at once.
         * @return this Builder instance for chaining.
         */
        public Builder setHighlightAllAtOnce(boolean highlightAllAtOnce) {
            this.highlightAllAtOnce = highlightAllAtOnce;
            return self();
        }

        /**
         * Sets the duration, in seconds, for which the highlight will be visible.
         *
         * @param highlightSeconds The duration in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setHighlightSeconds(double highlightSeconds) {
            this.highlightSeconds = highlightSeconds;
            return self();
        }

        /**
         * Sets the color of the highlight.
         * The color can be specified by name (e.g., "red", "blue") or as a hex code.
         * Refer to SikuliX documentation for supported color formats.
         *
         * @param highlightColor The color of the highlight.
         * @return this Builder instance for chaining.
         */
        public Builder setHighlightColor(String highlightColor) {
            this.highlightColor = highlightColor;
            return self();
        }

        /**
         * Builds the immutable {@link HighlightOptions} object.
         *
         * @return A new instance of HighlightOptions.
         */
        public HighlightOptions build() {
            return new HighlightOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
