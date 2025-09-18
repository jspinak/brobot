package io.github.jspinak.brobot.debug;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive headless environment debugger for Windows. Run this to diagnose why
 * GraphicsEnvironment is being initialized as headless.
 */
public class HeadlessDebugger {

    public static void main(String[] args) {
        System.out.println("=== Headless Environment Debugger ===\n");

        // 1. Check System Properties
        System.out.println("1. SYSTEM PROPERTIES:");
        System.out.println("   java.awt.headless = " + System.getProperty("java.awt.headless"));
        System.out.println("   awt.toolkit = " + System.getProperty("awt.toolkit"));
        System.out.println(
                "   java.awt.graphicsenv = " + System.getProperty("java.awt.graphicsenv"));
        System.out.println("   java.awt.printerjob = " + System.getProperty("java.awt.printerjob"));
        System.out.println("   os.name = " + System.getProperty("os.name"));
        System.out.println("   DISPLAY = " + System.getenv("DISPLAY"));
        System.out.println();

        // 2. Check JVM Arguments
        System.out.println("2. JVM ARGUMENTS:");
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        for (String arg : arguments) {
            if (arg.contains("headless") || arg.contains("awt")) {
                System.out.println("   Found: " + arg);
            }
        }
        if (arguments.stream().noneMatch(arg -> arg.contains("headless") || arg.contains("awt"))) {
            System.out.println("   No headless or awt arguments found in JVM args");
        }
        System.out.println();

        // 3. Check Environment Variables
        System.out.println("3. ENVIRONMENT VARIABLES:");
        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            if (key.contains("JAVA")
                    || key.contains("GRADLE")
                    || key.contains("AWT")
                    || key.contains("DISPLAY")
                    || key.contains("HEADLESS")) {
                System.out.println("   " + key + " = " + entry.getValue());
            }
        }
        System.out.println();

        // 4. Try to force non-headless BEFORE checking GraphicsEnvironment
        System.out.println("4. FORCING NON-HEADLESS MODE:");
        String originalValue = System.getProperty("java.awt.headless");
        System.out.println("   Original java.awt.headless = " + originalValue);
        System.setProperty("java.awt.headless", "false");
        System.out.println("   Set java.awt.headless = false");
        System.out.println();

        // 5. Check GraphicsEnvironment
        System.out.println("5. GRAPHICS ENVIRONMENT CHECK:");
        try {
            boolean isHeadless = GraphicsEnvironment.isHeadless();
            System.out.println("   GraphicsEnvironment.isHeadless() = " + isHeadless);

            if (!isHeadless) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                System.out.println("   Default screen device: " + ge.getDefaultScreenDevice());
                GraphicsDevice[] devices = ge.getScreenDevices();
                System.out.println("   Number of screen devices: " + devices.length);
                for (int i = 0; i < devices.length; i++) {
                    System.out.println("   Device " + i + ": " + devices[i].getIDstring());
                }
            }
        } catch (HeadlessException e) {
            System.out.println("   HeadlessException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("   Exception: " + e.getClass().getName() + " - " + e.getMessage());
        }
        System.out.println();

        // 6. Try creating Robot
        System.out.println("6. ROBOT CREATION TEST:");
        try {
            Robot robot = new Robot();
            System.out.println("   ✓ Robot created successfully");
            Point mousePos = MouseInfo.getPointerInfo().getLocation();
            System.out.println("   Mouse position: " + mousePos.x + ", " + mousePos.y);
        } catch (AWTException e) {
            System.out.println("   ✗ AWTException: " + e.getMessage());
        } catch (HeadlessException e) {
            System.out.println("   ✗ HeadlessException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(
                    "   ✗ Exception: " + e.getClass().getName() + " - " + e.getMessage());
        }
        System.out.println();

        // 7. Check Toolkit
        System.out.println("7. TOOLKIT CHECK:");
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            System.out.println("   Toolkit class: " + toolkit.getClass().getName());
            Dimension screenSize = toolkit.getScreenSize();
            System.out.println("   Screen size: " + screenSize.width + " x " + screenSize.height);
        } catch (HeadlessException e) {
            System.out.println("   HeadlessException: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("   Exception: " + e.getClass().getName() + " - " + e.getMessage());
        }
        System.out.println();

        // 8. Class Loading Order
        System.out.println("8. CLASS LOADING CHECK:");
        try {
            ClassLoader classLoader = HeadlessDebugger.class.getClassLoader();
            System.out.println("   ClassLoader: " + classLoader.getClass().getName());

            // Check if GraphicsEnvironment was already loaded
            try {
                Class<?> geClass =
                        Class.forName("java.awt.GraphicsEnvironment", false, classLoader);
                System.out.println("   GraphicsEnvironment class already loaded");
            } catch (ClassNotFoundException e) {
                System.out.println("   GraphicsEnvironment class not yet loaded");
            }
        } catch (Exception e) {
            System.out.println("   Exception checking class loading: " + e.getMessage());
        }
        System.out.println();

        // 9. Gradle Properties Check
        System.out.println("9. GRADLE PROPERTIES:");
        String[] gradleProps = {
            "org.gradle.java.awt.headless",
            "systemProp.java.awt.headless",
            "org.gradle.daemon",
            "org.gradle.jvmargs"
        };
        for (String prop : gradleProps) {
            String value = System.getProperty(prop);
            if (value != null) {
                System.out.println("   " + prop + " = " + value);
            }
        }
        System.out.println();

        System.out.println("=== Debug Complete ===");
    }
}
