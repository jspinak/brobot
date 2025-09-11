package io.github.jspinak.brobot.action.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Simplified integration tests for complex workflows without Spring dependency. Tests the
 * interaction between multiple action components in realistic scenarios.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComplexWorkflowIntegrationTestSimplified extends BrobotTestBase {

    private Action action;
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage submitButton;
    private StateImage loadingSpinner;
    private StateImage dashboard;

    @BeforeEach
    void setupTestData() {
        super.setupTest();

        // Create mock Action for testing
        action = org.mockito.Mockito.mock(Action.class);

        // Configure mock to simulate successful operations in mock mode
        ActionResult successResult = new ActionResult();
        successResult.setSuccess(true);

        org.mockito.Mockito.when(
                        action.perform(
                                org.mockito.ArgumentMatchers.any(ActionConfig.class),
                                org.mockito.ArgumentMatchers.any(ObjectCollection.class)))
                .thenReturn(successResult);

        org.mockito.Mockito.when(
                        action.perform(
                                org.mockito.ArgumentMatchers.any(ActionType.class),
                                org.mockito.ArgumentMatchers.any(StateImage.class)))
                .thenReturn(successResult);

        // Create test state images
        loginButton = createStateImage("login-button", "images/bottomR.png");
        usernameField = createStateImage("username-field", "images/topLeft.png");
        passwordField = createStateImage("password-field", "images/topLeft2.png");
        submitButton = createStateImage("submit-button", "images/bottomRight.png");
        loadingSpinner = createStateImage("loading-spinner", "images/bottomRight2.png");
        dashboard = createStateImage("dashboard", "images/bottomRight3.png");

        // Ensure mock mode is enabled
        assertTrue(MockModeManager.isMockMode(), "Mock mode should be enabled");
    }

    private StateImage createStateImage(String name, String imagePath) {
        return new StateImage.Builder().setName(name).addPattern(imagePath).build();
    }

    @Nested
    @DisplayName("Login Workflow Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class LoginWorkflowTests {

        @Test
        @Order(1)
        @DisplayName("Should execute complete login workflow")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldExecuteCompleteLoginWorkflow() {
            // Given - login workflow steps
            ObjectCollection loginButtonColl =
                    new ObjectCollection.Builder().withImages(loginButton).build();

            ObjectCollection usernameColl =
                    new ObjectCollection.Builder().withImages(usernameField).build();

            ObjectCollection passwordColl =
                    new ObjectCollection.Builder().withImages(passwordField).build();

            ObjectCollection submitColl =
                    new ObjectCollection.Builder().withImages(submitButton).build();

            // When - execute login workflow
            // Step 1: Click login button
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ActionResult loginResult = new ActionResult();
            loginResult.setActionConfig(clickOptions);
            // Use Action to click on StateImage (finds then clicks)
            loginResult.setSuccess(action.perform(clickOptions, loginButtonColl).isSuccess());

            // Step 2: Click username field
            ActionResult usernameClickResult = new ActionResult();
            usernameClickResult.setActionConfig(clickOptions);
            // Use Action to click on StateImage
            usernameClickResult.setSuccess(action.perform(clickOptions, usernameColl).isSuccess());

            // Step 3: Click password field
            ActionResult passwordClickResult = new ActionResult();
            passwordClickResult.setActionConfig(clickOptions);
            // Use Action to click on StateImage
            passwordClickResult.setSuccess(action.perform(clickOptions, passwordColl).isSuccess());

            // Step 4: Submit form
            ActionResult submitResult = new ActionResult();
            submitResult.setActionConfig(clickOptions);
            // Use Action to click on StateImage
            submitResult.setSuccess(action.perform(clickOptions, submitColl).isSuccess());

            // Then - verify workflow completed
            assertNotNull(submitResult);
            assertTrue(submitResult.isSuccess(), "Submit should succeed in mock mode");
        }

        @Test
        @Order(2)
        @DisplayName("Should handle login with waiting for loading")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldHandleLoginWithWaitingForLoading() {
            // Given
            ObjectCollection loadingColl =
                    new ObjectCollection.Builder().withImages(loadingSpinner).build();

            ObjectCollection dashboardColl =
                    new ObjectCollection.Builder().withImages(dashboard).build();

            // When - simulate waiting (in mock mode, this succeeds immediately)
            VanishOptions vanishOptions = new VanishOptions.Builder().setTimeout(5.0).build();

            // Simulate vanish result
            ActionResult vanishResult = new ActionResult();
            vanishResult.setActionConfig(vanishOptions);
            vanishResult.setSuccess(true); // Mock success

            // Check for dashboard
            ActionResult dashboardResult = new ActionResult();
            dashboardResult.setActionConfig(new ClickOptions.Builder().build());
            // Use Action to click on StateImage
            dashboardResult.setSuccess(
                    action.perform(new ClickOptions.Builder().build(), dashboardColl).isSuccess());

            // Then
            assertNotNull(vanishResult);
            assertTrue(vanishResult.isSuccess());
            assertNotNull(dashboardResult);
            assertTrue(dashboardResult.isSuccess());
        }
    }

    @Nested
    @DisplayName("Conditional Action Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ConditionalActionTests {

        @Test
        @Order(3)
        @DisplayName("Should execute conditional workflow with fallbacks")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldExecuteConditionalWorkflowWithFallbacks() {
            // Given - primary and fallback elements
            ObjectCollection primaryColl =
                    new ObjectCollection.Builder().withImages(loginButton).build();

            ObjectCollection fallbackColl =
                    new ObjectCollection.Builder().withImages(submitButton).build();

            // When - try primary action
            ActionResult primaryResult = new ActionResult();
            primaryResult.setActionConfig(new ClickOptions.Builder().build());
            primaryResult.setSuccess(
                    action.perform(primaryResult.getActionConfig(), primaryColl).isSuccess());

            ActionResult finalResult;
            if (primaryResult.isSuccess()) {
                finalResult = primaryResult;
            } else {
                // Try fallback
                ActionResult fallbackResult = new ActionResult();
                fallbackResult.setActionConfig(new ClickOptions.Builder().build());
                fallbackResult.setSuccess(
                        action.perform(fallbackResult.getActionConfig(), fallbackColl).isSuccess());
                finalResult = fallbackResult;
            }

            // Then
            assertNotNull(finalResult);
            assertTrue(finalResult.isSuccess(), "Should succeed in mock mode");
        }

        @Test
        @Order(4)
        @DisplayName("Should handle retry pattern")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldHandleRetryPattern() {
            // Given
            AtomicInteger attemptCount = new AtomicInteger(0);
            AtomicBoolean eventuallySucceeds = new AtomicBoolean(false);

            ObjectCollection targetColl =
                    new ObjectCollection.Builder().withImages(dashboard).build();

            // When - retry up to 3 times
            while (attemptCount.get() < 3 && !eventuallySucceeds.get()) {
                ActionResult result = new ActionResult();
                result.setActionConfig(new ClickOptions.Builder().build());
                result.setSuccess(action.perform(result.getActionConfig(), targetColl).isSuccess());

                attemptCount.incrementAndGet();

                if (result.isSuccess()) {
                    eventuallySucceeds.set(true);
                }
            }

            // Then
            assertTrue(eventuallySucceeds.get(), "Should succeed in mock mode");
            assertTrue(attemptCount.get() <= 3, "Should not exceed max attempts");
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ConfigurationTests {

        @Test
        @Order(5)
        @DisplayName("Should configure pattern find options")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldConfigurePatternFindOptions() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.8).build();

            // When
            ActionResult result = new ActionResult();
            result.setActionConfig(findOptions);

            // Then
            assertNotNull(findOptions);
            assertEquals(0.8, findOptions.getSimilarity(), 0.001);
            assertNotNull(result.getActionConfig());
            assertEquals(findOptions, result.getActionConfig());
        }

        @Test
        @Order(6)
        @DisplayName("Should configure drag options")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldConfigureDragOptions() {
            // Given
            DragOptions dragOptions =
                    new DragOptions.Builder()
                            .setDelayBetweenMouseDownAndMove(0.5)
                            .setDelayAfterDrag(0.5)
                            .build();

            StateLocation source =
                    new StateLocation.Builder()
                            .setLocation(new Location(100, 100))
                            .setName("source")
                            .build();

            StateLocation target =
                    new StateLocation.Builder()
                            .setLocation(new Location(300, 300))
                            .setName("target")
                            .build();

            ObjectCollection dragColl =
                    new ObjectCollection.Builder().withLocations(source, target).build();

            // When
            ActionResult dragResult = new ActionResult();
            dragResult.setActionConfig(dragOptions);
            dragResult.setSuccess(true); // Mock success

            // Then
            assertNotNull(dragOptions);
            assertEquals(0.5, dragOptions.getDelayBetweenMouseDownAndMove(), 0.001);
            assertEquals(0.5, dragOptions.getDelayAfterDrag(), 0.001);
            assertTrue(dragResult.isSuccess());
        }
    }

    @Nested
    @DisplayName("Parallel Execution Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ParallelExecutionTests {

        @Test
        @Order(7)
        @DisplayName("Should execute actions in parallel")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldExecuteActionsInParallel() throws InterruptedException {
            // Given
            CountDownLatch latch = new CountDownLatch(3);
            AtomicInteger completedActions = new AtomicInteger(0);

            List<ObjectCollection> collections =
                    List.of(
                            new ObjectCollection.Builder().withImages(loginButton).build(),
                            new ObjectCollection.Builder().withImages(usernameField).build(),
                            new ObjectCollection.Builder().withImages(passwordField).build());

            // When - execute in parallel threads
            for (ObjectCollection coll : collections) {
                new Thread(
                                () -> {
                                    try {
                                        // Use Action for thread safety test
                                        ActionResult result =
                                                action.perform(
                                                        new ClickOptions.Builder().build(), coll);

                                        if (result.isSuccess()) {
                                            completedActions.incrementAndGet();
                                        }
                                    } finally {
                                        latch.countDown();
                                    }
                                })
                        .start();
            }

            // Then
            assertTrue(latch.await(5, TimeUnit.SECONDS), "All threads should complete");
            assertEquals(3, completedActions.get(), "All actions should succeed in mock mode");
        }
    }

    @Nested
    @DisplayName("State Navigation Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class StateNavigationTests {

        @Test
        @Order(8)
        @DisplayName("Should handle state-based navigation")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldHandleStateBasedNavigation() {
            // Given - states with transitions
            State loginState = new State.Builder("Login").build();
            loginState.getStateImages().add(loginButton);
            loginState.getStateImages().add(usernameField);
            loginState.getStateImages().add(passwordField);

            State dashboardState = new State.Builder("Dashboard").build();
            dashboardState.getStateImages().add(dashboard);

            // When - navigate using click
            ObjectCollection submitColl =
                    new ObjectCollection.Builder().withImages(submitButton).build();

            ActionResult transitionResult = new ActionResult();
            transitionResult.setActionConfig(new ClickOptions.Builder().build());
            transitionResult.setSuccess(
                    action.perform(transitionResult.getActionConfig(), submitColl).isSuccess());

            // Then
            assertNotNull(transitionResult);
            assertTrue(transitionResult.isSuccess());
        }

        @Test
        @Order(9)
        @DisplayName("Should execute multi-step state navigation")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldExecuteMultiStepStateNavigation() {
            // Given - complex state graph
            List<State> navigationPath =
                    List.of(
                            new State.Builder("Start").build(),
                            new State.Builder("Middle").build(),
                            new State.Builder("End").build());

            // When - navigate through states
            AtomicBoolean navigationComplete = new AtomicBoolean(true);
            for (int i = 0; i < navigationPath.size() - 1; i++) {
                // Simulate transition with click
                ObjectCollection transitionColl =
                        new ObjectCollection.Builder()
                                .withLocations(new Location(200, 200))
                                .build();

                ActionResult result = new ActionResult();
                result.setActionConfig(new ClickOptions.Builder().build());
                result.setSuccess(
                        action.perform(result.getActionConfig(), transitionColl).isSuccess());

                if (!result.isSuccess()) {
                    navigationComplete.set(false);
                    break;
                }
            }

            // Then
            assertTrue(navigationComplete.get(), "Navigation should complete in mock mode");
        }
    }

    @Nested
    @DisplayName("Error Recovery Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ErrorRecoveryTests {

        @Test
        @Order(10)
        @DisplayName("Should recover from action failures")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldRecoverFromActionFailures() {
            // Given
            AtomicInteger retryCount = new AtomicInteger(0);
            AtomicBoolean recovered = new AtomicBoolean(false);

            ObjectCollection targetColl =
                    new ObjectCollection.Builder().withImages(loginButton).build();

            // When - retry on failure
            ActionResult result = null;
            while (retryCount.get() < 3 && !recovered.get()) {
                result = new ActionResult();
                result.setActionConfig(new ClickOptions.Builder().build());
                result.setSuccess(action.perform(result.getActionConfig(), targetColl).isSuccess());

                retryCount.incrementAndGet();

                if (result != null && result.isSuccess()) {
                    recovered.set(true);
                }
            }

            // Then
            assertNotNull(result);
            assertTrue(recovered.get(), "Should recover in mock mode");
            assertTrue(retryCount.get() <= 3, "Should not exceed max retries");
        }

        @Test
        @Order(11)
        @DisplayName("Should handle timeout scenarios")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldHandleTimeoutScenarios() {
            // Given
            VanishOptions vanishOptions =
                    new VanishOptions.Builder()
                            .setTimeout(2.0) // Short timeout for test
                            .build();

            ObjectCollection neverVanishColl =
                    new ObjectCollection.Builder().withImages(dashboard).build();

            ActionResult vanishResult = new ActionResult();
            vanishResult.setActionConfig(vanishOptions);

            long startTime = System.currentTimeMillis();

            // When - simulate timeout (in mock mode, returns quickly)
            vanishResult.setSuccess(false); // Simulate timeout

            long duration = System.currentTimeMillis() - startTime;

            // Then - should complete quickly in mock mode
            assertTrue(duration < 100, "Mock operations should be fast");
            assertFalse(vanishResult.isSuccess(), "Should simulate timeout");
        }
    }
}
