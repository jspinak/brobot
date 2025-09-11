package io.github.jspinak.brobot.runner.ui.components.base;

import javafx.scene.layout.VBox;

import org.springframework.beans.factory.annotation.Autowired;

import io.github.jspinak.brobot.runner.ui.lifecycle.UIComponent;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;
import io.github.jspinak.brobot.runner.ui.registry.UIComponentRegistry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for all panels in the application. Provides common functionality and implements the
 * UIComponent lifecycle.
 *
 * <p>Features: - Automatic component registration - Lifecycle management - Access to common
 * managers - Logging and debugging support
 */
@Slf4j
public abstract class BasePanel extends VBox implements UIComponent {

    @Autowired protected UIComponentRegistry componentRegistry;

    @Autowired protected UIUpdateManager updateManager;

    @Autowired protected LabelManager labelManager;

    @Getter private boolean initialized = false;

    @Getter private final String componentId;

    /** Creates a new BasePanel with automatic ID generation. */
    public BasePanel() {
        this.componentId = generateComponentId();
    }

    /**
     * Creates a new BasePanel with a specific ID.
     *
     * @param componentId The component ID to use
     */
    public BasePanel(String componentId) {
        this.componentId = componentId;
    }

    @Override
    public void initialize() {
        if (initialized) {
            log.warn("Component {} already initialized", componentId);
            return;
        }

        log.debug("Initializing component: {}", componentId);

        // Register with component registry
        if (componentRegistry != null) {
            componentRegistry.register(componentId, this);
        }

        // Set up base styling
        getStyleClass().add("base-panel");

        // Call subclass initialization
        doInitialize();

        initialized = true;
        log.info("Component {} initialized successfully", componentId);
    }

    @Override
    public void refresh() {
        if (!initialized) {
            log.warn("Cannot refresh uninitialized component: {}", componentId);
            return;
        }

        log.debug("Refreshing component: {}", componentId);
        doRefresh();
    }

    @Override
    public void cleanup() {
        log.debug("Cleaning up component: {}", componentId);

        // Cancel any scheduled updates
        if (updateManager != null) {
            updateManager.cancelScheduledUpdate(componentId);
        }

        // Unregister from component registry
        if (componentRegistry != null) {
            componentRegistry.unregister(componentId);
        }

        // Call subclass cleanup
        doCleanup();

        initialized = false;
        log.info("Component {} cleaned up", componentId);
    }

    @Override
    public boolean isValid() {
        return initialized && isVisible() && getScene() != null;
    }

    /**
     * Generates a unique component ID.
     *
     * @return A unique component ID
     */
    protected String generateComponentId() {
        return getClass().getSimpleName() + "_" + System.nanoTime();
    }

    /**
     * Schedules a periodic UI update for this component.
     *
     * @param updateTask The update task to run
     * @param periodSeconds The period in seconds
     */
    protected void schedulePeriodicUpdate(Runnable updateTask, int periodSeconds) {
        if (updateManager != null) {
            updateManager.schedulePeriodicUpdate(
                    componentId,
                    updateTask,
                    0,
                    periodSeconds,
                    java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    /** Logs the component state for debugging. */
    public void logComponentState() {
        log.info("Component State: {}", componentId);
        log.info("  Initialized: {}", initialized);
        log.info("  Valid: {}", isValid());
        log.info("  Visible: {}", isVisible());
        log.info("  Has Scene: {}", getScene() != null);
        log.info("  Style Classes: {}", String.join(", ", getStyleClass()));
        log.info("  Children Count: {}", getChildren().size());
    }

    /** Subclasses must implement this to perform their specific initialization. */
    protected abstract void doInitialize();

    /** Subclasses must implement this to perform their specific refresh logic. */
    protected abstract void doRefresh();

    /** Subclasses can override this to perform specific cleanup. */
    protected void doCleanup() {
        // Default implementation - subclasses can override
    }
}
