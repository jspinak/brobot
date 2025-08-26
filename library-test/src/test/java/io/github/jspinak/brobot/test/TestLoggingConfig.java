package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig.NormalModeConfig;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig.VerboseModeConfig;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration to provide mock logging beans for Spring tests.
 */
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
        verboseConfig.setShowActionOptions(false);
        config.setVerbose(verboseConfig);
        
        return config;
    }
    
    // BrobotLogger is now provided by MockBrobotLoggerConfig with a real implementation
    // instead of a mock to avoid NullPointerException issues
    
    @Bean
    @Primary
    public GuiAccessMonitor testGuiAccessMonitor() {
        GuiAccessMonitor monitor = Mockito.mock(GuiAccessMonitor.class);
        GuiAccessConfig config = new GuiAccessConfig();
        config.setContinueOnError(true);
        config.setCheckOnStartup(false);
        Mockito.when(monitor.getConfig()).thenReturn(config);
        Mockito.when(monitor.checkGuiAccess()).thenReturn(false);
        return monitor;
    }
}