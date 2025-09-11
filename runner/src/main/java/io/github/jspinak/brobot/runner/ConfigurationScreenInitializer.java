package io.github.jspinak.brobot.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.ConfigurationComponentFactory;
import io.github.jspinak.brobot.runner.ui.navigation.ScreenRegistry;

import lombok.Data;

/** Initializes and registers configuration screens with the navigation system. */
@Component
@Data
public class ConfigurationScreenInitializer {
    private static final Logger logger =
            LoggerFactory.getLogger(ConfigurationScreenInitializer.class);

    private final ScreenRegistry screenRegistry;
    private final ConfigurationComponentFactory componentFactory;

    @Autowired
    public ConfigurationScreenInitializer(
            ScreenRegistry screenRegistry, ConfigurationComponentFactory componentFactory) {
        this.screenRegistry = screenRegistry;
        this.componentFactory = componentFactory;
    }

    /** Registers configuration screens when the application is ready. */
    // @EventListener(ApplicationReadyEvent.class) // Disabled to prevent duplicate registration
    public void registerConfigurationScreens() {
        logger.info("Registering configuration screens");

        // Register the enhanced configuration screen
        screenRegistry.registerScreenFactory(
                "enhancedConfiguration",
                "Configuration Management",
                context -> componentFactory.createConfigManagementPanel());

        logger.info("Configuration screens registered");
    }
}
