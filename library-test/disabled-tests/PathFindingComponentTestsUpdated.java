package io.github.jspinak.brobot.manageStates;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.config.FrameworkInitializer;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated path finding component tests using new ActionConfig API.
 * Tests individual components of the path finding system.
 * 
 * Key changes:
 * - Uses ClickOptions instead of ClickOptions
 * - Demonstrates component testing with new API
 */
@SpringBootTest
public class PathFindingComponentTestsUpdated {

    @Autowired
    private PathFinder pathFinder;

    @Autowired
    private StateTransitionService stateTransitionsService;

    @Autowired
    private StateService allStatesInProjectService;

    @Autowired
    private StateTransitionStore stateTransitionsRepository;

    @Autowired
    private StateTransitionsJointTable stateTransitionsJointTable;

    @Autowired
    private FrameworkInitializer init;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
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
    
    @Test
    void testComplexTransitionWithMultipleActions() {
        State loginState = createState("LoginState");
        State mainMenuState = createState("MainMenuState");
        
        // Create a complex transition with Find then Click
        createFindAndClickTransition(loginState, mainMenuState, 
                createStateImage("LoginButton", "login.png"), "Find and Click Login");
        init.initializeStateStructure();

        Optional<StateTransitions> transitions = stateTransitionsService.getTransitions(loginState.getId());
        assertTrue(transitions.isPresent());
        
        // Verify the transition has multiple steps
        StateTransitions stateTransitions = transitions.get();
        assertEquals(1, stateTransitions.getTransitions().size());
        
        if (stateTransitions.getTransitions().get(0) instanceof TaskSequenceStateTransition) {
            TaskSequenceStateTransition taskSeqTransition = 
                (TaskSequenceStateTransition) stateTransitions.getTransitions().get(0);
            assertTrue(taskSeqTransition.getTaskSequenceOptional().isPresent());
            TaskSequence taskSeq = taskSeqTransition.getTaskSequenceOptional().get();
            assertEquals(2, taskSeq.getSteps().size()); // Find + Click
        }
    }
    
    @Test
    void testTransitionWithCustomClickOptions() {
        State stateA = createState("StateWithDoubleClick");
        State stateB = createState("StateAfterDoubleClick");
        
        createDoubleClickTransition(stateA, stateB, 
                createStateImage("DoubleClickTarget", "target.png"), "Double Click Target");
        init.initializeStateStructure();

        Set<Long> startStates = new HashSet<>();
        startStates.add(stateA.getId());
        Paths paths = pathFinder.getPathsToState(startStates, stateB.getId());

        assertFalse(paths.isEmpty());
        assertEquals(1, paths.getPaths().size());
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
        
        // NEW API: Use ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                // .setClickType(ClickOptions.Type.LEFT) // ClickOptions.Type enum removed
                .setNumberOfClicks(1)
                .build();
                
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
                
        actionDefinition.addStep(clickOptions, objects);

        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Collections.singleton(toState.getId()));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);
        stateTransitionsRepository.add(stateTransitions);
    }
    
    private void createFindAndClickTransition(State fromState, State toState, 
                                            StateImage stateImage, String actionDescription) {
        TaskSequence actionDefinition = new TaskSequence();
        
        // Step 1: Find with PatternFindOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .setCaptureImage(true)
                .build();
                
        ObjectCollection findObjects = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
                
        actionDefinition.addStep(findOptions, findObjects);
        
        // Step 2: Click on the found match
        ClickOptions clickOptions = new ClickOptions.Builder()
                // .setClickType(ClickOptions.Type.LEFT) // ClickOptions.Type enum removed
                .setPauseAfterEnd(1.0)
                .build();
                
        ObjectCollection clickObjects = new ObjectCollection.Builder()
                // .useMatchesFromPreviousAction() // Method not available in new API
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
    
    private void createDoubleClickTransition(State fromState, State toState,
                                           StateImage stateImage, String actionDescription) {
        TaskSequence actionDefinition = new TaskSequence();
        
        // NEW API: Use ClickOptions with DOUBLE type
        ClickOptions doubleClickOptions = new ClickOptions.Builder()
                // .setClickType(ClickOptions.Type.DOUBLE_LEFT) // ClickOptions.Type enum removed
                .setNumberOfClicks(2)
                .setPauseAfterEnd(0.5)
                .build();
                
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
                
        actionDefinition.addStep(doubleClickOptions, objects);

        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Collections.singleton(toState.getId()));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);
        stateTransitionsRepository.add(stateTransitions);
    }
    
    @Test
    void testPathFindingThroughMultipleTransitionTypes() {
        // Create a path that uses different transition types
        State start = createState("Start");
        State middle = createState("Middle");
        State end = createState("End");
        
        // Start -> Middle: Find and Click
        createFindAndClickTransition(start, middle,
                createStateImage("MiddleButton", "middle.png"), "Go to Middle");
                
        // Middle -> End: Double Click
        createDoubleClickTransition(middle, end,
                createStateImage("EndButton", "end.png"), "Go to End");
                
        init.initializeStateStructure();

        Set<Long> startStates = new HashSet<>();
        startStates.add(start.getId());
        Paths paths = pathFinder.getPathsToState(startStates, end.getId());

        assertFalse(paths.isEmpty());
        assertEquals(1, paths.getPaths().size());
        assertEquals(3, paths.getPaths().get(0).getStates().size());
        assertEquals(start.getId(), paths.getPaths().get(0).getStates().get(0));
        assertEquals(middle.getId(), paths.getPaths().get(0).getStates().get(1));
        assertEquals(end.getId(), paths.getPaths().get(0).getStates().get(2));
    }
}