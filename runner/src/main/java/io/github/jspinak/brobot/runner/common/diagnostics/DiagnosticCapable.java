package io.github.jspinak.brobot.runner.common.diagnostics;

/**
 * Interface for components that provide diagnostic information.
 *
 * <p>This interface enables AI-friendly debugging by providing a standard way to extract runtime
 * state and diagnostic information from components.
 *
 * @since 1.0.0
 */
public interface DiagnosticCapable {

    /**
     * Gets diagnostic information about the current state of this component.
     *
     * @return diagnostic information including state, metrics, and health status
     */
    DiagnosticInfo getDiagnosticInfo();

    /**
     * Checks if diagnostic mode is enabled for this component.
     *
     * @return true if diagnostic mode is enabled, false otherwise
     */
    default boolean isDiagnosticModeEnabled() {
        return false;
    }

    /**
     * Enables or disables diagnostic mode for this component. When enabled, the component may
     * collect additional diagnostic data.
     *
     * @param enabled true to enable diagnostic mode, false to disable
     */
    default void enableDiagnosticMode(boolean enabled) {
        // Default implementation does nothing
    }
}
