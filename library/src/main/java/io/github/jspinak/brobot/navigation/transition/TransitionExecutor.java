package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.statemanagement.StateVisibilityManager;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.logging.MessageFormatter;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Orchestrates complex state transitions in the Brobot framework.
 * 
 * <p>TransitionExecutor is the central execution engine for state transitions, managing the 
 * intricate process of moving from one GUI state to another. It coordinates all aspects 
 * of a transition including action execution, state activation/deactivation, hidden state 
 * management, and cascading transitions.</p>
 * 
 * <p>Transition anatomy:
 * <ul>
 *   <li><b>FromTransition</b>: Actions and state changes originating from the source state</li>
 *   <li><b>ToTransition</b>: Recognition and finalization of the target state</li>
 *   <li><b>Cascading Transitions</b>: Additional states activated as side effects</li>
 *   <li><b>Exit Transitions</b>: States deactivated during the transition</li>
 * </ul>
 * </p>
 * 
 * <p>Execution phases:
 * <ol>
 *   <li><b>Validation</b>: Verify source state is active and transition exists</li>
 *   <li><b>FromTransition</b>: Execute actions to leave source state</li>
 *   <li><b>State Discovery</b>: Determine all states to activate (including cascades)</li>
 *   <li><b>ToTransitions</b>: Execute recognition for each target state</li>
 *   <li><b>Hidden States</b>: Update hidden state relationships</li>
 *   <li><b>Exit States</b>: Deactivate states marked for exit</li>
 *   <li><b>Verification</b>: Confirm target state is now active</li>
 * </ol>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Cascading Support</b>: States can trigger activation of additional states</li>
 *   <li><b>Hidden State Handling</b>: Manages overlay relationships between states</li>
 *   <li><b>Probability Updates</b>: Adjusts state existence probabilities for mock mode</li>
 *   <li><b>Cycle Prevention</b>: Avoids infinite loops in mutually-activating states</li>
 *   <li><b>Partial Success</b>: Core transition can succeed even if cascades fail</li>
 * </ul>
 * </p>
 * 
 * <p>Complex transition scenarios:
 * <ul>
 *   <li><b>Multi-State Activation</b>: Opening a dialog that shows multiple panels</li>
 *   <li><b>State Replacement</b>: Tab switching where one state replaces another</li>
 *   <li><b>Overlay Management</b>: Modal dialogs that hide but don't close underlying states</li>
 *   <li><b>Back Navigation</b>: Returning to previously hidden states</li>
 * </ul>
 * </p>
 * 
 * <p>Design philosophy:
 * <ul>
 *   <li>Transitions are atomic - either the core transition succeeds or fails</li>
 *   <li>Side effects (cascading transitions) don't affect core success</li>
 *   <li>Already-active states are not re-activated (prevents cycles)</li>
 *   <li>Hidden states are preserved for back navigation</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, TransitionExecutor embodies the framework's understanding of 
 * how GUI state changes occur. Rather than simple page-to-page navigation, it models the 
 * complex reality of modern GUIs where actions can trigger multiple state changes, overlays, 
 * and cascading effects.</p>
 * 
 * @since 1.0
 * @see StateTransition
 * @see StateTransitions
 * @see TransitionFetcher
 * @see StateVisibilityManager
 * @see StateMemory
 */
@Component
public class TransitionExecutor {

    private final StateTransitionService stateTransitionsInProjectService;
    private final StateTransitionsJointTable stateTransitionsJointTable;
    private final StateVisibilityManager setHiddenStates;
    private final StateMemory stateMemory;
    private final StateService allStatesInProjectService;
    private final TransitionFetcher transitionFetcher;
    private final TransitionConditionPackager transitionBooleanSupplierPackager;

    public TransitionExecutor(StateTransitionService stateTransitionsInProjectService, StateTransitionsJointTable stateTransitionsJointTable,
                        StateVisibilityManager setHiddenStates, StateMemory stateMemory, StateService allStatesInProjectService,
                        TransitionFetcher transitionFetcher, TransitionConditionPackager transitionBooleanSupplierPackager) {
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
        this.stateTransitionsJointTable = stateTransitionsJointTable;
        this.setHiddenStates = setHiddenStates;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.transitionFetcher = transitionFetcher;
        this.transitionBooleanSupplierPackager = transitionBooleanSupplierPackager;
    }

