package io.github.jspinak.brobot.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import lombok.Getter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Abstract base class for all action configurations in the Brobot framework.
 * <p>
 * This class defines the common parameters that are applicable to any action,
 * such as timing, success criteria, and illustration settings. It uses a generic,
 * inheritable Builder pattern to ensure that all specialized configuration classes
 * can provide a consistent and fluent API.
 * <p>
 * Specialized configuration classes (e.g., {@code ClickOptions}, {@code FindOptions})
 * must extend this class.
 */
@Getter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.click.ClickOptions.class, name = "ClickOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.find.PatternFindOptions.class, name = "PatternFindOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.composite.drag.DragOptions.class, name = "DragOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.region.DefineRegionOptions.class, name = "DefineRegionOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.highlight.HighlightOptions.class, name = "HighlightOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.type.TypeOptions.class, name = "TypeOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.mouse.ScrollOptions.class, name = "ScrollOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions.class, name = "MouseMoveOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions.class, name = "MouseDownOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions.class, name = "MouseUpOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.type.KeyDownOptions.class, name = "KeyDownOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.basic.type.KeyUpOptions.class, name = "KeyUpOptions"),
    @JsonSubTypes.Type(value = io.github.jspinak.brobot.action.composite.repeat.ClickUntilOptions.class, name = "ClickUntilOptions")
})
public abstract class ActionConfig {

    /**
     * Overrides the global illustration setting for this specific action.
     */
    public enum Illustrate {
        /** Always generate an illustration for this action. */
        YES,
        /** Never generate an illustration for this action. */
        NO,
        /** Use the global framework setting to decide. This is the default. */
        USE_GLOBAL
    }

    /**
     * Configuration for automatic logging of action results.
     * Enables streamlined success/failure logging without manual checks.
     */
    @Data
    @lombok.Builder
    public static class LoggingOptions {
        private String beforeActionMessage;
        private String afterActionMessage;
        private String successMessage;
        private String failureMessage;
        @lombok.Builder.Default
        private boolean logBeforeAction = false;
        @lombok.Builder.Default
        private boolean logAfterAction = false;
        @lombok.Builder.Default
        private boolean logOnSuccess = true;
        @lombok.Builder.Default
        private boolean logOnFailure = true;
        @lombok.Builder.Default
        private LogEventType beforeActionLevel = LogEventType.ACTION;
        @lombok.Builder.Default
        private LogEventType afterActionLevel = LogEventType.ACTION;
        @lombok.Builder.Default
        private LogEventType successLevel = LogEventType.ACTION;
        @lombok.Builder.Default
        private LogEventType failureLevel = LogEventType.ERROR;
        
        /**
         * Creates default logging options with standard messages.
         */
        public static LoggingOptions defaults() {
            return LoggingOptions.builder().build();
        }
    }

