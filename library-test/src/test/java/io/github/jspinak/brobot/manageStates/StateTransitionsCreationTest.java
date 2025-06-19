package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.services.Init;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class StateTransitionsCreationTest {

    @Autowired
    private PathFinder pathFinder;

    @Autowired
    private StateTransitionsInProjectService stateTransitionsService;

    @Autowired
    private AllStatesInProjectService allStatesInProjectService;

    @Autowired
    private StateTransitionsRepository stateTransitionsRepository;

    @Autowired
    private Init init;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void testCreateAndSaveStateTransitions() {
        // Create states
        State stateA = createState("StateA");
        State stateB = createState("StateB");

        // Create a StateImage for the transition
        StateImage stateImage = createStateImage("ButtonImage", "topLeft");

        // Create and save the transition
        createTransition(stateA, stateB, stateImage, "Click Button");
        init.initializeStateStructure();

        // Test path finding
        Paths paths = pathFinder.getPathsToState(Collections.singletonList(stateA), stateB);

        assertFalse(paths.isEmpty());
        assertEquals(1, paths.getPaths().size());
        assertEquals(2, paths.getPaths().get(0).getStates().size());
        assertEquals(stateA.getId(), paths.getPaths().get(0).getStates().get(0));
        assertEquals(stateB.getId(), paths.getPaths().get(0).getStates().get(1));
    }

    private State createState(String stateName) {
        State state = new State.Builder(stateName).build();
        allStatesInProjectService.save(state);
        return state;
    }

    private StateImage createStateImage(String imageName, String filename) {
        // In mock mode, we don't need actual files, just create pattern with name
        Pattern pattern = new Pattern.Builder().setName(imageName).build();
        return new StateImage.Builder().setName(imageName).addPattern(pattern).build();
    }

    private void createTransition(State fromState, State toState, StateImage stateImage, String actionDescription) {
        // Create ActionDefinition
        ActionDefinition actionDefinition = new ActionDefinition();
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        actionDefinition.addStep(options, objects);

        // Create StateTransition
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Stream.of(toState.getId()).collect(Collectors.toSet()));

        // Create StateTransitions
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);

        // Save StateTransitions
        stateTransitionsRepository.add(stateTransitions);
    }
}