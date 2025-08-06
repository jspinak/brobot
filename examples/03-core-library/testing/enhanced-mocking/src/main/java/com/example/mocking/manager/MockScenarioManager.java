package com.example.mocking.manager;

import com.example.mocking.config.MockScenarioConfig;
import com.example.mocking.config.FailurePattern;
import com.example.mocking.config.TemporalConditions;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages mock scenarios and their lifecycle.
 */
@Component
@Slf4j
public class MockScenarioManager {
    
    private final Map<String, MockScenarioConfig> scenarios = new ConcurrentHashMap<>();
    private MockScenarioConfig activeScenario;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private long scenarioStartTime;
    
    /**
     * Register a scenario for later use
     */
    public void registerScenario(MockScenarioConfig scenario) {
        scenarios.put(scenario.getScenarioName(), scenario);
        log.info("Registered scenario: {}", scenario.getScenarioName());
    }
    
    /**
     * Activate a scenario
     */
    public void activateScenario(MockScenarioConfig scenario) {
        deactivateCurrentScenario();
        
        activeScenario = scenario;
        scenarioStartTime = System.currentTimeMillis();
        
        log.info("Activated scenario: {} - {}", 
            scenario.getScenarioName(), 
            scenario.getDescription());
        
        // Schedule timeout if configured
        if (scenario.getMaxDuration() != null) {
            scheduler.schedule(
                this::handleScenarioTimeout,
                scenario.getMaxDuration().toMillis(),
                TimeUnit.MILLISECONDS
            );
        }
        
        // Apply scenario settings
        applyScenarioSettings();
    }
    
    /**
     * Activate a registered scenario by name
     */
    public void activateScenario(String scenarioName) {
        MockScenarioConfig scenario = scenarios.get(scenarioName);
        if (scenario != null) {
            activateScenario(scenario);
        } else {
            log.error("Scenario not found: {}", scenarioName);
        }
    }
    
    /**
     * Deactivate the current scenario
     */
    public void deactivateCurrentScenario() {
        if (activeScenario != null) {
            log.info("Deactivating scenario: {}", activeScenario.getScenarioName());
            
            // Reset all failure patterns
            activeScenario.getActionFailurePatterns().values().forEach(pattern -> {
                pattern.setCurrentConsecutiveFailures(0);
                pattern.setCurrentProbability(0);
            });
            
            // Reset temporal conditions
            activeScenario.getTemporalConditions().values().forEach(TemporalConditions::reset);
            
            activeScenario = null;
        }
    }
    
    /**
     * Get the active scenario
     */
    public MockScenarioConfig getActiveScenario() {
        return activeScenario;
    }
    
    /**
     * Check if an action should fail based on current scenario
     */
    public boolean shouldActionFail(ActionOptions.Action action) {
        if (activeScenario == null) {
            return false;
        }
        
        FailurePattern pattern = activeScenario.getActionFailurePatterns().get(action);
        if (pattern == null) {
            return false;
        }
        
        // Check if should force success
        if (pattern.shouldForceSuccess()) {
            pattern.recordSuccess();
            return false;
        }
        
        // Calculate failure probability
        double probability = pattern.getCurrentFailureProbability();
        boolean shouldFail = Math.random() < probability;
        
        // Record result
        if (shouldFail) {
            pattern.recordFailure();
            log.debug("Action {} will fail (probability: {})", action, probability);
        } else {
            pattern.recordSuccess();
        }
        
        return shouldFail;
    }
    
    /**
     * Get delay for current action
     */
    public long getActionDelay(String conditionName) {
        if (activeScenario == null) {
            return 0;
        }
        
        TemporalConditions condition = activeScenario.getTemporalConditions().get(conditionName);
        if (condition == null) {
            return 0;
        }
        
        return condition.getCurrentDelay().toMillis();
    }
    
    /**
     * Get state appearance probability
     */
    public double getStateAppearanceProbability(String stateName) {
        if (activeScenario == null) {
            return 1.0; // Default to always appear
        }
        
        return activeScenario.getStateAppearanceProbabilities().getOrDefault(stateName, 1.0);
    }
    
    /**
     * Apply scenario settings to the mock environment
     */
    private void applyScenarioSettings() {
        // This would integrate with Brobot's mock system
        // For now, just log the settings
        log.debug("Applied scenario settings:");
        log.debug("  State probabilities: {}", activeScenario.getStateAppearanceProbabilities());
        log.debug("  Failure patterns: {} configured", activeScenario.getActionFailurePatterns().size());
        log.debug("  Temporal conditions: {} configured", activeScenario.getTemporalConditions().size());
    }
    
    /**
     * Handle scenario timeout
     */
    private void handleScenarioTimeout() {
        log.warn("Scenario {} timed out after {}", 
            activeScenario.getScenarioName(),
            activeScenario.getMaxDuration());
        deactivateCurrentScenario();
    }
    
    /**
     * Get scenario execution time
     */
    public long getScenarioExecutionTime() {
        if (activeScenario == null) {
            return 0;
        }
        return System.currentTimeMillis() - scenarioStartTime;
    }
    
    /**
     * Shutdown manager
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}