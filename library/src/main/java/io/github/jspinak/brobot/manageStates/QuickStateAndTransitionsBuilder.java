package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.CLICK;

/**
 * This class is meant mostly for testing state structures and state management,
 * although it can also be used for bootstrapping simple projects.
 * It sets up a state and transitions classes and add them to the repos. Only simple click transitions are used.
 */
@Component
public class QuickStateAndTransitionsBuilder {

    private final AllStatesInProjectService stateService;
    private final StateTransitionsRepository transitionsRepository;
    private final Action action;

    private String stateName;
    private List<StateImage> stateImages;
    private StateTransitions stateTransitions;

    public QuickStateAndTransitionsBuilder(AllStatesInProjectService stateService,
                                           StateTransitionsRepository transitionsRepository,
                                           Action action) {
        this.stateService = stateService;
        this.transitionsRepository = transitionsRepository;
        this.action = action;
    }

    public QuickStateAndTransitionsBuilder init(String stateName) {
        this.stateName = stateName;
        stateTransitions = new StateTransitions.Builder(stateName)
                .addTransitionFinish(() -> true)
                .build();
        stateImages = new ArrayList<>();
        return this;
    }

    public QuickStateAndTransitionsBuilder addTransitionImage(String imageFilename, String stateTransitionedTo) {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(imageFilename)
                .build();
        stateImages.add(stateImage);
        StateTransition stateTransition = new StateTransition.Builder()
                .addToActivate(stateTransitionedTo)
                .setFunction(() -> action.perform(CLICK, stateImage).isSuccess())
                .build();
        stateTransitions.addTransition(stateTransition);
        return this;
    }

    public QuickStateAndTransitionsBuilder addImage(String imageFilename) {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(imageFilename)
                .build();
        stateImages.add(stateImage);
        return this;
    }

    public void build() {
        State state = new State.Builder(stateName)
                .withImages(stateImages.toArray(new StateImage[0]))
                .build();
        stateService.save(state);
        transitionsRepository.add(stateTransitions);
    }

}
