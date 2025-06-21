package io.github.jspinak.brobot.automationScripts;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateFinder;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implements continuous state-based automation that monitors and responds to GUI changes.
 * 
 * <p>ContinuousAutomation provides an event-loop based automation pattern that continuously 
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
 * <p>In the model-based approach, ContinuousAutomation embodies the reactive automation 
 * paradigm where the system continuously adapts to the current GUI state. This pattern 
 * is particularly powerful for applications with dynamic behavior, unexpected events, or 
 * when human interaction may occur alongside automation.</p>
 * 
 * @since 1.0
 * @see BaseAutomation
 * @see StateFinder
 * @see StateHandler
 * @see StateTransitions
 */
@Component
public class ContinuousAutomation extends BaseAutomation {
    private static final Logger logger = LoggerFactory.getLogger(ContinuousAutomation.class);
    private final StateFinder stateFinder;
    private final StateTransitionsRepository stateTransitionsRepository;
    private final AllStatesInProjectService allStates;
    private final ScheduledExecutorService executor;
    private final int searchIntervalMs;

    public ContinuousAutomation(StateFinder stateFinder,
                                StateTransitionsRepository stateTransitionsRepository,
                                AllStatesInProjectService allStates,
                                StateHandler stateHandler) {
        super(stateHandler);
        this.stateFinder = stateFinder;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.allStates = allStates;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.searchIntervalMs = 1000; // Configurable search interval
    }

    @Override
    public void start() {
        if (isRunning()) {
            logger.warn("Automation already running");
            return;
        }

        running = true;
        executor.scheduleAtFixedRate(this::automationLoop, 0, searchIntervalMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        super.stop();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(searchIntervalMs * 2L, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void automationLoop() {
        if (!running) return;

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
     * The easiest way to do this is to search all states.
     * A more efficient way would involve searching for states that are likely to become active.
     * @return the currently active states.
     */
    private Set<Long> findActiveStates() {
        return stateFinder.refreshActiveStates();
    }
}
