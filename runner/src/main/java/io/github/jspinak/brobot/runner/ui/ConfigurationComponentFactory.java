package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.ui.config.ConfigManagementPanel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for creating configuration-related UI components.
 */
@Component
@RequiredArgsConstructor
public class ConfigurationComponentFactory {

    private final EventBus eventBus;
    private final BrobotRunnerProperties properties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationConfig appConfig;
    private final AutomationProjectManager projectManager;
    private final StateService allStatesInProjectService;

    /**
     * Creates the main configuration management panel.
     *
     * @return The configuration management panel
     */
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
}