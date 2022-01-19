package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;

public interface FindImageObject {

    Matches find(ActionOptions actionOptions, StateImageObject stateImage);
}
