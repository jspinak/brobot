package io.github.jspinak.brobot.diagnostics;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Comprehensive diagnostic tool for headless detection issues. Run this to understand why
 * GraphicsEnvironment is being detected as headless.
 */
public class HeadlessDiagnosticTool {

    public static void main(String[] args) {
        System.out.println("=== Headless Diagnostic Tool ===");
        System.out.println();

        // 1. Check environment detection
        checkEnvironment();

        // 2. Check Java properties
        checkJavaProperties();

        // 3. Check GraphicsEnvironment state
        checkGraphicsEnvironment();

        // 4. Check display detection
        checkDisplayDetection();

        // 5. Check WSL detection
        checkWSLEnvironment();

        // 6. Test forcing non-headless
        testForceNonHeadless();

        // 7. Check X11 availability
        checkX11Availability();

        System.out.println("\n=== Diagnostic Complete ===");
    }

    private static void checkEnvironment() {
        System.out.println("1. ENVIRONMENT DETECTION:");
        System.out.println("   OS Name: " + System.getProperty("os.name"));
        System.out.println("   OS Version: " + System.getProperty("os.version"));
        System.out.println("   Java Version: " + System.getProperty("java.version"));
        System.out.println("   Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("   User Home: " + System.getProperty("user.home"));
        System.out.println();
    }

    private static void checkJavaProperties() {
        System.out.println("2. JAVA PROPERTIES:");

        // Check all headless-related properties
        String[] properties = {
            "java.awt.headless",
            "java.awt.graphicsenv",
            "awt.toolkit",
            "sun.java2d.noddraw",
            "sun.awt.noerasebackground"
        };

        for (String prop : properties) {
            String value = System.getProperty(prop);
            System.out.println("   " + prop + " = " + (value != null ? value : "<not set>"));
        }

        // Check display environment variable
        Map<String, String> env = System.getenv();
        System.out.println("   DISPLAY env = " + env.getOrDefault("DISPLAY", "<not set>"));
        System.out.println(
                "   WAYLAND_DISPLAY env = " + env.getOrDefault("WAYLAND_DISPLAY", "<not set>"));
        System.out.println();
    }

    private static void checkGraphicsEnvironment() {
        System.out.println("3. GRAPHICSENVIRONMENT STATE:");

        try {
            // First check if GraphicsEnvironment is already initialized
            Field geField = GraphicsEnvironment.class.getDeclaredField("localEnv");
            geField.setAccessible(true);
            Object localEnv = geField.get(null);

            if (localEnv == null) {
                System.out.println("   GraphicsEnvironment not yet initialized");
            } else {
                System.out.println("   GraphicsEnvironment already initialized");
                System.out.println("   Class: " + localEnv.getClass().getName());
            }

            // Now check headless state
            boolean isHeadless = GraphicsEnvironment.isHeadless();
            System.out.println("   isHeadless() = " + isHeadless);

            // Try to get the actual GraphicsEnvironment instance
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            System.out.println("   Instance class: " + ge.getClass().getName());

            // Check if it's HeadlessGraphicsEnvironment
            if (ge.getClass().getName().contains("Headless")) {
                System.out.println("   WARNING: Using HeadlessGraphicsEnvironment!");
            }

        } catch (Exception e) {
            System.out.println("   Error checking GraphicsEnvironment: " + e.getMessage());
        }
        System.out.println();
    }

    private static void checkDisplayDetection() {
        System.out.println("4. DISPLAY DETECTION:");

        try {
            // Try to get default screen device
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            System.out.println("   Number of screens: " + screens.length);

            if (screens.length > 0) {
                GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
                System.out.println("   Default screen: " + defaultScreen.getIDstring());

                DisplayMode dm = defaultScreen.getDisplayMode();
                System.out.println(
                        "   Display mode: "
                                + dm.getWidth()
                                + "x"
                                + dm.getHeight()
                                + " @ "
                                + dm.getRefreshRate()
                                + "Hz");
            }

            // Try to get screen size via Toolkit
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            System.out.println(
                    "   Toolkit screen size: " + screenSize.width + "x" + screenSize.height);

        } catch (HeadlessException he) {
            System.out.println("   HeadlessException: " + he.getMessage());
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void checkWSLEnvironment() {
        System.out.println("5. WSL ENVIRONMENT:");

        Map<String, String> env = System.getenv();

        // Check WSL-specific environment variables
        String[] wslVars = {
            "WSL_DISTRO_NAME", "WSL_INTEROP", "WSLENV", "WSL2_GUI_APPS_ENABLED", "WSL2_INSTALL"
        };

        boolean isWSL = false;
        for (String var : wslVars) {
            String value = env.get(var);
            if (value != null) {
                System.out.println("   " + var + " = " + value);
                isWSL = true;
            }
        }

        if (!isWSL) {
            System.out.println("   No WSL environment variables detected");
        } else {
            System.out.println("   Running in WSL!");

            // Check if WSLg is available
            String display = env.get("DISPLAY");
            if (display != null && display.startsWith(":")) {
                System.out.println("   WSLg display detected: " + display);
            }
        }

        // Check /proc/version for WSL
        try {
            java.nio.file.Path procVersion = java.nio.file.Paths.get("/proc/version");
            if (java.nio.file.Files.exists(procVersion)) {
                String version = new String(java.nio.file.Files.readAllBytes(procVersion));
                if (version.toLowerCase().contains("microsoft")
                        || version.toLowerCase().contains("wsl")) {
                    System.out.println("   /proc/version indicates WSL");
                }
            }
        } catch (Exception e) {
            // Not Linux or can't read /proc/version
        }

        System.out.println();
    }

    private static void testForceNonHeadless() {
        System.out.println("6. FORCE NON-HEADLESS TEST:");

        // Try to force non-headless by setting property
        System.setProperty("java.awt.headless", "false");
        System.out.println("   Set java.awt.headless=false");

        // Check if already initialized
        try {
            Field geField = GraphicsEnvironment.class.getDeclaredField("localEnv");
            geField.setAccessible(true);
            Object localEnv = geField.get(null);

            if (localEnv != null) {
                System.out.println(
                        "   GraphicsEnvironment already initialized - property change won't help");

                // Try to force reinitialize (this is hacky and may not work)
                System.out.println("   Attempting to reset GraphicsEnvironment...");
                geField.set(null, null);

                // Force reload
                boolean newHeadless = GraphicsEnvironment.isHeadless();
                System.out.println("   After reset, isHeadless() = " + newHeadless);

            } else {
                boolean isHeadless = GraphicsEnvironment.isHeadless();
                System.out.println("   After property set, isHeadless() = " + isHeadless);
            }

        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void checkX11Availability() {
        System.out.println("7. X11 AVAILABILITY:");

        // Check if X11 libraries are available
        try {
            Class<?> x11Class = Class.forName("sun.awt.X11GraphicsEnvironment");
            System.out.println("   X11GraphicsEnvironment class found");

            // Check if we can create an X11 connection
            String display = System.getenv("DISPLAY");
            if (display != null) {
                System.out.println("   DISPLAY=" + display);

                // Try to validate X11 connection
                try {
                    // This will try to connect to X11
                    Method method = x11Class.getDeclaredMethod("isDisplayLocal");
                    method.setAccessible(true);
                    boolean isLocal = (boolean) method.invoke(null);
                    System.out.println("   X11 display is local: " + isLocal);
                } catch (Exception e) {
                    System.out.println("   Cannot validate X11 connection: " + e.getMessage());
                }
            } else {
                System.out.println("   No DISPLAY variable set - X11 not available");
            }

        } catch (ClassNotFoundException e) {
            System.out.println(
                    "   X11GraphicsEnvironment not available (Windows or non-X11 system)");
        } catch (Exception e) {
            System.out.println("   Error checking X11: " + e.getMessage());
        }
        System.out.println();
    }
}
