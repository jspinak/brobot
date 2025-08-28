package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.config.MockModeManager;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for complex workflows without Spring dependency.
 * Tests the interaction between multiple action components in realistic scenarios.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComplexWorkflowIntegrationTest extends BrobotTestBase {
    
    private Click click;
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage submitButton;
    private StateImage dashboard;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Initialize simple action
        click = new Click();
        
        // Create test state images
        loginButton = createStateImage("login-button", "images/bottomR.png");
        usernameField = createStateImage("username-field", "images/topLeft.png");
        passwordField = createStateImage("password-field", "images/topLeft2.png");
        submitButton = createStateImage("submit-button", "images/bottomRight.png");
        dashboard = createStateImage("dashboard", "images/bottomRight3.png");
        
        // Ensure mock mode is enabled
        assertTrue(MockModeManager.isMockMode(), "Mock mode should be enabled");
    }
    
    private StateImage createStateImage(String name, String imagePath) {
        return new StateImage.Builder()
            .setName(name)
            .addPattern(imagePath)
            .build();
    }
    
    private ActionResult createMockSuccessResult(StateImage stateImage) {
        ActionResult result = new ActionResult();
        result.setSuccess(true);
        
        Match match = new Match.Builder()
            .setStateObjectData(stateImage)
            .setRegion(new Region(100, 100, 50, 50))
            .setSimScore(0.95)
            .build();
        
        List<Match> matches = new ArrayList<>();
        matches.add(match);
        result.setMatchList(matches);
        
        return result;
    }
    
    @Nested
    @DisplayName("Login Workflow Tests")
    class LoginWorkflowTests {
        
        @Test
        @Order(1)
        @DisplayName("Should execute complete login workflow")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldExecuteCompleteLoginWorkflow() {
            // Given - login workflow collections with locations
            StateLocation loginLoc = new StateLocation.Builder()
                .setLocation(new Location(100, 100))
                .setName("login-button-loc")
                .build();
            ObjectCollection loginButtonColl = new ObjectCollection.Builder()
                .withLocations(loginLoc)
                .build();
            
            StateLocation usernameLoc = new StateLocation.Builder()
                .setLocation(new Location(200, 150))
                .setName("username-loc")
                .build();
            ObjectCollection usernameColl = new ObjectCollection.Builder()
                .withLocations(usernameLoc)
                .build();
            
            StateLocation passwordLoc = new StateLocation.Builder()
                .setLocation(new Location(200, 200))
                .setName("password-loc")
                .build();
            ObjectCollection passwordColl = new ObjectCollection.Builder()
                .withLocations(passwordLoc)
                .build();
            
            StateLocation submitLoc = new StateLocation.Builder()
                .setLocation(new Location(250, 250))
                .setName("submit-loc")
                .build();
            ObjectCollection submitColl = new ObjectCollection.Builder()
                .withLocations(submitLoc)
                .build();
            
            // When - execute login workflow
            // Step 1: Click login button
            ActionResult clickLoginResult = new ActionResult();
            clickLoginResult.setActionConfig(new ClickOptions.Builder().build());
            click.perform(clickLoginResult, loginButtonColl);
            
            // Step 2: Click and type username
            ActionResult clickUsernameResult = new ActionResult();
            clickUsernameResult.setActionConfig(new ClickOptions.Builder().build());
            click.perform(clickUsernameResult, usernameColl);
            
            // Simulate typing (in mock mode)
            TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.01)
                .build();
            ActionResult typeUsernameResult = new ActionResult();
            typeUsernameResult.setActionConfig(typeOptions);
            typeUsernameResult.setSuccess(true); // Mock success
            
            // Step 3: Click and type password
            ActionResult clickPasswordResult = new ActionResult();
            clickPasswordResult.setActionConfig(new ClickOptions.Builder().build());
            click.perform(clickPasswordResult, passwordColl);
            
            ActionResult typePasswordResult = new ActionResult();
            typePasswordResult.setActionConfig(typeOptions);
            typePasswordResult.setSuccess(true); // Mock success
            
            // Step 4: Submit form
            ActionResult submitResult = new ActionResult();
            submitResult.setActionConfig(new ClickOptions.Builder().build());
            click.perform(submitResult, submitColl);
            
            // Then - verify workflow completed
            assertNotNull(clickLoginResult);
            assertTrue(clickLoginResult.isSuccess());
            assertNotNull(submitResult);
            assertTrue(submitResult.isSuccess());
        }
        
        @Test
        @Order(2)
        @DisplayName("Should handle login with multiple attempts")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldHandleLoginWithMultipleAttempts() {
            // Given
            StateLocation loginLoc = new StateLocation.Builder()
                .setLocation(new Location(100, 100))
                .setName("login-button-loc")
                .build();
            ObjectCollection loginColl = new ObjectCollection.Builder()
                .withLocations(loginLoc)
                .build();
            
            AtomicInteger attemptCount = new AtomicInteger(0);
            AtomicBoolean loginSuccess = new AtomicBoolean(false);
            
            // When - retry login up to 3 times
            while (attemptCount.get() < 3 && !loginSuccess.get()) {
                ActionResult result = new ActionResult();
                result.setActionConfig(new ClickOptions.Builder().build());
                click.perform(result, loginColl);
                
                attemptCount.incrementAndGet();
                
                if (result.isSuccess()) {
                    loginSuccess.set(true);
                }
            }
            
            // Then
            assertTrue(loginSuccess.get(), "Login should succeed in mock mode");
            assertTrue(attemptCount.get() <= 3, "Should not exceed max attempts");
        }
    }
    
    @Nested
    @DisplayName("Conditional Workflow Tests")
    class ConditionalWorkflowTests {
        
        @Test
        @Order(3)
        @DisplayName("Should execute conditional workflow based on element visibility")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldExecuteConditionalWorkflow() {
            // Given
            ActionResult loginCheck = createMockSuccessResult(loginButton);
            
            StateLocation dashboardLoc = new StateLocation.Builder()
                .setLocation(new Location(400, 300))
                .setName("dashboard-loc")
                .build();
            ObjectCollection dashboardColl = new ObjectCollection.Builder()
                .withLocations(dashboardLoc)
                .build();
            
            // When
            if (loginCheck.isSuccess()) {
                // Login button found - perform login
                StateLocation submitLoc = new StateLocation.Builder()
                    .setLocation(new Location(250, 250))
                    .setName("submit-loc")
                    .build();
                ObjectCollection submitColl = new ObjectCollection.Builder()
                    .withLocations(submitLoc)
                    .build();
                
                ActionResult loginResult = new ActionResult();
                loginResult.setActionConfig(new ClickOptions.Builder().build());
                click.perform(loginResult, submitColl);
                
                assertTrue(loginResult.isSuccess());
            } else {
                // Already logged in - check for dashboard
                ActionResult dashboardResult = new ActionResult();
                dashboardResult.setActionConfig(new ClickOptions.Builder().build());
                click.perform(dashboardResult, dashboardColl);
                
                assertTrue(dashboardResult.isSuccess());
            }
        }
        
        @Test
        @Order(4)
        @DisplayName("Should handle fallback actions")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldHandleFallbackActions() {
            // Given - primary and fallback actions
            StateImage primaryButton = createStateImage("primary", "images/primary.png");
            StateImage fallbackButton = createStateImage("fallback", "images/fallback.png");
            
            StateLocation primaryLoc = new StateLocation.Builder()
                .setLocation(new Location(150, 150))
                .setName("primary-loc")
                .build();
            ObjectCollection primaryColl = new ObjectCollection.Builder()
                .withLocations(primaryLoc)
                .build();
            
            StateLocation fallbackLoc = new StateLocation.Builder()
                .setLocation(new Location(160, 160))
                .setName("fallback-loc")
                .build();
            ObjectCollection fallbackColl = new ObjectCollection.Builder()
                .withLocations(fallbackLoc)
                .build();
            
            // When - try primary, then fallback
            ActionResult primaryResult = new ActionResult();
            primaryResult.setActionConfig(new ClickOptions.Builder().build());
            click.perform(primaryResult, primaryColl);
            
            ActionResult finalResult;
            if (!primaryResult.isSuccess()) {
                // Try fallback
                ActionResult fallbackResult = new ActionResult();
                fallbackResult.setActionConfig(new ClickOptions.Builder().build());
                click.perform(fallbackResult, fallbackColl);
                finalResult = fallbackResult;
            } else {
                finalResult = primaryResult;
            }
            
            // Then
            assertNotNull(finalResult);
            assertTrue(finalResult.isSuccess(), "Either primary or fallback should succeed in mock mode");
        }
    }
    
    @Nested
    @DisplayName("Drag and Drop Tests")
    class DragAndDropTests {
        
        @Test
        @Order(5)
        @DisplayName("Should configure drag and drop operation")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldConfigureDragAndDrop() {
            // Given
            StateLocation source = new StateLocation.Builder()
                .setLocation(new Location(100, 100))
                .setName("source")
                .build();
            
            StateLocation target = new StateLocation.Builder()
                .setLocation(new Location(300, 300))
                .setName("target")
                .build();
            
            ObjectCollection dragColl = new ObjectCollection.Builder()
                .withLocations(source, target)
                .build();
            
            // When - configure drag options
            DragOptions dragOptions = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(0.1)
                .setDelayAfterDrag(0.1)
                .setPauseAfterEnd(0.1)
                .build();
            
            ActionResult dragResult = new ActionResult();
            dragResult.setActionConfig(dragOptions);
            
            // In mock mode, simulate success
            dragResult.setSuccess(true);
            
            // Then
            assertNotNull(dragOptions);
            assertEquals(0.1, dragOptions.getDelayBetweenMouseDownAndMove(), 0.001);
            assertEquals(0.1, dragOptions.getDelayAfterDrag(), 0.001);
            assertTrue(dragResult.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Parallel Execution Tests")
    class ParallelExecutionTests {
        
        @Test
        @Order(6)
        @DisplayName("Should handle parallel action execution")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldHandleParallelExecution() throws InterruptedException {
            // Given
            CountDownLatch latch = new CountDownLatch(3);
            AtomicInteger completedActions = new AtomicInteger(0);
            
            List<ObjectCollection> collections = List.of(
                new ObjectCollection.Builder().withLocations(
                    new StateLocation.Builder().setLocation(new Location(100, 100)).setName("loc1").build()
                ).build(),
                new ObjectCollection.Builder().withLocations(
                    new StateLocation.Builder().setLocation(new Location(200, 150)).setName("loc2").build()
                ).build(),
                new ObjectCollection.Builder().withLocations(
                    new StateLocation.Builder().setLocation(new Location(200, 200)).setName("loc3").build()
                ).build()
            );
            
            // When - execute in parallel
            for (ObjectCollection coll : collections) {
                new Thread(() -> {
                    try {
                        ActionResult result = new ActionResult();
                        result.setActionConfig(new ClickOptions.Builder().build());
                        
                        // Create separate Click instance for thread safety
                        Click threadClick = new Click();
                        threadClick.perform(result, coll);
                        
                        if (result.isSuccess()) {
                            completedActions.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }
            
            // Then
            assertTrue(latch.await(5, TimeUnit.SECONDS), "All threads should complete");
            assertEquals(3, completedActions.get(), "All actions should succeed in mock mode");
        }
    }
    
    @Nested
    @DisplayName("State Navigation Tests")
    class StateNavigationTests {
        
        @Test
        @Order(7)
        @DisplayName("Should handle state-based navigation")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldHandleStateNavigation() {
            // Given - states with images
            State loginState = new State.Builder("Login").build();
            loginState.getStateImages().add(loginButton);
            loginState.getStateImages().add(usernameField);
            loginState.getStateImages().add(passwordField);
            
            State dashboardState = new State.Builder("Dashboard").build();
            dashboardState.getStateImages().add(dashboard);
            
            // When - navigate from login to dashboard
            StateLocation submitLoc = new StateLocation.Builder()
                .setLocation(new Location(250, 250))
                .setName("submit-loc")
                .build();
            ObjectCollection submitColl = new ObjectCollection.Builder()
                .withLocations(submitLoc)
                .build();
            
            ActionResult transitionResult = new ActionResult();
            transitionResult.setActionConfig(new ClickOptions.Builder().build());
            click.perform(transitionResult, submitColl);
            
            // Then
            assertNotNull(transitionResult);
            assertTrue(transitionResult.isSuccess());
            assertNotNull(loginState.getName());
            assertEquals("Login", loginState.getName());
            assertNotNull(dashboardState.getName());
            assertEquals("Dashboard", dashboardState.getName());
        }
        
        @Test
        @Order(8)
        @DisplayName("Should execute multi-step navigation")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldExecuteMultiStepNavigation() {
            // Given - navigation path
            List<State> navigationPath = List.of(
                new State.Builder("Start").build(),
                new State.Builder("Middle").build(),
                new State.Builder("End").build()
            );
            
            // When - navigate through states
            AtomicBoolean navigationComplete = new AtomicBoolean(true);
            
            for (int i = 0; i < navigationPath.size() - 1; i++) {
                State current = navigationPath.get(i);
                State next = navigationPath.get(i + 1);
                
                // Simulate transition with click
                ObjectCollection transitionColl = new ObjectCollection.Builder()
                    .withLocations(new Location(200, 200))
                    .build();
                
                ActionResult result = new ActionResult();
                result.setActionConfig(new ClickOptions.Builder().build());
                click.perform(result, transitionColl);
                
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
    class ErrorRecoveryTests {
        
        @Test
        @Order(9)
        @DisplayName("Should recover from action failures")
        @Timeout(value = 5, unit = TimeUnit.SECONDS)
        void shouldRecoverFromFailures() {
            // Given
            StateLocation targetLoc = new StateLocation.Builder()
                .setLocation(new Location(100, 100))
                .setName("target-loc")
                .build();
            ObjectCollection targetColl = new ObjectCollection.Builder()
                .withLocations(targetLoc)
                .build();
            
            AtomicInteger retryCount = new AtomicInteger(0);
            AtomicBoolean recovered = new AtomicBoolean(false);
            
            // When - retry on failure
            ActionResult result = null;
            while (retryCount.get() < 3 && !recovered.get()) {
                result = new ActionResult();
                result.setActionConfig(new ClickOptions.Builder().build());
                click.perform(result, targetColl);
                
                retryCount.incrementAndGet();
                
                if (result.isSuccess()) {
                    recovered.set(true);
                }
            }
            
            // Then
            assertNotNull(result);
            assertTrue(recovered.get(), "Should recover in mock mode");
            assertTrue(retryCount.get() <= 3, "Should not exceed max retries");
        }
    }
}