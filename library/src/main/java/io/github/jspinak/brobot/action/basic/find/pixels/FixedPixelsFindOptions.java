package io.github.jspinak.brobot.action.basic.find.pixels;

import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import lombok.Getter;

/**
 * Configuration for finding fixed (static) pixels within GUI scenes.
 * <p>
 * This class encapsulates parameters for detecting pixels that remain unchanged
 * over time, useful for identifying stable UI elements, static backgrounds, or
 * persistent screen regions. It is an immutable object constructed using its 
 * inner {@link Builder}.
 * <p>
 * Fixed pixel detection helps identify areas of the screen that are not changing,
 * which can be useful for:
 * <ul>
 *   <li>Detecting when animations have stopped</li>
 *   <li>Identifying stable UI regions for interaction</li>
 *   <li>Waiting for screen stabilization</li>
 *   <li>Finding static reference points</li>
 * </ul>
 * 
 * Fluent API Usage:
 * <pre>
 * {@code
 * FixedPixelsFindOptions options = new FixedPixelsFindOptions.Builder()
 *     .setMaxMovement(2)
 *     .setStartPlayback(0.5)
 *     .setPlaybackDuration(1.5)
 *     .build();
 * }
 * </pre>
 * @see BaseFindOptions
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
@Getter
public final class FixedPixelsFindOptions extends BaseFindOptions {

    /**
     * The maximum pixel movement threshold to consider a pixel as fixed.
     * Pixels that move less than this amount between frames are classified as fixed.
     */
    private final int maxMovement;
    
    /**
     * The time in seconds to wait before starting to monitor for fixed pixels.
     * This allows for initial screen activity to settle.
     */
    private final double startPlayback;
    
    /**
     * The duration in seconds to monitor for fixed pixels.
     * Longer durations provide more confidence that pixels are truly static.
     */
    private final double playbackDuration;

    private FixedPixelsFindOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.maxMovement = builder.maxMovement;
        this.startPlayback = builder.startPlayback;
        this.playbackDuration = builder.playbackDuration;
    }

    @Override
    public FindStrategy getFindStrategy() {
        return FindStrategy.FIXED_PIXELS;
    }

    /**
     * Builder for constructing {@link FixedPixelsFindOptions} with a fluent API.
     */
    public static class Builder extends BaseFindOptions.Builder<Builder> {

        private int maxMovement = 2;
        private double startPlayback = 0.0;
        private double playbackDuration = 1.0;

        /**
         * Default constructor for creating a new FixedPixelsFindOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * FixedPixelsFindOptions object, allowing for easy modification or templating.
         *
         * @param original The FixedPixelsFindOptions instance to copy.
         */
        public Builder(FixedPixelsFindOptions original) {
            super(original); // Call parent copy logic
            this.maxMovement = original.maxMovement;
            this.startPlayback = original.startPlayback;
            this.playbackDuration = original.playbackDuration;
        }

        /**
         * Sets the maximum pixel movement threshold for fixed classification.
         * @param maxMovement The maximum movement in pixels.
         * @return this Builder instance for chaining.
         */
        public Builder setMaxMovement(int maxMovement) {
            this.maxMovement = maxMovement;
            return self();
        }
        
        /**
         * Sets the delay before starting to monitor fixed pixels.
         * @param startPlayback The delay in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setStartPlayback(double startPlayback) {
            this.startPlayback = startPlayback;
            return self();
        }
        
        /**
         * Sets the duration to monitor for fixed pixels.
         * @param playbackDuration The monitoring duration in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setPlaybackDuration(double playbackDuration) {
            this.playbackDuration = playbackDuration;
            return self();
        }
        
        /**
         * Builds the immutable {@link FixedPixelsFindOptions} object.
         *
         * @return A new instance of FixedPixelsFindOptions.
         */
        public FixedPixelsFindOptions build() {
            return new FixedPixelsFindOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}