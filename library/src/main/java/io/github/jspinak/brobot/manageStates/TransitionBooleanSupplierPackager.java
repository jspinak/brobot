package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.dsl.ActionStep;
import org.springframework.stereotype.Component;

import java.util.List;
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
        } else {
            return transition.getActionDefinition()
                    .map(this::toBooleanSupplier)
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported transition type"));
        }
    }

    /**
     * The ActionDefinition field of ActionDefinitionStateTransition contains a list of ActionStep objects,
     * which contain ActionOptions and ObjectCollections. These two variables can be passed as parameters
     * to the method 'perform' of the class Action (action.perform), which returns a Matches object.
     * The Matches object can return a boolean with the method isSuccess(); (matches.isSuccess()).
     * However, since there is a list of ActionStep objects, we should evaluate the success of all
     * ActionSteps objects as a whole. This requires some assumptions when creating an ActionDefinition;
     * specifically, that there is no conditional logic and that each ActionStep is run regardless of
     * whether it succeeds or fails. An example could be the following 3 ActionStep objects:
     * 1. click a button,
     * 2. click another button, and
     * 3. find an image.
     * The only ActionStep that should be evaluated for success is the last one.
     *
     * @param actionDefinition The actionDefinition to convert to a BooleanSupplier
     * @return the BooleanSupplier equivalent
     */
    public BooleanSupplier toBooleanSupplier(ActionDefinition actionDefinition) {
        return () -> {
            List<ActionStep> steps = actionDefinition.getSteps();
            if (steps.isEmpty()) {
                System.out.println("No steps in ActionDefinition");
                return false;
            }

            // Perform all steps except the last one without evaluating success
            for (int i = 0; i < steps.size() - 1; i++) {
                ActionStep step = steps.get(i);
                System.out.println("Executing step " + (i+1) + " of " + steps.size());
                action.perform(step.getActionOptions(), step.getObjectCollection());
            }

            // Evaluate only the last step
            ActionStep lastStep = steps.get(steps.size() - 1);
            System.out.println("Executing final step " + steps.size() + " of " + steps.size());
            System.out.println("Last step options: " + lastStep.getActionOptions());
            System.out.println("Last step objects: " + lastStep.getObjectCollection());
            return action.perform(lastStep.getActionOptions(), lastStep.getObjectCollection()).isSuccess();
        };
    }

    public boolean getAsBoolean(IStateTransition transition) {
        return toBooleanSupplier(transition).getAsBoolean();
    }
}
