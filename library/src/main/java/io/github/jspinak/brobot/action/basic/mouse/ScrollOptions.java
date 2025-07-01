package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;

/**
 * Configuration for mouse wheel scrolling actions.
 * <p>
 * This class encapsulates all parameters for scrolling the mouse wheel,
 * including direction and number of scroll steps. It is an immutable object
 * and must be constructed using its inner {@link Builder}.
 * </p>
 * <p>
 * By providing a specialized configuration class, the Brobot API ensures that only
 * relevant options are available for scroll operations, enhancing type safety
 * and ease of use.
 * </p>
 *
 * @see ActionConfig
 * @see io.github.jspinak.brobot.action.basic.mouse.ScrollMouseWheel
 */
@Getter
public final class ScrollOptions extends ActionConfig {

    /**
     * Defines the direction of mouse wheel scrolling.
     */
    public enum Direction {
        /**
         * Scroll upward (toward the top of the page/content).
         */
        UP,
        
        /**
         * Scroll downward (toward the bottom of the page/content).
         */
        DOWN
    }

    private final Direction direction;
    private final int scrollSteps;

    private ScrollOptions(Builder builder) {
        super(builder);
        this.direction = builder.direction;
        this.scrollSteps = builder.scrollSteps;
    }

    /**
     * Builder for constructing {@link ScrollOptions} with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {

        private Direction direction = Direction.DOWN;
        private int scrollSteps = 3;

        /**
         * Default constructor for creating a new ScrollOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * ScrollOptions object, allowing for easy modification or templating.
         *
         * @param original The ScrollOptions instance to copy.
         */
        public Builder(ScrollOptions original) {
            super(original);
            this.direction = original.direction;
            this.scrollSteps = original.scrollSteps;
        }

        /**
         * Sets the scroll direction.
         *
         * @param direction The direction to scroll (UP or DOWN).
         * @return this Builder instance for chaining.
         */
        public Builder setDirection(Direction direction) {
            this.direction = direction;
            return self();
        }

        /**
         * Sets the number of scroll steps (or "clicks" of the wheel).
         * Each step represents one notch of the mouse wheel.
         *
         * @param scrollSteps The number of scroll steps. Must be positive.
         * @return this Builder instance for chaining.
         */
        public Builder setScrollSteps(int scrollSteps) {
            this.scrollSteps = Math.max(1, scrollSteps);
            return self();
        }

        /**
         * Builds the immutable {@link ScrollOptions} object.
         *
         * @return A new instance of ScrollOptions.
         */
        public ScrollOptions build() {
            return new ScrollOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}