package io.github.jspinak.brobot.actions.actionExecution.manageTrainingData;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.trainingData.ActionVector;

/**
 * There can be different ways to translate an ActionOption variable to a vector. If you want a different
 * translation, feel free to add an implementation of this interface.
 */
public interface ActionVectorTranslation {

    ActionVector toVector(Matches matches);
    ActionOptions toActionOptions(ActionVector actionVector);
}
