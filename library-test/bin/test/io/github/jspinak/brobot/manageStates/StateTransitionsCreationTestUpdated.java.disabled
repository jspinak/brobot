package io.github.jspinak.brobot.manageStates;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Updated state transitions creation test using new ActionConfig API.
 * 
 * Key changes:
 * - Uses specific config classes instead of generic ActionOptions
 * - TaskSequence.addStep() now accepts ActionConfig
 * - Demonstrates various transition types with new API
 */
@SpringBootTest
public class StateTransitionsCreationTestUpdated {

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
    
    @Test
    void testCreateComplexTransitionWithMultipleSteps() {
        // Create states
        State loginPage = createState("LoginPage");
        State dashboard = createState("Dashboard");

        // Create StateImages for various elements
        StateImage usernameField = createStateImage("UsernameField", "username.png");
        StateImage passwordField = createStateImage("PasswordField", "password.png");
        StateImage loginButton = createStateImage("LoginButton", "login.png");

        // Create a complex transition with multiple steps
        createLoginTransition(loginPage, dashboard, usernameField, passwordField, loginButton);
        init.initializeStateStructure();

        // Test path finding
        Paths paths = pathFinder.getPathsToState(Collections.singletonList(loginPage), dashboard);

        assertFalse(paths.isEmpty());
        assertEquals(1, paths.getPaths().size());
        assertEquals(2, paths.getPaths().get(0).getStates().size());
    }
    
    @Test
    void testCreateTransitionWithDragAction() {
        // Create states
        State beforeDrag = createState("BeforeDrag");
        State afterDrag = createState("AfterDrag");

        // Create StateImages for drag elements
        StateImage dragSource = createStateImage("DragSource", "source.png");
        StateImage dragTarget = createStateImage("DragTarget", "target.png");

        // Create drag transition
        createDragTransition(beforeDrag, afterDrag, dragSource, dragTarget);
        init.initializeStateStructure();

        // Test path finding
        Paths paths = pathFinder.getPathsToState(Collections.singletonList(beforeDrag), afterDrag);

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
        // Create ActionDefinition
        TaskSequence actionDefinition = new TaskSequence();
        
        // NEW API: Use ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setNumberOfClicks(1)
                .build();
                
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
                
        actionDefinition.addStep(clickOptions, objects);

        // Create StateTransition
        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Stream.of(toState.getId()).collect(Collectors.toSet()));

        // Create StateTransitions
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);

        // Save StateTransitions
        stateTransitionsRepository.add(stateTransitions);
    }
    
    private void createLoginTransition(State fromState, State toState, 
                                     StateImage usernameField, StateImage passwordField, StateImage loginButton) {
        TaskSequence actionDefinition = new TaskSequence();
        
        // Step 1: Find and click username field
        PatternFindOptions findUsernameOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();
        actionDefinition.addStep(findUsernameOptions, 
            new ObjectCollection.Builder().withImages(usernameField).build());
            
        ClickOptions clickUsernameOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        actionDefinition.addStep(clickUsernameOptions,
            new ObjectCollection.Builder().useMatchesFromPreviousAction().build());
        
        // Step 2: Type username
        TypeOptions typeUsernameOptions = new TypeOptions.Builder()
                .build();
        actionDefinition.addStep(typeUsernameOptions,
            new ObjectCollection.Builder().withStrings("testuser").build());
        
        // Step 3: Find and click password field
        PatternFindOptions findPasswordOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();
        actionDefinition.addStep(findPasswordOptions,
            new ObjectCollection.Builder().withImages(passwordField).build());
            
        ClickOptions clickPasswordOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        actionDefinition.addStep(clickPasswordOptions,
            new ObjectCollection.Builder().useMatchesFromPreviousAction().build());
        
        // Step 4: Type password
        TypeOptions typePasswordOptions = new TypeOptions.Builder()
                .build();
        actionDefinition.addStep(typePasswordOptions,
            new ObjectCollection.Builder().withStrings("password123").build());
        
        // Step 5: Click login button
        ClickOptions clickLoginOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setPauseAfterEnd(2.0) // Wait for page to load
                .build();
        actionDefinition.addStep(clickLoginOptions,
            new ObjectCollection.Builder().withImages(loginButton).build());

        // Create StateTransition
        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Collections.singleton(toState.getId()));

        // Create StateTransitions
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);

        // Save StateTransitions
        stateTransitionsRepository.add(stateTransitions);
    }
    
    private void createDragTransition(State fromState, State toState,
                                    StateImage dragSource, StateImage dragTarget) {
        TaskSequence actionDefinition = new TaskSequence();
        
        // NEW API: Use DragOptions
        DragOptions dragOptions = new DragOptions.Builder()
                .setDragFromIndex(0)  // First image in collection (source)
                .setToIndex(1)    // Second image in collection (target)
                .setPauseAfterEnd(1.0)
                .build();
                
        ObjectCollection objects = new ObjectCollection.Builder()
                .withImages(dragSource, dragTarget)
                .build();
                
        actionDefinition.addStep(dragOptions, objects);

        // Create StateTransition
        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        transition.setActionDefinition(actionDefinition);
        transition.setActivate(Collections.singleton(toState.getId()));

        // Create StateTransitions
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(fromState.getId());
        stateTransitions.addTransition(transition);

        // Save StateTransitions
        stateTransitionsRepository.add(stateTransitions);
    }
}