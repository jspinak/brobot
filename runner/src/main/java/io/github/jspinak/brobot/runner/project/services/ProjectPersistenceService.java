package io.github.jspinak.brobot.runner.project.services;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.ProjectDefinition;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for persisting and loading automation projects. Handles all file I/O
 * operations for project data.
 */
@Slf4j
@Service
public class ProjectPersistenceService implements DiagnosticCapable {

    private static final String PROJECT_FILE_NAME = "project.json";
    private static final String BACKUP_EXTENSION = ".backup";

    private final EventBus eventBus;
    private final ObjectMapper objectMapper;

    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    private final AtomicLong saveCount = new AtomicLong(0);
    private final AtomicLong loadCount = new AtomicLong(0);

    public ProjectPersistenceService(EventBus eventBus) {
        this.eventBus = eventBus;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /** Saves a project to its default location. */
    public void saveProject(ProjectDefinition project) {
        if (project == null || project.getProjectPath() == null) {
            log.error("Cannot save null project or project with null path");
            return;
        }

        saveProjectAs(project, project.getProjectPath());
    }

    /** Saves a project to a specific path. */
    public void saveProjectAs(ProjectDefinition project, Path projectPath) {
        saveCount.incrementAndGet();
        log.info("Saving project {} to {}", project.getName(), projectPath);

        try {
            // Ensure directory exists
            Files.createDirectories(projectPath);

            // Create backup of existing file
            Path projectFile = projectPath.resolve(PROJECT_FILE_NAME);
            if (Files.exists(projectFile)) {
                createBackup(projectFile);
            }

            // Update project path
            project.setProjectPath(projectPath);
            project.touch();

            // Write project file
            objectMapper.writeValue(projectFile.toFile(), project);

            // Copy resources if saving to a new location
            copyProjectResources(project, projectPath);

            log.info("Project saved successfully: {}", project.getName());
            eventBus.publish(
                    LogEvent.info(this, "Project saved: " + project.getName(), "Persistence"));

        } catch (IOException e) {
            log.error("Failed to save project: {}", project.getName(), e);
            eventBus.publish(
                    LogEvent.error(
                            this, "Failed to save project: " + e.getMessage(), "Persistence", e));
            throw new RuntimeException("Failed to save project", e);
        }
    }

    /** Loads a project from disk. */
    public Optional<ProjectDefinition> loadProject(Path projectPath) {
        loadCount.incrementAndGet();
        log.info("Loading project from: {}", projectPath);

        Path projectFile = projectPath.resolve(PROJECT_FILE_NAME);
        if (!Files.exists(projectFile)) {
            log.warn("Project file not found: {}", projectFile);
            return Optional.empty();
        }

        try {
            ProjectDefinition project =
                    objectMapper.readValue(projectFile.toFile(), ProjectDefinition.class);

            // Ensure paths are set correctly
            project.setProjectPath(projectPath);
            if (project.getConfigPath() == null) {
                project.setConfigPath(projectPath.resolve("config"));
            }
            if (project.getImagePath() == null) {
                project.setImagePath(projectPath.resolve("images"));
            }
            if (project.getDataPath() == null) {
                project.setDataPath(projectPath.resolve("data"));
            }

            log.info("Project loaded successfully: {}", project.getName());
            eventBus.publish(
                    LogEvent.info(this, "Project loaded: " + project.getName(), "Persistence"));

            return Optional.of(project);

        } catch (IOException e) {
            log.error("Failed to load project from: {}", projectPath, e);
            eventBus.publish(
                    LogEvent.error(
                            this, "Failed to load project: " + e.getMessage(), "Persistence", e));
            return Optional.empty();
        }
    }

    /** Deletes a project and all its files. */
    public boolean deleteProject(Path projectPath) {
        log.info("Deleting project at: {}", projectPath);

        try {
            if (!Files.exists(projectPath)) {
                return true;
            }

            // Delete directory recursively
            Files.walkFileTree(
                    projectPath,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });

            log.info("Project deleted successfully");
            eventBus.publish(LogEvent.info(this, "Project deleted: " + projectPath, "Persistence"));
            return true;

        } catch (IOException e) {
            log.error("Failed to delete project at: {}", projectPath, e);
            eventBus.publish(
                    LogEvent.error(
                            this, "Failed to delete project: " + e.getMessage(), "Persistence", e));
            return false;
        }
    }

    /** Creates a backup of a project file. */
    private void createBackup(Path projectFile) {
        try {
            Path backupFile = projectFile.resolveSibling(PROJECT_FILE_NAME + BACKUP_EXTENSION);
            Files.copy(projectFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Created backup: {}", backupFile);
        } catch (IOException e) {
            log.warn("Failed to create backup of: {}", projectFile, e);
        }
    }

    /** Copies project resources to a new location. */
    private void copyProjectResources(ProjectDefinition project, Path targetPath)
            throws IOException {
        // Create subdirectories
        Files.createDirectories(targetPath.resolve("config"));
        Files.createDirectories(targetPath.resolve("images"));
        Files.createDirectories(targetPath.resolve("data"));

        // Copy config files if source differs from target
        if (project.getConfigPath() != null
                && !project.getConfigPath().equals(targetPath.resolve("config"))) {
            copyDirectory(project.getConfigPath(), targetPath.resolve("config"));
        }

        // Copy image files if source differs from target
        if (project.getImagePath() != null
                && !project.getImagePath().equals(targetPath.resolve("images"))) {
            copyDirectory(project.getImagePath(), targetPath.resolve("images"));
        }

        // Copy data files if source differs from target
        if (project.getDataPath() != null
                && !project.getDataPath().equals(targetPath.resolve("data"))) {
            copyDirectory(project.getDataPath(), targetPath.resolve("data"));
        }
    }

    /** Copies a directory recursively. */
    private void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            return;
        }

        Files.walkFileTree(
                source,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                            throws IOException {
                        Path targetDir = target.resolve(source.relativize(dir));
                        Files.createDirectories(targetDir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Path targetFile = target.resolve(source.relativize(file));
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    /** Checks if a backup exists for a project. */
    public boolean hasBackup(Path projectPath) {
        Path backupFile = projectPath.resolve(PROJECT_FILE_NAME + BACKUP_EXTENSION);
        return Files.exists(backupFile);
    }

    /** Restores a project from backup. */
    public boolean restoreFromBackup(Path projectPath) {
        Path projectFile = projectPath.resolve(PROJECT_FILE_NAME);
        Path backupFile = projectPath.resolve(PROJECT_FILE_NAME + BACKUP_EXTENSION);

        if (!Files.exists(backupFile)) {
            log.warn("No backup found for project at: {}", projectPath);
            return false;
        }

        try {
            Files.copy(backupFile, projectFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Project restored from backup: {}", projectPath);
            return true;
        } catch (IOException e) {
            log.error("Failed to restore project from backup: {}", projectPath, e);
            return false;
        }
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> diagnosticStates = new HashMap<>();
        diagnosticStates.put("status", "Active");
        diagnosticStates.put("totalSaves", saveCount.get());
        diagnosticStates.put("totalLoads", loadCount.get());

        return DiagnosticInfo.builder()
                .component("ProjectPersistenceService")
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
