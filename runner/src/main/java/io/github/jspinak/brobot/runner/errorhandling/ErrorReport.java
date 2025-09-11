package io.github.jspinak.brobot.runner.errorhandling;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.github.jspinak.brobot.runner.diagnostics.DiagnosticReport;
import io.github.jspinak.brobot.runner.session.SessionState;

import lombok.Builder;
import lombok.Data;

/** Comprehensive error report containing all relevant information. */
@Data
@Builder
public class ErrorReport {
    private String reportId;
    private LocalDateTime timestamp;
    private ErrorContext errorContext;
    private String errorMessage;
    private String errorType;
    private String stackTrace;
    private DiagnosticReport diagnosticReport;
    private ErrorStatistics errorStatistics;
    private SessionState sessionState;
    private List<String> recentLogs;
    private Map<String, String> systemProperties;
}
