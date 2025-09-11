package io.github.jspinak.brobot.startup;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;

import org.bytedeco.javacpp.Loader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Initializes OpenCV/JavaCV native libraries at application startup.
 *
 * <p>This class ensures that native OpenCV libraries are properly loaded before any OpenCV
 * operations are performed. It runs very early in the application lifecycle to prevent
 * UnsatisfiedLinkError exceptions.
 *
 * <p>The initialization happens in multiple places to ensure libraries are loaded regardless of how
 * the application starts:
 *
 * <ul>
 *   <li>Static initializer block - runs when class is first loaded
 *   <li>ApplicationContextInitializer - runs during Spring Boot startup
 *   <li>@PostConstruct - runs after bean creation as a fallback
 * </ul>
 *
 * @since 1.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpenCVNativeLibraryInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Static initializer to load libraries as early as possible. This runs when the class is first
     * loaded by the JVM.
     */
    static {
        // Suppress debug output BEFORE loading libraries
        System.setProperty("org.bytedeco.javacpp.logger.debug", "false");
        System.setProperty("org.bytedeco.javacv.debug", "false");
        System.setProperty("org.bytedeco.javacpp.logger", "slf4j");

        // Now load the libraries
        initializeOpenCV();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        // Check if we should skip native library loading (e.g., in mock mode)
        boolean mockMode = env.getProperty("brobot.framework.mock", Boolean.class, false);

        if (!mockMode) {
            log.debug("Initializing OpenCV native libraries via ApplicationContextInitializer");
            initializeOpenCV();
        } else {
            log.debug("Skipping OpenCV native library initialization in mock mode");
        }
    }

    /**
     * Fallback initialization via Spring @PostConstruct. This ensures libraries are loaded even if
     * the ApplicationContextInitializer wasn't registered properly.
     */
    @PostConstruct
    public void initializePostConstruct() {
        log.debug("Checking OpenCV initialization via @PostConstruct");
        initializeOpenCV();
    }

    /** Initializes OpenCV native libraries. This method is idempotent and thread-safe. */
    private static void initializeOpenCV() {
        if (initialized.compareAndSet(false, true)) {
            try {
                // Ensure debug is disabled before loading
                try {
                    // Use reflection to set the debug flag in the Loader class
                    Class<?> loaderClass = Loader.class;
                    java.lang.reflect.Field debugField = loaderClass.getDeclaredField("debug");
                    debugField.setAccessible(true);
                    debugField.setBoolean(null, false);
                } catch (Exception e) {
                    // Silently ignore if we can't set the field
                }

                log.info("Loading OpenCV native libraries...");

                // Load core OpenCV modules used by Brobot
                // The order matters - core must be loaded first
                Loader.load(org.bytedeco.opencv.global.opencv_core.class);
                Loader.load(org.bytedeco.opencv.global.opencv_imgproc.class);
                Loader.load(org.bytedeco.opencv.global.opencv_imgcodecs.class);
                Loader.load(org.bytedeco.opencv.global.opencv_highgui.class);

                // Load additional modules if present
                try {
                    Loader.load(org.bytedeco.opencv.global.opencv_features2d.class);
                } catch (Throwable t) {
                    log.debug("opencv_features2d not loaded (optional): {}", t.getMessage());
                }

                // Load JavaCV utilities - handle version differences
                try {
                    Loader.load(org.bytedeco.javacv.Java2DFrameUtils.class);
                } catch (Throwable t) {
                    log.debug(
                            "Java2DFrameUtils not loaded (may not be needed for JavaCV 1.5.3): {}",
                            t.getMessage());
                }

                try {
                    Loader.load(org.bytedeco.javacv.OpenCVFrameConverter.class);
                } catch (Throwable t) {
                    log.debug("OpenCVFrameConverter not loaded (optional): {}", t.getMessage());
                }

                log.info("OpenCV native libraries loaded successfully");

                // Log version information for debugging
                try {
                    String version = org.bytedeco.opencv.global.opencv_core.CV_VERSION;
                    log.debug("OpenCV version: {}", version);
                } catch (Throwable t) {
                    // Version might not be available in all builds
                    log.debug("OpenCV version information not available");
                }

            } catch (Throwable e) {
                log.error("Failed to load OpenCV native libraries", e);
                // Don't throw here - let the actual usage fail with a more specific error
                // This allows mock mode and tests to continue even if native libs aren't available
            }
        } else {
            log.debug("OpenCV native libraries already initialized");
        }
    }

    /**
     * Checks if OpenCV native libraries have been initialized.
     *
     * @return true if libraries have been loaded, false otherwise
     */
    public static boolean isInitialized() {
        return initialized.get();
    }

    /** Forces re-initialization of OpenCV libraries. This is mainly for testing purposes. */
    public static void forceReinitialize() {
        initialized.set(false);
        initializeOpenCV();
    }
}
