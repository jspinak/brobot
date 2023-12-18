package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenTransitions;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.ScreenObservationManager;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.TransitionImage;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

@Component
public class FindTransition {

    private final FindScreen findScreen;
    private final Action action;
    private final ScreenObservationManager screenObservationManager;

    public FindTransition(FindScreen findScreen, Action action, ScreenObservationManager screenObservationManager) {
        this.findScreen = findScreen;
        this.action = action;
        this.screenObservationManager = screenObservationManager;
    }

    /**
     * This is a real automation process and we can't just go to a specific screen. If there are screens with
     * unchecked images, the automation needs to find its way to those screens. The more transitions we check,
     * the more likely we will be able to direct the automation to a specific screen.
     */
    public void transitionAndObserve() {
        while (findTransition()) {
            System.out.println("transition found, active screenId = " + screenObservationManager.getCurrentScreenObservation());
        }
    }

    /**
     * Tries clicking all unchecked images until it finds a transition or has checked all images.
     * If it finds a transition, it saves the new observation and updates the pointers.
     * @return true if a transition occurred
     */
    public boolean findTransition() {
        System.out.println("FindTransition: start of method findTransition");
        int initialScreenId = screenObservationManager.getCurrentScreenId();
        ScreenObservation currentScreen = screenObservationManager.getCurrentScreenObservation();
        int currentScreenId;
        System.out.println("FindTransition: number of TransitionImage(s) in this screen = " + currentScreen.getImages().size());
        for (TransitionImage potentialLink : currentScreen.getUnvisitedImages()) {
            clickImage(potentialLink);
            findScreen.findCurrentScreenAndSaveIfNew();
            potentialLink.setChecked(true);
            currentScreenId = screenObservationManager.getCurrentScreenId();
            if (initialScreenId != currentScreenId) { // we transitioned to another screen
                // record the id of the screen to which this image transitions
                potentialLink.getFromScreenToScreen().put(initialScreenId, currentScreenId);
                return true;
            }
        }
        return false; // all images checked, no transitions
    }

    private void clickImage(TransitionImage image) {
        ObjectCollection match = new ObjectCollection.Builder()
                .withRegions(image.getRegion()) // Match(es) get added as Region(s) to an ObjectCollection
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setPauseAfterEnd(1.5)
                .build();
        action.perform(actionOptions, match);
    }

}
