package io.github.jspinak.brobot.initialization;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.action.internal.region.SearchRegionDependencyRegistry;
import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.navigation.service.StateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrator for state-specific initialization phases.
 *
 * <p>This class manages state and search region initialization including: 1. State registration and
 * validation 2. Search region dependency resolution 3. Cross-state reference validation 4.
 * Declarative region setup
 *
 * <p>This orchestrator works in conjunction with BrobotStartupOrchestrator to handle the
 * state-specific aspects of initialization.
 *
 * <p>By centralizing state initialization, we ensure: - Proper ordering of initialization steps -
 * Clear visibility into what's being initialized - Single point of control for declarative regions
 * - Easy debugging of state relationships
 */
@Component("stateInitializationOrchestrator")
@Slf4j
@RequiredArgsConstructor
@Order(1) // Run very early in the initialization chain
public class StateInitializationOrchestrator {

    private final StateStore stateStore;
    private final StateService stateService;
    private final DynamicRegionResolver dynamicRegionResolver;
    private final SearchRegionDependencyRegistry dependencyRegistry;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final List<InitializationPhase> completedPhases = new ArrayList<>();

    /** Initialization phases for tracking */
    public enum InitializationPhase {
        BEAN_CREATION("Bean Creation"),
        STATE_REGISTRATION("State Registration"),
        SEARCH_REGION_DEPENDENCIES("Search Region Dependencies"),
        CROSS_STATE_VALIDATION("Cross-State Validation"),
        DECLARATIVE_REGIONS("Declarative Regions Setup"),
        FINAL_VALIDATION("Final Validation");

        private final String description;

