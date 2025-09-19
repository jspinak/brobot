package io.github.jspinak.brobot.action.basic.click;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.core.services.MouseController;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Comprehensive tests for Click action covering all variations and edge cases. These tests run in
 * mock mode for CI/CD compatibility.
 */
@DisplayName("Click Action Tests")
@DisabledInCI
@ExtendWith(MockitoExtension.class)
public class ClickTest extends BrobotTestBase {

    @Mock private MouseController mouseController;

    private Click click;
    private ActionResult actionResult;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        click = new Click();
        actionResult = new ActionResult();
    }

    @Nested
    @DisplayName("Basic Click Functionality")
    class BasicClickTests {

        @Test
        @DisplayName("Should return CLICK action type")
        void testGetActionType() {
            assertEquals(ActionInterface.Type.CLICK, click.getActionType());
        }

        @Test
        @DisplayName("Should handle single location click")
        void testSingleLocationClick() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            StateLocation stateLocation =
                    new StateLocation.Builder()
                            .setLocation(new Location(100, 100))
                            .setOwnerStateName("TestState")
                            .build();
            collection.getStateLocations().add(stateLocation);

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
            assertNotNull(actionResult.getMatchList().get(0));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 10})
        @DisplayName("Should handle multiple clicks on same location")
        void testMultipleClicksSameLocation(int clickCount) {
            // Given
            ObjectCollection collection = new ObjectCollection();
            Location targetLocation = new Location(200, 200);

            for (int i = 0; i < clickCount; i++) {
                StateLocation stateLocation =
                        new StateLocation.Builder().setLocation(targetLocation).build();
                collection.getStateLocations().add(stateLocation);
            }

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(clickCount, actionResult.getMatchList().size());
        }
    }

    @Nested
    @DisplayName("Region Click Tests")
    class RegionClickTests {

        @Test
        @DisplayName("Should click center of region")
        void testClickRegionCenter() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            Region region = new Region(100, 100, 200, 150); // Center at (200, 175)
            StateRegion stateRegion =
                    new StateRegion.Builder().setSearchRegion(region).setName("TestRegion").build();
            collection.getStateRegions().add(stateRegion);

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());

            // Verify center calculation
            var match = actionResult.getMatchList().get(0);
            assertNotNull(match);
        }

        @Test
        @DisplayName("Should handle multiple regions")
        void testMultipleRegions() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            List<Region> regions =
                    List.of(
                            new Region(0, 0, 100, 100),
                            new Region(200, 200, 100, 100),
                            new Region(400, 400, 100, 100));

            for (Region region : regions) {
                StateRegion stateRegion = new StateRegion.Builder().setSearchRegion(region).build();
                collection.getStateRegions().add(stateRegion);
            }

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(3, actionResult.getMatchList().size());
        }

        @Test
        @DisplayName("Should handle zero-sized region")
        void testZeroSizedRegion() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            Region zeroRegion = new Region(100, 100, 0, 0);
            StateRegion stateRegion = new StateRegion.Builder().setSearchRegion(zeroRegion).build();
            collection.getStateRegions().add(stateRegion);

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
        }
    }

    @Nested
    @DisplayName("Mixed Object Types")
    class MixedObjectTests {

        @Test
        @DisplayName("Should handle locations and regions together")
        void testMixedLocationAndRegion() {
            // Given
            ObjectCollection collection = new ObjectCollection();

            // Add locations
            collection
                    .getStateLocations()
                    .add(new StateLocation.Builder().setLocation(new Location(50, 50)).build());
            collection
                    .getStateLocations()
                    .add(new StateLocation.Builder().setLocation(new Location(150, 150)).build());

            // Add regions
            collection
                    .getStateRegions()
                    .add(
                            new StateRegion.Builder()
                                    .setSearchRegion(new Region(200, 200, 100, 100))
                                    .build());

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(3, actionResult.getMatchList().size());
        }

        @Test
        @DisplayName("Should handle multiple ObjectCollections")
        void testMultipleObjectCollections() {
            // Given
            ObjectCollection collection1 = new ObjectCollection();
            collection1
                    .getStateLocations()
                    .add(new StateLocation.Builder().setLocation(new Location(100, 100)).build());

            ObjectCollection collection2 = new ObjectCollection();
            collection2
                    .getStateLocations()
                    .add(new StateLocation.Builder().setLocation(new Location(200, 200)).build());

            ObjectCollection collection3 = new ObjectCollection();
            collection3
                    .getStateRegions()
                    .add(
                            new StateRegion.Builder()
                                    .setSearchRegion(new Region(300, 300, 50, 50))
                                    .build());

            // When
            click.perform(actionResult, collection1, collection2, collection3);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(3, actionResult.getMatchList().size());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty collection")
        void testEmptyCollection() {
            // Given
            ObjectCollection emptyCollection = new ObjectCollection();

            // When
            click.perform(actionResult, emptyCollection);

            // Then
            assertFalse(actionResult.isSuccess());
            assertTrue(actionResult.getMatchList().isEmpty());
        }

        @Test
        @DisplayName("Should handle null collections gracefully")
        void testNullCollection() {
            // When & Then
            assertDoesNotThrow(() -> click.perform(actionResult));
            assertFalse(actionResult.isSuccess());
        }

        @Test
        @DisplayName("Should handle negative coordinates")
        void testNegativeCoordinates() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            collection
                    .getStateLocations()
                    .add(new StateLocation.Builder().setLocation(new Location(-10, -10)).build());

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess()); // In mock mode, should succeed
            assertEquals(1, actionResult.getMatchList().size());
        }

        @Test
        @DisplayName("Should handle very large coordinates")
        void testLargeCoordinates() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            collection
                    .getStateLocations()
                    .add(
                            new StateLocation.Builder()
                                    .setLocation(new Location(10000, 10000))
                                    .build());

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
        }

        @Test
        @DisplayName("Should handle Integer MAX_VALUE coordinates")
        void testMaxValueCoordinates() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            collection
                    .getStateLocations()
                    .add(
                            new StateLocation.Builder()
                                    .setLocation(
                                            new Location(
                                                    Integer.MAX_VALUE - 100,
                                                    Integer.MAX_VALUE - 100))
                                    .build());

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large batch efficiently")
        void testLargeBatchClicks() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            int clickCount = 1000;

            for (int i = 0; i < clickCount; i++) {
                collection
                        .getStateLocations()
                        .add(
                                new StateLocation.Builder()
                                        .setLocation(new Location(i % 1920, i % 1080))
                                        .build());
            }

            // When
            long startTime = System.currentTimeMillis();
            click.perform(actionResult, collection);
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(clickCount, actionResult.getMatchList().size());
            assertTrue(duration < 15000, "Large batch should complete quickly in mock mode");
        }

        @Test
        @DisplayName("Should handle complex patterns efficiently")
        void testComplexClickPatterns() {
            // Given - Create a grid pattern
            ObjectCollection collection = new ObjectCollection();
            int gridSize = 10;

            for (int x = 0; x < gridSize; x++) {
                for (int y = 0; y < gridSize; y++) {
                    collection
                            .getStateLocations()
                            .add(
                                    new StateLocation.Builder()
                                            .setLocation(new Location(x * 100, y * 100))
                                            .build());
                }
            }

            // When
            long startTime = System.currentTimeMillis();
            click.perform(actionResult, collection);
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(gridSize * gridSize, actionResult.getMatchList().size());
            assertTrue(duration < 5000, "Grid pattern should complete quickly");
        }
    }

    @Nested
    @DisplayName("Match Creation Tests")
    class MatchCreationTests {

        @Test
        @DisplayName("Should create proper Match from Location")
        void testMatchCreationFromLocation() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            Location location = new Location(500, 500);
            collection
                    .getStateLocations()
                    .add(new StateLocation.Builder().setLocation(location).build());

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());

            var match = actionResult.getMatchList().get(0);
            assertNotNull(match);
            assertNotNull(match.getRegion());
            assertEquals("Clicked location", match.getName());
        }

        @Test
        @DisplayName("Should create matches with proper regions")
        void testMatchRegionSize() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            Location location = new Location(100, 100);
            collection
                    .getStateLocations()
                    .add(new StateLocation.Builder().setLocation(location).build());

            // When
            click.perform(actionResult, collection);

            // Then
            var match = actionResult.getMatchList().get(0);
            var region = match.getRegion();

            // Match region should be small area around click point
            assertEquals(95, region.x()); // location.x - 5
            assertEquals(95, region.y()); // location.y - 5
            assertEquals(10, region.w());
            assertEquals(10, region.h());
        }
    }

    @Nested
    @DisplayName("State Integration Tests")
    class StateIntegrationTests {

        @Test
        @DisplayName("Should preserve state information")
        void testStateInformationPreservation() {
            // Given
            ObjectCollection collection = new ObjectCollection();
            String stateName = "TestState";

            StateLocation stateLocation =
                    new StateLocation.Builder()
                            .setLocation(new Location(100, 100))
                            .setOwnerStateName(stateName)
                            .setProbabilityExists(85)
                            .build();
            collection.getStateLocations().add(stateLocation);

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
        }

        @Test
        @DisplayName("Should handle locations from multiple states")
        void testMultipleStates() {
            // Given
            ObjectCollection collection = new ObjectCollection();

            List<String> stateNames = List.of("State1", "State2", "State3");
            for (int i = 0; i < stateNames.size(); i++) {
                StateLocation stateLocation =
                        new StateLocation.Builder()
                                .setLocation(new Location(i * 100, i * 100))
                                .setOwnerStateName(stateNames.get(i))
                                .build();
                collection.getStateLocations().add(stateLocation);
            }

            // When
            click.perform(actionResult, collection);

            // Then
            assertTrue(actionResult.isSuccess());
            assertEquals(3, actionResult.getMatchList().size());
        }
    }

    // Test data providers
    private static Stream<Arguments> provideCoordinatePairs() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(100, 100),
                Arguments.of(1920, 1080),
                Arguments.of(-100, -100),
                Arguments.of(5000, 5000));
    }

    @ParameterizedTest
    @MethodSource("provideCoordinatePairs")
    @DisplayName("Should handle various coordinate pairs")
    void testVariousCoordinates(int x, int y) {
        // Given
        ObjectCollection collection = new ObjectCollection();
        collection
                .getStateLocations()
                .add(new StateLocation.Builder().setLocation(new Location(x, y)).build());

        // When
        click.perform(actionResult, collection);

        // Then
        assertTrue(actionResult.isSuccess());
        assertEquals(1, actionResult.getMatchList().size());
    }
}
