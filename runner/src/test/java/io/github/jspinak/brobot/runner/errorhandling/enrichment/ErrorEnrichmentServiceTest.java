package io.github.jspinak.brobot.runner.errorhandling.enrichment;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ErrorEnrichmentService Tests")
class ErrorEnrichmentServiceTest {
    
    private ErrorEnrichmentService service;
    
    @BeforeEach
    void setUp() {
        service = new ErrorEnrichmentService();
    }
    
    @Test
    @DisplayName("Should enrich context with system state")
    void testEnrichContext() {
        // Arrange
        ErrorContext context = ErrorContext.minimal("TestOperation", ErrorContext.ErrorCategory.SYSTEM);
        
        // Act
        ErrorContext enriched = service.enrichContext(context);
        
        // Assert
        assertThat(enriched.getErrorId()).isNotNull();
        assertThat(enriched.getMemoryUsed()).isGreaterThan(0);
        assertThat(enriched.getActiveThreads()).isGreaterThan(0);
        assertThat(enriched.getCpuUsage()).isGreaterThanOrEqualTo(-1); // -1 if can't get CPU
    }
    
    @Test
    @DisplayName("Should preserve original context data")
    void testEnrichContextPreservesData() {
        // Arrange
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("key", "value");
        
        ErrorContext context = ErrorContext.builder()
            .operation("TestOp")
            .component("TestComponent")
            .userId("user123")
            .sessionId("session456")
            .additionalData(additionalData)
            .category(ErrorContext.ErrorCategory.VALIDATION)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .recoverable(true)
            .recoveryHint("Try again")
            .build();
        
        // Act
        ErrorContext enriched = service.enrichContext(context);
        
        // Assert
        assertThat(enriched.getOperation()).isEqualTo("TestOp");
        assertThat(enriched.getComponent()).isEqualTo("TestComponent");
        assertThat(enriched.getUserId()).isEqualTo("user123");
        assertThat(enriched.getSessionId()).isEqualTo("session456");
        assertThat(enriched.getAdditionalData()).containsEntry("key", "value");
        assertThat(enriched.getCategory()).isEqualTo(ErrorContext.ErrorCategory.VALIDATION);
        assertThat(enriched.getSeverity()).isEqualTo(ErrorContext.ErrorSeverity.HIGH);
        assertThat(enriched.isRecoverable()).isTrue();
        assertThat(enriched.getRecoveryHint()).isEqualTo("Try again");
    }
    
    @Test
    @DisplayName("Should generate error ID if not present")
    void testGenerateErrorId() {
        // Arrange
        ErrorContext context = ErrorContext.minimal("TestOp", ErrorContext.ErrorCategory.SYSTEM);
        
        // Act
        ErrorContext enriched = service.enrichContext(context);
        
        // Assert
        assertThat(enriched.getErrorId()).isNotNull();
        assertThat(enriched.getErrorId()).startsWith("ERR-");
    }
    
    @Test
    @DisplayName("Should throw exception for null context")
    void testEnrichNullContext() {
        // Act & Assert
        assertThatThrownBy(() -> service.enrichContext(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Context cannot be null");
    }
    
    @Test
    @DisplayName("Should categorize file IO errors")
    void testCategorizeFileIOError() {
        // Act
        ErrorContext.ErrorCategory category = service.categorizeError(new IOException("File not found"));
        
        // Assert
        assertThat(category).isEqualTo(ErrorContext.ErrorCategory.FILE_IO);
    }
    
    @Test
    @DisplayName("Should categorize database errors")
    void testCategorizeDatabaseError() {
        // Act
        ErrorContext.ErrorCategory category = service.categorizeError(new SQLException("Connection failed"));
        
        // Assert
        assertThat(category).isEqualTo(ErrorContext.ErrorCategory.DATABASE);
    }
    
    @Test
    @DisplayName("Should categorize validation errors")
    void testCategorizeValidationError() {
        // Act
        ErrorContext.ErrorCategory category = service.categorizeError(
            new IllegalArgumentException("Invalid input"));
        
        // Assert
        assertThat(category).isEqualTo(ErrorContext.ErrorCategory.VALIDATION);
    }
    
    @Test
    @DisplayName("Should categorize unknown errors")
    void testCategorizeUnknownError() {
        // Act
        ErrorContext.ErrorCategory category = service.categorizeError(new RuntimeException("Random error"));
        
        // Assert
        assertThat(category).isEqualTo(ErrorContext.ErrorCategory.UNKNOWN);
    }
    
    @Test
    @DisplayName("Should handle null error in categorization")
    void testCategorizeNullError() {
        // Act
        ErrorContext.ErrorCategory category = service.categorizeError(null);
        
        // Assert
        assertThat(category).isEqualTo(ErrorContext.ErrorCategory.UNKNOWN);
    }
    
    @Test
    @DisplayName("Should capture system state")
    void testCaptureSystemState() {
        // Act
        ErrorEnrichmentService.SystemState state = service.captureSystemState();
        
        // Assert
        assertThat(state.getMemoryUsed()).isGreaterThan(0);
        assertThat(state.getMemoryTotal()).isGreaterThan(0);
        assertThat(state.getMemoryMax()).isGreaterThan(0);
        assertThat(state.getActiveThreads()).isGreaterThan(0);
        assertThat(state.getAvailableProcessors()).isGreaterThan(0);
        // CPU usage might be -1 if not available
        assertThat(state.getCpuUsage()).isGreaterThanOrEqualTo(-1);
    }
    
    @Test
    @DisplayName("Should generate unique error IDs")
    void testGenerateUniqueErrorIds() {
        // Act
        String id1 = service.generateErrorId();
        String id2 = service.generateErrorId();
        
        // Assert
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).startsWith("ERR-");
        assertThat(id2).startsWith("ERR-");
    }
    
    @Test
    @DisplayName("Should track enrichment statistics")
    void testTrackStatistics() {
        // Arrange
        ErrorContext context1 = ErrorContext.minimal("Op1", ErrorContext.ErrorCategory.SYSTEM);
        ErrorContext context2 = ErrorContext.minimal("Op2", ErrorContext.ErrorCategory.VALIDATION);
        
        // Act
        service.enrichContext(context1);
        service.enrichContext(context2);
        
        var diagnosticInfo = service.getDiagnosticInfo();
        
        // Assert
        assertThat(diagnosticInfo.getStates())
            .containsEntry("totalEnrichments", 2L)
            .containsEntry("category.SYSTEM.count", 1L)
            .containsEntry("category.VALIDATION.count", 1L);
    }
    
    @Test
    @DisplayName("Should enable diagnostic mode")
    void testDiagnosticMode() {
        // Act
        service.enableDiagnosticMode(true);
        
        // Assert
        assertThat(service.isDiagnosticModeEnabled()).isTrue();
        
        // Act
        service.enableDiagnosticMode(false);
        
        // Assert
        assertThat(service.isDiagnosticModeEnabled()).isFalse();
    }
    
    @Test
    @DisplayName("Should provide diagnostic info")
    void testDiagnosticInfo() {
        // Arrange
        service.enrichContext(ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));
        
        // Act
        var diagnosticInfo = service.getDiagnosticInfo();
        
        // Assert
        assertThat(diagnosticInfo.getComponent()).isEqualTo("ErrorEnrichmentService");
        assertThat(diagnosticInfo.getStates())
            .containsKey("totalEnrichments")
            .containsKey("system.memoryUsedMB")
            .containsKey("system.activeThreads");
    }
}