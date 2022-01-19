package actions.composites.multipleActions;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.state.ObjectCollection;
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
