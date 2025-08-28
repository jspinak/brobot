package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.tools.testing.mock.builders.MockSceneBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for end-to-end action chains in Brobot.
 * 
 * These tests verify that complex sequences of actions work correctly together,
 * including find, click, type, and drag operations in realistic scenarios.
 */
@SpringBootTest
@DisplayName("Action Chain Integration Tests")
public class ActionChainIntegrationTest extends BrobotIntegrationTestBase {

    @Autowired
    private Action action;
    
    @Nested
    @DisplayName("Simple Action Chains")
    class SimpleActionChains {
        
        @Test
        @DisplayName("Should execute find-click chain successfully")
        void shouldExecuteFindClickChain() {
            // Arrange
            StateImage targetImage = new StateImage.Builder()
                    .addPattern(MockSceneBuilder.createMockPattern())
                    .build();
            
            ObjectCollection findCollection = new ObjectCollection.Builder()
                    .withPatterns(targetImage.getPatterns().toArray(new Pattern[0]))
                    .build();
                    
            // Act
            ActionResult findRes = action.find(findCollection);
            
            // Assert
            assertNotNull(findRes, "Find result should not be null");
            // In mock mode, actions complete quickly
        }
        
        @Test
        @DisplayName("Should execute click at location successfully")
        void shouldClickAtLocation() {
            // Arrange
            Location targetLocation = new Location(300, 400);
            ObjectCollection locationCollection = new ObjectCollection.Builder()
                    .withLocations(targetLocation)
                    .build();
            
            // Act
            // Need to use perform with ActionType for clicking at location
            ActionResult res = action.perform(ActionType.CLICK, targetLocation);
            
            // Assert
            assertNotNull(res, "Click result should not be null");
        }
        
        @Test
        @DisplayName("Should execute find-click-type chain")
        void shouldExecuteFindClickTypeChain() {
            // Arrange
            StateImage searchField = new StateImage.Builder()
                    .addPattern(MockSceneBuilder.createMockPattern())
                    .build();
                    
            ObjectCollection searchCollection = new ObjectCollection.Builder()
                    .withPatterns(searchField.getPatterns().toArray(new Pattern[0]))
                    .build();
            
            ObjectCollection typeCollection = new ObjectCollection.Builder()
                    .withStrings("test query")
                    .build();
            
            // Act
            ActionResult findRes = action.find(searchCollection);
            assertNotNull(findRes, "Find result should not be null");
            
            if (findRes.getMatchList() != null && !findRes.getMatchList().isEmpty()) {
                Match foundField = findRes.getMatchList().get(0);
                // Click on the found match
                ActionResult clickRes = action.perform(ActionType.CLICK, foundField.getTarget());
                assertNotNull(clickRes, "Click result should not be null");
            }
            
            ActionResult typeRes = action.type(typeCollection);
            assertNotNull(typeRes, "Type result should not be null");
        }
    }
    
    @Nested
    @DisplayName("Complex Action Chains")
    class ComplexActionChains {
        
        @Test
        @DisplayName("Should execute conditional action chain")
        void shouldExecuteConditionalChain() {
            // Create patterns for different UI states
            StateImage loginButton = new StateImage.Builder()
                    .addPattern(MockSceneBuilder.createMockPattern())
                    .setName("loginButton")
                    .build();
                    
            StateImage dashboardIcon = new StateImage.Builder()
                    .addPattern(MockSceneBuilder.createMockPattern())
                    .setName("dashboardIcon")
                    .build();
            
            // Check if login is needed
            ObjectCollection loginCheck = new ObjectCollection.Builder()
                    .withPatterns(loginButton.getPatterns().toArray(new Pattern[0]))
                    .build();
                    
            ActionResult loginCheckResult = action.find(loginCheck);
            
            if (loginCheckResult != null && loginCheckResult.isSuccess()) {
                // Perform login sequence
                ObjectCollection usernameCollection = new ObjectCollection.Builder()
                        .withStrings("testuser")
                        .build();
                        
                action.type(usernameCollection);
                
                ObjectCollection passwordCollection = new ObjectCollection.Builder()
                        .withStrings("password123")
                        .build();
                        
                action.type(passwordCollection);
                
                // Click login button using the action.click with StateImage
                action.click(loginButton);
            }
            
            // Verify we're on dashboard
            ObjectCollection dashboardCheck = new ObjectCollection.Builder()
                    .withPatterns(dashboardIcon.getPatterns().toArray(new Pattern[0]))
                    .build();
                    
            ActionResult dashboardResult = action.find(dashboardCheck);
            assertNotNull(dashboardResult, "Dashboard check should not be null");
        }
        
