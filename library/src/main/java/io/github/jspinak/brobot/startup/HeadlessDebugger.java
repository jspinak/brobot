package io.github.jspinak.brobot.startup;

import java.awt.GraphicsEnvironment;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Debug class to track headless property changes throughout the application lifecycle. This helps
 * identify where and when the headless property is being set.
 */
@Order(Ordered.HIGHEST_PRECEDENCE - 1) // Run before everything else
public class HeadlessDebugger
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static {
        // Log headless state as early as possible
        logHeadlessState("Static Initializer");
    }

    /**
     * Initializes the headless debugger during Spring context initialization.
     * Logs headless state and attempts to access GraphicsEnvironment internal fields.
     *
     * @param applicationContext the Spring application context being initialized
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        logHeadlessState("ApplicationContextInitializer");

        // Try to detect if GraphicsEnvironment was already initialized
        try {
            Class<?> geClass = GraphicsEnvironment.class;
            java.lang.reflect.Field headlessField = geClass.getDeclaredField("headless");
            headlessField.setAccessible(true);
            Object headlessValue = headlessField.get(null);
            System.out.println(
                    "[HeadlessDebugger] GraphicsEnvironment.headless field = " + headlessValue);
        } catch (Exception e) {
            System.out.println(
                    "[HeadlessDebugger] Could not access GraphicsEnvironment.headless field: "
                            + e.getMessage());
        }
    }

    /**
     * Logs the current headless state from various sources.
     * Captures both the system property and GraphicsEnvironment state.
     *
     * @param location descriptive name of where this check is being performed
     */
    private static void logHeadlessState(String location) {
        String property = System.getProperty("java.awt.headless");
        boolean isHeadless;
        String geState;

        try {
            isHeadless = java.awt.GraphicsEnvironment.isHeadless();
            geState = String.valueOf(isHeadless);
        } catch (Error e) {
            geState = "Error: " + e.getMessage();
            isHeadless = false;
        }

        System.out.println("[HeadlessDebugger - " + location + "]");
        System.out.println("  java.awt.headless property = " + property);
        System.out.println("  GraphicsEnvironment.isHeadless() = " + geState);
        System.out.println("  Timestamp = " + System.currentTimeMillis());
        System.out.println("  Thread = " + Thread.currentThread().getName());
    }
}
