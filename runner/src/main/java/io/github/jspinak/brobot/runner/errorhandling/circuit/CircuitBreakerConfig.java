package io.github.jspinak.brobot.runner.errorhandling.circuit;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Configuration for circuit breaker behavior.
 * 
 * <p>Defines thresholds and timeouts for circuit breaker state transitions.
 */
@Data
@Builder
public class CircuitBreakerConfig {
    
    /**
     * Number of consecutive failures before opening the circuit.
     * Default: 5
     */
    @Builder.Default
    private final int failureThreshold = 5;
    
    /**
     * Duration to wait before attempting to close the circuit.
     * Default: 30 seconds
     */
    @Builder.Default
    private final Duration resetTimeout = Duration.ofSeconds(30);
    
    /**
     * Number of successful calls in half-open state before closing.
     * Default: 3
     */
    @Builder.Default
    private final int halfOpenSuccessThreshold = 3;
    
    /**
     * Optional listener for state changes.
     */
    private final Consumer<CircuitBreaker.State> stateChangeListener;
    
    /**
     * Creates a default configuration.
     * 
     * @return default config
     */
    public static CircuitBreakerConfig defaultConfig() {
        return CircuitBreakerConfig.builder().build();
    }
    
    /**
     * Creates a strict configuration with lower thresholds.
     * 
     * @return strict config
     */
    public static CircuitBreakerConfig strict() {
        return CircuitBreakerConfig.builder()
            .failureThreshold(3)
            .resetTimeout(Duration.ofSeconds(20))
            .halfOpenSuccessThreshold(5)
            .build();
    }
    
    /**
     * Creates a lenient configuration with higher thresholds.
     * 
     * @return lenient config
     */
    public static CircuitBreakerConfig lenient() {
        return CircuitBreakerConfig.builder()
            .failureThreshold(10)
            .resetTimeout(Duration.ofMinutes(1))
            .halfOpenSuccessThreshold(2)
            .build();
    }
}