package io.github.jspinak.brobot.transitions;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.annotations.Transition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that replicates the claude-automator state structure
 * and tests state transitions with declarative region dependencies.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@Import({
    StateTransitionMockTest.TestPromptState.class,
    StateTransitionMockTest.TestWorkingState.class,
    StateTransitionMockTest.TestPromptToWorkingTransition.class,
    io.github.jspinak.brobot.annotations.AnnotationProcessor.class
})
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "logging.level.io.github.jspinak.brobot=DEBUG"
})
@Slf4j
public class StateTransitionMockTest extends BrobotTestBase {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateTransitionService transitionService;
    
    @Autowired
    private StateMemory stateMemory;
    
    @Autowired
    private PathFinder pathFinder;
    
    @Autowired
    private StateTransitionsJointTable jointTable;
    
    /**
     * Test PromptState that mimics claude-automator's PromptState
     */
    @Component
    @State(initial = true)
    @Getter
    public static class TestPromptState {
        private final StateImage claudePrompt;
        private final StateString continueCommand;
        
        public TestPromptState() {
            claudePrompt = new StateImage.Builder()
                .setName("ClaudePrompt")
                .build();
            
            continueCommand = new StateString.Builder()
                .setName("ContinueCommand")
                .setString("continue\n")
                .build();
        }
    }
    
    /**
     * Test WorkingState that mimics claude-automator's WorkingState
     */
    @Component
    @State
    @Getter
    public static class TestWorkingState {
        private final StateImage claudeIcon;
        
