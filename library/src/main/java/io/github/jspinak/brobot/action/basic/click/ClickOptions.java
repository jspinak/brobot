package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.VerificationOptions;
import io.github.jspinak.brobot.action.RepetitionOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import lombok.Getter;

/**
 * Configuration for all Click actions.
 * <p>
 * This class encapsulates all parameters for performing mouse clicks, including the
 * click type and any verification conditions that should terminate a repeating click.
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 * <p>
 * This specialized configuration enhances API clarity by only exposing options
 * relevant to click operations.
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * {@code
 * ClickOptions clickUntilTextAppears = new ClickOptions.Builder()
 * .setClickType(ClickOptions.Type.DOUBLE_LEFT)
 * .setVerification(new VerificationOptions.Builder()
 * .setEvent(VerificationOptions.Event.TEXT_APPEARS)
 * .setText("Success")
 * .build())
 * .build();
 * }
 * </pre>
 *
 * @see ActionConfig
 * @see VerificationOptions
 * @see io.github.jspinak.brobot.action.basic.click.Click
 */
@Getter
public final class ClickOptions extends ActionConfig {

    /**
     * Enumerates all supported mouse click types.
     * @deprecated Use MouseButton with numberOfClicks instead. This enum
     * violates Single Responsibility Principle by combining button selection
     * with click count.
     */
    @Deprecated
    public enum Type {
        /** A single click with the primary mouse button (typically the left button). */
        LEFT,
        /** A single click with the secondary mouse button (typically the right button). */
        RIGHT,
        /** A single click with the middle mouse button (often the scroll wheel). */
        MIDDLE,
        /** A double-click with the primary mouse button. */
        DOUBLE_LEFT,
        /** A double-click with the secondary mouse button. */
        DOUBLE_RIGHT,
        /** A double-click with the middle mouse button. */
        DOUBLE_MIDDLE
    }

    private final int numberOfClicks;
    private final MousePressOptions mousePressOptions; // Parameters for mouse button and timing
    private final VerificationOptions verificationOptions; // Conditions for repeating the click
    private final RepetitionOptions repetitionOptions; // Parameters for repetition and timing
    
    // Deprecated field for backward compatibility
    @Deprecated
    private final Type clickType;

    private ClickOptions(Builder builder) {
        super(builder); // Initialize common ActionConfig fields
        this.numberOfClicks = builder.numberOfClicks;
        this.mousePressOptions = builder.mousePressOptions.build();
        this.verificationOptions = builder.verificationOptions.build(); // Build the composed object
        this.repetitionOptions = builder.repetitionOptions.build();
        
        // For backward compatibility
        this.clickType = builder.clickType;
    }
    
    /**
     * Convenience getter for the number of times to repeat an action on an individual target.
     * @return The number of repetitions for an individual action
     */
    public int getTimesToRepeatIndividualAction() {
        return repetitionOptions.getTimesToRepeatIndividualAction();
    }
    
    /**
     * Convenience getter for the pause between individual actions.
     * @return The pause duration between individual actions in seconds
     */
    public double getPauseBetweenIndividualActions() {
        return repetitionOptions.getPauseBetweenIndividualActions();
    }

    /**
     * Builder for constructing {@link ClickOptions} with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {

        private int numberOfClicks = 1;
        private MousePressOptions.Builder mousePressOptions = new MousePressOptions.Builder(); // Default: LEFT button with default timings
        private VerificationOptions.Builder verificationOptions = new VerificationOptions.Builder(); // Default: no verification
        private RepetitionOptions.Builder repetitionOptions = new RepetitionOptions.Builder(); // Default: single repetition
        
        // Deprecated field for backward compatibility
        @Deprecated
        private Type clickType = Type.LEFT;

        /**
         * Default constructor for creating a new ClickOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * ClickOptions object, allowing for easy modification or templating.
         *
         * @param original The ClickOptions instance to copy.
         */
        public Builder(ClickOptions original) {
            super(original); // Call parent copy logic
            this.numberOfClicks = original.numberOfClicks;
            this.mousePressOptions = new MousePressOptions.Builder(original.mousePressOptions);
            this.verificationOptions = new VerificationOptions.Builder(original.verificationOptions);
            this.repetitionOptions = new RepetitionOptions.Builder(original.repetitionOptions);
            this.clickType = original.clickType; // For backward compatibility
        }

        /**
         * Sets the number of times to click. For example, 2 for a double-click.
         * @param numberOfClicks The number of clicks to perform.
         * @return this Builder instance for chaining.
         */
        public Builder setNumberOfClicks(int numberOfClicks) {
            this.numberOfClicks = Math.max(1, numberOfClicks); // Ensure at least 1 click
            return self();
        }
        
        /**
         * Sets the type of mouse click to perform.
         * @deprecated Use setNumberOfClicks() and setPressOptions() instead
         * @param clickType The click type (e.g., LEFT, DOUBLE_RIGHT).
         * @return this Builder instance for chaining.
         */
        @Deprecated
        public Builder setClickType(Type clickType) {
            this.clickType = clickType;
            // Also update numberOfClicks and button for compatibility
            switch (clickType) {
                case DOUBLE_LEFT:
                    this.numberOfClicks = 2;
                    this.mousePressOptions.setButton(io.github.jspinak.brobot.model.action.MouseButton.LEFT);
                    break;
                case DOUBLE_RIGHT:
                    this.numberOfClicks = 2;
                    this.mousePressOptions.setButton(io.github.jspinak.brobot.model.action.MouseButton.RIGHT);
                    break;
                case DOUBLE_MIDDLE:
                    this.numberOfClicks = 2;
                    this.mousePressOptions.setButton(io.github.jspinak.brobot.model.action.MouseButton.MIDDLE);
                    break;
                case RIGHT:
                    this.numberOfClicks = 1;
                    this.mousePressOptions.setButton(io.github.jspinak.brobot.model.action.MouseButton.RIGHT);
                    break;
                case MIDDLE:
                    this.numberOfClicks = 1;
                    this.mousePressOptions.setButton(io.github.jspinak.brobot.model.action.MouseButton.MIDDLE);
                    break;
                case LEFT:
                default:
                    this.numberOfClicks = 1;
                    this.mousePressOptions.setButton(io.github.jspinak.brobot.model.action.MouseButton.LEFT);
                    break;
            }
            return self();
        }

        /**
         * Configures the pause behaviors for the press-and-release part of the click.
         * @param pressOptionsBuilder A builder for MousePressOptions.
         * @return this Builder instance for chaining.
         */
        public Builder setPressOptions(MousePressOptions.Builder pressOptionsBuilder) {
            this.mousePressOptions = pressOptionsBuilder;
            return self();
        }

        /**
         * Sets the verification conditions that determine when this click action
         * should stop repeating.
         *
         * @param verificationOptionsBuilder The builder for the verification options.
         * @return this Builder instance for chaining.
         */
        public Builder setVerification(VerificationOptions.Builder verificationOptionsBuilder) {
            this.verificationOptions = verificationOptionsBuilder;
            return self();
        }
        
        /**
         * Sets the repetition options for controlling how clicks are repeated.
         *
         * @param repetitionOptionsBuilder The builder for the repetition options.
         * @return this Builder instance for chaining.
         */
        public Builder setRepetition(RepetitionOptions.Builder repetitionOptionsBuilder) {
            this.repetitionOptions = repetitionOptionsBuilder;
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