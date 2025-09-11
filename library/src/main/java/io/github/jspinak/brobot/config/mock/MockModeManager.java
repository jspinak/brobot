package io.github.jspinak.brobot.config.mock;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized manager for mock mode configuration across the Brobot framework.
 *
 * <p>This class provides a single source of truth for mock mode status and ensures consistency
 * across all components that need to check or set mock mode.
 *
 * <p>Mock mode is used to:
 *
 * <ul>
 *   <li>Enable testing in headless/CI environments
 *   <li>Use dummy images instead of real screen captures
 *   <li>Simulate user interactions without actual mouse/keyboard events
 *   <li>Provide predictable timing for test assertions
 * </ul>
 *
 * <p>The manager synchronizes mock mode across:
 *
 * <ul>
 *   <li>System properties (for configuration loading)
 *   <li>ExecutionEnvironment (for runtime behavior)
 *   <li>FrameworkSettings (for SikuliX compatibility)
 * </ul>
 *
 * @since 1.0
 */
@Slf4j
public class MockModeManager {

    private static final String MOCK_MODE_PROPERTY = "brobot.mock.mode";
    private static final String FRAMEWORK_MOCK_PROPERTY = "brobot.framework.mock";
    private static final String CORE_MOCK_PROPERTY = "brobot.core.mock-mode";

    /**
     * Checks if mock mode is currently enabled.
     *
     * <p>This method checks ExecutionEnvironment as the authoritative source, falling back to
     * system properties if ExecutionEnvironment is not initialized.
     *
     * @return true if mock mode is enabled, false otherwise
     */
    public static boolean isMockMode() {
        try {
            // Primary check: ExecutionEnvironment
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            if (env != null) {
                return env.isMockMode();
            }
        } catch (Exception e) {
            log.debug("ExecutionEnvironment not available, checking system properties");
        }

        // Fallback: Check system properties
        return "true".equalsIgnoreCase(System.getProperty(MOCK_MODE_PROPERTY))
                || "true".equalsIgnoreCase(System.getProperty(FRAMEWORK_MOCK_PROPERTY))
                || "true".equalsIgnoreCase(System.getProperty(CORE_MOCK_PROPERTY));
    }

    /**
     * Enables or disables mock mode across all Brobot components.
     *
     * <p>This method ensures consistency by updating:
     *
     * <ul>
     *   <li>All relevant system properties
     *   <li>ExecutionEnvironment configuration
     *   <li>FrameworkSettings (if available)
     * </ul>
     *
     * @param enable true to enable mock mode, false to disable
     */
    public static void setMockMode(boolean enable) {
        log.info("Setting mock mode to: {}", enable);

        // 1. Update system properties
        System.setProperty(MOCK_MODE_PROPERTY, String.valueOf(enable));
        System.setProperty(FRAMEWORK_MOCK_PROPERTY, String.valueOf(enable));
        System.setProperty(CORE_MOCK_PROPERTY, String.valueOf(enable));

        // 2. Update ExecutionEnvironment
        try {
            ExecutionEnvironment.Builder envBuilder =
                    ExecutionEnvironment.builder().mockMode(enable);

            // In mock mode, typically also want headless and no screen capture
            if (enable) {
                envBuilder.forceHeadless(true).allowScreenCapture(false);
            }

            ExecutionEnvironment.setInstance(envBuilder.build());
            log.debug("Updated ExecutionEnvironment mock mode to: {}", enable);
        } catch (Exception e) {
            log.warn("Failed to update ExecutionEnvironment: {}", e.getMessage());
        }

        // 3. Update FrameworkSettings for SikuliX compatibility
        updateFrameworkSettings(enable);

        // 4. Log the final state
        logMockModeState();
    }

    /**
     * Updates FrameworkSettings.mock using reflection to avoid direct dependency.
     *
     * @param enable the mock mode value to set
     */
    private static void updateFrameworkSettings(boolean enable) {
        try {
            Class<?> frameworkSettingsClass =
                    Class.forName("io.github.jspinak.brobot.config.core.FrameworkSettings");
            frameworkSettingsClass.getField("mock").set(null, enable);
            log.debug("Updated FrameworkSettings.mock to: {}", enable);
        } catch (ClassNotFoundException e) {
            log.debug("FrameworkSettings not available (expected in some configurations)");
        } catch (Exception e) {
            log.warn("Failed to update FrameworkSettings.mock: {}", e.getMessage());
        }
    }

    /** Logs the current mock mode state across all components for debugging. */
    public static void logMockModeState() {
        if (!log.isDebugEnabled()) {
            return;
        }

        StringBuilder state = new StringBuilder("Mock Mode State:\n");

        // Check system properties
        state.append("  System Properties:\n");
        state.append("    ")
                .append(MOCK_MODE_PROPERTY)
                .append(" = ")
                .append(System.getProperty(MOCK_MODE_PROPERTY))
                .append("\n");
        state.append("    ")
                .append(FRAMEWORK_MOCK_PROPERTY)
                .append(" = ")
                .append(System.getProperty(FRAMEWORK_MOCK_PROPERTY))
                .append("\n");
        state.append("    ")
                .append(CORE_MOCK_PROPERTY)
                .append(" = ")
                .append(System.getProperty(CORE_MOCK_PROPERTY))
                .append("\n");

        // Check ExecutionEnvironment
        try {
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            state.append("  ExecutionEnvironment:\n");
            state.append("    mockMode = ").append(env.isMockMode()).append("\n");
            state.append("    hasDisplay = ").append(env.hasDisplay()).append("\n");
            state.append("    canCaptureScreen = ").append(env.canCaptureScreen()).append("\n");
        } catch (Exception e) {
            state.append("  ExecutionEnvironment: Not available\n");
        }

        // Check FrameworkSettings
        try {
            Class<?> frameworkSettingsClass =
                    Class.forName("io.github.jspinak.brobot.config.core.FrameworkSettings");
            Object mockValue = frameworkSettingsClass.getField("mock").get(null);
            state.append("  FrameworkSettings.mock = ").append(mockValue).append("\n");
        } catch (Exception e) {
            state.append("  FrameworkSettings: Not available\n");
        }

        log.debug(state.toString());
    }

    /**
     * Ensures mock mode is properly initialized based on system properties.
     *
     * <p>This method should be called during application startup to ensure mock mode is
     * consistently set based on configuration.
     */
    public static void initializeMockMode() {
        boolean shouldEnableMock =
                "true".equalsIgnoreCase(System.getProperty(MOCK_MODE_PROPERTY))
                        || "true".equalsIgnoreCase(System.getProperty(FRAMEWORK_MOCK_PROPERTY))
                        || "true".equalsIgnoreCase(System.getProperty(CORE_MOCK_PROPERTY));

        if (shouldEnableMock) {
            log.info("Initializing mock mode based on system properties");
            setMockMode(true);
        }
    }
}
