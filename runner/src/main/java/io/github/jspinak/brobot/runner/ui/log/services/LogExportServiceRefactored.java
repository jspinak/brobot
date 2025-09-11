package io.github.jspinak.brobot.runner.ui.log.services;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import io.github.jspinak.brobot.runner.ui.log.services.export.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored service for exporting log entries using the Strategy pattern. Delegates to specific
 * export strategies based on format.
 */
@Slf4j
@Service
public class LogExportServiceRefactored {

    private final Map<ExportFormat, ExportStrategy> strategies = new HashMap<>();

    /** Export format options. */
    public enum ExportFormat {
        TEXT,
        CSV,
        JSON,
        HTML,
        MARKDOWN
    }

    public LogExportServiceRefactored() {
        // Register default strategies
        registerStrategy(ExportFormat.TEXT, new TextExportStrategy());
        registerStrategy(ExportFormat.CSV, new CsvExportStrategy());
        registerStrategy(ExportFormat.JSON, new JsonExportStrategy());
        registerStrategy(ExportFormat.HTML, new HtmlExportStrategy());
        registerStrategy(ExportFormat.MARKDOWN, new MarkdownExportStrategy());
    }

    /**
     * Registers an export strategy for a format.
     *
     * @param format The export format
     * @param strategy The strategy implementation
     */
    public void registerStrategy(ExportFormat format, ExportStrategy strategy) {
        strategies.put(format, strategy);
        log.debug("Registered export strategy for format: {}", format);
    }

    /**
     * Exports logs to a file in the specified format.
     *
     * @param entries The log entries to export
     * @param outputPath The output file path
     * @param format The export format
     * @param options Export options
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If the format is not supported
     */
    public void exportLogs(
            List<LogEntry> entries, Path outputPath, ExportFormat format, ExportOptions options)
            throws IOException {
        ExportStrategy strategy = strategies.get(format);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }

        strategy.export(entries, outputPath, options);
    }

    /** Exports logs using default options. */
    public void exportLogs(List<LogEntry> entries, Path outputPath, ExportFormat format)
            throws IOException {
        exportLogs(entries, outputPath, format, ExportOptions.defaultOptions());
    }

    /**
     * Gets the file extension for a format.
     *
     * @param format The export format
     * @return The file extension or null if format not supported
     */
    public String getFileExtension(ExportFormat format) {
        ExportStrategy strategy = strategies.get(format);
        return strategy != null ? strategy.getFileExtension() : null;
    }

    /**
     * Gets the format description for file chooser dialogs.
     *
     * @param format The export format
     * @return The format description or null if format not supported
     */
    public String getFormatDescription(ExportFormat format) {
        ExportStrategy strategy = strategies.get(format);
        return strategy != null ? strategy.getFormatDescription() : null;
    }

    /**
     * Checks if a format is supported.
     *
     * @param format The export format
     * @return true if the format is supported
     */
    public boolean isFormatSupported(ExportFormat format) {
        return strategies.containsKey(format);
    }

    /**
     * Gets all supported formats.
     *
     * @return Array of supported formats
     */
    public ExportFormat[] getSupportedFormats() {
        return strategies.keySet().toArray(new ExportFormat[0]);
    }
}

/** HTML export strategy implementation. */
@Slf4j
class HtmlExportStrategy extends AbstractExportStrategy {

