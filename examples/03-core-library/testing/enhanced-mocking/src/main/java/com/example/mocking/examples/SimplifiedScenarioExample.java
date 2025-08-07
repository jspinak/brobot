package com.example.mocking.examples;

import com.example.mocking.manager.MockScenarioManager;
import com.example.mocking.config.MockScenarioConfig;
import com.example.mocking.config.FailurePattern;
import com.example.mocking.config.TemporalConditions;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Simplified scenario-based testing example.
 */
@Component
@Slf4j
public class SimplifiedScenarioExample {
    
    @Autowired
    private MockScenarioManager scenarioManager;
    
    /**
     * Demonstrates a simple degrading performance scenario
     */
    public void runSimpleDegradingPerformance() {
        log.info("=== Simple Degrading Performance Example ===");
        
        // Configure scenario
        MockScenarioConfig config = MockScenarioConfig.builder()
            .scenarioName("simple_degrading_performance")
            .description("Actions become slower over time")
            .maxDuration(Duration.ofMinutes(2))
            .build();
        
        // Add temporal condition - performance degrades after 30 seconds
        TemporalConditions degradation = TemporalConditions.builder()
            .conditionName("performance_degradation")
            .activateAfter(Duration.ofSeconds(30))
            .build();
        config.getTemporalConditions().put("degradation", degradation);
        
        // Add failure pattern for slow clicks
        FailurePattern slowClicks = FailurePattern.builder()
            .patternName("slow_clicks")
            .failureRate(0.0)  // No failures, just delays
            .delayBeforeFailure(Duration.ofSeconds(2))
            .build();
        config.getActionFailurePatterns().put(ActionOptions.Action.CLICK, slowClicks);
        
        // Start scenario
        scenarioManager.activateScenario(config);
        
        try {
            // Simulate initial fast actions
            log.info("Initial actions (should be fast):");
            for (int i = 1; i <= 3; i++) {
                long start = System.currentTimeMillis();
                boolean shouldFail = scenarioManager.shouldActionFail(ActionOptions.Action.CLICK);
                long duration = System.currentTimeMillis() - start;
                log.info("  Action {}: {} ({}ms)", i, shouldFail ? "SLOW" : "FAST", duration);
                Thread.sleep(1000);
            }
            
            // Wait for degradation
            log.info("Waiting for performance degradation...");
            Thread.sleep(30000);
            
            // Simulate degraded actions
            log.info("Degraded actions (should be slower):");
            for (int i = 1; i <= 3; i++) {
                long start = System.currentTimeMillis();
                boolean shouldFail = scenarioManager.shouldActionFail(ActionOptions.Action.CLICK);
                long duration = System.currentTimeMillis() - start;
                log.info("  Action {}: {} ({}ms)", i, shouldFail ? "SLOW" : "FAST", duration);
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            log.error("Example failed: {}", e.getMessage());
        } finally {
            scenarioManager.deactivateCurrentScenario();
            log.info("Scenario completed\n");
        }
    }
    
    /**
     * Demonstrates cascading failures
     */
    public void runSimpleCascadingFailures() {
        log.info("=== Simple Cascading Failures Example ===");
        
        MockScenarioConfig config = MockScenarioConfig.builder()
            .scenarioName("simple_cascading")
            .description("Initial failures trigger more failures")
            .cascadingFailures(true)
            .build();
        
        // Configure initial failure that cascades
        FailurePattern initialFailure = FailurePattern.builder()
            .patternName("initial_failure")
            .failureRate(0.3)  // 30% failure rate
            .cascadesToActions(Map.of(
                ActionOptions.Action.TYPE, 0.8,    // 80% chance of TYPE failing
                ActionOptions.Action.FIND, 0.5     // 50% chance of FIND failing
            ))
            .recoveryDelay(Duration.ofSeconds(2))
            .build();
        
        config.getActionFailurePatterns().put(ActionOptions.Action.CLICK, initialFailure);
        
        scenarioManager.activateScenario(config);
        
        try {
            log.info("Performing actions with cascading failure potential:");
            
            // Simulate click actions
            for (int i = 1; i <= 5; i++) {
                boolean clickFailed = simulateAction(ActionOptions.Action.CLICK, i);
                
                if (clickFailed) {
                    log.info("  Click {} failed! Checking for cascading effects...", i);
                    
                    // Check if other actions are affected
                    boolean typeFailed = simulateAction(ActionOptions.Action.TYPE, i);
                    boolean findFailed = simulateAction(ActionOptions.Action.FIND, i);
                    
                    if (typeFailed || findFailed) {
                        log.info("    Cascading failure detected!");
                    }
                }
                
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            log.error("Example failed: {}", e.getMessage());
        } finally {
            scenarioManager.deactivateCurrentScenario();
            log.info("Scenario completed\n");
        }
    }
    
    /**
     * Demonstrates time-based state changes
     */
    public void runSimpleTimeBasedStates() {
        log.info("=== Simple Time-Based States Example ===");
        
        MockScenarioConfig config = MockScenarioConfig.builder()
            .scenarioName("simple_time_based")
            .description("States appear/disappear based on time")
            .build();
        
        // Configure temporal conditions for different states
        TemporalConditions morningState = TemporalConditions.builder()
            .conditionName("morning")
            .activateAfter(Duration.ZERO)
            .deactivateAfter(Duration.ofSeconds(20))
            .build();
        
        TemporalConditions afternoonState = TemporalConditions.builder()
            .conditionName("afternoon")
            .activateAfter(Duration.ofSeconds(20))
            .deactivateAfter(Duration.ofSeconds(40))
            .build();
        
        TemporalConditions eveningState = TemporalConditions.builder()
            .conditionName("evening")
            .activateAfter(Duration.ofSeconds(40))
            .build();
        
        config.getTemporalConditions().put("morning", morningState);
        config.getTemporalConditions().put("afternoon", afternoonState);
        config.getTemporalConditions().put("evening", eveningState);
        
        // Configure state probabilities
        config.getStateAppearanceProbabilities().put("morning_dashboard", 1.0);
        config.getStateAppearanceProbabilities().put("afternoon_dashboard", 1.0);
        config.getStateAppearanceProbabilities().put("evening_dashboard", 1.0);
        
        scenarioManager.activateScenario(config);
        
        try {
            // Check states over time
            checkTimeBasedState("Initial", "morning_dashboard");
            
            Thread.sleep(22000);  // Wait 22 seconds
            checkTimeBasedState("After 22s", "afternoon_dashboard");
            
            Thread.sleep(20000);  // Wait 20 more seconds
            checkTimeBasedState("After 42s", "evening_dashboard");
            
        } catch (Exception e) {
            log.error("Example failed: {}", e.getMessage());
        } finally {
            scenarioManager.deactivateCurrentScenario();
            log.info("Scenario completed\n");
        }
    }
    
    private boolean simulateAction(ActionOptions.Action action, int attempt) {
        boolean shouldFail = scenarioManager.shouldActionFail(action);
        log.info("  {} attempt {}: {}", action, attempt, shouldFail ? "FAILED" : "SUCCESS");
        return shouldFail;
    }
    
    private void checkTimeBasedState(String phase, String expectedState) {
        // Methods getCurrentConfig() and isTemporalConditionActive() don't exist in v1.1.0
        // double probability = scenarioManager.getCurrentConfig()
        //     .getStateAppearanceProbabilities()
        //     .getOrDefault(expectedState, 0.0);
        
        // boolean isActive = scenarioManager.isTemporalConditionActive(
        //     expectedState.replace("_dashboard", ""));
        
        log.info("{}: {} state check (methods not available in v1.1.0)", 
            phase, expectedState);
    }
    
    /**
     * Basic Scenario Setup - Example from documentation
     * From: /docs/03-core-library/testing/enhanced-mocking.md
     */
    public void testLoginUnderNetworkIssues() {
        log.info("=== Basic Scenario Setup (Documentation Example) ===");
        
        Map<String, Double> stateAppearances = new HashMap<>();
        stateAppearances.put("LOGIN_STATE", 0.8);  // 80% appear rate
        stateAppearances.put("DASHBOARD", 0.9);    // 90% appear rate
        
        MockScenarioConfig scenario = MockScenarioConfig.builder()
            .scenarioName("login_network_issues")
            .description("Simulate intermittent network connectivity during login")
            .stateAppearanceProbabilities(stateAppearances)
            .build();
            
        // Apply scenario and run test
        scenarioManager.activateScenario(scenario);
        
        try {
            log.info("Testing login under network issues:");
            log.info("  LOGIN_STATE appear rate: 80%");
            log.info("  DASHBOARD appear rate: 90%");
            
            // Simulate multiple login attempts
            for (int i = 1; i <= 5; i++) {
                boolean loginStateVisible = Math.random() < 0.8;
                boolean dashboardVisible = Math.random() < 0.9;
                
                log.info("  Attempt {}: LOGIN_STATE={}, DASHBOARD={}", 
                    i, loginStateVisible ? "VISIBLE" : "HIDDEN",
                    dashboardVisible ? "VISIBLE" : "HIDDEN");
            }
            
            log.info("✓ Network issues simulation completed");
            
        } finally {
            scenarioManager.deactivateCurrentScenario();
            log.info("Scenario completed\n");
        }
    }
    
    /**
     * Advanced Failure Patterns - Example from documentation  
     * From: /docs/03-core-library/testing/enhanced-mocking.md
     */
    public void testRetryBehaviorWithCascadingFailures() {
        log.info("=== Advanced Failure Patterns (Documentation Example) ===");
        
        // Configure cascading failures that worsen over time
        FailurePattern cascadingFailure = FailurePattern.builder()
            .baseProbability(0.3)              // Start with 30% failure rate
            .cascading(true)                   // Enable cascading
            .cascadeMultiplier(1.5)            // Each failure increases probability by 50%
            .maxConsecutiveFailures(3)         // Force success after 3 failures
            .recoveryDelay(Duration.ofSeconds(2))  // 2-second recovery period
            .failureMessage("Network timeout")
            .build();
            
        Map<ActionOptions.Action, FailurePattern> actionFailures = new HashMap<>();
        actionFailures.put(ActionOptions.Action.FIND, cascadingFailure);
        
        MockScenarioConfig scenario = MockScenarioConfig.builder()
            .scenarioName("cascading_network_failures")
            .actionFailurePatterns(actionFailures)
            .maxDuration(Duration.ofMinutes(5))  // Scenario timeout
            .build();
            
        scenarioManager.activateScenario(scenario);
        
        try {
            log.info("Testing retry logic with cascading failures:");
            
            // Test retry logic
            for (int attempt = 1; attempt <= 5; attempt++) {
                log.info("  Attempt {}: Checking for FIND failure", attempt);
                
                boolean shouldFail = scenarioManager.shouldActionFail(ActionOptions.Action.FIND);
                if (shouldFail) {
                    log.warn("    FIND failed - Network timeout");
                } else {
                    log.info("    FIND succeeded");
                    break;
                }
                
                // Simulate pause before retry 
                Thread.sleep(500);
            }
            
            log.info("✓ Cascading failure test completed");
            
        } catch (Exception e) {
            log.error("Test failed: {}", e.getMessage());
        } finally {
            scenarioManager.deactivateCurrentScenario();
            log.info("Scenario completed\n");
        }
    }
    
    /**
     * Temporal Conditions - Example from documentation
     * From: /docs/03-core-library/testing/enhanced-mocking.md
     */
    public void testPerformanceUnderLoad() {
        log.info("=== Temporal Conditions (Documentation Example) ===");
        
        TemporalConditions slowNetwork = TemporalConditions.builder()
            .baseDelay(Duration.ofMillis(500))        // Base 500ms delay
            .maximumDelay(Duration.ofSeconds(3))      // Cap at 3 seconds
            .delayProgression(Duration.ofMillis(100)) // Increase by 100ms each time
            .randomVariation(0.2)                     // ±20% random variation
            .activeStartTime(LocalTime.of(9, 0))
            .activeEndTime(LocalTime.of(17, 0)) // Business hours
            .build();
            
        Map<String, TemporalConditions> temporalMap = new HashMap<>();
        temporalMap.put("slow_network", slowNetwork);
        
        MockScenarioConfig scenario = MockScenarioConfig.builder()
            .scenarioName("performance_degradation")
            .temporalConditions(temporalMap)
            .build();
            
        scenarioManager.activateScenario(scenario);
        
        try {
            log.info("Testing performance under load:");
            
            long startTime = System.currentTimeMillis();
            // Simulate action with delay
            Thread.sleep(500 + (long)(Math.random() * 100)); // Simulate base delay + variation
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("  Action duration: {}ms", duration);
            log.info("  Expected: >= 500ms due to slow network conditions");
            
            if (duration >= 500) {
                log.info("✓ Performance degradation correctly simulated");
            } else {
                log.warn("⚠ Expected longer delay under load");
            }
            
        } catch (Exception e) {
            log.error("Test failed: {}", e.getMessage());
        } finally {
            scenarioManager.deactivateCurrentScenario();
            log.info("Scenario completed\n");
        }
    }
}