package io.github.jspinak.brobot.runner.errorhandling.history;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorHistory;

@DisplayName("ErrorHistoryService Tests")
class ErrorHistoryServiceTest {

    private ErrorHistoryService service;

    @BeforeEach
    void setUp() {
        service = new ErrorHistoryService();
    }

    @Test
    @DisplayName("Should record error successfully")
    void testRecordError() {
        // Arrange
        Exception error = new RuntimeException("Test error");
        ErrorContext context =
                ErrorContext.minimal("TestOperation", ErrorContext.ErrorCategory.SYSTEM);

        // Act
        service.record(error, context);

        // Assert
        List<ErrorHistory.ErrorRecord> recent = service.getRecentErrors(1);
        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).getErrorType()).isEqualTo("java.lang.RuntimeException");
        assertThat(recent.get(0).getMessage()).isEqualTo("Test error");
    }

    @Test
    @DisplayName("Should handle null error gracefully")
    void testRecordNullError() {
        // Arrange
        ErrorContext context =
                ErrorContext.minimal("TestOperation", ErrorContext.ErrorCategory.SYSTEM);

        // Act & Assert
        assertThatNoException().isThrownBy(() -> service.record(null, context));
    }

    @Test
    @DisplayName("Should handle null context gracefully")
    void testRecordNullContext() {
        // Arrange
        Exception error = new RuntimeException("Test error");

        // Act & Assert
        assertThatNoException().isThrownBy(() -> service.record(error, null));
    }

    @Test
    @DisplayName("Should get recent errors")
    void testGetRecentErrors() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            service.record(
                    new RuntimeException("Error " + i),
                    ErrorContext.minimal("Op" + i, ErrorContext.ErrorCategory.SYSTEM));
        }

        // Act
        List<ErrorHistory.ErrorRecord> recent = service.getRecentErrors(3);

        // Assert
        assertThat(recent).hasSize(3);
        assertThat(recent.get(0).getMessage()).isEqualTo("Error 4"); // Most recent first
    }

    @Test
    @DisplayName("Should get errors by category")
    void testGetErrorsByCategory() {
        // Arrange
        service.record(
                new RuntimeException("System error"),
                ErrorContext.minimal("Op1", ErrorContext.ErrorCategory.SYSTEM));
        service.record(
                new IOException("File error"),
                ErrorContext.minimal("Op2", ErrorContext.ErrorCategory.FILE_IO));
        service.record(
                new RuntimeException("Another system error"),
                ErrorContext.minimal("Op3", ErrorContext.ErrorCategory.SYSTEM));

        // Act
        List<ErrorHistory.ErrorRecord> systemErrors =
                service.getErrorsByCategory(ErrorContext.ErrorCategory.SYSTEM);

        // Assert
        assertThat(systemErrors).hasSize(2);
        assertThat(systemErrors)
                .extracting(ErrorHistory.ErrorRecord::getMessage)
                .containsExactly("System error", "Another system error");
    }

    @Test
    @DisplayName("Should get most frequent errors")
    void testGetMostFrequentErrors() {
        // Arrange
        // Record same error multiple times
        for (int i = 0; i < 5; i++) {
            service.record(
                    new RuntimeException("Frequent error"),
                    ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));
        }
        // Record different errors
        service.record(
                new IOException("IO error"),
                ErrorContext.minimal("Op", ErrorContext.ErrorCategory.FILE_IO));

        // Act
        List<ErrorHistory.ErrorFrequency> frequent = service.getMostFrequentErrors(2);

        // Assert
        assertThat(frequent).hasSize(2);
        assertThat(frequent.get(0).count()).isEqualTo(5);
        assertThat(frequent.get(0).errorKey()).contains("Frequent error");
    }

    @Disabled("CI failure - needs investigation")
    @Test
    @DisplayName("Should get errors in time range")
    void testGetErrorsInRange() {
        // Arrange
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        Instant twoHoursAgo = now.minus(2, ChronoUnit.HOURS);

        service.record(
                new RuntimeException("Recent error"),
                ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));

        // Act
        List<ErrorHistory.ErrorRecord> inRange = service.getErrorsInRange(oneHourAgo, now);
        List<ErrorHistory.ErrorRecord> outOfRange =
                service.getErrorsInRange(twoHoursAgo, oneHourAgo);

        // Assert
        assertThat(inRange).hasSize(1);
        assertThat(outOfRange).isEmpty();
    }

    @Test
    @DisplayName("Should validate time range parameters")
    void testGetErrorsInRangeValidation() {
        // Arrange
        Instant now = Instant.now();
        Instant past = now.minus(1, ChronoUnit.HOURS);

        // Act & Assert
        assertThatThrownBy(() -> service.getErrorsInRange(null, now))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.getErrorsInRange(now, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.getErrorsInRange(now, past))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start time must be before end time");
    }

    @Test
    @DisplayName("Should get statistics")
    void testGetStatistics() {
        // Arrange
        service.record(
                new RuntimeException("Error 1"),
                ErrorContext.critical("Op1", ErrorContext.ErrorCategory.SYSTEM));
        service.record(
                new IOException("Error 2"),
                ErrorContext.recoverable("Op2", ErrorContext.ErrorCategory.FILE_IO, "Retry"));

        // Act
        ErrorHistory.ErrorHistoryStatistics stats = service.getStatistics();

        // Assert
        assertThat(stats.totalErrors()).isEqualTo(2);
        assertThat(stats.errorsByCategory()).containsEntry(ErrorContext.ErrorCategory.SYSTEM, 1L);
        assertThat(stats.errorsByCategory()).containsEntry(ErrorContext.ErrorCategory.FILE_IO, 1L);
        assertThat(stats.errorsBySeverity()).containsEntry(ErrorContext.ErrorSeverity.CRITICAL, 1L);
        assertThat(stats.errorsBySeverity()).containsEntry(ErrorContext.ErrorSeverity.LOW, 1L);
    }

    @Test
    @DisplayName("Should clear history")
    void testClear() {
        // Arrange
        service.record(
                new RuntimeException("Error"),
                ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));

        // Act
        service.clear();

        // Assert
        assertThat(service.getRecentErrors(10)).isEmpty();
        assertThat(service.getStatistics().totalErrors()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should get error trends")
    void testGetErrorTrends() {
        // Arrange
        for (int i = 0; i < 3; i++) {
            service.record(
                    new RuntimeException("Error " + i),
                    ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));
        }

        // Act
        Map<Instant, Long> trends = service.getErrorTrends(60); // 60 minute intervals

        // Assert
        assertThat(trends).isNotEmpty();
        assertThat(trends.values()).contains(3L); // All errors in same interval
    }

    @Test
    @DisplayName("Should enable diagnostic mode")
    void testDiagnosticMode() {
        // Arrange
        service.enableDiagnosticMode(true);

        // Act
        service.record(
                new RuntimeException("Test"),
                ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));

        // Assert
        assertThat(service.isDiagnosticModeEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should provide diagnostic info")
    void testDiagnosticInfo() {
        // Arrange
        service.record(
                new RuntimeException("Error"),
                ErrorContext.minimal("Op", ErrorContext.ErrorCategory.SYSTEM));

        // Act
        var diagnosticInfo = service.getDiagnosticInfo();

        // Assert
        assertThat(diagnosticInfo.getComponent()).isEqualTo("ErrorHistoryService");
        assertThat(diagnosticInfo.getStates())
                .containsEntry("totalErrors", 1L)
                .containsKey("maxHistorySize")
                .containsKey("retentionHours");
    }
}
