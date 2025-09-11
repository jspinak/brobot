package io.github.jspinak.brobot.startup;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Early initializer to suppress native library debug output.
 *
 * <p>This initializer runs very early in the Spring Boot startup process, before beans are created,
 * to ensure native library debug messages are suppressed from the beginning.
 *
 * <p>This class is registered via META-INF/spring.factories.
 *
 * @since 1.0
 */
public class NativeLibraryDebugSuppressor
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        // Check if suppression is enabled (default true)
        boolean suppress = env.getProperty("brobot.native.logging.suppress", Boolean.class, true);

        if (suppress) {
            suppressNativeDebugOutput();
        }
    }

    /** Suppresses native library debug output by setting system properties. */
    private void suppressNativeDebugOutput() {
        // Suppress JavaCPP/JavaCV debug output
        System.setProperty("org.bytedeco.javacpp.logger.debug", "false");
        System.setProperty("org.bytedeco.javacv.debug", "false");
        System.setProperty("org.bytedeco.ffmpeg.debug", "false");

        // Suppress the Loader class debug output
        try {
            // Use reflection to set the debug flag in the Loader class
            Class<?> loaderClass = Class.forName("org.bytedeco.javacpp.Loader");
            java.lang.reflect.Field debugField = loaderClass.getDeclaredField("debug");
            debugField.setAccessible(true);
            debugField.setBoolean(null, false);
        } catch (Exception e) {
            // Silently ignore if we can't set the field
        }

        // Also try to set the logger level if the class is available
        try {
            Class<?> loggerClass = Class.forName("org.bytedeco.javacpp.tools.Logger");
            java.lang.reflect.Method setDebugMethod =
                    loggerClass.getDeclaredMethod("setDebug", boolean.class);
            setDebugMethod.invoke(null, false);
        } catch (Exception e) {
            // Silently ignore if the method doesn't exist
        }
    }

    /** Static initializer to suppress debug output as early as possible. */
    static {
        // Set properties immediately when this class is loaded
        System.setProperty("org.bytedeco.javacpp.logger.debug", "false");
        System.setProperty("org.bytedeco.javacv.debug", "false");
        System.setProperty("org.bytedeco.javacpp.logger", "slf4j");
    }
}