    /**
     * Executes a complete state transition from source to target state.
     * <p>
     * This is the primary entry point for state transitions, handling all aspects
     * of moving from one state to another including action execution, state
     * activation/deactivation, and cascade management.
     * <p>
     * Important notes:
     * <ul>
     *   <li>Source state must be currently active for transition to proceed</li>
     *   <li>PREVIOUS state references are resolved before reaching this method</li>
     *   <li>Success is determined by core transition, not cascading effects</li>
     *   <li>Logs success/failure to Report for debugging and monitoring</li>
     * </ul>
     * <p>
     * Side effects:
     * <ul>
     *   <li>Updates StateMemory with new active/inactive states</li>
     *   <li>Modifies state probabilities for mock mode</li>
     *   <li>Updates hidden state relationships</li>
     *   <li>Increments transition success counters</li>
     * </ul>
     *
     * @param fromStateId ID of the source state (must be active)
     * @param toStateId ID of the target state to transition to
     * @return true if the transition succeeded, false otherwise
     * @see #doTransitions(Long, Long)
     */
    public boolean go(Long fromStateId, Long toStateId) {
        if (doTransitions(fromStateId, toStateId)) {
            ConsoleReporter.format(MessageFormatter.check+" Transition %s->%s successful. \n", fromStateId, toStateId);
            return true;
        }
        ConsoleReporter.format(MessageFormatter.fail+" Transition %s->%s not successful. \n", fromStateId, toStateId);
        return false;
    }

    /**
     * Core transition execution logic handling both FromTransition and ToTransitions.
     * <p>
     * Implements the complete transition protocol:
     * <ol>
     *   <li><b>Validation</b>: Confirms source state is active and transitions exist</li>
     *   <li><b>FromTransition</b>: Executes actions to leave the source state</li>
     *   <li><b>State Collection</b>: Gathers all states to activate (including hidden states)</li>
     *   <li><b>Probability Reset</b>: Sets base probabilities for states to activate</li>
     *   <li><b>ToTransitions</b>: Executes recognition for each target state</li>
     *   <li><b>Exit Processing</b>: Deactivates states marked for exit</li>
     *   <li><b>Source Exit</b>: Optionally exits source state based on visibility rules</li>
     * </ol>
     * <p>
     * Transition composition:
     * <ul>
     *   <li><b>FromTransition</b>: Actions to trigger target state appearance</li>
     *   <li><b>ToTransition</b>: Recognition that target state is now active</li>
     *   <li><b>Cascading</b>: Additional states activated as side effects</li>
     * </ul>
     * <p>
     * Design decisions:
     * <ul>
     *   <li>Already-active states skip ToTransition (prevents cycles)</li>
     *   <li>Hidden states of exiting states are re-activated</li>
     *   <li>Success requires both FromTransition and primary ToTransition</li>
     *   <li>Cascading transition failures don't affect overall success</li>
     * </ul>
     *
     * @param from Source state ID (must be active)
     * @param to Target state ID
     * @return true if transition completes successfully
     */
    private boolean doTransitions(Long from, Long to) {
        if (!stateMemory.getActiveStates().contains(from)) {
            System.out.println("=== TRANSITION DEBUG: 'from' state " + from + " is not active. Active states: " + stateMemory.getActiveStates());
            return false; // the 'from' State is not active
        }
        Optional<TransitionFetcher> transitionsOpt = transitionFetcher.getTransitions(from, to);
        if (transitionsOpt.isEmpty()) {
            System.out.println("=== TRANSITION DEBUG: No transitions found from " + from + " to " + to);
            return false; // couldn't find one of the needed Transitions
        }
        TransitionFetcher transitions = transitionsOpt.get();
        System.out.println("=== TRANSITION DEBUG: Executing FromTransition from " + from + " to " + to);
        boolean transitionSuccess = transitions.getFromTransitionFunction().getAsBoolean();
        System.out.println("=== TRANSITION DEBUG: FromTransition result = " + transitionSuccess);
        if (!transitionSuccess) return false; // the FromTransition didn't succeed
        Set<Long> statesToActivate = getStatesToActivate(transitions, to);
        statesToActivate.forEach(stateName ->
                allStatesInProjectService.getState(stateName).ifPresent(State::setProbabilityToBaseProbability));
        StateTransition fromTrsn = transitions.getFromTransition();
        statesToActivate.forEach(this::doTransitionTo); // do all ToTransitions
        fromTrsn.getExit().forEach(this::exitState); // exit all States to exit
        if (!stateMemory.getActiveStates().contains(to)) return false;
        if (!transitions.getFromTransitions().stateStaysVisible(to)) exitState(from); // exit 'from' State
        return true;
    }

