package io.github.jspinak.brobot.runner.ui.log.services;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for exporting log entries to various formats. Supports text, CSV, JSON, and HTML formats.
 */
@Slf4j
@Service
public class LogExportService {

    private static final DateTimeFormatter EXPORT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String CSV_DELIMITER = ",";
    private static final String CSV_QUOTE = "\"";

    /** Export format options. */
    public enum ExportFormat {
        TEXT("Text Files", "*.txt"),
        CSV("CSV Files", "*.csv"),
        JSON("JSON Files", "*.json"),
        HTML("HTML Files", "*.html"),
        MARKDOWN("Markdown Files", "*.md");

        private final String description;
        private final String extension;

        ExportFormat(String description, String extension) {
            this.description = description;
            this.extension = extension;
        }

        public String getDescription() {
            return description;
        }

        public String getExtension() {
            return extension;
        }
    }

    /** Export options configuration. */
    public static class ExportOptions {
        private boolean includeHeaders = true;
        private boolean includeTimestamps = true;
        private boolean includeStackTraces = true;
        private boolean includeMetadata = false;
        private String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
        private int maxEntries = Integer.MAX_VALUE;

        public static ExportOptionsBuilder builder() {
            return new ExportOptionsBuilder();
        }

        public static class ExportOptionsBuilder {
            private ExportOptions options = new ExportOptions();

            public ExportOptionsBuilder includeHeaders(boolean include) {
                options.includeHeaders = include;
                return this;
            }

            public ExportOptionsBuilder includeTimestamps(boolean include) {
                options.includeTimestamps = include;
                return this;
            }

            public ExportOptionsBuilder includeStackTraces(boolean include) {
                options.includeStackTraces = include;
                return this;
            }

            public ExportOptionsBuilder includeMetadata(boolean include) {
                options.includeMetadata = include;
                return this;
            }

            public ExportOptionsBuilder dateFormat(String format) {
                options.dateFormat = format;
                return this;
            }

            public ExportOptionsBuilder maxEntries(int max) {
                options.maxEntries = max;
                return this;
            }

            public ExportOptions build() {
                return options;
            }
        }
    }

    /** Exports logs to a file in the specified format. */
    public void exportLogs(
            List<LogEntry> entries, Path outputPath, ExportFormat format, ExportOptions options)
            throws IOException {
        log.info("Exporting {} log entries to {} as {}", entries.size(), outputPath, format);

        // Limit entries if specified
        List<LogEntry> entriesToExport = entries;
        if (options.maxEntries < entries.size()) {
            entriesToExport = entries.subList(0, options.maxEntries);
            log.debug("Limited export to {} entries", options.maxEntries);
        }

        // Export based on format
        switch (format) {
            case TEXT:
                exportAsText(entriesToExport, outputPath, options);
                break;
            case CSV:
                exportAsCSV(entriesToExport, outputPath, options);
                break;
            case JSON:
                exportAsJSON(entriesToExport, outputPath, options);
                break;
            case HTML:
                exportAsHTML(entriesToExport, outputPath, options);
                break;
            case MARKDOWN:
                exportAsMarkdown(entriesToExport, outputPath, options);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }

        log.info("Successfully exported {} entries to {}", entriesToExport.size(), outputPath);
    }

    /** Exports logs as plain text. */
    private void exportAsText(List<LogEntry> entries, Path outputPath, ExportOptions options)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            // Write header
            if (options.includeHeaders) {
                writer.write("=== Log Export ===\n");
                writer.write("Exported: " + LocalDateTime.now().format(EXPORT_DATE_FORMAT) + "\n");
                writer.write("Total Entries: " + entries.size() + "\n");
                writer.write("==================\n\n");
            }

