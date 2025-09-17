package io.github.jspinak.brobot.config.core;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes and logs BrobotProperties configuration on startup.
 *
 * <p>This component runs after Spring context initialization to ensure all properties are loaded
 * and logs the configuration for debugging purposes.
 *
 * @since 1.1.0
 */
@Component
@ConditionalOnBean(BrobotProperties.class)
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BrobotPropertiesInitializer implements InitializingBean {

    private final BrobotProperties properties;
    private final MockModeResolver mockModeResolver;
    private final ApplicationContext applicationContext;
    private boolean initialized = false;

    /**
     * Early initialization via ApplicationStartedEvent. This ensures mock mode and other critical
     * settings are applied as early as possible.
     */
    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        if (!initialized) {
            initializeConfiguration();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!initialized) {
            initializeConfiguration();
        }
    }

    private synchronized void initializeConfiguration() {
        if (initialized) {
            return;
        }
        initialized = true;
        log.info("═══════════════════════════════════════════════════════");
        log.info("  BROBOT FRAMEWORK CONFIGURATION");
        log.info("═══════════════════════════════════════════════════════");

        // Also update ExecutionEnvironment to keep it in sync
        boolean mockMode = mockModeResolver.isMockMode();
        ExecutionEnvironment updatedEnv =
                ExecutionEnvironment.builder()
                        .mockMode(mockMode)
                        .forceHeadless(properties.getCore().isHeadless() ? true : null)
                        .allowScreenCapture(!mockMode)
                        .build();
        ExecutionEnvironment.setInstance(updatedEnv);
        log.debug("Set ExecutionEnvironment with mockMode={}", updatedEnv.isMockMode());

        // Log mock mode configuration prominently
        if (mockMode) {
            log.info("✅ MOCK MODE ENABLED");
            log.info("Mock timing configuration:");
            log.info("  - Find first: {}s", properties.getMock().getTimeFindFirst());
            log.info("  - Find all: {}s", properties.getMock().getTimeFindAll());
            log.info("  - Click: {}s", properties.getMock().getTimeClick());
            log.info("  - Move: {}s", properties.getMock().getTimeMove());
            log.info("  - Drag: {}s", properties.getMock().getTimeDrag());
        } else {
            log.info("❌ MOCK MODE DISABLED - Running in LIVE mode");
        }

        // Also set the SikuliX bundle path early for headless mode
        String imagePath = properties.getCore().getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            if (!env.shouldSkipSikuliX()) {
                log.debug("Setting SikuliX bundle path early to: {}", imagePath);
                org.sikuli.script.ImagePath.setBundlePath(imagePath);
            }
        }

        log.info("Framework settings:");
        log.info("  - Image path: {}", properties.getCore().getImagePath());
        log.info("  - Headless: {}", properties.getCore().isHeadless());
        log.info("  - Save snapshots: {}", properties.getScreenshot().isSaveSnapshots());
        log.info("  - Save history: {}", properties.getScreenshot().isSaveHistory());

        log.info("═══════════════════════════════════════════════════════");
    }
}
