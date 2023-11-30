package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.actions.customActions.CommonActions;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.manageStates.StateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.services.StateService;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ScreenStateCreator {

    private final TransitionImageRepo transitionImageRepo;
    private final ScreenObservations screenObservations;
    private final StateService stateService;
    private final CommonActions commonActions;
    private final StateTransitionsRepository stateTransitionsRepository;

    public ScreenStateCreator(TransitionImageRepo transitionImageRepo, ScreenObservations screenObservations,
                              StateService stateService, CommonActions commonActions,
                              StateTransitionsRepository stateTransitionsRepository) {
        this.transitionImageRepo = transitionImageRepo;
        this.screenObservations = screenObservations;
        this.stateService = stateService;
        this.commonActions = commonActions;
        this.stateTransitionsRepository = stateTransitionsRepository;
    }

    /**
     * Once all scraping is done, states can be created from the TransitionImageRepo.
     * To do this, we need to figure out the minimum combinations of images. For example, screens
     * a(1,2,3,4,5,6), b(1,2,3,4,6), c(1,2,3), d(4,5,6) would produce states i(1,2,3), ii(4,6), and iii(5).
     * Images can be classified by identifying them in screens: 1(a,b,c), 2(a,b,c), 3(a,b,c), 4(a,b,d), 5(a,d), 6(a,b,d).
     * When set of screens match, the corresponding images belong together in a state.
     * States = i(a,b,c;1,2,3), ii(a,b,d;4,6), iii(a,d; 5).
     */
    public List<ImageSetsAndAssociatedScreens> defineStatesWithImages() {
        List<ImageSetsAndAssociatedScreens> imagesScreens = new ArrayList<>();
        int size = transitionImageRepo.images.size();
        for (int i = 0; i < size; i++) {
            TransitionImage img = transitionImageRepo.images.get(i);
            addToImageSet(imagesScreens, img, i);
        }
        return imagesScreens;
    }

    /**
     * Set the state transitions using the image sets and associated screens.
     * If image 3 is a transition from screen c to d, image 3 transitions from state i to states {ii, iii}.
     * 1. screens are given sets of states. a(i,ii,iii), b(i,ii), c(i), d(ii,iii)
     * 2. get the from and to screens for an image
     * 3. the difference in states gives the effect of the transition. states can vanish as well as appear.
     * The same image can be found in multiple screens and be responsible for different transitions. The way to
     *      represent this accurately is by creating separate images for different transitions. This will also
     *      change the states and require rerunning the ImageSets algorithm. To keep things simple, I'm going to
     *      ignore this initially and only use the first transition. This will make the model less accurate but
     *      should be ok in most situations.
     */
    private void setScreenStates(List<ImageSetsAndAssociatedScreens> imgSetsList) {
        for (ScreenObservation screenObservation : screenObservations.getAll().values()) {
            for (ImageSetsAndAssociatedScreens imgSets : imgSetsList) {
                // if the screen is part of the ImageSets, include it as a state in the screen
                if (imgSets.getScreens().contains(screenObservation.getId())) {
                    screenObservation.addState(imgSetsList.indexOf(imgSets));
                }
            }
        }
    }

    /**
     * 1. get the from and to screens for an image
     * @param sio StateImageObject
     */
    private void setTransitionStatesToExitAndEnter(StateImageObject sio) {
        TransitionImage img = sio.getTransitionImage();
        if (img.getTransitionsTo().isEmpty()) return; // no transitions
        int fromIndex = img.getScreensFound().get(0); // get the correct screen number
        Optional<ScreenObservation> fromScreen = screenObservations.get(fromIndex); // for simplicity, i'm using the first screen the image was recorded on
        if (fromScreen.isEmpty()) return; // this screen doesn't exist in the repo
        ScreenObservation from = fromScreen.get();
        int toIndex = img.getFromScreenToScreen().get(fromIndex);
        Optional<ScreenObservation> toScreen = screenObservations.get(toIndex);
        if (toScreen.isEmpty()) return; // this screen doesn't exist in the repo
        ScreenObservation to = toScreen.get();

        // Find elements in a that are not in b
        Set<Integer> elementsInFromOnly = new HashSet<>(from.getStates());
        elementsInFromOnly.removeAll(to.getStates());
        elementsInFromOnly.remove(fromIndex); // it's exited automatically unless specified otherwise
        Set<String> toExit = new HashSet<>();
        for (Integer i : elementsInFromOnly) toExit.add(Integer.toString(i));
        sio.setStatesToExit(toExit);

        // Find elements in b that are not in a
        Set<Integer> elementsInToOnly = new HashSet<>(to.getStates());
        elementsInToOnly.removeAll(from.getStates());
        if (to.getStates().contains(fromIndex)) elementsInToOnly.add(fromIndex); // if the owner state doesn't disappear, you need to specify this in the transition (it's not the default)
        Set<String> toEnter = new HashSet<>();
        for (Integer i : elementsInToOnly) toEnter.add(Integer.toString(i));
        sio.setStatesToEnter(toEnter);
    }

    private void addToImageSet(List<ImageSetsAndAssociatedScreens> imagesScreens,
                               TransitionImage transitionImage, int pos) {
        for (ImageSetsAndAssociatedScreens imgScr : imagesScreens) {
            if (imgScr.ifSameScreensAddImage(transitionImage, pos)) {
                // ImageSetsAndAssociatedScreens are a preliminary representation of states, and the created state's name will be the imgScr's index
                transitionImage.setOwnerState(imagesScreens.indexOf(imgScr));
                return;
            }
        }
        imagesScreens.add(new ImageSetsAndAssociatedScreens(pos, transitionImage.getScreensFound()));
        transitionImage.setOwnerState(imagesScreens.size()-1); // the state name is the last index added
    }

    public State createState(ImageSetsAndAssociatedScreens imageSets, String name) {
        List<StateImageObject> stateImages = new ArrayList<>();
        imageSets.getImages().forEach(imgIndex -> {
            Image newImage = new Image(transitionImageRepo.images.get(imgIndex).getMatch().getImage());
            StateImageObject sio = new StateImageObject.Builder()
                    .withImage(newImage)
                    /*
                    This gives us the transition to the screenshot, not to specific states.
                    To get the states, compare the states in the from-screenshot and the to-screenshot.
                    State differences between the two can be recorded in the transition function.
                     */
                    .withTransitionImage(transitionImageRepo.images.get(imgIndex))
                    .build();
        });
        return new State.Builder(name)
                .withImages(stateImages)
                .build();
    }

    /**
     * This method should be called after a round of scraping is finished (when all images in the
     * active screen have been checked). After creating the state structure, we can move to states with unchecked
     * images and add to the state structure.
     *
     * 1. Create StateImageObject(s) with images from the repo.
     * 2. A StateImageObject might have a transition. At first, just include the transition to the screen.
     * 3. Make states using the StateImageObject(s).
     * 3. Identify the pre-transition screenshot for each StateImageObject. Compare the states in the pre-transition
     *      screenshot with the states in the post transition screenshot.
     * 4. Create transitions based on the change in states in pre- and post-transition screenshots.
     */
    public void createAndSaveStatesAndTransitions() {
        // first, delete all states in the current state structure
        stateService.deleteAllStates();
        // then, create states and add them to the state structure
        List<ImageSetsAndAssociatedScreens> imageSetsList = defineStatesWithImages();
        for (ImageSetsAndAssociatedScreens imgSets : imageSetsList) {
            stateService.save(createState(imgSets, Integer.toString(imageSetsList.indexOf(imgSets))));
        }
        // now that all states are defined, we can define the transitions
        for (State state : stateService.findAllStates()) {
            StateTransitions newST = new StateTransitions.Builder(state.getName())
                    .addTransitionFinish(() -> commonActions.findState(1, state.getName()))
                    .build();
            for (StateImageObject img : state.getStateImages()) {
                if (!img.getStatesToEnter().isEmpty() && !img.getStatesToExit().isEmpty()) {
                    boolean ownerStateStaysVisible =
                            img.getStatesToEnter().contains(Integer.toString(img.getTransitionImage().getOwnerState()));
                    StateTransition newTransition = new StateTransition.Builder()
                            .setFunction(() -> commonActions.click(1, img))
                            .addToActivate(img.getStatesToEnter().toArray(new String[0]))
                            .addToExit(img.getStatesToExit().toArray(new String[0]))
                            .setStaysVisibleAfterTransition(ownerStateStaysVisible)
                            .build();
                    newST.addTransition(newTransition);
                }
            }
            stateTransitionsRepository.add(newST);
        }
    }

}
