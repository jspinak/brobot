package io.github.jspinak.brobot.manageStates;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;

import io.github.jspinak.brobot.action.ActionConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

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
    private StateTransitionService stateTransitionsService;

    @Autowired
    private StateService allStatesInProjectService;

    @Autowired
    private StateTransitionStore stateTransitionsRepository;

    @Autowired
    private FrameworkInitializer init;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
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
        TaskSequence actionDefinition = new TaskSequence();
        ClickOptions clickOptions = new ClickOptions.Builder()
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
}