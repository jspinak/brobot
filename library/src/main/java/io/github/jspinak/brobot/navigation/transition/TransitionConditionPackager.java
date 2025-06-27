package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Converts various transition types into executable BooleanSupplier functions.
 * 
 * <p>TransitionConditionPackager serves as an adapter that unifies different transition 
 * implementations (JavaStateTransition and ActionDefinitionStateTransition) into a common 
 * executable format. This allows the transition execution engine to work with transitions 
 * regardless of whether they're defined in code or through declarative action definitions.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li><b>Type Adaptation</b>: Converts StateTransition implementations to BooleanSupplier</li>
 *   <li><b>Action Execution</b>: Orchestrates multi-step ActionDefinitions</li>
 *   <li><b>Success Evaluation</b>: Determines overall success based on final step</li>
 *   <li><b>Sequential Processing</b>: Executes action steps in order without conditional logic</li>
 * </ul>
 * </p>
 * 
 * <p>ActionDefinition execution strategy:
 * <ol>
 *   <li>Execute all steps except the last one (side effects only)</li>
 *   <li>Execute the final step and evaluate its success</li>
 *   <li>Return the success of only the final step</li>
 *   <li>This assumes no conditional logic between steps</li>
 * </ol>
 * </p>
 * 
 * <p>Design assumptions for ActionDefinitions:
 * <ul>
 *   <li>Steps are independent - no step depends on previous step's success</li>
 *   <li>All steps should execute regardless of individual failures</li>
 *   <li>Only the final step determines overall transition success</li>
 *   <li>Intermediate steps are for setup or side effects</li>
 * </ul>
 * </p>
 * 
 * <p>Example ActionDefinition pattern:
 * <pre>
 * Step 1: Click "Open Menu" button (setup action)
 * Step 2: Click "Settings" option (navigation action)
 * Step 3: Find "Settings Page" header (verification action)
 * 
 * Only Step 3's success determines if transition succeeded
 * </pre>
 * </p>
 * 
 * <p>Integration with transition types:
 * <ul>
 *   <li><b>JavaStateTransition</b>: Simply extracts existing BooleanSupplier</li>
 *   <li><b>ActionDefinitionStateTransition</b>: Builds BooleanSupplier from action steps</li>
 *   <li><b>Future Types</b>: Can be extended for new transition implementations</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, this packager enables seamless integration of different 
 * automation styles - from programmatic transitions written in Java to declarative workflows 
 * defined through visual tools. This flexibility allows teams to choose the most appropriate 
 * representation for each transition while maintaining a unified execution model.</p>
 * 
 * @since 1.0
 * @see StateTransition
 * @see JavaStateTransition
 * @see TaskSequenceStateTransition
 * @see TaskSequence
 * @see BooleanSupplier
 */
@Component
public class TransitionConditionPackager {
    private final Action action;

    public TransitionConditionPackager(Action action) {
        this.action = action;
    }

    /**
     * Converts a StateTransition to an executable BooleanSupplier.
     * <p>
     * Handles polymorphic dispatch based on transition type:
     * <ul>
     *   <li>JavaStateTransition: Extracts existing function</li>
     *   <li>ActionDefinition-based: Builds function from steps</li>
     * </ul>
     *
     * @param transition StateTransition to convert
     * @return BooleanSupplier that executes the transition logic
     * @throws IllegalArgumentException if transition type is unsupported
     */
    public BooleanSupplier toBooleanSupplier(StateTransition transition) {
        if (transition instanceof JavaStateTransition) {
            return ((JavaStateTransition) transition).getTransitionFunction();
        } else {
            return transition.getTaskSequenceOptional()
                    .map(this::toBooleanSupplier)
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported transition type"));
        }
    }

    /**
     * Converts an ActionDefinition into an executable BooleanSupplier.
     * <p>
     * Creates a function that executes all action steps sequentially, with only
     * the final step's success determining the overall result. This design supports
     * multi-step transitions where intermediate steps perform setup or navigation
     * actions, and the final step verifies arrival at the target state.
     * <p>
     * Execution behavior:
     * <ul>
     *   <li>All steps execute regardless of individual success/failure</li>
     *   <li>No conditional logic between steps</li>
     *   <li>Only the last step's success is evaluated</li>
     *   <li>Empty definitions return false</li>
     * </ul>
     * <p>
     * Example usage pattern:
     * <pre>
     * ActionDefinition transition = new ActionDefinition();
     * transition.addStep(click("Menu"));      // Setup - success ignored
     * transition.addStep(click("Settings"));  // Navigate - success ignored  
     * transition.addStep(find("SettingsPage")); // Verify - determines success
     * </pre>
     *
     * @param actionDefinition Definition containing ordered action steps
     * @return BooleanSupplier that executes all steps and returns final step success
     */
    public BooleanSupplier toBooleanSupplier(TaskSequence actionDefinition) {
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

    /**
     * Executes a transition and returns its success status.
     * <p>
     * Convenience method that converts and immediately executes a transition.
     * Useful when you need a one-time execution without storing the supplier.
     *
     * @param transition StateTransition to execute
     * @return true if transition succeeded, false otherwise
     * @see #toBooleanSupplier(StateTransition)
     */
    public boolean getAsBoolean(StateTransition transition) {
        return toBooleanSupplier(transition).getAsBoolean();
    }
}
