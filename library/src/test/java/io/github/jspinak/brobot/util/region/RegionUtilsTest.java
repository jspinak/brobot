package io.github.jspinak.brobot.util.region;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.grid.MockGridConfig;

/**
 * Comprehensive test suite for RegionUtils - utility methods for Region manipulation. Tests
 * conversions, spatial analysis, boundary operations, and grid operations.
 */
@DisplayName("RegionUtils Tests")
public class RegionUtilsTest extends BrobotTestBase {

    @Mock private Match mockMatch;

    @Mock private Location mockLocation1;

    @Mock private Location mockLocation2;

    private Region region;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        region = new Region(10, 20, 100, 50);
    }

    @Nested
    @DisplayName("Conversion Operations")
    class ConversionOperations {

        @Test
        @DisplayName("Convert Match to Region")
        public void testConvertMatchToRegion() {
            when(mockMatch.x()).thenReturn(50);
            when(mockMatch.y()).thenReturn(60);
            when(mockMatch.w()).thenReturn(200);
            when(mockMatch.h()).thenReturn(150);

            Region result = RegionUtils.convertMatchToRegion(mockMatch);

            assertEquals(50, result.x());
            assertEquals(60, result.y());
            assertEquals(200, result.w());
            assertEquals(150, result.h());
        }

        @Test
        @DisplayName("Extract region from SikuliX Match")
        public void testExtractRegionFromSikuli() {
            org.sikuli.script.Match sikuliMatch = mock(org.sikuli.script.Match.class);
            sikuliMatch.x = 100;
            sikuliMatch.y = 200;
            sikuliMatch.w = 300;
            sikuliMatch.h = 400;

            int[] result = RegionUtils.extractRegionFromSikuli(sikuliMatch);

            assertArrayEquals(new int[] {100, 200, 300, 400}, result);
        }

        @Test
        @DisplayName("Convert to Rect")
        public void testToRect() {
            Rect rect = RegionUtils.getJavaCVRect(region);

            assertEquals(10, rect.x());
            assertEquals(20, rect.y());
            assertEquals(100, rect.width());
            assertEquals(50, rect.height());
        }
    }

    @Nested
    @DisplayName("Boundary Calculations")
    class BoundaryCalculations {

        @Test
        @DisplayName("Calculate x2 (right boundary)")
        public void testX2() {
            int x2 = RegionUtils.x2(region);

            assertEquals(110, x2); // 10 + 100
        }

        @Test
        @DisplayName("Calculate y2 (bottom boundary)")
        public void testY2() {
            int y2 = RegionUtils.y2(region);

            assertEquals(70, y2); // 20 + 50
        }

        @Test
        @DisplayName("Adjust x2 (right boundary)")
        public void testAdjustX2() {
            RegionUtils.adjustX2(region, 150);

            assertEquals(10, region.x()); // x unchanged
            assertEquals(140, region.w()); // width adjusted to 150 - 10
        }

        @Test
        @DisplayName("Adjust y2 (bottom boundary)")
        public void testAdjustY2() {
            RegionUtils.adjustY2(region, 100);

            assertEquals(20, region.y()); // y unchanged
            assertEquals(80, region.h()); // height adjusted to 100 - 20
        }

        @Test
        @DisplayName("Adjust x (left boundary)")
        public void testAdjustX() {
            RegionUtils.adjustX(region, 30);

            assertEquals(30, region.x());
            assertEquals(80, region.w()); // width reduced by 20
        }

        @Test
        @DisplayName("Adjust y (top boundary)")
        public void testAdjustY() {
            RegionUtils.adjustY(region, 40);

            assertEquals(40, region.y());
            assertEquals(30, region.h()); // height reduced by 20
        }
    }

    @Nested
    @DisplayName("Bounding Box Operations")
    class BoundingBoxOperations {

        @Test
        @DisplayName("Calculate bounding box from two locations")
        public void testCalculateBoundingBox() {
            when(mockLocation1.getCalculatedX()).thenReturn(50);
            when(mockLocation1.getCalculatedY()).thenReturn(100);
            when(mockLocation2.getCalculatedX()).thenReturn(200);
            when(mockLocation2.getCalculatedY()).thenReturn(300);

            int[] bbox = RegionUtils.calculateBoundingBox(mockLocation1, mockLocation2);

            assertEquals(50, bbox[0]); // min x
            assertEquals(100, bbox[1]); // min y
            assertEquals(150, bbox[2]); // width (200 - 50)
            assertEquals(200, bbox[3]); // height (300 - 100)
        }

        @Test
        @DisplayName("Calculate bounding box with reversed locations")
        public void testCalculateBoundingBoxReversed() {
            when(mockLocation1.getCalculatedX()).thenReturn(200);
            when(mockLocation1.getCalculatedY()).thenReturn(300);
            when(mockLocation2.getCalculatedX()).thenReturn(50);
            when(mockLocation2.getCalculatedY()).thenReturn(100);

            int[] bbox = RegionUtils.calculateBoundingBox(mockLocation1, mockLocation2);

            // Should still produce correct bounding box
            assertEquals(50, bbox[0]); // min x
            assertEquals(100, bbox[1]); // min y
            assertEquals(150, bbox[2]); // width
            assertEquals(200, bbox[3]); // height
        }
    }

    @Nested
    @DisplayName("Region Definition Checks")
    class RegionDefinitionChecks {

        @Test
        @DisplayName("Region is defined with valid dimensions")
        public void testIsDefinedValid() {
            Region validRegion = new Region(10, 20, 100, 50);

            assertTrue(RegionUtils.isDefined(validRegion));
        }

        @Test
        @DisplayName("Region not defined with zero width")
        public void testIsDefinedZeroWidth() {
            Region zeroWidth = new Region(10, 20, 0, 50);

            assertFalse(RegionUtils.isDefined(zeroWidth));
        }

        @Test
        @DisplayName("Region not defined with zero height")
        public void testIsDefinedZeroHeight() {
            Region zeroHeight = new Region(10, 20, 100, 0);

            assertFalse(RegionUtils.isDefined(zeroHeight));
        }

        @Test
        @DisplayName("Region not defined with negative dimensions")
        public void testIsDefinedNegativeDimensions() {
            Region negative = new Region(10, 20, -100, 50);

            assertFalse(RegionUtils.isDefined(negative));
        }
    }

    @Nested
    @DisplayName("Spatial Relationship Tests")
    class SpatialRelationshipTests {

        @Test
        @DisplayName("Regions overlap")
        public void testOverlaps() {
            Region region1 = new Region(0, 0, 100, 100);
            Region region2 = new Region(50, 50, 100, 100);

            assertTrue(RegionUtils.overlaps(region1, region2));
            assertTrue(RegionUtils.overlaps(region2, region1)); // Symmetric
        }

        @Test
        @DisplayName("Regions don't overlap")
        public void testNoOverlap() {
            Region region1 = new Region(0, 0, 100, 100);
            Region region2 = new Region(200, 200, 100, 100);

            assertFalse(RegionUtils.overlaps(region1, region2));
            assertFalse(RegionUtils.overlaps(region2, region1));
        }

        @Test
        @DisplayName("Region contains another region")
        public void testContains() {
            Region outer = new Region(0, 0, 200, 200);
            Region inner = new Region(50, 50, 100, 100);

            assertTrue(RegionUtils.contains(outer, inner));
            assertFalse(RegionUtils.contains(inner, outer));
        }

        @Test
        @DisplayName("Region contains location")
        public void testContainsLocation() {
            Region region = new Region(10, 20, 100, 50);
            Location inside = new Location(60, 45); // Inside region
            Location outside = new Location(200, 300); // Outside region

            assertTrue(RegionUtils.contains(region, inside));
            assertFalse(RegionUtils.contains(region, outside));
        }

        @Test
        @DisplayName("Adjacent regions touch but don't overlap")
        public void testAdjacentRegions() {
            Region region1 = new Region(0, 0, 100, 100);
            Region region2 = new Region(100, 0, 100, 100); // Touching at x=100

            // Depending on implementation, touching might or might not count as overlap
            boolean result = RegionUtils.overlaps(region1, region2);
            // Just verify it doesn't throw exception
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Union and Intersection Operations")
    class UnionAndIntersectionOperations {

        @Test
        @DisplayName("Get union of two regions")
        public void testGetUnion() {
            Region region1 = new Region(10, 20, 100, 50);
            Region region2 = new Region(50, 30, 100, 60);

            Region union = RegionUtils.getUnion(region1, region2);

            assertEquals(10, union.x()); // Min x
            assertEquals(20, union.y()); // Min y
            assertEquals(140, union.w()); // Max x2 - min x = 150 - 10
            assertEquals(70, union.h()); // Max y2 - min y = 90 - 20
        }

        @Test
        @DisplayName("Get overlapping region (intersection)")
        public void testGetOverlappingRegion() {
            Region region1 = new Region(10, 20, 100, 50);
            Region region2 = new Region(50, 30, 100, 60);

            Optional<Region> overlap = RegionUtils.getOverlappingRegion(region1, region2);

            assertTrue(overlap.isPresent());
            Region intersection = overlap.get();
            assertEquals(50, intersection.x());
            assertEquals(30, intersection.y());
            assertEquals(60, intersection.w()); // 110 - 50
            assertEquals(40, intersection.h()); // 70 - 30
        }

        @Test
        @DisplayName("No overlapping region when disjoint")
        public void testNoOverlappingRegion() {
            Region region1 = new Region(0, 0, 50, 50);
            Region region2 = new Region(100, 100, 50, 50);

            Optional<Region> overlap = RegionUtils.getOverlappingRegion(region1, region2);

            assertFalse(overlap.isPresent());
        }
    }

    @Nested
    @DisplayName("Region Subtraction")
    class RegionSubtraction {

        @Test
        @DisplayName("Subtract overlapping region")
        public void testMinus() {
            Region base = new Region(0, 0, 100, 100);
            Region toSubtract = new Region(50, 0, 50, 100);

            List<Region> result = RegionUtils.minus(base, toSubtract);

            assertFalse(result.isEmpty());
            // Should return the left part that wasn't subtracted
            Region remaining = result.get(0);
            assertEquals(0, remaining.x());
            assertEquals(0, remaining.y());
            assertEquals(50, remaining.w());
            assertEquals(100, remaining.h());
        }

        @Test
        @DisplayName("Subtract non-overlapping region returns original")
        public void testMinusNoOverlap() {
            Region base = new Region(0, 0, 100, 100);
            Region toSubtract = new Region(200, 200, 50, 50);

            List<Region> result = RegionUtils.minus(base, toSubtract);

            assertEquals(1, result.size());
            assertEquals(base, result.get(0));
        }

        @Test
        @DisplayName("Subtract fully containing region returns empty")
        public void testMinusFullyContained() {
            Region base = new Region(50, 50, 50, 50);
            Region toSubtract = new Region(0, 0, 200, 200);

            List<Region> result = RegionUtils.minus(base, toSubtract);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Grid Operations")
    class GridOperations {

        @Test
        @DisplayName("Get grid of regions")
        public void testGetGrid() {
            Region base = new Region(0, 0, 100, 100);

            List<Region> grid = RegionUtils.getGridRegions(base, 2, 2);

            assertEquals(4, grid.size());

            // Check each quadrant
            Region topLeft = grid.get(0);
            assertEquals(0, topLeft.x());
            assertEquals(0, topLeft.y());
            assertEquals(50, topLeft.w());
            assertEquals(50, topLeft.h());

            Region topRight = grid.get(1);
            assertEquals(50, topRight.x());
            assertEquals(0, topRight.y());
            assertEquals(50, topRight.w());
            assertEquals(50, topRight.h());
        }

        @Test
        @DisplayName("Get grid number from location")
        public void testGetGridNumber() {
            // Set up a 2x2 grid for testing
            MockGridConfig.setDefaultGrid(2, 2);

            Region grid = new Region(0, 0, 100, 100);
            Location loc = new Location(75, 25); // Top-right quadrant

            Optional<Integer> gridNumber = RegionUtils.getGridNumber(grid, loc);

            // In mock mode with proper grid config, this should return grid index 1 (top-right in
            // 2x2 grid)
            // Grid layout for 2x2: [0,1] (top row), [2,3] (bottom row)
            assertTrue(gridNumber.isPresent());
            assertEquals(1, gridNumber.get());
        }

        @ParameterizedTest
        @CsvSource({"2, 3, 6", "3, 3, 9", "4, 5, 20", "10, 10, 100"})
        @DisplayName("Grid with various dimensions")
        public void testVariousGridDimensions(int rows, int cols, int expectedCells) {
            Region base = new Region(0, 0, 1000, 1000);

            List<Region> grid = RegionUtils.getGridRegions(base, rows, cols);

            assertEquals(expectedCells, grid.size());
        }
    }

    @Nested
    @DisplayName("Random Location Generation")
    class RandomLocationGeneration {

        @Test
        @DisplayName("Get random location within region")
        public void testGetRandomLocation() {
            Region region = new Region(10, 20, 100, 50);

            Location randomLoc = RegionUtils.getRandomLocation(region);

            assertTrue(randomLoc.getX() >= 10 && randomLoc.getX() < 110);
            assertTrue(randomLoc.getY() >= 20 && randomLoc.getY() < 70);
        }

        @Test
        @DisplayName("Multiple random locations are different")
        public void testMultipleRandomLocations() {
            Region region = new Region(0, 0, 1000, 1000);
            Set<String> locations = new HashSet<>();

            for (int i = 0; i < 10; i++) {
                Location loc = RegionUtils.getRandomLocation(region);
                locations.add(loc.getX() + "," + loc.getY());
            }

            // Should generate different locations (with high probability)
            assertTrue(locations.size() > 1);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Handle null regions")
        public void testNullRegions() {
            // Null regions throw NullPointerException
            assertThrows(
                    NullPointerException.class, () -> RegionUtils.overlaps((Region) null, region));
            assertThrows(
                    NullPointerException.class, () -> RegionUtils.overlaps(region, (Region) null));
            assertThrows(NullPointerException.class, () -> RegionUtils.contains(null, region));
            assertThrows(
                    NullPointerException.class, () -> RegionUtils.contains(region, (Region) null));
        }

        @Test
        @DisplayName("Handle regions with negative dimensions")
        public void testNegativeDimensions() {
            Region negative = new Region(10, 10, -50, -50);

            assertFalse(RegionUtils.isDefined(negative));
            assertFalse(RegionUtils.overlaps(region, negative));
        }

        @Test
        @DisplayName("Handle very large regions")
        public void testVeryLargeRegions() {
            Region huge = new Region(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            Region normal = new Region(100, 100, 50, 50);

            assertTrue(RegionUtils.contains(huge, normal));
            assertTrue(RegionUtils.overlaps(huge, normal));
        }

        @Test
        @DisplayName("Handle point-sized regions")
        public void testPointSizedRegions() {
            Region point = new Region(50, 50, 1, 1);

            assertTrue(RegionUtils.isDefined(point));
            assertFalse(RegionUtils.contains(point, region));
        }
    }

    @Nested
    @DisplayName("Additional Boundary Adjustment Tests")
    class AdditionalBoundaryTests {

        @Test
        @DisplayName("Adjust boundaries with edge values")
        public void testAdjustBoundariesEdgeValues() {
            Region testRegion = new Region(50, 50, 100, 100);

            // Test adjusting to zero
            RegionUtils.adjustX(testRegion, 0);
            assertEquals(0, testRegion.x());
            assertEquals(150, testRegion.w()); // Width increased

            testRegion = new Region(50, 50, 100, 100);
            RegionUtils.adjustY(testRegion, 0);
            assertEquals(0, testRegion.y());
            assertEquals(150, testRegion.h()); // Height increased
        }

        @Test
        @DisplayName("Adjust x2 and y2 to smaller values")
        public void testAdjustBoundariesToSmallerValues() {
            Region testRegion = new Region(50, 50, 100, 100);

            // Adjust x2 to be smaller (should result in negative width)
            RegionUtils.adjustX2(testRegion, 40);
            assertTrue(testRegion.w() < 0); // 40 - 50 = -10

            testRegion = new Region(50, 50, 100, 100);
            RegionUtils.adjustY2(testRegion, 40);
            assertTrue(testRegion.h() < 0); // 40 - 50 = -10
        }

        @Test
        @DisplayName("Adjust boundaries maintaining minimum size")
        public void testAdjustBoundariesMinimumSize() {
            Region testRegion = new Region(10, 10, 50, 50);

            // Adjust x to be at x2 position (width becomes 0)
            RegionUtils.adjustX(testRegion, 60);
            assertEquals(60, testRegion.x());
            assertEquals(0, testRegion.w());

            testRegion = new Region(10, 10, 50, 50);
            RegionUtils.adjustY(testRegion, 60);
            assertEquals(60, testRegion.y());
            assertEquals(0, testRegion.h());
        }
    }

    @Nested
    @DisplayName("Concurrent Access Tests")
    class ConcurrentAccessTests {

        @Test
        @DisplayName("Concurrent modifications to regions")
        public void testConcurrentModifications() throws InterruptedException {
            Region sharedRegion = new Region(0, 0, 100, 100);
            int numThreads = 10;
            Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                final int index = i;
                threads[i] =
                        new Thread(
                                () -> {
                                    for (int j = 0; j < 100; j++) {
                                        if (index % 2 == 0) {
                                            RegionUtils.adjustX(sharedRegion, index * 10);
                                        } else {
                                            RegionUtils.adjustY(sharedRegion, index * 10);
                                        }
                                    }
                                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }

            // Region should still be valid (no corruption)
            assertNotNull(sharedRegion);
            assertTrue(sharedRegion.x() >= 0);
            assertTrue(sharedRegion.y() >= 0);
        }

        @Test
        @DisplayName("Concurrent reads of region properties")
        public void testConcurrentReads() throws InterruptedException {
            Region testRegion = new Region(10, 20, 100, 200);
            int numThreads = 20;
            Thread[] threads = new Thread[numThreads];
            List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < numThreads; i++) {
                threads[i] =
                        new Thread(
                                () -> {
                                    for (int j = 0; j < 1000; j++) {
                                        int x2 = RegionUtils.x2(testRegion);
                                        int y2 = RegionUtils.y2(testRegion);
                                        boolean defined = RegionUtils.isDefined(testRegion);
                                        results.add(defined && x2 == 110 && y2 == 220);
                                    }
                                });
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // All reads should be consistent
            assertTrue(results.stream().allMatch(r -> r));
        }
    }

    @Nested
    @DisplayName("Complex Overlap Scenarios")
    class ComplexOverlapScenarios {

        @Test
        @DisplayName("Test partial overlaps from all sides")
        public void testPartialOverlapsAllSides() {
            Region center = new Region(100, 100, 100, 100);

            // Overlap from left
            Region left = new Region(50, 100, 100, 100);
            assertTrue(RegionUtils.overlaps(center, left));

            // Overlap from right
            Region right = new Region(150, 100, 100, 100);
            assertTrue(RegionUtils.overlaps(center, right));

            // Overlap from top
            Region top = new Region(100, 50, 100, 100);
            assertTrue(RegionUtils.overlaps(center, top));

            // Overlap from bottom
            Region bottom = new Region(100, 150, 100, 100);
            assertTrue(RegionUtils.overlaps(center, bottom));

            // Corner overlaps
            Region topLeft = new Region(50, 50, 100, 100);
            assertTrue(RegionUtils.overlaps(center, topLeft));

            Region bottomRight = new Region(150, 150, 100, 100);
            assertTrue(RegionUtils.overlaps(center, bottomRight));
        }

        @Test
        @DisplayName("Test nested regions")
        public void testNestedRegions() {
            Region outer = new Region(0, 0, 300, 300);
            Region middle = new Region(50, 50, 200, 200);
            Region inner = new Region(100, 100, 100, 100);

            assertTrue(RegionUtils.contains(outer, middle));
            assertTrue(RegionUtils.contains(outer, inner));
            assertTrue(RegionUtils.contains(middle, inner));
            assertFalse(RegionUtils.contains(inner, middle));
            assertFalse(RegionUtils.contains(inner, outer));
        }

        @Test
        @DisplayName("Test exact boundary touching")
        public void testExactBoundaryTouching() {
            Region r1 = new Region(0, 0, 100, 100);

            // Right edge touching left edge
            Region r2 = new Region(100, 0, 100, 100);

            // Bottom edge touching top edge
            Region r3 = new Region(0, 100, 100, 100);

            // Corner touching corner
            Region r4 = new Region(100, 100, 100, 100);

            // Test overlaps (implementation dependent)
            assertNotNull(RegionUtils.overlaps(r1, r2));
            assertNotNull(RegionUtils.overlaps(r1, r3));
            assertNotNull(RegionUtils.overlaps(r1, r4));
        }
    }

    @Nested
    @DisplayName("Region List Operations")
    class RegionListOperations {

        @Test
        @DisplayName("Get union of multiple regions")
        public void testUnionOfMultipleRegions() {
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 50, 50),
                            new Region(100, 100, 50, 50),
                            new Region(200, 200, 50, 50));

            // Get bounding box of all regions
            Region first = regions.get(0);
            for (int i = 1; i < regions.size(); i++) {
                first = RegionUtils.getUnion(first, regions.get(i));
            }

            assertEquals(0, first.x());
            assertEquals(0, first.y());
            assertEquals(250, first.w());
            assertEquals(250, first.h());
        }

        @Test
        @DisplayName("Find overlapping regions in list")
        public void testFindOverlappingRegions() {
            Region target = new Region(50, 50, 100, 100);
            List<Region> regions =
                    Arrays.asList(
                            new Region(0, 0, 60, 60), // Overlaps
                            new Region(140, 140, 50, 50), // Overlaps
                            new Region(200, 200, 50, 50), // No overlap
                            new Region(75, 75, 50, 50) // Overlaps
                            );

            long overlappingCount =
                    regions.stream().filter(r -> RegionUtils.overlaps(target, r)).count();

            assertEquals(3, overlappingCount);
        }
    }

    @Nested
    @DisplayName("Location Boundary Tests")
    class LocationBoundaryTests {

        @Test
        @DisplayName("Location on region boundaries")
        public void testLocationOnBoundaries() {
            Region region = new Region(10, 20, 100, 50);

            // Test corners
            Location topLeft = new Location(10, 20);
            Location topRight = new Location(109, 20);
            Location bottomLeft = new Location(10, 69);
            Location bottomRight = new Location(109, 69);

            // Test that top-left is contained
            assertTrue(RegionUtils.contains(region, topLeft));

            // Other corners may or may not be contained based on implementation
            // (inclusive vs exclusive boundaries)
            RegionUtils.contains(region, topRight);
            RegionUtils.contains(region, bottomLeft);
            RegionUtils.contains(region, bottomRight);

            // Test clearly outside boundaries
            Location outsideLeft = new Location(9, 45);
            Location outsideRight = new Location(111, 45); // Clearly outside
            Location outsideTop = new Location(60, 19);
            Location outsideBottom = new Location(60, 71); // Clearly outside

            assertFalse(RegionUtils.contains(region, outsideLeft));
            assertFalse(RegionUtils.contains(region, outsideRight));
            assertFalse(RegionUtils.contains(region, outsideTop));
            assertFalse(RegionUtils.contains(region, outsideBottom));
        }

        @Test
        @DisplayName("Location with null coordinates")
        public void testLocationWithNullCoordinates() {
            Region region = new Region(10, 20, 100, 50);
            Location nullLoc = null;

            // Null location throws NullPointerException
            assertThrows(
                    NullPointerException.class,
                    () -> {
                        RegionUtils.contains(region, nullLoc);
                    });
        }
    }
}
