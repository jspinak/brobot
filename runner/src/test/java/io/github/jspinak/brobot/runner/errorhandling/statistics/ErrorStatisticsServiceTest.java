package io.github.jspinak.brobot.runner.errorhandling.statistics;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ErrorStatisticsService Tests")
class ErrorStatisticsServiceTest {
    
    private ErrorStatisticsService service;
    
    @BeforeEach
    void setUp() {
        service = new ErrorStatisticsService();
    }
    
    @Test
    @DisplayName("Should record operation start")
    void testRecordOperationStart() {
        // Act
        service.recordOperationStart("TestOperation");
        
        // Assert
        var stats = service.getOperationStatistics("TestOperation");
        assertThat(stats.totalAttempts()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should handle null operation name")
    void testRecordNullOperationStart() {
        // Act & Assert
        assertThatNoException().isThrownBy(() -> service.recordOperationStart(null));
    }
    
    @Test
    @DisplayName("Should record successful operation")
    void testRecordSuccessfulOperation() {
        // Arrange
        service.recordOperationStart("TestOp");
        ErrorResult result = ErrorResult.handled("Success", "123");
        
        // Act
        service.recordOperationResult("TestOp", result);
        
        // Assert
        var stats = service.getOperationStatistics("TestOp");
        assertThat(stats.successCount()).isEqualTo(1);
        assertThat(stats.failureCount()).isEqualTo(0);
        assertThat(stats.successRate()).isEqualTo(100.0);
    }
    
    @Test
    @DisplayName("Should record failed operation")
    void testRecordFailedOperation() {
        // Arrange
        service.recordOperationStart("TestOp");
        ErrorResult result = ErrorResult.unrecoverable("Failed", "123");
        
        // Act
        service.recordOperationResult("TestOp", result);
        
        // Assert
        var stats = service.getOperationStatistics("TestOp");
        assertThat(stats.successCount()).isEqualTo(0);
        assertThat(stats.failureCount()).isEqualTo(1);
        assertThat(stats.successRate()).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should record recoverable operation")
    void testRecordRecoverableOperation() {
        // Arrange
        service.recordOperationStart("TestOp");
        ErrorResult result = ErrorResult.recoverable("Recoverable", "123", () -> {});
        
        // Act
        service.recordOperationResult("TestOp", result);
        
        // Assert
        assertThat(service.getOverallSuccessRate()).isEqualTo(0.0); // Still a failure
    }
    
    @Test
    @DisplayName("Should handle null parameters in recordOperationResult")
    void testRecordNullOperationResult() {
        // Act & Assert
        assertThatNoException().isThrownBy(() -> 
            service.recordOperationResult(null, ErrorResult.handled("Test", "123")));
        assertThatNoException().isThrownBy(() -> 
            service.recordOperationResult("TestOp", null));
    }
    
    @Test
    @DisplayName("Should record error")
    void testRecordError() {
        // Arrange
        Exception error = new RuntimeException("Test error");
        ErrorContext context = ErrorContext.minimal("TestOp", ErrorContext.ErrorCategory.SYSTEM);
        
        // Act
        service.recordError(error, context);
        
        // Assert
        assertThat(service.getCurrentErrorRate()).isGreaterThanOrEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should handle null parameters in recordError")
    void testRecordNullError() {
        // Act & Assert
        assertThatNoException().isThrownBy(() -> 
            service.recordError(null, ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM)));
        assertThatNoException().isThrownBy(() -> 
            service.recordError(new RuntimeException(), null));
    }
    
    @Test
    @DisplayName("Should calculate overall success rate")
    void testOverallSuccessRate() {
        // Arrange
        for (int i = 0; i < 3; i++) {
            service.recordOperationStart("Op" + i);
            service.recordOperationResult("Op" + i, ErrorResult.handled("Success", "id"));
        }
        
        service.recordOperationStart("FailOp");
        service.recordOperationResult("FailOp", ErrorResult.unrecoverable("Failed", "id"));
        
        // Act
        double successRate = service.getOverallSuccessRate();
        
        // Assert
        assertThat(successRate).isEqualTo(75.0); // 3 out of 4 successful
    }
    
    @Test
    @DisplayName("Should return 100% success rate when no operations")
    void testSuccessRateNoOperations() {
        // Act
        double successRate = service.getOverallSuccessRate();
        
        // Assert
        assertThat(successRate).isEqualTo(100.0);
    }
    
    @Test
    @DisplayName("Should calculate mean time between failures")
    void testMeanTimeBetweenFailures() {
        // Arrange
        service.recordError(new RuntimeException(), 
            ErrorContext.minimal("Op1", ErrorContext.ErrorCategory.SYSTEM));
        
        // Sleep to ensure time difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        service.recordError(new RuntimeException(), 
            ErrorContext.minimal("Op2", ErrorContext.ErrorCategory.SYSTEM));
        
        // Act
        long mtbf = service.getMeanTimeBetweenFailures();
        
        // Assert
        assertThat(mtbf).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    @DisplayName("Should return -1 for MTBF with insufficient data")
    void testMTBFInsufficientData() {
        // Act - no errors
        assertThat(service.getMeanTimeBetweenFailures()).isEqualTo(-1);
        
        // Act - one error
        service.recordError(new RuntimeException(), 
            ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));
        assertThat(service.getMeanTimeBetweenFailures()).isEqualTo(-1);
    }
    
    @Test
    @DisplayName("Should get operation statistics for unknown operation")
    void testGetUnknownOperationStatistics() {
        // Act
        var stats = service.getOperationStatistics("UnknownOp");
        
        // Assert
        assertThat(stats.operation()).isEqualTo("UnknownOp");
        assertThat(stats.totalAttempts()).isEqualTo(0);
        assertThat(stats.successCount()).isEqualTo(0);
        assertThat(stats.successRate()).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("Should get top error operations")
    void testGetTopErrorOperations() {
        // Arrange
        // Create operations with different error counts
        for (int i = 0; i < 5; i++) {
            service.recordOperationStart("HighErrorOp");
            service.recordOperationResult("HighErrorOp", 
                ErrorResult.unrecoverable("Failed", "id"));
        }
        
        for (int i = 0; i < 2; i++) {
            service.recordOperationStart("LowErrorOp");
            service.recordOperationResult("LowErrorOp", 
                ErrorResult.unrecoverable("Failed", "id"));
        }
        
        service.recordOperationStart("NoErrorOp");
        service.recordOperationResult("NoErrorOp", 
            ErrorResult.handled("Success", "id"));
        
        // Act
        List<ErrorStatisticsService.OperationStatistics> topErrors = 
            service.getTopErrorOperations(2);
        
        // Assert
        assertThat(topErrors).hasSize(2);
        assertThat(topErrors.get(0).operation()).isEqualTo("HighErrorOp");
        assertThat(topErrors.get(0).errorCount()).isEqualTo(5);
        assertThat(topErrors.get(1).operation()).isEqualTo("LowErrorOp");
        assertThat(topErrors.get(1).errorCount()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should reset statistics")
    void testReset() {
        // Arrange
        service.recordOperationStart("Op");
        service.recordOperationResult("Op", ErrorResult.handled("Success", "id"));
        service.recordError(new RuntimeException(), 
            ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));
        
        // Act
        service.reset();
        
        // Assert
        assertThat(service.getOverallSuccessRate()).isEqualTo(100.0);
        assertThat(service.getCurrentErrorRate()).isEqualTo(0.0);
        assertThat(service.getMeanTimeBetweenFailures()).isEqualTo(-1);
    }
    
    @Test
    @DisplayName("Should get error rate history")
    void testGetErrorRateHistory() {
        // Arrange
        service.recordError(new RuntimeException(), 
            ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));
        
        // Act
        Map<Instant, ErrorStatisticsService.ErrorRateSnapshot> history = 
            service.getErrorRateHistory(24);
        
        // Assert
        assertThat(history).isNotNull();
        // Current snapshot not yet added to history (added by scheduled method)
    }
    
    @Test
    @DisplayName("Should provide diagnostic info")
    void testDiagnosticInfo() {
        // Arrange
        service.recordOperationStart("Op");
        service.recordOperationResult("Op", ErrorResult.handled("Success", "id"));
        
        // Act
        var diagnosticInfo = service.getDiagnosticInfo();
        
        // Assert
        assertThat(diagnosticInfo.getComponent()).isEqualTo("ErrorStatisticsService");
        assertThat(diagnosticInfo.getStates())
            .containsKey("totalOperations")
            .containsKey("successRate")
            .containsKey("errorRate");
    }
    
    @Test
    @DisplayName("Should enable diagnostic mode")
    void testDiagnosticMode() {
        // Act
        service.enableDiagnosticMode(true);
        
        // Assert
        assertThat(service.isDiagnosticModeEnabled()).isTrue();
    }
}