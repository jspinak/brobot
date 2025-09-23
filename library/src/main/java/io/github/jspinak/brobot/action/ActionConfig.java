package io.github.jspinak.brobot.action;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;

/**
 * Abstract base class for all action configurations in the Brobot framework.
 *
 * <p>This class defines the common parameters that are applicable to any action, such as timing,
 * success criteria, and illustration settings. It uses a generic, inheritable Builder pattern to
 * ensure that all specialized configuration classes can provide a consistent and fluent API.
 *
 * <p>Specialized configuration classes (e.g., {@code ClickOptions}, {@code FindOptions}) must
 * extend this class.
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.click.ClickOptions.class,
            name = "ClickOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.find.PatternFindOptions.class,
            name = "PatternFindOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.composite.drag.DragOptions.class,
            name = "DragOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.region.DefineRegionOptions.class,
            name = "DefineRegionOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.highlight.HighlightOptions.class,
            name = "HighlightOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.type.TypeOptions.class,
            name = "TypeOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.mouse.ScrollOptions.class,
            name = "ScrollOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions.class,
            name = "MouseMoveOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions.class,
            name = "MouseDownOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions.class,
            name = "MouseUpOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.type.KeyDownOptions.class,
            name = "KeyDownOptions"),
    @JsonSubTypes.Type(
            value = io.github.jspinak.brobot.action.basic.type.KeyUpOptions.class,
            name = "KeyUpOptions")
})
public abstract class ActionConfig {

    /** Overrides the global illustration setting for this specific action. */
    public enum Illustrate {
        /** Always generate an illustration for this action. */
        YES,
        /** Never generate an illustration for this action. */
        NO,
        /** Use the global framework setting to decide. This is the default. */
        USE_GLOBAL
    }

    private final double pauseBeforeBegin;
    private final double pauseAfterEnd;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private final Predicate<ActionResult> successCriteria;

    private final Illustrate illustrate;
    private final List<ActionConfig> subsequentActions;

    // Logging fields
    private final String beforeActionLog;
    private final String afterActionLog;
    private final String successLog;
    private final String failureLog;

    /**
     * Protected constructor to be called by the builders of subclasses.
     *
     * @param builder The builder instance containing the configuration values.
     */
    protected ActionConfig(Builder<?> builder) {
        this.pauseBeforeBegin = builder.pauseBeforeBegin;
        this.pauseAfterEnd = builder.pauseAfterEnd;
        this.successCriteria = builder.successCriteria;
        this.illustrate = builder.illustrate;
        this.subsequentActions = builder.subsequentActions;
        this.beforeActionLog = builder.beforeActionLog;
        this.afterActionLog = builder.afterActionLog;
        this.successLog = builder.successLog;
        this.failureLog = builder.failureLog;
    }

    /**
     * Abstract generic Builder for constructing ActionConfig and its subclasses. This pattern
     * allows for fluent, inheritable builder methods.
     *
     * <p><b>Example of Chaining Actions:</b>
     *
     * <pre>{@code
     * // This example creates an action that finds the best match for "saveIcon"
     * // and then, if successful, performs a left click on the found location.
     *
     * PatternFindOptions findAndClick = new PatternFindOptions.Builder()
     * .setStrategy(PatternFindOptions.Strategy.BEST)
     * .setPauseAfterEnd(0.5)
     * .then(new ClickOptions.Builder()
     *     .setNumberOfClicks(1)
     *     .build())
     * .build();
     *
     * // This configuration object can now be passed to the Action.perform method.
     * action.perform(findAndClick, saveIcon);
     * }</pre>
     *
     * @param <B> The type of the concrete builder subclass.
     */
    public abstract static class Builder<B extends Builder<B>> {

        private double pauseBeforeBegin = 0;
        private double pauseAfterEnd = 0;
        @JsonIgnore private Predicate<ActionResult> successCriteria;
        private Illustrate illustrate = Illustrate.USE_GLOBAL;
        private List<ActionConfig> subsequentActions = new ArrayList<>();

        // Logging fields
        private String beforeActionLog;
        private String afterActionLog;
        private String successLog;
        private String failureLog;

        /** Default constructor for the builder. */
        public Builder() {}

        /**
         * Copy constructor to initialize a builder from an existing ActionConfig instance.
         *
         * @param original The ActionConfig instance to copy values from.
         */
        public Builder(ActionConfig original) {
            this.pauseBeforeBegin = original.pauseBeforeBegin;
            this.pauseAfterEnd = original.pauseAfterEnd;
            this.successCriteria = original.successCriteria;
            this.illustrate = original.illustrate;
            this.subsequentActions = new ArrayList<>(original.subsequentActions);
            this.beforeActionLog = original.beforeActionLog;
            this.afterActionLog = original.afterActionLog;
            this.successLog = original.successLog;
            this.failureLog = original.failureLog;
        }

        /**
         * Returns this builder instance, correctly typed to the subclass. This is a standard trick
         * to enable method chaining across class hierarchies.
         *
         * @return The correctly typed builder instance.
         */
        protected abstract B self();

        /**
         * Sets a pause duration, in seconds, to wait before the action begins execution.
         *
         * @param seconds The pause duration.
         * @return The builder instance for chaining.
         */
        public B setPauseBeforeBegin(double seconds) {
            this.pauseBeforeBegin = seconds;
            return self();
        }

        /**
         * Sets a pause duration, in seconds, to wait after the action has completed.
         *
         * @param seconds The pause duration.
         * @return The builder instance for chaining.
         */
        public B setPauseAfterEnd(double seconds) {
            this.pauseAfterEnd = seconds;
            return self();
        }

        /**
         * Sets a custom predicate to determine the success of the action. This overrides all
         * default success evaluation logic for the action.
         *
         * @param successCriteria A predicate that takes an ActionResult and returns true for
         *     success.
         * @return The builder instance for chaining.
         */
        public B setSuccessCriteria(Predicate<ActionResult> successCriteria) {
            this.successCriteria = successCriteria;
            return self();
        }

        /**
         * Overrides the global setting for generating visual illustrations for this action.
         *
         * @param illustrate The illustration override setting.
         * @return The builder instance for chaining.
         */
        public B setIllustrate(Illustrate illustrate) {
            this.illustrate = illustrate;
            return self();
        }

        /**
         * Sets a log message to be displayed before the action begins execution.
         *
         * @param message The log message to display before action execution.
         * @return The builder instance for chaining.
         */
        public B withBeforeActionLog(String message) {
            this.beforeActionLog = message;
            return self();
        }

        /**
         * Sets a log message to be displayed after the action has completed.
         *
         * @param message The log message to display after action execution.
         * @return The builder instance for chaining.
         */
        public B withAfterActionLog(String message) {
            this.afterActionLog = message;
            return self();
        }

        /**
         * Sets a log message to be displayed when the action succeeds.
         *
         * @param message The log message to display on successful action execution.
         * @return The builder instance for chaining.
         */
        public B withSuccessLog(String message) {
            this.successLog = message;
            return self();
        }

        /**
         * Sets a log message to be displayed when the action fails.
         *
         * @param message The log message to display on failed action execution.
         * @return The builder instance for chaining.
         */
        public B withFailureLog(String message) {
            this.failureLog = message;
            return self();
        }

        /**
         * Chains another action to be executed after this one. The subsequent action will operate
         * on the results of this action.
         *
         * @param nextActionConfig The configuration of the action to execute next.
         * @return The builder instance for chaining.
         */
        public B then(ActionConfig nextActionConfig) {
            this.subsequentActions.add(nextActionConfig);
            return self();
        }
    }
}
