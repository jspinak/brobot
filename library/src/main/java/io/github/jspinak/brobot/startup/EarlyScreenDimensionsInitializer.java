package io.github.jspinak.brobot.startup;

import java.awt.*;
import java.awt.HeadlessException;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.capture.ScreenDimensions;

import lombok.extern.slf4j.Slf4j;

/**
 * Initializes ScreenDimensions very early in the Spring Boot startup process. This runs before
 * beans are created, ensuring that Region creation uses the correct dimensions based on the capture
 * provider.
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
     * Initialize screen dimensions based on the capture provider. This method can also be called
     * directly for non-Spring applications.
     */
    public static void initializeScreenDimensions(String captureProviderName) {
        log.info("=== Early Screen Dimensions Initialization ===");

        String provider = captureProviderName.toUpperCase();
        int screenWidth;
        int screenHeight;

        // Check if we're using mock provider first
        if (provider.contains("MOCK")) {
            // Use default dimensions for mock mode
            screenWidth = 1920;
            screenHeight = 1080;
            log.info(
                    "Using MOCK resolution: {}x{} for provider: {}",
                    screenWidth,
                    screenHeight,
                    provider);
        } else if (provider.contains("JAVACV_FFMPEG")
                || provider.contains("FFMPEG")
                || provider.contains("ROBOT")) {
            // These providers capture at physical resolution
            // FFmpeg works independently of GraphicsEnvironment, so it can work even in "headless"
            // environments
            try {
                screenWidth = detectPhysicalResolution();
                screenHeight = detectPhysicalHeight();
                log.info(
                        "Using PHYSICAL resolution: {}x{} for provider: {}",
                        screenWidth,
                        screenHeight,
                        provider);
            } catch (Exception e) {
                // If detection fails, use default dimensions
                screenWidth = 1920;
                screenHeight = 1080;
                log.info(
                        "Using default resolution: {}x{} for provider: {} (detection failed: {})",
                        screenWidth,
                        screenHeight,
                        provider,
                        e.getMessage());
            }
        } else {
            // SikuliX and AUTO capture at logical resolution
            // Get logical dimensions from AWT
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension screenSize = toolkit.getScreenSize();
                screenWidth = screenSize.width;
                screenHeight = screenSize.height;
                log.info("Using LOGICAL resolution for provider: {}", provider);
            } catch (HeadlessException e) {
                // Fallback to default dimensions if headless
                screenWidth = 1920;
                screenHeight = 1080;
                log.warn(
                        "HeadlessException caught, using default resolution: {}x{}",
                        screenWidth,
                        screenHeight);
            }
        }

        // Initialize the static ScreenDimensions
        ScreenDimensions.initialize(captureProviderName, screenWidth, screenHeight);

        log.info(
                "Screen Dimensions initialized: {}x{} for provider: {}",
                screenWidth,
                screenHeight,
                captureProviderName);
        log.info("============================================");
    }

    private static int detectPhysicalResolution() {
        // For FFmpeg, we try to detect physical resolution even if GraphicsEnvironment reports
        // headless
        // because FFmpeg can capture screens independently of Java's AWT/Swing
        try {
            // First try to get screen size without triggering GraphicsEnvironment
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();

            // Check for known DPI scaling patterns
            if (screenSize.width == 1536 && screenSize.height == 864) {
                // 125% DPI scaling detected - return physical resolution
                log.debug("Detected 125% DPI scaling, returning physical width: 1920");
                return 1920;
            }

            // Try to get DPI scale from graphics configuration if available
            if (!GraphicsEnvironment.isHeadless()) {
                try {
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice gd = ge.getDefaultScreenDevice();
                    GraphicsConfiguration gc = gd.getDefaultConfiguration();
                    double scaleX = gc.getDefaultTransform().getScaleX();
                    if (scaleX > 1.0) {
                        int physicalWidth = (int) Math.round(screenSize.width * scaleX);
                        log.debug(
                                "Detected DPI scale {}x, returning physical width: {}",
                                scaleX,
                                physicalWidth);
                        return physicalWidth;
                    }
                } catch (Exception e) {
                    log.debug("Could not detect DPI scale: {}", e.getMessage());
                }
            }

            // Default to detected size or 1920
            int width = screenSize.width > 0 ? screenSize.width : 1920;
            log.debug("Using detected width: {}", width);
            return width;
        } catch (HeadlessException e) {
            // Even in headless mode, FFmpeg can work, so return default resolution
            log.debug(
                    "Toolkit reports headless, using default width: 1920 (FFmpeg can still"
                            + " capture)");
            return 1920;
        }
    }

    private static int detectPhysicalHeight() {
        // For FFmpeg, we try to detect physical resolution even if GraphicsEnvironment reports
        // headless
        // because FFmpeg can capture screens independently of Java's AWT/Swing
        try {
            // First try to get screen size without triggering GraphicsEnvironment
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();

            // Check for known DPI scaling patterns
            if (screenSize.width == 1536 && screenSize.height == 864) {
                // 125% DPI scaling detected - return physical resolution
                log.debug("Detected 125% DPI scaling, returning physical height: 1080");
                return 1080;
            }

            // Try to get DPI scale from graphics configuration if available
            if (!GraphicsEnvironment.isHeadless()) {
                try {
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice gd = ge.getDefaultScreenDevice();
                    GraphicsConfiguration gc = gd.getDefaultConfiguration();
                    double scaleY = gc.getDefaultTransform().getScaleY();
                    if (scaleY > 1.0) {
                        int physicalHeight = (int) Math.round(screenSize.height * scaleY);
                        log.debug(
                                "Detected DPI scale {}x, returning physical height: {}",
                                scaleY,
                                physicalHeight);
                        return physicalHeight;
                    }
                } catch (Exception e) {
                    log.debug("Could not detect DPI scale: {}", e.getMessage());
                }
            }

            // Default to detected size or 1080
            int height = screenSize.height > 0 ? screenSize.height : 1080;
            log.debug("Using detected height: {}", height);
            return height;
        } catch (HeadlessException e) {
            // Even in headless mode, FFmpeg can work, so return default resolution
            log.debug(
                    "Toolkit reports headless, using default height: 1080 (FFmpeg can still"
                            + " capture)");
            return 1080;
        }
    }
}
