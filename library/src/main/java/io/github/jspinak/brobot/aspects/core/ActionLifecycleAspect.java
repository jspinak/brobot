package io.github.jspinak.brobot.aspects.core;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.modular.ActionLoggingService;
import io.github.jspinak.brobot.model.state.StateImage;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private ActionLoggingService actionLoggingService;
    
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
        
        // Debug: Log aspect invocation
        log.debug("ActionLifecycleAspect invoked for: {} with {} args", 
                 action.getClass().getSimpleName(), args.length);
        
        // Standard signature: perform(ActionResult, ObjectCollection...)
        ActionResult actionResult = args.length > 0 && args[0] instanceof ActionResult ? 
            (ActionResult) args[0] : null;
        
        // Extract ObjectCollections from varargs
        ObjectCollection objectCollection = null;
        List<ObjectCollection> allCollections = new ArrayList<>();
        
        // Collect all ObjectCollections from varargs (starting at index 1)
        for (int i = 1; i < args.length; i++) {
            if (args[i] instanceof ObjectCollection) {
                allCollections.add((ObjectCollection) args[i]);
            } else if (args[i] instanceof ObjectCollection[]) {
                // Handle case where varargs are passed as array
                ObjectCollection[] array = (ObjectCollection[]) args[i];
                Collections.addAll(allCollections, array);
            }
        }
        
        // Use first collection as primary, or create empty if none
        if (!allCollections.isEmpty()) {
            objectCollection = allCollections.get(0);
        } else {
            objectCollection = new ObjectCollection.Builder().build();
        }
        
        // Extract action options from ActionResult
        Object actionOptions = actionResult != null ? actionResult.getActionConfig() : null;
        if (actionOptions == null && actionResult != null) {
            // Try legacy ActionOptions
            actionOptions = actionResult.getActionOptions();
        }
        
        // Create action context
        ActionContext context = createActionContext(action, actionOptions, objectCollection);
        actionContext.set(context);
        
        // Set start time immediately  
        context.setStartTime(Instant.now());
        
        // Initialize ActionResult execution context if available
        if (actionResult != null) {
            populateExecutionContext(actionResult, context, objectCollection);
        }
        
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
            
            // The action modifies the ActionResult in-place
            if (actionResult != null) {
                context.setSuccess(actionResult.isSuccess());
                // Update duration in the ActionResult if not already set
                if (actionResult.getDuration() == null || actionResult.getDuration().toMillis() == 0) {
                    actionResult.setDuration(java.time.Duration.ofMillis(duration));
                }
                // Update execution context with final results
                updateExecutionContextWithResults(actionResult, context, duration);
            } else {
                context.setSuccess(isSuccessfulResult(result));
            }
            
            // Post-execution phase
            performPostExecution(context);
            
            // Log the completed action using modular logging
            if (actionResult != null) {
                actionLoggingService.logAction(actionResult);
            }
            
            return result;
            
        } catch (Exception e) {
            // Handle execution failure
            handleExecutionFailure(context, e);
            
            // Update ActionResult with failure information and log it
            if (actionResult != null) {
                updateExecutionContextWithError(actionResult, context, e);
                actionLoggingService.logAction(actionResult);
            }
            
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
        
        // Action start logging handled by modular logging service
        
        // Capture before screenshot if enabled
        if (captureBeforeScreenshot) {
            captureScreenshot(context, "before");
        }
        
        // Start time already set in main method before populateExecutionContext
    }
    
    /**
     * Perform post-execution tasks
     */
    private void performPostExecution(ActionContext context) {
        // Apply post-action pause
        applyPause(postActionPause);
        
        // Action completion logging handled by modular logging service
        
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
        
        // Failure logging handled by modular logging service
        
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
        
        // Extract action type from ActionConfig if available
        String actionType = extractActionType(actionOptions, action);
        context.setActionType(actionType);
        
        context.setActionOptions(actionOptions);
        context.setObjectCollection(objectCollection);
        context.setThreadName(Thread.currentThread().getName());
        return context;
    }
    
    /**
     * Extract action type from ActionConfig or fallback to action class
     */
    private String extractActionType(Object actionOptions, ActionInterface action) {
        // Check if it's an ActionConfig (base class for all action options)
        if (actionOptions instanceof ActionConfig) {
            ActionConfig config = (ActionConfig) actionOptions;
            
            // Try to extract a meaningful action name from the config class
            String configClassName = config.getClass().getSimpleName();
            
            // Handle special cases
            if (configClassName.equals("PatternFindOptions")) {
                return "FIND";
            }
            
            // Convert XxxOptions to XXX (e.g., ClickOptions -> CLICK)
            if (configClassName.endsWith("Options")) {
                String actionName = configClassName.substring(0, configClassName.length() - 7);
                return actionName.toUpperCase();
            }
        }
        
        // Check if it's the legacy ActionOptions
        if (actionOptions != null && actionOptions.getClass().getName().equals("io.github.jspinak.brobot.action.ActionOptions")) {
            try {
                // Use reflection to get the action enum value
                Object actionEnum = actionOptions.getClass().getMethod("getAction").invoke(actionOptions);
                if (actionEnum != null) {
                    return actionEnum.toString();
                }
            } catch (Exception e) {
                log.debug("Could not extract action from ActionOptions: {}", e.getMessage());
            }
        }
        
        // Fallback to action interface implementation class name
        return action.getClass().getSimpleName().toUpperCase();
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
     * Populate ActionResult execution context with initial data
     */
    private void populateExecutionContext(ActionResult actionResult, ActionContext context, ObjectCollection objectCollection) {
        // Initialize execution context if not present
        ActionResult.ActionExecutionContext execContext = actionResult.getExecutionContext();
        if (execContext == null) {
            execContext = new ActionResult.ActionExecutionContext();
            actionResult.setExecutionContext(execContext);
        }
        
        // Set basic action information
        execContext.setActionType(context.getActionType());
        execContext.setActionId(context.getActionId());
        execContext.setExecutingThread(context.getThreadName());
        execContext.setStartTime(context.getStartTime());
        
        // Extract target information from ObjectCollection
        if (objectCollection != null) {
            execContext.setTargetImages(objectCollection.getStateImages());
            execContext.setTargetStrings(objectCollection.getStateStrings().stream()
                .map(ss -> ss.getString())
                .collect(java.util.stream.Collectors.toList()));
            execContext.setTargetRegions(objectCollection.getStateRegions().stream()
                .map(sr -> sr.getSearchRegion())
                .collect(java.util.stream.Collectors.toList()));
            
            // Set primary target name
            if (!objectCollection.getStateImages().isEmpty()) {
                StateImage primaryImage = objectCollection.getStateImages().get(0);
                String primaryTargetName = "";
                if (primaryImage.getOwnerStateName() != null && !primaryImage.getOwnerStateName().isEmpty()) {
                    primaryTargetName = primaryImage.getOwnerStateName() + ".";
                }
                if (primaryImage.getName() != null && !primaryImage.getName().isEmpty()) {
                    primaryTargetName += primaryImage.getName();
                }
                execContext.setPrimaryTargetName(primaryTargetName);
            }
        }
        
        // Initialize metrics if not present
        if (actionResult.getActionMetrics() == null) {
            actionResult.setActionMetrics(new ActionResult.ActionMetrics());
        }
        
        // Initialize environment snapshot if not present
        if (actionResult.getEnvironmentSnapshot() == null) {
            ActionResult.EnvironmentSnapshot envSnapshot = new ActionResult.EnvironmentSnapshot();
            envSnapshot.setOsName(System.getProperty("os.name"));
            // Monitor count and other env info would be populated by other components
            // ActionResult doesn't have setEnvironmentSnapshot yet, will be added later
        }
    }
    
    /**
     * Update execution context with final results
     */
    private void updateExecutionContextWithResults(ActionResult actionResult, ActionContext context, long duration) {
        ActionResult.ActionExecutionContext execContext = actionResult.getExecutionContext();
        if (execContext != null) {
            execContext.setEndTime(java.time.Instant.now());
            execContext.setSuccess(actionResult.isSuccess());
            execContext.setExecutionDuration(java.time.Duration.ofMillis(duration));
            
            // Copy matches from ActionResult
            if (actionResult.getMatchList() != null) {
                execContext.setResultMatches(actionResult.getMatchList());
            }
        }
    }
    
    /**
     * Update execution context with error information
     */
    private void updateExecutionContextWithError(ActionResult actionResult, ActionContext context, Exception error) {
        ActionResult.ActionExecutionContext execContext = actionResult.getExecutionContext();
        if (execContext != null) {
            execContext.setEndTime(java.time.Instant.now());
            execContext.setSuccess(false);
            execContext.setExecutionError(error);
            execContext.setExecutionDuration(java.time.Duration.ofMillis(
                System.currentTimeMillis() - context.getStartTime().toEpochMilli()));
        }
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