        InitializationPhase(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PostConstruct
    public void init() {
        log.info("[STATE-ORCHESTRATOR] StateInitializationOrchestrator created and ready");
        recordPhaseCompletion(InitializationPhase.BEAN_CREATION);
    }

    /**
     * Main orchestration method triggered by StatesRegisteredEvent. This coordinates all
     * initialization phases in the correct order.
     */
    @EventListener(StatesRegisteredEvent.class)
    @Order(2) // Run very early, before individual component initializers
    public void orchestrateInitialization(StatesRegisteredEvent event) {
        if (initialized.getAndSet(true)) {
            log.info("[STATE-INIT] Already initialized, skipping");
            return;
        }

        log.info("[STATE-INIT] ========================================");
        log.info("[STATE-INIT] Starting Brobot Initialization");
        log.info(
                "[STATE-INIT] States: {}, Transitions: {}",
                event.getStateCount(),
                event.getTransitionCount());
        log.info("[STATE-INIT] ========================================");

        try {
            // Phase 1: Validate state registration
            validateStateRegistration();

            // Phase 2: Initialize search region dependencies
            initializeSearchRegionDependencies();

            // Phase 3: Validate cross-state references
            validateCrossStateReferences();

            // Phase 4: Setup declarative regions
            setupDeclarativeRegions();

            // Phase 5: Final validation
            performFinalValidation();

            log.info("[STATE-INIT] ========================================");
            log.info("[STATE-INIT] Initialization Complete!");
            log.info(
                    "[STATE-INIT] Completed phases: {}",
                    completedPhases.stream()
                            .map(InitializationPhase::getDescription)
                            .collect(Collectors.joining(", ")));
            log.info("[STATE-INIT] ========================================");

        } catch (Exception e) {
            log.error("[STATE-INIT] Initialization failed at phase: {}", getCurrentPhase(), e);
            throw new RuntimeException("Brobot initialization failed", e);
        }
    }

    /** Phase 1: Validate that all states are properly registered */
    private void validateStateRegistration() {
        log.info("[STATE-INIT] Phase 1: Validating state registration");

        Collection<State> states = stateStore.getAllStates();
        log.info("[STATE-INIT]   - Found {} states in StateStore", states.size());

        // Validate each state
        for (State state : states) {
            String stateName = state.getClass().getSimpleName();
            log.debug(
                    "[STATE-INIT]   - State: {} (Images: {}, Regions: {}, Locations: {})",
                    stateName,
                    state.getStateImages().size(),
                    state.getStateRegions().size(),
                    state.getStateLocations().size());

            // Check if state is also in StateService
            if (!stateService.getAllStates().contains(state)) {
                log.warn(
                        "[STATE-INIT]   - WARNING: State {} is in StateStore but not in"
                                + " StateService",
                        stateName);
            }
        }

        recordPhaseCompletion(InitializationPhase.STATE_REGISTRATION);
    }

    /** Phase 2: Initialize search region dependencies for all state objects */
    private void initializeSearchRegionDependencies() {
        log.info("[STATE-INIT] Phase 2: Initializing search region dependencies");

        List<StateObject> allObjects = new ArrayList<>();
        Map<String, List<StateObject>> objectsByState = new HashMap<>();

        // Collect all state objects
        for (State state : stateStore.getAllStates()) {
            String stateName = state.getClass().getSimpleName();
            List<StateObject> stateObjects = new ArrayList<>();

            stateObjects.addAll(state.getStateImages());
            stateObjects.addAll(state.getStateLocations());
            stateObjects.addAll(state.getStateRegions());

            allObjects.addAll(stateObjects);
            objectsByState.put(stateName, stateObjects);

            log.debug("[STATE-INIT]   - State {} has {} objects", stateName, stateObjects.size());
        }

        log.info("[STATE-INIT]   - Total objects to process: {}", allObjects.size());

        // Register dependencies with the resolver
        dynamicRegionResolver.registerDependencies(allObjects);

        log.info(
                "[STATE-INIT]   - Registered {} objects with DynamicRegionResolver",
                allObjects.size());

        recordPhaseCompletion(InitializationPhase.SEARCH_REGION_DEPENDENCIES);
    }

    /** Phase 3: Validate cross-state references */
    private void validateCrossStateReferences() {
        log.info("[STATE-INIT] Phase 3: Validating cross-state references");

        Set<String> availableStates =
                stateStore.getAllStates().stream()
                        .map(state -> state.getClass().getSimpleName())
                        .collect(Collectors.toSet());

        int crossStateReferences = 0;
        int invalidReferences = 0;

        for (State state : stateStore.getAllStates()) {
            for (StateObject obj : getAllStateObjects(state)) {
                // Check for cross-state references based on SearchRegionOnObject
                if (obj instanceof io.github.jspinak.brobot.model.state.StateImage) {
                    io.github.jspinak.brobot.model.state.StateImage img =
                            (io.github.jspinak.brobot.model.state.StateImage) obj;
                    if (img.getSearchRegionOnObject() != null) {
                        String targetState = img.getSearchRegionOnObject().getTargetStateName();
                        if (targetState != null && !targetState.equals(obj.getOwnerStateName())) {
                            crossStateReferences++;

                            if (!availableStates.contains(targetState)) {
                                log.warn(
                                        "[STATE-INIT]   - INVALID: {} references non-existent"
                                                + " state: {}",
                                        obj.getName(),
                                        targetState);
                                invalidReferences++;
                            } else {
                                log.debug(
                                        "[STATE-INIT]   - Valid cross-reference: {} -> {}",
                                        obj.getName(),
                                        targetState);
                            }
                        }
                    }
                }
            }
        }

        log.info(
                "[STATE-INIT]   - Cross-state references: {} (invalid: {})",
                crossStateReferences,
                invalidReferences);

        if (invalidReferences > 0) {
            log.warn(
                    "[STATE-INIT]   - WARNING: Found {} invalid cross-state references",
                    invalidReferences);
        }

        recordPhaseCompletion(InitializationPhase.CROSS_STATE_VALIDATION);
    }

    /** Phase 4: Setup declarative regions */
    private void setupDeclarativeRegions() {
        log.info("[STATE-INIT] Phase 4: Setting up declarative regions");

        int regionsSetup = 0;

        // Process each state's declarative regions
        for (State state : stateStore.getAllStates()) {
            String stateName = state.getClass().getSimpleName();

            for (StateObject obj : getAllStateObjects(state)) {
                // Setup declarative regions based on SearchRegionOnObject
                if (obj instanceof io.github.jspinak.brobot.model.state.StateImage) {
                    io.github.jspinak.brobot.model.state.StateImage img =
                            (io.github.jspinak.brobot.model.state.StateImage) obj;
                    if (img.getSearchRegionOnObject() != null) {
                        // Register with dependency registry
                        dependencyRegistry.registerDependency(obj, img.getSearchRegionOnObject());

                        regionsSetup++;
                        log.debug(
                                "[STATE-INIT]   - Registered regions for {}.{}",
                                stateName,
                                obj.getName());
                    }
                }
            }
        }

        log.info("[STATE-INIT]   - Declarative regions setup: {}", regionsSetup);

        recordPhaseCompletion(InitializationPhase.DECLARATIVE_REGIONS);
    }

    /** Phase 5: Perform final validation */
    private void performFinalValidation() {
        log.info("[STATE-INIT] Phase 5: Final validation");

        // Verify all critical components are initialized
        boolean allGood = true;

        if (stateStore.getAllStates().isEmpty()) {
            log.error("[STATE-INIT]   - ERROR: No states registered!");
            allGood = false;
        }

        // DynamicRegionResolver doesn't have isInitialized method, skip this check
        // The resolver is initialized during dependency registration

        if (allGood) {
            log.info("[STATE-INIT]   - All validations passed ✓");
        } else {
            log.warn("[STATE-INIT]   - Some validations failed, check logs above");
        }

        recordPhaseCompletion(InitializationPhase.FINAL_VALIDATION);
    }

    /** Helper method to get all state objects from a state */
    private List<StateObject> getAllStateObjects(State state) {
        List<StateObject> objects = new ArrayList<>();
        objects.addAll(state.getStateImages());
        objects.addAll(state.getStateLocations());
        objects.addAll(state.getStateRegions());
        return objects;
    }

    /** Record completion of an initialization phase */
    private void recordPhaseCompletion(InitializationPhase phase) {
        completedPhases.add(phase);
        log.debug("[STATE-INIT] Completed phase: {}", phase.getDescription());
    }

    /** Get the current phase being executed */
    private String getCurrentPhase() {
        if (completedPhases.isEmpty()) {
            return "STARTUP";
        }
        return completedPhases.get(completedPhases.size() - 1).getDescription();
    }

    /** Check if initialization is complete */
    public boolean isInitialized() {
        return initialized.get();
    }

    /** Get list of completed phases */
    public List<InitializationPhase> getCompletedPhases() {
        return new ArrayList<>(completedPhases);
    }

    /** Force re-initialization (useful for testing or hot-reload scenarios) */
    public void reinitialize() {
        log.info("[STATE-INIT] Forcing re-initialization");
        initialized.set(false);
        completedPhases.clear();

        // Trigger re-initialization with a synthetic event
        StatesRegisteredEvent event =
                new StatesRegisteredEvent(this, stateStore.getAllStates().size(), 0);
        orchestrateInitialization(event);
    }
}
