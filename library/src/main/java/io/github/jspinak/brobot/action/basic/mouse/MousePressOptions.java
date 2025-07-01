package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.config.FrameworkSettings;
import lombok.Getter;

/**
 * Configuration for mouse button press-and-release behaviors.
 * <p>
 * This class encapsulates all settings related to the physical mouse button
 * press and release events, including which button to press and timing parameters.
 * It is designed to be a reusable component, composed within higher-level action
 * configurations like {@code ClickOptions}, {@code DragOptions}, {@code MouseDownOptions},
 * and {@code MouseUpOptions}.
 * <p>
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 */
@Getter
public final class MousePressOptions {

    private final MouseButton button;
    private final double pauseBeforeMouseDown;
    private final double pauseAfterMouseDown;
    private final double pauseBeforeMouseUp;
    private final double pauseAfterMouseUp;

    private MousePressOptions(Builder builder) {
        this.button = builder.button;
        this.pauseBeforeMouseDown = builder.pauseBeforeMouseDown;
        this.pauseAfterMouseDown = builder.pauseAfterMouseDown;
        this.pauseBeforeMouseUp = builder.pauseBeforeMouseUp;
        this.pauseAfterMouseUp = builder.pauseAfterMouseUp;
    }

    /**
     * Builder for constructing {@link MousePressOptions} with a fluent API.
     */
    public static class Builder {

        private MouseButton button = MouseButton.LEFT;
        private double pauseBeforeMouseDown = FrameworkSettings.pauseBeforeMouseDown;
        private double pauseAfterMouseDown = FrameworkSettings.pauseAfterMouseDown;
        private double pauseBeforeMouseUp = FrameworkSettings.pauseBeforeMouseUp;
        private double pauseAfterMouseUp = FrameworkSettings.pauseAfterMouseUp;

        /**
         * Default constructor for creating a new MousePressOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * MousePressOptions object.
         *
         * @param original The MousePressOptions instance to copy.
         */
        public Builder(MousePressOptions original) {
            if (original != null) {
                this.button = original.button;
                this.pauseBeforeMouseDown = original.pauseBeforeMouseDown;
                this.pauseAfterMouseDown = original.pauseAfterMouseDown;
                this.pauseBeforeMouseUp = original.pauseBeforeMouseUp;
                this.pauseAfterMouseUp = original.pauseAfterMouseUp;
            }
        }

        /**
         * Sets which mouse button to use.
         * @param button The mouse button (LEFT, RIGHT, MIDDLE).
         * @return this Builder instance for chaining.
         */
        public Builder setButton(MouseButton button) {
            this.button = button;
            return this;
        }

        /**
         * Sets the delay in seconds before the mouse button is pressed down.
         * @param seconds The pause duration.
         * @return this Builder instance for chaining.
         */
        public Builder setPauseBeforeMouseDown(double seconds) {
            this.pauseBeforeMouseDown = seconds;
            return this;
        }

        /**
         * Sets the delay in seconds after the mouse button has been pressed down.
         * @param seconds The pause duration.
         * @return this Builder instance for chaining.
         */
        public Builder setPauseAfterMouseDown(double seconds) {
            this.pauseAfterMouseDown = seconds;
            return this;
        }

        /**
         * Sets the delay in seconds before the mouse button is released.
         * @param seconds The pause duration.
         * @return this Builder instance for chaining.
         */
        public Builder setPauseBeforeMouseUp(double seconds) {
            this.pauseBeforeMouseUp = seconds;
            return this;
        }

        /**
         * Sets the delay in seconds after the mouse button has been released.
         * @param seconds The pause duration.
         * @return this Builder instance for chaining.
         */
        public Builder setPauseAfterMouseUp(double seconds) {
            this.pauseAfterMouseUp = seconds;
            return this;
        }

        /**
         * Builds the immutable {@link MousePressOptions} object.
         * @return A new instance of MousePressOptions.
         */
        public MousePressOptions build() {
            return new MousePressOptions(this);
        }
    }
}