            // Write entries
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(options.dateFormat);
            for (LogEntry entry : entries) {
                if (options.includeTimestamps) {
                    writer.write("[" + entry.getTimestamp().format(formatter) + "] ");
                }

                writer.write("[" + entry.getLevel().name() + "] ");
                writer.write("[" + entry.getSource() + "] ");
                writer.write(entry.getMessage());
                writer.newLine();

                if (entry.getDetails() != null) {
                    writer.write("  Details: " + entry.getDetails());
                    writer.newLine();
                }

                if (options.includeStackTraces && entry.hasException()) {
                    writer.write("  Exception: " + entry.getExceptionStackTrace());
                    writer.newLine();
                }

                if (options.includeMetadata && entry.hasMetadata()) {
                    writer.write("  Metadata: " + entry.getMetadata());
                    writer.newLine();
                }

                writer.newLine();
            }
        }
    }

    /** Exports logs as CSV. */
    private void exportAsCSV(List<LogEntry> entries, Path outputPath, ExportOptions options)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            // Write header
            if (options.includeHeaders) {
                writer.write("Timestamp,Level,Type,Source,Message");
                if (options.includeStackTraces) {
                    writer.write(",Exception");
                }
                if (options.includeMetadata) {
                    writer.write(",Metadata");
                }
                writer.newLine();
            }

            // Write entries
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(options.dateFormat);
            for (LogEntry entry : entries) {
                writer.write(escapeCSV(entry.getTimestamp().format(formatter)));
                writer.write(CSV_DELIMITER);
                writer.write(escapeCSV(entry.getLevel().name()));
                writer.write(CSV_DELIMITER);
                writer.write(escapeCSV(entry.getType()));
                writer.write(CSV_DELIMITER);
                writer.write(escapeCSV(entry.getSource()));
                writer.write(CSV_DELIMITER);
                writer.write(escapeCSV(entry.getMessage()));

                if (options.includeStackTraces) {
                    writer.write(CSV_DELIMITER);
                    writer.write(
                            escapeCSV(entry.hasException() ? entry.getExceptionStackTrace() : ""));
                }

                if (options.includeMetadata) {
                    writer.write(CSV_DELIMITER);
                    writer.write(
                            escapeCSV(entry.hasMetadata() ? entry.getMetadata().toString() : ""));
                }

                writer.newLine();
            }
        }
    }

    /** Exports logs as JSON. */
    private void exportAsJSON(List<LogEntry> entries, Path outputPath, ExportOptions options)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("{\n");
            writer.write(
                    "  \"exportDate\": \""
                            + LocalDateTime.now().format(EXPORT_DATE_FORMAT)
                            + "\",\n");
            writer.write("  \"totalEntries\": " + entries.size() + ",\n");
            writer.write("  \"entries\": [\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(options.dateFormat);
            for (int i = 0; i < entries.size(); i++) {
                LogEntry entry = entries.get(i);
                writer.write("    {\n");
                writer.write("      \"id\": \"" + escapeJSON(entry.getId()) + "\",\n");

                if (options.includeTimestamps) {
                    writer.write(
                            "      \"timestamp\": \""
                                    + entry.getTimestamp().format(formatter)
                                    + "\",\n");
                }

                writer.write("      \"level\": \"" + entry.getLevel().name() + "\",\n");
                writer.write("      \"type\": \"" + escapeJSON(entry.getType()) + "\",\n");
                writer.write("      \"source\": \"" + escapeJSON(entry.getSource()) + "\",\n");
                writer.write("      \"message\": \"" + escapeJSON(entry.getMessage()) + "\"");

                if (entry.getDetails() != null) {
                    writer.write(
                            ",\n      \"details\": \"" + escapeJSON(entry.getDetails()) + "\"");
                }

                if (options.includeStackTraces && entry.hasException()) {
                    writer.write(
                            ",\n      \"exception\": \""
                                    + escapeJSON(entry.getExceptionStackTrace())
                                    + "\"");
                }

                if (options.includeMetadata && entry.hasMetadata()) {
                    writer.write(",\n      \"metadata\": " + mapToJSON(entry.getMetadata()));
                }

                writer.write("\n    }");
                if (i < entries.size() - 1) {
                    writer.write(",");
                }
                writer.newLine();
            }

            writer.write("  ]\n");
            writer.write("}\n");
        }
    }

    /** Exports logs as HTML. */
    private void exportAsHTML(List<LogEntry> entries, Path outputPath, ExportOptions options)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            // Write HTML header
            writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
            writer.write(
                    "<title>Log Export - "
                            + LocalDateTime.now().format(EXPORT_DATE_FORMAT)
                            + "</title>\n");
            writer.write("<style>\n");
            writer.write("body { font-family: monospace; background: #f5f5f5; }\n");
            writer.write("table { border-collapse: collapse; width: 100%; background: white; }\n");
            writer.write("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            writer.write(
                    "th { background-color: #4CAF50; color: white; position: sticky; top: 0; }\n");
            writer.write("tr:nth-child(even) { background-color: #f2f2f2; }\n");
            writer.write(".level-ERROR, .level-FATAL { color: #d32f2f; font-weight: bold; }\n");
            writer.write(".level-WARNING { color: #f57c00; }\n");
            writer.write(".level-INFO { color: #1976d2; }\n");
            writer.write(".level-DEBUG, .level-TRACE { color: #757575; }\n");
            writer.write(".message { white-space: pre-wrap; }\n");
            writer.write(".exception { color: #d32f2f; font-size: 0.9em; }\n");
            writer.write("</style>\n</head>\n<body>\n");

            writer.write("<h1>Log Export</h1>\n");
            writer.write(
                    "<p>Exported: " + LocalDateTime.now().format(EXPORT_DATE_FORMAT) + "</p>\n");
            writer.write("<p>Total Entries: " + entries.size() + "</p>\n");

            // Write table
            writer.write("<table>\n<thead>\n<tr>\n");
            if (options.includeTimestamps) {
                writer.write("<th>Timestamp</th>\n");
            }
            writer.write("<th>Level</th>\n");
            writer.write("<th>Type</th>\n");
            writer.write("<th>Source</th>\n");
            writer.write("<th>Message</th>\n");
            writer.write("</tr>\n</thead>\n<tbody>\n");

            // Write entries
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(options.dateFormat);
            for (LogEntry entry : entries) {
                writer.write("<tr>\n");

                if (options.includeTimestamps) {
                    writer.write(
                            "<td>"
                                    + escapeHTML(entry.getTimestamp().format(formatter))
                                    + "</td>\n");
                }

                writer.write(
                        "<td class=\"level-"
                                + entry.getLevel().name()
                                + "\">"
                                + entry.getLevel().name()
                                + "</td>\n");
                writer.write("<td>" + escapeHTML(entry.getType()) + "</td>\n");
                writer.write("<td>" + escapeHTML(entry.getSource()) + "</td>\n");
                writer.write("<td class=\"message\">" + escapeHTML(entry.getMessage()));

                if (entry.getDetails() != null) {
                    writer.write(
                            "<br><small>Details: " + escapeHTML(entry.getDetails()) + "</small>");
                }

                if (options.includeStackTraces && entry.hasException()) {
                    writer.write(
                            "<br><pre class=\"exception\">"
                                    + escapeHTML(entry.getExceptionStackTrace())
                                    + "</pre>");
                }

                writer.write("</td>\n</tr>\n");
            }

            writer.write("</tbody>\n</table>\n</body>\n</html>\n");
        }
    }

    /** Exports logs as Markdown. */
    private void exportAsMarkdown(List<LogEntry> entries, Path outputPath, ExportOptions options)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            // Write header
            writer.write("# Log Export\n\n");
            writer.write("**Exported:** " + LocalDateTime.now().format(EXPORT_DATE_FORMAT) + "\n");
            writer.write("**Total Entries:** " + entries.size() + "\n\n");

            // Write table header
            writer.write("| ");
            if (options.includeTimestamps) {
                writer.write("Timestamp | ");
            }
            writer.write("Level | Type | Source | Message |\n");

            writer.write("| ");
            if (options.includeTimestamps) {
                writer.write("--- | ");
            }
            writer.write("--- | --- | --- | --- |\n");

            // Write entries
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(options.dateFormat);
            for (LogEntry entry : entries) {
                writer.write("| ");

                if (options.includeTimestamps) {
                    writer.write(escapeMD(entry.getTimestamp().format(formatter)) + " | ");
                }

                writer.write("**" + entry.getLevel().name() + "** | ");
                writer.write(escapeMD(entry.getType()) + " | ");
                writer.write(escapeMD(entry.getSource()) + " | ");
                writer.write(escapeMD(entry.getMessage()));

                if (entry.getDetails() != null) {
                    writer.write("<br>_" + escapeMD(entry.getDetails()) + "_");
                }

                writer.write(" |\n");

                if (options.includeStackTraces && entry.hasException()) {
                    writer.write("\n<details><summary>Exception Stack Trace</summary>\n\n```\n");
                    writer.write(entry.getExceptionStackTrace());
                    writer.write("\n```\n\n</details>\n\n");
                }
            }
        }
    }

    /** Escapes CSV values. */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(CSV_DELIMITER)
                || value.contains(CSV_QUOTE)
                || value.contains("\n")
                || value.contains("\r")) {
            return CSV_QUOTE + value.replace(CSV_QUOTE, CSV_QUOTE + CSV_QUOTE) + CSV_QUOTE;
        }

        return value;
    }

    /** Escapes JSON strings. */
    private String escapeJSON(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Escapes HTML content. */
    private String escapeHTML(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /** Escapes Markdown content. */
    private String escapeMD(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("|", "\\|").replace("\n", "<br>").replace("\r", "");
    }

    /** Converts a map to JSON string. */
    private String mapToJSON(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;

            sb.append("\"").append(escapeJSON(entry.getKey())).append("\": ");

            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(escapeJSON((String) value)).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(escapeJSON(value.toString())).append("\"");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /** Gets the default filename for an export. */
    public String getDefaultFilename(ExportFormat format) {
        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "logs_export_" + timestamp + getFileExtension(format);
    }

    /** Gets the file extension for a format. */
    private String getFileExtension(ExportFormat format) {
        switch (format) {
            case TEXT:
                return ".txt";
            case CSV:
                return ".csv";
            case JSON:
                return ".json";
            case HTML:
                return ".html";
            case MARKDOWN:
                return ".md";
            default:
                return ".txt";
        }
    }
}
