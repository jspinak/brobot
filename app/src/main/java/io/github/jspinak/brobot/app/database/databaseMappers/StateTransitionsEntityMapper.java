package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.StateTransitionsEntity;
import io.github.jspinak.brobot.app.database.entities.TransitionEntity;
import io.github.jspinak.brobot.app.services.PatternService;
import io.github.jspinak.brobot.app.services.SceneService;
import io.github.jspinak.brobot.manageStates.ActionDefinitionStateTransition;
import io.github.jspinak.brobot.manageStates.IStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StateTransitionsEntityMapper {

    private final TransitionEntityMapper transitionEntityMapper;

    public StateTransitionsEntityMapper(TransitionEntityMapper transitionEntityMapper) {
        this.transitionEntityMapper = transitionEntityMapper;
    }

    public StateTransitions map(StateTransitionsEntity entity, SceneService sceneService, PatternService patternService) {
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(entity.getStateId());

        // Map transitions
        List<IStateTransition> transitions = new ArrayList<>();
        for (TransitionEntity transitionEntity : entity.getTransitions()) {
            ActionDefinitionStateTransition transition = transitionEntityMapper.map(transitionEntity, sceneService, patternService);
            transitions.add(transition);
        }
        stateTransitions.setTransitions(transitions);

        // Map finish transition
        if (entity.getFinishTransition() != null) {
            stateTransitions.setTransitionFinish(transitionEntityMapper.map(entity.getFinishTransition(), sceneService, patternService));
        }

        stateTransitions.setStaysVisibleAfterTransition(
                entity.getStaysVisibleAfterTransition() == IStateTransition.StaysVisible.TRUE
        );

        return stateTransitions;
    }

    public StateTransitionsEntity map(StateTransitions stateTransitions, SceneService sceneService, PatternService patternService) {
        StateTransitionsEntity entity = new StateTransitionsEntity();

        entity.setStateId(stateTransitions.getStateId());

        // Map transitions
        entity.setTransitions(stateTransitions.getTransitions().stream()
                .map(transition -> transitionEntityMapper.map((ActionDefinitionStateTransition) transition, sceneService, patternService))
                .collect(Collectors.toList()));

        // Map finish transition
        if (stateTransitions.getTransitionFinish() != null) {
            entity.setFinishTransition(transitionEntityMapper.map(
                    (ActionDefinitionStateTransition) stateTransitions.getTransitionFinish(), sceneService, patternService));
        }

        entity.setStaysVisibleAfterTransition(
                stateTransitions.isStaysVisibleAfterTransition()
                        ? IStateTransition.StaysVisible.TRUE
                        : IStateTransition.StaysVisible.FALSE
        );

        return entity;
    }
}