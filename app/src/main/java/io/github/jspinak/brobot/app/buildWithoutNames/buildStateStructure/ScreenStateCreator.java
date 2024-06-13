package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.ScreenObservations;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.TransitionImage;
import io.github.jspinak.brobot.app.buildWithoutNames.screenObservations.TransitionImageRepo;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.actions.customActions.CommonActions;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.illustratedHistory.StateIllustration;
import io.github.jspinak.brobot.illustratedHistory.StateIllustrator;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import io.github.jspinak.brobot.manageStates.StateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.basics.Settings;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@Setter
public class ScreenStateCreator {

    private final TransitionImageRepo transitionImageRepo;
    private final ScreenObservations screenObservations;
    private final StateService stateService;
    private final CommonActions commonActions;
    private final StateTransitionsRepository stateTransitionsRepository;
    private final ImageUtils imageUtils;
    private final StateIllustrator stateIllustrator;
    private final MatVisualize matVisualize;

    private boolean saveStateIllustrations;

    public ScreenStateCreator(TransitionImageRepo transitionImageRepo, ScreenObservations screenObservations,
                              StateService stateService, CommonActions commonActions,
                              StateTransitionsRepository stateTransitionsRepository, ImageUtils imageUtils,
                              StateIllustrator stateIllustrator, MatVisualize matVisualize) {
        this.transitionImageRepo = transitionImageRepo;
        this.screenObservations = screenObservations;
        this.stateService = stateService;
        this.commonActions = commonActions;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.imageUtils = imageUtils;
        this.stateIllustrator = stateIllustrator;
        this.matVisualize = matVisualize;
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
        transitionImageRepo.getImages().forEach(img -> addToImageSet(imagesScreens, img));
        return imagesScreens;
    }

    private void addToImageSet(List<ImageSetsAndAssociatedScreens> imagesScreens,
                               TransitionImage transitionImage) {
        for (ImageSetsAndAssociatedScreens imgScr : imagesScreens) {
            if (imgScr.ifSameScreensAddImage(transitionImage)) {
                // ImageSetsAndAssociatedScreens are a preliminary representation of states, and the created state's name will be the imgScr's index
                transitionImage.setOwnerState(imagesScreens.indexOf(imgScr));
                return;
            }
        }
        imagesScreens.add(new ImageSetsAndAssociatedScreens(transitionImage.getIndexInRepo(), transitionImage.getScreensFound()));
        transitionImage.setOwnerState(imagesScreens.size()-1); // the state name is the last index added
    }

    /**
     * Saves state images to file, creates a state with these images.
     * @param imageSets used to identify which images belong to the state.
     * @param name state name
     * @return the newly created state
     */
    public State createState(ImageSetsAndAssociatedScreens imageSets, String name) {
        List<StateImage> stateImages = new ArrayList<>();
        imageSets.getImages().forEach(imgIndex -> {
            if (transitionImageRepo.getImages().size() <= imgIndex || imgIndex < 0) {
                System.out.println("image index not in transitionImageRepo: " + imgIndex);
            } else {
                Mat matchMat = transitionImageRepo.getImages().get(imgIndex).getImage();
                String imageName = Integer.toString(transitionImageRepo.getImages().get(imgIndex).getIndexInRepo());
                TransitionImage transitionImage = transitionImageRepo.getImages().get(imgIndex);
                Region reg = transitionImage.getRegion();
                Pattern pattern = imageUtils.matToPattern(matchMat, Settings.BundlePath + "/" + imageName);
                pattern.addMatchSnapshot(reg.x(), reg.y(), reg.w(), reg.h());
                StateImage sio = new StateImage.Builder()
                        .addPattern(pattern)
                        .build();
                stateImages.add(sio);
            }
        });
        List<Image> scenes = new ArrayList<>();
        for (int id : imageSets.getScreens()) {
            Optional<ScreenObservation> screenOptional = screenObservations.get(id);
            screenOptional.ifPresent(screenObservation -> scenes.add(new Image(screenObservation.getScreenshot())));
        }
        State newState = new State.Builder(name)
                .withImages(stateImages)
                .withScenes(scenes)
                .addIllustrations()
                .build();
        for (Image scene : scenes) {
            StateIllustration stateIllustration = stateIllustrator.drawState(newState, scene.getMatBGR());
            newState.addIllustrations(stateIllustration);
            matVisualize.writeMatToHistory(stateIllustration.getIllustratedScreenshotAsMat(), "illustration of state " + newState.getName());
        }
        return newState;
    }

    /**
     * This method should be called after a round of scraping is finished (when all images in the
     * active screen have been checked). After creating the state structure, we can move to states with unchecked
     * images and add to the state structure.
     *
     * The TransitionImage(s) should already contain all screens in which they appear.
     *
     * 1. Create StateImage(s) with images from the repo.
     * 2. A StateImage might have a transition. At first, just include the transition to the screen.
     * 3. Make states using the StateImage(s).
     * 3. Identify the pre-transition screenshot for each StateImage. Compare the states in the pre-transition
     *      screenshot with the states in the post transition screenshot.
     * 4. Create transitions based on the change in states in pre- and post-transition screenshots.
     */
    public void createAndSaveStatesAndTransitions() {
        // first, delete all states in the current state structure
        stateService.deleteAllStates();
        // then, create states and add them to the state structure
        List<ImageSetsAndAssociatedScreens> imageSetsList = defineStatesWithImages();
        for (ImageSetsAndAssociatedScreens imgSets : imageSetsList) {
            State newState = createState(imgSets, Integer.toString(imageSetsList.indexOf(imgSets)));
            stateService.save(newState);
        }
        // now that all states are defined, we can define the transitions
        for (State state : stateService.getAllStates()) {
            StateTransitions newST = new StateTransitions.Builder(state.getName())
                    .addTransitionFinish(() -> commonActions.findState(1, state.getName()))
                    .build();
            for (StateImage img : state.getStateImages()) {
                if (!img.getStatesToEnter().isEmpty() && !img.getStatesToExit().isEmpty()) {
                    StateTransition newTransition = new StateTransition.Builder()
                            .setFunction(() -> commonActions.click(1, img))
                            .addToActivate(img.getStatesToEnter().toArray(new String[0]))
                            .addToExit(img.getStatesToExit().toArray(new String[0]))
                            .build();
                    newST.addTransition(newTransition);
                }
            }
            stateTransitionsRepository.add(newST);
        }
    }

}
