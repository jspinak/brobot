package io.github.jspinak.brobot.action.logging;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.logging.BrobotLogger;
import io.github.jspinak.brobot.logging.LogCategory;
import io.github.jspinak.brobot.logging.LogLevel;

/**
 * Specialized logging for action chains with step-by-step tracking. Provides detailed logging for
 * complex action sequences.
 */
@Component
public class ActionChainLogger {

    @Autowired(required = false)
    private BrobotLogger logger;

    private final Map<String, ChainContext> activeChains = new HashMap<>();

    /** Context for tracking an action chain execution */
    public static class ChainContext {
        private final String chainId;
        private final LocalDateTime startTime;
        private final List<StepLog> steps;
        private boolean stepLoggingEnabled;
        private int currentStepIndex;

        public ChainContext(String chainId) {
            this.chainId = chainId;
            this.startTime = LocalDateTime.now();
            this.steps = new ArrayList<>();
            this.stepLoggingEnabled = true;
            this.currentStepIndex = 0;
        }

        public void addStep(StepLog step) {
            steps.add(step);
        }

        public List<StepLog> getSteps() {
            return Collections.unmodifiableList(steps);
        }

        public Duration getDuration() {
            return Duration.between(startTime, LocalDateTime.now());
        }

        public String getChainId() {
            return chainId;
        }

        public boolean isStepLoggingEnabled() {
            return stepLoggingEnabled;
        }

        public void setStepLoggingEnabled(boolean enabled) {
            this.stepLoggingEnabled = enabled;
        }

        public int getCurrentStepIndex() {
            return currentStepIndex;
        }

        public void incrementStepIndex() {
            currentStepIndex++;
        }
    }

    /** Represents a single step in an action chain */
    public static class StepLog {
        private final int stepNumber;
        private final String description;
        private final ActionConfig config;
        private final ActionResult result;
        private final LocalDateTime timestamp;

        public StepLog(
                int stepNumber, String description, ActionConfig config, ActionResult result) {
            this.stepNumber = stepNumber;
            this.description = description;
            this.config = config;
            this.result = result;
            this.timestamp = LocalDateTime.now();
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public String getDescription() {
            return description;
        }

        public ActionConfig getConfig() {
            return config;
        }

        public ActionResult getResult() {
            return result;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    /** Log the start of an action chain */
    public String logChainStart(String chainName, String description) {
        String chainId = UUID.randomUUID().toString();
        ChainContext context = new ChainContext(chainId);
        activeChains.put(chainId, context);

        if (logger != null) {
            logger.builder(LogCategory.SYSTEM)
                    .level(LogLevel.INFO)
                    .message("=== Starting Action Chain: " + chainName + " ===")
                    .context("chainId", chainId)
                    .context("description", description)
                    .log();
        }

        return chainId;
    }

    /** Log a step transition in the chain */
    public void logStepTransition(
            String chainId,
            String fromStep,
            String toStep,
            ActionConfig config,
            ActionResult result) {
        ChainContext context = activeChains.get(chainId);
        if (context == null) {
            if (logger != null) {
                logger.builder(LogCategory.SYSTEM)
                        .level(LogLevel.WARN)
                        .message("No active chain found with ID: " + chainId)
                        .log();
            }
            return;
        }

        context.incrementStepIndex();
        int stepNumber = context.getCurrentStepIndex();

        if (context.isStepLoggingEnabled() && logger != null) {
            logger.builder(LogCategory.SYSTEM)
                    .level(LogLevel.INFO)
                    .message(String.format("  Step %d: %s -> %s", stepNumber, fromStep, toStep))
                    .context("stepNumber", stepNumber)
                    .context(
                            "configType",
                            config != null ? config.getClass().getSimpleName() : "null")
                    .context("success", result != null ? result.isSuccess() : false)
                    .context("matches", result != null ? result.getMatchList().size() : 0)
                    .log();
        }

        StepLog stepLog = new StepLog(stepNumber, fromStep + " -> " + toStep, config, result);
        context.addStep(stepLog);
    }

    /** Log the end of an action chain */
    public void logChainEnd(String chainId, boolean success, String summary) {
        ChainContext context = activeChains.remove(chainId);
        if (context == null) {
            if (logger != null) {
                logger.builder(LogCategory.SYSTEM)
                        .level(LogLevel.WARN)
                        .message("No active chain found with ID: " + chainId)
                        .log();
            }
            return;
        }

        Duration duration = context.getDuration();
        if (logger != null) {
            logger.builder(LogCategory.SYSTEM)
                    .level(LogLevel.INFO)
                    .message("=== Action Chain Completed ===")
                    .context("chainId", chainId)
                    .context("success", success)
                    .context("durationMs", duration.toMillis())
                    .context("totalSteps", context.getSteps().size())
                    .context("summary", summary != null ? summary : "")
                    .log();

            // Log step summary if there were failures
            if (!success) {
                context.getSteps().stream()
                        .filter(step -> step.getResult() != null && !step.getResult().isSuccess())
                        .forEach(
                                step ->
                                        logger.builder(LogCategory.SYSTEM)
                                                .level(LogLevel.WARN)
                                                .message(
                                                        String.format(
                                                                "  Failed Step %d: %s",
                                                                step.getStepNumber(),
                                                                step.getDescription()))
                                                .log());
            }
        }
    }

    /** Enable or disable step logging for a chain */
    public ActionChainLogger withStepLogging(String chainId, boolean enabled) {
        ChainContext context = activeChains.get(chainId);
        if (context != null) {
            context.setStepLoggingEnabled(enabled);
        }
        return this;
    }

    /** Log a simple step without transition details */
    public void logStep(String chainId, String stepDescription) {
        ChainContext context = activeChains.get(chainId);
        if (context == null) {
            if (logger != null) {
                logger.builder(LogCategory.SYSTEM)
                        .level(LogLevel.WARN)
                        .message("No active chain found with ID: " + chainId)
                        .log();
            }
            return;
        }

        context.incrementStepIndex();
        if (context.isStepLoggingEnabled() && logger != null) {
            logger.builder(LogCategory.SYSTEM)
                    .level(LogLevel.INFO)
                    .message(
                            String.format(
                                    "  Step %d: %s",
                                    context.getCurrentStepIndex(), stepDescription))
                    .log();
        }

        StepLog stepLog = new StepLog(context.getCurrentStepIndex(), stepDescription, null, null);
        context.addStep(stepLog);
    }

    /** Get the context for an active chain */
    public ChainContext getChainContext(String chainId) {
        return activeChains.get(chainId);
    }

    /** Check if a chain is active */
    public boolean isChainActive(String chainId) {
        return activeChains.containsKey(chainId);
    }

    /** Clear all active chains (useful for cleanup) */
    public void clearAllChains() {
        activeChains.clear();
    }
}
