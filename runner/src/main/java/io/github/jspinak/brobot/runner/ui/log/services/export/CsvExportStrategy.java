package io.github.jspinak.brobot.runner.ui.log.services.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;

import lombok.extern.slf4j.Slf4j;

/** Export strategy for CSV format. */
@Slf4j
public class CsvExportStrategy extends AbstractExportStrategy {

    private static final String DELIMITER = ",";
    private static final String QUOTE = "\"";

    @Override
    protected void writeExport(BufferedWriter writer, List<LogEntry> entries, ExportOptions options)
            throws IOException {
        // Write header
        if (options.isIncludeHeaders()) {
            writeHeader(writer, options);
        }

        // Write entries
        for (LogEntry entry : entries) {
            if (options.isIncludeTimestamps()) {
                writer.write(escape(formatTimestamp(entry.getTimestamp(), options)));
                writer.write(DELIMITER);
            }

            writer.write(escape(entry.getLevel().name()));
            writer.write(DELIMITER);
            writer.write(escape(entry.getType()));
            writer.write(DELIMITER);
            writer.write(escape(entry.getSource()));
            writer.write(DELIMITER);
            writer.write(escape(entry.getMessage()));

            if (options.isIncludeStackTraces()) {
                writer.write(DELIMITER);
                writer.write(escape(entry.hasException() ? entry.getExceptionStackTrace() : ""));
            }

            if (options.isIncludeMetadata()) {
                writer.write(DELIMITER);
                writer.write(escape(entry.hasMetadata() ? entry.getMetadata().toString() : ""));
            }

            writer.newLine();
        }
    }

    private void writeHeader(BufferedWriter writer, ExportOptions options) throws IOException {
        boolean first = true;

        if (options.isIncludeTimestamps()) {
            writer.write("Timestamp");
            first = false;
        }

        if (!first) writer.write(DELIMITER);
        writer.write("Level");
        writer.write(DELIMITER);
        writer.write("Type");
        writer.write(DELIMITER);
        writer.write("Source");
        writer.write(DELIMITER);
        writer.write("Message");

        if (options.isIncludeStackTraces()) {
            writer.write(DELIMITER);
            writer.write("Exception");
        }

        if (options.isIncludeMetadata()) {
            writer.write(DELIMITER);
            writer.write("Metadata");
        }

        writer.newLine();
    }

    @Override
    protected String escape(String value) {
        if (value == null) {
            return "";
        }

        // If value contains delimiter, quotes, or newlines, quote it
        if (value.contains(DELIMITER)
                || value.contains(QUOTE)
                || value.contains("\n")
                || value.contains("\r")) {
            // Escape quotes by doubling them
            String escaped = value.replace(QUOTE, QUOTE + QUOTE);
            return QUOTE + escaped + QUOTE;
        }

        return value;
    }

    @Override
    public String getFormatName() {
        return "CSV";
    }

    @Override
    public String getFileExtension() {
        return ".csv";
    }

    @Override
    public String getFormatDescription() {
        return "CSV Files";
    }
}
