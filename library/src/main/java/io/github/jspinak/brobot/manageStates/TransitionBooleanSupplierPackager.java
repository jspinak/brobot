package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import org.springframework.stereotype.Component;

import java.util.function.BooleanSupplier;

@Component
public class TransitionBooleanSupplierPackager {
    private final Action action;

    public TransitionBooleanSupplierPackager(Action action) {
        this.action = action;
    }

    public BooleanSupplier toBooleanSupplier(IStateTransition transition) {
        if (transition instanceof JavaStateTransition) {
            return ((JavaStateTransition) transition).getTransitionFunction();
        } else if (transition instanceof ActionDefinitionStateTransition) {
            ActionDefinition actionDefinition = ((ActionDefinitionStateTransition) transition).getActionDefinition();
            return toBooleanSupplier(actionDefinition);
        } else {
            throw new IllegalArgumentException("Unsupported transition type");
        }
    }

    public BooleanSupplier toBooleanSupplier(ActionDefinition actionDefinition) {
        return () -> {
            for (ActionDefinition.ActionStep step : actionDefinition.getSteps()) {
                if (!action.perform(step.getOptions(), step.getObjects()).isEmpty()) {
                    return true;
                }
            }
            return false;
        };
    }

    public boolean getAsBoolean(IStateTransition transition) {
        return toBooleanSupplier(transition).getAsBoolean();
    }
}
