package io.github.jspinak.brobot.runner.errorhandling;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.diagnostics.DiagnosticReport;
import io.github.jspinak.brobot.runner.diagnostics.DiagnosticTool;
import io.github.jspinak.brobot.runner.session.SessionRecoveryManager;
import io.github.jspinak.brobot.runner.session.SessionState;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Generates comprehensive error reports for troubleshooting and support. */
@Slf4j
@Component
@RequiredArgsConstructor
@Data
public class ErrorReportGenerator {

    private final ErrorHandler errorHandler;
    private final DiagnosticTool diagnosticTool;
    private final SessionRecoveryManager sessionRecoveryManager;

    private static final String REPORTS_DIR = "error-reports";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Generate a comprehensive error report. */
    public ErrorReport generateReport(Throwable error, ErrorContext context) {
        log.info("Generating error report for: {}", context.getErrorId());

        try {
            // Collect all data
            DiagnosticReport diagnostics = diagnosticTool.runDiagnostics();
            ErrorStatistics errorStats = errorHandler.getStatistics();
            SessionState sessionState = sessionRecoveryManager.getCurrentSession();
            List<String> recentLogs = collectRecentLogs();

            // Build report
            ErrorReport report =
                    ErrorReport.builder()
                            .reportId(UUID.randomUUID().toString())
                            .timestamp(LocalDateTime.now())
                            .errorContext(context)
                            .errorMessage(error.getMessage())
                            .errorType(error.getClass().getName())
                            .stackTrace(formatStackTrace(error))
                            .diagnosticReport(diagnostics)
                            .errorStatistics(errorStats)
                            .sessionState(sessionState)
                            .recentLogs(recentLogs)
                            .systemProperties(collectRelevantSystemProperties())
                            .build();

            log.info("Error report generated: {}", report.getReportId());
            return report;

        } catch (Exception e) {
            log.error("Failed to generate error report", e);
            // Return minimal report
            return ErrorReport.builder()
                    .reportId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .errorContext(context)
                    .errorMessage(error.getMessage())
                    .errorType(error.getClass().getName())
                    .stackTrace(formatStackTrace(error))
                    .build();
        }
    }

    /** Export error report to file. */
    public Path exportReport(ErrorReport report, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);

        String timestamp = report.getTimestamp().format(TIMESTAMP_FORMAT);
        String filename =
                String.format(
                        "error_report_%s_%s.txt", timestamp, report.getReportId().substring(0, 8));

        Path reportFile = outputDir.resolve(filename);

        try (BufferedWriter writer = Files.newBufferedWriter(reportFile)) {
            writer.write(formatReport(report));
        }

