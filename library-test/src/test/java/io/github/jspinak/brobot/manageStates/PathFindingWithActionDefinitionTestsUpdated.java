package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.config.FrameworkInitializer;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated path finding tests using new ActionConfig API.
 * Demonstrates migration from ActionOptions to specific config classes.
 * 
 * Key changes:
 * - Uses ClickOptions instead of ActionOptions.Action.CLICK
 * - TaskSequence.addStep() now accepts ActionConfig
 * - Type-safe configuration builders
 */
@SpringBootTest
public class PathFindingWithActionDefinitionTestsUpdated {

    @Autowired
    private PathFinder pathFinder;

    @Autowired
    private StateTransitionService stateTransitionsService;

    @Autowired
    private StateService allStatesInProjectService;

    @Autowired
    private StateTransitionStore stateTransitionsRepository;

    @Autowired
    private FrameworkInitializer init;

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
    
    @Test
    void testComplexPathWithMultipleTransitions() {
        State stateA = createState("StateA");
        State stateB = createState("StateB");
        State stateC = createState("StateC");
        State stateD = createState("StateD");
        State stateE = createState("StateE");

        // Create a more complex graph
        createTransition(stateA, stateB, createStateImage("ButtonAB", "ab.png"), "A to B");
        createTransition(stateA, stateC, createStateImage("ButtonAC", "ac.png"), "A to C");
        createTransition(stateB, stateD, createStateImage("ButtonBD", "bd.png"), "B to D");
        createTransition(stateC, stateD, createStateImage("ButtonCD", "cd.png"), "C to D");
        createTransition(stateD, stateE, createStateImage("ButtonDE", "de.png"), "D to E");
        init.initializeStateStructure();

        Set<Long> startStates = new HashSet<>();
        startStates.add(stateA.getId());
        Paths paths = pathFinder.getPathsToState(startStates, stateE.getId());

        assertFalse(paths.isEmpty());
        assertEquals(2, paths.getPaths().size());
        assertTrue(paths.getPaths().stream().allMatch(path -> path.getStates().size() == 4));
        assertTrue(paths.getPaths().stream().allMatch(path -> 
            path.getStates().get(0).equals(stateA.getId()) && 
            path.getStates().get(3).equals(stateE.getId())));
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
        TaskSequence actionDefinition = new TaskSequence();
        
        // NEW API: Use ClickOptions instead of ActionOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setNumberOfClicks(1)
                .setPauseAfterEnd(0.5)
                .build();
                
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
                
        // TaskSequence now accepts ActionConfig
        actionDefinition.addStep(clickOptions, objects);

        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Collections.singleton(toState.getId()));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);
        stateTransitionsRepository.add(stateTransitions);
    }
    
    /**
     * Create a more complex transition with multiple steps
     */
    private void createMultiStepTransition(State fromState, State toState, String transitionName) {
        TaskSequence actionDefinition = new TaskSequence();
        
        // Step 1: Find the button
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();
                
        ObjectCollection findObjects = new ObjectCollection.Builder()
                .withImages(createStateImage(transitionName + "_Button", transitionName + ".png"))
                .build();
                
        actionDefinition.addStep(findOptions, findObjects);
        
        // Step 2: Click the found button
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
                
        ObjectCollection clickObjects = new ObjectCollection.Builder()
                .useMatchesFromPreviousAction()
                .build();
                
        actionDefinition.addStep(clickOptions, clickObjects);

        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Collections.singleton(toState.getId()));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);
        stateTransitionsRepository.add(stateTransitions);
    }
    
    @Test
    void testPathFindingWithMultiStepTransitions() {
        State stateA = createState("StateA");
        State stateB = createState("StateB");
        State stateC = createState("StateC");

        // Create transitions with multiple steps
        createMultiStepTransition(stateA, stateB, "TransitionAB");
        createMultiStepTransition(stateB, stateC, "TransitionBC");
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
}