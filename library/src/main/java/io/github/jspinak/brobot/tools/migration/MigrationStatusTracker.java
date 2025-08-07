package io.github.jspinak.brobot.tools.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks and reports the status of ActionHistory migration across the codebase.
 * 
 * <p>This service provides real-time tracking of migration progress, including:
 * <ul>
 *   <li>File-by-file migration status</li>
 *   <li>API usage statistics (deprecated vs modern)</li>
 *   <li>Migration health metrics</li>
 *   <li>Progress visualization and reporting</li>
 *   <li>Persistent state management</li>
 * </ul>
 * 
 * <p>The tracker maintains a persistent state file that survives application restarts,
 * allowing for long-running migration efforts to be tracked over time.</p>
 * 
 * @since 1.2.0
 */
@Service
@Slf4j
public class MigrationStatusTracker {
    
    private static final Path STATUS_FILE = Paths.get(
        System.getProperty("user.home"), ".brobot", "migration-status.json"
    );
    
    private final MigrationStatus status = new MigrationStatus();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, FileStatus> fileStatusMap = new ConcurrentHashMap<>();
    
    /**
     * Overall migration status.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MigrationStatus {
        private LocalDateTime startTime;
        private LocalDateTime lastUpdateTime;
        private int totalFiles;
        private int migratedFiles;
        private int pendingFiles;
        private int errorFiles;
        private long deprecatedApiCalls;
        private long modernApiCalls;
        private Map<String, Integer> migrationByModule = new HashMap<>();
        private List<String> criticalErrors = new ArrayList<>();
        private MigrationPhase currentPhase = MigrationPhase.ANALYSIS;
        
        public double getProgressPercentage() {
            if (totalFiles == 0) return 0;
            return (double) migratedFiles / totalFiles * 100;
        }
        
        public double getApiMigrationPercentage() {
            long total = deprecatedApiCalls + modernApiCalls;
            if (total == 0) return 100; // No API calls means fully migrated
            return (double) modernApiCalls / total * 100;
        }
        
        public String getEstimatedCompletion() {
            if (migratedFiles == 0 || pendingFiles == 0) {
                return "N/A";
            }
            
            long elapsedMinutes = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
            double rate = (double) migratedFiles / elapsedMinutes;
            long remainingMinutes = (long) (pendingFiles / rate);
            
            if (remainingMinutes < 60) {
                return remainingMinutes + " minutes";
            } else if (remainingMinutes < 1440) {
                return (remainingMinutes / 60) + " hours";
            } else {
                return (remainingMinutes / 1440) + " days";
            }
        }
    }
    
    /**
     * Migration phases.
     */
    public enum MigrationPhase {
        ANALYSIS("Analyzing codebase for deprecated usage"),
        PREPARATION("Preparing migration plan"),
        MIGRATION("Migrating files"),
        VALIDATION("Validating migrated code"),
        CLEANUP("Cleaning up and optimizing"),
        COMPLETE("Migration complete");
        
        private final String description;
        
