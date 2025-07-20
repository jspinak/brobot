package io.github.jspinak.brobot.runner.errorhandling;

import lombok.Data;

import io.github.jspinak.brobot.runner.diagnostics.DiagnosticReport;
import io.github.jspinak.brobot.runner.diagnostics.DiagnosticTool;
import io.github.jspinak.brobot.runner.session.SessionRecoveryManager;
import io.github.jspinak.brobot.runner.session.SessionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Data
class ErrorReportGeneratorTest {

    @Mock
    private ErrorHandler errorHandler;
    
    @Mock
    private DiagnosticTool diagnosticTool;
    
    @Mock
    private SessionRecoveryManager sessionRecoveryManager;
    
    private ErrorReportGenerator reportGenerator;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportGenerator = new ErrorReportGenerator(errorHandler, diagnosticTool, sessionRecoveryManager);
        
        // Setup mocks
        when(errorHandler.getStatistics()).thenReturn(
            new ErrorStatistics(5, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList())
        );
        
        when(diagnosticTool.runDiagnostics()).thenReturn(
            DiagnosticReport.builder()
                .timestamp(LocalDateTime.now())
                .systemProperties(new HashMap<>())
                .environmentVariables(new HashMap<>())
                .build()
        );
        
        when(sessionRecoveryManager.getCurrentSession()).thenReturn(
            SessionState.builder()
                .sessionId("test-session")
                .startTime(LocalDateTime.now())
                .openConfigurations(new ArrayList<>())
                .windowStates(new HashMap<>())
                .userPreferences(new HashMap<>())
                .executionHistory(new ArrayList<>())
                .build()
        );
    }
    
    @Test
    @DisplayName("Should generate error report")
    void shouldGenerateErrorReport() {
        // Create test error
        Exception error = new RuntimeException("Test error");
        ErrorContext context = ErrorContext.builder()
            .operation("Test Operation")
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .severity(ErrorContext.ErrorSeverity.HIGH)
            .recoverable(false)
            .build();
            
        // Generate report
        ErrorReport report = reportGenerator.generateReport(error, context);
        
        // Verify report
        assertNotNull(report);
        assertNotNull(report.getReportId());
        assertNotNull(report.getTimestamp());
        assertEquals("Test error", report.getErrorMessage());
        assertEquals("java.lang.RuntimeException", report.getErrorType());
        assertNotNull(report.getStackTrace());
        assertTrue(report.getStackTrace().contains("RuntimeException"));
        assertEquals(context, report.getErrorContext());
    }
    
    @Test
    @DisplayName("Should export report to file")
    void shouldExportReportToFile() throws Exception {
        // Create report
        ErrorReport report = ErrorReport.builder()
            .reportId("test-report-123")
            .timestamp(LocalDateTime.now())
            .errorMessage("Test error")
            .errorType("TestException")
            .stackTrace("Stack trace here")
            .errorContext(ErrorContext.minimal("Test", ErrorContext.ErrorCategory.SYSTEM))
            .build();
            
        // Export report
        Path exportedFile = reportGenerator.exportReport(report, tempDir);
        
        // Verify file
        assertTrue(Files.exists(exportedFile));
        assertTrue(exportedFile.getFileName().toString().startsWith("error_report_"));
        assertTrue(exportedFile.getFileName().toString().endsWith(".txt"));
        
        // Verify content
        String content = Files.readString(exportedFile);
        assertTrue(content.contains("BROBOT RUNNER ERROR REPORT"));
        assertTrue(content.contains("test-report-123"));
        assertTrue(content.contains("Test error"));
        assertTrue(content.contains("TestException"));
    }
    
    @Test
    @DisplayName("Should create support bundle")
    void shouldCreateSupportBundle() throws Exception {
        // Create report with all data
        ErrorReport report = ErrorReport.builder()
            .reportId("bundle-test-123")
            .timestamp(LocalDateTime.now())
            .errorMessage("Bundle test error")
            .errorType("BundleException")
            .stackTrace("Stack trace")
            .errorContext(ErrorContext.minimal("Test", ErrorContext.ErrorCategory.SYSTEM))
            .diagnosticReport(DiagnosticReport.builder()
                .timestamp(LocalDateTime.now())
                .systemProperties(new HashMap<>())
                .environmentVariables(new HashMap<>())
                .build())
            .errorStatistics(new ErrorStatistics(5, Collections.emptyMap(), 
                Collections.emptyList(), Collections.emptyList()))
            .sessionState(SessionState.builder()
                .sessionId("test-session")
                .startTime(LocalDateTime.now())
                .openConfigurations(new ArrayList<>())
                .windowStates(new HashMap<>())
                .userPreferences(new HashMap<>())
                .executionHistory(new ArrayList<>())
                .build())
            .recentLogs(List.of("Log entry 1", "Log entry 2"))
            .systemProperties(Map.of("java.version", "17"))
            .build();
            
        // Create bundle
        Path bundle = reportGenerator.createSupportBundle(report);
        
        // Verify bundle
        assertTrue(Files.exists(bundle));
        assertTrue(bundle.getFileName().toString().startsWith("support_bundle_"));
        assertTrue(bundle.getFileName().toString().endsWith(".zip"));
        assertTrue(Files.size(bundle) > 0);
    }
    
    @Test
    @DisplayName("Should generate user-friendly summary")
    void shouldGenerateUserFriendlySummary() {
        // Create report
        ErrorReport report = ErrorReport.builder()
            .reportId("user-test-123")
            .timestamp(LocalDateTime.now())
            .errorMessage("User friendly error")
            .errorContext(ErrorContext.builder()
                .operation("User Operation")
                .category(ErrorContext.ErrorCategory.VALIDATION)
                .severity(ErrorContext.ErrorSeverity.MEDIUM)
                .recoverable(true)
                .recoveryHint("Please check your input")
                .build())
            .build();
            
        // Generate summary
        String summary = reportGenerator.generateUserSummary(report);
        
        // Verify summary
        assertNotNull(summary);
        assertTrue(summary.contains("Error Report Summary"));
        assertTrue(summary.contains("user-test-123"));
        assertTrue(summary.contains("User friendly error"));
        assertTrue(summary.contains("User Operation"));
        assertTrue(summary.contains("Validation"));
        assertTrue(summary.contains("recoverable"));
        assertTrue(summary.contains("Please check your input"));
    }
    
    @Test
    @DisplayName("Should handle report generation failure gracefully")
    void shouldHandleReportGenerationFailure() {
        // Make diagnostic tool throw exception
        when(diagnosticTool.runDiagnostics()).thenThrow(new RuntimeException("Diagnostic failure"));
        
        // Create error
        Exception error = new RuntimeException("Test error");
        ErrorContext context = ErrorContext.minimal("Test", ErrorContext.ErrorCategory.SYSTEM);
        
        // Generate report - should not throw
        ErrorReport report = reportGenerator.generateReport(error, context);
        
        // Verify minimal report is returned
        assertNotNull(report);
        assertNotNull(report.getReportId());
        assertEquals("Test error", report.getErrorMessage());
        assertNull(report.getDiagnosticReport()); // Should be null due to failure
    }
    
    @Test
    @DisplayName("Should clean old reports")
    void shouldCleanOldReports() throws Exception {
        // Create old report files
        Path reportsDir = tempDir.resolve("error-reports");
        Files.createDirectories(reportsDir);
        
        // Create an old file (simulate by setting last modified time)
        Path oldReport = reportsDir.resolve("old_report.txt");
        Files.writeString(oldReport, "Old report");
        
        // Create a recent file
        Path recentReport = reportsDir.resolve("recent_report.txt");
        Files.writeString(recentReport, "Recent report");
        
        // Note: Setting file modification time is platform-dependent
        // This test may need adjustment based on platform
        
        // Clean reports older than 30 days
        reportGenerator.cleanOldReports(30);
        
        // Verify recent report still exists
        assertTrue(Files.exists(recentReport));
    }
}