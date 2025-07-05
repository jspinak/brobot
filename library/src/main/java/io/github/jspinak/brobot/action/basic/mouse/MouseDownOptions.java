package io.github.jspinak.brobot.action.basic.mouse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;

/**
 * Configuration for MouseDown actions.
 * <p>
 * This class extends ActionConfig and composes MousePressOptions to provide
 * settings for mouse button press operations. Following the composition pattern,
 * it reuses MousePressOptions but only utilizes the relevant timing parameters
 * (pauseBeforeMouseDown and pauseAfterMouseDown).
 * <p>
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 *
 * @see ActionConfig
 * @see MousePressOptions
 * @see io.github.jspinak.brobot.action.basic.mouse.MouseDown
 */
@Getter
@JsonDeserialize(builder = MouseDownOptions.Builder.class)
public final class MouseDownOptions extends ActionConfig {

    private final MousePressOptions mousePressOptions;

    private MouseDownOptions(Builder builder) {
        super(builder);
        this.mousePressOptions = builder.mousePressOptions.build();
    }
    
    /**
     * Convenience getter for the mouse button.
     * @return The mouse button to press
     */
    public io.github.jspinak.brobot.model.action.MouseButton getButton() {
        return mousePressOptions.getButton();
    }
    
    /**
     * Convenience getter for pause before mouse down.
     * @return The pause duration before pressing the button
     */
    public double getPauseBeforeMouseDown() {
        return mousePressOptions.getPauseBeforeMouseDown();
    }
    
    /**
     * Convenience getter for pause after mouse down.
     * @return The pause duration after pressing the button
     */
    public double getPauseAfterMouseDown() {
        return mousePressOptions.getPauseAfterMouseDown();
    }

    /**
     * Builder for constructing MouseDownOptions with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("mousePressOptions")
        private MousePressOptions.Builder mousePressOptions = new MousePressOptions.Builder();

        /**
         * Default constructor for creating a new MouseDownOptions configuration.
         */
        @JsonCreator
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * MouseDownOptions object.
         *
         * @param original The MouseDownOptions instance to copy.
         */
        public Builder(MouseDownOptions original) {
            super(original);
            if (original != null) {
                this.mousePressOptions = new MousePressOptions.Builder(original.mousePressOptions);
            }
        }

        /**
         * Configures the mouse press options.
         * Only the button, pauseBeforeMouseDown, and pauseAfterMouseDown from
         * MousePressOptions are relevant for this action.
         *
         * @param pressOptionsBuilder A builder for MousePressOptions
         * @return this Builder instance for chaining
         */
        public Builder setPressOptions(MousePressOptions.Builder pressOptionsBuilder) {
            this.mousePressOptions = pressOptionsBuilder;
            return self();
        }

        /**
         * Builds the immutable MouseDownOptions object.
         *
         * @return A new instance of MouseDownOptions
         */
        public MouseDownOptions build() {
            return new MouseDownOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}