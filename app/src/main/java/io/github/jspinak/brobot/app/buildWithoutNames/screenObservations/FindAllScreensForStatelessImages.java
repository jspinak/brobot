package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.FIND;

/**
 * This class should be called after all TransitionImage(s) have been created. It finds these images
 * on the set of screens provided.
 */
@Component
public class FindAllScreensForStatelessImages {

    private final StatelessImageRepo statelessImageRepo;
    private final ScreenObservations screenObservations;
    private final Action action;

    public FindAllScreensForStatelessImages(StatelessImageRepo statelessImageRepo,
                                            ScreenObservations screenObservations,
                                            Action action) {
        this.statelessImageRepo = statelessImageRepo;
        this.screenObservations = screenObservations;
        this.action = action;
    }

    /**
     * Finds StatelessImage(s) in all ScreenObservation(s), anywhere on the screen.
     */
    public void findScreens() {
        for (StatelessImage sI : statelessImageRepo.getStatelessImages()) {
            for (ScreenObservation sO : screenObservations.getAll()) {
                ObjectCollection objColl = new ObjectCollection.Builder()
                        .withPatterns(sI.toPattern())
                        .withScenes(sO.getPattern())
                        .build();
                if (action.perform(FIND, objColl).isSuccess()) {
                    sI.addScreenFound(sO.getId());
                    System.out.println(sI.getName()+" found in "+sI.getScreensFound());
                }
            }
        }
    }
}