    /**
     * Determines the complete set of states to activate during a transition.
     * <p>
     * Collects states from multiple sources:
     * <ul>
     *   <li>States defined in the FromTransition's activate list</li>
     *   <li>Hidden states of the source state (if source exits)</li>
     *   <li>Filters out PREVIOUS pseudo-state references</li>
     * </ul>
     * <p>
     * The inclusion of hidden states ensures proper back navigation when
     * overlaying states are closed.
     *
     * @param transitions Transition definitions containing state lists
     * @param to Target state ID for visibility checking
     * @return Set of state IDs to activate
     */
    private Set<Long> getStatesToActivate(TransitionFetcher transitions, Long to) {
        // initialize the States to activate with the States to activate in the FromTransition
        Set<Long> statesToActivate = new HashSet<>(transitions.getFromTransition().getActivate());
        // Always include the target state
        statesToActivate.add(to);
        // if the 'from' State exits, add its hidden States to the States to activate
        if (!transitions.getFromTransitions().stateStaysVisible(to))
            statesToActivate.addAll(transitions.getFromState().getHiddenStateIds());
        statesToActivate.remove(SpecialStateType.PREVIOUS.getId()); // previous can't be activated, so get rid of it
        System.out.println("states to activate: "+statesToActivate);
        return statesToActivate;
    }

    /**
     * Executes the ToTransition for a specific target state.
     * <p>
     * Handles the complete activation sequence for a state:
     * <ol>
     *   <li>Skip if state already active (cycle prevention)</li>
     *   <li>Verify state exists in the system</li>
     *   <li>Reset state probability to base value</li>
     *   <li>Execute transition finish actions</li>
     *   <li>Update probability based on success/failure</li>
     *   <li>Process hidden states if successful</li>
     *   <li>Execute cascading transitions</li>
     *   <li>Process exit states</li>
     * </ol>
     * <p>
     * Side effects:
     * <ul>
     *   <li>Updates StateMemory with active state</li>
     *   <li>Modifies state existence probability</li>
     *   <li>Updates hidden state relationships</li>
     *   <li>Triggers cascading state activations</li>
     * </ul>
     *
     * @param toStateName ID of the state to activate
     * @return true if state successfully activated
     */
    private boolean doTransitionTo(Long toStateName) {
        if (stateMemory.getActiveStates().contains(toStateName)) return true; // State is already active
        Optional<State> toStateOpt = allStatesInProjectService.getState(toStateName);
        if (toStateOpt.isEmpty()) return false; // State doesn't exist
        State toState = toStateOpt.get();
        toState.setProbabilityToBaseProbability();
        Optional<StateTransitions> stateTransitions = stateTransitionsInProjectService.getTransitions(toStateName);
        if (stateTransitions.isEmpty()) return false; // transition doesn't exist
        StateTransition StateTransition = stateTransitions.get().getTransitionFinish();
        BooleanSupplier booleanSupplier = transitionBooleanSupplierPackager.toBooleanSupplier(StateTransition);
        if (!booleanSupplier.getAsBoolean()) { // transition failed
            toState.setProbabilityExists(0); // mock assumes State is not present
            return false;
        }
        toState.setProbabilityExists(100); // State found
        setHiddenStates.set(toStateName);
        stateTransitionsJointTable.addTransitionsToHiddenStates(toState);
        StateTransition.getActivate().forEach(this::doTransitionTo);
        StateTransition.getExit().forEach(this::exitState);
        return stateMemory.getActiveStates().contains(toStateName);
    }

    /**
     * Deactivates a state and cleans up its relationships.
     * <p>
     * Performs complete state exit protocol:
     * <ul>
     *   <li>Removes transitions to the state's hidden states</li>
     *   <li>Removes state from active state memory</li>
     *   <li>Resets the state's hidden state list</li>
     * </ul>
     * <p>
     * TODO: Add VANISH operation to verify state actually disappeared from screen
     * <p>
     * Side effects:
     * <ul>
     *   <li>Updates StateMemory to remove state</li>
     *   <li>Cleans up hidden state relationships</li>
     *   <li>Updates joint transition table</li>
     * </ul>
     *
     * @param stateToExit ID of the state to deactivate
     * @return true if state successfully exited
     */
    private boolean exitState(Long stateToExit) {
        Optional<State> stateOpt = allStatesInProjectService.getState(stateToExit);
        if (stateOpt.isEmpty()) return false; // state doesn't exist
        stateTransitionsJointTable.removeTransitionsToHiddenStates(stateOpt.get());
        stateMemory.removeInactiveState(stateToExit);
        stateOpt.get().resetHidden();
        return true;
    }

}
