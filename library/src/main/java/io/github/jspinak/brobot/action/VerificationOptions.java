package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Getter;

/**
 * Configuration for action verification and termination conditions.
 * <p>
 * This class defines the criteria for repeatedly executing an action until a specific
 * condition, such as the appearance of text or an image, is met. It allows any
 * action to be converted into a "wait-like" operation.
 * <p>
 * It is an immutable object designed to be composed within other {@code Options} classes
 * and should be constructed using its inner {@link Builder}.
 */
@Getter
public final class VerificationOptions {

    /**
     * Defines the type of condition that will terminate the action.
     */
    public enum Condition {
        /** The action terminates when the specified condition is met. Default behavior. */
        CONTINUE_UNTIL_CONDITION_MET,
        /** The action terminates if the condition is met at any point. */
        TERMINATE_ON_CONDITION
    }

    /**
     * Defines the event that satisfies the termination condition.
     */
    public enum Event {
        /** No verification is performed; the action runs once. */
        NONE,
        /** The condition is met when target objects become visible. */
        OBJECTS_APPEAR,
        /** The condition is met when target objects disappear. */
        OBJECTS_VANISH,
        /** The condition is met when target text is found. */
        TEXT_APPEARS,
        /** The condition is met when target text is no longer found. */
        TEXT_VANISHES
    }

    private final Condition condition;
    private final Event event;
    private final String text;
    private final ObjectCollection objectCollection; // For OBJECTS_APPEAR/VANISH

    private VerificationOptions(Builder builder) {
        this.condition = builder.condition;
        this.event = builder.event;
        this.text = builder.text;
        this.objectCollection = builder.objectCollection;
    }

    /**
     * Builder for constructing {@link VerificationOptions} with a fluent API.
     */
    public static class Builder {
        private Condition condition = Condition.CONTINUE_UNTIL_CONDITION_MET;
        private Event event = Event.NONE;
        private String text = "";
        private ObjectCollection objectCollection = new ObjectCollection.Builder().build();

        public Builder(VerificationOptions original) {
            this.condition = original.condition;
            this.event = original.event;
            this.text = original.text;
            this.objectCollection = original.objectCollection;
        }

        public Builder() {}

        public Builder setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public Builder setEvent(Event event) {
            this.event = event;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setObjectCollection(ObjectCollection objectCollection) {
            this.objectCollection = objectCollection;
            return this;
        }

        public VerificationOptions build() {
            return new VerificationOptions(this);
        }
    }
}