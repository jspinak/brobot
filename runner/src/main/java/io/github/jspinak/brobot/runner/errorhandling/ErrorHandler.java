package io.github.jspinak.brobot.runner.errorhandling;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.errorhandling.enrichment.ErrorEnrichmentService;
import io.github.jspinak.brobot.runner.errorhandling.history.ErrorHistoryService;
import io.github.jspinak.brobot.runner.errorhandling.processing.ErrorProcessingService;
import io.github.jspinak.brobot.runner.errorhandling.statistics.ErrorStatisticsService;
import io.github.jspinak.brobot.runner.errorhandling.strategy.ErrorStrategyService;
import io.github.jspinak.brobot.runner.events.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central error handling orchestrator that coordinates error processing
 * across specialized services.
 * 
 * This component acts as a thin orchestrator that delegates specific
 * responsibilities to specialized services following the Single
 * Responsibility Principle:
 * 
 * - ErrorProcessingService: Manages error processor pipeline
 * - ErrorStrategyService: Manages error handling strategies  
 * - ErrorHistoryService: Tracks error history
 * - ErrorEnrichmentService: Enriches error context
 * - ErrorStatisticsService: Collects error statistics
 * 
 * The error handler coordinates these services to provide comprehensive
 * error handling, recovery, and analysis capabilities.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @see ErrorProcessingService
 * @see ErrorStrategyService
 * @see ErrorHistoryService
 * @see ErrorEnrichmentService
 * @see ErrorStatisticsService
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorHandler implements DiagnosticCapable {

    private final EventBus eventBus;
    private final ErrorProcessingService processingService;
    private final ErrorStrategyService strategyService;
    private final ErrorHistoryService historyService;
    private final ErrorEnrichmentService enrichmentService;
    private final ErrorStatisticsService statisticsService;
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    @PostConstruct
    public void initialize() {
        // Initialize default processors
        processingService.registerProcessor(new LoggingErrorProcessor());
        processingService.registerProcessor(new EventPublishingProcessor(eventBus));
        
        // Initialize default strategies
        strategyService.registerDefaultStrategies();
        
        log.info("Error handler initialized with {} processors and {} strategies",
            processingService.getProcessorCount(), strategyService.getStrategyCount());
    }
    
    /**
     * Handle an error with full context.
     * 
     * @param error the error to handle
     * @param context the error context
     * @return result of error handling
     */
    public ErrorResult handleError(Throwable error, ErrorContext context) {
        if (error == null || context == null) {
            throw new IllegalArgumentException("Error and context must not be null");
        }
        
        String operation = context.getOperation();
        
        // Record operation start in statistics
        statisticsService.recordOperationStart(operation);
        
        // Enrich context with system state
        ErrorContext enrichedContext = enrichmentService.enrichContext(context);
        
        // Record in history
        historyService.record(error, enrichedContext);
        
        // Record error in statistics
        statisticsService.recordError(error, enrichedContext);
        
        // Process error through processor pipeline
        processingService.processError(error, enrichedContext);
        
        // Execute appropriate strategy
        ErrorResult result = strategyService.handleError(error, enrichedContext);
        
        // Handle recovery if needed
        if (result.isRecoverable() && result.getRecoveryAction() != null) {
            result = attemptRecovery(result, enrichedContext);
        }
        
        // Record operation result in statistics
        statisticsService.recordOperationResult(operation, result);
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Handled error {} - Type: {}, Result: {}, Recoverable: {}",
                    enrichedContext.getErrorId(),
                    error.getClass().getSimpleName(),
                    result.isSuccess() ? "Success" : "Failed",
                    result.isRecoverable());
        }
        
        return result;
    }
    
    /**
     * Handle an error with minimal context.
     * 
     * @param error the error to handle
     * @return result of error handling
     */
    public ErrorResult handleError(Throwable error) {
        ErrorContext.ErrorCategory category = enrichmentService.categorizeError(error);
        ErrorContext context = ErrorContext.minimal("Unknown Operation", category);
        return handleError(error, context);
    }
    
    /**
     * Register a custom error processor.
     * 
     * @param processor the processor to register
     */
    public void registerProcessor(IErrorProcessor processor) {
        processingService.registerProcessor(processor);
    }
    
    /**
     * Register a custom error strategy for a specific exception type.
     * 
     * @param errorType the error type
     * @param strategy the strategy to use
     */
    public <T extends Throwable> void registerStrategy(Class<T> errorType, 
                                                      IErrorStrategy strategy) {
        strategyService.registerStrategy(errorType, strategy);
    }
    
    /**
     * Get comprehensive error statistics.
     * 
     * @return error statistics
     */
    public ErrorStatistics getStatistics() {
        ErrorHistory.ErrorHistoryStatistics historyStats = historyService.getStatistics();
        
        return new ErrorStatistics(
            historyStats.totalErrors(),
            historyStats.errorsByCategory(),
            historyService.getRecentErrors(10),
            historyStats.mostFrequent()
        );
    }
    
    /**
     * Clear all error history and statistics.
     */
    public void clearHistory() {
        historyService.clear();
        statisticsService.reset();
        log.info("Error history and statistics cleared");
    }
    
    /**
     * Get current error rate.
     * 
     * @return errors per minute
     */
    public double getCurrentErrorRate() {
        return statisticsService.getCurrentErrorRate();
    }
    
    /**
     * Get overall success rate.
     * 
     * @return success rate percentage
     */
    public double getOverallSuccessRate() {
        return statisticsService.getOverallSuccessRate();
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new ConcurrentHashMap<>();
        
        // Aggregate diagnostic info from all services
        states.put("processorCount", processingService.getProcessorCount());
        states.put("strategyCount", strategyService.getStrategyCount());
        states.put("errorRate", getCurrentErrorRate());
        states.put("successRate", getOverallSuccessRate());
        
        // Get recent errors
        var recentErrors = historyService.getRecentErrors(5);
        states.put("recentErrorCount", recentErrors.size());
        
        // Include diagnostic info from services
        states.put("services.processing", processingService.getDiagnosticInfo());
        states.put("services.strategy", strategyService.getDiagnosticInfo());
        states.put("services.history", historyService.getDiagnosticInfo());
        states.put("services.enrichment", enrichmentService.getDiagnosticInfo());
        states.put("services.statistics", statisticsService.getDiagnosticInfo());
        
        return DiagnosticInfo.builder()
                .component("ErrorHandler")
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
        
        // Propagate to all services
        processingService.enableDiagnosticMode(enabled);
        strategyService.enableDiagnosticMode(enabled);
        historyService.enableDiagnosticMode(enabled);
        enrichmentService.enableDiagnosticMode(enabled);
        statisticsService.enableDiagnosticMode(enabled);
        
        log.info("Diagnostic mode {} for ErrorHandler and all services", 
                enabled ? "enabled" : "disabled");
    }
    
    private ErrorResult attemptRecovery(ErrorResult result, ErrorContext context) {
        try {
            result.getRecoveryAction().run();
            log.info("Successfully executed recovery action for error: {}", 
                context.getErrorId());
            return result;
        } catch (Exception e) {
            log.error("Recovery action failed for error: {}", 
                context.getErrorId(), e);
            return ErrorResult.unrecoverable(
                "Recovery failed: " + e.getMessage(),
                context.getErrorId()
            );
        }
    }
    
    /**
     * Default error processor that logs errors.
     */
    private static class LoggingErrorProcessor implements IErrorProcessor {
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
    private static class EventPublishingProcessor implements IErrorProcessor {
        private final EventBus eventBus;
        
        EventPublishingProcessor(EventBus eventBus) {
            this.eventBus = eventBus;
        }
        
        @Override
        public void process(Throwable error, ErrorContext context) {
            // Create and publish error event
            io.github.jspinak.brobot.runner.events.ErrorEvent.ErrorSeverity eventSeverity = 
                switch (context.getSeverity()) {
                    case LOW -> io.github.jspinak.brobot.runner.events.ErrorEvent.ErrorSeverity.LOW;
                    case MEDIUM -> io.github.jspinak.brobot.runner.events.ErrorEvent.ErrorSeverity.MEDIUM;
                    case HIGH -> io.github.jspinak.brobot.runner.events.ErrorEvent.ErrorSeverity.HIGH;
                    case CRITICAL -> io.github.jspinak.brobot.runner.events.ErrorEvent.ErrorSeverity.FATAL;
                };
            
            io.github.jspinak.brobot.runner.events.ErrorEvent event = 
                new io.github.jspinak.brobot.runner.events.ErrorEvent(
                    this,
                    error.getMessage(),
                    error instanceof Exception ? (Exception) error : new Exception(error),
                    eventSeverity,
                    context.getComponent() != null ? context.getComponent() : "ErrorHandler"
                );
            eventBus.publish(event);
        }
    }
}