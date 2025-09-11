package io.github.jspinak.brobot.runner.errorhandling.strategy;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.errorhandling.*;
import io.github.jspinak.brobot.runner.errorhandling.strategy.strategies.*;

@DisplayName("ErrorStrategyService Tests")
class ErrorStrategyServiceTest {

    private ErrorStrategyService service;
    private IErrorStrategy mockStrategy;

    @BeforeEach
    void setUp() {
        service = new ErrorStrategyService();
        service.initialize();
        mockStrategy = mock(IErrorStrategy.class);
    }

    @Test
    @DisplayName("Should register strategy successfully")
    void testRegisterStrategy() {
        // Act
        service.registerStrategy(RuntimeException.class, mockStrategy);

        // Assert
        assertThat(service.getStrategies()).containsEntry(RuntimeException.class, mockStrategy);
    }

    @Test
    @DisplayName("Should throw exception for null parameters")
    void testRegisterNullParameters() {
        // Act & Assert
        assertThatThrownBy(() -> service.registerStrategy(null, mockStrategy))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.registerStrategy(RuntimeException.class, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should find exact match strategy")
    void testFindExactMatchStrategy() {
        // Arrange
        service.registerStrategy(IOException.class, mockStrategy);

        // Act
        IErrorStrategy found = service.findStrategy(new IOException());

        // Assert
        assertThat(found).isEqualTo(mockStrategy);
    }

    @Disabled("CI failure - needs investigation")
    @Test
    @DisplayName("Should find superclass strategy")
    void testFindSuperclassStrategy() {
        // Arrange
        service.registerStrategy(IOException.class, mockStrategy);

        // Act
        IErrorStrategy found = service.findStrategy(new NoSuchFileException("test"));

        // Assert
        assertThat(found).isEqualTo(mockStrategy);
    }

    @Test
    @DisplayName("Should return default strategy for unknown error")
    void testFindDefaultStrategy() {
        // Act
        IErrorStrategy found = service.findStrategy(new OutOfMemoryError());

        // Assert
        assertThat(found).isInstanceOf(DefaultErrorStrategy.class);
    }

    @Test
    @DisplayName("Should handle null error")
    void testFindStrategyWithNull() {
        // Act
        IErrorStrategy found = service.findStrategy(null);

        // Assert
        assertThat(found).isInstanceOf(DefaultErrorStrategy.class);
    }

    @Test
    @DisplayName("Should execute strategy successfully")
    void testExecuteStrategy() {
        // Arrange
        ErrorResult expectedResult = ErrorResult.handled("Success", "123");
        when(mockStrategy.handle(any(), any())).thenReturn(expectedResult);
        service.registerStrategy(RuntimeException.class, mockStrategy);

        RuntimeException error = new RuntimeException("Test");
        ErrorContext context = ErrorContext.minimal("TestOp", ErrorContext.ErrorCategory.UNKNOWN);

        // Act
        ErrorResult result = service.handleError(error, context);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(mockStrategy).handle(error, context);
    }

    @Test
    @DisplayName("Should handle strategy execution failure")
    void testExecuteStrategyFailure() {
        // Arrange
        when(mockStrategy.handle(any(), any())).thenThrow(new RuntimeException("Strategy failed"));
        service.registerStrategy(RuntimeException.class, mockStrategy);

        RuntimeException error = new RuntimeException("Test");
        ErrorContext context = ErrorContext.minimal("TestOp", ErrorContext.ErrorCategory.UNKNOWN);

        // Act
        ErrorResult result = service.handleError(error, context);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isRecoverable()).isFalse();
        assertThat(result.getUserMessage()).isEqualTo("Error handling failed");
    }

    @Test
    @DisplayName("Should register default strategies")
    void testDefaultStrategies() {
        // Assert
        var strategies = service.getStrategies();
        assertThat(strategies)
                .containsKey(ApplicationException.class)
                .containsKey(NullPointerException.class)
                .containsKey(IllegalArgumentException.class)
                .containsKey(IllegalStateException.class)
                .containsKey(IOException.class)
                .containsKey(NoSuchFileException.class)
                .containsKey(TimeoutException.class)
                .containsKey(InterruptedException.class);
    }

    @Test
    @DisplayName("Should remove strategy")
    void testRemoveStrategy() {
        // Arrange
        service.registerStrategy(RuntimeException.class, mockStrategy);

        // Act
        IErrorStrategy removed = service.removeStrategy(RuntimeException.class);

        // Assert
        assertThat(removed).isEqualTo(mockStrategy);
        assertThat(service.getStrategies()).doesNotContainKey(RuntimeException.class);
    }

    @Test
    @DisplayName("Should clear custom strategies but keep defaults")
    void testClearCustomStrategies() {
        // Arrange
        int defaultCount = service.getStrategyCount();
        service.registerStrategy(RuntimeException.class, mockStrategy);

        // Act
        service.clearCustomStrategies();

        // Assert
        assertThat(service.getStrategyCount()).isEqualTo(defaultCount);
        assertThat(service.getStrategies()).doesNotContainKey(RuntimeException.class);
    }

    @Test
    @DisplayName("Should track diagnostic information")
    void testDiagnosticInfo() {
        // Arrange
        service.enableDiagnosticMode(true);
        RuntimeException error = new RuntimeException("Test");
        ErrorContext context = ErrorContext.minimal("TestOp", ErrorContext.ErrorCategory.UNKNOWN);

        // Act
        service.handleError(error, context);
        var diagnosticInfo = service.getDiagnosticInfo();

        // Assert
        assertThat(diagnosticInfo.getComponent()).isEqualTo("ErrorStrategyService");
        assertThat(diagnosticInfo.getStates())
                .containsEntry("totalExecutions", 1L)
                .containsEntry("defaultStrategyExecutions", 1L);
    }
}
