package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.services.Init;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PathFindingComponentTests {

    @Autowired
    private PathFinder pathFinder;

    @Autowired
    private StateTransitionsInProjectService stateTransitionsService;

    @Autowired
    private AllStatesInProjectService allStatesInProjectService;

    @Autowired
    private StateTransitionsRepository stateTransitionsRepository;

    @Autowired
    private StateTransitionsJointTable stateTransitionsJointTable;

    @Autowired
    private Init init;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void testStateCreationAndRetrieval() {
        State stateA = createState("StateA");
        Optional<State> retrievedState = allStatesInProjectService.getState(stateA.getId());

        assertTrue(retrievedState.isPresent());
        assertEquals("StateA", retrievedState.get().getName());
    }

    @Test
    void testTransitionCreation() {
        State stateA = createState("StateA");
        State stateB = createState("StateB");
        createTransition(stateA, stateB, createStateImage("Button", "topLeft"), "Click Button");
        init.initializeStateStructure();

        Optional<StateTransitions> transitions = stateTransitionsService.getTransitions(stateA.getId());
        assertTrue(transitions.isPresent());
        assertEquals(1, transitions.get().getTransitions().size());
    }

    @Test
    void testStateTransitionsJointTable() {
        State stateA = createState("StateA");
        State stateB = createState("StateB");
        createTransition(stateA, stateB, createStateImage("Button", "topLeft"), "Click Button");
        init.initializeStateStructure();
        Set<Long> statesWithTransitionsTo = stateTransitionsJointTable.getStatesWithTransitionsTo(stateB.getId());
        assertTrue(statesWithTransitionsTo.contains(stateA.getId()));
    }

    @Test
    void testSingleStepPathFinding() {
        State stateA = createState("StateA");
        State stateB = createState("StateB");
        createTransition(stateA, stateB, createStateImage("Button", "topLeft"), "Click Button");
        init.initializeStateStructure();

        Set<Long> startStates = new HashSet<>();
        startStates.add(stateA.getId());
        Paths paths = pathFinder.getPathsToState(startStates, stateB.getId());

        assertFalse(paths.isEmpty());
        assertEquals(1, paths.getPaths().size());
        assertEquals(2, paths.getPaths().get(0).getStates().size());
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
        ActionDefinition actionDefinition = new ActionDefinition();
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        actionDefinition.addStep(options, objects);

        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Collections.singleton(toState.getId()));
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);
        stateTransitionsRepository.add(stateTransitions);
    }
}