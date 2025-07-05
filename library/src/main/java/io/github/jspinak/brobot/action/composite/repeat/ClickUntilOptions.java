package io.github.jspinak.brobot.action.composite.repeat;

import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for the composite action ClickUntil.
 * <p>
 * This class encapsulates the termination condition for a ClickUntil operation,
 * which repeatedly performs a click action until a specified condition involving
 * the appearance or disappearance of other objects is met.
 * <p>
 * It is an immutable object and must be constructed using its inner {@link Builder}.
 * This specialized configuration enhances API clarity by only exposing options
 * relevant to the ClickUntil action.
 *
 * @deprecated Use {@link RepeatUntilConfig} instead. RepeatUntilConfig provides
 *             more flexibility by allowing different action types and separate
 *             configuration for the repeated action and termination condition.
 * @see RepeatUntilConfig
 * @see ActionConfig
 * @see io.github.jspinak.brobot.action.composite.repeat.ClickUntil
 */
@Deprecated
@Getter
@JsonDeserialize(builder = ClickUntilOptions.Builder.class)
public final class ClickUntilOptions extends ActionConfig {

    /**
     * Defines the termination condition for a {@code CLICK_UNTIL} operation.
     * This controls when the repeated clicking should stop.
     */
    public enum Condition {
        /**
         * The action will continue until the target objects in the second
         * ObjectCollection become visible on the screen.
         */
        OBJECTS_APPEAR,
        /**
         * The action will continue until the target objects disappear from the screen.
         * If one ObjectCollection is provided, it clicks those objects until they vanish.
         * If two are provided, it clicks objects in the first collection until objects
         * in the second collection vanish.
         */
        OBJECTS_VANISH
    }

    private final Condition condition;

    private ClickUntilOptions(Builder builder) {
        super(builder); // Initialize fields from the base ActionConfig
        this.condition = builder.condition;
    }

    /**
     * Builder for constructing {@link ClickUntilOptions} with a fluent API.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("condition")
        private Condition condition = Condition.OBJECTS_APPEAR;

        /**
         * Default constructor for creating a new ClickUntilOptions configuration.
         */
        @JsonCreator
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * ClickUntilOptions object, allowing for easy modification or templating.
         *
         * @param original The ClickUntilOptions instance to copy.
         */
        public Builder(ClickUntilOptions original) {
            super(original); // Call parent copy logic
            this.condition = original.condition;
        }

        /**
         * Sets the termination condition for the ClickUntil action.
         *
         * @param condition The condition that will stop the action (e.g., OBJECTS_APPEAR).
         * @return this Builder instance for chaining.
         */
        public Builder setCondition(Condition condition) {
            this.condition = condition;
            return self();
        }

        /**
         * Builds the immutable {@link ClickUntilOptions} object.
         *
         * @return A new instance of ClickUntilOptions.
         */
        public ClickUntilOptions build() {
            return new ClickUntilOptions(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
