package io.github.jspinak.brobot.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Test to verify mock mode functionality with ActionHistory. */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Missing PromptState and WorkingState dependencies")
public class MockModeTest extends BrobotTestBase {

    @Autowired private BrobotProperties brobotProperties;

    @Autowired(required = false)
    private Action action;

    @BeforeEach
    public void setup() {
        // Ensure mock mode is enabled
        System.out.println("Mock mode enabled: " + brobotProperties.getCore().isMock());
    }

    @Test
    @Disabled("PromptState class not available")
    public void testActionHistoryIsSet() {
        System.out.println("\n=== Testing ActionHistory Setup ===");

        // Create a PromptState
        // PromptState promptState = new PromptState();
        // StateImage claudePrompt = promptState.getClaudePrompt();

        // assertNotNull(claudePrompt, "ClaudePrompt should not be null");
        // assertFalse(claudePrompt.getPatterns().isEmpty(), "Should have patterns");

        // Check each pattern has ActionHistory
        // for (Pattern pattern : claudePrompt.getPatterns()) {
        //     System.out.println("Pattern: " + pattern.getImgpath());
        //     assertNotNull(pattern.getMatchHistory(),
        //             "Pattern " + pattern.getImgpath() + " should have ActionHistory");

        //     if (pattern.getMatchHistory() != null) {
        //         assertFalse(pattern.getMatchHistory().getSnapshots().isEmpty(),
        //                 "ActionHistory should have snapshots");
        //         System.out.println("  Snapshots: " +
        //                 pattern.getMatchHistory().getSnapshots().size());
        //     }
        // }
    }

    @Test
    @Disabled("PromptState class not available")
    public void testMockFind() {
        if (action == null) {
            System.out.println("Action not autowired, skipping find test");
            return;
        }

        System.out.println("\n=== Testing Mock Find ===");
        System.out.println("Mock mode: " + brobotProperties.getCore().isMock());

        // Create state and try to find
        // PromptState promptState = new PromptState();
        // StateImage claudePrompt = promptState.getClaudePrompt();

        ObjectCollection objects =
                new ObjectCollection.Builder()
                        // .withImages(claudePrompt)
                        .build();

        // Attempt find in mock mode
        ActionResult result = action.find(objects);

        System.out.println("Find result: " + result);
        System.out.println("Success: " + result.isSuccess());
        System.out.println("Matches: " + result.getMatchList().size());

        // In mock mode with ActionHistory, this should succeed
        assertTrue(
                result.isSuccess() || !result.getMatchList().isEmpty(),
                "Mock find should succeed with ActionHistory");
    }
}
