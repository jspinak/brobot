package io.github.jspinak.brobot.util.location;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for LocationUtils - utilities for Location manipulation. Tests
 * conversions, calculations, and transformations of Location objects.
 */
@DisplayName("LocationUtils Tests")
public class LocationUtilsTest extends BrobotTestBase {

    @Mock private Region mockRegion;

    @Mock private Position mockPosition;

    private Location xyLocation;
    private Location regionLocation;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Setup XY-based location
        xyLocation = new Location(100, 200);

        // Setup region-based location
        when(mockRegion.x()).thenReturn(50);
        when(mockRegion.y()).thenReturn(75);
        when(mockRegion.w()).thenReturn(300);
        when(mockRegion.h()).thenReturn(400);

        when(mockPosition.getPercentW()).thenReturn(0.5);
        when(mockPosition.getPercentH()).thenReturn(0.5);

        regionLocation = new Location(mockRegion, mockPosition);
    }

    @Nested
    @DisplayName("Sikuli Location Conversions")
    class SikuliLocationConversions {

        @Test
        @DisplayName("Convert XY coordinates to Sikuli location")
        public void testGetSikuliLocationFromXY() {
            org.sikuli.script.Location sikuliLoc =
                    LocationUtils.getSikuliLocationFromXY(100, 200, 10, 20);

            assertEquals(110, sikuliLoc.x);
            assertEquals(220, sikuliLoc.y);
        }

        @Test
        @DisplayName("Convert XY with zero offset")
        public void testGetSikuliLocationFromXYNoOffset() {
            org.sikuli.script.Location sikuliLoc =
                    LocationUtils.getSikuliLocationFromXY(100, 200, 0, 0);

            assertEquals(100, sikuliLoc.x);
            assertEquals(200, sikuliLoc.y);
        }

        @Test
        @DisplayName("Convert region and position to Sikuli location")
        public void testGetSikuliLocationFromRegion() {
            org.sikuli.script.Location sikuliLoc =
                    LocationUtils.getSikuliLocationFromRegion(mockRegion, mockPosition, 5, 10);

            // Expected: x = 50 + (300 * 0.5) + 5 = 205
            //          y = 75 + (400 * 0.5) + 10 = 285
            assertEquals(205.0, sikuliLoc.x, 0.01);
            assertEquals(285.0, sikuliLoc.y, 0.01);
        }

        @Test
        @DisplayName("Get Sikuli location from XY-based Location")
        public void testGetSikuliLocationFromXYLocation() {
            xyLocation.setOffsetX(10);
            xyLocation.setOffsetY(15);

            org.sikuli.script.Location sikuliLoc = LocationUtils.getSikuliLocation(xyLocation);

            assertEquals(110, sikuliLoc.x);
            assertEquals(215, sikuliLoc.y);
        }

        @Test
        @DisplayName("Get Sikuli location from region-based Location")
        public void testGetSikuliLocationFromRegionLocation() {
            regionLocation.setOffsetX(5);
            regionLocation.setOffsetY(10);

            org.sikuli.script.Location sikuliLoc = LocationUtils.getSikuliLocation(regionLocation);

            assertEquals(205.0, sikuliLoc.x, 0.01);
            assertEquals(285.0, sikuliLoc.y, 0.01);
        }
    }

    @Nested
    @DisplayName("Location Type Checking")
    class LocationTypeChecking {

        @Test
        @DisplayName("Check if location is defined by XY")
        public void testIsDefinedByXY() {
            assertTrue(LocationUtils.isDefinedByXY(xyLocation));
            assertFalse(LocationUtils.isDefinedByXY(regionLocation));
        }

        @Test
        @DisplayName("Check if location is defined with region")
        public void testIsDefinedWithRegion() {
            assertFalse(LocationUtils.isDefinedWithRegion(xyLocation));
            assertTrue(LocationUtils.isDefinedWithRegion(regionLocation));
        }

        @Test
        @DisplayName("Location with null region is XY-based")
        public void testNullRegionIsXYBased() {
            Location location = new Location(100, 200);
            location.setRegion(null);

            assertTrue(LocationUtils.isDefinedByXY(location));
            assertFalse(LocationUtils.isDefinedWithRegion(location));
        }
    }

    @Nested
    @DisplayName("Region Dimensions")
    class RegionDimensions {

        @Test
        @DisplayName("Get region width for XY location")
        public void testGetRegionWForXYLocation() {
            assertEquals(1, LocationUtils.getRegionW(xyLocation));
        }

        @Test
        @DisplayName("Get region width for region location")
        public void testGetRegionWForRegionLocation() {
            assertEquals(300, LocationUtils.getRegionW(regionLocation));
        }

        @Test
        @DisplayName("Get region height for XY location")
        public void testGetRegionHForXYLocation() {
            assertEquals(1, LocationUtils.getRegionH(xyLocation));
        }

        @Test
        @DisplayName("Get region height for region location")
        public void testGetRegionHForRegionLocation() {
            assertEquals(400, LocationUtils.getRegionH(regionLocation));
        }
    }

    @Nested
    @DisplayName("Location Definition Check")
    class LocationDefinitionCheck {

        @Test
        @DisplayName("XY location with positive coordinates is defined")
        public void testIsDefinedPositiveXY() {
            Location location = new Location(100, 200);
            assertTrue(LocationUtils.isDefined(location));
        }

        @Test
        @DisplayName("XY location with negative X is defined")
        public void testIsDefinedNegativeX() {
            Location location = new Location(-10, 200);
            assertTrue(LocationUtils.isDefined(location));
        }

        @Test
        @DisplayName("XY location with negative Y is defined")
        public void testIsDefinedNegativeY() {
            Location location = new Location(100, -20);
            assertTrue(LocationUtils.isDefined(location));
        }

        @Test
        @DisplayName("Region location is always defined")
        public void testIsDefinedRegionLocation() {
            assertTrue(LocationUtils.isDefined(regionLocation));
        }
    }

    @Nested
    @DisplayName("Conversions to Other Types")
    class ConversionsToOtherTypes {

        @Test
        @DisplayName("Convert location to match")
        public void testToMatch() {
            Location location = new Location(150, 250);
            Match match = LocationUtils.toMatch(location);

            assertNotNull(match);
            assertEquals(150, match.getRegion().x());
            assertEquals(250, match.getRegion().y());
            assertEquals(1, match.getRegion().w());
            assertEquals(1, match.getRegion().h());
        }

        @Test
        @DisplayName("Create StateLocation in null state")
        public void testAsStateLocationInNullState() {
            StateLocation stateLocation = LocationUtils.asStateLocationInNullState(xyLocation);

            assertNotNull(stateLocation);
            assertEquals("null", stateLocation.getOwnerStateName());
            assertEquals(xyLocation, stateLocation.getLocation());
        }

        @Test
        @DisplayName("Create ObjectCollection from location")
        public void testAsObjectCollection() {
            ObjectCollection collection = LocationUtils.asObjectCollection(xyLocation);

            assertNotNull(collection);
            assertFalse(collection.getStateLocations().isEmpty());
            StateLocation stateLocation = collection.getStateLocations().get(0);
            assertEquals("null", stateLocation.getOwnerStateName());
            // Position is created from TOPLEFT which has coordinates (0.0, 0.0)
            Position expectedPosition = new Position(Positions.Name.TOPLEFT);
            assertEquals(
                    expectedPosition.getPercentW(),
                    stateLocation.getPosition().getPercentW(),
                    0.001);
            assertEquals(
                    expectedPosition.getPercentH(),
                    stateLocation.getPosition().getPercentH(),
                    0.001);
        }
    }

    @Nested
    @DisplayName("Opposite Location Calculations")
    class OppositeLocationCalculations {

        @Test
        @DisplayName("Get opposite of XY location returns same location")
        public void testGetOppositeXYLocation() {
            Location opposite = LocationUtils.getOpposite(xyLocation);
            assertEquals(xyLocation, opposite);
        }

        @Test
        @DisplayName("Get opposite of region location")
        public void testGetOppositeRegionLocation() {
            Location opposite = LocationUtils.getOpposite(regionLocation);

            assertNotNull(opposite);
            assertEquals(mockRegion, opposite.getRegion());
            assertEquals(0.5, opposite.getPosition().getPercentW(), 0.01);
            assertEquals(0.5, opposite.getPosition().getPercentH(), 0.01);
        }

        @Test
        @DisplayName("Get opposite to another location")
        public void testGetOppositeTo() {
            Location location1 = new Location(100, 100);
            Location location2 = new Location(150, 125);

            Location opposite = LocationUtils.getOppositeTo(location1, location2);

            // Expected: x = 100 + 2*(150-100) = 200
            //          y = 100 + 2*(125-100) = 150
            assertEquals(200, opposite.getCalculatedX());
            assertEquals(150, opposite.getCalculatedY());
        }
    }

    @Nested
    @DisplayName("Region Adjustment")
    class RegionAdjustment {

        @Test
        @DisplayName("Adjust location to region bounds")
        public void testAdjustToRegion() {
            Position outOfBounds = new Position(1.5, -0.5);
            Location location = new Location(mockRegion, outOfBounds);

            LocationUtils.adjustToRegion(location);

            assertEquals(1.0, location.getPosition().getPercentW(), 0.01);
            assertEquals(0.0, location.getPosition().getPercentH(), 0.01);
        }

        @Test
        @DisplayName("Adjust XY location does nothing")
        public void testAdjustToRegionXYLocation() {
            Location original = new Location(100, 200);
            LocationUtils.adjustToRegion(original);

            assertEquals(100, original.getCalculatedX());
            assertEquals(200, original.getCalculatedY());
        }

        @ParameterizedTest
        @CsvSource({
            "0.5, 0.5, 0.5, 0.5", // Within bounds
            "1.2, 0.8, 1.0, 0.8", // X out of bounds
            "0.3, -0.2, 0.3, 0.0", // Y out of bounds
            "2.0, 2.0, 1.0, 1.0" // Both out of bounds
        })
        @DisplayName("Adjust various positions to region")
        public void testAdjustVariousPositions(
                double inW, double inH, double expectedW, double expectedH) {
            Position position = new Position(inW, inH);
            Location location = new Location(mockRegion, position);

            LocationUtils.adjustToRegion(location);

            assertEquals(expectedW, location.getPosition().getPercentW(), 0.01);
            assertEquals(expectedH, location.getPosition().getPercentH(), 0.01);
        }
    }

    @Nested
    @DisplayName("Polar Coordinate Operations")
    class PolarCoordinateOperations {

        @ParameterizedTest
        @CsvSource({
            "0, 100", // Right
            "90, 100", // Up
            "180, 100", // Left
            "270, 100", // Down
            "45, 141.42" // Diagonal
        })
        @DisplayName("Set location from center with angle and distance")
        public void testSetFromCenter(double angle, double distance) {
            Location location = new Location(100, 100);
            LocationUtils.setFromCenter(location, angle, distance);

            // Verify location has moved
            assertTrue(location.getCalculatedX() != 100 || location.getCalculatedY() != 100);
        }

        @Test
        @DisplayName("Set from center for XY location")
        public void testSetFromCenterXYLocation() {
            Location location = new Location(100, 100);
            LocationUtils.setFromCenter(location, 0, 50); // Move 50 pixels to the right

            assertEquals(150, location.getCalculatedX());
            assertEquals(100, location.getCalculatedY());
        }

        @Test
        @DisplayName("Set from center for region location")
        public void testSetFromCenterRegionLocation() {
            Location location = new Location(mockRegion, new Position(0.5, 0.5));
            LocationUtils.setFromCenter(location, 90, 100); // Move 100 pixels up

            assertNotNull(location.getPosition());
        }
    }

    @Nested
    @DisplayName("Location Equality")
    class LocationEquality {

        @Test
        @DisplayName("Equal XY locations")
        public void testEqualsXYLocations() {
            Location loc1 = new Location(100, 200);
            Location loc2 = new Location(100, 200);

            assertTrue(LocationUtils.equals(loc1, loc2));
        }

        @Test
        @DisplayName("Different XY locations")
        public void testNotEqualsXYLocations() {
            Location loc1 = new Location(100, 200);
            Location loc2 = new Location(150, 200);

            assertFalse(LocationUtils.equals(loc1, loc2));
        }

        @Test
        @DisplayName("XY vs Region locations are not equal")
        public void testXYvsRegionNotEqual() {
            Location xyLoc = new Location(200, 275);

            // Even if calculated positions are same
            when(mockRegion.x()).thenReturn(0);
            when(mockRegion.y()).thenReturn(0);
            when(mockRegion.w()).thenReturn(400);
            when(mockRegion.h()).thenReturn(550);
            Location regionLoc = new Location(mockRegion, new Position(0.5, 0.5));

            assertFalse(LocationUtils.equals(xyLoc, regionLoc));
        }
    }

    @Nested
    @DisplayName("Location Addition")
    class LocationAddition {

        @Test
        @DisplayName("Add XY locations")
        public void testAddXYLocations() {
            Location loc1 = new Location(100, 200);
            Location loc2 = new Location(50, 75);

            LocationUtils.add(loc1, loc2);

            assertEquals(150, loc1.getCalculatedX());
            assertEquals(275, loc1.getCalculatedY());
        }

        @Test
        @DisplayName("Add region locations")
        public void testAddRegionLocations() {
            Position pos1 = new Position(0.3, 0.4);
            Position pos2 = new Position(0.2, 0.1);
            Location loc1 = new Location(mockRegion, pos1);
            Location loc2 = new Location(mockRegion, pos2);

            LocationUtils.add(loc1, loc2);

            assertEquals(0.5, loc1.getPosition().getPercentW(), 0.01);
            assertEquals(0.5, loc1.getPosition().getPercentH(), 0.01);
        }

        @Test
        @DisplayName("Add with negative values")
        public void testAddNegativeValues() {
            Location loc1 = new Location(100, 200);
            Location loc2 = new Location(-30, -50);

            LocationUtils.add(loc1, loc2);

            assertEquals(70, loc1.getCalculatedX());
            assertEquals(150, loc1.getCalculatedY());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Handle location at origin")
        public void testLocationAtOrigin() {
            Location origin = new Location(0, 0);

            assertTrue(LocationUtils.isDefined(origin));
            assertEquals(1, LocationUtils.getRegionW(origin));
            assertEquals(1, LocationUtils.getRegionH(origin));
        }

        @Test
        @DisplayName("Handle very large coordinates")
        public void testVeryLargeCoordinates() {
            Location large = new Location(Integer.MAX_VALUE, Integer.MAX_VALUE);

            assertTrue(LocationUtils.isDefined(large));
            Match match = LocationUtils.toMatch(large);
            assertEquals(Integer.MAX_VALUE, match.getRegion().x());
        }

        @Test
        @DisplayName("Region with zero dimensions")
        public void testZeroDimensionRegion() {
            when(mockRegion.w()).thenReturn(0);
            when(mockRegion.h()).thenReturn(0);
            Location location = new Location(mockRegion, mockPosition);

            assertEquals(0, LocationUtils.getRegionW(location));
            assertEquals(0, LocationUtils.getRegionH(location));
        }
    }
}
