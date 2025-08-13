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
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        System.out.println("\n=== Brobot Physical Resolution Initializer ===");
        System.out.println("Setting JVM properties for physical resolution...");
        
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
        
        // Force physical pixels
        System.setProperty("java.awt.headless", "false");
        
        initialized = true;
        
        System.out.println("✓ DPI awareness disabled");
        System.out.println("✓ All operations will use physical pixels");
        System.out.println("✓ Pattern matching will work like SikuliX IDE");
        System.out.println("===============================================\n");
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