        MigrationPhase(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Individual file migration status.
     */
    @Data
    public static class FileStatus {
        private String path;
        private FileState state = FileState.PENDING;
        private LocalDateTime lastModified;
        private int deprecatedUsages;
        private int migratedUsages;
        private List<String> errors = new ArrayList<>();
        private Map<String, Integer> apiUsageCount = new HashMap<>();
    }
    
    /**
     * File migration states.
     */
    public enum FileState {
        PENDING("Pending migration"),
        IN_PROGRESS("Migration in progress"),
        MIGRATED("Successfully migrated"),
        PARTIALLY_MIGRATED("Partially migrated"),
        ERROR("Migration failed"),
        SKIPPED("Skipped (no deprecated usage)");
        
        private final String description;
        
        FileState(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Initializes the tracker, loading previous state if available.
     */
    public MigrationStatusTracker() {
        loadStatus();
        
        // Start tracking if not already started
        if (status.getStartTime() == null) {
            status.setStartTime(LocalDateTime.now());
        }
    }
    
    /**
     * Starts tracking a new migration.
     * 
     * @param totalFiles total number of files to migrate
     */
    public void startMigration(int totalFiles) {
        log.info("Starting migration tracking for {} files", totalFiles);
        
        status.setStartTime(LocalDateTime.now());
        status.setTotalFiles(totalFiles);
        status.setPendingFiles(totalFiles);
        status.setMigratedFiles(0);
        status.setErrorFiles(0);
        status.setCurrentPhase(MigrationPhase.ANALYSIS);
        
        saveStatus();
    }
    
    /**
     * Updates the migration phase.
     * 
     * @param phase the new phase
     */
    public void updatePhase(MigrationPhase phase) {
        log.info("Migration phase updated: {} -> {}", status.getCurrentPhase(), phase);
        status.setCurrentPhase(phase);
        status.setLastUpdateTime(LocalDateTime.now());
        saveStatus();
    }
    
    /**
     * Records a file as analyzed.
     * 
     * @param filePath the file path
     * @param deprecatedUsages number of deprecated API usages found
     */
    public void recordFileAnalyzed(String filePath, int deprecatedUsages) {
        FileStatus fileStatus = fileStatusMap.computeIfAbsent(filePath, k -> new FileStatus());
        fileStatus.setPath(filePath);
        fileStatus.setDeprecatedUsages(deprecatedUsages);
        fileStatus.setState(deprecatedUsages > 0 ? FileState.PENDING : FileState.SKIPPED);
        fileStatus.setLastModified(LocalDateTime.now());
        
        if (deprecatedUsages == 0) {
            status.setPendingFiles(status.getPendingFiles() - 1);
        }
        
        status.setLastUpdateTime(LocalDateTime.now());
        saveStatus();
    }
    
    /**
     * Records a file migration start.
     * 
     * @param filePath the file path
     */
    public void recordMigrationStart(String filePath) {
        FileStatus fileStatus = fileStatusMap.get(filePath);
        if (fileStatus != null) {
            fileStatus.setState(FileState.IN_PROGRESS);
            fileStatus.setLastModified(LocalDateTime.now());
        }
        
        status.setLastUpdateTime(LocalDateTime.now());
        saveStatus();
    }
    
    /**
     * Records a successful file migration.
     * 
     * @param filePath the file path
     * @param migratedUsages number of usages successfully migrated
     */
    public void recordMigrationSuccess(String filePath, int migratedUsages) {
        FileStatus fileStatus = fileStatusMap.get(filePath);
        if (fileStatus != null) {
            fileStatus.setState(FileState.MIGRATED);
            fileStatus.setMigratedUsages(migratedUsages);
            fileStatus.setLastModified(LocalDateTime.now());
            
            status.setMigratedFiles(status.getMigratedFiles() + 1);
            status.setPendingFiles(Math.max(0, status.getPendingFiles() - 1));
            
            // Update module statistics
            String module = extractModule(filePath);
            status.getMigrationByModule().merge(module, 1, Integer::sum);
        }
        
        status.setLastUpdateTime(LocalDateTime.now());
        saveStatus();
        
        log.info("File migrated successfully: {} ({} usages)", filePath, migratedUsages);
    }
    
    /**
     * Records a file migration error.
     * 
     * @param filePath the file path
     * @param error the error message
     */
    public void recordMigrationError(String filePath, String error) {
        FileStatus fileStatus = fileStatusMap.get(filePath);
        if (fileStatus != null) {
            fileStatus.setState(FileState.ERROR);
            fileStatus.getErrors().add(error);
            fileStatus.setLastModified(LocalDateTime.now());
            
            status.setErrorFiles(status.getErrorFiles() + 1);
            status.setPendingFiles(Math.max(0, status.getPendingFiles() - 1));
            status.getCriticalErrors().add(filePath + ": " + error);
        }
        
        status.setLastUpdateTime(LocalDateTime.now());
        saveStatus();
        
        log.error("File migration failed: {} - {}", filePath, error);
    }
    
    /**
     * Records API usage statistics.
     * 
     * @param deprecated count of deprecated API calls
     * @param modern count of modern API calls
     */
    public void recordApiUsage(long deprecated, long modern) {
        status.setDeprecatedApiCalls(status.getDeprecatedApiCalls() + deprecated);
        status.setModernApiCalls(status.getModernApiCalls() + modern);
        status.setLastUpdateTime(LocalDateTime.now());
        saveStatus();
    }
    
    /**
     * Generates a comprehensive status report.
     * 
     * @return formatted status report
     */
    public String generateStatusReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("\n=== ActionHistory Migration Status Report ===\n");
        report.append("Generated: ").append(LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        // Overall Progress
        report.append("Overall Progress\n");
        report.append("----------------\n");
        report.append(String.format("Phase: %s - %s\n", 
            status.getCurrentPhase(), status.getCurrentPhase().getDescription()));
        report.append(String.format("Files: %d/%d (%.1f%%)\n", 
            status.getMigratedFiles(), status.getTotalFiles(), status.getProgressPercentage()));
        report.append(String.format("Pending: %d | Errors: %d\n", 
            status.getPendingFiles(), status.getErrorFiles()));
        report.append(String.format("API Migration: %.1f%% modern\n", 
            status.getApiMigrationPercentage()));
        report.append(String.format("Estimated Completion: %s\n\n", 
            status.getEstimatedCompletion()));
        
        // Progress Bar
        report.append(generateProgressBar()).append("\n\n");
        
        // Module Breakdown
        if (!status.getMigrationByModule().isEmpty()) {
            report.append("Migration by Module\n");
            report.append("------------------\n");
            status.getMigrationByModule().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> report.append(String.format("  %s: %d files\n", 
                    entry.getKey(), entry.getValue())));
            report.append("\n");
        }
        
        // API Usage Statistics
        report.append("API Usage Statistics\n");
        report.append("-------------------\n");
        report.append(String.format("Deprecated calls: %,d\n", status.getDeprecatedApiCalls()));
        report.append(String.format("Modern calls: %,d\n", status.getModernApiCalls()));
        report.append(String.format("Migration ratio: %.2f:1\n\n", 
            status.getModernApiCalls() > 0 ? 
                (double) status.getDeprecatedApiCalls() / status.getModernApiCalls() : 0));
        
        // Recent Errors
        if (!status.getCriticalErrors().isEmpty()) {
            report.append("Recent Errors\n");
            report.append("-------------\n");
            status.getCriticalErrors().stream()
                .limit(5)
                .forEach(error -> report.append("  • ").append(error).append("\n"));
            if (status.getCriticalErrors().size() > 5) {
                report.append("  ... and ")
                      .append(status.getCriticalErrors().size() - 5)
                      .append(" more\n");
            }
            report.append("\n");
        }
        
        // File Status Summary
        Map<FileState, Long> stateCounts = fileStatusMap.values().stream()
            .collect(java.util.stream.Collectors.groupingBy(
                FileStatus::getState, 
                java.util.stream.Collectors.counting()
            ));
        
        if (!stateCounts.isEmpty()) {
            report.append("File Status Summary\n");
            report.append("------------------\n");
            stateCounts.forEach((state, count) -> 
                report.append(String.format("  %s: %d files\n", state.getDescription(), count)));
            report.append("\n");
        }
        
        // Timing Information
        report.append("Timing Information\n");
        report.append("-----------------\n");
        report.append("Started: ").append(status.getStartTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        report.append("Last Update: ").append(status.getLastUpdateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        
        long duration = java.time.Duration.between(
            status.getStartTime(), LocalDateTime.now()).toMinutes();
        report.append(String.format("Duration: %d hours %d minutes\n", 
            duration / 60, duration % 60));
        
        if (status.getMigratedFiles() > 0) {
            double avgTime = (double) duration / status.getMigratedFiles();
            report.append(String.format("Average time per file: %.1f minutes\n", avgTime));
        }
        
        return report.toString();
    }
    
    /**
     * Generates a visual progress bar.
     * 
     * @return ASCII progress bar
     */
    private String generateProgressBar() {
        int barLength = 50;
        int filled = (int) (status.getProgressPercentage() / 100 * barLength);
        
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("=");
            } else if (i == filled) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ");
        bar.append(String.format("%.1f%%", status.getProgressPercentage()));
        
        return bar.toString();
    }
    
    /**
     * Extracts module name from file path.
     * 
     * @param filePath the file path
     * @return module name
     */
    private String extractModule(String filePath) {
        // Simple heuristic: use first meaningful directory after src/
        if (filePath.contains("/src/")) {
            String afterSrc = filePath.substring(filePath.indexOf("/src/") + 5);
            String[] parts = afterSrc.split("/");
            if (parts.length > 2) {
                return parts[1]; // Usually main/java/<package>
            }
        }
        return "unknown";
    }
    
    /**
     * Saves current status to disk.
     */
    private void saveStatus() {
        try {
            Files.createDirectories(STATUS_FILE.getParent());
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(STATUS_FILE.toFile(), status);
        } catch (IOException e) {
            log.error("Failed to save migration status: {}", e.getMessage());
        }
    }
    
    /**
     * Loads status from disk.
     */
    private void loadStatus() {
        if (Files.exists(STATUS_FILE)) {
            try {
                MigrationStatus loaded = objectMapper.readValue(
                    STATUS_FILE.toFile(), MigrationStatus.class
                );
                
                // Copy loaded values to current status
                status.setStartTime(loaded.getStartTime());
                status.setLastUpdateTime(loaded.getLastUpdateTime());
                status.setTotalFiles(loaded.getTotalFiles());
                status.setMigratedFiles(loaded.getMigratedFiles());
                status.setPendingFiles(loaded.getPendingFiles());
                status.setErrorFiles(loaded.getErrorFiles());
                status.setDeprecatedApiCalls(loaded.getDeprecatedApiCalls());
                status.setModernApiCalls(loaded.getModernApiCalls());
                status.setMigrationByModule(loaded.getMigrationByModule());
                status.setCriticalErrors(loaded.getCriticalErrors());
                status.setCurrentPhase(loaded.getCurrentPhase());
                
                log.info("Loaded migration status from disk");
            } catch (IOException e) {
                log.warn("Could not load previous migration status: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Clears the migration status and starts fresh.
     */
    public void reset() {
        status.setStartTime(LocalDateTime.now());
        status.setLastUpdateTime(LocalDateTime.now());
        status.setTotalFiles(0);
        status.setMigratedFiles(0);
        status.setPendingFiles(0);
        status.setErrorFiles(0);
        status.setDeprecatedApiCalls(0);
        status.setModernApiCalls(0);
        status.getMigrationByModule().clear();
        status.getCriticalErrors().clear();
        status.setCurrentPhase(MigrationPhase.ANALYSIS);
        fileStatusMap.clear();
        
        saveStatus();
        log.info("Migration status reset");
    }
    
    /**
     * Scheduled task to periodically log status.
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void logStatus() {
        if (status.getCurrentPhase() != MigrationPhase.COMPLETE && 
            status.getTotalFiles() > 0) {
            
            log.info("Migration Progress: {}/{} files ({}%), Phase: {}, Est. completion: {}",
                status.getMigratedFiles(),
                status.getTotalFiles(),
                String.format("%.1f", status.getProgressPercentage()),
                status.getCurrentPhase(),
                status.getEstimatedCompletion()
            );
            
            // Save current state
            saveStatus();
        }
    }
    
    /**
     * Checks if migration is complete.
     * 
     * @return true if migration is complete
     */
    public boolean isMigrationComplete() {
        return status.getCurrentPhase() == MigrationPhase.COMPLETE ||
               (status.getTotalFiles() > 0 && 
                status.getMigratedFiles() + status.getErrorFiles() >= status.getTotalFiles());
    }
    
    /**
     * Gets current migration status.
     * 
     * @return current status
     */
    public MigrationStatus getStatus() {
        return status;
    }
    
    /**
     * Gets status for a specific file.
     * 
     * @param filePath the file path
     * @return file status or null if not tracked
     */
    public FileStatus getFileStatus(String filePath) {
        return fileStatusMap.get(filePath);
    }
}