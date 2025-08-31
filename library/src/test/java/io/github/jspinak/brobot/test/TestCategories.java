package io.github.jspinak.brobot.test;

/**
 * Defines test categories as string constants for JUnit 5 @Tag annotations.
 * These categories help organize and selectively run different types of tests.
 */
public final class TestCategories {
    
    // Speed-based categories
    public static final String FAST = "fast";           // < 100ms per test
    public static final String SLOW = "slow";           // > 1s per test
    public static final String VERY_SLOW = "very-slow"; // > 10s per test
    
    // Type-based categories
    public static final String UNIT = "unit";
    public static final String INTEGRATION = "integration";
    public static final String E2E = "e2e";
    public static final String SMOKE = "smoke";
    
    // Component-based categories
    public static final String CV = "cv";               // Computer Vision
    public static final String IMAGE = "image";         // Image processing
    public static final String ACTION = "action";       // Action execution
    public static final String STATE = "state";         // State management
    public static final String CONFIG = "config";       // Configuration
    public static final String MOCK = "mock";           // Mock-specific tests
    
    // Resource-based categories
    public static final String MEMORY_INTENSIVE = "memory-intensive";
    public static final String CPU_INTENSIVE = "cpu-intensive";
    public static final String IO_INTENSIVE = "io-intensive";
    
    // Environment-based categories
    public static final String REQUIRES_DISPLAY = "requires-display";
    public static final String HEADLESS = "headless";
    public static final String CI_SAFE = "ci-safe";
    
    // Special categories
    public static final String FLAKY = "flaky";         // Known flaky tests
    public static final String MANUAL = "manual";       // Requires manual verification
    public static final String NIGHTLY = "nightly";     // Run in nightly builds
    public static final String QUARANTINE = "quarantine"; // Temporarily disabled
    
    private TestCategories() {
        // Utility class, prevent instantiation
    }
}