        public TestWorkingState() {
            claudeIcon = new StateImage.Builder()
                .setName("ClaudeIcon")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("TestPrompt")
                    .setTargetObjectName("ClaudePrompt")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddX(3)
                        .setAddY(10)
                        .setAddW(30)
                        .setAddH(55)
                        .build())
                    .build())
                .build();
        }
    }
    
    /**
     * Test transition from Prompt to Working
     */
    @Component
    @Transition(from = TestPromptState.class, to = TestWorkingState.class)
    @RequiredArgsConstructor
    public static class TestPromptToWorkingTransition {
        
        private final TestPromptState promptState;
        private final Action action;
        
        public boolean execute() {
            log.info("Executing test transition from Prompt to Working");
            return true;
        }
    }
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        stateMemory.removeAllStates();
        log.info("=== STARTING STATE TRANSITION TEST ===");
    }
    
    @Test
    public void testStateStructureSetup() {
        log.info("=== TESTING STATE STRUCTURE ===");
        
        // Verify states are registered
        List<io.github.jspinak.brobot.model.state.State> allStates = stateService.getAllStates();
        log.info("Total registered states: {}", allStates.size());
        
        for (io.github.jspinak.brobot.model.state.State state : allStates) {
            log.info("  State: {} (ID: {})", state.getName(), state.getId());
        }
        
        // Should have at least our two test states
        assertTrue(allStates.size() >= 2, "Should have at least 2 states registered");
        
        // Check specific states exist
        Optional<io.github.jspinak.brobot.model.state.State> promptState = 
            stateService.getState("TestPrompt");
        Optional<io.github.jspinak.brobot.model.state.State> workingState = 
            stateService.getState("TestWorking");
        
        assertTrue(promptState.isPresent(), "TestPrompt state should exist");
        assertTrue(workingState.isPresent(), "TestWorking state should exist");
        
        log.info("TestPrompt state ID: {}", promptState.get().getId());
        log.info("TestWorking state ID: {}", workingState.get().getId());
    }
    
    @Test
    public void testTransitionRegistration() {
        log.info("=== TESTING TRANSITION REGISTRATION ===");
        
        // List all transitions
        List<StateTransitions> allTransitions = transitionService.getAllStateTransitions();
        log.info("Total transition containers: {}", allTransitions.size());
        
        for (StateTransitions transitions : allTransitions) {
            log.info("From state: {} (ID: {})", 
                    transitions.getStateName(), transitions.getStateId());
            if (transitions.getTransitions() != null) {
                transitions.getTransitions().forEach(transition -> {
                    log.info("  -> To states: {}", transition.getActivate());
                    log.info("     Type: {}", transition.getClass().getSimpleName());
                    log.info("     Score: {}", transition.getScore());
                });
            }
        }
        
        // Check specific transition from TestPrompt to TestWorking
        Optional<io.github.jspinak.brobot.model.state.State> promptState = 
            stateService.getState("TestPrompt");
        
        assertTrue(promptState.isPresent(), "TestPrompt state should exist");
        
        Optional<StateTransitions> promptTransitions = 
            transitionService.getTransitions(promptState.get().getId());
        
        assertTrue(promptTransitions.isPresent(), 
            "TestPrompt should have transitions registered");
        
        assertFalse(promptTransitions.get().getTransitions().isEmpty(), 
            "TestPrompt should have at least one transition");
        
        // Check that the transition goes to TestWorking
        Optional<io.github.jspinak.brobot.model.state.State> workingState = 
            stateService.getState("TestWorking");
        assertTrue(workingState.isPresent(), "TestWorking state should exist");
        
        boolean hasTransitionToWorking = promptTransitions.get().getTransitions().stream()
            .anyMatch(trans -> trans.getActivate().contains(workingState.get().getId()));
        
        assertTrue(hasTransitionToWorking, 
            "TestPrompt should have a transition to TestWorking");
    }
    
    @Test
    public void testPathFinding() {
        log.info("=== TESTING PATH FINDING ===");
        
        // Get state IDs
        Optional<io.github.jspinak.brobot.model.state.State> promptState = 
            stateService.getState("TestPrompt");
        Optional<io.github.jspinak.brobot.model.state.State> workingState = 
            stateService.getState("TestWorking");
        
        assertTrue(promptState.isPresent() && workingState.isPresent(), 
            "Both states should exist");
        
        Long promptId = promptState.get().getId();
        Long workingId = workingState.get().getId();
        
        log.info("Finding path from TestPrompt ({}) to TestWorking ({})", 
            promptId, workingId);
        
        // Set Prompt as active
        stateMemory.addActiveState(promptId);
        Set<Long> activeStates = stateMemory.getActiveStates();
        log.info("Active states before pathfinding: {}", activeStates);
        assertTrue(activeStates.contains(promptId), "Prompt should be active");
        
        // Try to find path from Prompt to Working
        io.github.jspinak.brobot.navigation.path.Paths paths = pathFinder.getPathsToState(activeStates, workingId);
        
        log.info("Path finding result: {}", 
            !paths.isEmpty() ? "Paths found" : "No paths found");
        
        if (!paths.isEmpty()) {
            log.info("Number of paths found: {}", paths.getPaths().size());
            paths.getPaths().forEach(path -> {
                log.info("Path details:");
                log.info("  States in path: {}", path.getStates());
                log.info("  Score: {}", path.getScore());
                log.info("  Transitions: {}", path.getTransitions().size());
                path.getTransitions().forEach(trans -> {
                    log.info("    Transition activates: {}, exits: {}", 
                        trans.getActivate(), trans.getExit());
                });
            });
        } else {
            // Debug why path wasn't found
            log.error("No path found from TestPrompt to TestWorking!");
            
            // Check if Working state has parent states
            log.info("Checking parent states for TestWorking...");
            List<StateTransitions> allTransitions = transitionService.getAllStateTransitions();
            for (StateTransitions trans : allTransitions) {
                if (trans.getTransitions() != null) {
                    trans.getTransitions().forEach(t -> {
                        if (t.getActivate().contains(workingId)) {
                            log.info("Found transition to TestWorking from: {}", 
                                trans.getStateName());
                        }
                    });
                }
            }
        }
        
        assertFalse(paths.isEmpty(), 
            "Should be able to find path from TestPrompt to TestWorking");
    }
    
    @Test
    public void testStateOpening() {
        log.info("=== TESTING STATE OPENING ===");
        
        // Get state IDs
        Optional<io.github.jspinak.brobot.model.state.State> promptState = 
            stateService.getState("TestPrompt");
        Optional<io.github.jspinak.brobot.model.state.State> workingState = 
            stateService.getState("TestWorking");
        
        assertTrue(promptState.isPresent() && workingState.isPresent(), 
            "Both states should exist");
        
        // Set Prompt as active
        stateMemory.addActiveState(promptState.get().getId());
        log.info("Active states: {}", stateMemory.getActiveStateNames());
        
        // Try to open Working state (this mimics what happens in the application)
        log.info("Attempting to open TestWorking state...");
        
        // First check if path exists
        io.github.jspinak.brobot.navigation.path.Paths paths = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            workingState.get().getId()
        );
        
        if (paths.isEmpty()) {
            log.error("Cannot open TestWorking - no path found!");
            
            // Additional debugging
            log.info("=== DEBUGGING PATH FINDING FAILURE ===");
            log.info("Active states: {}", stateMemory.getActiveStates());
            log.info("Target state ID: {}", workingState.get().getId());
            
            // Check joint table directly
            log.info("Checking StateTransitionsJointTable directly...");
            Optional<StateTransitions> transFromPrompt = 
                transitionService.getTransitions(promptState.get().getId());
            if (transFromPrompt.isPresent()) {
                log.info("Transitions from TestPrompt found in joint table");
                transFromPrompt.get().getTransitions().forEach(t -> {
                    log.info("  Transition activates: {}", t.getActivate());
                });
            } else {
                log.error("No transitions from TestPrompt in joint table!");
            }
        }
        
        assertFalse(paths.isEmpty(), 
            "Should be able to find path to open TestWorking from TestPrompt");
    }
}