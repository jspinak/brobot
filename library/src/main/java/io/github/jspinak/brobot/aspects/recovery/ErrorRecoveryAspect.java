package io.github.jspinak.brobot.aspects.recovery;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.aspects.annotations.Recoverable;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that provides automatic error recovery with configurable retry policies.
 *
 * <p>This aspect intercepts methods annotated with @Recoverable and implements sophisticated retry
 * logic including: - Exponential backoff with jitter - Circuit breaker pattern - Fallback methods -
 * Selective retry based on exception types - Timeout handling
 *
 * <p>The aspect helps improve system resilience by automatically handling transient failures
 * without requiring boilerplate retry code.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "brobot.aspects.error-recovery",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ErrorRecoveryAspect {

    private final BrobotLogger brobotLogger;

    @Autowired
    public ErrorRecoveryAspect(BrobotLogger brobotLogger) {
        this.brobotLogger = brobotLogger;
    }

    // Circuit breaker state
    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers =
            new ConcurrentHashMap<>();

    // Thread pool for timeout handling
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private final Random random = new Random();

    /** Intercept methods annotated with @Recoverable */
    @Around("@annotation(recoverable)")
    public Object handleRecoverable(ProceedingJoinPoint joinPoint, Recoverable recoverable)
            throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        // Check circuit breaker
        CircuitBreaker breaker =
                circuitBreakers.computeIfAbsent(methodName, k -> new CircuitBreaker(methodName));
        if (breaker.isOpen()) {
            throw new CircuitBreakerOpenException("Circuit breaker is open for " + methodName);
        }

        // Execute with retry logic
        return executeWithRetry(joinPoint, recoverable, breaker);
    }

    /** Execute method with retry logic */
    private Object executeWithRetry(
            ProceedingJoinPoint joinPoint, Recoverable recoverable, CircuitBreaker breaker)
            throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        int attempt = 0;
        long delay = recoverable.delay();
        Throwable lastException = null;

        while (attempt <= recoverable.maxRetries()) {
            // Check timeout
            if (recoverable.timeout() > 0
                    && System.currentTimeMillis() - startTime > recoverable.timeout()) {
                throw new RetryTimeoutException(
                        "Retry timeout exceeded for " + methodName, lastException);
            }

            try {
                // Log retry attempt
                if (attempt > 0 && recoverable.logRetries()) {
                    logRetryAttempt(methodName, attempt, delay);
                }

                // Execute the method
                Object result = joinPoint.proceed();

                // Success - record in circuit breaker and return
                breaker.recordSuccess();
                if (attempt > 0) {
                    logRetrySuccess(methodName, attempt);
                }
                return result;

            } catch (Throwable e) {
                lastException = e;
                breaker.recordFailure();

                // Check if we should retry this exception
                if (!shouldRetry(e, recoverable)) {
                    throw e;
                }

                // Check if we have more attempts
                if (attempt >= recoverable.maxRetries()) {
                    break;
                }

                // Apply delay before next attempt
                if (delay > 0) {
                    Thread.sleep(applyJitter(delay, recoverable.jitter()));
                }

                // Calculate next delay based on strategy
                delay = calculateNextDelay(delay, attempt, recoverable);
                attempt++;
            }
        }

        // All retries exhausted - try fallback or throw
        return handleRetryExhaustion(joinPoint, recoverable, lastException);
    }

    /** Check if exception should trigger retry */
    private boolean shouldRetry(Throwable e, Recoverable recoverable) {
        Class<? extends Throwable> exceptionType = e.getClass();

        // Check skip list first (takes precedence)
        for (Class<? extends Throwable> skipType : recoverable.skipOn()) {
            if (skipType.isAssignableFrom(exceptionType)) {
                return false;
            }
        }

        // If retryOn is empty, retry on any exception
        if (recoverable.retryOn().length == 0) {
            return true;
        }

        // Check retry list
        for (Class<? extends Throwable> retryType : recoverable.retryOn()) {
            if (retryType.isAssignableFrom(exceptionType)) {
                return true;
            }
        }

        return false;
    }

    /** Calculate delay for next retry based on strategy */
    private long calculateNextDelay(long currentDelay, int attempt, Recoverable recoverable) {
        switch (recoverable.strategy()) {
            case FIXED_DELAY:
                return recoverable.delay();

            case EXPONENTIAL_BACKOFF:
                return (long) (currentDelay * recoverable.backoff());

            case FIBONACCI_BACKOFF:
                return calculateFibonacciDelay(attempt, recoverable.delay());

            default:
                return currentDelay;
        }
    }

    /** Calculate Fibonacci sequence delay */
    private long calculateFibonacciDelay(int attempt, long baseDelay) {
        if (attempt <= 1) return baseDelay;

        long a = baseDelay;
        long b = baseDelay;
        for (int i = 2; i <= attempt; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }

    /** Apply jitter to delay */
    private long applyJitter(long delay, boolean useJitter) {
        if (!useJitter) {
            return delay;
        }

        // Add random jitter between -25% and +25%
        double jitterFactor = 0.75 + (random.nextDouble() * 0.5);
        return (long) (delay * jitterFactor);
    }

    /** Handle retry exhaustion - try fallback or throw */
    private Object handleRetryExhaustion(
            ProceedingJoinPoint joinPoint, Recoverable recoverable, Throwable lastException)
            throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        // Log exhaustion
        logRetryExhaustion(methodName, recoverable.maxRetries(), lastException);

        // Try fallback method if specified
        if (!recoverable.fallbackMethod().isEmpty()) {
            try {
                return invokeFallbackMethod(joinPoint, recoverable.fallbackMethod());
            } catch (Exception e) {
                log.error("Fallback method {} failed", recoverable.fallbackMethod(), e);
                throw new RetryExhaustedException(
                        "All retries and fallback failed for " + methodName, lastException);
            }
        }

        // No fallback - throw exception
        throw new RetryExhaustedException("All retries exhausted for " + methodName, lastException);
    }

    /** Invoke fallback method */
    private Object invokeFallbackMethod(ProceedingJoinPoint joinPoint, String fallbackMethodName)
            throws Exception {
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?>[] paramTypes = signature.getParameterTypes();

        // Find and invoke fallback method
        Method fallbackMethod = target.getClass().getDeclaredMethod(fallbackMethodName, paramTypes);
        fallbackMethod.setAccessible(true);
        return fallbackMethod.invoke(target, args);
    }

    /** Log retry attempt */
    private void logRetryAttempt(String method, int attempt, long delay) {
        brobotLogger
                .log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.WARNING)
                .action("RETRY_ATTEMPT")
                .metadata("method", method)
                .metadata("attempt", attempt)
                .metadata("nextDelay", delay)
                .observation("Retrying after failure")
                .log();
    }

    /** Log retry success */
    private void logRetrySuccess(String method, int attemptsTaken) {
        brobotLogger
                .log()
                .type(LogEvent.Type.ACTION)
                .level(LogEvent.Level.INFO)
                .action("RETRY_SUCCESS")
                .metadata("method", method)
                .metadata("attemptsTaken", attemptsTaken)
                .observation("Operation succeeded after retry")
                .log();
    }

    /** Log retry exhaustion */
    private void logRetryExhaustion(String method, int maxRetries, Throwable lastException) {
        brobotLogger
                .log()
                .type(LogEvent.Type.ERROR)
                .level(LogEvent.Level.ERROR)
                .action("RETRY_EXHAUSTED")
                .metadata("method", method)
                .metadata("maxRetries", maxRetries)
                .metadata("lastException", lastException.getClass().getSimpleName())
                .error(lastException)
                .observation("All retry attempts failed")
                .log();
    }

    /** Circuit breaker implementation */
    private static class CircuitBreaker {
        private final String name;
        private final AtomicInteger failureCount = new AtomicInteger();
        private final AtomicInteger successCount = new AtomicInteger();
        private volatile long lastFailureTime = 0;
        private volatile State state = State.CLOSED;

        // Configuration
        private static final int FAILURE_THRESHOLD = 5;
        private static final long OPEN_DURATION = 60000; // 60 seconds
        private static final int SUCCESS_THRESHOLD = 3;

        public CircuitBreaker(String name) {
            this.name = name;
        }

        public boolean isOpen() {
            if (state == State.OPEN) {
                // Check if we should transition to half-open
                if (System.currentTimeMillis() - lastFailureTime > OPEN_DURATION) {
                    state = State.HALF_OPEN;
                    return false;
                }
                return true;
            }
            return false;
        }

        public void recordSuccess() {
            successCount.incrementAndGet();

            if (state == State.HALF_OPEN && successCount.get() >= SUCCESS_THRESHOLD) {
                // Transition to closed
                state = State.CLOSED;
                failureCount.set(0);
                successCount.set(0);
                log.info("Circuit breaker {} closed", name);
            }
        }

        public void recordFailure() {
            failureCount.incrementAndGet();
            lastFailureTime = System.currentTimeMillis();

            if (state == State.HALF_OPEN || failureCount.get() >= FAILURE_THRESHOLD) {
                // Transition to open
                state = State.OPEN;
                successCount.set(0);
                log.warn("Circuit breaker {} opened", name);
            }
        }

        private enum State {
            CLOSED,
            OPEN,
            HALF_OPEN
        }
    }

    /** Custom exceptions */
    public static class RetryExhaustedException extends Exception {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class RetryTimeoutException extends Exception {
        public RetryTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CircuitBreakerOpenException extends Exception {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
