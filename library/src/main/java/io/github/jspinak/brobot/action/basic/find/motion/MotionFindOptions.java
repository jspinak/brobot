package io.github.jspinak.brobot.action.basic.find.motion;

import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import lombok.Getter;

/**
 * Configuration for motion-based Find actions.
 * <p>
 * This class encapsulates parameters for finding objects by detecting pixel changes
 * between consecutive scenes. It is an immutable object and must be constructed
 * using its inner {@link Builder}.
 * <p>
 * This specialized configuration enhances API clarity by only exposing options
 * relevant to motion detection operations.
 * <p>
 * By extending {@link BaseFindOptions}, it inherits common find functionality including
 * match adjustment support, while adding motion-specific settings.
 *
 * @see BaseFindOptions
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
@Getter
public final class MotionFindOptions extends BaseFindOptions {

    private final int maxMovement;

    private MotionFindOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.maxMovement = builder.maxMovement;
    }

    @Override
    public FindStrategy getFindStrategy() {
        return FindStrategy.MOTION;
    }

    /**
     * Builder for constructing {@link MotionFindOptions} with a fluent API.
     */
    public static class Builder extends BaseFindOptions.Builder<Builder> {

        private int maxMovement = 300;

        /**
         * Default constructor for creating a new MotionFindOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * MotionFindOptions object.
         *
         * @param original The MotionFindOptions instance to copy.
         */
        public Builder(MotionFindOptions original) {
            super(original); // Call parent copy logic
            this.maxMovement = original.maxMovement;
        }

        /**
         * Sets the maximum distance, in pixels, that a moving object is expected
         * to travel between two consecutive frames (scenes). This helps the motion
         * detection algorithm correctly identify and track the same object across frames.
         *
         * @param maxMovement The maximum distance an object can move.
         * @return this Builder instance for chaining.
         */
        public Builder setMaxMovement(int maxMovement) {
            this.maxMovement = maxMovement;
            return self();
        }

        /**
         * Builds the immutable {@link MotionFindOptions} object.
         *
         * @return A new instance of MotionFindOptions.
         */
        public MotionFindOptions build() {
            return new MotionFindOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
