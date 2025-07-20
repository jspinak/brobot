package io.github.jspinak.brobot.runner.project;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a runner project definition with its configuration, tasks, and metadata.
 * This is the core domain model for project management in the runner module.
 * 
 * Note: This is different from the library's AutomationProject which focuses on
 * states and transitions. This class focuses on project file management and UI configuration.
 */
@Data
@Builder
@EqualsAndHashCode(of = "id")
public class ProjectDefinition {
    
    private String id;
    private String name;
    private String description;
    private String version;
    private String author;
    
    // Project paths
    private Path projectPath;
    private Path configPath;
    private Path imagePath;
    private Path dataPath;
    
    // Project metadata
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private LocalDateTime lastExecuted;
    
    // Project configuration
    private Map<String, Object> configuration;
    
    // Reference to the library's AutomationProject
    private String automationProjectId;
    
    // UI configuration
    private RunnerConfiguration runnerConfig;
    
    // Project state
    private ProjectState state;
    private boolean active;
    
    // Runtime data
    @Builder.Default
    private Map<String, Object> runtimeData = new HashMap<>();
    
    // Project history
    @Builder.Default
    private List<ProjectEvent> history = new ArrayList<>();
    
    /**
     * Runner-specific configuration.
     */
    @Data
    @Builder
    public static class RunnerConfiguration {
        private String mainClass;
        private List<TaskButton> buttons;
        private Map<String, String> settings;
        private List<String> dependencies;
        private UILayout layout;
    }
    
    /**
     * UI Layout configuration.
     */
    @Data
    @Builder
    public static class UILayout {
        private int columns;
        private int rows;
        private String theme;
        private Map<String, Object> customSettings;
    }
    
    /**
     * Project lifecycle states.
     */
    public enum ProjectState {
        NEW,
        INITIALIZED,
        CONFIGURED,
        READY,
        RUNNING,
        PAUSED,
        ERROR,
        ARCHIVED
    }
    
    /**
     * Project event for tracking history.
     */
    @Data
    @Builder
    public static class ProjectEvent {
        private LocalDateTime timestamp;
        private String type;
        private String description;
        private Map<String, Object> data;
    }
    
    /**
     * Adds an event to the project history.
     */
    public void addEvent(String type, String description) {
        if (history == null) {
            history = new ArrayList<>();
        }
        history.add(ProjectEvent.builder()
            .timestamp(LocalDateTime.now())
            .type(type)
            .description(description)
            .build());
    }
    
    /**
     * Updates the last modified timestamp.
     */
    public void touch() {
        this.lastModified = LocalDateTime.now();
    }
    
    /**
     * Checks if the project is in a runnable state.
     */
    public boolean isRunnable() {
        return state == ProjectState.READY || 
               state == ProjectState.PAUSED ||
               state == ProjectState.RUNNING;
    }
}