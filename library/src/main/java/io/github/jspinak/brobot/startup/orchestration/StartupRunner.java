package io.github.jspinak.brobot.startup.orchestration;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.startup.verification.InitialStateVerifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Automatically verifies initial states on application startup.
 *
 * <p>This runner executes after Spring context initialization and performs initial state
 * verification based on configuration. It's enabled by setting {@code
 * brobot.startup.verify-initial-states=true}.
 *
 * <p>The runner respects the configured startup delay and initial states list, making it easy to
 * ensure the application starts in a known state without writing boilerplate code.
 *
 * @since 1.1.0
 */
@Component
@ConditionalOnProperty(
        prefix = "brobot.startup",
        name = "verify-initial-states",
        havingValue = "true")
@Order(1) // Run early in the startup sequence
@RequiredArgsConstructor
@Slf4j
public class StartupRunner implements ApplicationRunner {

    private final InitialStateVerifier stateVerifier;
    private final StartupConfiguration configuration;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (configuration.getInitialStates().isEmpty()) {
            log.warn("Initial state verification is enabled but no states are configured");
            return;
        }

        // Apply startup delay if configured (skip in test mode)
        String testType = System.getProperty("brobot.test.type");
        boolean isTestMode =
                "unit".equals(testType)
                        || "true".equals(System.getProperty("brobot.test.mode"))
                        || FrameworkSettings.mock;

        if (!isTestMode && configuration.getStartupDelay() > 0) {
            log.info(
                    "Waiting {} seconds before initial state verification",
                    configuration.getStartupDelay());
            TimeUnit.SECONDS.sleep(configuration.getStartupDelay());
        } else if (isTestMode) {
            log.debug("Test mode detected - skipping startup delay");
        }

        // Perform verification
        log.info(
                "Performing initial state verification for states: {}",
                configuration.getInitialStates());

        boolean verified =
                stateVerifier
                        .builder()
                        .withStates(configuration.getInitialStates().toArray(new String[0]))
                        .withFallbackSearch(configuration.isFallbackSearch())
                        .activateFirstOnly(configuration.isActivateFirstOnly())
                        .verify();

        if (!verified) {
            log.error("Initial state verification failed - no configured states found");
            // Application continues running, but automation may need to handle this
        }
    }
}
