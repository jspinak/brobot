package io.github.jspinak.brobot.navigation.transition;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.statemanagement.StateMemory;

import lombok.Getter;

/**
 * Retrieves and packages all transition components needed for state navigation.
 *
 * <p>TransitionFetcher acts as a specialized repository that gathers all the transition-related
 * objects required to execute a navigation from one state to another. It resolves state references,
 * handles special states like PREVIOUS, and packages both the "from" transition (leaving the source
 * state) and "to" transition (arriving at the target state) into a convenient bundle.
 *
 * <p>Components fetched:
 *
 * <ul>
 *   <li><b>From Transitions</b>: StateTransitions object for the source state
 *   <li><b>From Transition</b>: Specific transition that activates the target state
 *   <li><b>From State</b>: Complete State object for the source
 *   <li><b>From Function</b>: Executable BooleanSupplier for the transition
 *   <li><b>To Transitions</b>: StateTransitions object for the target state
 *   <li><b>To Transition</b>: Transition finish for target state recognition
 *   <li><b>To State</b>: Complete State object for the target
 * </ul>
 *
 * <p>Special handling:
 *
 * <ul>
 *   <li><b>PREVIOUS State</b>: Resolved to actual hidden states when referenced
 *   <li><b>Missing Transitions</b>: Returns empty Optional if any component missing
 *   <li><b>Validation</b>: Ensures all components present before returning
 * </ul>
 *
 * <p>Usage pattern:
 *
 * <pre>
 * Optional&lt;TransitionFetcher&gt; fetcher = transitionFetcher.getTransitions(fromId, toId);
 * if (fetcher.isPresent()) {
 *     BooleanSupplier fromFunction = fetcher.get().getFromTransitionFunction();
 *     // Execute transition logic
 * }
 * </pre>
 *
 * <p>Design benefits:
 *
 * <ul>
 *   <li>Single point of transition resolution
 *   <li>Handles all special cases in one place
 *   <li>Provides complete transition context
 *   <li>Enables clean separation of fetching and execution
 * </ul>
 *
 * <p>In the model-based approach, TransitionFetcher encapsulates the complexity of resolving
 * transition references into executable components. This abstraction allows the execution engine to
 * focus on orchestrating transitions without worrying about the details of finding and preparing
 * the necessary objects.
 *
 * @since 1.0
 * @see StateTransitions
 * @see StateTransition
 * @see State
 * @see TransitionConditionPackager
 * @see TransitionExecutor
 */
@Component
@Getter
public class TransitionFetcher {

    private final StateMemory stateMemory;
    private final StateService allStatesInProjectService;
    private final StateTransitionService stateTransitionsInProjectService;
    private final TransitionConditionPackager transitionBooleanSupplierPackager;

    private Long transitionToEnum; // may be PREVIOUS
    private StateTransitions fromTransitions;
    private StateTransition fromTransition;
    private State fromState;
    private BooleanSupplier fromTransitionFunction;
    private StateTransitions toTransitions;
    private StateTransition toTransition;
    private State toState;

    public TransitionFetcher(
            StateMemory stateMemory,
            StateService allStatesInProjectService,
            StateTransitionService stateTransitionsInProjectService,
            TransitionConditionPackager transitionBooleanSupplierPackager) {
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
        this.transitionBooleanSupplierPackager = transitionBooleanSupplierPackager;
    }

    /**
     * Fetches all transition components for navigating between two states.
     *
     * <p>Main entry point that coordinates the fetching process:
     *
     * <ol>
     *   <li>Resets all internal state
     *   <li>Fetches "from" transition components
     *   <li>Fetches "to" transition components
     *   <li>Validates completeness
     * </ol>
     *
     * <p>Returns this object wrapped in Optional if all components are successfully fetched, or
     * empty Optional if any required component is missing.
     *
     * @param from Source state ID
     * @param to Target state ID (may be PREVIOUS)
     * @return Optional containing this fetcher if complete, empty otherwise
     */
    public Optional<TransitionFetcher> getTransitions(Long from, Long to) {
        reset();
        setFromTransitions(from, to);
        setToTransitions(to);
        if (!isComplete()) return Optional.empty();
        return Optional.of(this);
    }

    /**
     * Clears all fetched components for a fresh fetch operation.
     *
     * <p>Ensures no stale data from previous fetches affects the current operation.
     */
    private void reset() {
        transitionToEnum = null;
        fromTransitions = null;
        fromTransition = null;
        fromState = null;
        fromTransitionFunction = null;
        toTransitions = null;
        toTransition = null;
        toState = null;
    }