    private final double pauseBeforeBegin;
    private final double pauseAfterEnd;
    private final Predicate<ActionResult> successCriteria;
    private final Illustrate illustrate;
    private final List<ActionConfig> subsequentActions;
    private final LogEventType logType; // For chaining
    private final LoggingOptions loggingOptions;

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
        this.logType = builder.logType;
        this.loggingOptions = builder.loggingOptions;
    }

    /**
     * Abstract generic Builder for constructing ActionConfig and its subclasses.
     * This pattern allows for fluent, inheritable builder methods.
     *
     * <p><b>Example of Chaining Actions:</b></p>
     * <pre>{@code
     * // This example creates an action that finds the best match for "saveIcon"
     * // and then, if successful, performs a left click on the found location.
     *
     * PatternFindOptions findAndClick = new PatternFindOptions.Builder()
     * .setStrategy(PatternFindOptions.Strategy.BEST)
     * .setPauseAfterEnd(0.5)
     * .then(new ClickOptions.Builder()
     *     .setClickType(ClickOptions.Type.LEFT)
     *     .build())
     * .build();
     *
     * // This configuration object can now be passed to the Action.perform method.
     * action.perform(findAndClick, saveIcon);
     * }</pre>
     *
     * @param <B> The type of the concrete builder subclass.
     */
    public static abstract class Builder<B extends Builder<B>> {

        private double pauseBeforeBegin = 0;
        private double pauseAfterEnd = 0;
        @JsonIgnore
        private Predicate<ActionResult> successCriteria;
        private Illustrate illustrate = Illustrate.USE_GLOBAL;
        private List<ActionConfig> subsequentActions = new ArrayList<>();
        private LogEventType logType = LogEventType.ACTION;
        private LoggingOptions loggingOptions = LoggingOptions.defaults();

        /**
         * Default constructor for the builder.
         */
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
            this.logType = original.logType;
            this.loggingOptions = original.loggingOptions;
        }

        /**
         * Returns this builder instance, correctly typed to the subclass.
         * This is a standard trick to enable method chaining across class hierarchies.
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
         * Sets a custom predicate to determine the success of the action. This overrides
         * all default success evaluation logic for the action.
         *
         * @param successCriteria A predicate that takes an ActionResult and returns true for success.
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
         * Sets the log event type for categorizing this action in logs.
         *
         * @param logType The type of log event this action represents.
         * @return The builder instance for chaining.
         */
        public B setLogType(LogEventType logType) {
            this.logType = logType;
            return self();
        }

        /**
         * Chains another action to be executed after this one.
         * The subsequent action will operate on the results of this action.
         *
         * @param nextActionConfig The configuration of the action to execute next.
         * @return The builder instance for chaining.
         */
        public B then(ActionConfig nextActionConfig) {
            this.subsequentActions.add(nextActionConfig);
            return self();
        }

        /**
         * Sets a message to be logged before the action begins execution.
         *
         * @param message The message to log before action execution
         * @return The builder instance for chaining.
         */
        public B withBeforeActionLog(String message) {
            this.loggingOptions = LoggingOptions.builder()
                    .beforeActionMessage(message)
                    .afterActionMessage(this.loggingOptions.getAfterActionMessage())
                    .successMessage(this.loggingOptions.getSuccessMessage())
                    .failureMessage(this.loggingOptions.getFailureMessage())
                    .logBeforeAction(true)
                    .logAfterAction(this.loggingOptions.isLogAfterAction())
                    .logOnSuccess(this.loggingOptions.isLogOnSuccess())
                    .logOnFailure(this.loggingOptions.isLogOnFailure())
                    .beforeActionLevel(this.loggingOptions.getBeforeActionLevel())
                    .afterActionLevel(this.loggingOptions.getAfterActionLevel())
                    .successLevel(this.loggingOptions.getSuccessLevel())
                    .failureLevel(this.loggingOptions.getFailureLevel())
                    .build();
            return self();
        }

        /**
         * Sets a message to be logged after the action completes (regardless of success/failure).
         *
         * @param message The message to log after action execution
         * @return The builder instance for chaining.
         */
        public B withAfterActionLog(String message) {
            this.loggingOptions = LoggingOptions.builder()
                    .beforeActionMessage(this.loggingOptions.getBeforeActionMessage())
                    .afterActionMessage(message)
                    .successMessage(this.loggingOptions.getSuccessMessage())
                    .failureMessage(this.loggingOptions.getFailureMessage())
                    .logBeforeAction(this.loggingOptions.isLogBeforeAction())
                    .logAfterAction(true)
                    .logOnSuccess(this.loggingOptions.isLogOnSuccess())
                    .logOnFailure(this.loggingOptions.isLogOnFailure())
                    .beforeActionLevel(this.loggingOptions.getBeforeActionLevel())
                    .afterActionLevel(this.loggingOptions.getAfterActionLevel())
                    .successLevel(this.loggingOptions.getSuccessLevel())
                    .failureLevel(this.loggingOptions.getFailureLevel())
                    .build();
            return self();
        }

        /**
         * Sets a success message to be logged when the action completes successfully.
         *
         * @param message The message to log on success
         * @return The builder instance for chaining.
         */
        public B withSuccessLog(String message) {
            this.loggingOptions = LoggingOptions.builder()
                    .beforeActionMessage(this.loggingOptions.getBeforeActionMessage())
                    .afterActionMessage(this.loggingOptions.getAfterActionMessage())
                    .successMessage(message)
                    .failureMessage(this.loggingOptions.getFailureMessage())
                    .logBeforeAction(this.loggingOptions.isLogBeforeAction())
                    .logAfterAction(this.loggingOptions.isLogAfterAction())
                    .logOnSuccess(true)
                    .logOnFailure(this.loggingOptions.isLogOnFailure())
                    .beforeActionLevel(this.loggingOptions.getBeforeActionLevel())
                    .afterActionLevel(this.loggingOptions.getAfterActionLevel())
                    .successLevel(this.loggingOptions.getSuccessLevel())
                    .failureLevel(this.loggingOptions.getFailureLevel())
                    .build();
            return self();
        }

        /**
         * Sets a failure message to be logged when the action fails.
         *
         * @param message The message to log on failure
         * @return The builder instance for chaining.
         */
        public B withFailureLog(String message) {
            this.loggingOptions = LoggingOptions.builder()
                    .beforeActionMessage(this.loggingOptions.getBeforeActionMessage())
                    .afterActionMessage(this.loggingOptions.getAfterActionMessage())
                    .successMessage(this.loggingOptions.getSuccessMessage())
                    .failureMessage(message)
                    .logBeforeAction(this.loggingOptions.isLogBeforeAction())
                    .logAfterAction(this.loggingOptions.isLogAfterAction())
                    .logOnSuccess(this.loggingOptions.isLogOnSuccess())
                    .logOnFailure(true)
                    .beforeActionLevel(this.loggingOptions.getBeforeActionLevel())
                    .afterActionLevel(this.loggingOptions.getAfterActionLevel())
                    .successLevel(this.loggingOptions.getSuccessLevel())
                    .failureLevel(this.loggingOptions.getFailureLevel())
                    .build();
            return self();
        }

        /**
         * Configures logging options using a consumer function.
         * Provides full control over all logging settings.
         *
         * @param configurator A consumer that configures the LoggingOptions
         * @return The builder instance for chaining.
         */
        public B withLogging(Consumer<LoggingOptions.LoggingOptionsBuilder> configurator) {
            LoggingOptions.LoggingOptionsBuilder loggingBuilder = LoggingOptions.builder();
            // Copy existing values
            loggingBuilder.beforeActionMessage(this.loggingOptions.getBeforeActionMessage())
                    .afterActionMessage(this.loggingOptions.getAfterActionMessage())
                    .successMessage(this.loggingOptions.getSuccessMessage())
                    .failureMessage(this.loggingOptions.getFailureMessage())
                    .logBeforeAction(this.loggingOptions.isLogBeforeAction())
                    .logAfterAction(this.loggingOptions.isLogAfterAction())
                    .logOnSuccess(this.loggingOptions.isLogOnSuccess())
                    .logOnFailure(this.loggingOptions.isLogOnFailure())
                    .beforeActionLevel(this.loggingOptions.getBeforeActionLevel())
                    .afterActionLevel(this.loggingOptions.getAfterActionLevel())
                    .successLevel(this.loggingOptions.getSuccessLevel())
                    .failureLevel(this.loggingOptions.getFailureLevel());
            // Apply customizations
            configurator.accept(loggingBuilder);
            this.loggingOptions = loggingBuilder.build();
            return self();
        }

        /**
         * Disables all automatic logging for this action.
         *
         * @return The builder instance for chaining.
         */
        public B withNoLogging() {
            this.loggingOptions = LoggingOptions.builder()
                    .logOnSuccess(false)
                    .logOnFailure(false)
                    .build();
            return self();
        }
    }
}
