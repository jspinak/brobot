package io.github.jspinak.brobot.action.basic.mouse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;

/**
 * Configuration for MouseUp actions.
 * <p>
 * This class extends ActionConfig and composes MousePressOptions to provide
 * settings for mouse button release operations. Following the composition pattern,
 * it reuses MousePressOptions but only utilizes the relevant timing parameters
 * (pauseBeforeMouseUp and pauseAfterMouseUp).
 * <p>
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 *
 * @see ActionConfig
 * @see MousePressOptions
 * @see io.github.jspinak.brobot.action.basic.mouse.MouseUp
 */
@Getter
@JsonDeserialize(builder = MouseUpOptions.Builder.class)
public final class MouseUpOptions extends ActionConfig {

    private final MousePressOptions mousePressOptions;

    private MouseUpOptions(Builder builder) {
        super(builder);
        this.mousePressOptions = builder.mousePressOptions;
    }
    
    /**
     * Convenience getter for the mouse button.
     * @return The mouse button to release
     */
    public io.github.jspinak.brobot.model.action.MouseButton getButton() {
        return mousePressOptions.getButton();
    }
    
    /**
     * Convenience getter for pause before mouse up.
     * @return The pause duration before releasing the button
     */
    public double getPauseBeforeMouseUp() {
        return mousePressOptions.getPauseBeforeMouseUp();
    }
    
    /**
     * Convenience getter for pause after mouse up.
     * @return The pause duration after releasing the button
     */
    public double getPauseAfterMouseUp() {
        return mousePressOptions.getPauseAfterMouseUp();
    }

    /**
     * Builder for constructing MouseUpOptions with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("mousePressOptions")
        private MousePressOptions mousePressOptions = MousePressOptions.builder().build();

        /**
         * Default constructor for creating a new MouseUpOptions configuration.
         */
        @JsonCreator
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * MouseUpOptions object.
         *
         * @param original The MouseUpOptions instance to copy.
         */
        public Builder(MouseUpOptions original) {
            super(original);
            if (original != null) {
                this.mousePressOptions = original.mousePressOptions.toBuilder().build();
            }
        }

        /**
         * Configures the mouse press options.
         * Only the button, pauseBeforeMouseUp, and pauseAfterMouseUp from
         * MousePressOptions are relevant for this action.
         *
         * @param pressOptionsBuilder A builder for MousePressOptions
         * @return this Builder instance for chaining
         */
        public Builder setPressOptions(MousePressOptions pressOptions) {
            this.mousePressOptions = pressOptions;
            return self();
        }

        /**
         * Builds the immutable MouseUpOptions object.
         *
         * @return A new instance of MouseUpOptions
         */
        public MouseUpOptions build() {
            return new MouseUpOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}