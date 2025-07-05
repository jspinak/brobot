package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.sikuli.basics.Settings;

/**
 * Configuration for Type actions, which send keyboard input.
 * <p>
 * This class encapsulates all parameters for performing a type action, including
 * the delay between keystrokes and any modifier keys (like SHIFT or CTRL) to be held
 * during the action.
 * <p>
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 *
 * @see ActionConfig
 * @see io.github.jspinak.brobot.action.basic.type.TypeText
 */
@Getter
@JsonDeserialize(builder = TypeOptions.Builder.class)
public final class TypeOptions extends ActionConfig {

    private final double typeDelay;
    private final String modifiers;

    private TypeOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.typeDelay = builder.typeDelay;
        this.modifiers = builder.modifiers;
    }

    /**
     * Builder for constructing {@link TypeOptions} with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("typeDelay")
        private double typeDelay = Settings.TypeDelay;
        @JsonProperty("modifiers")
        private String modifiers = "";

        /**
         * Default constructor for creating a new TypeOptions configuration.
         */
        @JsonCreator
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * TypeOptions object.
         *
         * @param original The TypeOptions instance to copy.
         */
        public Builder(TypeOptions original) {
            super(original); // Call parent copy logic
            this.typeDelay = original.typeDelay;
            this.modifiers = original.modifiers;
        }

        /**
         * Sets the delay, in seconds, between individual keystrokes.
         *
         * @param typeDelay The delay in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setTypeDelay(double typeDelay) {
            this.typeDelay = typeDelay;
            return self();
        }

        /**
         * Sets the modifier keys (e.g., "SHIFT", "CTRL", "ALT") to be held down
         * during the type action. Multiple keys can be combined with a "+".
         *
         * @param modifiers A string representing the modifier keys.
         * @return this Builder instance for chaining.
         */
        public Builder setModifiers(String modifiers) {
            this.modifiers = modifiers;
            return self();
        }

        /**
         * Builds the immutable {@link TypeOptions} object.
         *
         * @return A new instance of TypeOptions.
         */
        public TypeOptions build() {
            return new TypeOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}