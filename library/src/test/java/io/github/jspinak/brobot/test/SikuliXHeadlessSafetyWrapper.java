package io.github.jspinak.brobot.test;

import java.lang.reflect.Field;
import java.util.List;

import org.sikuli.script.support.RunTime;

/**
 * Utility class to safely handle SikuliX in headless environments. Prevents SikuliX from attempting
 * GUI operations that cause HeadlessException.
 */
public class SikuliXHeadlessSafetyWrapper {

    private static boolean initialized = false;

    /**
     * Disables SikuliX shutdown hooks that try to access GUI components. This prevents
     * HeadlessException during test cleanup in WSL/CI environments.
     */
    public static synchronized void disableSikuliXShutdownHooks() {
        if (initialized) {
            return;
        }

        try {
            // Access the RunTime class and disable shutdown hooks
            RunTime runtime = RunTime.get();

            // Try to remove shutdown hooks via reflection
            Field hooksField = RunTime.class.getDeclaredField("shutdownHooks");
            hooksField.setAccessible(true);
            Object hooks = hooksField.get(runtime);

            if (hooks instanceof List) {
                ((List<?>) hooks).clear();
            }

            // Also try to set the flag that prevents cleanup
            try {
                Field cleanupField = RunTime.class.getDeclaredField("shouldCleanup");
                cleanupField.setAccessible(true);
                cleanupField.setBoolean(runtime, false);
            } catch (NoSuchFieldException ignored) {
                // Field might not exist in this version
            }

            initialized = true;
        } catch (Exception e) {
            // Silently ignore - if we can't disable hooks, tests will still run
            // but might show HeadlessException in shutdown
        }
    }

    /**
     * Checks if we're in a headless environment where SikuliX shutdown hooks should be disabled.
     */
    public static boolean shouldDisableShutdownHooks() {
        return EnvironmentDetector.isHeadless()
                || EnvironmentDetector.isWSL()
                || EnvironmentDetector.isCI()
                || System.getProperty("java.awt.headless", "false").equals("true");
    }

    /** Initialize safety measures for headless testing. */
    public static void initializeHeadlessSafety() {
        if (shouldDisableShutdownHooks()) {
            disableSikuliXShutdownHooks();

            // Set additional safety properties
            System.setProperty("sikuli.nocleanupatshutdown", "true");
            System.setProperty("sikuli.cleanshutdown", "false");

            // Disable mouse/keyboard operations
            System.setProperty("sikuli.nomouse", "true");
            System.setProperty("sikuli.nokeyboard", "true");
        }
    }
}
