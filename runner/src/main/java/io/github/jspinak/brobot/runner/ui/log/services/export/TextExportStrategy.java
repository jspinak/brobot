package io.github.jspinak.brobot.runner.ui.log.services.export;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * Export strategy for plain text format.
 */
@Slf4j
public class TextExportStrategy extends AbstractExportStrategy {
    
    @Override
    protected void writeExport(BufferedWriter writer, List<LogEntry> entries, 
                             ExportOptions options) throws IOException {
        // Write header
        if (options.isIncludeHeaders()) {
            writer.write("=== Log Export ===\n");
            writer.write("Exported: " + getCurrentTimestamp() + "\n");
            writer.write("Total Entries: " + entries.size() + "\n");
            writer.write("==================\n\n");
        }
        
        // Write entries
        for (LogEntry entry : entries) {
            writeSeparator(writer);
            
            if (options.isIncludeTimestamps()) {
                writer.write("[" + formatTimestamp(entry.getTimestamp(), options) + "] ");
            }
            
            writer.write("[" + entry.getLevel().name() + "] ");
            writer.write("[" + entry.getSource() + "] ");
            writer.write(entry.getMessage());
            writer.newLine();
            
            if (entry.getDetails() != null) {
                writer.write("  Details: " + entry.getDetails());
                writer.newLine();
            }
            
            if (options.isIncludeStackTraces() && entry.hasException()) {
                writer.write("  Exception: " + entry.getExceptionStackTrace());
                writer.newLine();
            }
            
            if (options.isIncludeMetadata() && entry.hasMetadata()) {
                writer.write("  Metadata: " + entry.getMetadata());
                writer.newLine();
            }
            
            writer.newLine();
        }
    }
    
    private void writeSeparator(BufferedWriter writer) throws IOException {
        writer.write("-".repeat(80));
        writer.newLine();
    }
    
    @Override
    protected String escape(String value) {
        // No escaping needed for plain text
        return value != null ? value : "";
    }
    
    @Override
    public String getFormatName() {
        return "TEXT";
    }
    
    @Override
    public String getFileExtension() {
        return ".txt";
    }
    
    @Override
    public String getFormatDescription() {
        return "Text Files";
    }
}