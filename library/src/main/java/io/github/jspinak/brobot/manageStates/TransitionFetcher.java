package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * This class returns an object containing the StateTransitions objects and StateTransition objects that
 * we are interested in for a specific transition from one State to another.
 */
@Component
@Getter
public class TransitionFetcher {

    private final StateMemory stateMemory;
    private final AllStatesInProjectService allStatesInProjectService;
    private final StateTransitionsInProjectService stateTransitionsInProjectService;
    private final TransitionBooleanSupplierPackager transitionBooleanSupplierPackager;

    private Long transitionToEnum; // may be PREVIOUS
    private StateTransitions fromTransitions;
    private IStateTransition fromTransition;
    private State fromState;
    private BooleanSupplier fromTransitionFunction;
    private StateTransitions toTransitions;
    private IStateTransition toTransition;
    private State toState;

    public TransitionFetcher(StateMemory stateMemory, AllStatesInProjectService allStatesInProjectService,
                             StateTransitionsInProjectService stateTransitionsInProjectService,
                             TransitionBooleanSupplierPackager transitionBooleanSupplierPackager) {
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
        this.transitionBooleanSupplierPackager = transitionBooleanSupplierPackager;
    }

    public Optional<TransitionFetcher> getTransitions(Long from, Long to) {
        reset();
        setFromTransitions(from, to);
        setToTransitions(to);
        if (!isComplete()) return Optional.empty();
        return Optional.of(this);
    }

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

    private boolean isComplete() {
        return
                !Objects.equals(transitionToEnum, "null") &&
                fromTransitions != null &&
                fromTransition != null &&
                fromState != null &&
                fromTransitionFunction != null &&
                toTransitions != null &&
                toTransition != null &&
                toState != null;
    }

    private void setFromTransitions(Long from, Long to) {
        Optional<StateTransitions> fromTransitions = stateTransitionsInProjectService.getTransitions(from);
        allStatesInProjectService.getState(from).ifPresent(state -> fromState = state);
        fromTransitions.ifPresent(transitions -> {
            this.fromTransitions = transitions;
            transitionToEnum = stateTransitionsInProjectService.getTransitionToEnum(from, to);
            transitions.getTransitionFunctionByActivatedStateId(transitionToEnum).ifPresent(trsn -> this.fromTransition = trsn);
            transitions.getTransitionFunctionByActivatedStateId(transitionToEnum).ifPresent(
                    trsFunction -> fromTransitionFunction =
                            transitionBooleanSupplierPackager.toBooleanSupplier(trsFunction));
        });
    }

    private void setToTransitions(Long to) {
        Optional<StateTransitions> fromTransitions = stateTransitionsInProjectService.getTransitions(to);
        allStatesInProjectService.getState(to).ifPresent(state -> toState = state);
        fromTransitions.ifPresent(transitions -> {
            this.toTransitions = transitions;
            this.toTransition = transitions.getTransitionFinish();
        });
    }
}
