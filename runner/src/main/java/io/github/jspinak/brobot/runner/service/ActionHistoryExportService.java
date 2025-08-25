package io.github.jspinak.brobot.runner.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.runner.persistence.entity.ActionRecordEntity;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for exporting and importing ActionHistory data in various formats.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionHistoryExportService {
    
    private final ObjectMapper objectMapper;
    private final ActionRecordingService recordingService;
    private final CsvMapper csvMapper = new CsvMapper();
    
    /**
     * Export formats supported
     */
    public enum ExportFormat {
        JSON,
        CSV,
        JSON_COMPRESSED,
        CSV_COMPRESSED
    }
    
    /**
     * Export ActionHistory to file
     */
    public void exportToFile(ActionHistory history, File file) throws IOException {
        exportToFile(history, file, ExportFormat.JSON);
    }
    
    /**
     * Export ActionHistory to file with specified format
     */
    public void exportToFile(ActionHistory history, File file, ExportFormat format) throws IOException {
        switch (format) {
            case JSON:
                exportToJson(history, file);
                break;
            case CSV:
                exportToCsv(history, file);
                break;
            case JSON_COMPRESSED:
                exportToCompressedJson(history, file);
                break;
            case CSV_COMPRESSED:
                exportToCompressedCsv(history, file);
                break;
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
        
        log.info("Exported ActionHistory to {} (format: {})", file.getAbsolutePath(), format);
    }
    
    /**
     * Export session to file
     */
    public void exportSessionToFile(Long sessionId, File file) throws IOException {
        exportSessionToFile(sessionId, file, ExportFormat.JSON);
    }
    
    /**
     * Export session to file with specified format
     */
    public void exportSessionToFile(Long sessionId, File file, ExportFormat format) throws IOException {
        ActionHistory history = recordingService.exportSession(sessionId);
        exportToFile(history, file, format);
        
        // Mark session as exported
        String formatStr = format.toString();
        recordingService.markSessionExported(sessionId, file.getAbsolutePath(), formatStr);
    }
    
    /**
     * Export to JSON format
     */
    private void exportToJson(ActionHistory history, File file) throws IOException {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(file, history);
    }
    
    /**
     * Export to CSV format
     */
    private void exportToCsv(ActionHistory history, File file) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        for (ActionRecord record : history.getSnapshots()) {
            Map<String, Object> row = new HashMap<>();
            row.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            row.put("action_type", record.getActionConfig() != null ? 
                record.getActionConfig().getClass().getSimpleName() : "Unknown");
            row.put("success", record.isActionSuccess());
            row.put("duration_ms", record.getDuration());
            row.put("text", record.getText());
            row.put("state_id", record.getStateId());
            
            // Add match information if available
            if (record.getMatchList() != null && !record.getMatchList().isEmpty()) {
                var match = record.getMatchList().get(0); // Use first match for CSV
                row.put("match_x", match.getRegion().getX());
                row.put("match_y", match.getRegion().getY());
                row.put("match_width", match.getRegion().getW());
                row.put("match_height", match.getRegion().getH());
                row.put("similarity", match.getScore());
            } else {
                row.put("match_x", null);
                row.put("match_y", null);
                row.put("match_width", null);
                row.put("match_height", null);
                row.put("similarity", null);
            }
            
            rows.add(row);
        }
        
        if (!rows.isEmpty()) {
            CsvSchema schema = csvMapper.schemaFor(Map.class).withHeader();
            csvMapper.writer(schema).writeValue(file, rows);
        }
    }
    
    /**
     * Export to compressed JSON
     */
    private void exportToCompressedJson(ActionHistory history, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            ZipEntry entry = new ZipEntry("action-history.json");
            zos.putNextEntry(entry);
            
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            byte[] jsonBytes = objectMapper.writeValueAsBytes(history);
            zos.write(jsonBytes);
            
            zos.closeEntry();
        }
    }
    
    /**
     * Export to compressed CSV
     */
    private void exportToCompressedCsv(ActionHistory history, File file) throws IOException {
        // First create CSV in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        File tempFile = File.createTempFile("export", ".csv");
        
        try {
            exportToCsv(history, tempFile);
            byte[] csvBytes = Files.readAllBytes(tempFile.toPath());
            
            // Compress the CSV
            try (FileOutputStream fos = new FileOutputStream(file);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                
                ZipEntry entry = new ZipEntry("action-history.csv");
                zos.putNextEntry(entry);
                zos.write(csvBytes);
                zos.closeEntry();
            }
        } finally {
            tempFile.delete();
        }
    }
    
    /**
     * Import ActionHistory from file
     */
    public ActionHistory importFromFile(File file) throws IOException {
        if (file.getName().endsWith(".zip")) {
            return importFromCompressed(file);
        } else if (file.getName().endsWith(".csv")) {
            return importFromCsv(file);
        } else {
            return importFromJson(file);
        }
    }
    
    /**
     * Import from JSON
     */
    private ActionHistory importFromJson(File file) throws IOException {
        return objectMapper.readValue(file, ActionHistory.class);
    }
    
    /**
     * Import from CSV
     */
    private ActionHistory importFromCsv(File file) throws IOException {
        ActionHistory history = new ActionHistory();
        
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        var parser = csvMapper.readerFor(Map.class).with(schema);
        
        try (Reader reader = new FileReader(file)) {
            MappingIterator<Map> iterator = parser.readValues(reader);
            
            while (iterator.hasNext()) {
                Map<String, String> row = iterator.next();
                // Reconstruct ActionRecord from CSV row
                ActionRecord record = new ActionRecord();
                
                // Basic fields
                record.setActionSuccess(Boolean.parseBoolean(row.get("success")));
                record.setDuration(Double.parseDouble(row.getOrDefault("duration_ms", "0")));
                record.setText(row.get("text"));
                
                String stateIdStr = row.get("state_id");
                if (stateIdStr != null && !stateIdStr.isEmpty()) {
                    record.setStateId(Long.parseLong(stateIdStr));
                }
                
                // Note: ActionConfig cannot be fully reconstructed from CSV
                // This is a limitation of CSV format
                
                history.addSnapshot(record);
            }
        }
        
        return history;
    }
    
    /**
     * Import from compressed file
     */
    private ActionHistory importFromCompressed(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(fis)) {
            
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                if (entry.getName().endsWith(".json")) {
                    return objectMapper.readValue(zis, ActionHistory.class);
                } else if (entry.getName().endsWith(".csv")) {
                    // Extract to temp file and import
                    File tempFile = File.createTempFile("import", ".csv");
                    try {
                        Files.copy(zis, tempFile.toPath());
                        return importFromCsv(tempFile);
                    } finally {
                        tempFile.delete();
                    }
                }
            }
        }
        
        throw new IOException("No valid entry found in compressed file");
    }
    
    /**
     * Import to a new session
     */
    public RecordingSessionEntity importToSession(File file, String sessionName) throws IOException {
        ActionHistory history = importFromFile(file);
        
        String application = extractApplicationFromFile(file);
        return recordingService.importSession(history, sessionName, application);
    }
    
    /**
     * Export multiple sessions to a single archive
     */
    public void exportMultipleSessions(List<Long> sessionIds, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            for (Long sessionId : sessionIds) {
                RecordingSessionEntity session = recordingService.getSession(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
                
                ActionHistory history = recordingService.exportSession(sessionId);
                
                String entryName = String.format("%s_%s.json", 
                    session.getName().replaceAll("[^a-zA-Z0-9-_]", "_"),
                    session.getStartTime().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                
                byte[] jsonBytes = objectMapper.writeValueAsBytes(history);
                zos.write(jsonBytes);
                
                zos.closeEntry();
            }
        }
        
        log.info("Exported {} sessions to {}", sessionIds.size(), outputFile.getAbsolutePath());
    }
    
    /**
     * Extract application name from file name
     */
    private String extractApplicationFromFile(File file) {
        String name = file.getName();
        // Try to extract application name from file name pattern
        // e.g., "login-flow_app-name_20240115.json"
        String[] parts = name.split("_");
        if (parts.length > 1) {
            return parts[1];
        }
        return "imported";
    }
}