    /**
     * Validates that all required transition components have been fetched.
     *
     * <p>Checks for non-null values in all component fields. Special handling for "null" string
     * which indicates invalid transition.
     *
     * @return true if all components present and valid
     */
    private boolean isComplete() {
        // Check that all required 'from' components are present
        boolean fromComplete =
                !Objects.equals(transitionToEnum, "null")
                        && fromTransitions != null
                        && fromTransition != null
                        && fromState != null
                        && fromTransitionFunction != null;

        // Check that required 'to' components are present
        // If toTransitions exists, we need a valid transitionFinish to verify arrival
        boolean toComplete = toState != null &&
                (toTransitions == null || toTransition != null);

        if (!fromComplete || !toComplete) {
            System.out.println("=== TRANSITION DEBUG: isComplete() check failed:");
            System.out.println("===   transitionToEnum: " + transitionToEnum);
            System.out.println("===   fromTransitions: " + (fromTransitions != null));
            System.out.println("===   fromTransition: " + (fromTransition != null));
            System.out.println("===   fromState: " + (fromState != null));
            System.out.println("===   fromTransitionFunction: " + (fromTransitionFunction != null));
            System.out.println("===   toState: " + (toState != null));
            System.out.println("===   toTransitions: " + (toTransitions != null) + " (optional)");
            System.out.println("===   toTransition: " + (toTransition != null) + " (optional)");
        }

        return fromComplete && toComplete;
    }

    /**
     * Fetches components related to leaving the source state.
     *
     * <p>Retrieves:
     *
     * <ul>
     *   <li>StateTransitions object for source state
     *   <li>Specific transition that activates target
     *   <li>Source State object
     *   <li>Executable function for the transition
     * </ul>
     *
     * <p>Handles PREVIOUS state resolution through getTransitionToEnum.
     *
     * @param from Source state ID
     * @param to Target state ID (may be PREVIOUS)
     */
    private void setFromTransitions(Long from, Long to) {
        System.out.println(
                "=== TRANSITION DEBUG: TransitionFetcher.setFromTransitions() from="
                        + from
                        + " to="
                        + to);

        Optional<StateTransitions> fromTransitions =
                stateTransitionsInProjectService.getTransitions(from);
        allStatesInProjectService
                .getState(from)
                .ifPresent(
                        state -> {
                            fromState = state;
                            System.out.println(
                                    "=== TRANSITION DEBUG: Found fromState: " + state.getName());
                        });

        if (fromTransitions.isEmpty()) {
            System.out.println(
                    "=== TRANSITION DEBUG: No StateTransitions found for from state " + from);
        }

        fromTransitions.ifPresent(
                transitions -> {
                    this.fromTransitions = transitions;
                    System.out.println(
                            "=== TRANSITION DEBUG: Found transitions object for state " + from);
                    System.out.println(
                            "=== TRANSITION DEBUG: Number of transitions: "
                                    + transitions.getTransitions().size());

                    transitionToEnum =
                            stateTransitionsInProjectService.getTransitionToEnum(from, to);
                    System.out.println(
                            "=== TRANSITION DEBUG: transitionToEnum resolved to: "
                                    + transitionToEnum);

                    transitions
                            .getTransitionFunctionByActivatedStateId(transitionToEnum)
                            .ifPresent(
                                    trsn -> {
                                        this.fromTransition = trsn;
                                        System.out.println(
                                                "=== TRANSITION DEBUG: Found transition function"
                                                        + " for "
                                                        + transitionToEnum);
                                    });

                    if (this.fromTransition == null) {
                        System.out.println(
                                "=== TRANSITION DEBUG: NO transition function found for "
                                        + transitionToEnum);
                        System.out.println(
                                "=== TRANSITION DEBUG: Available activations in transitions:");
                        transitions
                                .getTransitions()
                                .forEach(
                                        t -> {
                                            System.out.println(
                                                    "===   - Activates: " + t.getActivate());
                                        });
                    }

                    transitions
                            .getTransitionFunctionByActivatedStateId(transitionToEnum)
                            .ifPresent(
                                    trsFunction ->
                                            fromTransitionFunction =
                                                    transitionBooleanSupplierPackager
                                                            .toBooleanSupplier(trsFunction));
                });
    }

    /**
     * Fetches components related to arriving at the target state.
     *
     * <p>Retrieves:
     *
     * <ul>
     *   <li>StateTransitions object for target state
     *   <li>Transition finish for state recognition
     *   <li>Target State object
     * </ul>
     *
     * @param to Target state ID
     */
    private void setToTransitions(Long to) {
        Optional<StateTransitions> fromTransitions =
                stateTransitionsInProjectService.getTransitions(to);
        allStatesInProjectService.getState(to).ifPresent(state -> toState = state);
        fromTransitions.ifPresent(
                transitions -> {
                    this.toTransitions = transitions;
                    this.toTransition = transitions.getTransitionFinish();
                });
    }
}
