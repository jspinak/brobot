package io.github.jspinak.brobot.action.result;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Builder for creating ActionResult instances with the new component architecture.
 * Provides a fluent API for constructing complex action results.
 * 
 * This builder bridges the gap between the new component-based architecture
 * and the existing ActionResult API, enabling gradual migration.
 * 
 * @since 2.0
 */
public class ActionResultBuilder {
    private String actionDescription = "";
    private boolean success = false;
    private ActionConfig actionConfig;
    private MatchCollection matches = new MatchCollection();
    private TimingData timing = new TimingData();
    private TextExtractionResult text = new TextExtractionResult();
    private StateTracker states = new StateTracker();
    private RegionManager regions = new RegionManager();
    private MovementTracker movements = new MovementTracker();
    private ActionAnalysis analysis = new ActionAnalysis();
    private ActionMetrics metrics = new ActionMetrics();
    private ExecutionHistory history = new ExecutionHistory();
    
    /**
     * Creates a new ActionResultBuilder.
     */
    public ActionResultBuilder() {}
    
    /**
     * Sets the action description.
     * 
     * @param description Human-readable description
     * @return This builder for chaining
     */
    public ActionResultBuilder withDescription(String description) {
        this.actionDescription = description;
        return this;
    }
    
    /**
     * Sets the success status.
     * 
     * @param success Whether the action succeeded
     * @return This builder for chaining
     */
    public ActionResultBuilder withSuccess(boolean success) {
        this.success = success;
        return this;
    }
    
    /**
     * Sets the action configuration.
     * 
     * @param config The ActionConfig used
     * @return This builder for chaining
     */
    public ActionResultBuilder withActionConfig(ActionConfig config) {
        this.actionConfig = config;
        return this;
    }
    
    /**
     * Sets the match collection.
     * 
     * @param matchCollection The MatchCollection
     * @return This builder for chaining
     */
    public ActionResultBuilder withMatches(MatchCollection matchCollection) {
        if (matchCollection != null) {
            this.matches = matchCollection;
        }
        return this;
    }
    
    /**
     * Adds matches to the collection.
     * 
     * @param matchList List of matches to add
     * @return This builder for chaining
     */
    public ActionResultBuilder withMatches(List<Match> matchList) {
        if (matchList != null) {
            this.matches.addAll(matchList);
        }
        return this;
    }
    
    /**
     * Adds a single match.
     * 
     * @param match The match to add
     * @return This builder for chaining
     */
    public ActionResultBuilder withMatch(Match match) {
        if (match != null) {
            this.matches.add(match);
        }
        return this;
    }
    
    /**
     * Sets the timing data.
     * 
     * @param timingData The TimingData
     * @return This builder for chaining
     */
    public ActionResultBuilder withTiming(TimingData timingData) {
        if (timingData != null) {
            this.timing = timingData;
        }
        return this;
    }
    
    /**
     * Sets timing using start and end times.
     * 
     * @param startTime Start time of execution
     * @param endTime End time of execution
     * @return This builder for chaining
     */
    public ActionResultBuilder withTiming(LocalDateTime startTime, LocalDateTime endTime) {
        this.timing = new TimingData(startTime);
        if (endTime != null) {
            this.timing.setEndTime(endTime);
            this.timing.setTotalDuration(Duration.between(startTime, endTime));
        }
        return this;
    }
    
    /**
     * Sets the text extraction result.
     * 
     * @param textResult The TextExtractionResult
     * @return This builder for chaining
     */
    public ActionResultBuilder withText(TextExtractionResult textResult) {
        if (textResult != null) {
            this.text = textResult;
        }
        return this;
    }
    
    /**
     * Adds extracted text.
     * 
     * @param extractedText Text that was extracted
     * @return This builder for chaining
     */
    public ActionResultBuilder withText(String extractedText) {
        if (extractedText != null) {
            this.text.addText(extractedText);
        }
        return this;
    }
    
    /**
     * Sets selected text.
     * 
     * @param selectedText Text that was selected
     * @return This builder for chaining
     */
    public ActionResultBuilder withSelectedText(String selectedText) {
        if (selectedText != null) {
            this.text.setSelectedText(selectedText);
        }
        return this;
    }
    
    /**
     * Sets the state tracker.
     * 
     * @param stateTracker The StateTracker
     * @return This builder for chaining
     */
    public ActionResultBuilder withStates(StateTracker stateTracker) {
        if (stateTracker != null) {
            this.states = stateTracker;
        }
        return this;
    }
    
    /**
     * Adds an active state.
     * 
     * @param stateName Name of the active state
     * @return This builder for chaining
     */
    public ActionResultBuilder withActiveState(String stateName) {
        if (stateName != null) {
            this.states.recordActiveState(stateName);
        }
        return this;
    }
    
    /**
     * Sets the region manager.
     * 
     * @param regionManager The RegionManager
     * @return This builder for chaining
     */
    public ActionResultBuilder withRegions(RegionManager regionManager) {
        if (regionManager != null) {
            this.regions = regionManager;
        }
        return this;
    }
    
    /**
     * Adds a defined region.
     * 
     * @param region The region to add
     * @return This builder for chaining
     */
    public ActionResultBuilder withRegion(io.github.jspinak.brobot.model.element.Region region) {
        if (region != null) {
            this.regions.defineRegion(region);
        }
        return this;
    }
    
    /**
     * Sets the movement tracker.
     * 
     * @param movementTracker The MovementTracker
     * @return This builder for chaining
     */
    public ActionResultBuilder withMovements(MovementTracker movementTracker) {
        if (movementTracker != null) {
            this.movements = movementTracker;
        }
        return this;
    }
    
