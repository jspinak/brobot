package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateTransitionsEntity;
import io.github.jspinak.brobot.app.database.entities.TransitionEntity;
import io.github.jspinak.brobot.manageStates.IStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.ActionDefinitionStateTransition;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StateTransitionsEntityMapper {

    private final TransitionEntityMapper transitionEntityMapper;

    public StateTransitionsEntityMapper(TransitionEntityMapper transitionEntityMapper) {
        this.transitionEntityMapper = transitionEntityMapper;
    }

    public StateTransitions map(StateTransitionsEntity entity) {
        StateTransitions stateTransitions = new StateTransitions();

        stateTransitions.setStateId(entity.getStateId());

        // Map transitions
        Map<Long, IStateTransition> transitions = new HashMap<>();
        for (TransitionEntity transitionEntity : entity.getTransitions()) {
            ActionDefinitionStateTransition transition = transitionEntityMapper.map(transitionEntity);
            transitions.put(transitionEntity.getId(), transition);
        }
        stateTransitions.setTransitions(transitions);

        // Map finish transition
        if (entity.getFinishTransition() != null) {
            stateTransitions.setTransitionFinish(transitionEntityMapper.map(entity.getFinishTransition()));
        }

        stateTransitions.setStaysVisibleAfterTransition(
                entity.getStaysVisibleAfterTransition() == IStateTransition.StaysVisible.TRUE
        );

        return stateTransitions;
    }

    public StateTransitionsEntity map(StateTransitions stateTransitions) {
        StateTransitionsEntity entity = new StateTransitionsEntity();

        entity.setStateId(stateTransitions.getStateId());

        // Map transitions
        entity.setTransitions(stateTransitions.getTransitions().values().stream()
                .map(transition -> transitionEntityMapper.map((ActionDefinitionStateTransition) transition))
                .collect(Collectors.toList()));

        // Map finish transition
        if (stateTransitions.getTransitionFinish() != null) {
            entity.setFinishTransition(transitionEntityMapper.map((ActionDefinitionStateTransition) stateTransitions.getTransitionFinish()));
        }

        entity.setStaysVisibleAfterTransition(
                stateTransitions.isStaysVisibleAfterTransition()
                        ? IStateTransition.StaysVisible.TRUE
                        : IStateTransition.StaysVisible.FALSE
        );

        return entity;
    }
}