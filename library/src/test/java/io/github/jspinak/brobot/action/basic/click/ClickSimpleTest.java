package io.github.jspinak.brobot.action.basic.click;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Simple tests for Click action that work within mock mode constraints. These tests verify the
 * basic structure and flow without requiring full Spring context or complex dependency injection.
 */
@DisplayName("Simple Click Action Tests")
@DisabledInCI
public class ClickSimpleTest extends BrobotTestBase {

    private ActionResult actionResult;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        actionResult = new ActionResult();
    }

    @Test
    @DisplayName("Should create click options with default values")
    void testClickOptionsDefaults() {
        // Arrange & Act
        ClickOptions options = new ClickOptions.Builder().build();

        // Assert
        assertNotNull(options);
        assertEquals(1, options.getNumberOfClicks());
        assertNotNull(options.getMousePressOptions());
        assertNotNull(options.getVerificationOptions());
        assertNotNull(options.getRepetitionOptions());
    }

    @Test
    @DisplayName("Should create click options with custom click count")
    void testClickOptionsWithCustomClicks() {
        // Arrange & Act
        ClickOptions options = new ClickOptions.Builder().setNumberOfClicks(3).build();

        // Assert
        assertEquals(3, options.getNumberOfClicks());
    }

    @Test
    @DisplayName("Should create ObjectCollection with locations")
    void testObjectCollectionWithLocations() {
        // Arrange
        Location location1 = new Location(100, 100);
        Location location2 = new Location(200, 200);

        StateLocation stateLocation1 =
                new StateLocation.Builder().setLocation(location1).setName("loc1").build();

        StateLocation stateLocation2 =
                new StateLocation.Builder().setLocation(location2).setName("loc2").build();

        // Act
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withLocations(stateLocation1, stateLocation2)
                        .build();

        // Assert
        assertNotNull(collection);
        assertEquals(2, collection.getStateLocations().size());
    }

    @Test
    @DisplayName("Should create ObjectCollection with regions")
    void testObjectCollectionWithRegions() {
        // Arrange
        Region region1 = new Region(0, 0, 100, 100);
        Region region2 = new Region(100, 100, 200, 150);

        StateRegion stateRegion1 =
                new StateRegion.Builder().setSearchRegion(region1).setName("region1").build();

        StateRegion stateRegion2 =
                new StateRegion.Builder().setSearchRegion(region2).setName("region2").build();

        // Act
        ObjectCollection collection =
                new ObjectCollection.Builder().withRegions(stateRegion1, stateRegion2).build();

        // Assert
        assertNotNull(collection);
        assertEquals(2, collection.getStateRegions().size());
    }

    @Test
    @DisplayName("Should handle mixed ObjectCollection")
    void testMixedObjectCollection() {
        // Arrange
        Location location = new Location(50, 50);
        StateLocation stateLocation =
                new StateLocation.Builder().setLocation(location).setName("location").build();

        Region region = new Region(100, 100, 50, 50);
        StateRegion stateRegion =
                new StateRegion.Builder().setSearchRegion(region).setName("region").build();

        // Act
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withLocations(stateLocation)
                        .withRegions(stateRegion)
                        .build();

        // Assert
        assertNotNull(collection);
        assertEquals(1, collection.getStateLocations().size());
        assertEquals(1, collection.getStateRegions().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 10})
    @DisplayName("Should create click options with various click counts")
    void testVariousClickCounts(int clicks) {
        // Act
        ClickOptions options = new ClickOptions.Builder().setNumberOfClicks(clicks).build();

        // Assert
        assertEquals(clicks, options.getNumberOfClicks());
    }

    @Test
    @DisplayName("Should create locations at screen positions")
    void testScreenPositions() {
        // Test each standard screen position
        for (Positions.Name position : Positions.Name.values()) {
            // Act
            Location location = new Location(position);

            // Assert
            assertNotNull(location);
            // When created with a Position, x and y are -1 until calculated
            // The position itself is what matters
            assertNotNull(location.getPosition());
            // The position should have valid percentage values
            assertTrue(location.getPosition().getPercentW() >= 0);
            assertTrue(location.getPosition().getPercentW() <= 1);
            assertTrue(location.getPosition().getPercentH() >= 0);
            assertTrue(location.getPosition().getPercentH() <= 1);
        }
    }

    @Test
    @DisplayName("Should create StateLocation with state name")
    void testStateLocationWithState() {
        // Arrange
        String stateName = "TestState";
        Location location = new Location(150, 250);

        // Act
        StateLocation stateLocation =
                new StateLocation.Builder()
                        .setLocation(location)
                        .setOwnerStateName(stateName)
                        .setName("state-location")
                        .build();

        // Assert
        assertNotNull(stateLocation);
        assertEquals(stateName, stateLocation.getOwnerStateName());
        assertEquals("state-location", stateLocation.getName());
        assertEquals(150, stateLocation.getLocation().getX());
        assertEquals(250, stateLocation.getLocation().getY());
    }

    @Test
    @DisplayName("Should build complex click options")
    void testComplexClickOptions() {
        // Act
        ClickOptions options = new ClickOptions.Builder().setNumberOfClicks(2).build();

        // Assert
        assertNotNull(options);
        assertEquals(2, options.getNumberOfClicks());
    }

    @Test
    @DisplayName("Should handle empty ObjectCollection")
    void testEmptyObjectCollection() {
        // Act
        ObjectCollection empty = new ObjectCollection.Builder().build();

        // Assert
        assertNotNull(empty);
        assertTrue(empty.getStateLocations().isEmpty());
        assertTrue(empty.getStateRegions().isEmpty());
        assertTrue(empty.getStateImages().isEmpty());
    }

    @Test
    @DisplayName("Should create ActionResult")
    void testActionResult() {
        // Arrange
        ActionResult result = new ActionResult();
        ClickOptions options = new ClickOptions.Builder().setNumberOfClicks(2).build();

        // Act
        result.setActionConfig(options);
        result.setSuccess(true);

        // Assert
        assertTrue(result.isSuccess());
        assertNotNull(result.getActionConfig());
        assertTrue(result.getActionConfig() instanceof ClickOptions);
        assertEquals(2, ((ClickOptions) result.getActionConfig()).getNumberOfClicks());
    }

    @Test
    @DisplayName("Should handle large number of locations")
    void testManyLocations() {
        // Arrange
        List<StateLocation> locations = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Location loc = new Location(i * 10, i * 10);
            StateLocation stateLoc =
                    new StateLocation.Builder().setLocation(loc).setName("location-" + i).build();
            locations.add(stateLoc);
        }

        // Act
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withLocations(locations.toArray(new StateLocation[0]))
                        .build();

        // Assert
        assertNotNull(collection);
        assertEquals(100, collection.getStateLocations().size());
    }

    @Test
    @DisplayName("Should create region with dimensions")
    void testRegionDimensions() {
        // Arrange
        int x = 100, y = 150, w = 200, h = 250;

        // Act
        Region region = new Region(x, y, w, h);

        // Assert
        assertNotNull(region);
        assertEquals(x, region.x());
        assertEquals(y, region.y());
        assertEquals(w, region.w());
        assertEquals(h, region.h());
    }

    @Test
    @DisplayName("Should calculate region center")
    void testRegionCenter() {
        // Arrange
        Region region = new Region(100, 100, 200, 100);

        // Act
        int centerX = region.x() + region.w() / 2;
        int centerY = region.y() + region.h() / 2;

        // Assert
        assertEquals(200, centerX); // 100 + 200/2
        assertEquals(150, centerY); // 100 + 100/2
    }
}
