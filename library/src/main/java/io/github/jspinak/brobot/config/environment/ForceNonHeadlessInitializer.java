package io.github.jspinak.brobot.config.environment;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Forces non-headless mode by intercepting GraphicsEnvironment initialization. This class MUST be
 * loaded before any AWT/Swing classes.
 */
public class ForceNonHeadlessInitializer {

    private static boolean initialized = false;
    private static boolean forcedNonHeadless = false;

    static {
        forceNonHeadlessMode();
    }

    private static void forceNonHeadlessMode() {
        try {
            // 1. Set the property first
            String currentHeadless = System.getProperty("java.awt.headless");
            System.out.println(
                    "[ForceNonHeadlessInitializer] Current java.awt.headless = " + currentHeadless);

            // Always force to false, regardless of current value
            System.setProperty("java.awt.headless", "false");
            System.out.println("[ForceNonHeadlessInitializer] Set java.awt.headless = false");

            // 2. Try to override GraphicsEnvironment if it's already initialized
            try {
                Class<?> geClass = Class.forName("java.awt.GraphicsEnvironment");

                // Check if it's already initialized as headless
                Method isHeadlessMethod = geClass.getMethod("isHeadless");
                boolean currentlyHeadless = (Boolean) isHeadlessMethod.invoke(null);

                if (currentlyHeadless) {
                    System.out.println(
                            "[ForceNonHeadlessInitializer] GraphicsEnvironment is headless,"
                                    + " attempting to override...");

                    // Try to access and modify the internal headless field
                    Field[] fields = geClass.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getName().contains("headless")
                                || field.getName().contains("Headless")) {
                            field.setAccessible(true);
                            if (field.getType() == boolean.class
                                    || field.getType() == Boolean.class) {
                                field.set(null, false);
                                System.out.println(
                                        "[ForceNonHeadlessInitializer] Reset field: "
                                                + field.getName()
                                                + " to false");
                            }
                        }
                    }

                    // Try to force re-initialization
                    try {
                        Method getLocalGE =
                                geClass.getDeclaredMethod("getLocalGraphicsEnvironment");
                        getLocalGE.setAccessible(true);
                        getLocalGE.invoke(null);
                    } catch (Exception e) {
                        // Expected if method doesn't exist
                    }

                    forcedNonHeadless = true;
                } else {
                    System.out.println(
                            "[ForceNonHeadlessInitializer] GraphicsEnvironment is already"
                                    + " non-headless");
                }

            } catch (ClassNotFoundException e) {
                System.out.println(
                        "[ForceNonHeadlessInitializer] GraphicsEnvironment not yet loaded - good"
                                + " timing!");
            }

            // 3. Set additional properties that might help
            System.setProperty("java.awt.headless", "false");
            System.setProperty("awt.toolkit", "sun.awt.windows.WToolkit"); // Force Windows toolkit

            // 4. Verify the setting took effect
            boolean isHeadless = GraphicsEnvironment.isHeadless();
            System.out.println(
                    "[ForceNonHeadlessInitializer] Final GraphicsEnvironment.isHeadless() = "
                            + isHeadless);

            if (!isHeadless) {
                System.out.println(
                        "[ForceNonHeadlessInitializer] ✓ Successfully configured non-headless"
                                + " mode");
            } else {
                System.err.println(
                        "[ForceNonHeadlessInitializer] ✗ Failed to configure non-headless mode");
                System.err.println(
                        "[ForceNonHeadlessInitializer] Add -Djava.awt.headless=false to JVM"
                                + " arguments or gradle.properties");
            }

            initialized = true;

        } catch (Exception e) {
            System.err.println(
                    "[ForceNonHeadlessInitializer] Error forcing non-headless mode: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Call this method as early as possible in your application. It will ensure the static
     * initializer runs.
     */
    public static void init() {
        if (!initialized) {
            forceNonHeadlessMode();
        }
    }

    /** Check if we successfully forced non-headless mode. */
    public static boolean wasForcedNonHeadless() {
        return forcedNonHeadless;
    }

    /** Get diagnostic information about the headless state. */
    public static String getDiagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Headless Diagnostics:\n");
        sb.append("  java.awt.headless property: ")
                .append(System.getProperty("java.awt.headless"))
                .append("\n");
        sb.append("  GraphicsEnvironment.isHeadless(): ");
        try {
            sb.append(GraphicsEnvironment.isHeadless());
        } catch (Exception e) {
            sb.append("Error: ").append(e.getMessage());
        }
        sb.append("\n");
        sb.append("  Forced non-headless: ").append(forcedNonHeadless).append("\n");
        sb.append("  OS: ").append(System.getProperty("os.name")).append("\n");
        return sb.toString();
    }
}
