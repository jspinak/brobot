package io.github.jspinak.brobot.runner.execution.safety;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.execution.context.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides safety mechanisms for automation executions.
 * 
 * This service implements safety checks and controls to prevent:
 * - Runaway mouse movements
 * - Excessive key presses
 * - Unintended system interactions
 * - Resource exhaustion
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ExecutionSafetyService implements DiagnosticCapable {
    
    // Safety configuration constants
    private static final int MAX_ACTIONS_PER_SECOND = 50;
    private static final int MAX_CONSECUTIVE_FAILURES = 10;
    private static final long ACTION_RATE_WINDOW = 1000; // 1 second in millis
    private static final Rectangle SAFE_SCREEN_BOUNDS = getSafeScreenBounds();
    
    // Safety state
    private final AtomicBoolean safetyEnabled = new AtomicBoolean(true);
    private final AtomicBoolean emergencyStop = new AtomicBoolean(false);
    
    // Action tracking
    private final Map<String, ActionTracker> actionTrackers = new ConcurrentHashMap<>();
    
    // Failure tracking
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    public ExecutionSafetyService() {
        log.info("ExecutionSafetyService initialized with safety bounds: {}", SAFE_SCREEN_BOUNDS);
    }
    
    /**
     * Checks if an action is safe to perform.
     * 
     * @param context execution context
     * @param actionType type of action (e.g., "mouse_move", "key_press")
     * @return true if the action is allowed
     */
    public boolean checkActionSafety(ExecutionContext context, String actionType) {
        if (!safetyEnabled.get() || !context.getOptions().isSafeMode()) {
            return true; // Safety checks disabled
        }
        
        if (emergencyStop.get()) {
            log.warn("Action blocked due to emergency stop - Type: {}, Task: {}",
                    actionType, context.getTaskName());
            return false;
        }
        
        // Check action rate
        ActionTracker tracker = actionTrackers.computeIfAbsent(
                context.getId(),
                k -> new ActionTracker()
        );
        
        if (!tracker.checkRate(actionType)) {
            log.warn("Action rate limit exceeded - Type: {}, Task: {}, Rate: {}/s",
                    actionType, context.getTaskName(), tracker.getCurrentRate(actionType));
            return false;
        }
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Safety check passed - Type: {}, Task: {}, Rate: {}/s",
                    actionType, context.getTaskName(), tracker.getCurrentRate(actionType));
        }
        
        return true;
    }
    
    /**
     * Validates mouse coordinates are within safe bounds.
     * 
     * @param x mouse x coordinate
     * @param y mouse y coordinate
     * @return true if coordinates are safe
     */
    public boolean validateMousePosition(int x, int y) {
        if (!safetyEnabled.get()) {
            return true;
        }
        
        boolean inBounds = SAFE_SCREEN_BOUNDS.contains(x, y);
        
        if (!inBounds) {
            log.warn("Mouse position outside safe bounds - X: {}, Y: {}, Bounds: {}",
                    x, y, SAFE_SCREEN_BOUNDS);
        }
        
        return inBounds;
    }
    
    /**
     * Records an execution failure.
     * 
     * @param context execution context
     * @param error the error that occurred
     */
    public void recordFailure(ExecutionContext context, Throwable error) {
        int failures = consecutiveFailures.incrementAndGet();
        
        log.error("Execution failure recorded - Task: {}, Consecutive failures: {}",
                context.getTaskName(), failures, error);
        
        if (failures >= MAX_CONSECUTIVE_FAILURES) {
            triggerEmergencyStop("Maximum consecutive failures reached: " + failures);
        }
    }
    
    /**
     * Records a successful execution.
     * 
     * @param context execution context
     */
    public void recordSuccess(ExecutionContext context) {
        consecutiveFailures.set(0);
        log.debug("Execution success recorded - Task: {}", context.getTaskName());
    }
    
    /**
     * Triggers an emergency stop of all executions.
     * 
     * @param reason the reason for the emergency stop
     */
    public void triggerEmergencyStop(String reason) {
        emergencyStop.set(true);
        log.error("EMERGENCY STOP TRIGGERED - Reason: {}", reason);
        
        // Could trigger additional safety actions here:
        // - Move mouse to safe position
        // - Release all pressed keys
        // - Notify administrators
    }
    
    /**
     * Clears the emergency stop state.
     */
    public void clearEmergencyStop() {
        emergencyStop.set(false);
        consecutiveFailures.set(0);
        log.info("Emergency stop cleared");
    }
    
    /**
     * Checks if emergency stop is active.
     */
    public boolean isEmergencyStopped() {
        return emergencyStop.get();
    }
    
    /**
     * Enables or disables safety checks.
     */
    public void setSafetyEnabled(boolean enabled) {
        safetyEnabled.set(enabled);
        log.info("Safety checks {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Cleans up tracking data for a completed execution.
     */
    public void cleanupExecution(String executionId) {
        actionTrackers.remove(executionId);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("safetyEnabled", safetyEnabled.get());
        states.put("emergencyStop", emergencyStop.get());
        states.put("consecutiveFailures", consecutiveFailures.get());
        states.put("activeTrackers", actionTrackers.size());
        states.put("maxActionsPerSecond", MAX_ACTIONS_PER_SECOND);
        states.put("safeBounds", SAFE_SCREEN_BOUNDS.toString());
        
        return DiagnosticInfo.builder()
                .component("ExecutionSafetyService")
                .states(states)
                .build();
    }
    
    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }
    
    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Gets safe screen bounds (leaving margin from edges).
     */
    private static Rectangle getSafeScreenBounds() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            
            // Leave 10-pixel margin from screen edges
            return new Rectangle(
                    bounds.x + 10,
                    bounds.y + 10,
                    bounds.width - 20,
                    bounds.height - 20
            );
        } catch (Exception e) {
            log.warn("Could not determine screen bounds, using defaults", e);
            return new Rectangle(10, 10, 1900, 1060); // Default safe bounds
        }
    }
    
    /**
     * Internal class for tracking action rates.
     */
    private static class ActionTracker {
        private final Map<String, RateWindow> actionWindows = new ConcurrentHashMap<>();
        
        public boolean checkRate(String actionType) {
            RateWindow window = actionWindows.computeIfAbsent(
                    actionType,
                    k -> new RateWindow(ACTION_RATE_WINDOW)
            );
            
            return window.addAction() <= MAX_ACTIONS_PER_SECOND;
        }
        
        public int getCurrentRate(String actionType) {
            RateWindow window = actionWindows.get(actionType);
            return window != null ? window.getCurrentRate() : 0;
        }
    }
    
    /**
     * Internal class for tracking actions within a time window.
     */
    private static class RateWindow {
        private final long windowSize;
        private final Map<Long, AtomicInteger> buckets = new ConcurrentHashMap<>();
        
        public RateWindow(long windowSize) {
            this.windowSize = windowSize;
        }
        
        public int addAction() {
            long now = System.currentTimeMillis();
            long bucket = now / 100; // 100ms buckets
            
            buckets.computeIfAbsent(bucket, k -> new AtomicInteger(0)).incrementAndGet();
            
            // Clean old buckets
            long cutoff = (now - windowSize) / 100;
            buckets.entrySet().removeIf(entry -> entry.getKey() < cutoff);
            
            return getCurrentRate();
        }
        
        public int getCurrentRate() {
            return buckets.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum();
        }
    }
}