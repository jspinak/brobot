package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.stateStructureBuildManagement;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure.ScreenStateCreator;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure.StateStructureInfo;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.buildStateStructure.UncheckedImageHunter;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations.*;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenTtansitions.FindScreen;
import io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenTtansitions.FindTransition;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BuildStateStructureWithoutNames {

    private final GetScreenObservation getScreenObservation;
    private final ScreenObservations screenObservations;
    private final Action action;
    private final TransitionImageRepo transitionImageRepo;
    private final ScreenStateCreator screenStateCreator;
    private final UncheckedImageHunter uncheckedImageHunter;
    private final StateStructureInfo stateStructureInfo;
    private final GetUsableArea getUsableArea;
    private final FindTransition findTransition;
    private final FindScreen findScreen;

    public BuildStateStructureWithoutNames(GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                                           Action action, TransitionImageRepo transitionImageRepo, ScreenStateCreator screenStateCreator,
                                           UncheckedImageHunter uncheckedImageHunter, StateStructureInfo stateStructureInfo,
                                           GetUsableArea getUsableArea, FindTransition findTransition, FindScreen findScreen) {
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.action = action;
        this.transitionImageRepo = transitionImageRepo;
        this.screenStateCreator = screenStateCreator;
        this.uncheckedImageHunter = uncheckedImageHunter;
        this.stateStructureInfo = stateStructureInfo;
        this.getUsableArea = getUsableArea;
        this.findTransition = findTransition;
        this.findScreen = findScreen;
    }

    /**
     * Starts collecting screenshots and testing images for transitions. When it is finished, it
     * will have added screen observations and transitions. This needs to be run again until all images in
     * all screenshots have been checked.
     * The usable boundary is set by the inner boundaries of the included images.
     */
    public void automateStateStructure(Image topLeftBoundary, Image bottomRightBoundary) {
        Region usableArea = getUsableArea.getBoundariesFromExcludedImages(topLeftBoundary, bottomRightBoundary);
        action.perform(ActionOptions.Action.HIGHLIGHT, usableArea);
        getScreenObservation.setUsableArea(usableArea);
        System.out.println("BuildStateStructureWithoutNames: before findScreen");
        findScreen.findCurrentScreenAndSaveIfNew(); // get the first ScreenObservation
        System.out.println("BuildStateStructureWithoutNames: after findScreen");
        boolean uncheckedImages = true;
        // keep going, adding new observations and associated images, as long as you find a transition to a new, unique screen
        while (true) {
            findTransition.transitionAndObserve();
            /* Once we've exhausted all images and not reached a new screen, we should check to see if there are
            unchecked images. If there are no unchecked images, we're finished traversing the application. If there are
            unchecked images, we should try to reach those screens with unchecked images and click them. In both cases,
            the next step is to create a state structure from our observed images.
             */
            screenStateCreator.createAndSaveStatesAndTransitions();
            stateStructureInfo.printStateStructure();
            /*
            Once the new state structure has been created, we can go to states with unchecked images.
            1. Go to a state with unchecked images
            2. Continue this process of transitioning and observing
            3. When stuck, recreate the state structure
            4. Rinse and repeat until all images have been checked
             */
            Set<String> uncheckedStates = uncheckedImageHunter.getUncheckedStates();
            uncheckedImages = !uncheckedStates.isEmpty();
            if (!uncheckedImages) {
                System.out.println("all images in all states have been checked.");
                return; // we should probably write the state structure in java or save it with persistence.
            }
            if (!uncheckedImageHunter.setActiveStatesAndGoToUncheckedState(uncheckedStates)) {
                System.out.println("couldn't reach a state with unchecked images.");
                return;
            }
        }
    }


}
