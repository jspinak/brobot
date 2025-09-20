package io.github.jspinak.brobot.navigation.transition;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.statemanagement.StateVisibilityManager;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.logging.MessageFormatter;

import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates complex state transitions in the Brobot framework.
 *
 * <p>TransitionExecutor is the central execution engine for state transitions, managing the
 * intricate process of moving from one GUI state to another. It coordinates all aspects of a
 * transition including action execution, state activation/deactivation, hidden state management,
 * and cascading transitions.
 *
 * <p>Transition anatomy:
 *
 * <ul>
 *   <li><b>FromTransition</b>: Actions and state changes originating from the source state
 *   <li><b>ToTransition</b>: Recognition and finalization of the target state
 *   <li><b>Cascading Transitions</b>: Additional states activated as side effects
 *   <li><b>Exit Transitions</b>: States deactivated during the transition
 * </ul>
 *
 * <p>Execution phases:
 *
 * <ol>
 *   <li><b>Validation</b>: Verify source state is active and transition exists
 *   <li><b>FromTransition</b>: Execute actions to leave source state
 *   <li><b>State Discovery</b>: Determine all states to activate (including cascades)
 *   <li><b>ToTransitions</b>: Execute recognition for each target state
 *   <li><b>Hidden States</b>: Update hidden state relationships
 *   <li><b>Exit States</b>: Deactivate states marked for exit
 *   <li><b>Verification</b>: Confirm target state is now active
 * </ol>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Cascading Support</b>: States can trigger activation of additional states
 *   <li><b>Hidden State Handling</b>: Manages overlay relationships between states
 *   <li><b>Probability Updates</b>: Adjusts state existence probabilities for mock mode
 *   <li><b>Cycle Prevention</b>: Avoids infinite loops in mutually-activating states
 *   <li><b>Partial Success</b>: Core transition can succeed even if cascades fail
 * </ul>
 *
 * <p>Complex transition scenarios:
 *
 * <ul>
 *   <li><b>Multi-State Activation</b>: Opening a dialog that shows multiple panels
 *   <li><b>State Replacement</b>: Tab switching where one state replaces another
 *   <li><b>Overlay Management</b>: Modal dialogs that hide but don't close underlying states
 *   <li><b>Back Navigation</b>: Returning to previously hidden states
 * </ul>
 *
 * <p>Design philosophy:
 *
 * <ul>
 *   <li>Transitions are atomic - either the core transition succeeds or fails
 *   <li>Side effects (cascading transitions) don't affect core success
 *   <li>Already-active states are not re-activated (prevents cycles)
 *   <li>Hidden states are preserved for back navigation
 * </ul>
 *
 * <p>In the model-based approach, TransitionExecutor embodies the framework's understanding of how
 * GUI state changes occur. Rather than simple page-to-page navigation, it models the complex
 * reality of modern GUIs where actions can trigger multiple state changes, overlays, and cascading
 * effects.
 *
 * @since 1.0
 * @see StateTransition
 * @see StateTransitions
 * @see TransitionFetcher
 * @see StateVisibilityManager
 * @see StateMemory
 */
@Slf4j
@Component
public class TransitionExecutor {

    private final StateTransitionService stateTransitionsInProjectService;
    private final StateTransitionsJointTable stateTransitionsJointTable;
    private final StateVisibilityManager setHiddenStates;
    private final StateMemory stateMemory;
    private final StateService allStatesInProjectService;
    private final TransitionFetcher transitionFetcher;
    private final TransitionConditionPackager transitionBooleanSupplierPackager;

