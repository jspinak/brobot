package io.github.jspinak.brobot.config.environment;

import java.awt.*;
import java.lang.reflect.Field;

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
            // 1. Set the property first - MUST be done before any AWT class is loaded
            String currentHeadless = System.getProperty("java.awt.headless");
            System.out.println(
                    "[ForceNonHeadlessInitializer] Current java.awt.headless = " + currentHeadless);

            // Always force to false, regardless of current value
            System.setProperty("java.awt.headless", "false");
            System.out.println("[ForceNonHeadlessInitializer] Set java.awt.headless = false");

            // 2. Check if GraphicsEnvironment is already initialized
            boolean alreadyHeadless = false;
            try {
                // This will load GraphicsEnvironment if not already loaded
                alreadyHeadless = GraphicsEnvironment.isHeadless();

                if (alreadyHeadless) {
                    System.out.println(
                            "[ForceNonHeadlessInitializer] WARNING: GraphicsEnvironment is already"
                                    + " initialized as headless");
                    System.out.println(
                            "[ForceNonHeadlessInitializer] Due to Java module restrictions, we"
                                    + " cannot override this at runtime");
                    System.out.println(
                            "[ForceNonHeadlessInitializer] SOLUTION: You MUST add"
                                    + " -Djava.awt.headless=false to JVM arguments");

                    // Try reflection with explicit module opening (won't work without --add-opens)
                    attemptReflectionOverride();

                } else {
                    System.out.println(
                            "[ForceNonHeadlessInitializer] GraphicsEnvironment is already"
                                    + " non-headless - good!");
                }

            } catch (HeadlessException e) {
                System.err.println(
                        "[ForceNonHeadlessInitializer] HeadlessException when checking"
                                + " GraphicsEnvironment");
                alreadyHeadless = true;
            }

            // 3. Set additional properties that might help future initializations
            System.setProperty("java.awt.headless", "false");

            // Detect OS and set appropriate toolkit
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("windows")) {
                System.setProperty("awt.toolkit", "sun.awt.windows.WToolkit");
            } else if (os.contains("mac")) {
                System.setProperty("awt.toolkit", "sun.lwawt.macosx.LWCToolkit");
            } else {
                // Linux/Unix
                System.setProperty("awt.toolkit", "sun.awt.X11.XToolkit");
            }

            // 4. Final verification
            if (alreadyHeadless) {
                System.err.println(
                        "[ForceNonHeadlessInitializer]"
                                + " =============================================");
                System.err.println(
                        "[ForceNonHeadlessInitializer] CRITICAL: GraphicsEnvironment is HEADLESS");
                System.err.println("[ForceNonHeadlessInitializer] ");
                System.err.println(
                        "[ForceNonHeadlessInitializer] To fix this, use ONE of these solutions:");
                System.err.println("[ForceNonHeadlessInitializer] ");
                System.err.println("[ForceNonHeadlessInitializer] 1. Add JVM argument:");
                System.err.println("[ForceNonHeadlessInitializer]    -Djava.awt.headless=false");
                System.err.println("[ForceNonHeadlessInitializer] ");
                System.err.println(
                        "[ForceNonHeadlessInitializer] 2. With module system (Java 9+), also add:");
                System.err.println(
                        "[ForceNonHeadlessInitializer]    --add-opens"
                                + " java.desktop/java.awt=ALL-UNNAMED");
                System.err.println("[ForceNonHeadlessInitializer] ");
                System.err.println("[ForceNonHeadlessInitializer] 3. In gradle.properties, add:");
                System.err.println(
                        "[ForceNonHeadlessInitializer]   "
                                + " org.gradle.jvmargs=-Djava.awt.headless=false");
                System.err.println(
                        "[ForceNonHeadlessInitializer]"
                                + " =============================================");
            } else {
                System.out.println(
                        "[ForceNonHeadlessInitializer] ✓ GraphicsEnvironment is non-headless");
            }

            initialized = true;

        } catch (Exception e) {
            System.err.println("[ForceNonHeadlessInitializer] Unexpected error: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Attempts to override headless via reflection. This will only work if --add-opens
     * java.desktop/java.awt=ALL-UNNAMED is set.
     */
    private static void attemptReflectionOverride() {
        try {
            Class<?> geClass = GraphicsEnvironment.class;

            // Try to find and modify the headless field
            Field[] fields = geClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("headless")) {
                    try {
                        field.setAccessible(true);
                        field.set(null, Boolean.FALSE);
                        System.out.println(
                                "[ForceNonHeadlessInitializer] Successfully overrode headless field"
                                        + " via reflection");
                        forcedNonHeadless = true;
                        return;
                    } catch (Exception e) {
                        System.out.println(
                                "[ForceNonHeadlessInitializer] Cannot override due to module"
                                        + " restrictions: "
                                        + e.getMessage());
                        System.out.println(
                                "[ForceNonHeadlessInitializer] To enable reflection, add JVM"
                                        + " argument:");
                        System.out.println(
                                "[ForceNonHeadlessInitializer]   --add-opens"
                                        + " java.desktop/java.awt=ALL-UNNAMED");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // Silently fail - reflection not critical
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
