package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Configuration for action verification and termination conditions.
 * <p>
 * This class defines the criteria for repeatedly executing an action until a specific
 * condition, such as the appearance of text or an image, is met. It allows any
 * action to be converted into a "wait-like" operation.
 * <p>
 * It is an immutable object designed to be composed within other {@code Options} classes.
 */
@Getter
@Builder(toBuilder = true, builderClassName = "VerificationOptionsBuilder", setterPrefix = "set")
@JsonDeserialize(builder = VerificationOptions.VerificationOptionsBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
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

    @Builder.Default
    private final Condition condition = Condition.CONTINUE_UNTIL_CONDITION_MET;
    
    @Builder.Default
    private final Event event = Event.NONE;
    
    @Builder.Default
    private final String text = "";
    
    @Builder.Default
    private final ObjectCollection objectCollection = new ObjectCollection.Builder().build(); // For OBJECTS_APPEAR/VANISH

    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerificationOptionsBuilder {
        // Lombok generates the implementation
    }
}