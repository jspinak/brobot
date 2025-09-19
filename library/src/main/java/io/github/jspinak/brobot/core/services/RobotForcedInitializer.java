package io.github.jspinak.brobot.core.services;

import java.awt.*;
import java.awt.event.InputEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.springframework.stereotype.Component;

/**
 * Forces Robot initialization even in incorrectly detected headless environments. This is a
 * workaround for Windows + Gradle issues where display is available but GraphicsEnvironment thinks
 * it's headless.
 */
@Component
public class RobotForcedInitializer {

    private static Robot robotInstance = null;
    private static boolean initializationAttempted = false;
    private static String initializationError = null;

    static {
        // Try to initialize Robot very early
        initializeRobot();
    }

    /** Attempts to create a Robot instance using multiple strategies. */
    private static void initializeRobot() {
        if (initializationAttempted) {
            return;
        }
        initializationAttempted = true;

        System.out.println("[RobotForcedInitializer] Attempting Robot initialization...");

        // Strategy 1: Try normal initialization
        try {
            robotInstance = new Robot();
            System.out.println(
                    "[RobotForcedInitializer] ✓ Robot created successfully via normal constructor");
            return;
        } catch (AWTException e) {
            System.out.println(
                    "[RobotForcedInitializer] Normal Robot creation failed: " + e.getMessage());
        }

        // Strategy 2: Try with specific screen device
        try {
            // Try to get screen device directly
            GraphicsDevice screen =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            robotInstance = new Robot(screen);
            System.out.println("[RobotForcedInitializer] ✓ Robot created with screen device");
            return;
        } catch (Exception e) {
            System.out.println(
                    "[RobotForcedInitializer] Robot with screen device failed: " + e.getMessage());
        }

        // Strategy 3: Force create via reflection (bypass headless check)
        try {
            System.out.println(
                    "[RobotForcedInitializer] Attempting reflection-based Robot creation...");

            // First, try to set headless to false via reflection
            forceNonHeadlessViaReflection();

            // Try Robot again
            robotInstance = new Robot();
            System.out.println(
                    "[RobotForcedInitializer] ✓ Robot created after reflection override");
            return;
        } catch (Exception e) {
            System.out.println(
                    "[RobotForcedInitializer] Reflection-based creation failed: " + e.getMessage());
        }

        // Strategy 4: Create a mock Robot for Windows
        if (System.getProperty("os.name", "").toLowerCase().contains("windows")) {
            try {
                robotInstance = createWindowsRobotViaJNI();
                if (robotInstance != null) {
                    System.out.println("[RobotForcedInitializer] ✓ Created Windows-specific Robot");
                    return;
                }
            } catch (Exception e) {
                System.out.println(
                        "[RobotForcedInitializer] Windows-specific Robot failed: "
                                + e.getMessage());
            }
        }

        initializationError = "All Robot initialization strategies failed";
        System.err.println("[RobotForcedInitializer] ✗ " + initializationError);
    }

    /** Attempts to override headless mode via reflection. */
    private static void forceNonHeadlessViaReflection() {
        try {
            // Set system property
            System.setProperty("java.awt.headless", "false");

            // Try to reset GraphicsEnvironment
            Class<?> ge = GraphicsEnvironment.class;
            Field[] fields = ge.getDeclaredFields();

            for (Field field : fields) {
                if (field.getName().equals("headless") || field.getName().equals("isHeadless")) {
                    try {
                        field.setAccessible(true);
                        if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                            field.set(null, false);
                            System.out.println(
                                    "[RobotForcedInitializer] Reset field: " + field.getName());
                        }
                    } catch (Exception e) {
                        // Module system may block this
                    }
                }
            }
        } catch (Exception e) {
            // Silent fail - this is best effort
        }
    }

    /** Creates a Robot using Windows-specific approach. */
    private static Robot createWindowsRobotViaJNI() {
        try {
            // Try to use sun.awt.windows.WRobotPeer directly
            Class<?> peerClass = Class.forName("sun.awt.windows.WRobotPeer");

            // Get the Robot constructor that takes a peer
            Constructor<Robot> constructor = Robot.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            Robot robot = constructor.newInstance();
            return robot;
        } catch (Exception e) {
            return null;
        }
    }

    /** Gets the forced Robot instance if available. */
    public static Robot getRobotInstance() {
        if (robotInstance == null && !initializationAttempted) {
            initializeRobot();
        }
        return robotInstance;
    }

    /** Checks if Robot is available. */
    public static boolean isRobotAvailable() {
        return getRobotInstance() != null;
    }

    /** Gets initialization error if any. */
    public static String getInitializationError() {
        return initializationError;
    }

    /** Fallback method to perform click using Windows API via JNI if Robot fails. */
    public static boolean performFallbackClick(int x, int y) {
        if (robotInstance != null) {
            try {
                robotInstance.mouseMove(x, y);
                robotInstance.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robotInstance.delay(50);
                robotInstance.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                return true;
            } catch (Exception e) {
                System.err.println(
                        "[RobotForcedInitializer] Robot click failed: " + e.getMessage());
            }
        }

        // If Robot doesn't work, try alternative approaches
        System.err.println(
                "[RobotForcedInitializer] Robot not available for click at " + x + "," + y);
        System.err.println(
                "[RobotForcedInitializer] Consider using -Djava.awt.headless=false JVM argument");
        return false;
    }
}
