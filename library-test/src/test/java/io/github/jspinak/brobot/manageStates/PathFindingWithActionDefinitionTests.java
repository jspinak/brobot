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
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PathFindingWithActionDefinitionTests {

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
    void testSimplePathFinding() {
        State stateA = createState("StateA");
        State stateB = createState("StateB");
        State stateC = createState("StateC");

        createTransition(stateA, stateB, createStateImage("Button1", "topLeft"), "Click Button 1");
        createTransition(stateB, stateC, createStateImage("Button2", "topLeft"), "Click Button 2");
        init.initializeStateStructure();

        Set<Long> startStates = new HashSet<>();
        startStates.add(stateA.getId());

        Paths paths = pathFinder.getPathsToState(startStates, stateC.getId());

        assertFalse(paths.isEmpty());
        assertEquals(1, paths.getPaths().size());
        assertEquals(3, paths.getPaths().get(0).getStates().size());
        assertEquals(stateA.getId(), paths.getPaths().get(0).getStates().get(0));
        assertEquals(stateC.getId(), paths.getPaths().get(0).getStates().get(2));
    }

    @Test
    void testMultiplePathFinding() {
        State stateA = createState("StateA");
        State stateB = createState("StateB");
        State stateC = createState("StateC");
        State stateD = createState("StateD");

        createTransition(stateA, stateB, createStateImage("Button1", "button1.png"), "Path 1 - Step 1");
        createTransition(stateB, stateD, createStateImage("Button2", "button2.png"), "Path 1 - Step 2");
        createTransition(stateA, stateC, createStateImage("Button3", "button3.png"), "Path 2 - Step 1");
        createTransition(stateC, stateD, createStateImage("Button4", "button4.png"), "Path 2 - Step 2");
        init.initializeStateStructure();

        Set<Long> startStates = new HashSet<>();
        startStates.add(stateA.getId());
        Paths paths = pathFinder.getPathsToState(startStates, stateD.getId());

        assertFalse(paths.isEmpty());
        assertEquals(2, paths.getPaths().size());
        assertTrue(paths.getPaths().stream().allMatch(path -> path.getStates().size() == 3));
        assertTrue(paths.getPaths().stream().allMatch(path -> path.getStates().get(0).equals(stateA.getId())));
        assertTrue(paths.getPaths().stream().allMatch(path -> path.getStates().get(2).equals(stateD.getId())));
    }

    @Test
    void testNoPathAvailable() {
        State stateA = createState("StateA");
        State stateB = createState("StateB");
        State stateC = createState("StateC");

        createTransition(stateA, stateB, createStateImage("Button", "button.png"), "Click Button");
        init.initializeStateStructure();

        Set<Long> startStates = new HashSet<>();
        startStates.add(stateA.getId());
        Paths paths = pathFinder.getPathsToState(startStates, stateC.getId());

        assertTrue(paths.isEmpty());
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