    @Override
    protected void writeExport(BufferedWriter writer, List<LogEntry> entries, ExportOptions options)
            throws IOException {
        writeHeader(writer);
        writeStyles(writer);
        writeBody(writer, entries, options);
        writeFooter(writer);
    }

    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
        writer.write("<meta charset=\"UTF-8\">\n");
        writer.write("<title>Log Export - " + getCurrentTimestamp() + "</title>\n");
    }

    private void writeStyles(BufferedWriter writer) throws IOException {
        writer.write("<style>\n");
        writer.write(
                "body { font-family: 'Consolas', 'Monaco', monospace; background: #f5f5f5; margin:"
                        + " 20px; }\n");
        writer.write("h1 { color: #333; }\n");
        writer.write(
                "table { border-collapse: collapse; width: 100%; background: white; box-shadow: 0"
                        + " 2px 4px rgba(0,0,0,0.1); }\n");
        writer.write("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        writer.write("th { background-color: #4CAF50; color: white; position: sticky; top: 0; }\n");
        writer.write("tr:nth-child(even) { background-color: #f9f9f9; }\n");
        writer.write("tr:hover { background-color: #f5f5f5; }\n");
        writer.write(".level-ERROR { color: #d32f2f; font-weight: bold; }\n");
        writer.write(".level-WARNING { color: #f57c00; }\n");
        writer.write(".level-INFO { color: #1976d2; }\n");
        writer.write(".level-DEBUG { color: #757575; }\n");
        writer.write(".message { white-space: pre-wrap; word-wrap: break-word; }\n");
        writer.write(
                ".exception { color: #d32f2f; font-size: 0.9em; background: #ffebee; padding: 4px;"
                        + " }\n");
        writer.write(".metadata { font-size: 0.85em; color: #666; }\n");
        writer.write("</style>\n</head>\n");
    }

    private void writeBody(BufferedWriter writer, List<LogEntry> entries, ExportOptions options)
            throws IOException {
        writer.write("<body>\n");
        writer.write("<h1>Log Export</h1>\n");
        writer.write("<p>Exported: " + getCurrentTimestamp() + "</p>\n");
        writer.write("<p>Total Entries: " + entries.size() + "</p>\n");

        writer.write("<table>\n<thead>\n<tr>\n");

        if (options.isIncludeTimestamps()) {
            writer.write("<th>Timestamp</th>\n");
        }
        writer.write("<th>Level</th>\n");
        writer.write("<th>Type</th>\n");
        writer.write("<th>Source</th>\n");
        writer.write("<th>Message</th>\n");
        writer.write("</tr>\n</thead>\n<tbody>\n");

        for (LogEntry entry : entries) {
            writeEntry(writer, entry, options);
        }

        writer.write("</tbody>\n</table>\n");
    }

    private void writeEntry(BufferedWriter writer, LogEntry entry, ExportOptions options)
            throws IOException {
        writer.write("<tr>\n");

        if (options.isIncludeTimestamps()) {
            writer.write(
                    "<td>" + escape(formatTimestamp(entry.getTimestamp(), options)) + "</td>\n");
        }

        writer.write(
                "<td class=\"level-"
                        + entry.getLevel().name()
                        + "\">"
                        + entry.getLevel().name()
                        + "</td>\n");
        writer.write("<td>" + escape(entry.getType()) + "</td>\n");
        writer.write("<td>" + escape(entry.getSource()) + "</td>\n");
        writer.write("<td class=\"message\">" + escape(entry.getMessage()));

        if (entry.getDetails() != null) {
            writer.write("<br><small>Details: " + escape(entry.getDetails()) + "</small>");
        }

        if (options.isIncludeStackTraces() && entry.hasException()) {
            writer.write(
                    "<br><div class=\"exception\">"
                            + escape(entry.getExceptionStackTrace())
                            + "</div>");
        }

        if (options.isIncludeMetadata() && entry.hasMetadata()) {
            writer.write(
                    "<br><div class=\"metadata\">Metadata: "
                            + escape(entry.getMetadata().toString())
                            + "</div>");
        }

        writer.write("</td>\n</tr>\n");
    }

    private void writeFooter(BufferedWriter writer) throws IOException {
        writer.write("</body>\n</html>\n");
    }

    @Override
    protected String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Override
    public String getFormatName() {
        return "HTML";
    }

    @Override
    public String getFileExtension() {
        return ".html";
    }

    @Override
    public String getFormatDescription() {
        return "HTML Files";
    }
}

/** Markdown export strategy implementation. */
@Slf4j
class MarkdownExportStrategy extends AbstractExportStrategy {

    @Override
    protected void writeExport(BufferedWriter writer, List<LogEntry> entries, ExportOptions options)
            throws IOException {
        writer.write("# Log Export\n\n");
        writer.write("**Exported:** " + getCurrentTimestamp() + "\n");
        writer.write("**Total Entries:** " + entries.size() + "\n\n");

        // Write table header
        writer.write("| ");
        if (options.isIncludeTimestamps()) {
            writer.write("Timestamp | ");
        }
        writer.write("Level | Type | Source | Message |\n");

        writer.write("| ");
        if (options.isIncludeTimestamps()) {
            writer.write("--- | ");
        }
        writer.write("--- | --- | --- | --- |\n");

        // Write entries
        for (LogEntry entry : entries) {
            writer.write("| ");

            if (options.isIncludeTimestamps()) {
                writer.write(escape(formatTimestamp(entry.getTimestamp(), options)) + " | ");
            }

            writer.write("**" + entry.getLevel().name() + "** | ");
            writer.write(escape(entry.getType()) + " | ");
            writer.write(escape(entry.getSource()) + " | ");
            writer.write(escape(entry.getMessage()));

            if (entry.getDetails() != null) {
                writer.write("<br>_Details: " + escape(entry.getDetails()) + "_");
            }

            if (options.isIncludeStackTraces() && entry.hasException()) {
                writer.write("<br>`" + escape(entry.getExceptionStackTrace()) + "`");
            }

            writer.write(" |\n");
        }
    }

    @Override
    protected String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("|", "\\|").replace("\n", "<br>").replace("\r", "");
    }

    @Override
    public String getFormatName() {
        return "MARKDOWN";
    }

    @Override
    public String getFileExtension() {
        return ".md";
    }

    @Override
    public String getFormatDescription() {
        return "Markdown Files";
    }
}
