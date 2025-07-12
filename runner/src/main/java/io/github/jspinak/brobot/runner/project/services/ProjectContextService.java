package io.github.jspinak.brobot.runner.project.services;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.ProjectContext;
import io.github.jspinak.brobot.runner.project.ProjectDefinition;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service responsible for managing the ProjectContext that bridges runner and library concepts.
 * 
 * Single Responsibility: Create and manage ProjectContext instances
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectContextService {
    
    private final ProjectLifecycleService lifecycleService;
    private final ProjectAutomationLoader automationLoader;
    private final EventBus eventBus;
    
    private final AtomicReference<ProjectContext> currentContext = new AtomicReference<>(ProjectContext.empty());
    
    /**
     * Creates a new project context from a project definition.
     */
    public ProjectContext createContext(ProjectDefinition projectDefinition) {
        if (projectDefinition == null) {
            return ProjectContext.empty();
        }
        
        // Try to load the automation project if referenced
        AutomationProject automationProject = null;
        if (projectDefinition.getAutomationProjectId() != null) {
            try {
                automationProject = automationLoader.loadAutomation(
                    projectDefinition.getConfigPath(), 
                    projectDefinition.getAutomationProjectId()
                );
            } catch (Exception e) {
                log.warn("Failed to load automation for project: {}", projectDefinition.getName(), e);
                eventBus.publish(LogEvent.warning(this, 
                    "Automation loading failed: " + e.getMessage(), "Project"));
            }
        }
        
        ProjectContext context = ProjectContext.builder()
            .projectDefinition(projectDefinition)
            .automationProject(automationProject)
            .build();
            
        currentContext.set(context);
        
        return context;
    }
    
    /**
     * Gets the current project context.
     */
    public ProjectContext getCurrentContext() {
        ProjectDefinition activeProject = lifecycleService.getActiveProject();
        
        // Update context if project changed
        ProjectContext current = currentContext.get();
        if (current.getProjectDefinition() != activeProject) {
            return createContext(activeProject);
        }
        
        return current;
    }
    
    /**
     * Updates the automation project in the current context.
     */
    public void updateAutomation(AutomationProject automationProject) {
        ProjectContext current = currentContext.get();
        if (current.isValid()) {
            ProjectContext updated = ProjectContext.builder()
                .projectDefinition(current.getProjectDefinition())
                .automationProject(automationProject)
                .build();
            currentContext.set(updated);
            
            // Update the project definition reference
            if (automationProject != null) {
                current.getProjectDefinition().setAutomationProjectId(String.valueOf(automationProject.getId()));
                lifecycleService.updateProjectMetadata("automationProjectId", automationProject.getId());
            }
        }
    }
    
    /**
     * Clears the current context.
     */
    public void clearContext() {
        currentContext.set(ProjectContext.empty());
    }
    
    /**
     * Checks if there's an active context with automation.
     */
    public boolean hasActiveAutomation() {
        ProjectContext context = getCurrentContext();
        return context.isValid() && context.hasAutomation();
    }
}