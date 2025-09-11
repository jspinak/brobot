package io.github.jspinak.brobot.runner.project;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.services.*;
import io.github.jspinak.brobot.runner.resources.ResourceManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages automation projects with a service-based architecture.
 *
 * <p>This class acts as a facade that coordinates between different project services:
 *
 * <ul>
 *   <li>{@link ProjectLifecycleService} - Manages project lifecycle (create, open, close)
 *   <li>{@link ProjectValidationService} - Validates project structure and configuration
 *   <li>{@link ProjectPersistenceService} - Handles project file I/O operations
 *   <li>{@link ProjectDiscoveryService} - Discovers and indexes available projects
 *   <li>{@link ProjectMigrationService} - Handles project version migrations
 * </ul>
 *
 * <p>This refactoring follows the Single Responsibility Principle by separating concerns into
 * focused, testable services.
 *
 * @see ProjectDefinition
 * @see ProjectLifecycleService
 * @see ProjectPersistenceService
 * @since 1.0.0
 */
@Slf4j
@Component
public class AutomationProjectManager implements AutoCloseable, DiagnosticCapable {

    private final EventBus eventBus;
    private final ResourceManager resourceManager;

    // Delegated services
    private final ProjectLifecycleService lifecycleService;
    private final ProjectValidationService validationService;
    private final ProjectPersistenceService persistenceService;
    private final ProjectDiscoveryService discoveryService;
    private final ProjectMigrationService migrationService;

    // Additional services for UI support
    @Autowired(required = false)
    private ProjectContextService contextService;

    @Autowired(required = false)
    private ProjectUIService uiService;

    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    @Autowired
    public AutomationProjectManager(
            EventBus eventBus,
            ResourceManager resourceManager,
            ProjectLifecycleService lifecycleService,
            ProjectValidationService validationService,
            ProjectPersistenceService persistenceService,
            ProjectDiscoveryService discoveryService,
            ProjectMigrationService migrationService) {

        this.eventBus = eventBus;
        this.resourceManager = resourceManager;
        this.lifecycleService = lifecycleService;
        this.validationService = validationService;
        this.persistenceService = persistenceService;
        this.discoveryService = discoveryService;
        this.migrationService = migrationService;
    }

    @PostConstruct
    public void initialize() {
        // Register with resource manager
        resourceManager.registerResource(this, "AutomationProjectManager");

        // Discover available projects
        discoveryService.scanForProjects();

        log.info("AutomationProjectManager initialized");
        eventBus.publish(LogEvent.info(this, "Project manager initialized", "Project"));
    }

    /** Creates a new automation project. */
    public ProjectDefinition createProject(String name, Path projectPath) {
        log.info("Creating new project: {} at {}", name, projectPath);
        return lifecycleService.createProject(name, projectPath);
    }

    /** Opens an existing project. */
    public ProjectDefinition openProject(Path projectPath) {
        log.info("Opening project at: {}", projectPath);

        // Validate project structure
        if (!validationService.validateProjectStructure(projectPath)) {
            throw new IllegalArgumentException("Invalid project structure at: " + projectPath);
        }

        // Check if migration is needed
        if (migrationService.isMigrationNeeded(projectPath)) {
            migrationService.migrateProject(projectPath);
        }

        return lifecycleService.openProject(projectPath);
    }

    /** Closes the current project. */
    public void closeProject() {
        lifecycleService.closeCurrentProject();
    }

    /** Gets the currently active project. */
    public ProjectDefinition getActiveProject() {
        return lifecycleService.getActiveProject();
    }

    /** Checks if a project is currently active. */
    public boolean isProjectActive() {
        return lifecycleService.isProjectActive();
    }

    /** Saves the current project. */
    public void saveProject() {
        ProjectDefinition project = getActiveProject();
        if (project != null) {
            persistenceService.saveProject(project);
        }
    }

    /** Saves a project to a specific path. */
    public void saveProjectAs(ProjectDefinition project, Path path) {
        persistenceService.saveProjectAs(project, path);
    }

    /** Loads a project from disk. */
    public Optional<ProjectDefinition> loadProject(Path projectPath) {
        return persistenceService.loadProject(projectPath);
    }

    /** Gets all available projects. */
    public List<ProjectInfo> getAvailableProjects() {
        return discoveryService.getDiscoveredProjects();
    }

    /** Refreshes the list of available projects. */
    public void refreshProjects() {
        discoveryService.scanForProjects();
    }

    /** Validates a project configuration. */
    public ValidationResult validateProject(ProjectDefinition project) {
        return validationService.validateProject(project);
    }

