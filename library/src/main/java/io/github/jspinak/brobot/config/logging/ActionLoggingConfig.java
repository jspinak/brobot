package io.github.jspinak.brobot.config.logging;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

// import io.github.jspinak.brobot.tools.logging.console.ConsoleActionConfig;
// import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
// import io.github.jspinak.brobot.tools.logging.gui.GuiAccessMonitor;
// import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;

/**
 * Spring configuration for action logging with console output and visual feedback.
 *
 * <p>This configuration sets up the enhanced action logging system that provides:
 *
 * <ul>
 *   <li>Console output for action execution
 *   <li>Visual highlighting of finds and search regions
 *   <li>GUI access problem detection
 *   <li>Integration with the unified logging system
 * </ul>
 *
 * <p>The configuration can be customized via properties files. See
 * brobot-visual-feedback.properties for all available settings.
 */
@Configuration
@EnableConfigurationProperties({
    LoggingVerbosityConfig.class,
    LoggingVerbosityConfig.NormalModeConfig.class,
    LoggingVerbosityConfig.VerboseModeConfig.class
})
@PropertySource(
        value = "classpath:brobot-visual-feedback.properties",
        ignoreResourceNotFound = true)
@PropertySource(
        value = "classpath:brobot-logging-defaults.properties",
        ignoreResourceNotFound = true)
public class ActionLoggingConfig {

    // ConsoleActionReporter is already annotated with @Component, so it's automatically created by
    // Spring
    // The @ConditionalOnProperty should be moved to the ConsoleActionReporter class itself

    // to avoid circular dependency issues

    // GuiAccessMonitor is already annotated with @Component, so it's automatically created by
    // Spring

    /** Creates a startup bean that performs initial GUI access check if configured. */

    /** Inner class for startup GUI check. */
    public static class GuiAccessStartupChecker {
        public GuiAccessStartupChecker() {
            // GUI access check disabled - GuiAccessMonitor class no longer exists
            // Perform GUI access check on startup
            // boolean accessible = monitor.checkGuiAccess();
            // if (!accessible && !monitor.getConfig().isContinueOnError()) {
            //     throw new IllegalStateException(
            //             "GUI is not accessible. See console output for details. "
            //                     + "Set brobot.gui-access.continue-on-error=true to ignore.");
            // }
        }
    }
}
