package io.github.jspinak.brobot.action.basic.click;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.action.VerificationOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;

import lombok.Getter;

/**
 * Configuration for all Click actions.
 *
 * <p>This class encapsulates all parameters for performing mouse clicks, including the click type
 * and any verification conditions that should terminate a repeating click. It is an immutable
 * object and must be constructed using its inner {@link Builder}.
 *
 * <p>This specialized configuration enhances API clarity by only exposing options relevant to click
 * operations.
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * ClickOptions clickUntilTextAppears = new ClickOptions.Builder()
 * .setNumberOfClicks(2)
 * .setPressOptions(MousePressOptions.builder()
 *     .button(MouseButton.LEFT)
 *     .build())
 * .setVerification(new VerificationOptions.Builder()
 * .setEvent(VerificationOptions.Event.TEXT_APPEARS)
 * .setText("Success")
 * .build())
 * .build();
 * }</pre>
 *
 * @see ActionConfig
 * @see VerificationOptions
 * @see io.github.jspinak.brobot.action.basic.click.Click
 */
@Getter
@JsonDeserialize(builder = ClickOptions.Builder.class)
public final class ClickOptions extends ActionConfig {

    private final int numberOfClicks;
    private final MousePressOptions mousePressOptions; // Parameters for mouse button and timing
    private final VerificationOptions verificationOptions; // Conditions for repeating the click
    private final RepetitionOptions repetitionOptions; // Parameters for repetition and timing

    private ClickOptions(Builder builder) {
        super(builder); // Initialize common ActionConfig fields
        this.numberOfClicks = builder.numberOfClicks;
        this.mousePressOptions = builder.mousePressOptions;
        this.verificationOptions = builder.verificationOptions; // Already built in the builder
        this.repetitionOptions = builder.repetitionOptions; // Already built in the builder
    }

    /**
     * Convenience getter for the number of times to repeat an action on an individual target.
     *
     * @return The number of repetitions for an individual action
     */
    public int getTimesToRepeatIndividualAction() {
        return repetitionOptions.getTimesToRepeatIndividualAction();
    }

    /**
     * Convenience getter for the pause between individual actions.
     *
     * @return The pause duration between individual actions in seconds
     */
    public double getPauseBetweenIndividualActions() {
        return repetitionOptions.getPauseBetweenIndividualActions();
    }

    /** Builder for constructing {@link ClickOptions} with a fluent API. */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("numberOfClicks")
        private int numberOfClicks = 1;

        @JsonProperty("mousePressOptions")
        private MousePressOptions mousePressOptions =
                MousePressOptions.builder().build(); // Default: LEFT button with default timings

        @JsonProperty("verificationOptions")
        private VerificationOptions verificationOptions =
                VerificationOptions.builder().build(); // Default: no verification

        @JsonProperty("repetitionOptions")
        private RepetitionOptions repetitionOptions =
                RepetitionOptions.builder().build(); // Default: single repetition

        /** Default constructor for creating a new ClickOptions configuration. */
        @JsonCreator
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing ClickOptions
         * object, allowing for easy modification or templating.
         *
         * @param original The ClickOptions instance to copy.
         */
        public Builder(ClickOptions original) {
            super(original); // Call parent copy logic
            this.numberOfClicks = original.numberOfClicks;
            this.mousePressOptions = original.mousePressOptions.toBuilder().build();
            this.verificationOptions = original.verificationOptions.toBuilder().build();
            this.repetitionOptions = original.repetitionOptions.toBuilder().build();
        }

        /**
         * Sets the number of times to click. For example, 2 for a double-click.
         *
         * @param numberOfClicks The number of clicks to perform.
         * @return this Builder instance for chaining.
         */
        public Builder setNumberOfClicks(int numberOfClicks) {
            this.numberOfClicks = numberOfClicks;
            return self();
        }

        /**
         * Configures the pause behaviors for the press-and-release part of the click.
         *
         * @param pressOptions The mouse press options.
         * @return this Builder instance for chaining.
         */
        public Builder setPressOptions(MousePressOptions pressOptions) {
            this.mousePressOptions = pressOptions;
            return self();
        }

        /**
         * Sets the verification conditions that determine when this click action should stop
         * repeating.
         *
         * @param verificationOptions The verification options.
         * @return this Builder instance for chaining.
         */
        public Builder setVerification(VerificationOptions verificationOptions) {
            this.verificationOptions = verificationOptions;
            return self();
        }

        /**
         * Sets the repetition options for controlling how clicks are repeated.
         *
         * @param repetitionOptions The repetition options.
         * @return this Builder instance for chaining.
         */
        public Builder setRepetition(RepetitionOptions repetitionOptions) {
            this.repetitionOptions = repetitionOptions;
            return self();
        }

        /**
         * Builds the immutable {@link ClickOptions} object.
         *
         * @return A new instance of ClickOptions.
         */
        public ClickOptions build() {
            return new ClickOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
