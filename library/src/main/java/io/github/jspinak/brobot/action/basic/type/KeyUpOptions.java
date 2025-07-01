package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the KeyUp action.
 * <p>
 * This class encapsulates all parameters for releasing previously pressed keyboard keys.
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 * </p>
 * <p>
 * By providing a specialized configuration class, the Brobot API ensures that only
 * relevant options are available for key release operations, enhancing type safety
 * and ease of use.
 * </p>
 *
 * @see ActionConfig
 * @see io.github.jspinak.brobot.action.basic.type.KeyUp
 * @see io.github.jspinak.brobot.action.basic.type.KeyDownOptions
 */
@Getter
public final class KeyUpOptions extends ActionConfig {

    private final List<String> modifiers;

    private KeyUpOptions(Builder builder) {
        super(builder);
        this.modifiers = new ArrayList<>(builder.modifiers);
    }

    /**
     * Builder for constructing {@link KeyUpOptions} with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {

        private List<String> modifiers = new ArrayList<>();

        /**
         * Default constructor for creating a new KeyUpOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * KeyUpOptions object, allowing for easy modification or templating.
         *
         * @param original The KeyUpOptions instance to copy.
         */
        public Builder(KeyUpOptions original) {
            super(original);
            this.modifiers = new ArrayList<>(original.modifiers);
        }

        /**
         * Sets the modifier keys to be released.
         * These are typically released after all other keys to maintain proper
         * key combination semantics.
         *
         * @param modifiers The list of modifier keys to release.
         * @return this Builder instance for chaining.
         */
        public Builder setModifiers(List<String> modifiers) {
            this.modifiers = modifiers != null ? new ArrayList<>(modifiers) : new ArrayList<>();
            return self();
        }

        /**
         * Adds a single modifier key to the list of modifiers to release.
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
         * Builds the immutable {@link KeyUpOptions} object.
         *
         * @return A new instance of KeyUpOptions.
         */
        public KeyUpOptions build() {
            return new KeyUpOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}