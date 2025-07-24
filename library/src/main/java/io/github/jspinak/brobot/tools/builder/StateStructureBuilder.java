package io.github.jspinak.brobot.tools.builder;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Action.CLICK;

/**
 * This class is meant mostly for testing state structures and state management,
 * although it can also be used for bootstrapping simple projects.
 * It sets up a state and transitions classes and add them to the repos. Only
 * simple click transitions are used.
 */
@Component
public class StateStructureBuilder {

    private final StateService stateService;
    private final StateTransitionStore transitionsRepository;
    private final Action action;

    private String stateName;
    private List<StateImage> stateImages;
    private StateTransitions stateTransitions;

    public StateStructureBuilder(StateService stateService,
            StateTransitionStore transitionsRepository,
            Action action) {
        this.stateService = stateService;
        this.transitionsRepository = transitionsRepository;
        this.action = action;
    }

    public StateStructureBuilder init(String stateName) {
        this.stateName = stateName;
        stateTransitions = new StateTransitions.Builder(stateName)
                .addTransitionFinish(() -> true)
                .build();
        stateImages = new ArrayList<>();
        return this;
    }

    public StateStructureBuilder addTransitionImage(String imageFilename, String stateTransitionedTo) {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(imageFilename)
                .build();
        stateImages.add(stateImage);
        JavaStateTransition javaStateTransition = new JavaStateTransition.Builder()
                .addToActivate(stateTransitionedTo)
                .setFunction(() -> action.perform(CLICK, stateImage).isSuccess())
                .build();
        stateTransitions.addTransition(javaStateTransition);
        return this;
    }

    public StateStructureBuilder addImage(String imageFilename) {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(imageFilename)
                .build();
        stateImages.add(stateImage);
        return this;
    }

    public void build() {
        State state = new State.Builder(stateName)
                .withImages(stateImages)
                .build();
        stateService.save(state);
        transitionsRepository.add(stateTransitions);
        //transitionsRepository.print();
    }

}
