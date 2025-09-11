package io.github.jspinak.brobot.runner.errorhandling;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.errorhandling.services.*;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Refactored central error handling component that delegates to specialized services.
 *
 * <p>This class now acts as a facade that coordinates between different error services:
 *
 * <ul>
 *   <li>ErrorStrategyManager - Manages error handling strategies
 *   <li>ErrorProcessingPipeline - Manages error processors
 *   <li>ErrorMetricsService - Tracks error statistics
 *   <li>ErrorContextEnricher - Enriches error context with system state
 * </ul>
 *
 * <p>This refactoring follows the Single Responsibility Principle by separating concerns into
 * focused, testable services.
 *
 * @see ErrorStrategyManager
 * @see ErrorProcessingPipeline
 * @see ErrorMetricsService
 * @see ErrorContextEnricher
 */
@Slf4j
@Component
@Data
public class RefactoredErrorHandler {

    private final ErrorStrategyManager strategyManager;
    private final ErrorProcessingPipeline processingPipeline;
    private final ErrorMetricsService metricsService;
    private final ErrorContextEnricher contextEnricher;

    @Autowired
    public RefactoredErrorHandler(
            ErrorStrategyManager strategyManager,
            ErrorProcessingPipeline processingPipeline,
            ErrorMetricsService metricsService,
            ErrorContextEnricher contextEnricher) {

        this.strategyManager = strategyManager;
        this.processingPipeline = processingPipeline;
        this.metricsService = metricsService;
        this.contextEnricher = contextEnricher;
    }

    @PostConstruct
    public void initialize() {
        log.info(
                "Error handler initialized with {} strategies and {} processors",
                strategyManager.getStrategyCount(),
                processingPipeline.getProcessorCount());
    }

    /** Handles an error with full context. */
    public ErrorResult handleError(Throwable error, ErrorContext context) {
        if (error == null) {
            log.warn("Null error passed to error handler");
            return ErrorResult.builder()
                    .errorId("unknown")
                    .success(false)
                    .recoverable(false)
                    .userMessage("An unknown error occurred")
                    .build();
        }

        // Enrich context with system state
        ErrorContext enrichedContext = contextEnricher.enrich(context);

        // Record metrics
        metricsService.recordError(error, enrichedContext);

        // Find appropriate strategy
        IErrorStrategy strategy = strategyManager.findStrategy(error);

        // Process error through pipeline
        processingPipeline.process(error, enrichedContext);

        // Execute strategy
        ErrorResult result = strategy.handle(error, enrichedContext);

        // Handle recovery if needed
        if (result.isRecoverable() && result.getRecoveryAction() != null) {
            result = executeRecovery(result, enrichedContext);
        }

        log.debug(
                "Error handled: {} - Result: {}",
                error.getClass().getSimpleName(),
                result.isSuccess() ? "success" : "failure");

        return result;
    }

    /** Handles an error with minimal context. */
    public ErrorResult handleError(Throwable error) {
        ErrorContext context =
                ErrorContext.minimal("Unknown Operation", contextEnricher.categorizeError(error));
        return handleError(error, context);
    }

    /** Handles an error with operation context. */
    public ErrorResult handleError(Throwable error, String operation) {
        ErrorContext context =
                ErrorContext.builder()
                        .operation(operation)
                        .category(contextEnricher.categorizeError(error))
                        .severity(contextEnricher.determineSeverity(error, null))
                        .build();
        return handleError(error, context);
    }

    /** Registers a custom error processor. */
    public void registerProcessor(IErrorProcessor processor) {
        processingPipeline.registerProcessor(processor);
    }

    /** Registers a custom error strategy for a specific exception type. */
    public <T extends Throwable> void registerStrategy(
            Class<T> errorType, IErrorStrategy strategy) {
        strategyManager.registerStrategy(errorType, strategy);
    }

    /** Gets error statistics. */
    public ErrorStatistics getStatistics() {
        return metricsService.getStatistics();
    }

    /** Gets detailed error metrics. */
    public ErrorMetricsService.DetailedMetrics getDetailedMetrics() {
        return metricsService.getDetailedMetrics();
    }

    /** Clears error history and metrics. */
    public void clearHistory() {
        metricsService.clearMetrics();
    }

    /** Gets error count by category. */
    public long getErrorCountByCategory(ErrorContext.ErrorCategory category) {
        return metricsService.getErrorCountByCategory(category);
    }

    /** Gets error count by severity. */
    public long getErrorCountBySeverity(ErrorContext.ErrorSeverity severity) {
        return metricsService.getErrorCountBySeverity(severity);
    }

    /** Calculates current error rate (errors per minute). */
    public double getErrorRate() {
        return metricsService.calculateErrorRate();
    }

    /** Executes recovery action with proper error handling. */
    private ErrorResult executeRecovery(ErrorResult originalResult, ErrorContext context) {
        try {
            originalResult.getRecoveryAction().run();
            log.info("Successfully executed recovery action for error: {}", context.getErrorId());
            return originalResult;
        } catch (Exception e) {
            log.error("Recovery action failed for error: {}", context.getErrorId(), e);
            return ErrorResult.builder()
                    .errorId(context.getErrorId())
                    .success(false)
                    .recoverable(false)
                    .userMessage(originalResult.getUserMessage() + " (Recovery failed)")
                    .technicalDetails("Recovery failed: " + e.getMessage())
                    .build();
        }
    }

    /** Convenience method to create an error context for a component operation. */
    public ErrorContext createContext(
            String component, String operation, ErrorContext.ErrorCategory category) {
        return ErrorContext.builder()
                .component(component)
                .operation(operation)
                .category(category)
                .severity(ErrorContext.ErrorSeverity.MEDIUM)
                .build();
    }

    /** Checks if a specific processor type is registered. */
    public boolean hasProcessor(Class<? extends IErrorProcessor> processorType) {
        return processingPipeline.hasProcessorType(processorType);
    }

    /** Checks if a strategy is registered for a specific error type. */
    public boolean hasStrategy(Class<? extends Throwable> errorType) {
        return strategyManager.hasStrategy(errorType);
    }

    /** Gets the list of registered processor names. */
    public List<String> getProcessorNames() {
        return processingPipeline.getProcessorNames();
    }

    /** Gets metrics for a specific time period. */
    public ErrorMetricsService.PeriodMetrics getMetricsForPeriod(
            java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return metricsService.getMetricsForPeriod(start, end);
    }
}
