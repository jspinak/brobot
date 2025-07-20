package io.github.jspinak.brobot.runner.ui.config.services;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import io.github.jspinak.brobot.runner.ui.config.models.ConfigData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling configuration file operations.
 * Manages reading, writing, and backup of configuration files.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigFileService {
    
    private final ConfigJsonService jsonService;
    private static final String BACKUP_DIR = ".brobot-config-backups";
    private static final DateTimeFormatter BACKUP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Loads configuration data from a file.
     * Returns a ConfigData object with the parsed content.
     */
    public ConfigData loadConfigurationData(Path configPath) throws IOException {
        log.info("Loading configuration from: {}", configPath);
        
        if (!Files.exists(configPath)) {
            throw new IOException("Configuration file not found: " + configPath);
        }
        
        try {
            JsonNode rootNode = jsonService.readJsonFile(configPath);
            Map<String, String> flatData = jsonService.flattenToMap(rootNode);
            
            ConfigData configData = new ConfigData();
            configData.setConfigPath(configPath);
            configData.setRawData(flatData);
            configData.setLastModified(Files.getLastModifiedTime(configPath).toInstant());
            configData.setJsonNode(rootNode);
            
            log.debug("Loaded {} configuration entries from {}", flatData.size(), configPath);
            return configData;
            
        } catch (IOException e) {
            log.error("Failed to load configuration from {}", configPath, e);
            throw new IOException("Failed to load configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Saves configuration data to the project configuration file.
     * Creates a backup before overwriting if the file exists.
     */
    public void saveProjectConfigurationData(ConfigEntry config, Map<String, String> updates) throws IOException {
        Path configPath = config.getProjectConfigPath();
        log.info("Saving configuration to: {}", configPath);
        saveConfigurationData(configPath, updates);
    }
    
    /**
     * Saves configuration data to the DSL configuration file.
     * Creates a backup before overwriting if the file exists.
     */
    public void saveDslConfigurationData(ConfigEntry config, Map<String, String> updates) throws IOException {
        Path configPath = config.getDslConfigPath();
        log.info("Saving DSL configuration to: {}", configPath);
        saveConfigurationData(configPath, updates);
    }
    
    /**
     * Internal method to save configuration data to a file.
     * Creates a backup before overwriting if the file exists.
     */
    private void saveConfigurationData(Path configPath, Map<String, String> updates) throws IOException {
        log.debug("Saving {} updates to: {}", updates.size(), configPath);
        
        // Create backup if file exists
        if (Files.exists(configPath)) {
            createBackup(configPath);
        }
        
        try {
            // Load existing content or create new
            JsonNode rootNode = Files.exists(configPath) 
                ? jsonService.readJsonFile(configPath)
                : jsonService.updateValue(null, "", "{}");
            
            // Apply updates
            for (Map.Entry<String, String> entry : updates.entrySet()) {
                rootNode = jsonService.updateValue(rootNode, entry.getKey(), entry.getValue());
            }
            
            // Write back to file
            jsonService.writeJsonFile(configPath, rootNode);
            log.info("Successfully saved configuration with {} updates", updates.size());
            
        } catch (IOException e) {
            log.error("Failed to save configuration to {}", configPath, e);
            // Try to restore from backup
            restoreLatestBackup(configPath);
            throw new IOException("Failed to save configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a backup of the configuration file.
     */
    private void createBackup(Path configPath) {
        try {
            Path backupDir = configPath.getParent().resolve(BACKUP_DIR);
            Files.createDirectories(backupDir);
            
            String timestamp = LocalDateTime.now().format(BACKUP_FORMATTER);
            String fileName = configPath.getFileName().toString();
            Path backupPath = backupDir.resolve(fileName + "." + timestamp);
            
            Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Created backup: {}", backupPath);
            
            // Clean old backups (keep only last 10)
            cleanOldBackups(backupDir, fileName, 10);
            
        } catch (IOException e) {
            log.warn("Failed to create backup for {}", configPath, e);
        }
    }
    
    /**
     * Restores the latest backup of a configuration file.
     */
    private void restoreLatestBackup(Path configPath) {
        try {
            Path backupDir = configPath.getParent().resolve(BACKUP_DIR);
            String fileName = configPath.getFileName().toString();
            
            Files.list(backupDir)
                .filter(path -> path.getFileName().toString().startsWith(fileName + "."))
                .sorted((a, b) -> b.getFileName().compareTo(a.getFileName()))
                .findFirst()
                .ifPresent(backup -> {
                    try {
                        Files.copy(backup, configPath, StandardCopyOption.REPLACE_EXISTING);
                        log.info("Restored configuration from backup: {}", backup);
                    } catch (IOException e) {
                        log.error("Failed to restore backup", e);
                    }
                });
                
        } catch (IOException e) {
            log.error("Failed to access backup directory", e);
        }
    }
    
    /**
     * Cleans old backup files, keeping only the specified number of most recent backups.
     */
    private void cleanOldBackups(Path backupDir, String filePrefix, int keepCount) {
        try {
            Files.list(backupDir)
                .filter(path -> path.getFileName().toString().startsWith(filePrefix + "."))
                .sorted((a, b) -> b.getFileName().compareTo(a.getFileName()))
                .skip(keepCount)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("Deleted old backup: {}", path);
                    } catch (IOException e) {
                        log.warn("Failed to delete old backup: {}", path, e);
                    }
                });
        } catch (IOException e) {
            log.warn("Failed to clean old backups", e);
        }
    }
    
    /**
     * Validates that a configuration file exists and is readable.
     */
    public boolean isValidConfigFile(Path configPath) {
        return Files.exists(configPath) && 
               Files.isReadable(configPath) && 
               configPath.toString().endsWith(".json");
    }
    
    /**
     * Gets the size of a configuration file in bytes.
     */
    public long getFileSize(Path configPath) throws IOException {
        return Files.size(configPath);
    }
    
    /**
     * Creates a new empty configuration file with default structure.
     */
    public void createNewConfigFile(Path configPath, Map<String, String> defaults) throws IOException {
        log.info("Creating new configuration file: {}", configPath);
        
        // Ensure parent directory exists
        Files.createDirectories(configPath.getParent());
        
        // Create empty JSON object
        JsonNode rootNode = jsonService.updateValue(null, "", "{}");
        
        // Apply defaults
        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            rootNode = jsonService.updateValue(rootNode, entry.getKey(), entry.getValue());
        }
        
        // Write to file
        jsonService.writeJsonFile(configPath, rootNode);
        log.info("Created new configuration file with {} defaults", defaults.size());
    }
    
    /**
     * Watches a configuration file for changes.
     * Returns a WatchKey that can be used to monitor changes.
     */
    public WatchKey watchConfigFile(Path configPath, WatchService watchService) throws IOException {
        Path parent = configPath.getParent();
        return parent.register(watchService, 
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE);
    }
    
    /**
     * Exports configuration to a different format (e.g., properties file).
     */
    public void exportAsProperties(Path configPath, Path outputPath) throws IOException {
        ConfigData data = loadConfigurationData(configPath);
        
        StringBuilder properties = new StringBuilder();
        properties.append("# Exported from: ").append(configPath).append("\n");
        properties.append("# Date: ").append(LocalDateTime.now()).append("\n\n");
        
        data.getRawData().forEach((key, value) -> {
            properties.append(key).append("=").append(value).append("\n");
        });
        
        Files.writeString(outputPath, properties.toString());
        log.info("Exported configuration to properties file: {}", outputPath);
    }
}