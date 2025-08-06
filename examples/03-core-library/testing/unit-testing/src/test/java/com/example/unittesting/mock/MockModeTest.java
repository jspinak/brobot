package com.example.unittesting.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests demonstrating mock mode usage.
 */
class MockModeTest {
    
    private MockActionRecorder actionRecorder;
    
    @BeforeEach
    void setUp() {
        actionRecorder = new MockActionRecorder();
        // In real Brobot, would enable mock mode here
    }
    
    @AfterEach
    void tearDown() {
        actionRecorder.clear();
        // In real Brobot, would disable mock mode here
    }
    
    @Test
    @DisplayName("Should record actions in mock mode")
    void testActionRecording() {
        // Simulate actions
        actionRecorder.recordAction("CLICK", "login_button", true);
        actionRecorder.recordAction("TYPE", "username_field", true);
        actionRecorder.recordAction("TYPE", "password_field", true);
        actionRecorder.recordAction("CLICK", "submit_button", true);
        
        List<MockAction> actions = actionRecorder.getRecordedActions();
        
        assertThat(actions).hasSize(4);
        assertThat(actions.get(0).getActionType()).isEqualTo("CLICK");
        assertThat(actions.get(0).getTarget()).isEqualTo("login_button");
        assertThat(actions.get(3).getActionType()).isEqualTo("CLICK");
    }
    
    @Test
    @DisplayName("Should simulate failures in mock mode")
    void testFailureSimulation() {
        // Configure to fail specific actions
        actionRecorder.configureFailure("CLICK", "unreliable_button", 0.5);
        
        // Simulate multiple attempts
        int successCount = 0;
        for (int i = 0; i < 10; i++) {
            boolean success = actionRecorder.simulateAction("CLICK", "unreliable_button");
            if (success) successCount++;
        }
        
        // Should have some successes and some failures
        assertThat(successCount).isGreaterThan(0).isLessThan(10);
    }
    
    @Test
    @DisplayName("Should verify action sequences")
    void testActionSequenceVerification() {
        // Record a sequence
        actionRecorder.recordAction("CLICK", "menu_button", true);
        actionRecorder.recordAction("CLICK", "settings_option", true);
        actionRecorder.recordAction("TYPE", "search_field", true);
        
        // Verify sequence
        boolean sequenceCorrect = actionRecorder.verifySequence(
            new String[]{"CLICK", "CLICK", "TYPE"},
            new String[]{"menu_button", "settings_option", "search_field"}
        );
        
        assertThat(sequenceCorrect).isTrue();
    }
    
    /**
     * Simple mock action recorder for demonstration.
     */
    static class MockActionRecorder {
        private final List<MockAction> recordedActions = new ArrayList<>();
        private final List<FailureConfig> failureConfigs = new ArrayList<>();
        
        void recordAction(String actionType, String target, boolean success) {
            recordedActions.add(new MockAction(actionType, target, success));
        }
        
        void configureFailure(String actionType, String target, double failureRate) {
            failureConfigs.add(new FailureConfig(actionType, target, failureRate));
        }
        
        boolean simulateAction(String actionType, String target) {
            // Check if this action should fail
            for (FailureConfig config : failureConfigs) {
                if (config.matches(actionType, target)) {
                    boolean success = Math.random() > config.failureRate;
                    recordAction(actionType, target, success);
                    return success;
                }
            }
            recordAction(actionType, target, true);
            return true;
        }
        
        boolean verifySequence(String[] expectedTypes, String[] expectedTargets) {
            if (recordedActions.size() < expectedTypes.length) return false;
            
            for (int i = 0; i < expectedTypes.length; i++) {
                MockAction action = recordedActions.get(i);
                if (!action.actionType.equals(expectedTypes[i]) ||
                    !action.target.equals(expectedTargets[i])) {
                    return false;
                }
            }
            return true;
        }
        
        List<MockAction> getRecordedActions() {
            return new ArrayList<>(recordedActions);
        }
        
        void clear() {
            recordedActions.clear();
            failureConfigs.clear();
        }
    }
    
    /**
     * Represents a recorded mock action.
     */
    static class MockAction {
        private final String actionType;
        private final String target;
        private final boolean success;
        
        MockAction(String actionType, String target, boolean success) {
            this.actionType = actionType;
            this.target = target;
            this.success = success;
        }
        
        String getActionType() { return actionType; }
        String getTarget() { return target; }
        boolean isSuccess() { return success; }
    }
    
    /**
     * Configuration for action failures.
     */
    static class FailureConfig {
        private final String actionType;
        private final String target;
        private final double failureRate;
        
        FailureConfig(String actionType, String target, double failureRate) {
            this.actionType = actionType;
            this.target = target;
            this.failureRate = failureRate;
        }
        
        boolean matches(String actionType, String target) {
            return this.actionType.equals(actionType) && this.target.equals(target);
        }
    }
}