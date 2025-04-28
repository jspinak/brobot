package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.dsl.ActionDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * This method is equivalent of TransitionEntity in the library module.
 * Transition in the library module is not converted to TransitionEntity in the app module.
 * TransitionEntity objects are created with the web UI.
 * If a transition is created in code, it stays a Transition class.
 * When created with the web UI, a TransitionEntity can then be converted to this class, which
 *   can be used by the library in the same way the Transition class can be used (both are IStateTransition).
 */
@Getter
@Setter
public class ActionDefinitionStateTransition implements IStateTransition {
    private ActionDefinition actionDefinition;

    private StaysVisible staysVisibleAfterTransition;
    private Set<Long> activate = new HashSet<>();
    private Set<Long> exit = new HashSet<>();
    private int score = 0;
    private int timesSuccessful = 0;

    @Override
    public Optional<ActionDefinition> getActionDefinition() {
        return Optional.ofNullable(actionDefinition);
    }

    @Override
    public String toString() {
        return "ActionDefinitionStateTransition{" +
                ", activate=" + activate +
                ", exit=" + exit +
                '}';
    }
}
