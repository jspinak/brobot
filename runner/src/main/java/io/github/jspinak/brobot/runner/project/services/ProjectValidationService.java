package io.github.jspinak.brobot.runner.project.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.project.ProjectDefinition;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.project.ValidationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for validating automation projects. Ensures project structure, configuration,
 * and data integrity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectValidationService implements DiagnosticCapable {

    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    private final AtomicLong validationCount = new AtomicLong(0);

    /** Validates complete project structure and configuration. */
    public ValidationResult validateProject(ProjectDefinition project) {
        validationCount.incrementAndGet();

        ValidationResult result = ValidationResult.builder().valid(true).build();

        // Validate basic project info
        validateBasicInfo(project, result);

        // Validate project paths
        validateProjectPaths(project, result);

        // Validate automation definition
        validateAutomationDefinition(project, result);

        // Validate task buttons
        validateTaskButtons(project, result);

        return result;
    }

    /** Validates project structure on disk. */
    public boolean validateProjectStructure(Path projectPath) {
        if (!Files.exists(projectPath)) {
            log.warn("Project path does not exist: {}", projectPath);
            return false;
        }

        if (!Files.isDirectory(projectPath)) {
            log.warn("Project path is not a directory: {}", projectPath);
            return false;
        }

        // Check required subdirectories
        Path configPath = projectPath.resolve("config");
        Path imagesPath = projectPath.resolve("images");
        Path dataPath = projectPath.resolve("data");

        boolean valid =
                Files.exists(configPath)
                        && Files.isDirectory(configPath)
                        && Files.exists(imagesPath)
                        && Files.isDirectory(imagesPath)
                        && Files.exists(dataPath)
                        && Files.isDirectory(dataPath);

        if (!valid) {
            log.warn("Project structure incomplete at: {}", projectPath);
        }

        return valid;
    }

    /** Checks if a project exists at the given path. */
    public boolean projectExists(Path projectPath) {
        if (!Files.exists(projectPath)) {
            return false;
        }

        // Check for project file
        Path projectFile = projectPath.resolve("project.json");
        return Files.exists(projectFile);
    }

    /** Validates basic project information. */
    private void validateBasicInfo(ProjectDefinition project, ValidationResult result) {
        if (project.getId() == null || project.getId().isBlank()) {
            result.addError("Basic Info", "Project ID is required");
        }

        if (project.getName() == null || project.getName().isBlank()) {
            result.addError("Basic Info", "Project name is required");
        }

        if (project.getProjectPath() == null) {
            result.addError("Basic Info", "Project path is required");
        }

        if (project.getState() == null) {
            result.addError("Basic Info", "Project state is required");
        }

        // Validate version format if present
        if (project.getVersion() != null && !project.getVersion().isBlank()) {
            if (!isValidVersion(project.getVersion())) {
                result.addWarning("Basic Info", "Invalid version format: " + project.getVersion());
            }
        }
    }

    /** Validates project paths. */
    private void validateProjectPaths(ProjectDefinition project, ValidationResult result) {
        if (project.getProjectPath() != null) {
            if (!Files.exists(project.getProjectPath())) {
                result.addError(
                        "Paths", "Project path does not exist: " + project.getProjectPath());
            }
        }

        if (project.getConfigPath() != null) {
            if (!Files.exists(project.getConfigPath())) {
                result.addWarning(
                        "Paths", "Config path does not exist: " + project.getConfigPath());
            }
        }

        if (project.getImagePath() != null) {
            if (!Files.exists(project.getImagePath())) {
                result.addWarning("Paths", "Image path does not exist: " + project.getImagePath());
            }
        }

        if (project.getDataPath() != null) {
            if (!Files.exists(project.getDataPath())) {
                result.addWarning("Paths", "Data path does not exist: " + project.getDataPath());
            }
        }
    }

    /** Validates automation definition. */
    private void validateAutomationDefinition(ProjectDefinition project, ValidationResult result) {
        if (project.getRunnerConfig() == null) {
            result.addWarning("Automation", "No automation definition found");
            return;
        }

        var automation = project.getRunnerConfig();

        if (automation.getMainClass() == null || automation.getMainClass().isBlank()) {
            result.addError("Automation", "Main class is required for automation");
        } else {
            // Validate class name format
            if (!isValidClassName(automation.getMainClass())) {
                result.addError(
                        "Automation", "Invalid main class name: " + automation.getMainClass());
            }
        }

        // Check for at least one button or task
        if ((automation.getButtons() == null || automation.getButtons().isEmpty())) {
            result.addWarning("Automation", "No task buttons defined");
        }
    }

    /** Validates task buttons. */
    private void validateTaskButtons(ProjectDefinition project, ValidationResult result) {
        if (project.getRunnerConfig() == null || project.getRunnerConfig().getButtons() == null) {
            return;
        }

        var buttons = project.getRunnerConfig().getButtons();

        for (int i = 0; i < buttons.size(); i++) {
            TaskButton button = buttons.get(i);
            String prefix = "Button[" + i + "]";

            if (button.getId() == null || button.getId().isBlank()) {
                result.addError(prefix, "Button ID is required");
            }

            if (button.getLabel() == null || button.getLabel().isBlank()) {
                result.addError(prefix, "Button label is required");
            }

            if (button.getFunctionName() == null || button.getFunctionName().isBlank()) {
                result.addError(prefix, "Function name is required");
            } else if (!isValidMethodName(button.getFunctionName())) {
                result.addError(prefix, "Invalid function name: " + button.getFunctionName());
            }

            // Validate position
            if (button.getPosition() != null) {
                if (button.getPosition().getRow() != null && button.getPosition().getRow() < 0) {
                    result.addError(
                            prefix, "Invalid row position: " + button.getPosition().getRow());
                }

                if (button.getPosition().getColumn() != null
                        && button.getPosition().getColumn() < 0) {
                    result.addError(
                            prefix, "Invalid column position: " + button.getPosition().getColumn());
                }
            }

            // Check for duplicate IDs
            for (int j = i + 1; j < buttons.size(); j++) {
                if (button.getId().equals(buttons.get(j).getId())) {
                    result.addError(prefix, "Duplicate button ID: " + button.getId());
                }
            }
        }
    }

    /** Validates version format (semantic versioning). */
    private boolean isValidVersion(String version) {
        return version.matches("^\\d+\\.\\d+\\.\\d+(-.*)?$");
    }

    /** Validates Java class name format. */
    private boolean isValidClassName(String className) {
        return className.matches("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");
    }

    /** Validates Java method name format. */
    private boolean isValidMethodName(String methodName) {
        return methodName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> diagnosticStates = new HashMap<>();
        diagnosticStates.put("status", "Active");
        diagnosticStates.put("totalValidations", validationCount.get());

        return DiagnosticInfo.builder()
                .component("ProjectValidationService")
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