    /** Deletes a project. */
    public boolean deleteProject(Path projectPath) {
        return lifecycleService.deleteProject(projectPath);
    }

    /** Archives a project. */
    public boolean archiveProject(ProjectDefinition project) {
        return lifecycleService.archiveProject(project);
    }

    /** Imports a project from an external source. */
    public ProjectDefinition importProject(Path sourcePath, Path destinationPath) {
        return lifecycleService.importProject(sourcePath, destinationPath);
    }

    /** Exports a project to an external location. */
    public void exportProject(ProjectDefinition project, Path destinationPath) {
        lifecycleService.exportProject(project, destinationPath);
    }

    /** Updates project metadata. */
    public void updateProjectMetadata(String key, Object value) {
        lifecycleService.updateProjectMetadata(key, value);
    }

    /** Adds a task button to the current project. */
    public void addTaskButton(TaskButton button) {
        lifecycleService.addTaskButton(button);
    }

    /** Removes a task button from the current project. */
    public void removeTaskButton(String buttonId) {
        lifecycleService.removeTaskButton(buttonId);
    }

    /** Updates a task button in the current project. */
    public void updateTaskButton(TaskButton button) {
        lifecycleService.updateTaskButton(button);
    }

    /** Gets recent projects. */
    public List<RecentProject> getRecentProjects() {
        return discoveryService.getRecentProjects();
    }

    /** Checks if a project exists at the given path. */
    public boolean projectExists(Path projectPath) {
        return validationService.projectExists(projectPath);
    }

    // ========== UI Compatibility Methods ==========
    // These methods provide backward compatibility for UI components
    // that expect to work with library's AutomationProject

    /**
     * Gets the current automation project for UI display. This adapts between ProjectDefinition and
     * AutomationProject.
     *
     * @deprecated Use getActiveProject() for project management operations
     */
    @Deprecated
    public AutomationProject getCurrentProject() {
        if (uiService != null) {
            return uiService.getCurrentAutomationProject();
        }

        // Fallback: create minimal AutomationProject from ProjectDefinition
        ProjectDefinition projectDef = getActiveProject();
        if (projectDef != null) {
            AutomationProject automationProject = new AutomationProject();
            automationProject.setName(projectDef.getName());
            // AutomationProject expects Long ID, but ProjectDefinition uses String
            try {
                automationProject.setId(Long.parseLong(projectDef.getId()));
            } catch (NumberFormatException e) {
                automationProject.setId((long) projectDef.getId().hashCode());
            }
            return automationProject;
        }

        return null;
    }

    /**
     * Alias for getActiveProject() to support UI components. Returns the library's
     * AutomationProject for UI compatibility.
     */
    public AutomationProject getActiveAutomationProject() {
        return getCurrentProject();
    }

    /**
     * Loads a project by name (UI convenience method).
     *
     * @param projectName the name of the project to load
     * @return the loaded AutomationProject or null
     */
    public AutomationProject loadProject(String projectName) {
        if (uiService != null && uiService.openProjectByName(projectName)) {
            return getCurrentProject();
        }
        return null;
    }

    /**
     * Gets available project names for UI display.
     *
     * @return list of project names
     */
    public List<String> getAvailableProjectNames() {
        if (uiService != null) {
            return uiService.getAvailableProjectNames();
        }

        return getAvailableProjects().stream()
                .map(ProjectInfo::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void close() {
        // Close any active project
        if (isProjectActive()) {
            closeProject();
        }

        log.info("AutomationProjectManager closed");
        eventBus.publish(LogEvent.info(this, "Project manager closed", "Resources"));
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> diagnosticStates = new HashMap<>();
        diagnosticStates.put("status", isProjectActive() ? "Active" : "Idle");

        // Add active project info
        ProjectDefinition activeProject = getActiveProject();
        if (activeProject != null) {
            diagnosticStates.put("activeProject", activeProject.getName());
            diagnosticStates.put("projectId", activeProject.getId());
            diagnosticStates.put("projectState", activeProject.getState().toString());
        }

        // Add service diagnostics
        diagnosticStates.put("availableProjects", getAvailableProjects().size());
        diagnosticStates.put("recentProjects", getRecentProjects().size());

        return DiagnosticInfo.builder()
                .component("AutomationProjectManager")
                .states(diagnosticStates)
                .build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);

        // Enable diagnostic mode in all services
        lifecycleService.enableDiagnosticMode(enabled);
        validationService.enableDiagnosticMode(enabled);
        persistenceService.enableDiagnosticMode(enabled);
        discoveryService.enableDiagnosticMode(enabled);
        migrationService.enableDiagnosticMode(enabled);
    }
}
