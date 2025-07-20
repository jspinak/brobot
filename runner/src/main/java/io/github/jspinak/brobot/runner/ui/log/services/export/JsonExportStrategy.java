package io.github.jspinak.brobot.runner.ui.log.services.export;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Export strategy for JSON format.
 */
@Slf4j
public class JsonExportStrategy extends AbstractExportStrategy {
    
    @Override
    protected void writeExport(BufferedWriter writer, List<LogEntry> entries, 
                             ExportOptions options) throws IOException {
        writer.write("{\n");
        writer.write("  \"exportDate\": \"" + getCurrentTimestamp() + "\",\n");
        writer.write("  \"totalEntries\": " + entries.size() + ",\n");
        writer.write("  \"entries\": [\n");
        
        for (int i = 0; i < entries.size(); i++) {
            LogEntry entry = entries.get(i);
            writeEntry(writer, entry, options, i < entries.size() - 1);
        }
        
        writer.write("  ]\n");
        writer.write("}\n");
    }
    
    private void writeEntry(BufferedWriter writer, LogEntry entry, ExportOptions options, 
                          boolean hasNext) throws IOException {
        writer.write("    {\n");
        writer.write("      \"id\": \"" + escape(entry.getId()) + "\",\n");
        
        if (options.isIncludeTimestamps()) {
            writer.write("      \"timestamp\": \"" + 
                formatTimestamp(entry.getTimestamp(), options) + "\",\n");
        }
        
        writer.write("      \"level\": \"" + entry.getLevel().name() + "\",\n");
        writer.write("      \"type\": \"" + escape(entry.getType()) + "\",\n");
        writer.write("      \"source\": \"" + escape(entry.getSource()) + "\",\n");
        writer.write("      \"message\": \"" + escape(entry.getMessage()) + "\"");
        
        if (entry.getDetails() != null) {
            writer.write(",\n      \"details\": \"" + escape(entry.getDetails()) + "\"");
        }
        
        if (options.isIncludeStackTraces() && entry.hasException()) {
            writer.write(",\n      \"exception\": \"" + 
                escape(entry.getExceptionStackTrace()) + "\"");
        }
        
        if (options.isIncludeMetadata() && entry.hasMetadata()) {
            writer.write(",\n      \"metadata\": " + mapToJson(entry.getMetadata()));
        }
        
        writer.write("\n    }");
        if (hasNext) {
            writer.write(",");
        }
        writer.newLine();
    }
    
    private String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{\n");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            json.append("        \"").append(escape(entry.getKey())).append("\": ");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escape(value.toString())).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value.toString());
            } else if (value == null) {
                json.append("null");
            } else {
                json.append("\"").append(escape(value.toString())).append("\"");
            }
            
            first = false;
        }
        
        json.append("\n      }");
        return json.toString();
    }
    
    @Override
    protected String escape(String value) {
        if (value == null) {
            return "";
        }
        
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    
    @Override
    public String getFormatName() {
        return "JSON";
    }
    
    @Override
    public String getFileExtension() {
        return ".json";
    }
    
    @Override
    public String getFormatDescription() {
        return "JSON Files";
    }
}