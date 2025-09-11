package io.github.jspinak.brobot.runner.project.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.ProjectDefinition;
import io.github.jspinak.brobot.runner.project.TaskButton;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing the lifecycle of automation projects. Handles project creation,
 * opening, closing, and state transitions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectLifecycleService implements DiagnosticCapable {

    private final EventBus eventBus;
    private final ProjectPersistenceService persistenceService;
    private final ProjectValidationService validationService;

    private final AtomicReference<ProjectDefinition> activeProject = new AtomicReference<>();
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    /** Creates a new automation project. */
    public ProjectDefinition createProject(String name, Path projectPath) {
        log.info("Creating new project: {} at {}", name, projectPath);

        try {
            // Create project directory structure
            Files.createDirectories(projectPath);
            Files.createDirectories(projectPath.resolve("config"));
            Files.createDirectories(projectPath.resolve("images"));
            Files.createDirectories(projectPath.resolve("data"));

            // Create project object
            ProjectDefinition project =
                    ProjectDefinition.builder()
                            .id(UUID.randomUUID().toString())
                            .name(name)
                            .projectPath(projectPath)
                            .configPath(projectPath.resolve("config"))
                            .imagePath(projectPath.resolve("images"))
                            .dataPath(projectPath.resolve("data"))
                            .createdAt(LocalDateTime.now())
                            .lastModified(LocalDateTime.now())
                            .state(ProjectDefinition.ProjectState.NEW)
                            .active(false)
                            .build();

            // Save initial project
            persistenceService.saveProject(project);

            // Add creation event
            project.addEvent("PROJECT_CREATED", "Project created: " + name);

            eventBus.publish(LogEvent.info(this, "Project created: " + name, "Project"));

            return project;

        } catch (Exception e) {
            log.error("Failed to create project: {}", name, e);
            eventBus.publish(
                    LogEvent.error(
                            this, "Failed to create project: " + e.getMessage(), "Project", e));
            throw new RuntimeException("Failed to create project", e);
        }
    }

    /** Opens an existing project. */
    public ProjectDefinition openProject(Path projectPath) {
        log.info("Opening project at: {}", projectPath);

        // Close current project if any
        closeCurrentProject();

        try {
            // Load project
            ProjectDefinition project =
                    persistenceService
                            .loadProject(projectPath)
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Project not found at: " + projectPath));

            // Validate project
            var validationResult = validationService.validateProject(project);
            if (!validationResult.isValid()) {
                log.warn(
                        "Project validation failed with {} issues",
                        validationResult.getIssues().size());
            }

            // Update project state
            project.setState(ProjectDefinition.ProjectState.READY);
            project.setActive(true);
            project.touch();

            // Set as active project
            activeProject.set(project);

            // Add open event
            project.addEvent("PROJECT_OPENED", "Project opened");

            eventBus.publish(
                    LogEvent.info(this, "Project opened: " + project.getName(), "Project"));

            return project;

        } catch (Exception e) {
            log.error("Failed to open project at: {}", projectPath, e);
            eventBus.publish(
                    LogEvent.error(
                            this, "Failed to open project: " + e.getMessage(), "Project", e));
            throw new RuntimeException("Failed to open project", e);
        }
    }

    /** Closes the current project. */
    public void closeCurrentProject() {
        ProjectDefinition current = activeProject.get();
        if (current != null) {
            log.info("Closing project: {}", current.getName());

            // Save project state
            current.setActive(false);
            current.setState(ProjectDefinition.ProjectState.CONFIGURED);
            current.addEvent("PROJECT_CLOSED", "Project closed");

            // Save to disk
            persistenceService.saveProject(current);

            // Clear active project
            activeProject.set(null);

            eventBus.publish(
                    LogEvent.info(this, "Project closed: " + current.getName(), "Project"));
        }
    }

    /** Gets the currently active project. */
    public ProjectDefinition getActiveProject() {
        return activeProject.get();
    }

    /** Checks if a project is currently active. */
    public boolean isProjectActive() {
        return activeProject.get() != null;
    }

    /** Deletes a project. */
    public boolean deleteProject(Path projectPath) {
        log.info("Deleting project at: {}", projectPath);

        try {
            // Close if it's the active project
            ProjectDefinition current = activeProject.get();
            if (current != null && current.getProjectPath().equals(projectPath)) {
                closeCurrentProject();
            }

            // Delete project files
            return persistenceService.deleteProject(projectPath);

        } catch (Exception e) {
            log.error("Failed to delete project at: {}", projectPath, e);
            return false;
        }
    }

    /** Archives a project. */
    public boolean archiveProject(ProjectDefinition project) {
        log.info("Archiving project: {}", project.getName());

        try {
            project.setState(ProjectDefinition.ProjectState.ARCHIVED);
            project.addEvent("PROJECT_ARCHIVED", "Project archived");
            persistenceService.saveProject(project);

            // Close if it's the active project
            if (project.equals(activeProject.get())) {
                activeProject.set(null);
            }

            return true;

        } catch (Exception e) {
            log.error("Failed to archive project: {}", project.getName(), e);
            return false;
        }
    }

    /** Imports a project from an external source. */
    public ProjectDefinition importProject(Path sourcePath, Path destinationPath) {
        log.info("Importing project from {} to {}", sourcePath, destinationPath);

        try {
            // Load source project
            ProjectDefinition sourceProject =
                    persistenceService
                            .loadProject(sourcePath)
                            .orElseThrow(
                                    () -> new IllegalArgumentException("Source project not found"));

            // Create new project with imported data
            ProjectDefinition importedProject =
                    ProjectDefinition.builder()
                            .id(UUID.randomUUID().toString())
                            .name(sourceProject.getName() + " (Imported)")
                            .description(sourceProject.getDescription())
                            .version(sourceProject.getVersion())
                            .author(sourceProject.getAuthor())
                            .projectPath(destinationPath)
                            .configPath(destinationPath.resolve("config"))
                            .imagePath(destinationPath.resolve("images"))
                            .dataPath(destinationPath.resolve("data"))
                            .configuration(sourceProject.getConfiguration())
                            .runnerConfig(sourceProject.getRunnerConfig())
                            .createdAt(LocalDateTime.now())
                            .lastModified(LocalDateTime.now())
                            .state(ProjectDefinition.ProjectState.NEW)
                            .build();

            // Save imported project
            persistenceService.saveProjectAs(importedProject, destinationPath);

            importedProject.addEvent("PROJECT_IMPORTED", "Project imported from: " + sourcePath);

            return importedProject;

        } catch (Exception e) {
            log.error("Failed to import project from: {}", sourcePath, e);
            throw new RuntimeException("Failed to import project", e);
        }
    }

    /** Exports a project to an external location. */
    public void exportProject(ProjectDefinition project, Path destinationPath) {
        log.info("Exporting project {} to {}", project.getName(), destinationPath);

        try {
            persistenceService.saveProjectAs(project, destinationPath);
            project.addEvent("PROJECT_EXPORTED", "Project exported to: " + destinationPath);

        } catch (Exception e) {
            log.error("Failed to export project: {}", project.getName(), e);
            throw new RuntimeException("Failed to export project", e);
        }
    }

    /** Updates project metadata. */
    public void updateProjectMetadata(String key, Object value) {
        ProjectDefinition project = activeProject.get();
        if (project != null) {
            if (project.getConfiguration() == null) {
                project.setConfiguration(new java.util.HashMap<>());
            }
            project.getConfiguration().put(key, value);
            project.touch();
            project.addEvent("METADATA_UPDATED", "Updated " + key);
        }
    }

    /** Adds a task button to the current project. */
    public void addTaskButton(TaskButton button) {
        ProjectDefinition project = activeProject.get();
        if (project != null && project.getRunnerConfig() != null) {
            if (project.getRunnerConfig().getButtons() == null) {
                project.getRunnerConfig().setButtons(new java.util.ArrayList<>());
            }
            project.getRunnerConfig().getButtons().add(button);
            project.touch();
            project.addEvent("BUTTON_ADDED", "Added button: " + button.getLabel());
        }
    }

    /** Removes a task button from the current project. */
    public void removeTaskButton(String buttonId) {
        ProjectDefinition project = activeProject.get();
        if (project != null
                && project.getRunnerConfig() != null
                && project.getRunnerConfig().getButtons() != null) {
            project.getRunnerConfig().getButtons().removeIf(b -> b.getId().equals(buttonId));
            project.touch();
            project.addEvent("BUTTON_REMOVED", "Removed button: " + buttonId);
        }
    }

    /** Updates a task button in the current project. */
    public void updateTaskButton(TaskButton button) {
        ProjectDefinition project = activeProject.get();
        if (project != null
                && project.getRunnerConfig() != null
                && project.getRunnerConfig().getButtons() != null) {
            project.getRunnerConfig().getButtons().removeIf(b -> b.getId().equals(button.getId()));
            project.getRunnerConfig().getButtons().add(button);
            project.touch();
            project.addEvent("BUTTON_UPDATED", "Updated button: " + button.getLabel());
        }
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> diagnosticStates = new HashMap<>();
        diagnosticStates.put("status", isProjectActive() ? "Active" : "Idle");

        ProjectDefinition current = activeProject.get();
        if (current != null) {
            diagnosticStates.put("activeProject", current.getName());
            diagnosticStates.put("projectId", current.getId());
            diagnosticStates.put("projectState", current.getState().toString());
            diagnosticStates.put("lastModified", current.getLastModified().toString());
        }

        return DiagnosticInfo.builder()
                .component("ProjectLifecycleService")
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
        log.debug("Diagnostic mode {}", enabled ? "enabled" : "disabled");
    }
}
