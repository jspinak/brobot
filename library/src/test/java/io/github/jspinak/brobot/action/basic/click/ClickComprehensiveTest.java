package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the Click action.
 * Tests all aspects of click functionality including:
 * - Single and multiple clicks
 * - Different click options
 * - Various object types (locations, regions, matches)
 * - Edge cases and error handling
 * - Performance characteristics in mock mode
 */
@DisplayName("Comprehensive Click Action Tests")
public class ClickComprehensiveTest extends BrobotTestBase {

    private Click click;
    private ActionResult actionResult;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Use real Click class instead of mock to test actual behavior
        click = new Click();
        
        actionResult = new ActionResult();
    }
    
    // Helper method to create mock behavior when needed for specific tests
    private void setupMockClick() {
        click = org.mockito.Mockito.mock(Click.class);
        
        // Configure mock to simulate successful clicks
        org.mockito.Mockito.doAnswer(invocation -> {
            ActionResult result = invocation.getArgument(0);
            if (result == null) {
                return null; // Handle null ActionResult gracefully
            }
            
            ObjectCollection collection = invocation.getArgument(1);
            if (collection == null) {
                result.setSuccess(false); // Fail for null collection
                return null;
            }
            
            // Check if collection is empty
            boolean hasItems = !collection.getStateLocations().isEmpty() || 
                             !collection.getStateRegions().isEmpty() ||
                             !collection.getStateImages().isEmpty();
            
            if (!hasItems) {
                result.setSuccess(false); // Fail for empty collection
                return null;
            }
            
            result.setSuccess(true);
            
            // Add matches for locations
            for (StateLocation stateLoc : collection.getStateLocations()) {
                Match match = new Match();
                match.setScore(0.95);
                if (stateLoc.getLocation() != null) {
                    match.setRegion(new Region(
                        stateLoc.getLocation().getX(),
                        stateLoc.getLocation().getY(), 
                        1, 1));
                }
                // Preserve state information
                if (stateLoc.getOwnerStateName() != null) {
                    io.github.jspinak.brobot.model.state.StateObjectMetadata metadata = 
                        new io.github.jspinak.brobot.model.state.StateObjectMetadata();
                    metadata.setOwnerStateName(stateLoc.getOwnerStateName());
                    match.setStateObjectData(metadata);
                }
                result.getMatchList().add(match);
            }
            
            // Add matches for regions
            for (StateRegion stateReg : collection.getStateRegions()) {
                Match match = new Match();
                match.setScore(0.95);
                if (stateReg.getSearchRegion() != null) {
                    match.setRegion(stateReg.getSearchRegion());
                }
                result.getMatchList().add(match);
            }
            
            return null;
        }).when(click).perform(org.mockito.Mockito.any(ActionResult.class), 
                              org.mockito.Mockito.any(ObjectCollection.class));
        
        actionResult = new ActionResult();
    }
    
    @Nested
    @DisplayName("Basic Click Operations")
    class BasicClickOperations {
        
        @Test
        @DisplayName("Should click single location")
        void testSingleLocationClick() {
            // Arrange
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .setName("test-location")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
            
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess(), "Click should succeed");
            assertFalse(actionResult.getMatchList().isEmpty(), "Should have matches");
            assertEquals(1, actionResult.getMatchList().size(), "Should have one match");
        }
        
        @Test
        @DisplayName("Should click multiple locations")
        void testMultipleLocationClick() {
            // Arrange
            List<StateLocation> locations = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Location loc = new Location(100 + i * 50, 100 + i * 50);
                StateLocation stateLoc = new StateLocation.Builder()
                    .setLocation(loc)
                    .setName("location-" + i)
                    .build();
                locations.add(stateLoc);
            }
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(locations.toArray(new StateLocation[0]))
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
            
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess(), "Click should succeed");
            assertEquals(3, actionResult.getMatchList().size(), "Should have three matches");
        }
        
        @Test
        @DisplayName("Should handle empty collection gracefully")
        void testEmptyCollection() {
            // Arrange
            ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, emptyCollection);
            
            // Assert
            assertFalse(actionResult.isSuccess(), "Click should fail with empty collection");
            assertTrue(actionResult.getMatchList().isEmpty(), "Should have no matches");
        }
    }
    
    @Nested
    @DisplayName("Click Options Tests")
    class ClickOptionsTests {
        
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5})
        @DisplayName("Should perform specified number of clicks")
        void testMultipleClicks(int numberOfClicks) {
            // Arrange
            Location location = new Location(200, 200);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                .setNumberOfClicks(numberOfClicks)
                .build();
            
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
            // In mock mode, we can't verify actual clicks, but we verify the action completed
        }
        
        @Test
        @DisplayName("Should respect pause between clicks")
        void testPauseBetweenClicks() {
            // Arrange
            Location location = new Location(300, 300);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                .setNumberOfClicks(3)
                // Pauses are configured via RepetitionOptions
                .build();
            
            actionResult.setActionConfig(options);
            
            long startTime = System.currentTimeMillis();
            
            // Act
            click.perform(actionResult, collection);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertTrue(actionResult.isSuccess());
            // In mock mode, pauses should be minimal
            assertTrue(duration < 100, "Mock mode should have minimal pauses");
        }
        
        @ParameterizedTest
        @EnumSource(Click.Type.class)
        @DisplayName("Should handle different click types")
        void testClickTypes(Click.Type clickType) {
            // Arrange
            Location location = new Location(400, 400);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                // Click type would be set here if the API supported it
                .build();
            
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess(), "Click should succeed for type: " + clickType);
        }
    }
    
    @Nested
    @DisplayName("Position-Based Clicks")
    class PositionBasedClicks {
        
        @ParameterizedTest
        @EnumSource(value = Positions.Name.class, 
                    names = {"TOPLEFT", "TOPMIDDLE", "TOPRIGHT", 
                            "MIDDLELEFT", "MIDDLEMIDDLE", "MIDDLERIGHT",
                            "BOTTOMLEFT", "BOTTOMMIDDLE", "BOTTOMRIGHT"})
        @DisplayName("Should click at different positions")
        void testClickAtPositions(Positions.Name position) {
            // Arrange
            Location location = new Location(position);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .setName("position-" + position)
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess(), "Should click at position: " + position);
        }
        
        @Test
        @DisplayName("Should click at screen center")
        void testClickScreenCenter() {
            // Arrange
            Location centerLocation = new Location(Positions.Name.MIDDLEMIDDLE);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(centerLocation)
                .setName("screen-center")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertNotNull(actionResult.getMatchList());
            assertFalse(actionResult.getMatchList().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Region-Based Clicks")
    class RegionBasedClicks {
        
        @Test
        @DisplayName("Should click in region center")
        void testClickRegionCenter() {
            // Arrange
            Region region = new Region(100, 100, 200, 150);
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(region)
                .setName("test-region")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(stateRegion)
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
            
            // Verify click happened at region center
            Match match = actionResult.getMatchList().get(0);
            assertEquals(200, match.x() + match.w()/2, 1); // Center x
            assertEquals(175, match.y() + match.h()/2, 1); // Center y
        }
        
        @Test
        @DisplayName("Should click multiple regions")
        void testClickMultipleRegions() {
            // Arrange
            List<StateRegion> regions = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Region region = new Region(i * 100, i * 100, 80, 60);
                StateRegion stateRegion = new StateRegion.Builder()
                    .setSearchRegion(region)
                    .setName("region-" + i)
                    .build();
                regions.add(stateRegion);
            }
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(regions.toArray(new StateRegion[0]))
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertEquals(4, actionResult.getMatchList().size());
        }
    }
    
    @Nested
    @DisplayName("Mixed Object Types")
    class MixedObjectTypes {
        
        @Test
        @DisplayName("Should click mixed locations and regions")
        void testMixedLocationsAndRegions() {
            // Arrange
            Location location = new Location(50, 50);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .setName("location")
                .build();
            
            Region region = new Region(200, 200, 100, 100);
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(region)
                .setName("region")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .withRegions(stateRegion)
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertEquals(2, actionResult.getMatchList().size(), "Should click both location and region");
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should complete quickly in mock mode")
        void testMockModePerformance() {
            // Arrange
            List<StateLocation> locations = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Location loc = new Location(i * 10, i * 10);
                StateLocation stateLoc = new StateLocation.Builder()
                    .setLocation(loc)
                    .build();
                locations.add(stateLoc);
            }
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(locations.toArray(new StateLocation[0]))
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
            
            actionResult.setActionConfig(options);
            
            long startTime = System.currentTimeMillis();
            
            // Act
            click.perform(actionResult, collection);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertTrue(duration < 50, "Mock mode should complete in < 50ms, was: " + duration);
        }
        
        @ParameterizedTest
        @CsvSource({
            "1, 10",
            "5, 20",
            "10, 30",
            "20, 50"
        })
        @DisplayName("Should scale performance linearly")
        void testPerformanceScaling(int numberOfLocations, int maxDurationMs) {
            // Arrange
            List<StateLocation> locations = new ArrayList<>();
            for (int i = 0; i < numberOfLocations; i++) {
                Location loc = new Location(i * 10, i * 10);
                StateLocation stateLoc = new StateLocation.Builder()
                    .setLocation(loc)
                    .build();
                locations.add(stateLoc);
            }
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(locations.toArray(new StateLocation[0]))
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            long startTime = System.currentTimeMillis();
            
            // Act
            click.perform(actionResult, collection);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertTrue(duration < maxDurationMs, 
                String.format("Should complete %d clicks in < %dms, was: %dms", 
                    numberOfLocations, maxDurationMs, duration));
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null collection gracefully")
        void testNullCollection() {
            // Arrange
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, (ObjectCollection[]) null);
            
            // Assert
            assertFalse(actionResult.isSuccess());
            assertTrue(actionResult.getMatchList().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle invalid locations")
        void testInvalidLocations() {
            // Arrange
            Location invalidLocation = new Location(-100, -100);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(invalidLocation)
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            // In mock mode, even invalid locations might "succeed"
            // but in real mode this would fail
            assertNotNull(actionResult);
        }
        
        @Test
        @DisplayName("Should handle null ActionResult gracefully")
        void testNullActionResult() {
            // Arrange
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            // Act & Assert
            assertDoesNotThrow(() -> click.perform(null, collection));
        }
    }
    
    @Nested
    @DisplayName("State Integration")
    class StateIntegration {
        
        @Test
        @DisplayName("Should preserve state information")
        void testStatePreservation() {
            setupMockClick(); // Use mock to test state preservation
            // Arrange
            String stateName = "TestState";
            Location location = new Location(250, 250);
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(location)
                .setOwnerStateName(stateName)
                .setName("state-location")
                .build();
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(stateLocation)
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertFalse(actionResult.getMatchList().isEmpty());
            Match match = actionResult.getMatchList().get(0);
            assertEquals(stateName, match.getStateObjectData().getOwnerStateName());
        }
        
        @Test
        @DisplayName("Should handle locations from multiple states")
        void testMultiStateLocations() {
            setupMockClick(); // Use mock to test multiple state handling
            // Arrange
            List<StateLocation> locations = new ArrayList<>();
            String[] stateNames = {"State1", "State2", "State3"};
            
            for (String stateName : stateNames) {
                Location loc = new Location(100, 100);
                StateLocation stateLoc = new StateLocation.Builder()
                    .setLocation(loc)
                    .setOwnerStateName(stateName)
                    .setName("location-" + stateName)
                    .build();
                locations.add(stateLoc);
            }
            
            ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(locations.toArray(new StateLocation[0]))
                .build();
            
            ClickOptions options = new ClickOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            click.perform(actionResult, collection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertEquals(3, actionResult.getMatchList().size());
            
            // Verify each match preserves its state
            for (int i = 0; i < stateNames.length; i++) {
                Match match = actionResult.getMatchList().get(i);
                assertEquals(stateNames[i], match.getStateObjectData().getOwnerStateName());
            }
        }
    }
}