package io.github.jspinak.brobot.config.logging;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for controlling native library debug output.
 *
 * <p>This configuration suppresses debug messages from native libraries like JavaCV/FFmpeg that
 * print directly to stdout/stderr, bypassing the Java logging system.
 *
 * <p>Examples of messages that are suppressed:
 *
 * <ul>
 *   <li>"Debug: Loading library nppig64_12"
 *   <li>"Debug: Failed to load for nppig64_12: java.lang.UnsatisfiedLinkError"
 *   <li>"Debug: Collecting org.bytedeco.javacpp.Pointer$NativeDeallocator"
 * </ul>
 *
 * @since 1.0
 */
@Slf4j
@Configuration
public class NativeLibraryLoggingConfig {

    @Value("${brobot.native.logging.suppress:true}")
    private boolean suppressNativeLogging;

    @Value("${brobot.javacpp.debug:false}")
    private boolean javacppDebug;

    @Value("${brobot.javacv.debug:false}")
    private boolean javacvDebug;

    /**
     * Configures system properties to control native library debug output. This must run very early
     * in the application lifecycle.
     */
    @PostConstruct
    public void configureNativeLogging() {
        if (suppressNativeLogging) {
            log.debug("Suppressing native library debug output");

            // Suppress JavaCPP debug output
            System.setProperty("org.bytedeco.javacpp.logger.debug", String.valueOf(javacppDebug));

            // Suppress JavaCV debug output
            System.setProperty("org.bytedeco.javacv.debug", String.valueOf(javacvDebug));

            // Suppress FFmpeg debug output
            System.setProperty("org.bytedeco.ffmpeg.debug", "false");

            // Suppress native library loading messages
            System.setProperty("org.bytedeco.javacpp.loadlibraries", "false");
            System.setProperty("org.bytedeco.javacpp.preload", "false");

            // Redirect native library output to null if completely suppressing
            if (!javacppDebug && !javacvDebug) {
                // Set the Loader debug flag to false
                try {
                    Class<?> loaderClass = Class.forName("org.bytedeco.javacpp.Loader");
                    java.lang.reflect.Field debugField = loaderClass.getDeclaredField("debug");
                    debugField.setAccessible(true);
                    debugField.setBoolean(null, false);
                    log.debug("Successfully disabled JavaCPP Loader debug output");
                } catch (Exception e) {
                    log.debug("Could not disable JavaCPP Loader debug output: {}", e.getMessage());
                }
            }

            log.info("Native library debug output suppression configured");
        } else {
            log.info("Native library debug output enabled");
        }
    }

    /**
     * Static initializer to set properties as early as possible. This runs before Spring context
     * initialization.
     */
    static {
        // Set default properties to suppress debug output
        // These can be overridden by application properties
        if (System.getProperty("org.bytedeco.javacpp.logger.debug") == null) {
            System.setProperty("org.bytedeco.javacpp.logger.debug", "false");
        }
        if (System.getProperty("org.bytedeco.javacv.debug") == null) {
            System.setProperty("org.bytedeco.javacv.debug", "false");
        }
    }
}
