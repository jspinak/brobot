package io.github.jspinak.brobot.startup.state;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.startup.orchestration.StartupConfiguration;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import io.github.jspinak.brobot.statemanagement.StateMemory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Auto-configuration for initial state management in Brobot applications.
 *
 * <p>This configuration automatically handles states marked with @State(initial = true) by creating
 * the necessary startup configuration and activating initial states when the application is ready.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Automatic BrobotStartupConfiguration creation from annotations
 *   <li>ApplicationReadyEvent listener for state activation
 *   <li>Configurable delay before verification in real mode
 *   <li>Profile-specific behavior (test vs production)
 *   <li>Opt-out capability via properties
 * </ul>
 *
 * <p>Configuration properties:
 *
 * <pre>
 * brobot.startup.verify: true           # Enable verification
 * brobot.startup.auto-activate: true    # Auto-activate initial states
 * brobot.startup.initial-delay: 5       # Seconds to wait before verification
 * brobot.startup.delay: 1               # Additional delay for startup
 * brobot.startup.fallback-search: false # Search all states if initial not found
 * brobot.startup.activate-first-only: true # Only activate first found state
 * </pre>
 *
 * @since 1.2.0
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Order(1) // High priority to ensure early initialization
public class InitialStateAutoConfiguration {

    private final InitialStates initialStates;
    private final StateMemory stateMemory;

    // Configuration properties with defaults
    @Value("${brobot.startup.verify:true}")
    private boolean verifyInitialStates;

    @Value("${brobot.startup.auto-activate:true}")
    private boolean autoActivate;

    @Value("${brobot.startup.initial-delay:5}")
    private int initialDelay; // Seconds to wait before verification in real mode

    @Value("${brobot.startup.delay:1}")
    private int startupDelay; // Additional delay for startup configuration

    @Value("${brobot.startup.fallback-search:false}")
    private boolean fallbackSearch;

    @Value("${brobot.startup.activate-first-only:true}")
    private boolean activateFirstOnly;

    /**
     * Creates BrobotStartupConfiguration automatically from states marked with @State(initial =
     * true). Applications can override this by providing their own BrobotStartupConfiguration bean.
     *
     * @return Configured BrobotStartupConfiguration
     */
    @Bean
    @ConditionalOnMissingBean
    public StartupConfiguration brobotStartupConfiguration() {
        log.info(
                "Auto-configuring BrobotStartupConfiguration from @State(initial = true)"
                        + " annotations");

        StartupConfiguration config = new StartupConfiguration();

        // Enable verification if initial states are registered
        config.setVerifyInitialStates(
                verifyInitialStates && initialStates.hasRegisteredInitialStates());

        // Get states marked with @State(initial = true) from the InitialStates bean
        List<String> registeredStates = initialStates.getRegisteredInitialStates();

        if (!registeredStates.isEmpty()) {
            config.getInitialStates().addAll(registeredStates);
            log.info(
                    "Found {} initial states from annotations: {}",
                    registeredStates.size(),
                    registeredStates);
        } else {
            log.debug("No states marked with @State(initial = true) found");
            config.setVerifyInitialStates(false); // Nothing to verify
        }

        // Apply configuration settings
        config.setStartupDelay(startupDelay);
        config.setFallbackSearch(fallbackSearch);
        config.setActivateFirstOnly(activateFirstOnly);

        log.info(
                "Initial state configuration: verify={}, delay={}s, fallback={},"
                        + " activateFirstOnly={}",
                config.isVerifyInitialStates(),
                config.getStartupDelay(),
                config.isFallbackSearch(),
                config.isActivateFirstOnly());

        return config;
    }

    /**
     * Automatically activates initial states when the application is ready. This listener is
     * triggered after all beans are initialized and the application is fully started.
     *
     * <p>The activation includes a configurable delay for real mode to allow the GUI to stabilize
     * before attempting to find states on the screen.
     *
     * @param event ApplicationReadyEvent indicating the application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    @ConditionalOnProperty(
            value = "brobot.startup.auto-activate",
            havingValue = "true",
            matchIfMissing = true)
    public void autoActivateInitialStates(ApplicationReadyEvent event) {
        if (!initialStates.hasRegisteredInitialStates()) {
            log.debug("No initial states registered, skipping auto-activation");
            return;
        }

        log.info("════════════════════════════════════════════════════════");
        log.info("  AUTO-ACTIVATING INITIAL STATES");
        log.info("════════════════════════════════════════════════════════");

        try {
            // Apply initial delay in real mode to let GUI stabilize (skip in test mode)
            String testType = System.getProperty("brobot.test.type");
            boolean isTestMode =
                    "unit".equals(testType)
                            || "true".equals(System.getProperty("brobot.test.mode"));

            if (!FrameworkSettings.mock && !isTestMode && initialDelay > 0) {
                log.info(
                        "Waiting {} seconds before initial state verification (real mode)",
                        initialDelay);
                TimeUnit.SECONDS.sleep(initialDelay);
            } else if (isTestMode || FrameworkSettings.mock) {
                log.debug("Test/Mock mode detected - skipping initial delay");
            }

            // Find and activate initial states
            log.info(
                    "Searching for initial states: {}", initialStates.getRegisteredInitialStates());
            initialStates.findInitialStates();

            // Log results
            if (!stateMemory.getActiveStates().isEmpty()) {
                log.info(
                        "✅ Successfully activated initial states: {}",
                        stateMemory.getActiveStateNames());
            } else {
                log.warn("⚠️ No initial states could be activated");
                if (fallbackSearch) {
                    log.info("Fallback search is enabled, will search all states");
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Initial state activation interrupted", e);
        } catch (Exception e) {
            log.error("Error during initial state activation", e);
        }

        log.info("════════════════════════════════════════════════════════");
    }

    /** Test profile specific configuration that optimizes initial state handling for tests. */
    @Configuration
    @ConditionalOnProperty(value = "spring.profiles.active", havingValue = "test")
    @Order(0) // Higher priority than main configuration
    public static class TestInitialStateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public StartupConfiguration testBrobotStartupConfiguration(InitialStates initialStates) {
            log.info("Configuring test-optimized initial state handling");

            StartupConfiguration config = new StartupConfiguration();
            config.setVerifyInitialStates(true);
            config.getInitialStates().addAll(initialStates.getRegisteredInitialStates());
            config.setStartupDelay(0); // No delay in tests
            config.setFallbackSearch(false); // Deterministic behavior
            config.setActivateFirstOnly(true); // Activate only first state for consistency

            log.info("Test profile: immediate activation, deterministic behavior");
            return config;
        }
    }
}
