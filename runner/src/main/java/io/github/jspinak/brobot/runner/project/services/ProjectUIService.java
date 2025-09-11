package io.github.jspinak.brobot.runner.project.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.project.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that provides UI-specific project functionality. This service adapts between what UI
 * components expect and the underlying project structure.
 *
 * <p>Single Responsibility: Provide UI-friendly project operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectUIService {

    private final ProjectContextService contextService;
    private final AutomationProjectManager projectManager;

    /**
     * Gets the current project suitable for UI display. This method bridges between the runner's
     * ProjectDefinition and library's AutomationProject.
     */
    public AutomationProject getCurrentAutomationProject() {
        ProjectContext context = contextService.getCurrentContext();

        if (context.hasAutomation()) {
            return context.getAutomationProject();
        }

        // Create a minimal AutomationProject for UI compatibility
        if (context.isValid()) {
            AutomationProject minimal = new AutomationProject();
            minimal.setName(context.getProjectName());
            // AutomationProject expects Long ID, but ProjectDefinition uses String
            try {
                minimal.setId(Long.parseLong(context.getProjectId()));
            } catch (NumberFormatException e) {
                // If ID is not numeric, use hash code
                minimal.setId((long) context.getProjectId().hashCode());
            }

            // Create empty automation with buttons from project definition
            RunnerInterface automation = new RunnerInterface();
            automation.setButtons(getTaskButtonsFromContext(context));
            minimal.setAutomation(automation);

            return minimal;
        }

        return null;
    }

    /** Gets task buttons from the current project context. */
    public List<TaskButton> getCurrentTaskButtons() {
        ProjectContext context = contextService.getCurrentContext();

        // First try to get from automation project
        if (context.hasAutomation()
                && context.getAutomationProject().getAutomation() != null
                && context.getAutomationProject().getAutomation().getButtons() != null) {
            return context.getAutomationProject().getAutomation().getButtons();
        }

        // Fall back to project definition
        return getTaskButtonsFromContext(context);
    }

    /** Gets task buttons from project definition's runner config. */
    private List<TaskButton> getTaskButtonsFromContext(ProjectContext context) {
        if (context.isValid()
                && context.getProjectDefinition().getRunnerConfig() != null
                && context.getProjectDefinition().getRunnerConfig().getButtons() != null) {
            return context.getProjectDefinition().getRunnerConfig().getButtons();
        }

        return new ArrayList<>();
    }

    /** Updates task buttons in the current project. */
    public void updateTaskButtons(List<TaskButton> buttons) {
        ProjectContext context = contextService.getCurrentContext();

        if (!context.isValid()) {
            log.warn("Cannot update buttons - no valid project context");
            return;
        }

        // Update in project definition
        ProjectDefinition projectDef = context.getProjectDefinition();
        if (projectDef.getRunnerConfig() == null) {
            projectDef.setRunnerConfig(
                    ProjectDefinition.RunnerConfiguration.builder()
                            .buttons(new ArrayList<>())
                            .build());
        }
        projectDef.getRunnerConfig().setButtons(buttons);

        // Save the project
        projectManager.saveProject();

        // Update in automation project if available
        if (context.hasAutomation() && context.getAutomationProject().getAutomation() != null) {
            context.getAutomationProject().getAutomation().setButtons(buttons);
        }
    }

    /** Gets available projects formatted for UI display. */
    public List<String> getAvailableProjectNames() {
        return projectManager.getAvailableProjects().stream()
                .map(ProjectInfo::getName)
                .collect(Collectors.toList());
    }

    /** Opens a project by name (UI convenience method). */
    public boolean openProjectByName(String projectName) {
        List<ProjectInfo> projects = projectManager.getAvailableProjects();

        for (ProjectInfo info : projects) {
            if (info.getName().equals(projectName)) {
                try {
                    projectManager.openProject(info.getProjectPath());
                    return true;
                } catch (Exception e) {
                    log.error("Failed to open project: {}", projectName, e);
                    return false;
                }
            }
        }

        log.warn("Project not found: {}", projectName);
        return false;
    }

    /** Checks if a project is currently active (UI convenience method). */
    public boolean isProjectActive() {
        return contextService.getCurrentContext().isValid();
    }

    /** Gets the current project name for UI display. */
    public String getCurrentProjectName() {
        return contextService.getCurrentContext().getProjectName();
    }
}
