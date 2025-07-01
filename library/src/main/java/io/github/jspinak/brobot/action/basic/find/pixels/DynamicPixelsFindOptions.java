package io.github.jspinak.brobot.action.basic.find.pixels;

import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import lombok.Getter;

/**
 * Configuration for finding dynamic pixels within GUI scenes.
 * <p>
 * This class encapsulates parameters for detecting pixels that change over time,
 * useful for identifying animated elements, loading indicators, or other dynamic
 * UI components. It is an immutable object constructed using its inner {@link Builder}.
 * <p>
 * Dynamic pixel detection helps identify areas of the screen that are actively
 * changing, which can be crucial for:
 * <ul>
 *   <li>Waiting for animations to complete</li>
 *   <li>Detecting progress indicators</li>
 *   <li>Identifying active UI elements</li>
 *   <li>Monitoring screen updates</li>
 * </ul>
 * 
 * Fluent API Usage:
 * <pre>
 * {@code
 * DynamicPixelsFindOptions options = new DynamicPixelsFindOptions.Builder()
 *     .setMaxMovement(10)
 *     .setStartPlayback(0.5)
 *     .setPlaybackDuration(2.0)
 *     .build();
 * }
 * </pre>
 * @see BaseFindOptions
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
@Getter
public final class DynamicPixelsFindOptions extends BaseFindOptions {

    /**
     * The maximum pixel movement threshold to consider a pixel as dynamic.
     * Pixels that move more than this amount between frames are classified as dynamic.
     */
    private final int maxMovement;
    
    /**
     * The time in seconds to wait before starting to monitor for dynamic pixels.
     * This allows for initial screen stabilization.
     */
    private final double startPlayback;
    
    /**
     * The duration in seconds to monitor for dynamic pixels.
     * Longer durations provide more accurate detection but take more time.
     */
    private final double playbackDuration;

    private DynamicPixelsFindOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.maxMovement = builder.maxMovement;
        this.startPlayback = builder.startPlayback;
        this.playbackDuration = builder.playbackDuration;
    }

    @Override
    public FindStrategy getFindStrategy() {
        return FindStrategy.DYNAMIC_PIXELS;
    }

    /**
     * Builder for constructing {@link DynamicPixelsFindOptions} with a fluent API.
     */
    public static class Builder extends BaseFindOptions.Builder<Builder> {

        private int maxMovement = 5;
        private double startPlayback = 0.0;
        private double playbackDuration = 1.0;

        /**
         * Default constructor for creating a new DynamicPixelsFindOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * DynamicPixelsFindOptions object, allowing for easy modification or templating.
         *
         * @param original The DynamicPixelsFindOptions instance to copy.
         */
        public Builder(DynamicPixelsFindOptions original) {
            super(original); // Call parent copy logic
            this.maxMovement = original.maxMovement;
            this.startPlayback = original.startPlayback;
            this.playbackDuration = original.playbackDuration;
        }

        /**
         * Sets the maximum pixel movement threshold.
         * @param maxMovement The maximum movement in pixels.
         * @return this Builder instance for chaining.
         */
        public Builder setMaxMovement(int maxMovement) {
            this.maxMovement = maxMovement;
            return self();
        }
        
        /**
         * Sets the delay before starting to monitor dynamic pixels.
         * @param startPlayback The delay in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setStartPlayback(double startPlayback) {
            this.startPlayback = startPlayback;
            return self();
        }
        
        /**
         * Sets the duration to monitor for dynamic pixels.
         * @param playbackDuration The monitoring duration in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setPlaybackDuration(double playbackDuration) {
            this.playbackDuration = playbackDuration;
            return self();
        }
        
        /**
         * Builds the immutable {@link DynamicPixelsFindOptions} object.
         *
         * @return A new instance of DynamicPixelsFindOptions.
         */
        public DynamicPixelsFindOptions build() {
            return new DynamicPixelsFindOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}