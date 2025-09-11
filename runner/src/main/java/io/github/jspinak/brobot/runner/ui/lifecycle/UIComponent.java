package io.github.jspinak.brobot.runner.ui.lifecycle;

/**
 * Lifecycle interface for UI components. Implementing this interface ensures consistent
 * initialization, refresh, and cleanup behavior across all UI components.
 */
public interface UIComponent {

    /**
     * Initializes the component. This method should be called once when the component is first
     * created. It should set up the initial UI structure, register event handlers, and perform any
     * one-time setup operations.
     *
     * @throws IllegalStateException if the component is already initialized
     */
    void initialize();

    /**
     * Refreshes the component's content. This method can be called multiple times to update the
     * component's display based on current data or state changes. It should be safe to call this
     * method at any time after initialization.
     */
    void refresh();

    /**
     * Cleans up the component's resources. This method should be called when the component is being
     * removed or the application is shutting down. It should: - Unregister event handlers - Cancel
     * any running tasks - Release resources - Clear references to prevent memory leaks
     */
    void cleanup();

    /**
     * Checks if the component has been initialized.
     *
     * @return true if initialize() has been called successfully
     */
    boolean isInitialized();

    /**
     * Gets the component's unique identifier. This ID should be unique within the application and
     * can be used for debugging, logging, or component registry purposes.
     *
     * @return The component's unique identifier
     */
    default String getComponentId() {
        return getClass().getSimpleName() + "_" + hashCode();
    }

    /**
     * Validates that the component is in a valid state. This can be used to check prerequisites
     * before operations.
     *
     * @return true if the component is in a valid state
     */
    default boolean isValid() {
        return isInitialized();
    }
}
