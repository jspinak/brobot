package io.github.jspinak.brobot.runner.errorhandling.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.errorhandling.ApplicationException;
import io.github.jspinak.brobot.runner.errorhandling.IErrorStrategy;
import io.github.jspinak.brobot.runner.errorhandling.strategies.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing error handling strategies. Provides registration, lookup, and
 * default strategy initialization.
 */
@Slf4j
@Service
public class ErrorStrategyManager {

    private final Map<Class<? extends Throwable>, IErrorStrategy> errorStrategies =
            new ConcurrentHashMap<>();
    private final IErrorStrategy defaultStrategy = new DefaultErrorStrategy();

    @PostConstruct
    public void initialize() {
        registerDefaultStrategies();
        log.info("Error strategy manager initialized with {} strategies", errorStrategies.size());
    }

    /** Registers a custom error strategy for a specific exception type. */
    public <T extends Throwable> void registerStrategy(
            Class<T> errorType, IErrorStrategy strategy) {
        if (errorType == null || strategy == null) {
            throw new IllegalArgumentException("Error type and strategy cannot be null");
        }

        errorStrategies.put(errorType, strategy);
        log.debug(
                "Registered strategy {} for error type {}",
                strategy.getClass().getSimpleName(),
                errorType.getSimpleName());
    }

    /**
     * Finds the appropriate strategy for the given error. Searches for exact match first, then
     * superclasses, finally returns default.
     */
    public IErrorStrategy findStrategy(Throwable error) {
        if (error == null) {
            return defaultStrategy;
        }

        // Look for exact match
        IErrorStrategy strategy = errorStrategies.get(error.getClass());
        if (strategy != null) {
            log.trace("Found exact strategy match for {}", error.getClass().getSimpleName());
            return strategy;
        }

        // Look for superclass match
        Class<?> currentClass = error.getClass();
        while (currentClass != null && Throwable.class.isAssignableFrom(currentClass)) {
            strategy = errorStrategies.get(currentClass);
            if (strategy != null) {
                log.trace(
                        "Found superclass strategy match for {} using {}",
                        error.getClass().getSimpleName(),
                        currentClass.getSimpleName());
                return strategy;
            }
            currentClass = currentClass.getSuperclass();
        }

        // Look for interface match (e.g., for exception hierarchies)
        for (Class<?> interfaceClass : error.getClass().getInterfaces()) {
            if (Throwable.class.isAssignableFrom(interfaceClass)) {
                strategy = errorStrategies.get(interfaceClass);
                if (strategy != null) {
                    log.trace(
                            "Found interface strategy match for {} using {}",
                            error.getClass().getSimpleName(),
                            interfaceClass.getSimpleName());
                    return strategy;
                }
            }
        }

        log.trace("Using default strategy for {}", error.getClass().getSimpleName());
        return defaultStrategy;
    }

    /** Removes a strategy for a specific error type. */
    public void removeStrategy(Class<? extends Throwable> errorType) {
        errorStrategies.remove(errorType);
        log.debug("Removed strategy for error type {}", errorType.getSimpleName());
    }

    /** Clears all registered strategies except defaults. */
    public void clearCustomStrategies() {
        errorStrategies.clear();
        registerDefaultStrategies();
        log.info("Cleared custom strategies, restored defaults");
    }

    /** Gets the count of registered strategies. */
    public int getStrategyCount() {
        return errorStrategies.size();
    }

    /** Checks if a strategy is registered for a specific error type. */
    public boolean hasStrategy(Class<? extends Throwable> errorType) {
        return errorStrategies.containsKey(errorType);
    }

    /** Registers default strategies for common exception types. */
    private void registerDefaultStrategies() {
        // Application exceptions
        registerStrategy(ApplicationException.class, new ApplicationExceptionStrategy());

        // Common Java exceptions
        registerStrategy(NullPointerException.class, new NullPointerStrategy());
        registerStrategy(IllegalArgumentException.class, new IllegalArgumentStrategy());
        registerStrategy(IllegalStateException.class, new IllegalStateStrategy());

        // I/O exceptions
        registerStrategy(java.io.IOException.class, new IOExceptionStrategy());
        registerStrategy(java.nio.file.NoSuchFileException.class, new FileNotFoundStrategy());

        // Concurrency exceptions
        registerStrategy(java.util.concurrent.TimeoutException.class, new TimeoutStrategy());
        registerStrategy(InterruptedException.class, new InterruptedStrategy());

        // Runtime exceptions
        registerStrategy(UnsupportedOperationException.class, new UnsupportedOperationStrategy());
        registerStrategy(IndexOutOfBoundsException.class, new IndexOutOfBoundsStrategy());

        log.debug("Registered {} default error strategies", errorStrategies.size());
    }
}
