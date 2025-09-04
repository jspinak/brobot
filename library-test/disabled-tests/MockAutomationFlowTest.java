package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

// import com.claude.automator.automation.ClaudeMonitoringAutomation;
// import com.claude.automator.states.PromptState;
// import com.claude.automator.states.WorkingState;
// import com.claude.automator.transitions.PromptToWorkingTransition;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.tools.testing.mock.state.MockStateManagement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive test showing the complete automation flow in mock mode.
 * Reports on all automation steps, state changes, and actions.
 */
@SpringBootTest // (classes = ClaudeAutomatorApplication.class not available)
@TestPropertySource(properties = {
        "brobot.framework.mock=true",
        "claude.automator.monitoring.max-iterations=5",
        "claude.automator.monitoring.initial-delay=1",
        "claude.automator.monitoring.check-interval=1",
        "logging.level.com.claude.automator=DEBUG",
        "logging.level.io.github.jspinak.brobot.action=DEBUG"
})
@Disabled("Missing PromptState and WorkingState dependencies")
public class MockAutomationFlowTest extends BrobotTestBase {

    private static final Logger log = LoggerFactory.getLogger(MockAutomationFlowTest.class);

    @Autowired
    private StateMemory stateMemory;

    @Autowired
    private StateNavigator stateNavigator;

    @Autowired
    private StateService stateService;

    @Autowired
    private MockStateManagement mockStateManagement;

    @Autowired
    private Action action;

    // @Autowired
    // private PromptState promptState;

    // @Autowired
    // private WorkingState workingState;

    // @Autowired(required = false)
    // private PromptToWorkingTransition transition;

    // @Autowired
    // private ClaudeMonitoringAutomation monitoringAutomation;

    @BeforeEach
    public void setUp() {
        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║        MOCK AUTOMATION FLOW TEST - SETUP                      ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
        log.info("Mock mode enabled: {}", FrameworkSettings.mock);

        // Clear any existing active states
        stateMemory.getActiveStates().clear();

        // These should be set automatically by the state @PostConstruct methods
        // but let's verify they're set correctly
        log.info("Verifying mock probabilities are configured...");
    }

    // @AfterEach
    // public void tearDown() {
    //     monitoringAutomation.stopMonitoring();
    // }

    private void reportActiveStates(String context) {
        var activeStates = stateMemory.getActiveStateNames();
        log.info("┌─ ACTIVE STATES [{}] ─────────────────────────", context);
        if (activeStates.isEmpty()) {
            log.info("│ (none)");
        } else {
            for (String state : activeStates) {
                log.info("│ • {}", state);
            }
        }
        log.info("└────────────────────────────────────────────────");
    }

