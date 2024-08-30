package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
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

    private final Action action;

    public FindAllScreensForStatelessImages(Action action) {
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
                        .withScenes(sO.getScene())
                        .build();
                if (action.perform(FIND, objColl).isSuccess()) {
                    sI.getScenesFound().add(sO.getScene());
                    System.out.print(sI.getName()+" found in ");
                    sI.getScenesFound().forEach(screen -> System.out.print(screen.getPattern().getName()+" "));
                }
                System.out.println();
            }
        }
    }
}
