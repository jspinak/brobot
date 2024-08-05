package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.app.buildWithoutNames.buildLive.ScreenObservations;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.FIND;

/**
 * This class should be called after all TransitionImage(s) have been created. It finds these images
 * on the set of screens provided.
 */
@Component
public class FindAllScreensForStatelessImages {

    private final StatelessImageOps statelessImageOps;
    private final ScreenObservations screenObservations;
    private final Action action;

    public FindAllScreensForStatelessImages(StatelessImageOps statelessImageOps,
                                            ScreenObservations screenObservations,
                                            Action action) {
        this.statelessImageOps = statelessImageOps;
        this.screenObservations = screenObservations;
        this.action = action;
    }

    /**
     * Finds StatelessImage(s) in all ScreenObservation(s), anywhere on the screen.
     * Each statelessImage should already have a list of screens from the methods that compare images.
     */
    public void findScreens(List<StatelessImage> statelessImages, List<ScreenObservation> observations) {
        for (StatelessImage sI : statelessImages) {
            for (ScreenObservation sO : observations) {
                ObjectCollection objColl = new ObjectCollection.Builder()
                        .withPatterns(sI.toPattern())
                        .withScenes(sO.getPattern())
                        .build();
                if (action.perform(FIND, objColl).isSuccess()) {
                    sI.addScreenFound(sO.getPattern());
                    System.out.print(sI.getName()+" found in ");
                    sI.getScreensFound().forEach(screen -> System.out.print(screen.getName()+" "));
                }
                System.out.println();
            }
        }
    }
}
