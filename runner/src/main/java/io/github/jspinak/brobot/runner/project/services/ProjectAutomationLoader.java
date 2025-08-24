package io.github.jspinak.brobot.runner.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service responsible for loading library AutomationProject instances from disk.
 * 
 * Single Responsibility: Load and parse AutomationProject files
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectAutomationLoader {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Loads an automation project from the config path.
     * 
     * @param configPath The configuration directory path
     * @param automationId The ID of the automation project to load
     * @return The loaded AutomationProject
     * @throws IOException if loading fails
     */
    public AutomationProject loadAutomation(Path configPath, String automationId) throws IOException {
        if (configPath == null || automationId == null) {
            throw new IllegalArgumentException("Config path and automation ID are required");
        }
        
        // Try common file patterns
        Path[] possiblePaths = {
            configPath.resolve(automationId + ".json"),
            configPath.resolve("automation.json"),
            configPath.resolve("states.json"),
            configPath.resolve(automationId + "/automation.json")
        };
        
        for (Path path : possiblePaths) {
            if (Files.exists(path)) {
                log.debug("Loading automation from: {}", path);
                return loadFromFile(path);
            }
        }
        
        throw new IOException("Automation file not found for ID: " + automationId);
    }
    
    /**
     * Loads an automation project from a specific file.
     */
    public AutomationProject loadFromFile(Path automationFile) throws IOException {
        if (!Files.exists(automationFile)) {
            throw new IOException("Automation file does not exist: " + automationFile);
        }
        
        try {
            AutomationProject project = objectMapper.readValue(
                automationFile.toFile(), 
                AutomationProject.class
            );
            
            log.info("Loaded automation project: {} with {} states", 
                project.getName(), 
                project.getStates() != null ? project.getStates().size() : 0);
                
            return project;
            
        } catch (IOException e) {
            log.error("Failed to parse automation file: {}", automationFile, e);
            throw new IOException("Failed to parse automation file", e);
        }
    }
    
    /**
     * Saves an automation project to disk.
     */
    public void saveAutomation(AutomationProject automation, Path configPath) throws IOException {
        if (automation == null || configPath == null) {
            throw new IllegalArgumentException("Automation and config path are required");
        }
        
        Files.createDirectories(configPath);
        
        Path automationFile = configPath.resolve(
            automation.getId() != null ? automation.getId() + ".json" : "automation.json"
        );
        
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(automationFile.toFile(), automation);
            
        log.info("Saved automation project to: {}", automationFile);
    }
    
    /**
     * Checks if an automation file exists.
     */
    public boolean automationExists(Path configPath, String automationId) {
        if (configPath == null || automationId == null) {
            return false;
        }
        
        Path automationFile = configPath.resolve(automationId + ".json");
        return Files.exists(automationFile);
    }
}