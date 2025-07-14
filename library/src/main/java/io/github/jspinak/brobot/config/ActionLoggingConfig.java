package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.tools.logging.ActionLogger;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionConfig;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionReporter;
import io.github.jspinak.brobot.tools.logging.enhanced.EnhancedActionLogger;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
import io.github.jspinak.brobot.tools.logging.visual.HighlightManager;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import io.github.jspinak.brobot.action.Action;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring configuration for action logging with console output and visual feedback.
 * 
 * <p>This configuration sets up the enhanced action logging system that provides:</p>
 * <ul>
 *   <li>Console output for action execution</li>
 *   <li>Visual highlighting of finds and search regions</li>
 *   <li>GUI access problem detection</li>
 *   <li>Integration with the unified logging system</li>
 * </ul>
 * 
 * <p>The configuration can be customized via properties files. See
 * brobot-visual-feedback.properties for all available settings.</p>
 */
@Configuration
@EnableConfigurationProperties({
    ConsoleActionConfig.class,
    VisualFeedbackConfig.class,
    VisualFeedbackConfig.FindHighlightConfig.class,
    VisualFeedbackConfig.SearchRegionHighlightConfig.class,
    VisualFeedbackConfig.ErrorHighlightConfig.class,
    VisualFeedbackConfig.ClickHighlightConfig.class,
    GuiAccessConfig.class,
    LoggingVerbosityConfig.class,
    LoggingVerbosityConfig.NormalModeConfig.class,
    LoggingVerbosityConfig.VerboseModeConfig.class
})
@PropertySource(value = "classpath:brobot-visual-feedback.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:brobot-logging-defaults.properties", ignoreResourceNotFound = true)
public class ActionLoggingConfig {
    
    /**
     * Creates the console action reporter.
     * Only created if console actions are enabled.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "brobot.console.actions",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public ConsoleActionReporter consoleActionReporter(
        BrobotLogger brobotLogger,
        ConsoleActionConfig config
    ) {
        return new ConsoleActionReporter(brobotLogger, config);
    }
    
    /**
     * Creates the highlight manager for visual feedback.
     * Only created if highlighting is enabled.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "brobot.highlight",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public HighlightManager highlightManager(
        VisualFeedbackConfig config,
        BrobotLogger brobotLogger,
        Action action
    ) {
        return new HighlightManager(config, brobotLogger, action);
    }
    
    /**
     * Creates the GUI access monitor.
     * Always created as it provides important diagnostics.
     */
    @Bean
    public GuiAccessMonitor guiAccessMonitor(
        BrobotLogger brobotLogger,
        GuiAccessConfig config
    ) {
        return new GuiAccessMonitor(brobotLogger, config);
    }
    
    // Removed the enhanced action logger configuration as it was causing bean conflicts
    // The base ActionLoggerImpl is sufficient for now
    
    /**
     * Creates a startup bean that performs initial GUI access check if configured.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "brobot.gui-access",
        name = "check-on-startup",
        havingValue = "true",
        matchIfMissing = true
    )
    public GuiAccessStartupChecker guiAccessStartupChecker(GuiAccessMonitor monitor) {
        return new GuiAccessStartupChecker(monitor);
    }
    
    /**
     * Inner class for startup GUI check.
     */
    public static class GuiAccessStartupChecker {
        public GuiAccessStartupChecker(GuiAccessMonitor monitor) {
            // Perform GUI access check on startup
            boolean accessible = monitor.checkGuiAccess();
            if (!accessible && !monitor.getConfig().isContinueOnError()) {
                throw new IllegalStateException(
                    "GUI is not accessible. See console output for details. " +
                    "Set brobot.gui-access.continue-on-error=true to ignore."
                );
            }
        }
    }
}