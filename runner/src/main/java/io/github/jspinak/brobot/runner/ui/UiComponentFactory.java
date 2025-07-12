package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.ui.config.ConfigManagementPanel;
import io.github.jspinak.brobot.runner.ui.config.AtlantaConfigPanel;
import io.github.jspinak.brobot.runner.ui.config.ImprovedAtlantaConfigPanel;
import io.github.jspinak.brobot.runner.ui.config.RefactoredConfigPanel;
import io.github.jspinak.brobot.runner.ui.config.RefactoredConfigDetailsPanel;
import io.github.jspinak.brobot.runner.ui.panels.AtlantaAutomationPanel;
import io.github.jspinak.brobot.runner.ui.panels.AtlantaLogsPanel;
import io.github.jspinak.brobot.runner.ui.panels.RefactoredResourceMonitorPanel;
import io.github.jspinak.brobot.runner.ui.log.RefactoredAtlantaLogsPanel;
import io.github.jspinak.brobot.runner.ui.execution.RefactoredExecutionDashboardPanel;
import io.github.jspinak.brobot.runner.ui.monitoring.PerformanceMonitorPanel;
import io.github.jspinak.brobot.runner.ui.screens.ComponentShowcaseScreen;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.ui.panels.UnifiedAutomationPanel;
import io.github.jspinak.brobot.runner.ui.registry.UIComponentRegistry;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UiComponentFactory {

    private final BrobotRunnerProperties properties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final EventBus eventBus;
    private final ApplicationContext applicationContext;
    private final AutomationProjectManager projectManager;
    private final AutomationOrchestrator automationOrchestrator;
    private final ResourceManager resourceManager;
    private final ImageResourceManager imageResourceManager;
    private final CacheManager cacheManager;
    private final SessionManager sessionManager;
    private final ThemeManager themeManager;
    private final StateService allStatesInProjectService;
    private final ApplicationConfig appConfig;
    private final UIComponentRegistry componentRegistry;
    private final HotkeyManager hotkeyManager;
    private final AutomationWindowController windowController;
    private final LogQueryService logQueryService;
    private final LabelManager labelManager;
    private final UIUpdateManager uiUpdateManager;
    private final StateTransitionStore stateTransitionStore;

    public ConfigurationPanel createConfigurationPanel() {
        return new ConfigurationPanel(properties, libraryInitializer, eventBus, allStatesInProjectService);
    }

    public ConfigManagementPanel createConfigManagementPanel() {
        // Use the new AtlantaFX styled configuration panel
        return new ConfigManagementPanel(
                eventBus,
                properties,
                libraryInitializer,
                appConfig,
                projectManager,
                allStatesInProjectService
        );
    }
    
    /**
     * Creates a modern AtlantaFX styled configuration panel.
     * This is the new preferred method for creating configuration panels.
     * 
     * @return A new AtlantaConfigPanel instance
     */
    public AtlantaConfigPanel createAtlantaConfigPanel() {
        // Use the original AtlantaConfigPanel for now
        return new AtlantaConfigPanel(
                eventBus,
                properties,
                libraryInitializer,
                appConfig
        );
    }
    
    /**
     * Creates a refactored configuration panel that follows single responsibility principle.
     * 
     * @return A new RefactoredConfigPanel instance
     */
    public RefactoredConfigPanel createRefactoredConfigPanel() {
        return new RefactoredConfigPanel(
                eventBus,
                properties,
                libraryInitializer,
                appConfig
        );
    }
    
    /**
     * Creates an improved configuration panel with better spacing.
     * 
     * @return A new ImprovedAtlantaConfigPanel instance
     */
    public ImprovedAtlantaConfigPanel createImprovedConfigPanel() {
        return new ImprovedAtlantaConfigPanel(
                eventBus,
                properties,
                libraryInitializer,
                appConfig
        );
    }

    /**
     * Creates a unified automation panel that combines features from both
     * AutomationPanel and EnhancedAutomationPanel.
     * 
     * @return A new UnifiedAutomationPanel instance
     */
    public UnifiedAutomationPanel createAutomationPanel() {
        UnifiedAutomationPanel panel = applicationContext.getBean(UnifiedAutomationPanel.class);
        
        // Register the panel in the component registry
        componentRegistry.register("automationPanel", panel);
        
        return panel;
    }
    
    /**
     * Creates a modern AtlantaFX styled automation panel.
     * 
     * @return A new AtlantaAutomationPanel instance
     */
    public AtlantaAutomationPanel createAtlantaAutomationPanel() {
        return applicationContext.getBean(AtlantaAutomationPanel.class);
    }
    
    
    /**
     * Creates a refactored ResourceMonitorPanel with LabelManager and UIUpdateManager.
     * This is the preferred method for creating resource monitor panels.
     * 
     * @return A new RefactoredResourceMonitorPanel instance
     */
    public RefactoredResourceMonitorPanel createRefactoredResourceMonitorPanel() {
        RefactoredResourceMonitorPanel panel = applicationContext.getBean(RefactoredResourceMonitorPanel.class);
        componentRegistry.register("resourceMonitorPanel", panel);
        return panel;
    }
    
    /**
     * Creates a modern AtlantaFX styled resource panel.
     * 
     * @return A new AtlantaResourcePanel instance
     */
    public AtlantaResourcePanel createAtlantaResourcePanel() {
        return new AtlantaResourcePanel(resourceManager, imageResourceManager, cacheManager, sessionManager);
    }

    /**
     * Creates a ComponentShowcaseScreen instance.
     */
    public ComponentShowcaseScreen createComponentShowcaseScreen() {
        return new ComponentShowcaseScreen(themeManager);
    }
    
    
    /**
     * Creates a refactored AtlantaFX logs panel with centralized resource management.
     * Uses the existing RefactoredAtlantaLogsPanel which has a service-oriented architecture.
     * 
     * @return A new RefactoredAtlantaLogsPanel instance
     */
    public RefactoredAtlantaLogsPanel createRefactoredAtlantaLogsPanel() {
        RefactoredAtlantaLogsPanel panel = applicationContext.getBean(RefactoredAtlantaLogsPanel.class);
        componentRegistry.register("logsPanel", panel);
        return panel;
    }
    
    /**
     * Creates a refactored ConfigDetailsPanel with centralized label and update management.
     * 
     * @return A new RefactoredConfigDetailsPanel instance
     */
    public RefactoredConfigDetailsPanel createRefactoredConfigDetailsPanel() {
        RefactoredConfigDetailsPanel panel = applicationContext.getBean(RefactoredConfigDetailsPanel.class);
        componentRegistry.register("configDetailsPanel", panel);
        return panel;
    }
    
    /**
     * Creates a refactored ExecutionDashboardPanel with improved resource management.
     * 
     * @return A new RefactoredExecutionDashboardPanel instance
     */
    public RefactoredExecutionDashboardPanel createRefactoredExecutionDashboardPanel() {
        RefactoredExecutionDashboardPanel panel = applicationContext.getBean(RefactoredExecutionDashboardPanel.class);
        componentRegistry.register("executionDashboardPanel", panel);
        return panel;
    }
    
    /**
     * Convenience method to create all refactored panels for a complete UI.
     * This ensures consistent architecture across all components.
     * 
     * @return A record containing all refactored panels
     */
    public RefactoredPanels createAllRefactoredPanels() {
        return new RefactoredPanels(
            createAutomationPanel(),  // Already uses UnifiedAutomationPanel
            createRefactoredResourceMonitorPanel(),
            createRefactoredConfigDetailsPanel(),
            createRefactoredExecutionDashboardPanel(),
            createRefactoredAtlantaLogsPanel()
        );
    }
    
    /**
     * Creates a performance monitoring panel for development and debugging.
     * This panel displays real-time metrics from LabelManager and UIUpdateManager.
     * 
     * @return A new PerformanceMonitorPanel instance
     */
    public PerformanceMonitorPanel createPerformanceMonitorPanel() {
        PerformanceMonitorPanel panel = applicationContext.getBean(PerformanceMonitorPanel.class);
        componentRegistry.register("performanceMonitorPanel", panel);
        return panel;
    }
    
    /**
     * Record to hold all refactored panels.
     */
    public record RefactoredPanels(
        UnifiedAutomationPanel automationPanel,
        RefactoredResourceMonitorPanel resourceMonitorPanel,
        RefactoredConfigDetailsPanel configDetailsPanel,
        RefactoredExecutionDashboardPanel executionDashboardPanel,
        RefactoredAtlantaLogsPanel logsPanel
    ) {}
}
