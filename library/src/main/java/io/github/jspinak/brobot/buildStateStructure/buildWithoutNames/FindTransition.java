package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FindTransition {

    private final ScreenObservations screenObservations;
    private final FindScreen findScreen;
    private final Action action;

    public FindTransition(ScreenObservations screenObservations, FindScreen findScreen, Action action) {
        this.screenObservations = screenObservations;
        this.findScreen = findScreen;
        this.action = action;
    }

    /**
     * This is a real automation process and we can't just go to a specific screen. If there are screens with
     * unchecked images, the automation needs to find its way to those screens. The more transitions we check,
     * the more likely we will be able to direct the automation to a specific screen.
     */
    public void transitionAndObserve(int currentScreenId, int nextId, double minSimilarityImages) {
        while (findTransition(currentScreenId, nextId, minSimilarityImages)) {
            System.out.println("Transition found on screen " + currentScreenId);
            nextId++;
        }
    }

    /**
     * Tries clicking all unchecked images until it finds a transition or has checked all images.
     * If it finds a transition, it saves the new observation and updates the pointers.
     * @return true if a transition occurred
     */
    public boolean findTransition(int currentScreenId, int nextId, double minSimilarityImages) {
        Optional<ScreenObservation> optScrObs = screenObservations.get(currentScreenId);
        if (optScrObs.isEmpty()) return false; // screen with this id doesn't exist
        ScreenObservation screenObservation = optScrObs.get();
        List<TransitionImage> unvisitedImages = screenObservation.getUnvisitedImages();
        for (TransitionImage transitionImage : unvisitedImages) {
            clickImage(transitionImage); // click on an unvisited image
            int observedScreenId = findScreen.findCurrentScreen(nextId, minSimilarityImages); // nextId if new; otherwise, the active screen's id
            transitionImage.setChecked(true); // mark the TransitionImage as checked
            if (observedScreenId != currentScreenId) { // we transitioned
                transitionImage.getFromScreenToScreen().put(currentScreenId, observedScreenId); // record the id of the screen to which this image transitions
                return true;
            }
        }
        return false; // all images checked, no transitions
    }

    private void clickImage(TransitionImage image) {
        ObjectCollection match = new ObjectCollection.Builder()
                .withMatches(image.getMatch()) // Match(es) get added as Region(s) to an ObjectCollection
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setPauseAfterEnd(1.5)
                .build();
        action.perform(actionOptions, match);
    }

}
