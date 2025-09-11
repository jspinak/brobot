package io.github.jspinak.brobot.statemanagement;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateStore;

import lombok.extern.slf4j.Slf4j;

/**
 * Initializes search region dependencies when states are loaded.
 *
 * <p>NOTE: The main initialization logic has been moved to BrobotInitializationOrchestrator for
 * better centralized control. This class is kept for backward compatibility and as a fallback, but
 * the orchestrator handles the primary initialization.
 *
 * @deprecated Use BrobotInitializationOrchestrator for centralized initialization
 */
@Component
@Slf4j
@Deprecated(since = "2.0", forRemoval = false)
public class SearchRegionDependencyInitializer
        implements ApplicationListener<StatesRegisteredEvent> {

    private final StateStore stateStore;
    private final DynamicRegionResolver dynamicRegionResolver;

    @Autowired private ApplicationContext applicationContext;

    public SearchRegionDependencyInitializer(
            StateStore stateStore, DynamicRegionResolver dynamicRegionResolver) {
        this.stateStore = stateStore;
        this.dynamicRegionResolver = dynamicRegionResolver;
        log.debug("[INIT DEBUG] SearchRegionDependencyInitializer constructor called (legacy)");
    }

    @PostConstruct
    public void init() {
        log.info(
                "[INIT DEBUG] SearchRegionDependencyInitializer @PostConstruct called - bean is"
                        + " active!");
    }

    private volatile boolean initialized = false;

    /**
     * Implements ApplicationListener interface for robust event handling. This is the most reliable
     * way to receive Spring events. Using ApplicationListener interface avoids issues
     * with @EventListener annotation processing.
     */
    @Override
    public void onApplicationEvent(StatesRegisteredEvent event) {
        // NOTE: BrobotInitializationOrchestrator handles this now
        // This is kept as a fallback in case the orchestrator is disabled
        log.debug(
                "[INIT DEBUG] SearchRegionDependencyInitializer: Received event (delegating to"
                        + " orchestrator)");

        // Only proceed if orchestrator hasn't handled it
        synchronized (this) {
            if (initialized) {
                log.debug("[INIT DEBUG] Already initialized by orchestrator");
                return;
            }

            // Check if orchestrator exists and has run
            try {
                var orchestrator =
                        applicationContext.getBean(
                                "stateInitializationOrchestrator",
                                io.github.jspinak.brobot.initialization
                                        .StateInitializationOrchestrator.class);
                if (orchestrator.isInitialized()) {
                    initialized = true;
                    log.debug("[INIT DEBUG] Orchestrator has handled initialization");
                    return;
                }
            } catch (Exception e) {
                // Orchestrator not available, proceed with fallback
                log.info("[INIT DEBUG] Orchestrator not available, using fallback initialization");
            }

            try {
                initializeDependencies();
                initialized = true;
                log.info("[INIT DEBUG] SearchRegionDependencyInitializer: COMPLETED (fallback)");
            } catch (Exception e) {
                log.error("[INIT DEBUG] SearchRegionDependencyInitializer: FAILED", e);
                throw new RuntimeException("Failed to initialize search region dependencies", e);
            }
        }
    }

    /**
     * Initializes search region dependencies. This method collects all state objects and registers
     * their dependencies.
     */
    private void initializeDependencies() {
        log.info(
                "[INIT DEBUG] SearchRegionDependencyInitializer: Starting dependency registration");

        List<StateObject> allObjects = new ArrayList<>();

        // Collect all state objects from all states
        for (State state : stateStore.getAllStates()) {
            log.info("[INIT DEBUG] Processing state: {}", state.getClass().getSimpleName());
            allObjects.addAll(state.getStateImages());
            allObjects.addAll(state.getStateLocations());
            allObjects.addAll(state.getStateRegions());
        }

        log.info(
                "[INIT DEBUG] Found {} state objects to process for dependencies",
                allObjects.size());
        for (StateObject obj : allObjects) {
            log.info("[INIT DEBUG] Object: {} (owner: {})", obj.getName(), obj.getOwnerStateName());
        }

        // Register dependencies with the resolver
        dynamicRegionResolver.registerDependencies(allObjects);

        log.info("SearchRegionDependencyInitializer: Completed registration");
    }

    /**
     * Re-registers dependencies when states are reloaded or updated. This can be called manually or
     * triggered by state reload events.
     */
    public void refreshDependencies() {
        synchronized (this) {
            initialized = false; // Allow re-initialization
            initializeDependencies();
            initialized = true;
        }
    }

    /** Check if dependencies have been initialized. */
    public boolean isInitialized() {
        return initialized;
    }
}
