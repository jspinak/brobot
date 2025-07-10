package io.github.jspinak.brobot.runner.errorhandling.strategy;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.errorhandling.*;
import io.github.jspinak.brobot.runner.errorhandling.strategy.strategies.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service responsible for managing and executing error handling strategies.
 * 
 * This service maintains a registry of error strategies mapped to exception types,
 * finds the appropriate strategy for a given error, and executes it.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ErrorStrategyService implements DiagnosticCapable {
    
    private final Map<Class<? extends Throwable>, IErrorStrategy> strategies = new ConcurrentHashMap<>();
    
    // Statistics
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final Map<String, AtomicLong> strategyExecutions = new ConcurrentHashMap<>();
    private final AtomicLong defaultStrategyExecutions = new AtomicLong(0);
    
    // Default strategy
    private final IErrorStrategy defaultStrategy = new DefaultErrorStrategy();
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    @PostConstruct
    public void initialize() {
        registerDefaultStrategies();
        log.info("ErrorStrategyService initialized with {} strategies", strategies.size());
    }
    
    /**
     * Register a strategy for a specific exception type.
     * 
     * @param errorType the exception type
     * @param strategy the strategy to use
     * @param <T> the exception type
     */
    public <T extends Throwable> void registerStrategy(Class<T> errorType, IErrorStrategy strategy) {
        if (errorType == null || strategy == null) {
            throw new IllegalArgumentException("Error type and strategy must not be null");
        }
        
        strategies.put(errorType, strategy);
        log.info("Registered strategy {} for error type {}", 
                strategy.getClass().getSimpleName(), errorType.getSimpleName());
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Total registered strategies: {}", strategies.size());
        }
    }
    
    /**
     * Find the appropriate strategy for an error.
     * 
     * @param error the error to find a strategy for
     * @return the matching strategy or default strategy
     */
    public IErrorStrategy findStrategy(Throwable error) {
        if (error == null) {
            return defaultStrategy;
        }
        
        // Look for exact match
        IErrorStrategy strategy = strategies.get(error.getClass());
        if (strategy != null) {
            if (diagnosticMode.get()) {
                log.debug("[DIAGNOSTIC] Found exact strategy match for {}", 
                        error.getClass().getSimpleName());
            }
            return strategy;
        }
        
        // Look for superclass match
        Class<?> currentClass = error.getClass();
        while (currentClass != null && Throwable.class.isAssignableFrom(currentClass)) {
            strategy = strategies.get(currentClass);
            if (strategy != null) {
                if (diagnosticMode.get()) {
                    log.debug("[DIAGNOSTIC] Found superclass strategy match for {} using {}", 
                            error.getClass().getSimpleName(), currentClass.getSimpleName());
                }
                return strategy;
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // Check interfaces
        for (Class<?> interfaceClass : error.getClass().getInterfaces()) {
            if (Throwable.class.isAssignableFrom(interfaceClass)) {
                strategy = strategies.get(interfaceClass);
                if (strategy != null) {
                    if (diagnosticMode.get()) {
                        log.debug("[DIAGNOSTIC] Found interface strategy match for {} using {}", 
                                error.getClass().getSimpleName(), interfaceClass.getSimpleName());
                    }
                    return strategy;
                }
            }
        }
        
        if (diagnosticMode.get()) {
            log.debug("[DIAGNOSTIC] Using default strategy for {}", 
                    error.getClass().getSimpleName());
        }
        
        return defaultStrategy;
    }
    
    /**
     * Handle an error using the appropriate strategy.
     * 
     * @param error the error to handle
     * @param context the error context
     * @return the error result
     */
    public ErrorResult handleError(Throwable error, ErrorContext context) {
        return executeStrategy(error, context);
    }
    
    /**
     * Execute a strategy for the given error and context.
     * 
     * @param error the error to handle
     * @param context the error context
     * @return the error result
     */
    public ErrorResult executeStrategy(Throwable error, ErrorContext context) {
        totalExecutions.incrementAndGet();
        
        IErrorStrategy strategy = findStrategy(error);
        String strategyName = strategy.getClass().getSimpleName();
        
        // Track execution
        if (strategy == defaultStrategy) {
            defaultStrategyExecutions.incrementAndGet();
        } else {
            strategyExecutions.computeIfAbsent(strategyName, k -> new AtomicLong())
                    .incrementAndGet();
        }
        
        try {
            ErrorResult result = strategy.handle(error, context);
            
            if (diagnosticMode.get()) {
                log.info("[DIAGNOSTIC] Strategy {} returned result - Success: {}, Recoverable: {}",
                        strategyName, result.isSuccess(), result.isRecoverable());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Strategy {} failed for error {}", strategyName, context.getErrorId(), e);
            
            // Return a safe error result
            return ErrorResult.builder()
                    .errorId(context.getErrorId())
                    .success(false)
                    .recoverable(false)
                    .userMessage("Error handling failed")
                    .technicalDetails("Strategy execution failed: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Remove a strategy for a specific error type.
     * 
     * @param errorType the error type
     * @return the removed strategy or null
     */
    public IErrorStrategy removeStrategy(Class<? extends Throwable> errorType) {
        return strategies.remove(errorType);
    }
    
    /**
     * Clear all custom strategies (keeps default strategies).
     */
    public void clearCustomStrategies() {
        strategies.clear();
        registerDefaultStrategies();
    }
    
    /**
     * Get all registered strategies.
     * 
     * @return unmodifiable map of strategies
     */
    public Map<Class<? extends Throwable>, IErrorStrategy> getStrategies() {
        return Map.copyOf(strategies);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        states.put("registeredStrategies", strategies.size());
        states.put("totalExecutions", totalExecutions.get());
        states.put("defaultStrategyExecutions", defaultStrategyExecutions.get());
        
        // Per-strategy statistics
        strategyExecutions.forEach((name, count) -> {
            states.put("strategy." + name + ".executions", count.get());
        });
        
        // Strategy type mapping
        strategies.forEach((errorType, strategy) -> {
            states.put("mapping." + errorType.getSimpleName(), 
                    strategy.getClass().getSimpleName());
        });
        
        return DiagnosticInfo.builder()
                .component("ErrorStrategyService")
                .states(states)
                .build();
    }
    
    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }
    
    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info("Diagnostic mode {} for ErrorStrategyService", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Get the number of registered strategies.
     * 
     * @return strategy count
     */
    public int getStrategyCount() {
        return strategies.size();
    }
    
    /**
     * Register default strategies for common exception types.
     */
    public void registerDefaultStrategies() {
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
        
        log.info("Registered {} default error strategies", strategies.size());
    }
}