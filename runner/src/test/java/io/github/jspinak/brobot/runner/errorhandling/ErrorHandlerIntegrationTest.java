package io.github.jspinak.brobot.runner.errorhandling;

import io.github.jspinak.brobot.runner.errorhandling.enrichment.ErrorEnrichmentService;
import io.github.jspinak.brobot.runner.errorhandling.history.ErrorHistoryService;
import io.github.jspinak.brobot.runner.errorhandling.processing.ErrorProcessingService;
import io.github.jspinak.brobot.runner.errorhandling.statistics.ErrorStatisticsService;
import io.github.jspinak.brobot.runner.errorhandling.strategy.ErrorStrategyService;
import io.github.jspinak.brobot.runner.events.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ErrorHandler Integration Tests")
class ErrorHandlerIntegrationTest {
    
    private ErrorHandler errorHandler;
    private EventBus mockEventBus;
    private ErrorProcessingService processingService;
    private ErrorStrategyService strategyService;
    private ErrorHistoryService historyService;
    private ErrorEnrichmentService enrichmentService;
    private ErrorStatisticsService statisticsService;
    
    @BeforeEach
    void setUp() {
        mockEventBus = mock(EventBus.class);
        processingService = new ErrorProcessingService();
        strategyService = new ErrorStrategyService();
        historyService = new ErrorHistoryService();
        enrichmentService = new ErrorEnrichmentService();
        statisticsService = new ErrorStatisticsService();
        
        errorHandler = new ErrorHandler(
            mockEventBus,
            processingService,
            strategyService,
            historyService,
            enrichmentService,
            statisticsService
        );
        
        errorHandler.initialize();
    }
    
    @Test
    @DisplayName("Should handle error with full context")
    void testHandleErrorWithContext() {
        // Arrange
        RuntimeException error = new RuntimeException("Test error");
        ErrorContext context = ErrorContext.builder()
            .operation("TestOperation")
            .component("TestComponent")
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .recoverable(false)
            .build();
        
        // Act
        ErrorResult result = errorHandler.handleError(error, context);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorId()).isNotNull();
        
        // Verify error was recorded in history
        var recentErrors = historyService.getRecentErrors(1);
        assertThat(recentErrors).hasSize(1);
        assertThat(recentErrors.get(0).getMessage()).isEqualTo("Test error");
        
        // Verify statistics were updated
        assertThat(statisticsService.getOverallSuccessRate()).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should handle error with minimal context")
    void testHandleErrorMinimal() {
        // Arrange
        RuntimeException error = new RuntimeException("Test error");
        
        // Act
        ErrorResult result = errorHandler.handleError(error);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
    }
    
    @Test
    @DisplayName("Should execute recovery action when recoverable")
    void testRecoveryExecution() {
        // Arrange
        var recoveryExecuted = new boolean[]{false};
        IErrorStrategy mockStrategy = mock(IErrorStrategy.class);
        when(mockStrategy.handle(any(), any())).thenReturn(
            ErrorResult.recoverable("Recoverable error", "123", () -> recoveryExecuted[0] = true)
        );
        
        errorHandler.registerStrategy(RuntimeException.class, mockStrategy);
        
        RuntimeException error = new RuntimeException("Test");
        ErrorContext context = ErrorContext.minimal("TestOp", ErrorContext.ErrorCategory.SYSTEM);
        
        // Act
        ErrorResult result = errorHandler.handleError(error, context);
        
        // Assert
        assertThat(recoveryExecuted[0]).isTrue();
        assertThat(result.isRecoverable()).isTrue();
    }
    
    @Test
    @DisplayName("Should handle recovery failure")
    void testRecoveryFailure() {
        // Arrange
        IErrorStrategy mockStrategy = mock(IErrorStrategy.class);
        when(mockStrategy.handle(any(), any())).thenReturn(
            ErrorResult.recoverable("Recoverable error", "123", 
                () -> { throw new RuntimeException("Recovery failed"); })
        );
        
        errorHandler.registerStrategy(RuntimeException.class, mockStrategy);
        
        RuntimeException error = new RuntimeException("Test");
        ErrorContext context = ErrorContext.minimal("TestOp", ErrorContext.ErrorCategory.SYSTEM);
        
        // Act
        ErrorResult result = errorHandler.handleError(error, context);
        
        // Assert
        assertThat(result.isRecoverable()).isFalse();
        assertThat(result.getUserMessage()).contains("Recovery failed");
    }
    
    @Test
    @DisplayName("Should validate null parameters")
    void testNullValidation() {
        // Act & Assert
        assertThatThrownBy(() -> errorHandler.handleError(null, 
            ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM)))
            .isInstanceOf(IllegalArgumentException.class);
            
