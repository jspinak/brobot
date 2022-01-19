package actions.methods.sikuliWrappers.find;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.image.Image;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;

public interface FindPatternInterface {

    Matches find(Region region, StateImageObject stateImageObject, Image image, ActionOptions actionOptions);
}
