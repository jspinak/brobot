package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for Move Mouse actions.
 * <p>
 * This class encapsulates parameters for controlling the mouse movement speed.
 * It is an immutable object and must be constructed using its inner
 * {@link Builder}.
 * <p>
 * This specialized configuration enhances API clarity by only exposing options
 * relevant to mouse movement.
 *
 * @see ActionConfig
 * @see io.github.jspinak.brobot.action.basic.mouse.MoveMouse
 */
@Getter
@JsonDeserialize(builder = MouseMoveOptions.Builder.class)
public final class MouseMoveOptions extends ActionConfig {

    private final float moveMouseDelay;

    private MouseMoveOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.moveMouseDelay = builder.moveMouseDelay;
    }

    /**
     * Builder for constructing {@link MouseMoveOptions} with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("moveMouseDelay")
        private float moveMouseDelay = FrameworkSettings.moveMouseDelay;

        /**
         * Default constructor for creating a new MouseMoveOptions configuration.
         */
        @JsonCreator
        public Builder() {
        }

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * MouseMoveOptions object.
         *
         * @param original The MouseMoveOptions instance to copy.
         */
        public Builder(MouseMoveOptions original) {
            super(original); // Call parent copy logic
            this.moveMouseDelay = original.moveMouseDelay;
        }

        /**
         * Sets the delay for each step of the mouse movement, effectively controlling
         * the speed of the mouse. A higher value results in a slower mouse movement.
         * The value is in seconds.
         *
         * @param moveMouseDelay The delay in seconds for each mouse movement step.
         * @return this Builder instance for chaining.
         */
        public Builder setMoveMouseDelay(float moveMouseDelay) {
            this.moveMouseDelay = moveMouseDelay;
            return self();
        }

        /**
         * Builds the immutable {@link MouseMoveOptions} object.
         *
         * @return A new instance of MouseMoveOptions.
         */
        public MouseMoveOptions build() {
            return new MouseMoveOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
