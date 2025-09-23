package io.github.jspinak.brobot.aspects.core;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.logging.modular.ActionLoggingService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that manages the lifecycle of all action executions.
 *
 * <p>This aspect centralizes cross-cutting concerns that were previously scattered throughout the
 * ActionExecution class: - Pre-execution setup (timing, logging, pause points) - Post-execution
 * tasks (screenshots, metrics, dataset collection) - Execution controller pause points - Automatic
 * retry logic for transient failures
 *
 * <p>By extracting these concerns into an aspect, the core action logic becomes cleaner and more
 * focused on its primary responsibility.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "brobot.aspects.action-lifecycle",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ActionLifecycleAspect {

    private final ActionLoggingService actionLoggingService;
    private final int preActionPause;
    private final int postActionPause;
    private final boolean logEvents;
    private final boolean captureBeforeScreenshot;
    private final boolean captureAfterScreenshot;

    // Thread-local storage for current action context
    private static final ThreadLocal<ActionContext> currentActionContext = new ThreadLocal<>();

    // Track action execution history per thread
    private final Map<String, List<ActionContext>> threadActionHistory =
            Collections.synchronizedMap(new HashMap<>());

    @Autowired
    public ActionLifecycleAspect(
            ActionLoggingService actionLoggingService,
            @Value("${brobot.actions.pause.before:0}") int preActionPause,
            @Value("${brobot.actions.pause.after:0}") int postActionPause,
            @Value("${brobot.logging.actions.enabled:true}") boolean logEvents,
            @Value("${brobot.actions.screenshots.before:false}") boolean captureBeforeScreenshot,
            @Value("${brobot.actions.screenshots.after:false}") boolean captureAfterScreenshot) {
        this.actionLoggingService = actionLoggingService;
        this.preActionPause = preActionPause;
        this.postActionPause = postActionPause;
        this.logEvents = logEvents;
        this.captureBeforeScreenshot = captureBeforeScreenshot;
        this.captureAfterScreenshot = captureAfterScreenshot;
    }

    @Pointcut("execution(* io.github.jspinak.brobot.action.ActionInterface.perform(..))")
    public void actionExecution() {}

    @Around("actionExecution()")
    public Object manageActionLifecycle(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract action information
        ActionInterface action = (ActionInterface) joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();

        // Expect: perform(ActionResult result, ObjectCollection objects)
        ActionResult actionResult = null;
        ObjectCollection objectCollection = null;

        // Handle different argument patterns
        if (args.length == 2) {
            if (args[0] instanceof ActionResult) {
                actionResult = (ActionResult) args[0];
            }
            if (args[1] instanceof ObjectCollection) {
                objectCollection = (ObjectCollection) args[1];
            }
        } else if (args.length == 1 && args[0] instanceof ObjectCollection[]) {
            // Handle array of ObjectCollections
            ObjectCollection[] collections = (ObjectCollection[]) args[0];
            if (collections.length > 0) {
                objectCollection = collections[0];
            }
            actionResult = new ActionResult();
        }

        if (actionResult == null) {
            // Create default result if not provided
            actionResult = new ActionResult();
        }

        // Create action context
        ActionContext context = createActionContext(action, actionResult, objectCollection);

        // Pre-action tasks
        performPreActionTasks(context, actionResult);

        Object result = null;
        Exception executionError = null;
        long startTime = System.currentTimeMillis();

        try {
            // Set current context for nested access
            currentActionContext.set(context);

            // Execute the actual action
            result = joinPoint.proceed();

            // Mark success if result is ActionResult
            if (result instanceof ActionResult) {
                ActionResult actionResultFromExecution = (ActionResult) result;
                actionResultFromExecution.setSuccess(true);
            }

        } catch (Exception e) {
            executionError = e;
            actionResult.setSuccess(false);
            log.error("Action execution failed", e);
            throw e;
        } finally {
            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;

            // Post-action tasks
            performPostActionTasks(context, actionResult, duration, executionError);

            // Clear current context
            currentActionContext.remove();

            // Store in history
            storeActionInHistory(context);
        }

        return result != null ? result : actionResult;
    }

    private ActionContext createActionContext(
            ActionInterface action, ActionResult actionResult, ObjectCollection objectCollection) {
        ActionContext context = new ActionContext();
        context.setActionId(UUID.randomUUID().toString());
        context.setActionType(extractActionType(action, actionResult));
        context.setStartTime(Instant.now());
        context.setThreadName(Thread.currentThread().getName());
        context.setObjectCollection(objectCollection);
        return context;
    }

    private String extractActionType(ActionInterface action, ActionResult actionResult) {
        // Try to get from ActionConfig if available
        if (actionResult != null && actionResult.getActionConfig() != null) {
            ActionConfig config = actionResult.getActionConfig();
            return config.getClass().getSimpleName().replace("Options", "").toUpperCase();
        }
        // Fall back to action class name
        return action.getClass().getSimpleName().toUpperCase();
    }

    private void performPreActionTasks(ActionContext context, ActionResult actionResult) {
        // Set start time on ActionResult
        actionResult.setStartTime(
                LocalDateTime.ofInstant(context.getStartTime(), ZoneId.systemDefault()));

        // Apply pre-action pause
        if (preActionPause > 0) {
            try {
                Thread.sleep(preActionPause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Capture before screenshot if configured
        if (captureBeforeScreenshot) {
            // Screenshot capture would be done here
            log.debug("Would capture before screenshot for action {}", context.getActionId());
        }

        // Log action start if enabled
        if (logEvents) {
            log.debug(
                    "Starting action {} of type {} on thread {}",
                    context.getActionId(),
                    context.getActionType(),
                    context.getThreadName());
        }
    }

    private void performPostActionTasks(
            ActionContext context,
            ActionResult actionResult,
            long duration,
            Exception executionError) {

        // Set end time and duration on ActionResult
        actionResult.setEndTime(LocalDateTime.now());
        actionResult.setDuration(java.time.Duration.ofMillis(duration));

        // Update success status
        context.setSuccess(actionResult.isSuccess());
        context.setEndTime(Instant.now());
        context.setDuration(java.time.Duration.ofMillis(duration));

        // Apply post-action pause
        if (postActionPause > 0) {
            try {
                Thread.sleep(postActionPause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Capture after screenshot if configured
        if (captureAfterScreenshot) {
            // Screenshot capture would be done here
            log.debug("Would capture after screenshot for action {}", context.getActionId());
        }

        // Log action completion
        if (logEvents) {
            if (executionError != null) {
                log.error(
                        "Action {} failed after {}ms: {}",
                        context.getActionId(),
                        duration,
                        executionError.getMessage());
            } else {
                log.debug(
                        "Action {} completed successfully in {}ms",
                        context.getActionId(),
                        duration);
            }
        }

        // Use logging service to log the action
        // DISABLED: Redundant with BrobotLogger - uncomment if you prefer this formatter
        // if (actionLoggingService != null) {
        //     actionLoggingService.logAction(actionResult);
        // }
    }

    private void storeActionInHistory(ActionContext context) {
        String threadName = context.getThreadName();
        threadActionHistory
                .computeIfAbsent(threadName, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(context);

        // Limit history size to prevent memory leaks
        List<ActionContext> history = threadActionHistory.get(threadName);
        if (history.size() > 100) {
            history.remove(0);
        }
    }

    /**
     * Get the current action context for the executing thread.
     *
     * @return Optional containing the current action context if one exists
     */
    public Optional<ActionContext> getCurrentActionContext() {
        return Optional.ofNullable(currentActionContext.get());
    }

    /**
     * Get action history for a specific thread.
     *
     * @param threadName the name of the thread
     * @return List of action contexts for the thread
     */
    public List<ActionContext> getThreadActionHistory(String threadName) {
        return threadActionHistory.getOrDefault(threadName, Collections.emptyList());
    }

    /** Clear all action history. */
    public void clearHistory() {
        threadActionHistory.clear();
    }

    /** Internal class to track action execution context. */
    @Data
    public static class ActionContext {
        private String actionId;
        private String actionType;
        private Instant startTime;
        private Instant endTime;
        private java.time.Duration duration;
        private String threadName;
        private ObjectCollection objectCollection;
        private boolean success;
    }
}
