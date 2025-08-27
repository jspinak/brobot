package io.github.jspinak.brobot.action.integration;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeText;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.wait.WaitVanish;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.composite.drag.Drag;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for complex workflows using real Spring context.
 * Tests the interaction between multiple action components in realistic scenarios.
 * 
 * Updated to use pure actions that don't have circular dependencies.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.logging.verbosity=VERBOSE",
    "brobot.console.actions.enabled=true",
    "brobot.execution.timeout=10",
    "brobot.mock.enabled=true",  // Use mock mode for testing
    "spring.main.allow-bean-definition-overriding=true"
})
class ComplexWorkflowIntegrationTest extends BrobotIntegrationTestBase {
    
    @Autowired
    private Action action;
    
    @Autowired
    private Find find;
    
    @Autowired
    private Click click;  // Now using pure Click without Find dependency
    
    @Autowired(required = false)
    private TypeText typeText;
    
    @Autowired
    private WaitVanish waitVanish;  // Now using pure WaitVanish
    
    @Autowired(required = false)
    private Drag drag;
    
    @Autowired
    private ActionChainExecutor chainExecutor;
    
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage submitButton;
    private StateImage loadingSpinner;
    private StateImage dashboard;
    
    @BeforeEach
    void setupTestData() {
        // Create test state images
        loginButton = createStateImage("login-button", "images/bottomR.png");
        usernameField = createStateImage("username-field", "images/topLeft.png");
        passwordField = createStateImage("password-field", "images/topLeft2.png");
        submitButton = createStateImage("submit-button", "images/bottomRight.png");
        loadingSpinner = createStateImage("loading-spinner", "images/bottomRight2.png");
        dashboard = createStateImage("dashboard", "images/bottomRight3.png");
    }
    
    private StateImage createStateImage(String name, String imagePath) {
        StateImage stateImage = new StateImage.Builder()
            .setName(name)
            .addPattern(imagePath)
            .build();
        return stateImage;
    }
    
    @Nested
    @DisplayName("Login Workflow Tests")
    class LoginWorkflowTests {
        
        @Test
        @DisplayName("Should complete login workflow using pure actions")
        void testLoginWorkflowWithPureActions() {
            // Step 1: Find login button
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
            
            ActionResult findResult = action.perform(findOptions, loginButton.asObjectCollection());
            assertTrue(findResult.isSuccess(), "Should find login button");
            
            // Step 2: Click on login button (pure Click - no Find)
            if (!findResult.getMatchList().isEmpty()) {
                Match loginMatch = findResult.getMatchList().get(0);
                ObjectCollection clickCollection = new ObjectCollection.Builder()
                    .withRegions(loginMatch.getRegion())
                    .build();
                    
                ClickOptions clickOptions = new ClickOptions.Builder()
                    .setNumberOfClicks(1)
                    .build();
                
                ActionResult clickResult = action.perform(clickOptions, clickCollection);
                assertTrue(clickResult.isSuccess(), "Should click login button");
            }
        }
        
        @Test
        @DisplayName("Should use ConditionalActionChain for find-then-click")
        void testConditionalChainWorkflow() {
            // Modern approach using ConditionalActionChain
            ConditionalActionChain.find(new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.FIRST)
                    .build())
                .ifFoundClick()
                .ifNotFoundLog("Login button not found")
                .perform(action, loginButton.asObjectCollection());
        }
        
        @Test
        @DisplayName("Should use ActionChainOptions for complex workflow")
        void testActionChainWorkflow() {
            // Create a chain for login workflow
            ActionChainOptions loginChain = new ActionChainOptions.Builder(
                // Find username field
                new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.FIRST)
                    .build())
                // Click on it
                .then(new ClickOptions.Builder()
                    .setNumberOfClicks(1)
                    .build())
                // Type username
                .then(new TypeOptions.Builder()
                    .build())
                .build();
            
            // Execute the chain with text in ObjectCollection for Type
            ActionResult result = chainExecutor.executeChain(
                loginChain, 
                new ActionResult(),
                usernameField.asObjectCollection(),
                usernameField.asObjectCollection(), // For click
                new ObjectCollection.Builder().withStrings("testuser").build() // For type with text
            );
            
