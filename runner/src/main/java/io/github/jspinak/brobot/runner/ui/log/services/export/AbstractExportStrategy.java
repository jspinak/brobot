package io.github.jspinak.brobot.runner.ui.log.services.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for export strategies. Provides common functionality for all export
 * implementations.
 */
@Slf4j
public abstract class AbstractExportStrategy implements ExportStrategy {

    protected static final DateTimeFormatter EXPORT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void export(List<LogEntry> entries, Path outputPath, ExportOptions options)
            throws IOException {
        log.info(
                "Exporting {} log entries to {} as {}",
                entries.size(),
                outputPath,
                getFormatName());

        // Limit entries if specified
        List<LogEntry> entriesToExport = entries;
        if (options.getMaxEntries() < entries.size()) {
            entriesToExport = entries.subList(0, options.getMaxEntries());
            log.debug("Limited export to {} entries", options.getMaxEntries());
        }

        // Create parent directories if needed
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }

        // Perform the export
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writeExport(writer, entriesToExport, options);
        }

        log.info("Successfully exported {} entries to {}", entriesToExport.size(), outputPath);
    }

    /**
     * Writes the export data to the writer.
     *
     * @param writer The writer to write to
     * @param entries The entries to export
     * @param options Export options
     * @throws IOException If an I/O error occurs
     */
    protected abstract void writeExport(
            BufferedWriter writer, List<LogEntry> entries, ExportOptions options)
            throws IOException;

    /**
     * Escapes special characters for the specific format.
     *
     * @param value The value to escape
     * @return The escaped value
     */
    protected abstract String escape(String value);

    /**
     * Formats a timestamp according to the options.
     *
     * @param timestamp The timestamp to format
     * @param options Export options containing date format
     * @return The formatted timestamp
     */
    protected String formatTimestamp(LocalDateTime timestamp, ExportOptions options) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(options.getDateFormat());
        return timestamp.format(formatter);
    }

    /**
     * Gets the current export timestamp.
     *
     * @return The formatted current timestamp
     */
    protected String getCurrentTimestamp() {
        return LocalDateTime.now().format(EXPORT_DATE_FORMAT);
    }
}
