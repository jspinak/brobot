package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.runner.automation.AutomationExecutor;
import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.ui.config.ConfigManagementPanel;
import io.github.jspinak.brobot.runner.ui.screens.ComponentShowcaseScreen;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import io.github.jspinak.brobot.services.ProjectManager;
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
    private final ProjectManager projectManager;
    private final AutomationExecutor automationExecutor;
    private final ResourceManager resourceManager;
    private final ImageResourceManager imageResourceManager;
    private final CacheManager cacheManager;
    private final SessionManager sessionManager;
    private final ThemeManager themeManager;
    private final AllStatesInProjectService allStatesInProjectService;
    private final ApplicationConfig appConfig;

    public ConfigurationPanel createConfigurationPanel() {
        return new ConfigurationPanel(properties, libraryInitializer, eventBus, allStatesInProjectService);
    }

    public ConfigManagementPanel createConfigManagementPanel() {
        return new ConfigManagementPanel(
                eventBus,
                properties,
                libraryInitializer,
                appConfig,
                projectManager,
                allStatesInProjectService
        );
    }

    public AutomationPanel createAutomationPanel() {
        return new AutomationPanel(applicationContext, projectManager, properties, automationExecutor, eventBus);
    }

    /**
     * Creates a ResourceMonitorPanel instance.
     */
    public ResourceMonitorPanel createResourceMonitorPanel() {
        return new ResourceMonitorPanel(resourceManager, imageResourceManager, cacheManager, sessionManager);
    }

    /**
     * Creates a ComponentShowcaseScreen instance.
     */
    public ComponentShowcaseScreen createComponentShowcaseScreen() {
        return new ComponentShowcaseScreen(themeManager);
    }
}
