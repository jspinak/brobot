package io.github.jspinak.brobot.startup;

/**
 * Static initializer that MUST run before ANY AWT/Swing classes are loaded.
 * This ensures Java works in physical pixels like Java 8, not logical pixels.
 * 
 * CRITICAL: This class must be loaded before any Screen, Robot, or other AWT classes.
 */
public class PhysicalResolutionInitializer {
    
    private static boolean initialized = false;
    
    static {
        // This runs when the class is first loaded
        initialize();
    }
    
    /**
     * Forces physical resolution mode for the entire JVM.
     * Must be called before ANY AWT classes are loaded.
     * Skips configuration in headless environments.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        // Check if we're in headless mode
        String headlessProperty = System.getProperty("java.awt.headless");
        if ("true".equalsIgnoreCase(headlessProperty)) {
            // Skip configuration in headless mode
            initialized = true;
            return;
        }
        
        // CRITICAL: Disable DPI awareness completely
        // This makes modern Java behave like Java 8
        System.setProperty("sun.java2d.dpiaware", "false");
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        
        // Windows-specific settings
        System.setProperty("sun.java2d.win.uiScaleX", "1.0");
        System.setProperty("sun.java2d.win.uiScaleY", "1.0");
        System.setProperty("sun.java2d.win.uiScale.enabled", "false");
        
        // Override any DPI awareness
        System.setProperty("sun.java2d.dpiaware.override", "false");
        
        // Disable scaling for Swing/AWT
        System.setProperty("swing.bufferPerformance", "false");
        System.setProperty("awt.nativeDoubleBuffering", "false");
        
        // Note: We do NOT force headless mode to false anymore
        // This allows tests to run in headless mode
        // System.setProperty("java.awt.headless", "false");  // REMOVED
        
        initialized = true;
        
        System.out.println("[Brobot] Physical resolution mode enabled");
    }
    
    /**
     * Verify that initialization was successful.
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Force initialization if not already done.
     * Call this at the very start of your application.
     */
    public static void forceInitialization() {
        if (!initialized) {
            initialize();
        }
    }
}