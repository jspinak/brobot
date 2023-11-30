package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.bytedeco.opencv.global.opencv_core.absdiff;

@Component
public class GetScreenObservations {

    private final GetScreenObservation getScreenObservation;
    private final ScreenObservations screenObservations;
    private final Action action;
    private final TransitionImageRepo transitionImageRepo;
    private final ScreenStateCreator screenStateCreator;
    private final UncheckedImageHunter uncheckedImageHunter;
    private final StateStructureInfo stateStructureInfo;
    private final GetUsableArea getUsableArea;

    private int minimumDifference = 10;
    private int currentScreenId = 0;
    private int nextId = 0; // the id represents the next id number used to initialize an observation
    private double minSimilarityImages = .95;

    public GetScreenObservations(GetScreenObservation getScreenObservation, ScreenObservations screenObservations,
                                 Action action, TransitionImageRepo transitionImageRepo, ScreenStateCreator screenStateCreator,
                                 UncheckedImageHunter uncheckedImageHunter, StateStructureInfo stateStructureInfo,
                                 GetUsableArea getUsableArea) {
        this.getScreenObservation = getScreenObservation;
        this.screenObservations = screenObservations;
        this.action = action;
        this.transitionImageRepo = transitionImageRepo;
        this.screenStateCreator = screenStateCreator;
        this.uncheckedImageHunter = uncheckedImageHunter;
        this.stateStructureInfo = stateStructureInfo;
        this.getUsableArea = getUsableArea;
    }

    /**
     * Starts collecting screenshots and testing images for transitions. When it is finished, it
     * will have added screen observations and transitions. This needs to be run again until all images in
     * all screenshots have been checked.
     * The usable boundary is set by the inner boundaries of the included images.
     */
    public void automateStateStructure(String topLeftBoundary, String bottomRightBoundary) {
        Region usableArea = getUsableArea.getBoundariesFromExcludedImages(topLeftBoundary, bottomRightBoundary);
        action.perform(ActionOptions.Action.HIGHLIGHT, usableArea);
        getScreenObservation.setUsableArea(usableArea);
        ScreenObservation startingObservation = getScreenObservation.takeScreenshotAndGetImages(nextId);
        screenObservations.addScreenObservation(nextId, startingObservation);
        transitionImageRepo.addUniqueImagesToRepo(startingObservation, minSimilarityImages);
        currentScreenId = nextId; // currentScreenId is the id of the active screen
        nextId++;
        boolean uncheckedImages = true;
        // keep going, adding new observations and associated images, as long as you find a transition to a new, unique screen
        while (true) {
            transitionAndObserve();
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
            if (!uncheckedImageHunter.setActiveStatesAndGoToUncheckedState(currentScreenId, uncheckedStates)) {
                System.out.println("couldn't reach a state with unchecked images.");
                return;
            }
            currentScreenId = uncheckedImageHunter.getScreenIdFromActiveStates();
            if (currentScreenId < 0) currentScreenId = findCurrentScreen(); // if the state list didn't give a valid screen, try to recognize the screen visually
        }
    }

    /**
     * This is a real automation process and we can't just go to a specific screen. If there are screens with
     * unchecked images, the automation needs to find its way to those screens. The more transitions we check,
     * the more likely we will be able to direct the automation to a specific screen.
     */
    private void transitionAndObserve() {
        while (findTransition()) {
            System.out.println("Transition found on screen " + currentScreenId);
        }
    }

    /**
     * Tries clicking all unchecked images until it finds a transition or has checked all images.
     * If it finds a transition, it saves the new observation and updates the pointers.
     * @return true if a transition occurred
     */
    public boolean findTransition() {
        Optional<ScreenObservation> optScrObs = screenObservations.get(currentScreenId);
        if (optScrObs.isEmpty()) return false; // screen with this id doesn't exist
        ScreenObservation screenObservation = optScrObs.get();
        List<TransitionImage> unvisitedImages = screenObservation.getUnvisitedImages();
        for (TransitionImage transitionImage : unvisitedImages) {
            clickImage(transitionImage); // click on an unvisited image
            int observedScreenId = findCurrentScreen(); // newId if new; otherwise, the active screen's id
            transitionImage.setChecked(true); // mark the TransitionImage as checked
            if (observedScreenId != currentScreenId) { // we transitioned
                transitionImage.getFromScreenToScreen().put(currentScreenId, observedScreenId); // record the id of the screen to which this image transitions
                currentScreenId = observedScreenId;
                return true;
            }
        }
        return false; // all images checked, no transitions
    }

    /**
     * Takes a new screenshot and compares with the screenshots in the repo. If new, adds it to the repo.
     * If in the repo, returns the screen's id.
     * @return the id of the current screen
     */
    private int findCurrentScreen() {
        ScreenObservation newObservation = getScreenObservation.takeScreenshotAndGetImages(nextId); // get a new screenshot
        int screenId = getScreenId(newObservation, minimumDifference); // compare to previous screenshots
        if (screenId < 0) { // screen hasn't been seen before
            screenObservations.addScreenObservation(nextId, newObservation); // add screen to repo
            transitionImageRepo.addUniqueImagesToRepo(newObservation, minSimilarityImages);
            nextId++;
            return newObservation.getId();
        }
        return screenId; // screen is not new
    }

    /**
     * The new screenshot taken is compared to all other screenshots taken.
     * @return active screen's id if found; otherwise -1
     */
    private int getScreenId(ScreenObservation newObservation, int minimumDifference) {
        for (ScreenObservation obs : screenObservations.getAll().values()) {
            Mat dist = new Mat(newObservation.getScreenshot().size(), newObservation.getScreenshot().type()); //CV_64FC3);
            absdiff(obs.getScreenshot(), newObservation.getScreenshot(), dist);
            Scalar sum = opencv_core.sumElems(dist);
            System.out.println("sum of dist is " + sum);
            if (sum.get() < minimumDifference) {
                return obs.getId();
            }
        }
        return -1;
    }

    private void clickImage(TransitionImage image) {
        ObjectCollection match = new ObjectCollection.Builder()
                .withMatches(image.getMatch()) // Match(es) get added as Region(s) to an ObjectCollection
                .build();
        action.perform(ActionOptions.Action.CLICK, match);
    }

}
