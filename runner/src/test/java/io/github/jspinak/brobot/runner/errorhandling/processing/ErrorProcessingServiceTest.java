package io.github.jspinak.brobot.runner.errorhandling.processing;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.IErrorProcessor;

@DisplayName("ErrorProcessingService Tests")
class ErrorProcessingServiceTest {

    private ErrorProcessingService service;
    private IErrorProcessor mockProcessor1;
    private IErrorProcessor mockProcessor2;

    @BeforeEach
    void setUp() {
        service = new ErrorProcessingService();
        mockProcessor1 = mock(IErrorProcessor.class);
        mockProcessor2 = mock(IErrorProcessor.class);
    }

    @Test
    @DisplayName("Should register processor successfully")
    void testRegisterProcessor() {
        // Act
        service.registerProcessor(mockProcessor1);

        // Assert
        assertThat(service.getProcessors()).containsExactly(mockProcessor1);
        assertThat(service.getProcessorCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw exception for null processor")
    void testRegisterNullProcessor() {
        // Act & Assert
        assertThatThrownBy(() -> service.registerProcessor(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Processor cannot be null");
    }

    @Test
    @DisplayName("Should process error through all processors")
    void testProcessError() {
        // Arrange
        service.registerProcessor(mockProcessor1);
        service.registerProcessor(mockProcessor2);

        Exception error = new RuntimeException("Test error");
        ErrorContext context =
                ErrorContext.minimal("TestOperation", ErrorContext.ErrorCategory.UNKNOWN);

        // Act
        service.processError(error, context);

        // Assert
        verify(mockProcessor1).process(error, context);
        verify(mockProcessor2).process(error, context);
    }

    @Test
    @DisplayName("Should continue processing even if one processor fails")
    void testProcessErrorWithFailure() {
        // Arrange
        service.registerProcessor(mockProcessor1);
        service.registerProcessor(mockProcessor2);

        Exception error = new RuntimeException("Test error");
        ErrorContext context =
                ErrorContext.minimal("TestOperation", ErrorContext.ErrorCategory.UNKNOWN);

        // Make first processor throw exception
        doThrow(new RuntimeException("Processor failed"))
                .when(mockProcessor1)
                .process(any(), any());

        // Act
        service.processError(error, context);

        // Assert
        verify(mockProcessor1).process(error, context);
        verify(mockProcessor2).process(error, context); // Should still be called
    }

    @Test
    @DisplayName("Should handle null error gracefully")
    void testProcessNullError() {
        // Arrange
        service.registerProcessor(mockProcessor1);
        ErrorContext context =
                ErrorContext.minimal("TestOperation", ErrorContext.ErrorCategory.UNKNOWN);

        // Act
        service.processError(null, context);

        // Assert
        verify(mockProcessor1, never()).process(any(), any());
    }

    @Test
    @DisplayName("Should handle null context gracefully")
    void testProcessNullContext() {
        // Arrange
        service.registerProcessor(mockProcessor1);
        Exception error = new RuntimeException("Test error");

        // Act
        service.processError(error, null);

        // Assert
        verify(mockProcessor1, never()).process(any(), any());
    }

    @Test
    @DisplayName("Should remove processor successfully")
    void testRemoveProcessor() {
        // Arrange
        service.registerProcessor(mockProcessor1);
        service.registerProcessor(mockProcessor2);

        // Act
        boolean removed = service.removeProcessor(mockProcessor1);

        // Assert
        assertThat(removed).isTrue();
        assertThat(service.getProcessors()).containsExactly(mockProcessor2);
    }

    @Test
    @DisplayName("Should return false when removing non-existent processor")
    void testRemoveNonExistentProcessor() {
        // Act
        boolean removed = service.removeProcessor(mockProcessor1);

        // Assert
        assertThat(removed).isFalse();
    }

    @Test
    @DisplayName("Should clear all processors")
    void testClearProcessors() {
        // Arrange
        service.registerProcessor(mockProcessor1);
        service.registerProcessor(mockProcessor2);

        // Act
        service.clearProcessors();

        // Assert
        assertThat(service.getProcessors()).isEmpty();
        assertThat(service.getProcessorCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return unmodifiable list of processors")
    void testGetProcessorsUnmodifiable() {
        // Arrange
        service.registerProcessor(mockProcessor1);
        List<IErrorProcessor> processors = service.getProcessors();

        // Act & Assert
        assertThatThrownBy(() -> processors.add(mockProcessor2))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should track diagnostic information")
    void testDiagnosticInfo() {
        // Arrange
        service.enableDiagnosticMode(true);
        service.registerProcessor(mockProcessor1);

        Exception error = new RuntimeException("Test error");
        ErrorContext context =
                ErrorContext.minimal("TestOperation", ErrorContext.ErrorCategory.UNKNOWN);

        // Act
        service.processError(error, context);
        var diagnosticInfo = service.getDiagnosticInfo();

        // Assert
        assertThat(diagnosticInfo.getComponent()).isEqualTo("ErrorProcessingService");
        assertThat(diagnosticInfo.getStates())
                .containsEntry("registeredProcessors", 1)
                .containsEntry("totalProcessed", 1L)
                .containsEntry("processingFailures", 0L);
    }
}
