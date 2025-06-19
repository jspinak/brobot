package io.github.jspinak.brobot.runner.errorhandling;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.ErrorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Central error handling component that manages error processing,
 * recovery, and notification throughout the application.
 * 
 * <p>This component provides a comprehensive error handling framework with:
 * <ul>
 *   <li>Pluggable error processing strategies for different exception types</li>
 *   <li>Error recovery mechanisms with retry capabilities</li>
 *   <li>Error history tracking and statistics</li>
 *   <li>System state enrichment for better error context</li>
 *   <li>Event-based error notification</li>
 * </ul>
 * </p>
 * 
 * <p>The error handler supports registering custom:
 * <ul>
 *   <li>{@link IErrorStrategy} - Define how specific error types should be handled</li>
 *   <li>{@link IErrorProcessor} - Process errors for logging, metrics, notifications</li>
 * </ul>
 * </p>
 * 
 * <p>Error handling workflow:
 * <ol>
 *   <li>Error is received with context information</li>
 *   <li>Context is enriched with system state (memory, CPU, threads)</li>
 *   <li>Error is recorded in history</li>
 *   <li>Appropriate strategy is selected based on error type</li>
 *   <li>All registered processors are executed</li>
 *   <li>Strategy handles the error and returns result</li>
 *   <li>Recovery actions are executed if applicable</li>
 * </ol>
 * </p>
 * 
 * @see IErrorStrategy
 * @see IErrorProcessor
 * @see ErrorContext
 * @see ErrorResult
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorHandler {

    private final EventBus eventBus;
    private final List<IErrorProcessor> errorProcessors = new CopyOnWriteArrayList<>();
    private final Map<Class<? extends Throwable>, IErrorStrategy> errorStrategies = new ConcurrentHashMap<>();
    private final ErrorHistory errorHistory = new ErrorHistory();
    
    // Error metrics
    private final AtomicLong totalErrors = new AtomicLong();
    private final Map<ErrorContext.ErrorCategory, AtomicLong> errorsByCategory = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        // Register default error strategies
        registerDefaultStrategies();
        
        // Register default processors
        registerProcessor(new LoggingErrorProcessor());
        registerProcessor(new EventPublishingProcessor());
        
        log.info("Error handler initialized with {} strategies and {} processors",
            errorStrategies.size(), errorProcessors.size());
    }
    
    /**
     * Handle an error with full context.
     */
    public ErrorResult handleError(Throwable error, ErrorContext context) {
        totalErrors.incrementAndGet();
        errorsByCategory.computeIfAbsent(context.getCategory(), k -> new AtomicLong())
            .incrementAndGet();
        
        // Enrich context with system state
        ErrorContext enrichedContext = enrichContext(context);
        
        // Record in history
        errorHistory.record(error, enrichedContext);
        
        // Find appropriate strategy
        IErrorStrategy strategy = findStrategy(error);
        
        // Process error through all processors
        for (IErrorProcessor processor : errorProcessors) {
            try {
                processor.process(error, enrichedContext);
            } catch (Exception e) {
                log.error("Error processor {} failed", processor.getClass().getSimpleName(), e);
            }
        }
        
        // Execute strategy
        ErrorResult result = strategy.handle(error, enrichedContext);
        
        // Handle recovery if needed
        if (result.isRecoverable() && result.getRecoveryAction() != null) {
            try {
                result.getRecoveryAction().run();
                log.info("Successfully executed recovery action for error: {}", 
                    enrichedContext.getErrorId());
            } catch (Exception e) {
                log.error("Recovery action failed for error: {}", 
                    enrichedContext.getErrorId(), e);
                result = ErrorResult.unrecoverable(
                    "Recovery failed: " + e.getMessage(),
                    enrichedContext.getErrorId()
                );
            }
        }
        
        return result;
    }
    
    /**
     * Handle an error with minimal context.
     */
    public ErrorResult handleError(Throwable error) {
        ErrorContext context = ErrorContext.minimal(
            "Unknown Operation",
            categorizeError(error)
        );
        return handleError(error, context);
    }
    
    /**
     * Register a custom error processor.
     */
    public void registerProcessor(IErrorProcessor processor) {
        errorProcessors.add(processor);
    }
    
    /**
     * Register a custom error strategy for a specific exception type.
     */
    public <T extends Throwable> void registerStrategy(Class<T> errorType, 
                                                      IErrorStrategy strategy) {
        errorStrategies.put(errorType, strategy);
    }
    
    /**
     * Get error statistics.
     */
    public ErrorStatistics getStatistics() {
        Map<ErrorContext.ErrorCategory, Long> categoryStats = new HashMap<>();
        errorsByCategory.forEach((category, count) -> 
            categoryStats.put(category, count.get())
        );
        
        return new ErrorStatistics(
            totalErrors.get(),
            categoryStats,
            errorHistory.getRecentErrors(10),
            errorHistory.getMostFrequentErrors(5)
        );
    }
    
    /**
     * Clear error history.
     */
    public void clearHistory() {
        errorHistory.clear();
        totalErrors.set(0);
        errorsByCategory.clear();
    }
    
    private ErrorContext enrichContext(ErrorContext context) {
        Runtime runtime = Runtime.getRuntime();
        
        return ErrorContext.builder()
            .errorId(context.getErrorId())
            .timestamp(context.getTimestamp())
            .operation(context.getOperation())
            .component(context.getComponent())
            .userId(context.getUserId())
            .sessionId(context.getSessionId())
            .additionalData(context.getAdditionalData())
            .category(context.getCategory())
            .severity(context.getSeverity())
            .recoverable(context.isRecoverable())
            .recoveryHint(context.getRecoveryHint())
            .memoryUsed(runtime.totalMemory() - runtime.freeMemory())
            .activeThreads(ManagementFactory.getThreadMXBean().getThreadCount())
            .cpuUsage(getCpuUsage())
            .build();
    }
    
    private double getCpuUsage() {
        try {
            return ((com.sun.management.OperatingSystemMXBean) 
                ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad();
        } catch (Exception e) {
            return -1;
        }
    }
    
    private IErrorStrategy findStrategy(Throwable error) {
        // Look for exact match
        IErrorStrategy strategy = errorStrategies.get(error.getClass());
        if (strategy != null) {
            return strategy;
        }
        
        // Look for superclass match
        Class<?> currentClass = error.getClass();
        while (currentClass != null && Throwable.class.isAssignableFrom(currentClass)) {
            strategy = errorStrategies.get(currentClass);
            if (strategy != null) {
                return strategy;
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // Return default strategy
        return new DefaultErrorStrategy();
    }
    
    private ErrorContext.ErrorCategory categorizeError(Throwable error) {
        String errorName = error.getClass().getSimpleName().toLowerCase();
        
        if (errorName.contains("file") || errorName.contains("io")) {
            return ErrorContext.ErrorCategory.FILE_IO;
        } else if (errorName.contains("network") || errorName.contains("connection")) {
            return ErrorContext.ErrorCategory.NETWORK;
        } else if (errorName.contains("database") || errorName.contains("sql")) {
            return ErrorContext.ErrorCategory.DATABASE;
        } else if (errorName.contains("validation") || errorName.contains("invalid")) {
            return ErrorContext.ErrorCategory.VALIDATION;
        } else if (errorName.contains("auth")) {
            return ErrorContext.ErrorCategory.AUTHORIZATION;
        } else if (errorName.contains("config")) {
            return ErrorContext.ErrorCategory.CONFIGURATION;
        }
        
        return ErrorContext.ErrorCategory.UNKNOWN;
    }
    
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
    }
    
    /**
     * Default error processor that logs errors.
     */
    private class LoggingErrorProcessor implements IErrorProcessor {
        @Override
        public void process(Throwable error, ErrorContext context) {
            if (context.getSeverity().getLevel() >= ErrorContext.ErrorSeverity.HIGH.getLevel()) {
                log.error("[{}] {} - {} in {}: {}", 
                    context.getErrorId(),
                    context.getCategory().getDisplayName(),
                    context.getSeverity().getDisplayName(),
                    context.getOperation(),
                    error.getMessage(),
                    error);
            } else {
                log.warn("[{}] {} - {} in {}: {}", 
                    context.getErrorId(),
                    context.getCategory().getDisplayName(),
                    context.getSeverity().getDisplayName(),
                    context.getOperation(),
                    error.getMessage());
            }
        }
    }
    
    /**
     * Error processor that publishes error events.
     */
    private class EventPublishingProcessor implements IErrorProcessor {
        @Override
        public void process(Throwable error, ErrorContext context) {
            // Map ErrorContext.ErrorSeverity to ErrorEvent.ErrorSeverity
            ErrorEvent.ErrorSeverity eventSeverity = switch (context.getSeverity()) {
                case LOW -> ErrorEvent.ErrorSeverity.LOW;
                case MEDIUM -> ErrorEvent.ErrorSeverity.MEDIUM;
                case HIGH -> ErrorEvent.ErrorSeverity.HIGH;
                case CRITICAL -> ErrorEvent.ErrorSeverity.FATAL;
            };
            
            ErrorEvent event = new ErrorEvent(
                this,
                error.getMessage(),
                error instanceof Exception ? (Exception) error : new Exception(error),
                eventSeverity,
                context.getComponent() != null ? context.getComponent() : "ErrorHandler"
            );
            eventBus.publish(event);
        }
    }
    
    /**
     * Default error strategy for unhandled exceptions.
     */
    private static class DefaultErrorStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("An unexpected error occurred: " + error.getMessage())
                .technicalDetails(error.toString())
                .build();
        }
    }
    
    /**
     * Strategy for application exceptions.
     */
    private static class ApplicationExceptionStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            ApplicationException appEx = (ApplicationException) error;
            
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(appEx.isRecoverable())
                .userMessage(appEx.getDisplayMessage())
                .technicalDetails(appEx.getTechnicalDetails())
                .recoveryAction(createRecoveryAction(appEx))
                .build();
        }
        
        private Runnable createRecoveryAction(ApplicationException ex) {
            if (!ex.isRecoverable()) {
                return null;
            }
            
            // Create appropriate recovery action based on error category
            return switch (ex.getContext().getCategory()) {
                case FILE_IO -> () -> log.info("Retrying file operation...");
                case NETWORK -> () -> log.info("Retrying network operation...");
                case VALIDATION -> () -> log.info("Awaiting user correction...");
                default -> null;
            };
        }
    }
    
    /**
     * Strategy for null pointer exceptions.
     */
    private static class NullPointerStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("A required value was missing.")
                .technicalDetails("NullPointerException in " + context.getOperation())
                .build();
        }
    }
    
    /**
     * Strategy for illegal argument exceptions.
     */
    private static class IllegalArgumentStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("Invalid input provided: " + error.getMessage())
                .technicalDetails(error.toString())
                .build();
        }
    }
    
    /**
     * Strategy for illegal state exceptions.
     */
    private static class IllegalStateStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("Operation cannot be performed in current state.")
                .technicalDetails(error.getMessage())
                .recoveryAction(() -> log.info("Resetting to valid state..."))
                .build();
        }
    }
    
    /**
     * Strategy for I/O exceptions.
     */
    private static class IOExceptionStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("File operation failed: " + error.getMessage())
                .technicalDetails(error.toString())
                .recoveryAction(() -> log.info("Retrying file operation..."))
                .build();
        }
    }
    
    /**
     * Strategy for file not found exceptions.
     */
    private static class FileNotFoundStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("File not found: " + error.getMessage())
                .technicalDetails("Check file path and permissions")
                .build();
        }
    }
    
    /**
     * Strategy for timeout exceptions.
     */
    private static class TimeoutStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(true)
                .userMessage("Operation timed out. Please try again.")
                .technicalDetails("Timeout after waiting period")
                .recoveryAction(() -> log.info("Retrying with extended timeout..."))
                .build();
        }
    }
    
    /**
     * Strategy for interrupted exceptions.
     */
    private static class InterruptedStrategy implements IErrorStrategy {
        @Override
        public ErrorResult handle(Throwable error, ErrorContext context) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            
            return ErrorResult.builder()
                .errorId(context.getErrorId())
                .success(false)
                .recoverable(false)
                .userMessage("Operation was cancelled.")
                .technicalDetails("Thread interrupted during " + context.getOperation())
                .build();
        }
    }
}