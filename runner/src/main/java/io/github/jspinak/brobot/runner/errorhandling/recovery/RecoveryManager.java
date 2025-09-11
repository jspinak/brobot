package io.github.jspinak.brobot.runner.errorhandling.recovery;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.events.EventBus;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Manages error recovery workflows and retry strategies. */
@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class RecoveryManager {

    private final EventBus eventBus;
    private final Map<String, IRecoveryStrategy> recoveryStrategies = new ConcurrentHashMap<>();
    private final Map<String, RecoveryState> activeRecoveries = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(
                    2,
                    r -> {
                        Thread t = new Thread(r, "recovery-manager");
                        t.setDaemon(true);
                        return t;
                    });

    @PostConstruct
    public void init() {
        initializeDefaultStrategies();
    }

    /** Register a recovery strategy for a specific error category. */
    public void registerStrategy(ErrorContext.ErrorCategory category, IRecoveryStrategy strategy) {
        recoveryStrategies.put(category.name(), strategy);
    }

    /** Register a recovery strategy by name. */
    public void registerStrategy(String name, IRecoveryStrategy strategy) {
        recoveryStrategies.put(name, strategy);
    }

    /** Attempt to recover from an error. */
    public CompletableFuture<RecoveryResult> attemptRecovery(
            Throwable error, ErrorContext context, Supplier<Object> retryOperation) {

        String recoveryId = UUID.randomUUID().toString();
        RecoveryState state = new RecoveryState(recoveryId, error, context);
        activeRecoveries.put(recoveryId, state);

        // Find appropriate strategy
        IRecoveryStrategy strategy = findStrategy(context);

        log.info("Attempting recovery [{}] for error in {}", recoveryId, context.getOperation());

        // Execute recovery
        CompletableFuture<RecoveryResult> future =
                strategy.recover(error, context, retryOperation, state);

        // Clean up when done
        future.whenComplete(
                (result, throwable) -> {
                    activeRecoveries.remove(recoveryId);
                    logRecoveryResult(recoveryId, result, throwable);
                });

        return future;
    }

    /** Get active recovery operations. */
    public List<RecoveryInfo> getActiveRecoveries() {
        return activeRecoveries.values().stream()
                .map(
                        state ->
                                new RecoveryInfo(
                                        state.recoveryId,
                                        state.context.getOperation(),
                                        state.attemptCount.get(),
                                        state.startTime,
                                        state.lastAttemptTime))
                .toList();
    }

    /** Cancel an active recovery operation. */
    public boolean cancelRecovery(String recoveryId) {
        RecoveryState state = activeRecoveries.get(recoveryId);
        if (state != null) {
            state.cancelled = true;
            activeRecoveries.remove(recoveryId);
            log.info("Cancelled recovery operation: {}", recoveryId);
            return true;
        }
        return false;
    }

    private IRecoveryStrategy findStrategy(ErrorContext context) {
        // Try category-specific strategy
        IRecoveryStrategy strategy = recoveryStrategies.get(context.getCategory().name());
        if (strategy != null) {
            return strategy;
        }

        // Try operation-specific strategy
        if (context.getOperation() != null) {
            strategy = recoveryStrategies.get(context.getOperation());
            if (strategy != null) {
                return strategy;
            }
        }

        // Return default strategy
        return recoveryStrategies.getOrDefault("default", new ExponentialBackoffStrategy());
    }

    private void initializeDefaultStrategies() {
        // Default strategies
        recoveryStrategies.put("default", new ExponentialBackoffStrategy());
        recoveryStrategies.put("immediate", new ImmediateRetryStrategy());
        recoveryStrategies.put("circuit-breaker", new CircuitBreakerStrategy());

        // Category-specific strategies
        recoveryStrategies.put(
                ErrorContext.ErrorCategory.NETWORK.name(),
                new ExponentialBackoffStrategy(3, 1000, 30000));

        recoveryStrategies.put(
                ErrorContext.ErrorCategory.FILE_IO.name(), new ImmediateRetryStrategy(2));

        recoveryStrategies.put(
                ErrorContext.ErrorCategory.DATABASE.name(), new CircuitBreakerStrategy(5, 60000));
    }

    private void logRecoveryResult(String recoveryId, RecoveryResult result, Throwable error) {
        if (error != null) {
            log.error("Recovery [{}] failed with exception", recoveryId, error);
        } else if (result.isSuccess()) {
            log.info("Recovery [{}] succeeded after {} attempts", recoveryId, result.getAttempts());
        } else {
            log.warn(
                    "Recovery [{}] failed after {} attempts: {}",
                    recoveryId,
                    result.getAttempts(),
                    result.getFailureReason());
        }
    }

    /** Recovery state tracking. */
    public static class RecoveryState {
        final String recoveryId;
        final Throwable originalError;
        final ErrorContext context;
        final long startTime;
        final AtomicInteger attemptCount = new AtomicInteger(0);
        volatile long lastAttemptTime;
        volatile boolean cancelled = false;

        RecoveryState(String recoveryId, Throwable error, ErrorContext context) {
            this.recoveryId = recoveryId;
            this.originalError = error;
            this.context = context;
            this.startTime = System.currentTimeMillis();
            this.lastAttemptTime = startTime;
        }

        boolean shouldContinue(int maxAttempts) {
            return !cancelled && attemptCount.get() < maxAttempts;
        }

        void recordAttempt() {
            attemptCount.incrementAndGet();
            lastAttemptTime = System.currentTimeMillis();
        }
    }

    /** Recovery operation information. */
    public record RecoveryInfo(
            String recoveryId,
            String operation,
            int attempts,
            long startTime,
            long lastAttemptTime) {}

    /** Exponential backoff retry strategy. */
    public static class ExponentialBackoffStrategy implements IRecoveryStrategy {
        private final int maxAttempts;
        private final long initialDelay;
        private final long maxDelay;

        public ExponentialBackoffStrategy() {
            this(3, 500, 10000);
        }

        public ExponentialBackoffStrategy(int maxAttempts, long initialDelay, long maxDelay) {
            this.maxAttempts = maxAttempts;
            this.initialDelay = initialDelay;
            this.maxDelay = maxDelay;
        }

        @Override
        public CompletableFuture<RecoveryResult> recover(
                Throwable error,
                ErrorContext context,
                Supplier<Object> retryOperation,
                RecoveryState state) {

            return CompletableFuture.supplyAsync(
                    () -> {
                        while (state.shouldContinue(maxAttempts)) {
                            state.recordAttempt();

                            try {
                                // Calculate delay with exponential backoff
                                long delay =
                                        Math.min(
                                                initialDelay
                                                        * (long)
                                                                Math.pow(
                                                                        2,
                                                                        state.attemptCount.get()
                                                                                - 1),
                                                maxDelay);

                                if (state.attemptCount.get() > 1) {
                                    log.debug(
                                            "Waiting {} ms before retry attempt {}",
                                            delay,
                                            state.attemptCount.get());
                                    Thread.sleep(delay);
                                }

                                // Attempt operation
                                Object result = retryOperation.get();

                                return RecoveryResult.success(state.attemptCount.get(), result);

                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return RecoveryResult.failure(
                                        state.attemptCount.get(), "Recovery interrupted");
                            } catch (Exception e) {
                                log.debug(
                                        "Recovery attempt {} failed: {}",
                                        state.attemptCount.get(),
                                        e.getMessage());

                                if (!state.shouldContinue(maxAttempts)) {
                                    return RecoveryResult.failure(
                                            state.attemptCount.get(),
                                            "Max attempts reached: " + e.getMessage());
                                }
                            }
                        }

                        return RecoveryResult.failure(
                                state.attemptCount.get(),
                                state.cancelled ? "Recovery cancelled" : "Max attempts reached");
                    });
        }
    }

    /** Immediate retry strategy without delays. */
    public static class ImmediateRetryStrategy implements IRecoveryStrategy {
        private final int maxAttempts;

        public ImmediateRetryStrategy() {
            this(3);
        }

        public ImmediateRetryStrategy(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        @Override
        public CompletableFuture<RecoveryResult> recover(
                Throwable error,
                ErrorContext context,
                Supplier<Object> retryOperation,
                RecoveryState state) {

            return CompletableFuture.supplyAsync(
                    () -> {
                        while (state.shouldContinue(maxAttempts)) {
                            state.recordAttempt();

                            try {
                                Object result = retryOperation.get();
                                return RecoveryResult.success(state.attemptCount.get(), result);
                            } catch (Exception e) {
                                if (!state.shouldContinue(maxAttempts)) {
                                    return RecoveryResult.failure(
                                            state.attemptCount.get(),
                                            "Max attempts reached: " + e.getMessage());
                                }
                            }
                        }

                        return RecoveryResult.failure(
                                state.attemptCount.get(), "Max attempts reached");
                    });
        }
    }

    /** Circuit breaker pattern for preventing cascading failures. */
    public static class CircuitBreakerStrategy implements IRecoveryStrategy {
        private final int failureThreshold;
        private final long resetTimeoutMs;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private volatile long lastFailureTime = 0;
        private volatile CircuitState state = CircuitState.CLOSED;

        public CircuitBreakerStrategy() {
            this(5, 60000); // 5 failures, 1 minute reset
        }

        public CircuitBreakerStrategy(int failureThreshold, long resetTimeoutMs) {
            this.failureThreshold = failureThreshold;
            this.resetTimeoutMs = resetTimeoutMs;
        }

        @Override
        public CompletableFuture<RecoveryResult> recover(
                Throwable error,
                ErrorContext context,
                Supplier<Object> retryOperation,
                RecoveryState recoveryState) {

            return CompletableFuture.supplyAsync(
                    () -> {
                        // Check circuit state
                        updateCircuitState();

                        if (state == CircuitState.OPEN) {
                            return RecoveryResult.failure(
                                    0, "Circuit breaker is open - too many recent failures");
                        }

                        recoveryState.recordAttempt();

                        try {
                            Object result = retryOperation.get();

                            // Success - reset failure count
                            onSuccess();

                            return RecoveryResult.success(1, result);

                        } catch (Exception e) {
                            // Failure - update circuit breaker
                            onFailure();

                            return RecoveryResult.failure(1, "Operation failed: " + e.getMessage());
                        }
                    });
        }

        private synchronized void updateCircuitState() {
            if (state == CircuitState.OPEN) {
                // Check if timeout has passed
                if (System.currentTimeMillis() - lastFailureTime > resetTimeoutMs) {
                    state = CircuitState.HALF_OPEN;
                    log.info("Circuit breaker moved to HALF_OPEN state");
                }
            }
        }

        private synchronized void onSuccess() {
            failureCount.set(0);
            if (state != CircuitState.CLOSED) {
                state = CircuitState.CLOSED;
                log.info("Circuit breaker closed after successful operation");
            }
        }

        private synchronized void onFailure() {
            lastFailureTime = System.currentTimeMillis();
            int failures = failureCount.incrementAndGet();

            if (failures >= failureThreshold && state != CircuitState.OPEN) {
                state = CircuitState.OPEN;
                log.warn("Circuit breaker opened after {} failures", failures);
            }
        }

        private enum CircuitState {
            CLOSED, // Normal operation
            OPEN, // Blocking requests
            HALF_OPEN // Testing if service recovered
        }
    }
}
