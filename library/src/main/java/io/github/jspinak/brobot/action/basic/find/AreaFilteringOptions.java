package io.github.jspinak.brobot.action.basic.find;

import lombok.Getter;

/**
 * Configuration for filtering action results based on their pixel area.
 * <p>
 * This class encapsulates options for constraining matches by their minimum and maximum size.
 * It is primarily used by Find operations that do not have an inherent size, such as
 * color and motion detection, to filter out noise or irrelevant results.
 * <p>
 * It is an immutable object and should be constructed using its inner {@link Builder}.
 * This object is designed to be composed within other, more specific `Options` classes
 * like {@code ColorFindOptions} or {@code MotionFindOptions}.
 */
@Getter
public final class AreaFilteringOptions {

    private final int minArea;
    private final int maxArea;

    private AreaFilteringOptions(Builder builder) {
        this.minArea = builder.minArea;
        this.maxArea = builder.maxArea;
    }

    /**
     * Builder for constructing {@link AreaFilteringOptions} with a fluent API.
     */
    public static class Builder {

        private int minArea = 1;
        private int maxArea = -1; // A value <= 0 typically disables the max area check.

        /**
         * Default constructor for creating a new AreaFilteringOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * AreaFilteringOptions object.
         *
         * @param original The AreaFilteringOptions instance to copy.
         */
        public Builder(AreaFilteringOptions original) {
            if (original != null) {
                this.minArea = original.minArea;
                this.maxArea = original.maxArea;
            }
        }

        /**
         * Sets the minimum number of pixels for a match to be considered valid.
         * Used to filter out small, noisy results.
         *
         * @param minArea The minimum area in pixels.
         * @return this Builder instance for chaining.
         */
        public Builder setMinArea(int minArea) {
            this.minArea = minArea;
            return this;
        }

        /**
         * Sets the maximum number of pixels for a match to be considered valid.
         * Used to filter out overly large or unintended results. A value less than
         * or equal to 0 typically disables this check.
         *
         * @param maxArea The maximum area in pixels.
         * @return this Builder instance for chaining.
         */
        public Builder setMaxArea(int maxArea) {
            this.maxArea = maxArea;
            return this;
        }

        /**
         * Builds the immutable {@link AreaFilteringOptions} object.
         *
         * @return A new instance of AreaFilteringOptions.
         */
        public AreaFilteringOptions build() {
            return new AreaFilteringOptions(this);
        }
    }
}
