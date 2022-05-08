package io.github.jspinak.brobot.actions.composites.multipleActions;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;

@Getter
public class ActionOptionsObjectCollectionPair {

    private ActionOptions actionOptions;
    private ObjectCollection objectCollection;

    public ActionOptionsObjectCollectionPair(ActionOptions actionOptions, ObjectCollection objectCollection) {
        this.actionOptions = actionOptions;
        this.objectCollection = objectCollection;
    }
}
