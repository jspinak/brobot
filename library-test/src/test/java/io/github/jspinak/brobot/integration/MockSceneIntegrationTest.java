package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.tools.testing.mock.MockStatus;
import io.github.jspinak.brobot.tools.testing.mock.action.MockFind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for mock scene interactions.
 * Tests complex UI scenarios using mock objects to simulate real applications.
 */
@SpringBootTest
public class MockSceneIntegrationTest extends BrobotIntegrationTestBase {

    @Autowired
    private Action action;

    @Autowired(required = false)
    private MockFind mockFind;
    
    @Autowired(required = false)
    private MockStatus mockStatus;

    private State loginScreen;
    private State mainMenu;
    private State settingsScreen;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage loginButton;
    private StateImage settingsButton;
    private StateImage saveButton;

    @BeforeEach
    public void setupTest() {
        // Mock components are autowired, no need to instantiate
        setupMockStates();
    }

    private void setupMockStates() {
        // Create login screen state
        loginScreen = new State.Builder("LoginScreen").build();

        usernameField = new StateImage.Builder()
                .setName("usernameField")
                .setSearchRegionForAllPatterns(new Region(100, 200, 200, 30))
                .build();

        passwordField = new StateImage.Builder()
                .setName("passwordField")
                .setSearchRegionForAllPatterns(new Region(100, 250, 200, 30))
                .build();

        loginButton = new StateImage.Builder()
                .setName("loginButton")
                .setSearchRegionForAllPatterns(new Region(150, 300, 100, 40))
                .build();

        loginScreen.getStateImages().addAll(Arrays.asList(usernameField, passwordField, loginButton));

        // Create main menu state
        mainMenu = new State.Builder("MainMenu").build();

        settingsButton = new StateImage.Builder()
                .setName("settingsButton")
                .setSearchRegionForAllPatterns(new Region(500, 100, 80, 30))
                .build();

        mainMenu.getStateImages().add(settingsButton);

        // Create settings screen state
        settingsScreen = new State.Builder("SettingsScreen").build();

        saveButton = new StateImage.Builder()
                .setName("saveButton")
                .setSearchRegionForAllPatterns(new Region(400, 500, 100, 40))
                .build();

        settingsScreen.getStateImages().add(saveButton);
    }

    private Match createMatch(StateImage stateImage, int x, int y) {
        return new Match.Builder()
                .setStateObjectData(stateImage)
                .setRegion(new Region(x, y, 10, 10))
                .setSimScore(0.95)
                .build();
    }

    @Nested
    class BasicMockInteractions {

        @Test
        public void testMockLoginWorkflow() {
            // Simulate finding and interacting with login form
            ActionResult findUsername = action.find(usernameField);
            assertNotNull(findUsername, "Find result should not be null");
            
            // Click on username field - can click directly on found matches
            if (findUsername.isSuccess() && !findUsername.getMatchList().isEmpty()) {
                // In mock mode, we can just click directly on the image
                ActionResult clickUsername = action.click(usernameField);
                assertNotNull(clickUsername, "Click result should not be null");
            }

            // Type username
            ObjectCollection typeUsernameCollection = new ObjectCollection.Builder()
                    .withStrings("testuser")
                    .build();
            ActionResult typeUsername = action.type(typeUsernameCollection);
            assertNotNull(typeUsername, "Type result should not be null");

            // Find and interact with password field
            ActionResult findPassword = action.find(passwordField);
            assertNotNull(findPassword, "Password find result should not be null");

            if (findPassword.isSuccess() && !findPassword.getMatchList().isEmpty()) {
                // In mock mode, we can just click directly on the image
                action.click(passwordField);
            }

            // Type password
            ObjectCollection typePasswordCollection = new ObjectCollection.Builder()
                    .withStrings("testpass123")
                    .build();
            action.type(typePasswordCollection);

            // Click login button
            ActionResult clickLogin = action.click(loginButton);
            assertNotNull(clickLogin, "Login click result should not be null");
        }

        @Test
        public void testNavigationBetweenScreens() {
            // Start on main menu, navigate to settings
            // Check if we're on main menu
            ActionResult findSettings = action.find(settingsButton);
            assertNotNull(findSettings, "Settings find result should not be null");
            
            if (findSettings.isSuccess() && !findSettings.getMatchList().isEmpty()) {
                // Navigate to settings
                ActionResult clickSettings = action.click(settingsButton);
                assertNotNull(clickSettings, "Settings click result should not be null");
            }
            
            // Verify on settings screen
            ActionResult findSave = action.find(saveButton);
            assertNotNull(findSave, "Save button find result should not be null");
        }
    }

