package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.services.StateService;
import io.github.jspinak.brobot.services.StateTransitionsService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static io.github.jspinak.brobot.datatypes.state.NullState.Name.NULL;

/**
 * This class returns an object containing the StateTransitions objects and StateTransition objects that
 * we are interested in for a specific transition from one State to another.
 */
@Component
@Getter
public class TransitionFetcher {

    private final StateMemory stateMemory;
    private final StateService stateService;
    private final StateTransitionsService stateTransitionsService;

    private String transitionToEnum; // may be PREVIOUS
    private StateTransitions fromTransitions;
    private StateTransition fromTransition;
    private State fromState;
    private BooleanSupplier fromTransitionFunction;
    private StateTransitions toTransitions;
    private StateTransition toTransition;
    private State toState;

    public TransitionFetcher(StateMemory stateMemory, StateService stateService,
                             StateTransitionsService stateTransitionsService) {
        this.stateMemory = stateMemory;
        this.stateService = stateService;
        this.stateTransitionsService = stateTransitionsService;
    }

    public Optional<TransitionFetcher> getTransitions(String from, String to) {
        reset();
        setFromTransitions(from, to);
        setToTransitions(to);
        if (!isComplete()) return Optional.empty();
        return Optional.of(this);
    }

    private void reset() {
        transitionToEnum = NULL.toString();
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
                !Objects.equals(transitionToEnum, NULL.toString()) &&
                fromTransitions != null &&
                fromTransition != null &&
                fromState != null &&
                fromTransitionFunction != null &&
                toTransitions != null &&
                toTransition != null &&
                toState != null;
    }

    private void setFromTransitions(String from, String to) {
        Optional<StateTransitions> fromTransitions = stateTransitionsService.getTransitions(from);
        stateService.findByName(from).ifPresent(state -> fromState = state);
        fromTransitions.ifPresent(transitions -> {
            this.fromTransitions = transitions;
            transitionToEnum = stateTransitionsService.getTransitionToEnum(from, to);
            transitions.getStateTransition(transitionToEnum).ifPresent(trsn -> this.fromTransition = trsn);
            transitions.getTransitionFunction(transitionToEnum).ifPresent(
                    trsFunction -> fromTransitionFunction = trsFunction);
        });
    }

    private void setToTransitions(String to) {
        Optional<StateTransitions> fromTransitions = stateTransitionsService.getTransitions(to);
        stateService.findByName(to).ifPresent(state -> toState = state);
        fromTransitions.ifPresent(transitions -> {
            this.toTransitions = transitions;
            this.toTransition = transitions.getTransitionFinish();
        });
    }
}