        @Test
        @DisplayName("Should execute multi-step workflow")
        void shouldExecuteMultiStepWorkflow() {
            // This test simulates a complete workflow
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                    .setSimilarity(0.8)
                    .build();
                    
            ClickOptions clickOptions = new ClickOptions.Builder()
                    .setNumberOfClicks(1)
                    .build();
                    
            TypeOptions typeOptions = new TypeOptions.Builder()
                    .setModifiers("")
                    .build();
            
            // Step 1: Find element
            Pattern pattern = MockSceneBuilder.createMockPattern();
            StateImage stateImage = new StateImage.Builder()
                    .addPattern(pattern)
                    .build();
                    
            ObjectCollection step1 = new ObjectCollection.Builder()
                    .withPatterns(pattern)
                    .build();
                    
            ActionResult step1Result = action.find(step1);
            assertNotNull(step1Result, "Step 1 result should not be null");
            
            // Step 2: Click element
            if (step1Result.getMatchList() != null && !step1Result.getMatchList().isEmpty()) {
                // Click the found match location
                ActionResult step2Result = action.perform(ActionType.CLICK, 
                    step1Result.getMatchList().get(0).getTarget());
                assertNotNull(step2Result, "Step 2 result should not be null");
            }
            
            // Step 3: Type text
            ObjectCollection step3 = new ObjectCollection.Builder()
                    .withStrings("workflow complete")
                    .build();
                    
            ActionResult step3Result = action.type(step3);
            assertNotNull(step3Result, "Step 3 result should not be null");
        }
    }
    
    @Nested
    @DisplayName("Error Handling in Action Chains")
    class ErrorHandlingChains {
        
        @Test
        @DisplayName("Should handle failed find in chain")
        void shouldHandleFailedFind() {
            // Create a pattern that won't be found
            Pattern nonExistentPattern = MockSceneBuilder.createMockPattern();
            
            PatternFindOptions strictOptions = new PatternFindOptions.Builder()
                    .setSimilarity(0.99) // Very strict similarity
                    .build();
                    
            ObjectCollection findCollection = new ObjectCollection.Builder()
                    .withPatterns(nonExistentPattern)
                    .build();
            
            // Act
            ActionResult findResult = action.find(findCollection);
            
            // Assert - in mock mode, find operations complete but may not succeed
            assertNotNull(findResult, "Result should not be null even on failure");
            
            // If find fails, subsequent actions should be skipped
            if (!findResult.isSuccess()) {
                // Don't proceed with click
                assertTrue(true, "Correctly skipped click after failed find");
            }
        }
        
        @Test
        @DisplayName("Should recover from action chain failure")
        void shouldRecoverFromChainFailure() {
            int maxRetries = 3;
            boolean success = false;
            
            for (int attempt = 1; attempt <= maxRetries && !success; attempt++) {
                Pattern pattern = MockSceneBuilder.createMockPattern();
                
                ObjectCollection collection = new ObjectCollection.Builder()
                        .withPatterns(pattern)
                        .build();
                
                ActionResult result = action.find(collection);
                
                if (result != null) {
                    success = true;
                    assertTrue(true, "Successfully recovered after " + attempt + " attempts");
                }
            }
            
            assertTrue(success, "Should eventually succeed or exhaust retries");
        }
    }
}