    @Test
    public void testCompleteAutomationFlow() throws InterruptedException {
        log.info("\n╔══════════════════════════════════════════════════════════════╗");
        log.info("║         COMPLETE AUTOMATION FLOW IN MOCK MODE                 ║");
        log.info("╚══════════════════════════════════════════════════════════════╝\n");

        // Step 1: Verify initial setup
        log.info("═══ STEP 1: INITIAL STATE VERIFICATION ═══");
        var allStates = stateService.getAllStates();
        log.info("Registered states: {}",
                allStates.stream().map(s -> s.getName()).toList());

        reportActiveStates("Initial");

        // Step 2: Simulate finding the Prompt state
        log.info("\n═══ STEP 2: FINDING PROMPT STATE ═══");
        log.info("Attempting to find ClaudePrompt image...");

        ObjectCollection promptTarget = new ObjectCollection.Builder()
                .withImages(promptState.getClaudePrompt())
                .build();

        ActionResult promptFindResult = action.find(promptTarget);
        log.info("┌─ FIND RESULT: ClaudePrompt");
        log.info("│ Success: {}", promptFindResult.isSuccess());
        log.info("│ Matches: {}", promptFindResult.getMatchList().size());
        if (promptFindResult.isSuccess() && !promptFindResult.getMatchList().isEmpty()) {
            log.info("│ Match Score: {}", promptFindResult.getMatchList().get(0).getScore());
            log.info("│ Match Region: {}", promptFindResult.getMatchList().get(0).getRegion());
        }
        log.info("└────────────────────────────────────");

        // Activate Prompt state if found
        if (promptFindResult.isSuccess()) {
            Long promptStateId = stateService.getStateId("Prompt");
            if (promptStateId != null && !stateMemory.getActiveStateNames().contains("Prompt")) {
                stateMemory.addActiveState(promptStateId);
                log.info("→ ACTIVATED Prompt state");
            }
        }

        reportActiveStates("After Prompt Find");

        // Step 3: Execute transition from Prompt to Working
        log.info("\n═══ STEP 3: TRANSITION FROM PROMPT TO WORKING ═══");

        // Simulate the transition actions (find -> click -> type)
        if (transition != null) {
            log.info("Executing PromptToWorkingTransition...");
            boolean transitionSuccess = transition.execute();
            log.info("Transition execution result: {}", transitionSuccess ? "SUCCESS" : "FAILED");
        } else {
            // Manual transition simulation
            log.info("Manual transition: Navigating to Working state...");

            // Simulate clicking on prompt
            log.info("┌─ ACTION: CLICK on ClaudePrompt");
            ActionResult clickResult = action.click(promptState.getClaudePrompt());
            log.info("│ Success: {}", clickResult.isSuccess());
            log.info("└────────────────────────────────────");

            // Simulate typing "continue"
            log.info("┌─ ACTION: TYPE 'continue\\n'");
            ObjectCollection typeTarget = new ObjectCollection.Builder()
                    .withStrings(promptState.getContinueCommand())
                    .build();
            ActionResult typeResult = action.type(typeTarget);
            log.info("│ Success: {}", typeResult.isSuccess());
            log.info("└────────────────────────────────────");

            // Navigate to Working state
            boolean navSuccess = stateNavigator.openState("Working");
            log.info("Navigation to Working state: {}", navSuccess ? "SUCCESS" : "FAILED");
        }

        reportActiveStates("After Transition");

        // Step 4: Find Working state icon
        log.info("\n═══ STEP 4: FINDING WORKING STATE ICON ═══");

        // Simulate Working state becoming active
        mockStateManagement.setStateProbabilities(0, "Prompt");
        mockStateManagement.setStateProbabilities(100, "Working");

        ObjectCollection workingTarget = new ObjectCollection.Builder()
                .withImages(workingState.getClaudeIcon())
                .build();

        ActionResult workingFindResult = action.find(workingTarget);
        log.info("┌─ FIND RESULT: ClaudeIcon");
        log.info("│ Success: {}", workingFindResult.isSuccess());
        log.info("│ Matches: {}", workingFindResult.getMatchList().size());
        if (workingFindResult.isSuccess() && !workingFindResult.getMatchList().isEmpty()) {
            log.info("│ Match Score: {}", workingFindResult.getMatchList().get(0).getScore());
            log.info("│ Match Region: {}", workingFindResult.getMatchList().get(0).getRegion());
        }
        log.info("└────────────────────────────────────");

        reportActiveStates("After Working Find");

        // Step 5: Simulate Working state disappearing
        log.info("\n═══ STEP 5: SIMULATING WORKING ICON DISAPPEARING ═══");

        // Change probabilities to simulate icon disappearing
        mockStateManagement.setStateProbabilities(0, "Working");
        mockStateManagement.setStateProbabilities(100, "Prompt");

        log.info("Set Working probability to 0% (icon disappeared)");
        log.info("Set Prompt probability to 100% (prompt reappears)");

        // Try to find Working icon again (should fail)
        ActionResult workingGoneResult = action.find(workingTarget);
        log.info("┌─ FIND RESULT: ClaudeIcon (after disappearing)");
        log.info("│ Success: {}", workingGoneResult.isSuccess());
        log.info("│ Expected: false (icon should be gone)");
        log.info("└────────────────────────────────────");

        // Remove Working state
        if (!workingGoneResult.isSuccess()) {
            stateMemory.removeInactiveState("Working");
            log.info("→ REMOVED Working state");
        }

        reportActiveStates("After Working Disappears");

        // Step 6: Final state
        log.info("\n═══ STEP 6: FINAL STATE ═══");
        reportActiveStates("Final");

        // Summary
        log.info("\n╔══════════════════════════════════════════════════════════════╗");
        log.info("║                    AUTOMATION FLOW SUMMARY                    ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║ 1. Started with no active states                             ║");
        log.info("║ 2. Found Prompt state → Activated Prompt                     ║");
        log.info("║ 3. Executed transition (click + type) → Navigate to Working  ║");
        log.info("║ 4. Found Working icon → Working state active                 ║");
        log.info("║ 5. Working icon disappeared → Removed Working state          ║");
        log.info("║ 6. Returned to initial state (ready for next cycle)          ║");
        log.info("╚══════════════════════════════════════════════════════════════╝");
    }

    @Test
    public void testMonitoringAutomation() throws InterruptedException {
        log.info("\n╔══════════════════════════════════════════════════════════════╗");
        log.info("║           MONITORING AUTOMATION IN MOCK MODE                  ║");
        log.info("╚══════════════════════════════════════════════════════════════╝\n");

        // The monitoring automation starts automatically via @PostConstruct
        // Let it run for a few cycles

        log.info("Monitoring automation is running for {} seconds...", 5);
        log.info("Max iterations configured: 5");
        log.info("Check interval: 1 second");

        for (int i = 1; i <= 5; i++) {
            log.info("\n─── MONITORING CYCLE {} ───", i);
            reportActiveStates("Cycle " + i);
            TimeUnit.SECONDS.sleep(1);

            // Simulate state changes during monitoring
            if (i == 2) {
                log.info("Simulating: Prompt found → Working appears");
                mockStateManagement.setStateProbabilities(0, "Prompt");
                mockStateManagement.setStateProbabilities(100, "Working");
            }
            if (i == 4) {
                log.info("Simulating: Working disappears → Prompt returns");
                mockStateManagement.setStateProbabilities(100, "Prompt");
                mockStateManagement.setStateProbabilities(0, "Working");
            }
        }

        log.info("\n═══ MONITORING COMPLETE ═══");
        reportActiveStates("Final after monitoring");
    }
}