package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildLive;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildStateStructure.ReplaceStateStructure;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildStateStructure.StateStructureInfo;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildStateStructure.UncheckedImageHunter;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.GetScreenObservation;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.SetUsableArea;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildLive.screenTransitions.FindScreen;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildLive.screenTransitions.FindTransition;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class BuildStateStructureFromGUI {

    private final GetScreenObservation getScreenObservation;
    private final Action action;
    private final ReplaceStateStructure replaceStateStructure;
    private final UncheckedImageHunter uncheckedImageHunter;
    private final StateStructureInfo stateStructureInfo;
    private final SetUsableArea setUsableArea;
    private final FindTransition findTransition;
    private final FindScreen findScreen;

    public BuildStateStructureFromGUI(GetScreenObservation getScreenObservation,
                                      Action action, ReplaceStateStructure replaceStateStructure,
                                      UncheckedImageHunter uncheckedImageHunter, StateStructureInfo stateStructureInfo,
                                      SetUsableArea setUsableArea, FindTransition findTransition, FindScreen findScreen) {
        this.getScreenObservation = getScreenObservation;
        this.action = action;
        this.replaceStateStructure = replaceStateStructure;
        this.uncheckedImageHunter = uncheckedImageHunter;
        this.stateStructureInfo = stateStructureInfo;
        this.setUsableArea = setUsableArea;
        this.findTransition = findTransition;
        this.findScreen = findScreen;
    }

    /**
     * Starts collecting screenshots and testing images for transitions. When it is finished, it
     * will have added screen observations and transitions. This needs to be run again until all images in
     * all screenshots have been checked.
     * The usable boundary is set by the inner boundaries of the included images.
     * This class can be used for live scraping or building with screenshots.
     */
    public void automateStateStructure(StateStructureConfiguration config) {
        Region usableArea = setUsableArea.getBoundariesFromExcludedImages(config);
        List<ScreenObservation> observations = new ArrayList<>();
        List<StatelessImage> statelessImages = new ArrayList<>();
        //action.perform(ActionOptions.Action.HIGHLIGHT, usableArea);
        findScreen.findCurrentScreenAndSaveIfNew(config, observations, statelessImages); // get the first ScreenObservation
        boolean uncheckedImages;
        // keep going, adding new observations and associated images, as long as you find a transition to a new, unique screen
        while (true) {
            findTransition.transitionAndObserve(config, observations, statelessImages);
            /* Once we've exhausted all images and not reached a new screen, we should check to see if there are
            unchecked images. If there are no unchecked images, we're finished traversing the application. If there are
            unchecked images, we should try to reach those screens with unchecked images and click them. In both cases,
            the next step is to create a state structure from our observed images.
             */
            replaceStateStructure.createNewStateStructure(usableArea, statelessImages);
            stateStructureInfo.printStateStructure();
            /*
            Once the new state structure has been created, we can go to states with unchecked images.
            1. Go to a state with unchecked images
            2. Continue this process of transitioning and observing
            3. When stuck, recreate the state structure
            4. Rinse and repeat until all images have been checked
             */
            Set<String> uncheckedStates = uncheckedImageHunter.getUncheckedStates(observations);
            uncheckedImages = !uncheckedStates.isEmpty();
            if (!uncheckedImages) {
                System.out.println("all images in all states have been checked.");
                return; // we should probably write the state structure in java or save it with persistence.
            }
            if (!uncheckedImageHunter.setActiveStatesAndGoToUncheckedState(uncheckedStates, observations)) {
                System.out.println("couldn't reach a state with unchecked images.");
                return;
            }
        }
    }


}
