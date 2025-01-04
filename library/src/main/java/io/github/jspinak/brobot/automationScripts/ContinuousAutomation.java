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
 * This class runs an automation script continuously by
 * - searching for active states at a fixed interval
 * - running transitions for any active states found
 *
 * When using this script, the model could contain some states without transitions and some states with transitions.
 * The script will run transitions for active states with transitions until a rest state without transitions is reached.
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