    @Nested
    class ComplexMockScenarios {

        @Test
        public void testConditionalWorkflowBasedOnMockState() {
            // Check if we're on login screen
            ActionResult loginCheckResult = action.find(loginButton);
            
            if (loginCheckResult != null && loginCheckResult.isSuccess()) {
                // We're on login screen - perform login
                ActionResult findUsername = action.find(usernameField);
                
                if (findUsername.isSuccess() && !findUsername.getMatchList().isEmpty()) {
                    action.click(usernameField);
                }
                
                ObjectCollection typeUsernameCollection = new ObjectCollection.Builder()
                        .withStrings("admin")
                        .build();
                action.type(typeUsernameCollection);
                
                ActionResult findPassword = action.find(passwordField);
                
                if (findPassword.isSuccess() && !findPassword.getMatchList().isEmpty()) {
                    action.click(passwordField);
                }
                
                ObjectCollection typePasswordCollection = new ObjectCollection.Builder()
                        .withStrings("admin123")
                        .build();
                action.type(typePasswordCollection);
                
                ActionResult loginResult = action.click(loginButton);
                assertNotNull(loginResult, "Login result should not be null");
            }
            
            // Now check if we're on main menu
            ActionResult menuCheck = action.find(settingsButton);
            
            assertNotNull(menuCheck, "Menu check result should not be null");
        }

        @Test
        public void testMockSceneWithMultipleStates() {
            // Test transitioning through multiple states
            State[] allStates = {loginScreen, mainMenu, settingsScreen};
            
            for (State state : allStates) {
                assertNotNull(state, "State should not be null");
                assertFalse(state.getStateImages().isEmpty(), "State should have images: " + state.getName());
                
                // Verify each state's images can be searched
                for (StateImage image : state.getStateImages()) {
                    ActionResult result = action.find(image);
                    assertNotNull(result, "Should get result for " + image.getName());
                }
            }
        }

        @Test
        public void testMockSceneWithActionOptions() {
            // Test with various action options
            PatternFindOptions strictFind = new PatternFindOptions.Builder()
                    .setSimilarity(0.95)
                    .build();
            
            // For actions with options, use the perform method
            ActionResult strictResult = action.perform(strictFind, loginButton.asObjectCollection());
            assertNotNull(strictResult, "Strict find result should not be null");
            
            // Test click with options
            ClickOptions doubleClick = new ClickOptions.Builder()
                    .setNumberOfClicks(2)
                    .build();
            
            // For actions with options, use the perform method
            ActionResult doubleClickResult = action.perform(doubleClick, settingsButton.asObjectCollection());
            assertNotNull(doubleClickResult, "Double click result should not be null");
            
            // Test type with options
            TypeOptions slowType = new TypeOptions.Builder()
                    .build();
            
            ObjectCollection slowTypeCollection = new ObjectCollection.Builder()
                    .withStrings("slow typing test")
                    .build();
            
            // For actions with options, use the perform method  
            ActionResult typeResult = action.perform(slowType, slowTypeCollection);
            assertNotNull(typeResult, "Type result should not be null");
        }
    }

    @Nested
    class MockSceneErrorHandling {

        @Test
        public void testHandleMissingMockElements() {
            // Create a state image that doesn't exist in any state
            StateImage nonExistentImage = new StateImage.Builder()
                    .setName("nonExistent")
                    .setSearchRegionForAllPatterns(new Region(0, 0, 10, 10))
                    .build();
            
            ObjectCollection searchCollection = new ObjectCollection.Builder()
                    .withImages(nonExistentImage)
                    .build();
            
            ActionResult result = action.find(searchCollection);
            assertNotNull(result, "Result should not be null even for non-existent elements");
            
            // In mock mode, operations should complete without throwing exceptions
            assertDoesNotThrow(() -> {
                action.click(nonExistentImage);
                action.type(new ObjectCollection.Builder().withStrings("test").build());
            });
        }

        @Test
        public void testRecoveryFromFailedMockActions() {
            // Simulate recovery from failed actions
            int maxRetries = 3;
            ActionResult successfulResult = null;
            
            for (int i = 0; i < maxRetries; i++) {
                ActionResult result = action.find(loginButton);
                if (result != null) {
                    successfulResult = result;
                    break;
                }
                
                // Wait before retry (in mock mode this is instant)
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            assertNotNull(successfulResult, "Should eventually get a result after retries");
        }
    }
}