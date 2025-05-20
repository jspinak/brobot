package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.config.ConfigManagementPanel;
import io.github.jspinak.brobot.services.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Main configuration panel for the Brobot Runner.
 * This panel integrates all configuration-related functionality, including
 * selection, import, browser, and metadata editor components.
 */
@Component
public class EnhancedConfigurationPanel extends ConfigManagementPanel {

    /**
     * Creates a new EnhancedConfigurationPanel.
     */
    @Autowired
    public EnhancedConfigurationPanel(
            EventBus eventBus,
            BrobotRunnerProperties properties,
            BrobotLibraryInitializer libraryInitializer,
            ApplicationConfig appConfig,
            ProjectManager projectManager,
            AllStatesInProjectService allStatesService) {

        super(eventBus, properties, libraryInitializer, appConfig, projectManager, allStatesService);
    }
}