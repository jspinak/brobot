package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * This class should be called after all TransitionImage(s) have been created. It finds these images
 * on the set of screens provided.
 */
@Component
public class FindAllScreensForTransitionImages {

    private final TransitionImageRepo transitionImageRepo;
    private final ScreenObservations screenObservations;
    private final Action action;

    public FindAllScreensForTransitionImages(TransitionImageRepo transitionImageRepo,
                                             ScreenObservations screenObservations,
                                             Action action) {
        this.transitionImageRepo = transitionImageRepo;
        this.screenObservations = screenObservations;
        this.action = action;
    }

    /**
     * Finds TransitionImage(s) in all ScreenObservation(s), anywhere on the screen.
     */
    public void findScreens() {
        transitionImageRepo.getImages().forEach(transitionImage -> screenObservations.getAll().forEach(
                screenObs -> {
                    ObjectCollection objColl = new ObjectCollection.Builder()
                            .withPatterns(transitionImage.asPattern())
                            .withScenes(screenObs.getPattern())
                            .build();
                    if (action.perform(ActionOptions.Action.FIND, objColl).isSuccess())
                        transitionImage.addScreenFound(screenObs.getId());
                })
        );
    }
}
