package io.github.jspinak.brobot.runner.ui.log.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;

/** Unit tests for LogExportService. */
class LogExportServiceTest {

    private LogExportService exportService;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        exportService = new LogExportService();
    }

    @Test
    @DisplayName("Should export logs as text format")
    void testExportAsText() throws IOException {
        // Given
        List<LogEntry> entries = createTestEntries();
        Path outputPath = tempDir.resolve("logs.txt");
        LogExportService.ExportOptions options =
                LogExportService.ExportOptions.builder()
                        .includeHeaders(true)
                        .includeTimestamps(true)
                        .build();

        // When
        exportService.exportLogs(entries, outputPath, LogExportService.ExportFormat.TEXT, options);

        // Then
        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath);
        assertTrue(content.contains("=== Log Export ==="));
        assertTrue(content.contains("Test message 1"));
        assertTrue(content.contains("ERROR"));
    }

    @Test
    @DisplayName("Should export logs as CSV format")
    void testExportAsCSV() throws IOException {
        // Given
        List<LogEntry> entries = createTestEntries();
        Path outputPath = tempDir.resolve("logs.csv");
        LogExportService.ExportOptions options =
                LogExportService.ExportOptions.builder().includeHeaders(true).build();

        // When
        exportService.exportLogs(entries, outputPath, LogExportService.ExportFormat.CSV, options);

        // Then
        assertTrue(Files.exists(outputPath));
        List<String> lines = Files.readAllLines(outputPath);
        assertEquals("Timestamp,Level,Type,Source,Message", lines.get(0));
        assertTrue(lines.size() >= 3); // Header + 2 entries
    }

    @Test
    @DisplayName("Should export logs as JSON format")
    void testExportAsJSON() throws IOException {
        // Given
        List<LogEntry> entries = createTestEntries();
        Path outputPath = tempDir.resolve("logs.json");
        LogExportService.ExportOptions options = LogExportService.ExportOptions.builder().build();

        // When
        exportService.exportLogs(entries, outputPath, LogExportService.ExportFormat.JSON, options);

        // Then
        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath);
        assertTrue(content.contains("\"entries\""));
        assertTrue(content.contains("\"level\": \"ERROR\""));
        assertTrue(content.contains("\"message\": \"Test message 1\""));
    }

    @Test
    @DisplayName("Should export logs as HTML format")
    void testExportAsHTML() throws IOException {
        // Given
        List<LogEntry> entries = createTestEntries();
        Path outputPath = tempDir.resolve("logs.html");
        LogExportService.ExportOptions options = LogExportService.ExportOptions.builder().build();

        // When
        exportService.exportLogs(entries, outputPath, LogExportService.ExportFormat.HTML, options);

        // Then
        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath);
        assertTrue(content.contains("<!DOCTYPE html>"));
        assertTrue(content.contains("<table>"));
        assertTrue(content.contains("class=\"level-ERROR\""));
    }

    @Test
    @DisplayName("Should respect max entries limit")
    void testMaxEntriesLimit() throws IOException {
        // Given
        List<LogEntry> entries = createManyEntries(10);
        Path outputPath = tempDir.resolve("limited.txt");
        LogExportService.ExportOptions options =
                LogExportService.ExportOptions.builder().maxEntries(5).build();

        // When
        exportService.exportLogs(entries, outputPath, LogExportService.ExportFormat.TEXT, options);

        // Then
        String content = Files.readString(outputPath);
        // Count occurrences of log messages
        long count =
                Arrays.stream(content.split("\n"))
                        .filter(line -> line.contains("Test message"))
                        .count();
        assertEquals(5, count);
    }

    @Test
    @DisplayName("Should include stack traces when enabled")
    void testIncludeStackTraces() throws IOException {
        // Given
        Exception exception = new RuntimeException("Test exception");
        LogEntry entry =
                LogEntry.builder()
                        .id("1")
                        .timestamp(LocalDateTime.now())
                        .level(LogEntry.LogLevel.ERROR)
                        .type("ERROR")
                        .source("Test")
                        .message("Error occurred")
                        .exception(exception)
                        .build();

        Path outputPath = tempDir.resolve("with-exception.txt");
        LogExportService.ExportOptions options =
                LogExportService.ExportOptions.builder().includeStackTraces(true).build();

        // When
        exportService.exportLogs(
                List.of(entry), outputPath, LogExportService.ExportFormat.TEXT, options);

        // Then
        String content = Files.readString(outputPath);
        assertTrue(content.contains("Exception:"));
        assertTrue(content.contains("RuntimeException"));
    }

    @Test
    @DisplayName("Should handle CSV escaping correctly")
    void testCSVEscaping() throws IOException {
        // Given
        LogEntry entry =
                LogEntry.builder()
                        .id("1")
                        .timestamp(LocalDateTime.now())
                        .level(LogEntry.LogLevel.INFO)
                        .type("INFO")
                        .source("Test")
                        .message("Message with \"quotes\" and, commas")
                        .build();

        Path outputPath = tempDir.resolve("escaped.csv");
        LogExportService.ExportOptions options = LogExportService.ExportOptions.builder().build();

        // When
        exportService.exportLogs(
                List.of(entry), outputPath, LogExportService.ExportFormat.CSV, options);

        // Then
        List<String> lines = Files.readAllLines(outputPath);
        assertTrue(lines.get(1).contains("\"Message with \"\"quotes\"\" and, commas\""));
    }

    @Test
    @DisplayName("Should generate correct default filename")
    void testDefaultFilename() {
        // When
        String filename = exportService.getDefaultFilename(LogExportService.ExportFormat.CSV);

        // Then
        assertTrue(filename.startsWith("logs_export_"));
        assertTrue(filename.endsWith(".csv"));
    }

    private List<LogEntry> createTestEntries() {
        return List.of(
                LogEntry.builder()
                        .id("1")
                        .timestamp(LocalDateTime.now())
                        .level(LogEntry.LogLevel.ERROR)
                        .type("ERROR")
                        .source("TestSource")
                        .message("Test message 1")
                        .details("Some details")
                        .build(),
                LogEntry.builder()
                        .id("2")
                        .timestamp(LocalDateTime.now())
                        .level(LogEntry.LogLevel.INFO)
                        .type("INFO")
                        .source("TestSource")
                        .message("Test message 2")
                        .metadata(Map.of("key", "value"))
                        .build());
    }

    private List<LogEntry> createManyEntries(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(
                        i ->
                                LogEntry.builder()
                                        .id(String.valueOf(i))
                                        .timestamp(LocalDateTime.now())
                                        .level(LogEntry.LogLevel.INFO)
                                        .type("INFO")
                                        .source("Test")
                                        .message("Test message " + i)
                                        .build())
                .toList();
    }
}
