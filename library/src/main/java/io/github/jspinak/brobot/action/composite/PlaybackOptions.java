package io.github.jspinak.brobot.action.composite;

import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;

/**
 * Configuration for recording and playback actions.
 * <p>
 * This class encapsulates all parameters for controlling the playback of a recorded
 * automation sequence, including the start time and duration.
 * <p>
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 *
 * @see ActionConfig
 */
@Getter
public final class PlaybackOptions extends ActionConfig {

    private final double startPlayback;
    private final double playbackDuration;

    private PlaybackOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.startPlayback = builder.startPlayback;
        this.playbackDuration = builder.playbackDuration;
    }

    /**
     * Builder for constructing {@link PlaybackOptions} with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {

        private double startPlayback = -1;
        private double playbackDuration = 5.0;

        /**
         * Default constructor for creating a new PlaybackOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * PlaybackOptions object.
         *
         * @param original The PlaybackOptions instance to copy.
         */
        public Builder(PlaybackOptions original) {
            super(original); // Call parent copy logic
            this.startPlayback = original.startPlayback;
            this.playbackDuration = original.playbackDuration;
        }

        /**
         * Sets the start point in the recording, in seconds, to begin the playback.
         * <p>
         * A value of -1 (the default) indicates that the start point should be
         * determined dynamically, for example, by matching the current screen
         * against scenes in the recording.
         *
         * @param startPlayback The start time in seconds, or -1 for dynamic start.
         * @return this Builder instance for chaining.
         */
        public Builder setStartPlayback(double startPlayback) {
            this.startPlayback = startPlayback;
            return self();
        }

        /**
         * Sets the total duration of the playback sequence, in seconds.
         *
         * @param playbackDuration The duration in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setPlaybackDuration(double playbackDuration) {
            this.playbackDuration = playbackDuration;
            return self();
        }

        /**
         * Builds the immutable {@link PlaybackOptions} object.
         *
         * @return A new instance of PlaybackOptions.
         */
        public PlaybackOptions build() {
            return new PlaybackOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