            assertTrue(result.isSuccess(), "Login chain should succeed");
        }
    }
    
    @Nested
    @DisplayName("Wait and Vanish Tests")
    class WaitVanishTests {
        
        @Test
        @DisplayName("Should wait for element to vanish using pure WaitVanish")
        void testPureWaitVanish() {
            // First find the loading spinner
            ActionResult findResult = action.find(loadingSpinner);
            
            if (findResult.isSuccess() && !findResult.getMatchList().isEmpty()) {
                // Use pure WaitVanish with the found matches
                VanishOptions vanishOptions = new VanishOptions.Builder()
                    .setTimeout(5.0)
                    .build();
                
                // WaitVanish now works with matches in ActionResult
                ActionResult vanishResult = new ActionResult();
                vanishResult.setActionConfig(vanishOptions);
                vanishResult.getMatchList().addAll(findResult.getMatchList());
                
                waitVanish.perform(vanishResult);
                
                // In mock mode, this should succeed
                assertTrue(vanishResult.isSuccess(), "Element should vanish");
            }
        }
    }
    
    @Nested
    @DisplayName("Drag and Drop Tests")
    class DragDropTests {
        
        @Test
        @DisplayName("Should perform drag operation with pure actions")
        void testDragWithPureActions() {
            // Find source element
            ActionResult sourceResult = action.find(loginButton);
            // Find target element
            ActionResult targetResult = action.find(dashboard);
            
            if (sourceResult.isSuccess() && targetResult.isSuccess()) {
                // Create drag operation with found locations
                Match sourceMatch = sourceResult.getMatchList().get(0);
                Match targetMatch = targetResult.getMatchList().get(0);
                
                ObjectCollection dragCollection = new ObjectCollection.Builder()
                    .withLocations(sourceMatch.getTarget(), targetMatch.getTarget())
                    .build();
                DragOptions dragOptions = new DragOptions.Builder()
                    .build();
                
                ActionResult dragResult = action.perform(dragOptions, dragCollection);
                assertTrue(dragResult.isSuccess(), "Drag should succeed");
            }
        }
    }
    
    @Test
    @DisplayName("Should handle concurrent actions without deadlock")
    void testConcurrentActionsNoCIrcularDependency() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicBoolean hasDeadlock = new AtomicBoolean(false);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Run multiple actions concurrently
        Thread findThread = new Thread(() -> {
            try {
                ActionResult result = action.find(loginButton);
                if (result.isSuccess()) successCount.incrementAndGet();
            } catch (Exception e) {
                hasDeadlock.set(true);
            } finally {
                latch.countDown();
            }
        });
        
        Thread clickThread = new Thread(() -> {
            try {
                // Pure Click doesn't need Find
                ObjectCollection collection = new ObjectCollection.Builder()
                    .withLocations(new Location(100, 100))
                    .build();
                ClickOptions clickOptions = new ClickOptions.Builder().build();
                ActionResult result = action.perform(clickOptions, collection);
                if (result.isSuccess()) successCount.incrementAndGet();
            } catch (Exception e) {
                hasDeadlock.set(true);
            } finally {
                latch.countDown();
            }
        });
        
        Thread vanishThread = new Thread(() -> {
            try {
                // Pure WaitVanish works with provided regions
                ObjectCollection collection = new ObjectCollection.Builder()
                    .withRegions(new Region(50, 50, 100, 100))
                    .build();
                    
                VanishOptions options = new VanishOptions.Builder()
                    .build();
                    
                ActionResult result = action.perform(options, collection);
                if (result.isSuccess()) successCount.incrementAndGet();
            } catch (Exception e) {
                hasDeadlock.set(true);
            } finally {
                latch.countDown();
            }
        });
        
        // Start all threads
        findThread.start();
        clickThread.start();
        vanishThread.start();
        
        // Wait for completion with timeout
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Actions should complete within 5 seconds");
        assertFalse(hasDeadlock.get(), "No deadlock should occur with pure actions");
        assertTrue(successCount.get() > 0, "At least some actions should succeed");
    }
}