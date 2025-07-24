package io.github.jspinak.brobot.runner.errorhandling.circuit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Circuit breaker implementation to prevent cascading failures.
 * 
 * <p>The circuit breaker has three states:
 * <ul>
 *   <li>CLOSED: Normal operation, requests are allowed</li>
 *   <li>OPEN: Failures exceeded threshold, requests are blocked</li>
 *   <li>HALF_OPEN: Testing if the service has recovered</li>
 * </ul>
 * 
 * <p>Thread-safe implementation using atomic operations.
 * 
 * @see CircuitBreakerConfig
 */
@Slf4j
@Getter
public class CircuitBreaker {
    
    /**
     * Circuit breaker states.
     */
    public enum State {
        CLOSED("Circuit is closed, normal operation"),
        OPEN("Circuit is open, requests are blocked"),
        HALF_OPEN("Circuit is half-open, testing recovery");
        
        private final String description;
        
        State(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final String name;
    private final CircuitBreakerConfig config;
    
    // State management
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicReference<Instant> lastStateTransition = new AtomicReference<>(Instant.now());
    
    // Failure tracking
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicInteger halfOpenSuccesses = new AtomicInteger(0);
    
    // Metrics
    private final AtomicInteger totalCalls = new AtomicInteger(0);
    private final AtomicInteger blockedCalls = new AtomicInteger(0);
    private final AtomicInteger failedCalls = new AtomicInteger(0);
    
    public CircuitBreaker(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
        log.info("Circuit breaker '{}' initialized with config: {}", name, config);
    }
    
    /**
     * Executes a supplier through the circuit breaker.
     * 
     * @param supplier the operation to execute
     * @param <T> the return type
     * @return the result
     * @throws CircuitBreakerOpenException if circuit is open
     * @throws Exception if the operation fails
     */
    public <T> T execute(Supplier<T> supplier) throws Exception {
        totalCalls.incrementAndGet();
        
        if (!allowRequest()) {
            blockedCalls.incrementAndGet();
            throw new CircuitBreakerOpenException(
                String.format("Circuit breaker '%s' is OPEN", name)
            );
        }
        
        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
    
    /**
     * Executes a runnable through the circuit breaker.
     * 
     * @param runnable the operation to execute
     * @throws CircuitBreakerOpenException if circuit is open
     * @throws Exception if the operation fails
     */
    public void execute(Runnable runnable) throws Exception {
        execute(() -> {
            runnable.run();
            return null;
        });
    }
    
    /**
     * Checks if a request should be allowed.
     * 
     * @return true if request is allowed
     */
    private boolean allowRequest() {
        State currentState = state.get();
        
        switch (currentState) {
            case CLOSED:
                return true;
                
            case OPEN:
                if (shouldAttemptReset()) {
                    transitionToHalfOpen();
                    return true;
                }
                return false;
                
            case HALF_OPEN:
                // Allow limited requests in half-open state
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Handles successful operation.
     */
    private void onSuccess() {
        State currentState = state.get();
        
        switch (currentState) {
            case CLOSED:
                consecutiveFailures.set(0);
                break;
                
            case HALF_OPEN:
                int successes = halfOpenSuccesses.incrementAndGet();
                if (successes >= config.getHalfOpenSuccessThreshold()) {
                    transitionToClosed();
                }
                break;
        }
    }
    
    /**
     * Handles failed operation.
     */
    private void onFailure() {
        failedCalls.incrementAndGet();
        State currentState = state.get();
        
        switch (currentState) {
            case CLOSED:
                int failures = consecutiveFailures.incrementAndGet();
                if (failures >= config.getFailureThreshold()) {
                    transitionToOpen();
                }
                break;
                
            case HALF_OPEN:
                transitionToOpen();
                break;
        }
    }
    
    /**
     * Checks if enough time has passed to attempt reset.
     * 
     * @return true if should attempt reset
     */
    private boolean shouldAttemptReset() {
        Duration timeSinceLastTransition = Duration.between(
            lastStateTransition.get(), 
            Instant.now()
        );
        return timeSinceLastTransition.compareTo(config.getResetTimeout()) >= 0;
    }
    
    /**
     * Transitions to CLOSED state.
     */
    private void transitionToClosed() {
        if (state.compareAndSet(State.HALF_OPEN, State.CLOSED) ||
            state.compareAndSet(State.OPEN, State.CLOSED)) {
            
            lastStateTransition.set(Instant.now());
            consecutiveFailures.set(0);
            halfOpenSuccesses.set(0);
            
            log.info("Circuit breaker '{}' transitioned to CLOSED", name);
            
            if (config.getStateChangeListener() != null) {
                config.getStateChangeListener().accept(State.CLOSED);
            }
        }
    }
    
    /**
     * Transitions to OPEN state.
     */
    private void transitionToOpen() {
        State previousState = state.getAndSet(State.OPEN);
        if (previousState != State.OPEN) {
            lastStateTransition.set(Instant.now());
            halfOpenSuccesses.set(0);
            
            log.warn("Circuit breaker '{}' transitioned to OPEN after {} consecutive failures", 
                     name, consecutiveFailures.get());
            
            if (config.getStateChangeListener() != null) {
                config.getStateChangeListener().accept(State.OPEN);
            }
        }
    }
    
    /**
     * Transitions to HALF_OPEN state.
     */
    private void transitionToHalfOpen() {
        if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
            lastStateTransition.set(Instant.now());
            halfOpenSuccesses.set(0);
            
            log.info("Circuit breaker '{}' transitioned to HALF_OPEN", name);
            
            if (config.getStateChangeListener() != null) {
                config.getStateChangeListener().accept(State.HALF_OPEN);
            }
        }
    }
    
    /**
     * Manually resets the circuit breaker to CLOSED state.
     */
    public void reset() {
        state.set(State.CLOSED);
        lastStateTransition.set(Instant.now());
        consecutiveFailures.set(0);
        halfOpenSuccesses.set(0);
        
        log.info("Circuit breaker '{}' manually reset to CLOSED", name);
    }
    
    /**
     * Gets current circuit breaker metrics.
     * 
     * @return metrics snapshot
     */
    public CircuitBreakerMetrics getMetrics() {
        return CircuitBreakerMetrics.builder()
            .name(name)
            .state(state.get())
            .totalCalls(totalCalls.get())
            .blockedCalls(blockedCalls.get())
            .failedCalls(failedCalls.get())
            .consecutiveFailures(consecutiveFailures.get())
            .lastStateTransition(lastStateTransition.get())
            .build();
    }
}