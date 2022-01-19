package actions.methods.basicactions.find;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;

public interface FindImageObject {

    Matches find(ActionOptions actionOptions, StateImageObject stateImage);
}
