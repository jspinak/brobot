package io.github.jspinak.brobot.config.dpi;

import java.lang.instrument.Instrumentation;

/**
 * Java Agent that disables DPI awareness before any classes are loaded.
 *
 * <p>This agent must be loaded with: -javaagent:brobot.jar
 *
 * @since 1.1.0
 */
public class DPIAgent {

    /**
     * Premain method called before the application's main method. This is the earliest possible
     * point to set system properties.
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("[Brobot Agent] Disabling DPI awareness...");

        // Set properties before ANY classes are loaded
        System.setProperty("sun.java2d.dpiaware", "false");
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("sun.java2d.win.uiScale", "1.0");
        System.setProperty("sun.java2d.uiScale.enabled", "false");

        System.out.println("[Brobot Agent] DPI awareness disabled successfully");
    }

    /** Alternative entry point for agents attached at runtime. */
    public static void agentmain(String args, Instrumentation inst) {
        System.err.println(
                "[Brobot Agent] WARNING: Setting DPI properties after JVM start may not work!");
        premain(args, inst);
    }
}
