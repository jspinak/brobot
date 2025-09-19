package io.github.jspinak.brobot.startup;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.capture.ScreenDimensions;

import lombok.extern.slf4j.Slf4j;

/**
 * Initializes ScreenDimensions with defaults early in the Spring Boot startup process.
 *
 * <p>Following the Brobot 1.0.7 pattern, this simply sets default dimensions without trying to
 * detect the actual display. The real dimensions will be determined when the capture provider
 * actually captures the screen.
 *
 * <p>This class is registered via META-INF/spring.factories to run during application context
 * initialization.
 */
@Slf4j
@Component
public class EarlyScreenDimensionsInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        String captureProviderName = env.getProperty("brobot.capture.provider", "JAVACV_FFMPEG");

        initializeScreenDimensions(captureProviderName);
    }

    /**
     * Initialize screen dimensions with sensible defaults. No environment detection - actual
     * dimensions determined at capture time.
     */
    public static void initializeScreenDimensions(String captureProviderName) {
        log.info("=== Screen Dimensions Initialization ===");

        String provider = captureProviderName.toUpperCase();
        int screenWidth = 1920; // Default width
        int screenHeight = 1080; // Default height

        // For mock provider, use explicit defaults
        if (provider.contains("MOCK")) {
            log.info("Using MOCK resolution: {}x{}", screenWidth, screenHeight);
        } else {
            // For all real providers, just use defaults
            // The actual dimensions will be determined when capture happens
            log.info(
                    "Using default resolution: {}x{} for provider: {}",
                    screenWidth,
                    screenHeight,
                    provider);
            log.info("Actual resolution will be determined at capture time");
        }

        // Initialize the static ScreenDimensions with defaults
        ScreenDimensions.initialize(captureProviderName, screenWidth, screenHeight);

        log.info("Screen Dimensions initialized with defaults: {}x{}", screenWidth, screenHeight);
        log.info("=========================================");
    }
}
