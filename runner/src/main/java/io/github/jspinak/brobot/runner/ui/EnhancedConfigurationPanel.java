package io.github.jspinak.brobot.runner.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.ui.config.ConfigManagementPanel;

/**
 * Main configuration panel for the Brobot Runner. This panel integrates all configuration-related
 * functionality, including selection, import, browser, and metadata editor components.
 */
@Component
public class EnhancedConfigurationPanel extends ConfigManagementPanel {

    /** Creates a new EnhancedConfigurationPanel. */
    @Autowired
    public EnhancedConfigurationPanel(
            EventBus eventBus,
            BrobotRunnerProperties properties,
            BrobotLibraryInitializer libraryInitializer,
            ApplicationConfig appConfig,
            AutomationProjectManager projectManager,
            StateService allStatesService) {

        super(
                eventBus,
                properties,
                libraryInitializer,
                appConfig,
                projectManager,
                allStatesService);
    }
}
