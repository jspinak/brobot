package io.github.jspinak.brobot.runner.errorhandling.circuit;

/** Exception thrown when a circuit breaker is open and blocking requests. */
public class CircuitBreakerOpenException extends Exception {

    public CircuitBreakerOpenException(String message) {
        super(message);
    }

    public CircuitBreakerOpenException(String message, Throwable cause) {
        super(message, cause);
    }
}
