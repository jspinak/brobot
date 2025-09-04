package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.navigation.service.StateService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Debug test to diagnose why the state transition isn't happening.
 */
@Disabled("Missing ClaudeAutomatorApplication dependency")
@SpringBootTest // (classes = com.claude.automator.ClaudeAutomatorApplication.class)
public class StateTransitionDebugTest extends BrobotTestBase {
    
    private static final Logger log = LoggerFactory.getLogger(StateTransitionDebugTest.class);

    @Autowired
    private StateMemory stateMemory;
    
    @Autowired
    private StateNavigator stateNavigator;
    
    @Autowired
    private StateService stateService;
    
    @Autowired
    private Action action;
    
    // @Autowired
    // private PromptState promptState;
    
    // @Autowired
    // private WorkingState workingState;
    
    // @Autowired(required = false)
    // private PromptToWorkingTransition transition;

    @Test
    public void debugStateTransition() {
        log.info("=== STATE TRANSITION DEBUG TEST ===");
        
        // 1. Check if states are registered
        log.info("Checking registered states...");
        var allStates = stateService.getAllStates();
        log.info("All registered states: {}", allStates);
        
        // 2. Check current active states
        log.info("\nChecking active states...");
        var activeStates = stateMemory.getActiveStateNames();
        log.info("Active states: {}", activeStates);
        
        // 3. Try to find the prompt image
        log.info("\nAttempting to find ClaudePrompt image...");
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchDuration(3)
                .setSimilarity(0.7) // Lower threshold for testing
                .build();
        
        ObjectCollection target = new ObjectCollection.Builder()
                .withImages(promptState.getClaudePrompt())
                .build();
        
        ActionResult findResult = action.perform(findOptions, target);
        log.info("Find result: success={}, matches={}", 
                findResult.isSuccess(), findResult.getMatchList().size());
        
        if (!findResult.isSuccess()) {
            log.warn("Could not find ClaudePrompt image!");
            log.info("ClaudePrompt patterns: {}", 
                    promptState.getClaudePrompt().getPatterns());
        }
        
        // 4. Manually activate Prompt state if needed
        if (!activeStates.contains("Prompt")) {
            log.info("\nManually activating Prompt state...");
            Long promptStateId = stateService.getStateId("Prompt");
            if (promptStateId != null) {
                stateMemory.addActiveState(promptStateId);
                activeStates = stateMemory.getActiveStateNames();
                log.info("Active states after manual activation: {}", activeStates);
            } else {
                log.warn("Could not find Prompt state ID!");
            }
        }
        
        // 5. Try to navigate to Working state
        log.info("\nAttempting to navigate to Working state...");
        boolean navSuccess = stateNavigator.openState("Working");
        log.info("Navigation result: {}", navSuccess);
        
        // 6. Check if transition is available
        if (transition != null) {
            log.info("\nTransition object is available");
            log.info("Attempting direct transition execution...");
            boolean transitionSuccess = transition.execute();
            log.info("Direct transition result: {}", transitionSuccess);
        } else {
            log.warn("PromptToWorkingTransition is not autowired!");
        }
        
        // 7. Final state check
        log.info("\nFinal active states: {}", stateMemory.getActiveStateNames());
        
        log.info("=== END DEBUG TEST ===");
    }
}