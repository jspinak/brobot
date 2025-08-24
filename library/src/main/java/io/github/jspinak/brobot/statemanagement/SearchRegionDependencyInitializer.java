package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes search region dependencies when states are loaded.
 * This component listens for state initialization events and registers
 * all cross-state search region dependencies with the DynamicRegionResolver.
 */
@Component
@Slf4j
public class SearchRegionDependencyInitializer {
    
    private final StateStore stateStore;
    private final DynamicRegionResolver dynamicRegionResolver;
    
    public SearchRegionDependencyInitializer(StateStore stateStore, 
                                           DynamicRegionResolver dynamicRegionResolver) {
        this.stateStore = stateStore;
        this.dynamicRegionResolver = dynamicRegionResolver;
        log.info("SearchRegionDependencyInitializer constructor called");
    }
    
    /**
     * Initializes search region dependencies when all states have been registered.
     * This listens for the StatesRegisteredEvent to ensure all states are available.
     */
    @EventListener(StatesRegisteredEvent.class)
    public void onStatesRegistered(StatesRegisteredEvent event) {
        log.info("SearchRegionDependencyInitializer: Received StatesRegisteredEvent with {} states", 
                event.getStateCount());
        initializeDependencies();
    }
    
    /**
     * Initializes search region dependencies.
     * This method collects all state objects and registers their dependencies.
     */
    private void initializeDependencies() {
        log.info("SearchRegionDependencyInitializer: Starting dependency registration");
        
        List<StateObject> allObjects = new ArrayList<>();
        
        // Collect all state objects from all states
        for (State state : stateStore.getAllStates()) {
            log.debug("Processing state: {}", state.getClass().getSimpleName());
            allObjects.addAll(state.getStateImages());
            allObjects.addAll(state.getStateLocations());
            allObjects.addAll(state.getStateRegions());
        }
        
        log.info("Found {} state objects to process for dependencies", allObjects.size());
        
        // Register dependencies with the resolver
        dynamicRegionResolver.registerDependencies(allObjects);
        
        log.info("SearchRegionDependencyInitializer: Completed registration");
    }
    
    /**
     * Re-registers dependencies when states are reloaded or updated.
     * This can be called manually or triggered by state reload events.
     */
    public void refreshDependencies() {
        initializeDependencies();
    }
}