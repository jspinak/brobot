package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import lombok.Getter;

/**
 * Configuration for adjusting the position and dimensions of found matches.
 * <p>
 * This class encapsulates all parameters for post-processing the region of a {@link io.github.jspinak.brobot.model.match.Match}.
 * It allows for dynamic resizing or targeting of specific points within a match, providing
 * flexibility for subsequent actions like clicks or drags.
 * <p>
 * It is an immutable object designed to be composed within other {@code Options} classes
 * and should be constructed using its inner {@link Builder}.
 */
@Getter
public final class MatchAdjustmentOptions {

    private final Position targetPosition;
    private final Location targetOffset;
    private final int addW;
    private final int addH;
    private final int absoluteW;
    private final int absoluteH;
    private final int addX;
    private final int addY;

    private MatchAdjustmentOptions(Builder builder) {
        this.targetPosition = builder.targetPosition;
        this.targetOffset = builder.targetOffset;
        this.addW = builder.addW;
        this.addH = builder.addH;
        this.absoluteW = builder.absoluteW;
        this.absoluteH = builder.absoluteH;
        this.addX = builder.addX;
        this.addY = builder.addY;
    }

    /**
     * Builder for constructing {@link MatchAdjustmentOptions} with a fluent API.
     */
    public static class Builder {

        private Position targetPosition;
        private Location targetOffset;
        private int addW = 0;
        private int addH = 0;
        private int absoluteW = -1;
        private int absoluteH = -1;
        private int addX = 0;
        private int addY = 0;

        /**
         * Default constructor for creating a new MatchAdjustmentOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * MatchAdjustmentOptions object.
         *
         * @param original The MatchAdjustmentOptions instance to copy.
         */
        public Builder(MatchAdjustmentOptions original) {
            if (original != null) {
                this.targetPosition = original.targetPosition;
                this.targetOffset = original.targetOffset;
                this.addW = original.addW;
                this.addH = original.addH;
                this.absoluteW = original.absoluteW;
                this.absoluteH = original.absoluteH;
                this.addX = original.addX;
                this.addY = original.addY;
            }
        }

        /**
         * Sets a target position within a match's bounds (e.g., CENTER, TOP_LEFT).
         * This overrides any default position defined in the search pattern.
         *
         * @param targetPosition The relative position within the match.
         * @return this Builder instance for chaining.
         */
        public Builder setTargetPosition(Position targetPosition) {
            this.targetPosition = targetPosition;
            return this;
        }

        /**
         * Sets a pixel offset from the calculated target position.
         * Useful for interacting near, but not directly on, an element.
         *
         * @param targetOffset The x,y offset from the target position.
         * @return this Builder instance for chaining.
         */
        public Builder setTargetOffset(Location targetOffset) {
            this.targetOffset = targetOffset;
            return this;
        }

        /**
         * Adds a specified number of pixels to the width of the match region.
         * @param pixelsToAdd The number of pixels to add to the width.
         * @return this Builder instance for chaining.
         */
        public Builder setAddW(int pixelsToAdd) {
            this.addW = pixelsToAdd;
            return this;
        }

        /**
         * Adds a specified number of pixels to the height of the match region.
         * @param pixelsToAdd The number of pixels to add to the height.
         * @return this Builder instance for chaining.
         */
        public Builder setAddH(int pixelsToAdd) {
            this.addH = pixelsToAdd;
            return this;
        }

        /**
         * Sets the absolute width of the match region, overriding its original width.
         * A value less than 0 disables this setting.
         *
         * @param width The new absolute width of the region.
         * @return this Builder instance for chaining.
         */
        public Builder setAbsoluteW(int width) {
            this.absoluteW = width;
            return this;
        }

        /**
         * Sets the absolute height of the match region, overriding its original height.
         * A value less than 0 disables this setting.
         *
         * @param height The new absolute height of the region.
         * @return this Builder instance for chaining.
         */
        public Builder setAbsoluteH(int height) {
            this.absoluteH = height;
            return this;
        }

        /**
         * Adds a specified number of pixels to the x-coordinate of the match region's origin.
         * @param pixelsToAdd The number of pixels to add to the x-coordinate.
         * @return this Builder instance for chaining.
         */
        public Builder setAddX(int pixelsToAdd) {
            this.addX = pixelsToAdd;
            return this;
        }

        /**
         * Adds a specified number of pixels to the y-coordinate of the match region's origin.
         * @param pixelsToAdd The number of pixels to add to the y-coordinate.
         * @return this Builder instance for chaining.
         */
        public Builder setAddY(int pixelsToAdd) {
            this.addY = pixelsToAdd;
            return this;
        }

        /**
         * Builds the immutable {@link MatchAdjustmentOptions} object.
         * @return A new instance of MatchAdjustmentOptions.
         */
        public MatchAdjustmentOptions build() {
            return new MatchAdjustmentOptions(this);
        }
    }
}