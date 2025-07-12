package io.github.jspinak.brobot.runner.project;

import lombok.Builder;
import lombok.Data;

/**
 * ProjectContext bridges the runner's project management with the library's automation engine.
 * 
 * This class follows the Adapter pattern to provide a unified interface for UI components
 * that need to work with both project management (file paths, persistence) and automation
 * concepts (states, transitions, buttons).
 * 
 * Single Responsibility: Adapt between ProjectDefinition and AutomationProject
 */
@Data
@Builder
public class ProjectContext {
    
    /**
     * The runner's project definition containing file paths and metadata.
     */
    private final ProjectDefinition projectDefinition;
    
    /**
     * The library's automation project containing states and transitions.
     * This may be null if the project hasn't loaded an automation yet.
     */
    private final io.github.jspinak.brobot.runner.project.AutomationProject automationProject;
    
    /**
     * Indicates if this context represents a valid, loaded project.
     */
    public boolean isValid() {
        return projectDefinition != null && projectDefinition.isRunnable();
    }
    
    /**
     * Indicates if the project has automation data loaded.
     */
    public boolean hasAutomation() {
        return automationProject != null;
    }
    
    /**
     * Gets the project name from the definition.
     */
    public String getProjectName() {
        return projectDefinition != null ? projectDefinition.getName() : "No Project";
    }
    
    /**
     * Gets the project ID from the definition.
     */
    public String getProjectId() {
        return projectDefinition != null ? projectDefinition.getId() : null;
    }
    
    /**
     * Creates an empty context for when no project is loaded.
     */
    public static ProjectContext empty() {
        return ProjectContext.builder().build();
    }
}