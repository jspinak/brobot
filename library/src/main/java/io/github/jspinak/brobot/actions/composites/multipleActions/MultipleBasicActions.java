package io.github.jspinak.brobot.actions.composites.multipleActions;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.BasicAction;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Helper function for Composite Actions
 */
@Component
public class MultipleBasicActions {

    private BasicAction basicAction;

    public MultipleBasicActions(BasicAction basicAction) {
        this.basicAction = basicAction;
    }

    public Matches perform(MultipleActionsObject mao) {
        Matches matches = new Matches();
        for (int i=0; i<mao.getTimesToRepeat(); i++) {
            for (ActionOptionsObjectCollectionPair aooc : mao.getAoocs()) {
                Optional<ActionInterface> action = basicAction.getAction(aooc.getActionOptions().getAction());
                if (action.isPresent())
                    matches = action.get().perform(aooc.getActionOptions(), aooc.getObjectCollection());
            }
        }
        return matches; // we should return the last Matches object
    }

}
