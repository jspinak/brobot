package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.actions.customActions.CommonActions;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.preliminaryStates.ImageSetsAndAssociatedScreens;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.StatelessImage;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.JavaStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.services.Init;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReplaceStateStructure {

    private final StateService stateService;
    private final CreateState createState;
    private final CommonActions commonActions;
    private final StateTransitionsRepository stateTransitionsRepository;
    private final PrepareImageSets prepareImageSets;
    private final Init init;

    public ReplaceStateStructure(StateService stateService, CreateState createState, CommonActions commonActions,
                                 StateTransitionsRepository stateTransitionsRepository,
                                 PrepareImageSets prepareImageSets, Init init) {
        this.stateService = stateService;
        this.createState = createState;
        this.commonActions = commonActions;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.prepareImageSets = prepareImageSets;
        this.init = init;
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
    public void createNewStateStructure(Region usableArea, List<StatelessImage> statelessImages) {
        // first, delete all states in the current state structure
        stateService.deleteAllStates();
        // then, create states and add them to the state structure
        List<ImageSetsAndAssociatedScreens> imageSetsList = prepareImageSets.defineStatesWithImages(statelessImages);
        for (ImageSetsAndAssociatedScreens imgSets : imageSetsList) {
            String stateName = Integer.toString(imageSetsList.indexOf(imgSets) + 1); // JPA starts ids at 1, not 0
            State newState = createState.createState(imgSets, stateName, usableArea);
            stateService.save(newState);
        }
        List<State> allStates = stateService.getAllStates();
        // now that all states are defined, we can define the transitions
        createTransitionsAndAddToStateStructure();
        init.populateStateIds();
    }

    private void createTransitionsAndAddToStateStructure() {
        for (State state : stateService.getAllStates()) {
            StateTransitions newST = new StateTransitions.Builder(state.getName())
                    .addTransitionFinish(() -> commonActions.findState(1, state.getName()))
                    .build();
            /*for (StateImage img : state.getStateImages()) {
                addTransition(img, newST);
            }*/
            stateTransitionsRepository.add(newST);
        }
    }

    /*
    This requires using the ExtendedStateImageDTO, since StateImage no longer has fields related to
    state structure building methods (statesToEnter, statesToExit).
     */
    /*private void addTransition(StateImage img, StateTransitions newST) {
        if (!img.getStatesToEnter().isEmpty() && !img.getStatesToExit().isEmpty()) {
            JavaStateTransition newTransition = new JavaStateTransition.Builder()
                    .setFunction(() -> commonActions.click(1, img))
                    .addToActivate(img.getStatesToEnter().toArray(new String[0]))
                    .addToExit(img.getStatesToExit().toArray(new String[0]))
                    .build();
            newST.addTransition(newTransition);
        }
    }*/
}
