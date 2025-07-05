package io.github.jspinak.brobot.action.basic.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the KeyDown action.
 * <p>
 * This class encapsulates all parameters for pressing and holding keyboard keys
 * without releasing them. It is an immutable object and must be constructed using
 * its inner {@link Builder}.
 * </p>
 * <p>
 * By providing a specialized configuration class, the Brobot API ensures that only
 * relevant options are available for key press operations, enhancing type safety
 * and ease of use.
 * </p>
 *
 * @see ActionConfig
 * @see io.github.jspinak.brobot.action.basic.type.KeyDown
 * @see io.github.jspinak.brobot.action.basic.type.KeyUpOptions
 */
@Getter
@JsonDeserialize(builder = KeyDownOptions.Builder.class)
public final class KeyDownOptions extends ActionConfig {

    private final List<String> modifiers;
    private final double pauseBetweenKeys;

    private KeyDownOptions(Builder builder) {
        super(builder);
        this.modifiers = new ArrayList<>(builder.modifiers);
        this.pauseBetweenKeys = builder.pauseBetweenKeys;
    }

    /**
     * Builder for constructing {@link KeyDownOptions} with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("modifiers")
        private List<String> modifiers = new ArrayList<>();
        @JsonProperty("pauseBetweenKeys")
        private double pauseBetweenKeys = 0.0;

        /**
         * Default constructor for creating a new KeyDownOptions configuration.
         */
        @JsonCreator
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * KeyDownOptions object, allowing for easy modification or templating.
         *
         * @param original The KeyDownOptions instance to copy.
         */
        public Builder(KeyDownOptions original) {
            super(original);
            this.modifiers = new ArrayList<>(original.modifiers);
            this.pauseBetweenKeys = original.pauseBetweenKeys;
        }

        /**
         * Sets the modifier keys to be held down during the key press.
         * Common modifiers include "CTRL", "SHIFT", "ALT", "META" (Windows key).
         *
         * @param modifiers The list of modifier keys.
         * @return this Builder instance for chaining.
         */
        public Builder setModifiers(List<String> modifiers) {
            this.modifiers = modifiers != null ? new ArrayList<>(modifiers) : new ArrayList<>();
            return self();
        }

        /**
         * Adds a single modifier key to the list of modifiers.
         *
         * @param modifier The modifier key to add.
         * @return this Builder instance for chaining.
         */
        public Builder addModifier(String modifier) {
            if (modifier != null) {
                this.modifiers.add(modifier);
            }
            return self();
        }

        /**
         * Sets the pause duration between individual key presses when pressing
         * multiple keys in sequence.
         *
         * @param pauseBetweenKeys The pause duration in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setPauseBetweenKeys(double pauseBetweenKeys) {
            this.pauseBetweenKeys = pauseBetweenKeys;
            return self();
        }

        /**
         * Builds the immutable {@link KeyDownOptions} object.
         *
         * @return A new instance of KeyDownOptions.
         */
        public KeyDownOptions build() {
            return new KeyDownOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}