    public TransitionExecutor(
            StateTransitionService stateTransitionsInProjectService,
            StateTransitionsJointTable stateTransitionsJointTable,
            StateVisibilityManager setHiddenStates,
            StateMemory stateMemory,
            StateService allStatesInProjectService,
            TransitionFetcher transitionFetcher,
            TransitionConditionPackager transitionBooleanSupplierPackager) {
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
     *
     * <p>This is the primary entry point for state transitions, handling all aspects of moving from
     * one state to another including action execution, state activation/deactivation, and cascade
     * management.
     *
     * <p>Important notes:
     *
     * <ul>
     *   <li>Source state must be currently active for transition to proceed
     *   <li>PREVIOUS state references are resolved before reaching this method
     *   <li>Success is determined by core transition, not cascading effects
     *   <li>Logs success/failure to Report for debugging and monitoring
     * </ul>
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Updates StateMemory with new active/inactive states
     *   <li>Modifies state probabilities for mock mode
     *   <li>Updates hidden state relationships
     *   <li>Increments transition success counters
     * </ul>
     *
     * @param fromStateId ID of the source state (must be active)
     * @param toStateId ID of the target state to transition to
     * @return true if the transition succeeded, false otherwise
     * @see #doTransitions(Long, Long)
     */
    public boolean go(Long fromStateId, Long toStateId) {
        String fromStateName = allStatesInProjectService.getStateName(fromStateId);
        String toStateName = allStatesInProjectService.getStateName(toStateId);
        if (doTransitions(fromStateId, toStateId)) {
            ConsoleReporter.format(
                    MessageFormatter.check + " Transition %s(%s)->%s(%s) successful. \n",
                    fromStateId,
                    fromStateName,
                    toStateId,
                    toStateName);
            return true;
        }
        ConsoleReporter.format(
                MessageFormatter.fail + " Transition %s(%s)->%s(%s) not successful. \n",
                fromStateId,
                fromStateName,
                toStateId,
                toStateName);
        return false;
    }

    /**
     * Core transition execution logic handling both FromTransition and ToTransitions.
     *
     * <p>Implements the complete transition protocol:
     *
     * <ol>
     *   <li><b>Validation</b>: Confirms source state is active and transitions exist
     *   <li><b>FromTransition</b>: Executes actions to leave the source state
     *   <li><b>ToTransition</b>: Executes the primary target state's transition finish
     *   <li><b>State Collection</b>: Gathers all states to activate (including hidden states)
     *   <li><b>Probability Reset</b>: Sets base probabilities for states to activate
     *   <li><b>Additional ToTransitions</b>: Executes recognition for cascading states
     *   <li><b>Exit Processing</b>: Deactivates states marked for exit
     *   <li><b>Source Exit</b>: Optionally exits source state based on visibility rules
     * </ol>
     *
     * <p>Transition composition:
     *
     * <ul>
     *   <li><b>FromTransition</b>: Actions to trigger target state appearance
     *   <li><b>ToTransition</b>: Recognition that target state is now active
     *   <li><b>Cascading</b>: Additional states activated as side effects
     * </ul>
     *
     * <p>Design decisions:
     *
     * <ul>
     *   <li>Already-active states skip ToTransition (prevents cycles)
     *   <li>Hidden states of exiting states are re-activated
     *   <li>Success requires BOTH FromTransition AND primary ToTransition to succeed
     *   <li>Cascading transition failures don't affect overall success
     * </ul>
     *
     * @param from Source state ID (must be active)
     * @param to Target state ID
     * @return true if transition completes successfully
     */
    private boolean doTransitions(Long from, Long to) {
        String fromStateName = allStatesInProjectService.getStateName(from);
        String toStateName = allStatesInProjectService.getStateName(to);
        if (!stateMemory.getActiveStates().contains(from)) {
            String activeStatesStr =
                    stateMemory.getActiveStates().stream()
                            .map(id -> id + "(" + allStatesInProjectService.getStateName(id) + ")")
                            .reduce("", (s1, s2) -> s1.isEmpty() ? s2 : s1 + ", " + s2);
            System.out.println(
                    "=== TRANSITION DEBUG: 'from' state "
                            + from
                            + "("
                            + fromStateName
                            + ") is not active. Active states: ["
                            + activeStatesStr
                            + "]");
            return false; // the 'from' State is not active
        }
        Optional<TransitionFetcher> transitionsOpt = transitionFetcher.getTransitions(from, to);
        if (transitionsOpt.isEmpty()) {
            System.out.println(
                    "=== TRANSITION DEBUG: No transitions found from "
                            + from
                            + "("
                            + fromStateName
                            + ") to "
                            + to
                            + "("
                            + toStateName
                            + ")");
            return false; // couldn't find one of the needed Transitions
        }
        TransitionFetcher transitions = transitionsOpt.get();

        // Execute FromTransition first
        System.out.println(
                "=== TRANSITION DEBUG: Executing FromTransition from "
                        + from
                        + "("
                        + fromStateName
                        + ") to "
                        + to
                        + "("
                        + toStateName
                        + ")");
        boolean fromTransitionSuccess = transitions.getFromTransitionFunction().getAsBoolean();
        System.out.println(
                "=== TRANSITION DEBUG: FromTransition result = " + fromTransitionSuccess);
        if (!fromTransitionSuccess) return false; // the FromTransition didn't succeed

        // Now execute the ToTransition for the primary target state
        System.out.println(
                "=== TRANSITION DEBUG: Executing ToTransition for target state "
                        + to
                        + "("
                        + toStateName
                        + ")");
        boolean toTransitionSuccess = doTransitionTo(to);
        System.out.println("=== TRANSITION DEBUG: ToTransition result = " + toTransitionSuccess);
        if (!toTransitionSuccess) {
            System.out.println(
                    "=== TRANSITION DEBUG: Transition failed - ToTransition for primary target"
                            + " state "
                            + to
                            + "("
                            + toStateName
                            + ") did not succeed");
            return false; // The ToTransition for the primary target didn't succeed
        }

        // Process additional states to activate (cascading transitions)
        Set<Long> statesToActivate = getStatesToActivate(transitions, to);
        statesToActivate.remove(to); // Remove primary target since we already processed it
        statesToActivate.forEach(
                stateName ->
                        allStatesInProjectService
                                .getState(stateName)
                                .ifPresent(State::setProbabilityToBaseProbability));
        StateTransition fromTrsn = transitions.getFromTransition();

        // Execute IncomingTransition for each additional activated state
        if (!statesToActivate.isEmpty()) {
            log.trace(
                    "Executing IncomingTransitions for additional activated states: {}",
                    statesToActivate);
            statesToActivate.forEach(
                    stateId -> {
                        String stateName = allStatesInProjectService.getStateName(stateId);
                        log.trace(
                                "Executing IncomingTransition for state {} ({})",
                                stateId,
                                stateName);
                        boolean success = doTransitionTo(stateId);
                        if (!success) {
                            log.warn(
                                    "IncomingTransition failed for state {} ({})",
                                    stateId,
                                    stateName);
                        }
                    }); // Execute IncomingTransitions for all additional activated states
        }
        fromTrsn.getExit().forEach(this::exitState); // exit all States to exit

        // Verify the primary target state is active
        if (!stateMemory.getActiveStates().contains(to)) {
            System.out.println(
                    "=== TRANSITION DEBUG: Transition failed - target state "
                            + to
                            + "("
                            + toStateName
                            + ") is not active after transition");
            return false;
        }

        if (!transitions.getFromTransitions().stateStaysVisible(to))
            exitState(from); // exit 'from' State
        return true;
    }

    /**
     * Determines the complete set of states to activate during a transition.
     *
     * <p>Collects states from multiple sources:
     *
     * <ul>
     *   <li>States defined in the FromTransition's activate list
     *   <li>Hidden states of the source state (if source exits)
     *   <li>Filters out PREVIOUS pseudo-state references
     * </ul>
     *
     * <p>The inclusion of hidden states ensures proper back navigation when overlaying states are
     * closed.
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
        statesToActivate.remove(
                SpecialStateType.PREVIOUS.getId()); // previous can't be activated, so get rid of it
        System.out.println("states to activate: " + statesToActivate);
        return statesToActivate;
    }

    /**
     * Executes the IncomingTransition (stored as transitionFinish) for a specific target state.
     *
     * <p>Handles the complete activation sequence for a state:
     *
     * <ol>
     *   <li>Skip if state already active (cycle prevention)
     *   <li>Verify state exists in the system
     *   <li>Reset state probability to base value
     *   <li>Execute IncomingTransition (transitionFinish) to verify state arrival
     *   <li>Update probability based on success/failure
     *   <li>Process hidden states if successful
     *   <li>Execute cascading transitions
     *   <li>Process exit states
     * </ol>
     *
     * <p>Note: The transitionFinish field contains the @IncomingTransition method that verifies
     * successful arrival at this state.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Updates StateMemory with active state
     *   <li>Modifies state existence probability
     *   <li>Updates hidden state relationships
     *   <li>Triggers cascading state activations
     * </ul>
     *
     * @param toStateName ID of the state to activate
     * @return true if state successfully activated (IncomingTransition succeeded)
     */
    private boolean doTransitionTo(Long toStateName) {
        if (stateMemory.getActiveStates().contains(toStateName))
            return true; // State is already active
        Optional<State> toStateOpt = allStatesInProjectService.getState(toStateName);
        if (toStateOpt.isEmpty()) {
            System.out.println("=== TRANSITION DEBUG: State doesn't exist for ID " + toStateName);
            return false; // State doesn't exist
        }
        State toState = toStateOpt.get();
        toState.setProbabilityToBaseProbability();

        // Get the StateTransitions for this state (may not exist for terminal states)
        Optional<StateTransitions> stateTransitions =
                stateTransitionsInProjectService.getTransitions(toStateName);

        boolean result = true; // Default to true if no transition finish exists
        StateTransition stateTransition = null;

        if (stateTransitions.isPresent()) {
            stateTransition = stateTransitions.get().getTransitionFinish();
            if (stateTransition != null) {
                // Execute the transition finish function
                BooleanSupplier booleanSupplier =
                        transitionBooleanSupplierPackager.toBooleanSupplier(stateTransition);
                result = booleanSupplier.getAsBoolean();
                System.out.println(
                        "=== TRANSITION DEBUG: ToTransition for state "
                                + toStateName
                                + " ("
                                + allStatesInProjectService.getStateName(toStateName)
                                + ") returned: "
                                + result);
            } else {
                System.out.println(
                        "=== TRANSITION DEBUG: No transition finish defined for state "
                                + toStateName
                                + " ("
                                + allStatesInProjectService.getStateName(toStateName)
                                + "), treating as successful");
            }
        } else {
            System.out.println(
                    "=== TRANSITION DEBUG: No StateTransitions found for state "
                            + toStateName
                            + " ("
                            + allStatesInProjectService.getStateName(toStateName)
                            + "), treating as terminal state");
        }

        if (!result) { // transition failed
            toState.setProbabilityExists(0); // mock assumes State is not present
            return false;
        }

        toState.setProbabilityExists(100); // State found
        // Add the state to active states!
        stateMemory.addActiveState(toStateName);
        setHiddenStates.set(toStateName);
        stateTransitionsJointTable.addTransitionsToHiddenStates(toState);

        // Process cascading transitions if they exist
        if (stateTransition != null) {
            stateTransition.getActivate().forEach(this::doTransitionTo);
            stateTransition.getExit().forEach(this::exitState);
        }

        return stateMemory.getActiveStates().contains(toStateName);
    }

    /**
     * Deactivates a state and cleans up its relationships.
     *
     * <p>Performs complete state exit protocol:
     *
     * <ul>
     *   <li>Removes transitions to the state's hidden states
     *   <li>Removes state from active state memory
     *   <li>Resets the state's hidden state list
     * </ul>
     *
     * <p>TODO: Add VANISH operation to verify state actually disappeared from screen
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Updates StateMemory to remove state
     *   <li>Cleans up hidden state relationships
     *   <li>Updates joint transition table
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
