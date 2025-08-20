package io.github.jspinak.brobot.util.geometry;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for GridBasedClusterer - spatial clustering utility.
 * Tests grid-based clustering, overlapping grids, and density detection.
 */
@DisplayName("GridBasedClusterer Tests")
public class GridBasedClustererTest extends BrobotTestBase {
    
    private GridBasedClusterer clusterer;
    private Region testRegion;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        clusterer = new GridBasedClusterer();
        testRegion = new Region(0, 0, 300, 300); // 300x300 region for 100x100 cells
    }
    
    @Nested
    @DisplayName("Basic Clustering Operations")
    class BasicClusteringOperations {
        
        @Test
        @DisplayName("Cluster single dense region")
        public void testSingleDenseCluster() {
            List<Location> points = new ArrayList<>();
            // Create dense cluster in top-left cell (0,0 to 100,100)
            for (int i = 0; i < 20; i++) {
                points.add(new Location(50 + (i % 5) * 10, 50 + (i / 5) * 10));
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 3);
            
            assertNotNull(clusters);
            assertFalse(clusters.isEmpty());
            
            // The top cluster should contain most points
            Map.Entry<Region, Integer> topCluster = clusters.entrySet().iterator().next();
            assertTrue(topCluster.getValue() >= 15); // Most points should be in top cluster
        }
        
        @Test
        @DisplayName("Cluster multiple dense regions")
        public void testMultipleDenseClusters() {
            List<Location> points = new ArrayList<>();
            
            // Create three dense clusters in different cells
            // Top-left cluster
            for (int i = 0; i < 10; i++) {
                points.add(new Location(25 + i * 5, 25 + i * 5));
            }
            
            // Center cluster
            for (int i = 0; i < 15; i++) {
                points.add(new Location(150 + (i % 5) * 5, 150 + (i / 5) * 5));
            }
            
            // Bottom-right cluster
            for (int i = 0; i < 12; i++) {
                points.add(new Location(250 + (i % 4) * 5, 250 + (i / 4) * 5));
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 3);
            
            assertNotNull(clusters);
            assertEquals(3, clusters.size());
            
            // Verify clusters are sorted by density
            List<Integer> counts = new ArrayList<>(clusters.values());
            for (int i = 0; i < counts.size() - 1; i++) {
                assertTrue(counts.get(i) >= counts.get(i + 1));
            }
        }
        
        @Test
        @DisplayName("Empty points list returns empty clusters")
        public void testEmptyPointsList() {
            List<Location> emptyPoints = new ArrayList<>();
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, emptyPoints, 3);
            
            assertNotNull(clusters);
            assertTrue(clusters.isEmpty());
        }
        
        @Test
        @DisplayName("Single point creates single cluster")
        public void testSinglePoint() {
            List<Location> points = Collections.singletonList(new Location(150, 150));
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 1);
            
            assertNotNull(clusters);
            assertEquals(1, clusters.size());
            assertEquals(1, clusters.values().iterator().next());
        }
    }
    
    @Nested
    @DisplayName("Grid Cell Distribution")
    class GridCellDistribution {
        
        @Test
        @DisplayName("Points distributed across all 9 cells")
        public void testAllCellsDistribution() {
            List<Location> points = new ArrayList<>();
            
            // Add points to each of the 9 grid cells
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int x = col * 100 + 50;
                    int y = row * 100 + 50;
                    points.add(new Location(x, y));
                }
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 9);
            
            assertNotNull(clusters);
            // Should have at most 9 regions (one per cell)
            assertTrue(clusters.size() <= 9);
            
            // Each cluster should have at least 1 point
            for (Integer count : clusters.values()) {
                assertTrue(count >= 1);
            }
        }
        
        @ParameterizedTest
        @CsvSource({
            "50, 50, 0",    // Top-left cell
            "150, 50, 1",   // Top-middle cell
            "250, 50, 2",   // Top-right cell
            "50, 150, 3",   // Middle-left cell
            "150, 150, 4",  // Center cell
            "250, 150, 5",  // Middle-right cell
            "50, 250, 6",   // Bottom-left cell
            "150, 250, 7",  // Bottom-middle cell
            "250, 250, 8"   // Bottom-right cell
        })
        @DisplayName("Points map to correct grid cells")
        public void testGridCellMapping(int x, int y, int expectedCell) {
            List<Location> points = Collections.singletonList(new Location(x, y));
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 1);
            
            assertNotNull(clusters);
            assertEquals(1, clusters.size());
            
            Region clusterRegion = clusters.keySet().iterator().next();
            // Verify the point is within the cluster region
            assertTrue(clusterRegion.contains(new Location(x, y)));
        }
    }
    
    @Nested
    @DisplayName("Overlapping Grid Analysis")
    class OverlappingGridAnalysis {
        
        @Test
        @DisplayName("Cluster on grid boundary detected by overlapping grid")
        public void testBoundaryCluster() {
            List<Location> points = new ArrayList<>();
            
            // Create cluster right on the boundary between cells (at x=100)
            for (int i = 0; i < 20; i++) {
                points.add(new Location(95 + (i % 10), 150 + (i / 10) * 5));
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 2);
            
            assertNotNull(clusters);
            assertFalse(clusters.isEmpty());
            
            // Overlapping grid should detect this boundary cluster
            Map.Entry<Region, Integer> topCluster = clusters.entrySet().iterator().next();
            assertTrue(topCluster.getValue() >= 10); // Should capture significant portion
        }
        
        @Test
        @DisplayName("Corner cluster detected by both grids")
        public void testCornerCluster() {
            List<Location> points = new ArrayList<>();
            
            // Create cluster at corner of four cells
            for (int i = 0; i < 16; i++) {
                points.add(new Location(95 + (i % 4) * 5, 95 + (i / 4) * 5));
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 4);
            
            assertNotNull(clusters);
            assertFalse(clusters.isEmpty());
            
            // At least one cluster should capture most points
            boolean foundMainCluster = false;
            for (Integer count : clusters.values()) {
                if (count >= 8) {
                    foundMainCluster = true;
                    break;
                }
            }
            assertTrue(foundMainCluster);
        }
    }
    
    @Nested
    @DisplayName("Max Clusters Limitation")
    class MaxClustersLimitation {
        
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 9})
        @DisplayName("Respect max clusters parameter")
        public void testMaxClustersLimit(int maxClusters) {
            List<Location> points = new ArrayList<>();
            
            // Create 9 separate clusters (one per cell)
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    int centerX = col * 100 + 50;
                    int centerY = row * 100 + 50;
                    // Add 5 points per cell
                    for (int p = 0; p < 5; p++) {
                        points.add(new Location(centerX + p, centerY));
                    }
                }
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, maxClusters);
            
            assertNotNull(clusters);
            assertTrue(clusters.size() <= maxClusters);
        }
        
        @Test
        @DisplayName("Zero max clusters returns empty map")
        public void testZeroMaxClusters() {
            List<Location> points = Arrays.asList(
                new Location(50, 50),
                new Location(150, 150),
                new Location(250, 250)
            );
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 0);
            
            assertNotNull(clusters);
            assertTrue(clusters.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Density Sorting")
    class DensitySorting {
        
        @Test
        @DisplayName("Clusters sorted by density descending")
        public void testDensitySorting() {
            List<Location> points = new ArrayList<>();
            
            // Create clusters with different densities
            // High density cluster (20 points)
            for (int i = 0; i < 20; i++) {
                points.add(new Location(50 + i % 5, 50 + i / 5));
            }
            
            // Medium density cluster (10 points)
            for (int i = 0; i < 10; i++) {
                points.add(new Location(150 + i % 3, 150 + i / 3));
            }
            
            // Low density cluster (5 points)
            for (int i = 0; i < 5; i++) {
                points.add(new Location(250 + i, 250));
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 3);
            
            assertNotNull(clusters);
            
            List<Integer> densities = new ArrayList<>(clusters.values());
            // Verify descending order
            for (int i = 0; i < densities.size() - 1; i++) {
                assertTrue(densities.get(i) >= densities.get(i + 1),
                    "Density at index " + i + " (" + densities.get(i) + 
                    ") should be >= density at index " + (i + 1) + " (" + densities.get(i + 1) + ")");
            }
        }
        
        @Test
        @DisplayName("Equal density clusters all included up to max")
        public void testEqualDensityClusters() {
            List<Location> points = new ArrayList<>();
            
            // Create 4 clusters with equal density (5 points each)
            for (int cluster = 0; cluster < 4; cluster++) {
                int baseX = (cluster % 2) * 150 + 50;
                int baseY = (cluster / 2) * 150 + 50;
                for (int i = 0; i < 5; i++) {
                    points.add(new Location(baseX + i, baseY));
                }
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 3);
            
            assertNotNull(clusters);
            assertEquals(3, clusters.size()); // Should return exactly max clusters
            
            // All should have same density
            Set<Integer> uniqueDensities = new HashSet<>(clusters.values());
            assertEquals(1, uniqueDensities.size()); // All densities should be equal
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Points outside region ignored")
        public void testPointsOutsideRegion() {
            List<Location> points = Arrays.asList(
                new Location(-50, 150),   // Outside left
                new Location(350, 150),   // Outside right
                new Location(150, -50),   // Outside top
                new Location(150, 350),   // Outside bottom
                new Location(150, 150)    // Inside center
            );
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 5);
            
            assertNotNull(clusters);
            // Should only count the one point inside
            int totalPoints = clusters.values().stream().mapToInt(Integer::intValue).sum();
            assertEquals(1, totalPoints);
        }
        
        @Test
        @DisplayName("Points exactly on region boundary")
        public void testBoundaryPoints() {
            List<Location> points = Arrays.asList(
                new Location(0, 0),       // Top-left corner
                new Location(299, 0),     // Top-right corner
                new Location(0, 299),     // Bottom-left corner
                new Location(299, 299),   // Bottom-right corner
                new Location(150, 0),     // Top edge
                new Location(150, 299),   // Bottom edge
                new Location(0, 150),     // Left edge
                new Location(299, 150)    // Right edge
            );
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 9);
            
            assertNotNull(clusters);
            assertFalse(clusters.isEmpty());
            
            // All boundary points should be counted
            int totalPoints = clusters.values().stream().mapToInt(Integer::intValue).sum();
            assertTrue(totalPoints >= 4); // At least some boundary points should be counted
        }
        
        @Test
        @DisplayName("Very small region")
        public void testVerySmallRegion() {
            Region tinyRegion = new Region(0, 0, 3, 3); // 3x3 pixels, 1x1 per cell
            List<Location> points = Arrays.asList(
                new Location(0, 0),
                new Location(1, 1),
                new Location(2, 2)
            );
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(tinyRegion, points, 3);
            
            assertNotNull(clusters);
            // Should handle tiny regions without error
        }
        
        @Test
        @DisplayName("Null points list throws exception")
        public void testNullPointsList() {
            assertThrows(NullPointerException.class, () -> {
                clusterer.getClusterRegions(testRegion, null, 3);
            });
        }
        
        @Test
        @DisplayName("Null region throws exception")
        public void testNullRegion() {
            List<Location> points = Arrays.asList(new Location(50, 50));
            
            assertThrows(NullPointerException.class, () -> {
                clusterer.getClusterRegions(null, points, 3);
            });
        }
    }
    
    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {
        
        @Test
        @DisplayName("Handle large number of points efficiently")
        public void testLargePointSet() {
            List<Location> points = new ArrayList<>();
            Random random = new Random(42); // Fixed seed for reproducibility
            
            // Add 10000 random points
            for (int i = 0; i < 10000; i++) {
                points.add(new Location(random.nextInt(300), random.nextInt(300)));
            }
            
            long startTime = System.currentTimeMillis();
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 5);
            long endTime = System.currentTimeMillis();
            
            assertNotNull(clusters);
            assertFalse(clusters.isEmpty());
            
            // Should complete in reasonable time (< 1 second)
            assertTrue(endTime - startTime < 1000, 
                "Clustering 10000 points took " + (endTime - startTime) + "ms");
            
            // Total points in clusters should not exceed input
            int totalInClusters = clusters.values().stream().mapToInt(Integer::intValue).sum();
            assertTrue(totalInClusters <= 10000);
        }
        
        @RepeatedTest(5)
        @DisplayName("Consistent results for same input")
        public void testConsistentResults() {
            List<Location> points = Arrays.asList(
                new Location(50, 50),
                new Location(51, 51),
                new Location(150, 150),
                new Location(151, 151),
                new Location(250, 250),
                new Location(251, 251)
            );
            
            Map<Region, Integer> clusters1 = clusterer.getClusterRegions(testRegion, points, 3);
            Map<Region, Integer> clusters2 = clusterer.getClusterRegions(testRegion, points, 3);
            
            assertEquals(clusters1.size(), clusters2.size());
            
            // Cluster counts should be identical
            List<Integer> counts1 = new ArrayList<>(clusters1.values());
            List<Integer> counts2 = new ArrayList<>(clusters2.values());
            Collections.sort(counts1);
            Collections.sort(counts2);
            assertEquals(counts1, counts2);
        }
    }
    
    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {
        
        @Test
        @DisplayName("UI button clustering")
        public void testUIButtonClustering() {
            List<Location> points = new ArrayList<>();
            
            // Simulate toolbar buttons (horizontal cluster)
            for (int i = 0; i < 8; i++) {
                points.add(new Location(20 + i * 35, 20));
            }
            
            // Simulate menu items (vertical cluster)
            for (int i = 0; i < 6; i++) {
                points.add(new Location(20, 60 + i * 30));
            }
            
            // Simulate main content area clicks (center cluster)
            for (int i = 0; i < 15; i++) {
                points.add(new Location(120 + (i % 5) * 20, 120 + (i / 5) * 20));
            }
            
            Map<Region, Integer> clusters = clusterer.getClusterRegions(testRegion, points, 3);
            
            assertNotNull(clusters);
            assertEquals(3, clusters.size());
            
            // Should identify three distinct UI areas
            List<Integer> clusterSizes = new ArrayList<>(clusters.values());
            assertTrue(clusterSizes.contains(15) || clusterSizes.get(0) >= 10); // Main content
            assertTrue(clusterSizes.stream().anyMatch(size -> size >= 6)); // At least one toolbar/menu
        }
        
        @Test
        @DisplayName("Heatmap generation from click data")
        public void testClickHeatmap() {
            List<Location> points = new ArrayList<>();
            Random random = new Random(123);
            
            // Simulate user clicks with hotspots
            // Hotspot 1: Login button area
            for (int i = 0; i < 50; i++) {
                points.add(new Location(
                    250 + random.nextInt(30),
                    20 + random.nextInt(20)
                ));
            }
            
            // Hotspot 2: Search bar
            for (int i = 0; i < 30; i++) {
                points.add(new Location(
                    100 + random.nextInt(100),
                    10 + random.nextInt(15)
                ));
            }
            
            // Random background clicks
            for (int i = 0; i < 20; i++) {
                points.add(new Location(
                    random.nextInt(300),
                    random.nextInt(300)
                ));
            }
            
            Map<Region, Integer> heatmap = clusterer.getClusterRegions(testRegion, points, 5);
            
            assertNotNull(heatmap);
            assertFalse(heatmap.isEmpty());
            
            // Top clusters should have significantly more clicks
            List<Integer> densities = new ArrayList<>(heatmap.values());
            assertTrue(densities.get(0) >= 20); // Top hotspot should have many clicks
            if (densities.size() > 1) {
                assertTrue(densities.get(0) > densities.get(densities.size() - 1)); // Clear gradient
            }
        }
    }
}