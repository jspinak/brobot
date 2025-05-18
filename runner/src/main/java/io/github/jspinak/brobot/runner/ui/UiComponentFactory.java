package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.automation.AutomationExecutor;
import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import io.github.jspinak.brobot.runner.session.SessionManager;
import io.github.jspinak.brobot.runner.ui.screens.ComponentShowcaseScreen;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import io.github.jspinak.brobot.services.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
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

    // Other dependencies as needed

    @Autowired
    public UiComponentFactory(BrobotRunnerProperties properties,
                              BrobotLibraryInitializer libraryInitializer,
                              EventBus eventBus, ApplicationContext applicationContext, ProjectManager projectManager,
                              AutomationExecutor automationExecutor, ResourceManager resourceManager,
                              ImageResourceManager imageResourceManager, CacheManager cacheManager,
                              SessionManager sessionManager, ThemeManager themeManager) {
        this.properties = properties;
        this.libraryInitializer = libraryInitializer;
        this.eventBus = eventBus;
        this.applicationContext = applicationContext;
        this.projectManager = projectManager;
        this.automationExecutor = automationExecutor;
        this.resourceManager = resourceManager;
        this.imageResourceManager = imageResourceManager;
        this.cacheManager = cacheManager;
        this.sessionManager = sessionManager;
        this.themeManager = themeManager;
    }

    public ConfigurationPanel createConfigurationPanel() {
        return new ConfigurationPanel(properties, libraryInitializer, eventBus);
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
