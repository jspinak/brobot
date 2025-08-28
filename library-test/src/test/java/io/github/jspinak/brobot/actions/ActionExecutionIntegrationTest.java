package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.config.MockModeManager;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for action execution system.
 * Tests the execution of various action types in mock mode.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ActionExecutionIntegrationTest extends BrobotTestBase {

    private Click click;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Initialize only the simple action that doesn't require dependencies
        click = new Click();
        
        // Ensure mock mode is enabled for all tests
        assertTrue(MockModeManager.isMockMode(), "Mock mode should be enabled");
    }
    
    @Test
    @Order(1)
    @DisplayName("Should execute click action successfully")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testClickAction() {
        // Arrange
        ObjectCollection collection = new ObjectCollection.Builder()
            .withLocations(new Location(100, 100), new Location(200, 200))
            .withRegions(new Region(300, 300, 100, 100))
            .build();
            
        ClickOptions options = new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .setPauseAfterEnd(0.1)
            .build();
            
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        // Act
        click.perform(result, collection);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Click action should succeed in mock mode");
        assertFalse(result.getMatchList().isEmpty(), "Should have matches for clicked locations");
        assertEquals(3, result.getMatchList().size(), "Should have 3 matches (2 locations + 1 region)");
    }
    
    @Test
    @Order(2)
    @DisplayName("Should handle find options configuration")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFindConfiguration() {
        // This test focuses on configuration rather than execution
        // since Find requires complex dependencies
        
        // Arrange
        StateImage stateImage = new StateImage.Builder()
            .setName("TestImage")
            .addPattern("test-pattern.png")
            .setSearchRegionForAllPatterns(new Region(0, 0, 800, 600))
            .build();
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withImages(stateImage)
            .build();
            
        PatternFindOptions options = PatternFindOptions.forQuickSearch();
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        // Assert - verify configuration is properly set up
        assertNotNull(options);
        assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
        assertTrue(options.getSimilarity() > 0);
        assertNotNull(result.getActionConfig());
    }
    
    @Test
    @Order(3)
    @DisplayName("Should configure type options")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testTypeConfiguration() {
        // Test configuration without execution
        
        // Arrange
        StateString stateString = new StateString.Builder()
            .setString("Hello, World!")
            .setName("TestString")
            .build();
            
        StateRegion targetRegion = new StateRegion.Builder()
            .setSearchRegion(new Region(100, 100, 400, 50))
            .setName("InputField")
            .build();
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withStrings(stateString)
            .withRegions(targetRegion)
            .build();
            
        TypeOptions options = new TypeOptions.Builder()
            .setTypeDelay(0.01)
            .setPauseAfterEnd(0.1)
            .build();
            
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        // Create a Text object instead of using String
        Text textObj = new Text();
        textObj.add("Hello, World!");
        result.setText(textObj);
        
        // Assert
        assertNotNull(options);
        assertEquals(0.01, options.getTypeDelay(), 0.001);
        assertEquals(0.1, options.getPauseAfterEnd(), 0.001);
        assertNotNull(result.getText());
        assertEquals("Hello, World!", result.getText().get(0));
    }
    
    @Test
    @Order(4)
    @DisplayName("Should configure drag options")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDragConfiguration() {
        // Test drag configuration without execution
        
        // Arrange
        StateLocation fromLocation = new StateLocation.Builder()
            .setLocation(new Location(100, 100))
            .setName("DragStart")
            .build();
            
        StateLocation toLocation = new StateLocation.Builder()
            .setLocation(new Location(300, 300))
            .setName("DragEnd")
            .build();
            
        ObjectCollection collection = new ObjectCollection.Builder()
            .withLocations(fromLocation, toLocation)
            .build();
            
        DragOptions options = new DragOptions.Builder()
            .setDelayAfterDrag(0.1)
            .setPauseAfterEnd(0.1)
            .build();
            
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        // Assert configuration
        assertNotNull(options);
        assertEquals(0.1, options.getDelayAfterDrag(), 0.001);
        assertEquals(0.1, options.getPauseAfterEnd(), 0.001);
        assertNotNull(collection.getStateLocations());
        assertEquals(2, collection.getStateLocations().size());
    }
    
    @Test
    @Order(5)
    @DisplayName("Should handle empty object collection")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testEmptyCollection() {
        // Arrange
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        
        ClickOptions options = new ClickOptions.Builder().build();
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        // Act
        click.perform(result, emptyCollection);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess(), "Should not succeed with empty collection");
        assertTrue(result.getMatchList().isEmpty(), "Should have no matches");
    }
    
    @Test
    @Order(6)
    @DisplayName("Should handle multiple object collections")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testMultipleCollections() {
        // Arrange
        ObjectCollection collection1 = new ObjectCollection.Builder()
            .withLocations(new Location(100, 100))
            .build();
            
        ObjectCollection collection2 = new ObjectCollection.Builder()
            .withLocations(new Location(200, 200))
            .build();
            
        ObjectCollection collection3 = new ObjectCollection.Builder()
            .withRegions(new Region(300, 300, 50, 50))
            .build();
            
        ClickOptions options = new ClickOptions.Builder().build();
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        
        // Act
        click.perform(result, collection1, collection2, collection3);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess(), "Should succeed with multiple collections");
        assertEquals(3, result.getMatchList().size(), "Should have matches from all collections");
    }
    
    @Test
    @Order(7)
    @DisplayName("Should preserve action metadata")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testActionMetadata() {
        // Arrange
        ObjectCollection collection = new ObjectCollection.Builder()
            .withLocations(new Location(100, 100))
            .build();
            
        ClickOptions options = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .build();
            
        ActionResult result = new ActionResult();
        result.setActionConfig(options);
        result.setActionDescription("Test Click");
        
        LocalDateTime startTime = LocalDateTime.now();
        
        // Act
        click.perform(result, collection);
        
        // Assert
        assertNotNull(result);
        assertEquals(options, result.getActionConfig(), "Config should be preserved");
        assertEquals("Test Click", result.getActionDescription(), "Description should be preserved");
        assertNotNull(result.getDuration(), "Duration should be set");
    }
}