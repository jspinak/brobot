package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for creating action chains in a readable, declarative style.
 * <p>
 * This builder simplifies the creation of complex action sequences by providing
 * a fluent interface that clearly expresses the order and configuration of actions.
 * </p>
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * ActionChainOptions chain = ActionChainBuilder
 *     .of(ActionInterface.Type.FIND, new PatternFindOptions.Builder().build())
 *     .then(ActionInterface.Type.CLICK, new ClickOptions.Builder().build())
 *     .then(ActionInterface.Type.TYPE, new TypeTextOptions.Builder().build())
 *     .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
 *     .build();
 * }</pre>
 */
public class ActionChainBuilder {
    
    private final ActionConfig initialAction;
    private final List<ActionConfig> chainedActions;
    private ActionChainOptions.ChainingStrategy strategy = ActionChainOptions.ChainingStrategy.NESTED;
    private double pauseBeforeBegin = 0.0;
    private double pauseAfterEnd = 0.0;
    private ActionConfig.Illustrate illustrate = ActionConfig.Illustrate.USE_GLOBAL;
    private LogEventType logType = LogEventType.ACTION;

    private ActionChainBuilder(ActionConfig initialAction) {
        this.initialAction = initialAction;
        this.chainedActions = new ArrayList<>();
    }

    /**
     * Creates a new ActionChainBuilder with the initial action.
     *
     * @param initialActionType   The type of the initial action
     * @param initialActionConfig The configuration for the initial action
     * @return A new ActionChainBuilder instance
     */
    public static ActionChainBuilder of(ActionInterface.Type initialActionType, ActionConfig initialActionConfig) {
        return new ActionChainBuilder(initialActionConfig);
    }

    /**
     * Creates a new ActionChainBuilder with the initial action configuration.
     *
     * @param initialActionConfig The configuration for the initial action
     * @return A new ActionChainBuilder instance
     */
    public static ActionChainBuilder of(ActionConfig initialActionConfig) {
        return new ActionChainBuilder(initialActionConfig);
    }

    /**
     * Adds the next action to the chain.
     *
     * @param nextActionType   The type of the next action
     * @param nextActionConfig The configuration for the next action
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder then(ActionInterface.Type nextActionType, ActionConfig nextActionConfig) {
        this.chainedActions.add(nextActionConfig);
        return this;
    }

    /**
     * Adds the next action to the chain.
     *
     * @param nextActionConfig The configuration for the next action
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder then(ActionConfig nextActionConfig) {
        this.chainedActions.add(nextActionConfig);
        return this;
    }

    /**
     * Adds multiple actions to the chain in sequence.
     *
     * @param actions The configurations for the actions to add
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder thenAll(ActionConfig... actions) {
        for (ActionConfig action : actions) {
            this.chainedActions.add(action);
        }
        return this;
    }

    /**
     * Sets the chaining strategy for the action chain.
     *
     * @param strategy The chaining strategy to use
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder withStrategy(ActionChainOptions.ChainingStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    /**
     * Sets the pause duration before beginning the chain execution.
     *
     * @param pauseBeforeBegin The pause duration in seconds
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder pauseBeforeBegin(double pauseBeforeBegin) {
        this.pauseBeforeBegin = pauseBeforeBegin;
        return this;
    }

    /**
     * Sets the pause duration after completing the chain execution.
     *
     * @param pauseAfterEnd The pause duration in seconds
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder pauseAfterEnd(double pauseAfterEnd) {
        this.pauseAfterEnd = pauseAfterEnd;
        return this;
    }

    /**
     * Sets whether to illustrate the chain execution.
     *
     * @param illustrate The illustration setting
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder illustrate(ActionConfig.Illustrate illustrate) {
        this.illustrate = illustrate;
        return this;
    }

    /**
     * Sets the log event type for the chain.
     *
     * @param logType The log event type
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder logEventType(LogEventType logType) {
        this.logType = logType;
        return this;
    }

    /**
     * Configures the chain to use NESTED chaining strategy.
     * This is a convenience method equivalent to withStrategy(NESTED).
     *
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder nested() {
        return withStrategy(ActionChainOptions.ChainingStrategy.NESTED);
    }

    /**
     * Configures the chain to use CONFIRM chaining strategy.
     * This is a convenience method equivalent to withStrategy(CONFIRM).
     *
     * @return This builder for fluent chaining
     */
    public ActionChainBuilder confirm() {
        return withStrategy(ActionChainOptions.ChainingStrategy.CONFIRM);
    }

    /**
     * Builds and returns the configured ActionChainOptions.
     *
     * @return The configured ActionChainOptions instance
     * @throws IllegalStateException if the chain is invalid
     */
    public ActionChainOptions build() {
        if (initialAction == null) {
            throw new IllegalStateException("Initial action cannot be null");
        }

        ActionChainOptions.Builder builder = new ActionChainOptions.Builder(initialAction);
        
        // Add all chained actions
        for (ActionConfig action : chainedActions) {
            builder.then(action);
        }
        
        // Configure chain properties
        builder.setStrategy(strategy)
                .setPauseBeforeBegin(pauseBeforeBegin)
                .setPauseAfterEnd(pauseAfterEnd)
                .setIllustrate(illustrate)
                .setLogType(logType);

        return builder.build();
    }

    /**
     * Creates a simple two-action chain.
     *
     * @param firstAction  The first action configuration
     * @param secondAction The second action configuration
     * @return The configured ActionChainOptions instance
     */
    public static ActionChainOptions simple(ActionConfig firstAction, ActionConfig secondAction) {
        return of(firstAction).then(secondAction).build();
    }

    /**
     * Creates a chain from a list of actions.
     *
     * @param actions The list of action configurations
     * @return The configured ActionChainOptions instance
     * @throws IllegalArgumentException if the list is empty
     */
    public static ActionChainOptions fromList(List<ActionConfig> actions) {
        if (actions == null || actions.isEmpty()) {
            throw new IllegalArgumentException("Action list cannot be null or empty");
        }
        
        ActionChainBuilder builder = of(actions.get(0));
        for (int i = 1; i < actions.size(); i++) {
            builder.then(actions.get(i));
        }
        
        return builder.build();
    }
}