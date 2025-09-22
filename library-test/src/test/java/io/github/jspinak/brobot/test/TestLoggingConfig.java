package io.github.jspinak.brobot.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.NormalModeConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.VerboseModeConfig;

// GuiAccessConfig removed
// GuiAccessMonitor removed

/** Test configuration to provide mock logging beans for Spring tests. */
@TestConfiguration
public class TestLoggingConfig {

    @Bean
    @Primary
    public LoggingVerbosityConfig loggingVerbosityConfig() {
        LoggingVerbosityConfig config = new LoggingVerbosityConfig();
        config.setVerbosity(LoggingVerbosityConfig.VerbosityLevel.QUIET);

        // Configure normal mode
        NormalModeConfig normalConfig = new NormalModeConfig();
        normalConfig.setShowTiming(false);
        normalConfig.setShowMatchCoordinates(false);
        normalConfig.setShowMatchCount(false);
        config.setNormal(normalConfig);

        // Configure verbose mode
        VerboseModeConfig verboseConfig = new VerboseModeConfig();
        verboseConfig.setShowSearchRegions(false);
        verboseConfig.setShowMatchScores(false);
        verboseConfig.setShowActionConfig(false);
        config.setVerbose(verboseConfig);

        return config;
    }

    // BrobotLogger is now provided by MockBrobotLoggerConfig with a real implementation
    // instead of a mock to avoid NullPointerException issues

    // GuiAccessMonitor has been removed from the library
    // This bean is no longer needed
}