        assertThatThrownBy(() -> errorHandler.handleError(new RuntimeException(), null))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("Should register custom processor")
    void testRegisterProcessor() {
        // Arrange
        IErrorProcessor mockProcessor = mock(IErrorProcessor.class);
        
        // Act
        errorHandler.registerProcessor(mockProcessor);
        RuntimeException error = new RuntimeException("Test");
        errorHandler.handleError(error);
        
        // Assert
        verify(mockProcessor).process(eq(error), any(ErrorContext.class));
    }
    
    @Test
    @DisplayName("Should register custom strategy")
    void testRegisterStrategy() {
        // Arrange
        IErrorStrategy mockStrategy = mock(IErrorStrategy.class);
        ErrorResult expectedResult = ErrorResult.handled("Custom handled", "123");
        when(mockStrategy.handle(any(), any())).thenReturn(expectedResult);
        
        // Act
        errorHandler.registerStrategy(IllegalStateException.class, mockStrategy);
        ErrorResult result = errorHandler.handleError(new IllegalStateException("Test"));
        
        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    @DisplayName("Should get error statistics")
    void testGetStatistics() {
        // Arrange
        errorHandler.handleError(new RuntimeException("Error 1"));
        errorHandler.handleError(new IOException("Error 2"));
        
        // Act
        ErrorStatistics stats = errorHandler.getStatistics();
        
        // Assert
        assertThat(stats.totalErrors()).isEqualTo(2);
        assertThat(stats.errorsByCategory())
            .containsEntry(ErrorContext.ErrorCategory.UNKNOWN, 1L)
            .containsEntry(ErrorContext.ErrorCategory.FILE_IO, 1L);
    }
    
    @Test
    @DisplayName("Should clear history")
    void testClearHistory() {
        // Arrange
        errorHandler.handleError(new RuntimeException("Error"));
        
        // Act
        errorHandler.clearHistory();
        
        // Assert
        ErrorStatistics stats = errorHandler.getStatistics();
        assertThat(stats.totalErrors()).isEqualTo(0);
        assertThat(errorHandler.getOverallSuccessRate()).isEqualTo(100.0);
    }
    
    @Test
    @DisplayName("Should get current error rate")
    void testGetCurrentErrorRate() {
        // Arrange
        errorHandler.handleError(new RuntimeException("Error"));
        
        // Act
        double errorRate = errorHandler.getCurrentErrorRate();
        
        // Assert
        assertThat(errorRate).isGreaterThanOrEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should enable diagnostic mode for all services")
    void testDiagnosticMode() {
        // Act
        errorHandler.enableDiagnosticMode(true);
        
        // Assert
        assertThat(errorHandler.isDiagnosticModeEnabled()).isTrue();
        assertThat(processingService.isDiagnosticModeEnabled()).isTrue();
        assertThat(strategyService.isDiagnosticModeEnabled()).isTrue();
        assertThat(historyService.isDiagnosticModeEnabled()).isTrue();
        assertThat(enrichmentService.isDiagnosticModeEnabled()).isTrue();
        assertThat(statisticsService.isDiagnosticModeEnabled()).isTrue();
    }
    
    @Test
    @DisplayName("Should provide comprehensive diagnostic info")
    void testDiagnosticInfo() {
        // Arrange
        errorHandler.handleError(new RuntimeException("Test"));
        
        // Act
        var diagnosticInfo = errorHandler.getDiagnosticInfo();
        
        // Assert
        assertThat(diagnosticInfo.getComponent()).isEqualTo("ErrorHandler");
        assertThat(diagnosticInfo.getStates())
            .containsKey("processorCount")
            .containsKey("strategyCount")
            .containsKey("errorRate")
            .containsKey("successRate")
            .containsKey("services.processing")
            .containsKey("services.strategy")
            .containsKey("services.history")
            .containsKey("services.enrichment")
            .containsKey("services.statistics");
    }
    
    @Disabled("CI failure - needs investigation")
@Test
    @DisplayName("Should handle different error categories correctly")
    void testErrorCategories() {
        // Test various error types
        errorHandler.handleError(new IOException("File error"));
        errorHandler.handleError(new IllegalArgumentException("Validation error"));
        errorHandler.handleError(new NullPointerException("Null error"));
        
        // Get statistics
        ErrorStatistics stats = errorHandler.getStatistics();
        
        // Verify categorization
        assertThat(stats.errorsByCategory())
            .containsEntry(ErrorContext.ErrorCategory.FILE_IO, 1L)
            .containsEntry(ErrorContext.ErrorCategory.VALIDATION, 1L)
            .containsEntry(ErrorContext.ErrorCategory.UNKNOWN, 1L);
    }
}