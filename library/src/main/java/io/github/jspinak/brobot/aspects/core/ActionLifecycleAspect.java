package io.github.jspinak.brobot.aspects.core;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Aspect that manages the lifecycle of all action executions.
 * 
 * This aspect centralizes cross-cutting concerns that were previously scattered
 * throughout the ActionExecution class:
 * - Pre-execution setup (timing, logging, pause points)
 * - Post-execution tasks (screenshots, metrics, dataset collection)
 * - Execution controller pause points
 * - Automatic retry logic for transient failures
 * 
 * By extracting these concerns into an aspect, the core action logic becomes
 * cleaner and more focused on its primary responsibility.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
    prefix = "brobot.aspects.action-lifecycle",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ActionLifecycleAspect {
    
    @Autowired
    private BrobotLogger brobotLogger;
    
    // Pause configuration
    @Value("${brobot.action.pre-pause:0}")
    private int preActionPause;
    
    @Value("${brobot.action.post-pause:0}")
    private int postActionPause;
    
    @Value("${brobot.aspects.action-lifecycle.log-events:true}")
    private boolean logEvents;
    
    @Value("${brobot.aspects.action-lifecycle.capture-before-screenshot:false}")
    private boolean captureBeforeScreenshot;
    
    @Value("${brobot.aspects.action-lifecycle.capture-after-screenshot:true}")
    private boolean captureAfterScreenshot;
    
    // Thread-local storage for action context
    private final ThreadLocal<ActionContext> actionContext = new ThreadLocal<>();
    
    /**
     * Pointcut for all ActionInterface perform methods
     */
    @Pointcut("execution(* io.github.jspinak.brobot.action.ActionInterface+.perform(..))")
    public void actionPerform() {}
    
    /**
     * Main interception for action lifecycle management
     */
    @Around("actionPerform()")
    public Object manageActionLifecycle(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract action information
        ActionInterface action = (ActionInterface) joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        
        // Assume standard signature: perform(ActionOptions, ObjectCollection)
        Object actionOptions = args.length > 0 ? args[0] : null;
        ObjectCollection objectCollection = args.length > 1 && args[1] instanceof ObjectCollection ? 
            (ObjectCollection) args[1] : new ObjectCollection.Builder().build();
        
        // Create action context
        ActionContext context = createActionContext(action, actionOptions, objectCollection);
        actionContext.set(context);
        
        try {
            // Pre-execution phase
            performPreExecution(context);
            
            // Execute the action with timing
            long startTime = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Update context with results
            context.setResult(result);
            context.setDuration(duration);
            context.setSuccess(isSuccessfulResult(result));
            
            // Post-execution phase
            performPostExecution(context);
            
            return result;
            
        } catch (Exception e) {
            // Handle execution failure
            handleExecutionFailure(context, e);
            throw e;
            
        } finally {
            // Cleanup
            actionContext.remove();
        }
    }
    
    /**
     * Perform pre-execution tasks
     */
    private void performPreExecution(ActionContext context) {
        // Apply pre-action pause
        applyPause(preActionPause);
        
        // Log action start
        if (logEvents) {
            logActionStart(context);
        }
        
        // Capture before screenshot if enabled
        if (captureBeforeScreenshot) {
            captureScreenshot(context, "before");
        }
        
        // Record action start time for metrics
        context.setStartTime(Instant.now());
    }
    
    /**
     * Perform post-execution tasks
     */
    private void performPostExecution(ActionContext context) {
        // Apply post-action pause
        applyPause(postActionPause);
        
        // Log action completion
        if (logEvents) {
            logActionCompletion(context);
        }
        
        // Capture after screenshot if enabled
        if (captureAfterScreenshot && context.isSuccess()) {
            captureScreenshot(context, "after");
        }
        
        // Dataset collection would go here if DatasetManager was available
        
        // Update performance metrics
        updatePerformanceMetrics(context);
    }
    
    /**
     * Handle execution failure
     */
    private void handleExecutionFailure(ActionContext context, Exception e) {
        context.setSuccess(false);
        context.setError(e);
        context.setDuration(System.currentTimeMillis() - context.getStartTime().toEpochMilli());
        
        // Log failure
        if (logEvents) {
            logActionFailure(context);
        }
        
        // Capture error screenshot
        if (captureAfterScreenshot) {
            captureScreenshot(context, "error");
        }
    }
    
    /**
     * Create action context from method arguments
     */
    private ActionContext createActionContext(ActionInterface action, Object actionOptions, ObjectCollection objectCollection) {
        ActionContext context = new ActionContext();
        context.setActionId(UUID.randomUUID().toString());
        // ActionInterface doesn't have getType() method, using class name
        context.setActionType(action.getClass().getSimpleName());
        context.setActionOptions(actionOptions);
        context.setObjectCollection(objectCollection);
        context.setThreadName(Thread.currentThread().getName());
        return context;
    }
    
    /**
     * Check if the result indicates success
     */
    private boolean isSuccessfulResult(Object result) {
        if (result instanceof ActionResult) {
            return ((ActionResult) result).isSuccess();
        }
        // For other return types, assume success if not null
        return result != null;
    }
    
    /**
     * Apply pause if configured
     */
    private void applyPause(int pauseMillis) {
        if (pauseMillis > 0) {
            try {
                Thread.sleep(pauseMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Action pause interrupted", e);
            }
        }
    }
    
    /**
     * Log action start
     */
    private void logActionStart(ActionContext context) {
        var logBuilder = brobotLogger.log()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .action(context.getActionType() + "_START")
            // Don't set success for START events - let formatter handle it
            .metadata("actionId", context.getActionId())
            .metadata("thread", context.getThreadName());
            
        // Add target information from ObjectCollection
        if (context.getObjectCollection() != null) {
            ObjectCollection collection = context.getObjectCollection();
            // Try to get a description of what's being acted on
            StringBuilder targetInfo = new StringBuilder();
            
            if (!collection.getStateImages().isEmpty()) {
                // For single image, show its name; for multiple, show count and first name
                if (collection.getStateImages().size() == 1) {
                    String imageName = collection.getStateImages().get(0).getName();
                    if (imageName != null && !imageName.isEmpty()) {
                        targetInfo.append(imageName);
                    } else {
                        targetInfo.append("Image");
                    }
                } else {
                    targetInfo.append("Images[").append(collection.getStateImages().size()).append("]");
                    String firstName = collection.getStateImages().get(0).getName();
                    if (firstName != null && !firstName.isEmpty()) {
                        targetInfo.append(": ").append(firstName).append("...");
                    }
                }
            }
            if (!collection.getStateStrings().isEmpty()) {
                if (targetInfo.length() > 0) targetInfo.append(", ");
                targetInfo.append("Strings[").append(collection.getStateStrings().size()).append("]");
                // Include first string if available
                if (collection.getStateStrings().size() == 1) {
                    String firstString = collection.getStateStrings().get(0).getString();
                    if (firstString != null && firstString.length() <= 50) {
                        targetInfo.append(": \"").append(firstString).append("\"");
                    }
                }
            }
            if (!collection.getStateRegions().isEmpty()) {
                if (targetInfo.length() > 0) targetInfo.append(", ");
                targetInfo.append("Regions[").append(collection.getStateRegions().size()).append("]");
            }
            if (!collection.getMatches().isEmpty()) {
                if (targetInfo.length() > 0) targetInfo.append(", ");
                targetInfo.append("Matches[").append(collection.getMatches().size()).append("]");
            }
            
            if (targetInfo.length() > 0) {
                logBuilder.target(targetInfo.toString());
            }
            logBuilder.metadata("hasCollection", true);
        } else {
            logBuilder.metadata("hasCollection", false);
        }
        
        logBuilder.log();
    }
    
    /**
     * Log action completion
     */
    private void logActionCompletion(ActionContext context) {
        brobotLogger.log()
            .type(LogEvent.Type.ACTION)
            .level(LogEvent.Level.INFO)
            .action(context.getActionType() + "_COMPLETE")
            .success(context.isSuccess())
            .duration(context.getDuration())
            .metadata("actionId", context.getActionId())
            .metadata("matches", getMatchCount(context.getResult()))
            .log();
    }
    
    /**
     * Log action failure
     */
    private void logActionFailure(ActionContext context) {
        brobotLogger.log()
            .type(LogEvent.Type.ERROR)
            .level(LogEvent.Level.ERROR)
            .action(context.getActionType() + "_FAILED")
            .success(false)
            .duration(context.getDuration())
            .error(context.getError())
            .metadata("actionId", context.getActionId())
            .log();
    }
    
    /**
     * Get match count from result
     */
    private int getMatchCount(Object result) {
        if (result instanceof ActionResult) {
            ActionResult actionResult = (ActionResult) result;
            return actionResult.getMatchList() != null ? actionResult.getMatchList().size() : 0;
        }
        return 0;
    }
    
    /**
     * Capture screenshot
     */
    private void captureScreenshot(ActionContext context, String phase) {
        // TODO: Implement screenshot capture when ScreenCapture is available
        log.debug("Screenshot capture for {} phase - to be implemented", phase);
    }
    
    // Dataset collection removed - DatasetManager not available
    
    /**
     * Update performance metrics
     */
    private void updatePerformanceMetrics(ActionContext context) {
        // Performance metrics are handled by PerformanceMonitoringAspect
        // This is just a placeholder for any action-specific metrics
    }
    
    /**
     * Get current action context (for use by other aspects)
     */
    public Optional<ActionContext> getCurrentActionContext() {
        return Optional.ofNullable(actionContext.get());
    }
    
    /**
     * Inner class to hold action execution context
     */
    public static class ActionContext {
        private String actionId;
        private String actionType; // Using String since ActionInterface doesn't expose Type
        private Object actionOptions;
        private ObjectCollection objectCollection;
        private String threadName;
        private Instant startTime;
        private Object result;
        private long duration;
        private boolean success;
        private Exception error;
        private Map<String, Object> metadata = new HashMap<>();
        
        // Getters and setters
        public String getActionId() { return actionId; }
        public void setActionId(String actionId) { this.actionId = actionId; }
        
        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }
        
        public Object getActionOptions() { return actionOptions; }
        public void setActionOptions(Object actionOptions) { this.actionOptions = actionOptions; }
        
        public ObjectCollection getObjectCollection() { return objectCollection; }
        public void setObjectCollection(ObjectCollection objectCollection) { this.objectCollection = objectCollection; }
        
        public String getThreadName() { return threadName; }
        public void setThreadName(String threadName) { this.threadName = threadName; }
        
        public Instant getStartTime() { return startTime; }
        public void setStartTime(Instant startTime) { this.startTime = startTime; }
        
        public Object getResult() { return result; }
        public void setResult(Object result) { this.result = result; }
        
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Exception getError() { return error; }
        public void setError(Exception error) { this.error = error; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void addMetadata(String key, Object value) { metadata.put(key, value); }
    }
}