package io.github.jspinak.brobot.runner.errorhandling;

import io.github.jspinak.brobot.runner.events.ErrorEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.BrobotEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ErrorHandlerTest {

    @Mock
    private EventBus eventBus;
    
    private ErrorHandler errorHandler;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        errorHandler = new ErrorHandler(eventBus);
        errorHandler.initialize();
    }
    
    @Test
    @DisplayName("Should handle ApplicationException with proper context")
    void shouldHandleApplicationException() {
        // Create an application exception
        ApplicationException exception = ApplicationException.validationError(
            "Invalid email format", 
            "email"
        );
        
        // Handle the error
        ErrorResult result = errorHandler.handleError(exception);
        
        // Verify result
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.isRecoverable());
        assertTrue(result.getUserMessage().contains("Invalid email format"));
        assertTrue(result.getUserMessage().contains("email"));
    }
    
    @Test
    @DisplayName("Should publish error event when handling errors")
    void shouldPublishErrorEvent() {
        // Create error context
        ErrorContext context = ErrorContext.builder()
            .operation("Test Operation")
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .recoverable(false)
            .build();
            
        Exception error = new RuntimeException("Test error");
        
        // Handle the error
        errorHandler.handleError(error, context);
        
        // Capture published event
        ArgumentCaptor<BrobotEvent> eventCaptor = ArgumentCaptor.forClass(BrobotEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        ErrorEvent publishedEvent = (ErrorEvent) eventCaptor.getValue();
        assertEquals("Test error", publishedEvent.getErrorMessage());
        assertEquals(ErrorEvent.ErrorSeverity.HIGH, publishedEvent.getSeverity());
        assertEquals("ErrorHandler", publishedEvent.getComponentName());
    }
    
    @Test
    @DisplayName("Should categorize errors correctly")
    void shouldCategorizeErrors() {
        // File I/O error
        IOException ioError = new IOException("File not found");
        ErrorResult ioResult = errorHandler.handleError(ioError);
        assertTrue(ioResult.getUserMessage().contains("File operation failed"));
        
        // Validation error
        IllegalArgumentException validationError = new IllegalArgumentException("Invalid input");
        ErrorResult validationResult = errorHandler.handleError(validationError);
        assertTrue(validationResult.getUserMessage().contains("Invalid input provided"));
        
        // Null pointer error
        NullPointerException nullError = new NullPointerException();
        ErrorResult nullResult = errorHandler.handleError(nullError);
        assertTrue(nullResult.getUserMessage().contains("required value was missing"));
    }
    
    @Test
    @DisplayName("Should track error statistics")
    void shouldTrackErrorStatistics() {
        // Generate some errors
        for (int i = 0; i < 5; i++) {
            ErrorContext context = ErrorContext.builder()
                .operation("Operation " + i)
                .category(ErrorContext.ErrorCategory.FILE_IO)
                .severity(ErrorContext.ErrorSeverity.MEDIUM)
                .build();
            errorHandler.handleError(new IOException("Test IO error " + i), context);
        }
        
        for (int i = 0; i < 3; i++) {
            ErrorContext context = ErrorContext.builder()
                .operation("Operation " + i)
                .category(ErrorContext.ErrorCategory.NETWORK)
                .severity(ErrorContext.ErrorSeverity.HIGH)
                .build();
            errorHandler.handleError(new RuntimeException("Network error " + i), context);
        }
        
        // Get statistics
        ErrorStatistics stats = errorHandler.getStatistics();
        
        // Verify statistics
        assertEquals(8, stats.totalErrors());
        assertEquals(5, stats.errorsByCategory().get(ErrorContext.ErrorCategory.FILE_IO));
        assertEquals(3, stats.errorsByCategory().get(ErrorContext.ErrorCategory.NETWORK));
        assertFalse(stats.recentErrors().isEmpty());
    }
    
    @Test
    @DisplayName("Should execute recovery actions for recoverable errors")
    void shouldExecuteRecoveryActions() {
        AtomicBoolean recoveryExecuted = new AtomicBoolean(false);
        
        // Register custom strategy with recovery action
        errorHandler.registerStrategy(TestException.class, (error, context) -> 
            ErrorResult.recoverable(
                "Test error occurred",
                context.getErrorId(),
                () -> recoveryExecuted.set(true)
            )
        );
        
        // Handle error
        ErrorContext context = ErrorContext.recoverable(
            "Test Operation",
            ErrorContext.ErrorCategory.SYSTEM,
            "Retry the operation"
        );
        
        errorHandler.handleError(new TestException(), context);
        
        // Verify recovery was executed
        assertTrue(recoveryExecuted.get());
    }
    
    @Test
    @DisplayName("Should handle errors from custom processors")
    void shouldHandleCustomProcessors() {
        AtomicBoolean processorCalled = new AtomicBoolean(false);
        
        // Register custom processor
        errorHandler.registerProcessor((error, context) -> {
            processorCalled.set(true);
            assertEquals("Custom error", error.getMessage());
        });
        
        // Handle error
        errorHandler.handleError(new RuntimeException("Custom error"));
        
        // Verify processor was called
        assertTrue(processorCalled.get());
    }
    
    @Test
    @DisplayName("Should handle processor failures gracefully")
    void shouldHandleProcessorFailures() {
        // Register failing processor
        errorHandler.registerProcessor((error, context) -> {
            throw new RuntimeException("Processor failure");
        });
        
        // Handle error - should not throw
        assertDoesNotThrow(() -> {
            errorHandler.handleError(new RuntimeException("Test error"));
        });
    }
    
    @Test
    @DisplayName("Should enrich context with system information")
    void shouldEnrichContext() {
        ErrorContext basicContext = ErrorContext.minimal(
            "Test Operation",
            ErrorContext.ErrorCategory.SYSTEM
        );
        
        // Handle error to trigger context enrichment
        ErrorResult result = errorHandler.handleError(
            new RuntimeException("Test"), 
            basicContext
        );
        
        // Verify event was published with enriched context
        ArgumentCaptor<BrobotEvent> eventCaptor = ArgumentCaptor.forClass(BrobotEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        ErrorEvent event = (ErrorEvent) eventCaptor.getValue();
        assertNotNull(event.getErrorMessage());
    }
    
    @Test
    @DisplayName("Should find strategies for exception hierarchies")
    void shouldFindStrategiesForHierarchies() {
        AtomicBoolean strategyCalled = new AtomicBoolean(false);
        
        // Register strategy for parent exception
        errorHandler.registerStrategy(IOException.class, (error, context) -> {
            strategyCalled.set(true);
            return ErrorResult.handled("IO error handled", context.getErrorId());
        });
        
        // Handle child exception
        errorHandler.handleError(new java.io.FileNotFoundException("test.txt"));
        
        // Verify parent strategy was used
        assertTrue(strategyCalled.get());
    }
    
    @Test
    @DisplayName("Should clear error history")
    void shouldClearHistory() {
        // Generate some errors
        for (int i = 0; i < 5; i++) {
            errorHandler.handleError(new RuntimeException("Error " + i));
        }
        
        // Verify errors exist
        ErrorStatistics statsBefore = errorHandler.getStatistics();
        assertEquals(5, statsBefore.totalErrors());
        
        // Clear history
        errorHandler.clearHistory();
        
        // Verify history is cleared
        ErrorStatistics statsAfter = errorHandler.getStatistics();
        assertEquals(0, statsAfter.totalErrors());
        assertTrue(statsAfter.recentErrors().isEmpty());
    }
    
    // Test exception class
    private static class TestException extends Exception {
        TestException() {
            super("Test exception");
        }
    }
}