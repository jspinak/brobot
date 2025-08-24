package io.github.jspinak.brobot.action.basic.highlight;

import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@JsonDeserialize(builder = HighlightOptions.Builder.class)
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
     * Alias for getHighlightSeconds to support legacy tests.
     * @return The duration in seconds.
     */
    public double getDuration() {
        return highlightSeconds;
    }
    
    /**
     * Alias for getHighlightColor to support legacy tests.
     * @return The color of the highlight.
     */
    public String getColor() {
        return highlightColor;
    }

    /**
     * Builder for constructing {@link HighlightOptions} with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("highlightAllAtOnce")
        private boolean highlightAllAtOnce = false;
        @JsonProperty("highlightSeconds")
        private double highlightSeconds = 1.0;
        @JsonProperty("highlightColor")
        private String highlightColor = "red";

        /**
         * Default constructor for creating a new HighlightOptions configuration.
         */
        @JsonCreator
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
         * Alias for setHighlightSeconds to support legacy tests.
         *
         * @param duration The duration in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setDuration(double duration) {
            return setHighlightSeconds(duration);
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
         * Alias for setHighlightColor to support legacy tests.
         *
         * @param color The color of the highlight.
         * @return this Builder instance for chaining.
         */
        public Builder setColor(String color) {
            return setHighlightColor(color);
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
