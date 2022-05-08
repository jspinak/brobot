package io.github.jspinak.brobot.actions.composites.multipleActions;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MultipleActionsObject {

    private List<ActionOptionsObjectCollectionPair> aoocs = new ArrayList<>();
    private int timesToRepeat = 1;

    public void addActionOptionsObjectCollectionPair(ActionOptions actionOptions, ObjectCollection objectCollection) {
        aoocs.add(new ActionOptionsObjectCollectionPair(actionOptions, objectCollection));
    }

    public void resetTimesActedOn() {
        aoocs.forEach(pair -> pair.getObjectCollection().resetTimesActedOn());
    }

    public void print() {
        System.out.println("__MultipleActionsObject__");
        aoocs.forEach(aooc -> System.out.println(
                aooc.getActionOptions().getAction()+"->"+aooc.getObjectCollection().toString()));
        System.out.println();
    }
}
