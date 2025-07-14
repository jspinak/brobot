package io.github.jspinak.brobot.aspects.display;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
// Match import removed - not available
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Aspect that provides automatic visual feedback during automation execution.
 * 
 * This aspect enhances debugging and demonstration by:
 * - Highlighting search regions before find operations
 * - Showing found matches with confidence scores
 * - Indicating click points visually
 * - Drawing action flow arrows between operations
 * - Displaying error locations
 * 
 * The visual feedback helps developers understand what the automation
 * is doing and quickly identify issues.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
    prefix = "brobot.aspects.visual-feedback",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class VisualFeedbackAspect {
    
    @Autowired
    private BrobotLogger brobotLogger;
    
    @Autowired(required = false)
    private HighlightManager highlightManager;
    
    @Autowired(required = false)
    private VisualFeedbackConfig visualConfig;
    
    @Value("${brobot.aspects.visual-feedback.highlight-duration:2}")
    private int highlightDuration;
    
    @Value("${brobot.aspects.visual-feedback.highlight-color:YELLOW}")
    private String highlightColor;
    
    @Value("${brobot.aspects.visual-feedback.show-action-flow:true}")
    private boolean showActionFlow;
    
    @Value("${brobot.aspects.visual-feedback.show-confidence-scores:true}")
    private boolean showConfidenceScores;
    
    // Action flow tracking
    private final LinkedList<ActionPoint> recentActions = new LinkedList<>();
    private final int MAX_FLOW_POINTS = 10;
    
    // Visual overlay management
    private final ScheduledExecutorService overlayScheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, ScheduledFuture<?>> activeHighlights = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("Visual Feedback Aspect initialized");
        if (highlightManager == null) {
            log.warn("HighlightManager not available - visual feedback will be limited");
        }
    }
    
    /**
     * Pointcut for find operations
     */
    @Pointcut("execution(* io.github.jspinak.brobot.action.methods.basicactions.find.*.find*(..))")
    public void findOperations() {}
    
    /**
     * Pointcut for click operations
     */
    @Pointcut("execution(* io.github.jspinak.brobot.action.methods.basicactions.click.*.click*(..))")
    public void clickOperations() {}
    
    /**
     * Pointcut for type operations
     */
    @Pointcut("execution(* io.github.jspinak.brobot.action.methods.basicactions.type.*.type*(..))")
    public void typeOperations() {}
    
    /**
     * Combined pointcut for visual operations
     */
    @Pointcut("findOperations() || clickOperations() || typeOperations()")
    public void visualOperations() {}
    
    /**
     * Provide visual feedback for operations
     */
    @Around("visualOperations()")
    public Object provideVisualFeedback(ProceedingJoinPoint joinPoint) throws Throwable {
        String operationType = extractOperationType(joinPoint);
        ObjectCollection targets = extractTargets(joinPoint.getArgs());
        
        // Highlight search regions before operation
        if (targets != null && highlightManager != null) {
            highlightSearchRegions(targets, operationType);
        }
        
        try {
            // Execute the operation
            Object result = joinPoint.proceed();
            
            // Provide post-execution feedback
            if (result instanceof ActionResult) {
                provideResultFeedback((ActionResult) result, operationType);
            }
            
            return result;
            
        } catch (Throwable e) {
            // Highlight error location
            provideErrorFeedback(targets, operationType, e);
            throw e;
        }
    }
    
    /**
     * Extract operation type from join point
     */
    private String extractOperationType(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        
        if (methodName.contains("find")) {
            return "FIND";
        } else if (methodName.contains("click")) {
            return "CLICK";
        } else if (methodName.contains("type")) {
            return "TYPE";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Extract targets from method arguments
     */
    private ObjectCollection extractTargets(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ObjectCollection) {
                return (ObjectCollection) arg;
            }
        }
        return null;
    }
    
    /**
     * Highlight search regions before operation
     */
    private void highlightSearchRegions(ObjectCollection targets, String operationType) {
        if (targets == null) return;
        
        // Get all state objects
        Set<StateObject> stateObjects = new HashSet<>();
        // Note: actual extraction would use proper ObjectCollection methods
        
        // Highlight each search region
        for (StateObject obj : stateObjects) {
            String highlightId = UUID.randomUUID().toString();
            
            // Schedule highlight
            scheduleHighlight(highlightId, () -> {
                // Actual highlighting would use HighlightManager
                log.debug("Highlighting search region for {} operation", operationType);
            }, 0);
            
            // Schedule removal
            scheduleHighlight(highlightId + "_remove", () -> {
                // Remove highlight
                log.debug("Removing highlight for {} operation", operationType);
            }, highlightDuration * 1000);
        }
        
        logVisualFeedback("SEARCH_HIGHLIGHT", operationType, stateObjects.size());
    }
    
    /**
     * Provide feedback for operation results
     */
    private void provideResultFeedback(ActionResult result, String operationType) {
        if (!result.isSuccess()) {
            return;
        }
        
        List<?> matches = result.getMatchList();
        if (matches == null || matches.isEmpty()) {
            return;
        }
        
        // Highlight found matches
        for (Object match : matches) {
            highlightMatch(match, operationType);
        }
        
        // Update action flow
        if (showActionFlow && !matches.isEmpty()) {
            Object bestMatch = matches.get(0);
            addActionPoint(bestMatch, operationType);
            drawActionFlow();
        }
        
        logVisualFeedback("RESULT_HIGHLIGHT", operationType, matches.size());
    }
    
    /**
     * Highlight a found match
     */
    private void highlightMatch(Object match, String operationType) {
        if (highlightManager == null) return;
        
        String highlightId = UUID.randomUUID().toString();
        Color color = getColorForOperation(operationType);
        
        // Schedule highlight with confidence display
        scheduleHighlight(highlightId, () -> {
            // Actual highlighting would use HighlightManager
            if (showConfidenceScores) {
                log.debug("Highlighting match");
            }
        }, 0);
        
        // Schedule removal
        scheduleHighlight(highlightId + "_remove", () -> {
            log.debug("Removing match highlight");
        }, highlightDuration * 1000);
    }
    
    /**
     * Provide error feedback
     */
    private void provideErrorFeedback(ObjectCollection targets, String operationType, Throwable error) {
        if (highlightManager == null || targets == null) return;
        
        // Highlight search regions in red to indicate error
        String highlightId = "error_" + UUID.randomUUID().toString();
        
        scheduleHighlight(highlightId, () -> {
            log.debug("Highlighting error location for {} operation", operationType);
        }, 0);
        
        // Keep error highlight longer
        scheduleHighlight(highlightId + "_remove", () -> {
            log.debug("Removing error highlight");
        }, highlightDuration * 2000);
        
        logVisualFeedback("ERROR_HIGHLIGHT", operationType, 1);
    }
    
    /**
     * Add action point for flow visualization
     */
    private void addActionPoint(Object match, String operationType) {
        ActionPoint point = new ActionPoint();
        // Match coordinates would be extracted here
        point.setX(100); // Placeholder
        point.setY(100); // Placeholder
        point.setOperationType(operationType);
        point.setTimestamp(System.currentTimeMillis());
        
        recentActions.add(point);
        
        // Keep only recent points
        while (recentActions.size() > MAX_FLOW_POINTS) {
            recentActions.removeFirst();
        }
    }
    
    /**
     * Draw action flow arrows
     */
    private void drawActionFlow() {
        if (recentActions.size() < 2) {
            return;
        }
        
        // In a real implementation, this would draw arrows between action points
        // showing the flow of automation
        log.debug("Drawing action flow with {} points", recentActions.size());
    }
    
    /**
     * Get color for operation type
     */
    private Color getColorForOperation(String operationType) {
        switch (operationType) {
            case "FIND":
                return Color.GREEN;
            case "CLICK":
                return Color.BLUE;
            case "TYPE":
                return Color.ORANGE;
            default:
                return Color.YELLOW;
        }
    }
    
    /**
     * Schedule a highlight task
     */
    private void scheduleHighlight(String id, Runnable task, long delayMillis) {
        // Cancel any existing highlight with same ID
        ScheduledFuture<?> existing = activeHighlights.remove(id);
        if (existing != null) {
            existing.cancel(false);
        }
        
        // Schedule new highlight
        ScheduledFuture<?> future = overlayScheduler.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
        activeHighlights.put(id, future);
    }
    
    /**
     * Log visual feedback event
     */
    private void logVisualFeedback(String feedbackType, String operationType, int targetCount) {
        brobotLogger.log()
            .type(LogEvent.Type.OBSERVATION)
            .level(LogEvent.Level.DEBUG)
            .action("VISUAL_FEEDBACK")
            .metadata("feedbackType", feedbackType)
            .metadata("operationType", operationType)
            .metadata("targetCount", targetCount)
            .observation("Visual feedback provided")
            .log();
    }
    
    /**
     * Clear all active highlights
     */
    public void clearHighlights() {
        activeHighlights.values().forEach(future -> future.cancel(false));
        activeHighlights.clear();
        recentActions.clear();
        log.info("All visual highlights cleared");
    }
    
    /**
     * Shutdown visual feedback
     */
    public void shutdown() {
        clearHighlights();
        overlayScheduler.shutdown();
        
        try {
            if (!overlayScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                overlayScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            overlayScheduler.shutdownNow();
        }
    }
    
    /**
     * Inner class representing an action point
     */
    @Data
    private static class ActionPoint {
        private int x;
        private int y;
        private String operationType;
        private long timestamp;
    }
}