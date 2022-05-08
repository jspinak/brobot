package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;

public interface FindPatternInterface {

    Matches find(Region region, StateImageObject stateImageObject, Image image, ActionOptions actionOptions);
}