    /**
     * Adds a movement.
     * 
     * @param movement The movement to add
     * @return This builder for chaining
     */
    public ActionResultBuilder withMovement(io.github.jspinak.brobot.model.element.Movement movement) {
        if (movement != null) {
            this.movements.recordMovement(movement);
        }
        return this;
    }
    
    /**
     * Sets the action analysis.
     * 
     * @param actionAnalysis The ActionAnalysis
     * @return This builder for chaining
     */
    public ActionResultBuilder withAnalysis(ActionAnalysis actionAnalysis) {
        if (actionAnalysis != null) {
            this.analysis = actionAnalysis;
        }
        return this;
    }
    
    /**
     * Sets the action metrics.
     * 
     * @param actionMetrics The ActionMetrics
     * @return This builder for chaining
     */
    public ActionResultBuilder withMetrics(ActionMetrics actionMetrics) {
        if (actionMetrics != null) {
            this.metrics = actionMetrics;
        }
        return this;
    }
    
    /**
     * Sets the execution history.
     * 
     * @param executionHistory The ExecutionHistory
     * @return This builder for chaining
     */
    public ActionResultBuilder withHistory(ExecutionHistory executionHistory) {
        if (executionHistory != null) {
            this.history = executionHistory;
        }
        return this;
    }
    
    /**
     * Builds the ActionResult with all configured components.
     * 
     * @return The constructed ActionResult
     */
    public ActionResult build() {
        ActionResult result = new ActionResult(actionConfig);
        
        // Set basic properties
        result.setActionDescription(actionDescription);
        result.setSuccess(success);
        
        // Apply match collection
        if (!matches.isEmpty()) {
            result.setMatchList(matches.getMatches());
            result.setInitialMatchList(matches.getInitialMatches());
        }
        
        // Apply timing data
        if (timing.hasStarted()) {
            result.setStartTime(timing.getStartTime());
            if (timing.hasCompleted()) {
                result.setEndTime(timing.getEndTime());
                result.setDuration(timing.getElapsed());
            }
        }
        
        // Apply text extraction
        if (text.hasText()) {
            result.setText(text.getAccumulatedText());
            result.setSelectedText(text.getSelectedText());
        }
        
        // Apply state tracking
        if (!states.getActiveStates().isEmpty()) {
            result.setActiveStates(states.getActiveStates());
        }
        
        // Apply regions
        if (!regions.isEmpty()) {
            result.setDefinedRegions(regions.getAllRegions());
        }
        
        // Apply movements
        if (!movements.isEmpty()) {
            result.setMovements(movements.getMovementSequence());
        }
        
        // Apply analysis
        if (analysis.hasSceneAnalyses()) {
            result.setSceneAnalysisCollection(analysis.getSceneAnalyses());
        }
        if (analysis.hasMask()) {
            result.setMask(analysis.getMask());
        }
        
        // Apply metrics
        if (metrics != null) {
            // Convert standalone ActionMetrics to inner ActionMetrics
            ActionResult.ActionMetrics innerMetrics = new ActionResult.ActionMetrics();
            innerMetrics.setExecutionTimeMs(metrics.getExecutionTimeMs());
            innerMetrics.setMatchCount(matches.size());
            innerMetrics.setThreadName(metrics.getThreadName());
            innerMetrics.setActionId(metrics.getActionId());
            innerMetrics.setRetryCount(metrics.getRetryCount());
            innerMetrics.setRetryTimeMs(metrics.getRetryTimeMs());
            matches.getBest().ifPresent(m -> innerMetrics.setBestMatchConfidence(m.getScore()));
            result.setActionMetrics(innerMetrics);
        }
        
        // Apply history
        if (!history.isEmpty()) {
            result.setExecutionHistory(history.getHistory());
        }
        
        return result;
    }
    
    /**
     * Creates a successful result with matches.
     * 
     * @param matchList The matches found
     * @return Constructed ActionResult
     */
    public static ActionResult successWith(List<Match> matchList) {
        return new ActionResultBuilder()
                .withSuccess(true)
                .withMatches(matchList)
                .build();
    }
    
    /**
     * Creates a failed result with no matches.
     * 
     * @return Constructed ActionResult
     */
    public static ActionResult failure() {
        return new ActionResultBuilder()
                .withSuccess(false)
                .build();
    }
    
    /**
     * Creates a result from an existing ActionResult.
     * Useful for copying and modifying results.
     * 
     * @param source The source ActionResult
     * @return New builder initialized with source data
     */
    public static ActionResultBuilder from(ActionResult source) {
        ActionResultBuilder builder = new ActionResultBuilder()
                .withDescription(source.getActionDescription())
                .withSuccess(source.isSuccess())
                .withActionConfig(source.getActionConfig());
        
        // Copy matches
        if (!source.getMatchList().isEmpty()) {
            builder.withMatches(source.getMatchList());
        }
        
        // Copy timing
        if (source.getStartTime() != null) {
            builder.withTiming(source.getStartTime(), source.getEndTime());
        }
        
        // Copy text
        if (source.getText() != null && !source.getText().isEmpty()) {
            builder.text.setAccumulatedText(source.getText());
        }
        if (source.getSelectedText() != null && !source.getSelectedText().isEmpty()) {
            builder.withSelectedText(source.getSelectedText());
        }
        
        // Copy states
        source.getActiveStates().forEach(builder::withActiveState);
        
        // Copy regions
        source.getDefinedRegions().forEach(builder::withRegion);
        
        // Copy movements
        source.getMovements().forEach(builder::withMovement);
        
        return builder;
    }
}