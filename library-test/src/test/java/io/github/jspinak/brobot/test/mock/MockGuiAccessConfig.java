package io.github.jspinak.brobot.test.mock;

import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for GUI access in headless test environment.
 * Overrides the default GUI access configuration to allow tests to run
 * without a display.
 */
@TestConfiguration
public class MockGuiAccessConfig {
    
    @Bean
    @Primary
    public GuiAccessConfig testGuiAccessConfig() {
        GuiAccessConfig config = new GuiAccessConfig();
        config.setContinueOnError(true);
        config.setCheckOnStartup(false);
        config.setReportProblems(false);
        config.setVerboseErrors(false);
        config.setSuggestSolutions(false);
        config.setLogSuccessfulChecks(false);
        config.setWarnRemoteDesktop(false);
        config.setCheckMacPermissions(false);
        return config;
    }
}