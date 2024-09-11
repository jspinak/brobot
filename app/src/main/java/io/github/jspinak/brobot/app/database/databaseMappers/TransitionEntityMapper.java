package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.TransitionEntity;
import io.github.jspinak.brobot.manageStates.ActionDefinitionStateTransition;
import io.github.jspinak.brobot.manageStates.IStateTransition;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.manageStates.IStateTransition.StaysVisible.FALSE;
import static io.github.jspinak.brobot.manageStates.IStateTransition.StaysVisible.TRUE;

@Component
public class TransitionEntityMapper {
    private final ActionDefinitionEntityMapper actionDefinitionEntityMapper;

    public TransitionEntityMapper(ActionDefinitionEntityMapper actionDefinitionEntityMapper) {
        this.actionDefinitionEntityMapper = actionDefinitionEntityMapper;
    }

    public ActionDefinitionStateTransition map(TransitionEntity transitionEntity) {
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        transition.setActionDefinition(actionDefinitionEntityMapper.map(transitionEntity.getActionDefinition()));

        // Map the StaysVisible enum
        transition.setStaysVisibleAfterTransition(mapStaysVisibleToActionDefinition(transitionEntity.getStaysVisibleAfterTransition()));

        transition.setActivate(transitionEntity.getStatesToEnter());
        transition.setExit(transitionEntity.getStatesToExit());
        transition.setScore(transitionEntity.getScore());
        transition.setTimesSuccessful(transitionEntity.getTimesSuccessful());

        return transition;
    }

    private ActionDefinitionStateTransition.StaysVisible mapStaysVisibleToActionDefinition(IStateTransition.StaysVisible entityStaysVisible) {
        if (entityStaysVisible == null) {
            return ActionDefinitionStateTransition.StaysVisible.NONE;
        }
        switch (entityStaysVisible) {
            case TRUE:
                return TRUE;
            case FALSE:
                return FALSE;
            default:
                return ActionDefinitionStateTransition.StaysVisible.NONE;
        }
    }

    public TransitionEntity map(ActionDefinitionStateTransition actionDefinitionStateTransition) {
        TransitionEntity entity = new TransitionEntity();
        entity.setActionDefinition(actionDefinitionEntityMapper.map(actionDefinitionStateTransition.getActionDefinition()));

        // Map the StaysVisible enum
        entity.setStaysVisibleAfterTransition(mapStaysVisibleToIState(actionDefinitionStateTransition.getStaysVisibleAfterTransition()));

        entity.setStatesToEnter(actionDefinitionStateTransition.getActivate());
        entity.setStatesToExit(actionDefinitionStateTransition.getExit());
        entity.setScore(actionDefinitionStateTransition.getScore());
        entity.setTimesSuccessful(actionDefinitionStateTransition.getTimesSuccessful());

        return entity;
    }

    private IStateTransition.StaysVisible mapStaysVisibleToIState(ActionDefinitionStateTransition.StaysVisible staysVisible) {
        if (staysVisible == null) {
            return IStateTransition.StaysVisible.NONE;
        }
        switch (staysVisible) {
            case TRUE:
                return IStateTransition.StaysVisible.TRUE;
            case FALSE:
                return IStateTransition.StaysVisible.FALSE;
            default:
                return IStateTransition.StaysVisible.NONE;
        }
    }
}