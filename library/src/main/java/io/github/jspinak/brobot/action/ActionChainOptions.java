package io.github.jspinak.brobot.action;

import lombok.Getter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for executing a chain of actions with specific chaining behavior.
 * <p>
 * ActionChainOptions wraps an initial ActionConfig and adds parameters that control
 * how an entire sequence of actions behaves. This includes the chaining strategy
 * (nested vs. confirmed) and the list of subsequent actions to execute.
 * </p>
 * <p>
 * This design separates the configuration of individual actions from the configuration
 * of how those actions work together, following the Single Responsibility Principle.
 * </p>
 * 
 * @since 1.0
 */
@Getter
public final class ActionChainOptions extends ActionConfig {
    
    /**
     * Defines how results from one action in the chain relate to the next.
     */
    public enum ChainingStrategy {
        /**
         * Each action searches within the results of the previous action.
         * Best for hierarchical searches like finding a button within a dialog.
         */
        NESTED,
        
        /**
         * Each action validates/confirms the results of the previous action.
         * Best for eliminating false positives by requiring multiple confirmations.
         */
        CONFIRM
    }
    
    private final ActionConfig initialAction;
    private final ChainingStrategy strategy;
    private final List<ActionConfig> chainedActions;
    
    private ActionChainOptions(Builder builder) {
        super(builder);
        this.initialAction = builder.initialAction;
        this.strategy = builder.strategy;
        this.chainedActions = new ArrayList<>(builder.chainedActions);
    }
    
    /**
     * Returns an unmodifiable view of the chained actions list.
     * @return unmodifiable list of chained actions
     */
    public List<ActionConfig> getChainedActions() {
        return Collections.unmodifiableList(chainedActions);
    }
    
    /**
     * Builder for constructing ActionChainOptions with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {
        
        private ActionConfig initialAction;
        private ChainingStrategy strategy = ChainingStrategy.NESTED;
        private final List<ActionConfig> chainedActions = new ArrayList<>();
        
        /**
         * Creates a new Builder with the initial action.
         *
         * @param initialAction the first action in the chain
         */
        public Builder(ActionConfig initialAction) {
            this.initialAction = initialAction;
        }
        
        /**
         * Sets the chaining strategy.
         *
         * @param strategy how actions in the chain relate to each other
         * @return this Builder instance for chaining
         */
        public Builder setStrategy(ChainingStrategy strategy) {
            this.strategy = strategy;
            return self();
        }
        
        /**
         * Adds an action to the chain.
         *
         * @param action the action to add to the chain
         * @return this Builder instance for chaining
         */
        public Builder then(ActionConfig action) {
            this.chainedActions.add(action);
            return self();
        }
        
        /**
         * Builds the immutable ActionChainOptions object.
         *
         * @return a new instance of ActionChainOptions
         */
        public ActionChainOptions build() {
            return new ActionChainOptions(this);
        }
        
        @Override
        protected Builder self() {
            return this;
        }
    }
}