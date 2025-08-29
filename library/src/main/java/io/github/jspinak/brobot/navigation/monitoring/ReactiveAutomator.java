package io.github.jspinak.brobot.navigation.monitoring;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.statemanagement.StateDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * Implements continuous state-based automation that monitors and responds to GUI changes.
 * 
 * <p>ReactiveAutomator provides an event-loop based automation pattern that continuously 
 * monitors the GUI for active states and executes appropriate transitions. This implementation 
 * is ideal for reactive automation scenarios where the system must respond to unpredictable 
 * GUI events or maintain ongoing interaction with an application.</p>
 * 
 * <p>Execution model:
 * <ul>
 *   <li><b>Periodic Scanning</b>: Searches for active states at configurable intervals</li>
 *   <li><b>Transition Execution</b>: Runs transitions for any active states found</li>
 *   <li><b>Rest State Support</b>: Naturally pauses at states without transitions</li>
 *   <li><b>Concurrent Safety</b>: Single-threaded executor prevents race conditions</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Automatic state discovery through periodic searches</li>
 *   <li>Graceful handling of states with and without transitions</li>
 *   <li>Clean shutdown with proper thread termination</li>
 *   <li>Error recovery with automatic stop on exceptions</li>
 *   <li>Configurable search intervals for performance tuning</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Monitoring applications for specific conditions</li>
 *   <li>Handling asynchronous popups or notifications</li>
 *   <li>Maintaining application state during long-running processes</li>
 *   <li>Creating responsive bots that react to GUI events</li>
 *   <li>Implementing watchdog functionality</li>
 * </ul>
 * </p>
 * 
 * <p>State handling strategy:
 * <ul>
 *   <li>Active states with transitions trigger state handler execution</li>
 *   <li>Active states without transitions serve as rest points</li>
 *   <li>Multiple active states are processed sequentially</li>
 *   <li>State handler failures are logged but don't stop execution</li>
 * </ul>
 * </p>
 * 
 * <p>Performance considerations:
 * <ul>
 *   <li>Search interval affects responsiveness vs. CPU usage</li>
 *   <li>State finder efficiency impacts overall performance</li>
 *   <li>Consider search optimization for large state spaces</li>
 *   <li>Single-threaded design prevents parallel state handling</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ReactiveAutomator embodies the reactive automation 
 * paradigm where the system continuously adapts to the current GUI state. This pattern 
 * is particularly powerful for applications with dynamic behavior, unexpected events, or 
 * when human interaction may occur alongside automation.</p>
 * 
 * @since 1.0
 * @see BaseAutomation
 * @see StateDetector
 * @see StateHandler
 * @see StateTransitions
 */
public class ReactiveAutomator extends BaseAutomation {
    private static final Logger logger = LoggerFactory.getLogger(ReactiveAutomator.class);
    
    private final StateDetector stateFinder;
    private final StateTransitionStore stateTransitionsRepository;
    private final StateService allStates;
    private final MonitoringService monitoringService; // Inject the generic service

    private final int searchIntervalSeconds = 1;

    /**
     * Constructs a ReactiveAutomator with required dependencies.
     * <p>
     * Initializes the automation with all necessary services for state
     * discovery, transition management, and continuous monitoring.
     *
     * @param stateFinder Service for discovering active states
     * @param stateTransitionsRepository Repository of state transitions
     * @param allStates Service providing access to all states
     * @param stateHandler Handler for processing states and transitions
     * @param monitoringService Service for scheduled task execution
     */
    public ReactiveAutomator(StateDetector stateFinder,
                              StateTransitionStore stateTransitionsRepository,
                              StateService allStates,
                              StateHandler stateHandler,
                              MonitoringService monitoringService) {
        super(stateHandler);
        this.stateFinder = stateFinder;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.allStates = allStates;
        this.monitoringService = monitoringService;
    }

    /**
     * Starts the continuous state monitoring automation.
     * <p>
     * Initiates a scheduled task that periodically searches for active
     * states and executes their transitions. The automation continues
     * until explicitly stopped or an error occurs.
     * <p>
     * Side effects:
     * <ul>
     *   <li>Sets running flag to true</li>
     *   <li>Starts scheduled task in MonitoringService</li>
     *   <li>Logs warning if already running</li>
     * </ul>
     */
    @Override
    public void start() {
        if (isRunning()) {
            logger.warn("Automation already running");
            return;
        }
        super.start(); // Set the execution state to RUNNING
        
        // Use the monitoring service to run the automation loop
        // The task is the automationLoop, the condition is the 'running' flag.
        monitoringService.startContinuousTask(
            this::automationLoop,   // The task to run (Runnable)
            this::isRunning,        // The condition to continue (BooleanSupplier)
            searchIntervalSeconds   // The delay
        );
    }

    /**
     * Stops the continuous automation.
     * <p>
     * Gracefully terminates the automation by setting the running flag
     * to false and stopping the scheduled task in MonitoringService.
     * <p>
     * Side effects:
     * <ul>
     *   <li>Sets running flag to false</li>
     *   <li>Stops scheduled task in MonitoringService</li>
     * </ul>
     */
    @Override
    public void stop() {
        super.stop(); // This will set running = false
        monitoringService.stop(); // Tell the service to stop the task
    }

    /**
     * Core automation loop executed periodically.
     * <p>
     * Searches for active states and processes each one through the
     * configured StateHandler. States with transitions are handled
     * normally, while states without transitions trigger the no-transition
     * callback. Any exceptions cause the automation to stop.
     * <p>
     * Execution flow:
     * <ol>
     *   <li>Find all currently active states</li>
     *   <li>For each active state with transitions: call handleState</li>
     *   <li>For states without transitions: call onNoTransitionFound</li>
     *   <li>On exception: log error and stop automation</li>
     * </ol>
     */
    private void automationLoop() {
        if (!isRunning()) return;

        try {
            // Find active states
            Set<Long> activeStates = findActiveStates();

            for (Long activeStateId : activeStates) {
                Optional<StateTransitions> stateTransitions = stateTransitionsRepository.get(activeStateId);
                if (stateTransitions.isPresent()) {
                    Optional<State> state = allStates.getState(activeStateId);
                    if (state.isPresent()) {
                        if (!stateHandler.handleState(state.get(), stateTransitions.get())) {
                            // If state handler returns false, you might want to do something
                            logger.info("State handler returned false");
                        }
                    }
                } else {
                    stateHandler.onNoTransitionFound();
                }
            }
        } catch (Exception e) {
            logger.error("Error in script loop", e);
            stop();
        }
    }

    /**
     * Discovers all currently active states in the GUI.
     * <p>
     * Uses StateFinder to perform a complete state discovery, identifying
     * which states are currently visible/active. This approach ensures
     * comprehensive coverage but may be computationally expensive for
     * large state spaces.
     * <p>
     * Performance notes:
     * <ul>
     *   <li>Current implementation searches all states</li>
     *   <li>Could be optimized by predicting likely active states</li>
     *   <li>Consider caching or incremental search for large state sets</li>
     * </ul>
     *
     * @return Set of IDs for all currently active states
     */
    private Set<Long> findActiveStates() {
        return stateFinder.refreshActiveStates();
    }
}
