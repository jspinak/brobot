package io.github.jspinak.brobot.runner.ui.log.services.export;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Strategy interface for log export implementations.
 */
public interface ExportStrategy {
    
    /**
     * Exports log entries to the specified path.
     * 
     * @param entries The log entries to export
     * @param outputPath The path to write the export file
     * @param options Export options configuration
     * @throws IOException If an I/O error occurs during export
     */
    void export(List<LogEntry> entries, Path outputPath, ExportOptions options) throws IOException;
    
    /**
     * Gets the format name for this strategy.
     * 
     * @return The format name (e.g., "TEXT", "CSV", "JSON")
     */
    String getFormatName();
    
    /**
     * Gets the file extension for this format.
     * 
     * @return The file extension including the dot (e.g., ".txt", ".csv")
     */
    String getFileExtension();
    
    /**
     * Gets the format description for file chooser dialogs.
     * 
     * @return The format description (e.g., "Text Files")
     */
    String getFormatDescription();
}