        log.info("Error report exported to: {}", reportFile);
        return reportFile;
    }

    /** Create a support bundle with all relevant information. */
    public Path createSupportBundle(ErrorReport report) throws IOException {
        Path reportsDir = Paths.get(REPORTS_DIR);
        Files.createDirectories(reportsDir);

        String timestamp = report.getTimestamp().format(TIMESTAMP_FORMAT);
        String bundleName =
                String.format(
                        "support_bundle_%s_%s.zip",
                        timestamp, report.getReportId().substring(0, 8));

        Path bundlePath = reportsDir.resolve(bundleName);

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(bundlePath))) {
            // Add error report
            addToZip(zos, "error_report.txt", formatReport(report));

            // Add diagnostic report
            if (report.getDiagnosticReport() != null) {
                addToZip(
                        zos,
                        "diagnostics.txt",
                        formatDiagnosticReport(report.getDiagnosticReport()));
            }

            // Add recent logs
            if (report.getRecentLogs() != null && !report.getRecentLogs().isEmpty()) {
                addToZip(zos, "recent_logs.txt", String.join("\n", report.getRecentLogs()));
            }

            // Add session state
            if (report.getSessionState() != null) {
                addToZip(zos, "session_state.json", formatSessionState(report.getSessionState()));
            }

            // Add system info
            addToZip(zos, "system_info.txt", formatSystemInfo(report));

            // Add error history
            if (report.getErrorStatistics() != null) {
                addToZip(zos, "error_history.txt", report.getErrorStatistics().getSummary());
            }
        }

        log.info("Support bundle created: {}", bundlePath);
        return bundlePath;
    }

    /** Generate a user-friendly error report summary. */
    public String generateUserSummary(ErrorReport report) {
        StringBuilder summary = new StringBuilder();

        summary.append("Error Report Summary\n");
        summary.append("===================\n\n");

        summary.append("Report ID: ").append(report.getReportId()).append("\n");
        summary.append("Time: ")
                .append(
                        report.getTimestamp()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n");
        summary.append("Error: ").append(report.getErrorMessage()).append("\n");

        if (report.getErrorContext() != null) {
            summary.append("Operation: ")
                    .append(report.getErrorContext().getOperation())
                    .append("\n");
            summary.append("Category: ")
                    .append(report.getErrorContext().getCategory().getDisplayName())
                    .append("\n");

            if (report.getErrorContext().isRecoverable()) {
                summary.append("\nThis error may be recoverable.\n");
                if (report.getErrorContext().getRecoveryHint() != null) {
                    summary.append("Suggestion: ")
                            .append(report.getErrorContext().getRecoveryHint())
                            .append("\n");
                }
            }
        }

        summary.append("\nFor technical support, please provide the Report ID.\n");
        summary.append("A support bundle has been created with detailed information.\n");

        return summary.toString();
    }

    private List<String> collectRecentLogs() {
        // This would integrate with the logging system
        // For now, return placeholder
        List<String> logs = new ArrayList<>();
        logs.add("[INFO] Application started");
        logs.add("[DEBUG] Configuration loaded");
        // In real implementation, would read from log files or log buffer
        return logs;
    }

    private Map<String, String> collectRelevantSystemProperties() {
        Map<String, String> relevant = new HashMap<>();

        String[] keys = {
            "java.version",
            "java.vendor",
            "java.home",
            "os.name",
            "os.version",
            "os.arch",
            "user.home",
            "user.dir",
            "file.separator",
            "path.separator"
        };

        for (String key : keys) {
            String value = System.getProperty(key);
            if (value != null) {
                relevant.put(key, value);
            }
        }

        return relevant;
    }

    private String formatStackTrace(Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return sw.toString();
    }

    private String formatReport(ErrorReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== BROBOT RUNNER ERROR REPORT ===\n");
        sb.append("Report ID: ").append(report.getReportId()).append("\n");
        sb.append("Generated: ")
                .append(
                        report.getTimestamp()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\n\n");

        sb.append("ERROR INFORMATION\n");
        sb.append("=================\n");
        sb.append("Type: ").append(report.getErrorType()).append("\n");
        sb.append("Message: ").append(report.getErrorMessage()).append("\n");

        if (report.getErrorContext() != null) {
            ErrorContext ctx = report.getErrorContext();
            sb.append("Error ID: ").append(ctx.getErrorId()).append("\n");
            sb.append("Operation: ").append(ctx.getOperation()).append("\n");
            sb.append("Component: ").append(ctx.getComponent()).append("\n");
            sb.append("Category: ").append(ctx.getCategory().getDisplayName()).append("\n");
            sb.append("Severity: ").append(ctx.getSeverity().getDisplayName()).append("\n");
            sb.append("Recoverable: ").append(ctx.isRecoverable()).append("\n");

            if (ctx.getRecoveryHint() != null) {
                sb.append("Recovery Hint: ").append(ctx.getRecoveryHint()).append("\n");
            }
        }

        sb.append("\nSTACK TRACE\n");
        sb.append("===========\n");
        sb.append(report.getStackTrace()).append("\n");

        if (report.getSessionState() != null) {
            sb.append("\nSESSION INFORMATION\n");
            sb.append("===================\n");
            SessionState session = report.getSessionState();
            sb.append("Session ID: ").append(session.getSessionId()).append("\n");
            sb.append("Start Time: ").append(session.getStartTime()).append("\n");
            sb.append("Open Configurations: ")
                    .append(session.getOpenConfigurations().size())
                    .append("\n");

            if (!session.getExecutionHistory().isEmpty()) {
                sb.append("Recent Executions: ")
                        .append(session.getExecutionHistory().size())
                        .append("\n");
            }
        }

        if (report.getErrorStatistics() != null) {
            sb.append("\nERROR STATISTICS\n");
            sb.append("================\n");
            sb.append(report.getErrorStatistics().getSummary());
        }

        if (report.getDiagnosticReport() != null) {
            sb.append("\nSYSTEM DIAGNOSTICS\n");
            sb.append("==================\n");
            sb.append("See diagnostics.txt for detailed system information\n");
        }

        if (report.getRecentLogs() != null && !report.getRecentLogs().isEmpty()) {
            sb.append("\nRECENT LOGS\n");
            sb.append("===========\n");
            int logCount = Math.min(50, report.getRecentLogs().size());
            for (int i = 0; i < logCount; i++) {
                sb.append(report.getRecentLogs().get(i)).append("\n");
            }
            if (report.getRecentLogs().size() > 50) {
                sb.append("... ")
                        .append(report.getRecentLogs().size() - 50)
                        .append(" more log entries ...\n");
            }
        }

        return sb.toString();
    }

    private String formatDiagnosticReport(DiagnosticReport diagnostics) {
        // Delegate to DiagnosticTool's formatting
        // This is a simplified version
        return diagnostics.toString();
    }

    private String formatSessionState(SessionState session) {
        // Convert to JSON or readable format
        // This is a simplified version
        return session.toString();
    }

    private String formatSystemInfo(ErrorReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append("SYSTEM PROPERTIES\n");
        sb.append("=================\n");

        if (report.getSystemProperties() != null) {
            report.getSystemProperties()
                    .forEach(
                            (key, value) -> sb.append(key).append(": ").append(value).append("\n"));
        }

        return sb.toString();
    }

    private void addToZip(ZipOutputStream zos, String filename, String content) throws IOException {
        ZipEntry entry = new ZipEntry(filename);
        zos.putNextEntry(entry);
        zos.write(content.getBytes());
        zos.closeEntry();
    }

    /** Clean old error reports. */
    public void cleanOldReports(int daysToKeep) {
        try {
            Path reportsDir = Paths.get(REPORTS_DIR);
            if (!Files.exists(reportsDir)) {
                return;
            }

            LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);

            Files.list(reportsDir)
                    .filter(Files::isRegularFile)
                    .filter(
                            path -> {
                                try {
                                    return Files.getLastModifiedTime(path)
                                            .toInstant()
                                            .isBefore(
                                                    cutoff.atZone(java.time.ZoneId.systemDefault())
                                                            .toInstant());
                                } catch (IOException e) {
                                    return false;
                                }
                            })
                    .forEach(
                            path -> {
                                try {
                                    Files.delete(path);
                                    log.debug("Deleted old error report: {}", path);
                                } catch (IOException e) {
                                    log.error("Failed to delete old report: {}", path, e);
                                }
                            });

        } catch (Exception e) {
            log.error("Failed to clean old error reports", e);
        }
    }
}
