package io.github.jspinak.brobot.actions.composites.multipleActions;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

@Component
public class MultipleActions {

    private Action action;

    public MultipleActions(Action action) {
        this.action = action;
    }

    public Matches perform(MultipleActionsObject mao) {
        Matches matches = new Matches();
        for (int i=0; i<mao.getTimesToRepeat(); i++) {
            for (ActionOptionsObjectCollectionPair aooc : mao.getAoocs()) {
                System.out.println(aooc.getActionOptions().getAction()+"->"+aooc.getObjectCollection());
                //action.perform(new ActionOptions.Builder().setAction(ActionOptions.Action.HIGHLIGHT).build(), aooc.getObjectCollection());
                matches = action.perform(aooc.getActionOptions(), aooc.getObjectCollection());
            }
        }
        return matches; // we should return the last Matches object
    }
}
