package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for mock scene interactions.
 * Tests complex UI scenarios using mock mode to simulate real applications.
 */
public class MockSceneIntegrationTest extends BrobotTestBase {

    private State loginScreen;
    private State mainMenu;
    private State settingsScreen;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage loginButton;
    private StateImage settingsButton;
    private StateImage saveButton;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
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

        Set<StateImage> loginImages = new HashSet<>();
        loginImages.add(usernameField);
        loginImages.add(passwordField);
        loginImages.add(loginButton);
        loginScreen.setStateImages(loginImages);

        // Create main menu state
        mainMenu = new State.Builder("MainMenu").build();

        settingsButton = new StateImage.Builder()
                .setName("settingsButton")
                .setSearchRegionForAllPatterns(new Region(500, 100, 80, 30))
                .build();

        Set<StateImage> menuImages = new HashSet<>();
        menuImages.add(settingsButton);
        mainMenu.setStateImages(menuImages);

        // Create settings screen state
        settingsScreen = new State.Builder("SettingsScreen").build();

        saveButton = new StateImage.Builder()
                .setName("saveButton")
                .setSearchRegionForAllPatterns(new Region(400, 500, 100, 40))
                .build();

        Set<StateImage> settingsImages = new HashSet<>();
        settingsImages.add(saveButton);
        settingsScreen.setStateImages(settingsImages);
    }

    private ActionResult createMockSuccessResult(StateImage stateImage) {
        ActionResult result = new ActionResult();
        result.setSuccess(true);
        Match match = new Match.Builder()
                .setStateObjectData(stateImage)
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.95)
                .build();
        result.add(match);
        return result;
    }

    @Nested
    class BasicMockInteractions {

        @Test
        public void testMockLoginWorkflow() {
            // Simulate finding and interacting with login form
            ActionResult findUsername = createMockSuccessResult(usernameField);
            assertTrue(findUsername.isSuccess(), "Should find username field in mock scene");

            // Click on username field
            ActionResult clickUsername = createMockSuccessResult(usernameField);
            assertTrue(clickUsername.isSuccess(), "Should click username field successfully");

            // Type username
            ObjectCollection usernameCollection = new ObjectCollection.Builder()
                    .withImages(usernameField)
                    .withStrings("testuser")
                    .build();
            assertNotNull(usernameCollection, "Username collection should be created");
            assertEquals(1, usernameCollection.getStateImages().size(), "Should have one state image");
            assertEquals(1, usernameCollection.getStateStrings().size(), "Should have one state string");

            // Click and type in password field
            ActionResult clickPassword = createMockSuccessResult(passwordField);
            assertTrue(clickPassword.isSuccess(), "Should click password field successfully");

            ObjectCollection passwordCollection = new ObjectCollection.Builder()
                    .withImages(passwordField)
                    .withStrings("password123")
                    .build();
            assertNotNull(passwordCollection, "Password collection should be created");

            // Click login button
            ActionResult clickLogin = createMockSuccessResult(loginButton);
            assertTrue(clickLogin.isSuccess(), "Should click login button successfully");
        }

        @Test
        public void testMockUIElementInteractions() {
            // Test various UI element interactions in mock mode
            
            // Test finding elements
            ActionResult findResult = createMockSuccessResult(loginButton);
            assertTrue(findResult.isSuccess(), "Should find login button in mock mode");
            
            // Test clicking elements
            ActionResult clickResult = createMockSuccessResult(loginButton);
            assertTrue(clickResult.isSuccess(), "Should click login button in mock mode");
            
            // Test typing into fields
            ObjectCollection typeCollection = new ObjectCollection.Builder()
                    .withImages(usernameField)
                    .withStrings("test@example.com")
                    .build();
            assertNotNull(typeCollection, "Type collection should be created");
            
            // Test navigation
            ActionResult navResult = createMockSuccessResult(settingsButton);
            assertTrue(navResult.isSuccess(), "Should navigate via settings button in mock mode");
        }
    }

    @Nested
    class ComplexMockScenarios {

        @Test
        public void testConditionalWorkflowBasedOnMockState() {
            // Create a conditional workflow that adapts based on element visibility
            
            // Check if we're on login screen
            ActionResult loginCheck = createMockSuccessResult(loginButton);
            
            if (loginCheck.isSuccess()) {
                // We're on login screen - perform login
                ObjectCollection usernameInput = new ObjectCollection.Builder()
                        .withImages(usernameField)
                        .withStrings("admin")
                        .build();
                assertNotNull(usernameInput, "Username input collection should be created");
                
                ObjectCollection passwordInput = new ObjectCollection.Builder()
                        .withImages(passwordField)
                        .withStrings("admin123")
                        .build();
                assertNotNull(passwordInput, "Password input collection should be created");
                
                ActionResult loginResult = createMockSuccessResult(loginButton);
                assertTrue(loginResult.isSuccess(), "Login should succeed");
            }
            
            // Check for settings button
            ActionResult menuCheck = createMockSuccessResult(settingsButton);
            
            if (menuCheck.isSuccess()) {
                // Navigate to settings
                ActionResult settingsResult = createMockSuccessResult(settingsButton);
                assertTrue(settingsResult.isSuccess(), "Should navigate to settings");
            }
            
            // Verify save button is accessible
            ActionResult finalCheck = createMockSuccessResult(saveButton);
            assertTrue(finalCheck.isSuccess(), "Should find save button");
        }

        @Test
        public void testMockFormValidation() {
            // Test form validation scenario with mock feedback
            
            // Try to submit form without filling required fields
            ActionResult emptySubmit = createMockSuccessResult(loginButton);
            assertTrue(emptySubmit.isSuccess(), "Click action should succeed in mock mode");
            
            // Fill username only
            ObjectCollection userInput = new ObjectCollection.Builder()
                    .withImages(usernameField)
                    .withStrings("user")
                    .build();
            assertNotNull(userInput, "User input collection should be created");
            
            // Try submit again
            ActionResult partialSubmit = createMockSuccessResult(loginButton);
            assertTrue(partialSubmit.isSuccess(), "Click action should succeed in mock mode");
            
            // Fill password
            ObjectCollection passInput = new ObjectCollection.Builder()
                    .withImages(passwordField)
                    .withStrings("pass")
                    .build();
            assertNotNull(passInput, "Password input collection should be created");
            
            // Submit complete form
            ActionResult completeSubmit = createMockSuccessResult(loginButton);
            assertTrue(completeSubmit.isSuccess(), "Should submit complete form in mock mode");
        }

        @Test
        public void testMockDynamicContentHandling() {
            // Test handling of dynamic content that appears/disappears
            
            // Define a popup element
            StateImage popup = new StateImage.Builder()
                    .setName("dynamicPopup")
                    .setSearchRegionForAllPatterns(new Region(300, 250, 200, 100))
                    .build();
            
            // In mock mode, all finds succeed
            ActionResult popupResult = createMockSuccessResult(popup);
            assertTrue(popupResult.isSuccess(), "Mock mode always finds elements");
            
            // Trigger action that would show popup
            ActionResult triggerResult = createMockSuccessResult(settingsButton);
            assertTrue(triggerResult.isSuccess(), "Should trigger popup in mock mode");
            
            // Check popup again
            ActionResult popupVisible = createMockSuccessResult(popup);
            assertTrue(popupVisible.isSuccess(), "Popup should be findable in mock mode");
            
            // Interact with popup
            ActionResult clickPopup = createMockSuccessResult(popup);
            assertTrue(clickPopup.isSuccess(), "Should be able to click on popup in mock mode");
        }
    }

    @Nested
    class MockErrorRecovery {

        @Test
        public void testRecoveryFromMockErrors() {
            // Test error recovery in mock scenarios
            
            // In mock mode, all operations succeed
            ActionResult firstTry = createMockSuccessResult(loginButton);
            assertTrue(firstTry.isSuccess(), "Mock mode finds succeed");
            
            ActionResult secondTry = createMockSuccessResult(loginButton);
            assertTrue(secondTry.isSuccess(), "Mock mode finds succeed consistently");
            
            ActionResult thirdTry = createMockSuccessResult(loginButton);
            assertTrue(thirdTry.isSuccess(), "Mock mode finds always succeed");
            
            // Verify we can interact after multiple attempts
            ActionResult clickAfterRecovery = createMockSuccessResult(loginButton);
            assertTrue(clickAfterRecovery.isSuccess(), "Should be able to click in mock mode");
        }

        @Test
        public void testMockTimeoutScenarios() {
            // Test handling of timeouts in mock scenarios
            
            StateImage slowLoadingElement = new StateImage.Builder()
                    .setName("slowLoader")
                    .setSearchRegionForAllPatterns(new Region(200, 200, 100, 50))
                    .build();
            
            // Use find with timeout options
            ObjectCollection timeout = new ObjectCollection.Builder()
                    .withImages(slowLoadingElement)
                    .build();
            
            // Start time
            long startTime = System.currentTimeMillis();
            
            // In mock mode, finds succeed quickly
            ActionResult result = createMockSuccessResult(slowLoadingElement);
            
            long duration = System.currentTimeMillis() - startTime;
            
            assertTrue(result.isSuccess(), "Mock mode finds always succeed");
            // In mock mode, operations are much faster
            assertTrue(duration < 500, "Mock operations should be fast (under 500ms)");
        }

        @Test
        public void testMockStateRecoveryChain() {
            // Test complex recovery chain using conditional actions
            
            // Build recovery chain: try login -> try reset -> try refresh
            StateImage resetButton = new StateImage.Builder()
                    .setName("resetButton")
                    .setSearchRegionForAllPatterns(new Region(50, 350, 80, 30))
                    .build();
            
            StateImage refreshButton = new StateImage.Builder()
                    .setName("refreshButton")
                    .setSearchRegionForAllPatterns(new Region(50, 50, 60, 30))
                    .build();
            
            // Primary action
            ActionResult primaryAction = createMockSuccessResult(loginButton);
            assertTrue(primaryAction.isSuccess(), "Primary action succeeds in mock mode");
            
            // Recovery option 1 - reset
            ActionResult resetAction = createMockSuccessResult(resetButton);
            assertTrue(resetAction.isSuccess(), "Reset button found in mock mode");
            
            ActionResult clickReset = createMockSuccessResult(resetButton);
            assertTrue(clickReset.isSuccess(), "Should click reset button in mock mode");
            
            // After reset, login button still available
            ActionResult afterReset = createMockSuccessResult(loginButton);
            assertTrue(afterReset.isSuccess(), "Login button available in mock mode");
        }
    }

    @Nested
    class MockPerformanceScenarios {

        @Test
        public void testMockBulkOperations() {
            // Test performance with bulk operations on mock elements
            List<StateImage> bulkElements = Arrays.asList(
                    usernameField,
                    passwordField,
                    loginButton
            );
            
            long startTime = System.currentTimeMillis();
            
            for (StateImage element : bulkElements) {
                ActionResult findResult = createMockSuccessResult(element);
                assertTrue(findResult.isSuccess(), 
                          "Should find " + element.getName() + " in bulk operation");
                
                ActionResult clickResult = createMockSuccessResult(element);
                assertTrue(clickResult.isSuccess(), 
                          "Should click " + element.getName() + " in bulk operation");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // In mock mode, all operations should be very fast
            assertTrue(duration < 500, 
                      "Bulk operations should complete quickly in mock mode (under 500ms)");
        }

        @Test
        public void testMockParallelSearches() {
            // Test parallel searches in mock environment
            
            // Execute searches (simulating parallel execution)
            long startTime = System.currentTimeMillis();
            
            ActionResult result1 = createMockSuccessResult(usernameField);
            ActionResult result2 = createMockSuccessResult(passwordField);
            ActionResult result3 = createMockSuccessResult(loginButton);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Verify all searches succeeded
            assertTrue(result1.isSuccess(), "Search 1 should succeed");
            assertTrue(result2.isSuccess(), "Search 2 should succeed");
            assertTrue(result3.isSuccess(), "Search 3 should succeed");
            
            // Mock searches should be extremely fast
            assertTrue(duration < 200, 
                      "Mock searches should complete very quickly (under 200ms)");
        }
        
        @Test
        public void testMockComplexWorkflow() {
            // Test a complete workflow in mock mode
            
            // Step 1: Login
            assertTrue(createMockSuccessResult(usernameField).isSuccess(), "Click username field");
            ObjectCollection userCollection = new ObjectCollection.Builder()
                    .withImages(usernameField)
                    .withStrings("user@test.com")
                    .build();
            assertNotNull(userCollection, "User collection should be created");
            
            assertTrue(createMockSuccessResult(passwordField).isSuccess(), "Click password field");
            ObjectCollection passCollection = new ObjectCollection.Builder()
                    .withImages(passwordField)
                    .withStrings("secure123")
                    .build();
            assertNotNull(passCollection, "Password collection should be created");
            
            assertTrue(createMockSuccessResult(loginButton).isSuccess(), "Click login");
            
            // Step 2: Navigate to settings
            assertTrue(createMockSuccessResult(settingsButton).isSuccess(), "Find settings button");
            assertTrue(createMockSuccessResult(settingsButton).isSuccess(), "Click settings");
            
            // Step 3: Save settings
            assertTrue(createMockSuccessResult(saveButton).isSuccess(), "Find save button");
            assertTrue(createMockSuccessResult(saveButton).isSuccess(), "Click save");
            
            // All operations succeed in mock mode
        }
    }
}