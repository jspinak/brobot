package io.github.jspinak.brobot.launcher;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Launcher that restarts the JVM with DPI awareness disabled if needed.
 *
 * <p>Use this as your main class to ensure DPI settings are applied correctly.
 *
 * @since 1.1.0
 */
public class BrobotLauncher {

    private static final String DPI_DISABLED_MARKER = "brobot.dpi.launcher.active";

    public static void launchWithDPIDisabled(String mainClass, String[] args) {
        // Check if we've already relaunched with DPI disabled
        if ("true".equals(System.getProperty(DPI_DISABLED_MARKER))) {
            System.out.println("[Brobot Launcher] Already running with DPI disabled");
            return;
        }

        // Check if DPI properties are already set correctly
        if ("false".equals(System.getProperty("sun.java2d.dpiaware"))) {
            System.out.println("[Brobot Launcher] DPI already disabled via JVM arguments");
            return;
        }

        System.out.println("[Brobot Launcher] Restarting JVM with DPI awareness disabled...");

        try {
            // Build command to restart JVM with DPI disabled
            List<String> command = new ArrayList<>();

            // Java executable
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            command.add(javaBin);

            // Get current JVM arguments
            List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
            command.addAll(jvmArgs);

            // Add DPI disabling properties
            command.add("-Dsun.java2d.dpiaware=false");
            command.add("-Dsun.java2d.uiScale=1.0");
            command.add("-Dsun.java2d.win.uiScale=1.0");
            command.add("-Dsun.java2d.uiScale.enabled=false");
            command.add("-D" + DPI_DISABLED_MARKER + "=true");

            // Classpath
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));

            // Main class and arguments
            command.add(mainClass);
            for (String arg : args) {
                command.add(arg);
            }

            // Start new process
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.inheritIO(); // Use same input/output as current process
            Process process = builder.start();

            // Wait for new process to complete
            int exitCode = process.waitFor();
            System.exit(exitCode);

        } catch (Exception e) {
            System.err.println("[Brobot Launcher] Failed to restart with DPI disabled: " + e);
            e.printStackTrace();
            // Continue running without DPI disabled
        }
    }

    /**
     * Example usage in your main method:
     *
     * <pre>
     * public static void main(String[] args) {
     *     BrobotLauncher.launchWithDPIDisabled(MyApp.class.getName(), args);
     *     // Your normal application code here
     * }
     * </pre>
     */
    public static void ensureDPIDisabled() {
        // Get the calling class (the actual main class)
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String callingClass = stack[2].getClassName();

        // Relaunch if needed
        launchWithDPIDisabled(callingClass, new String[0]);
    }
}
