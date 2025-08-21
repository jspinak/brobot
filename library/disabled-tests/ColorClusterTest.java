package io.github.jspinak.brobot.model.analysis.color;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ColorCluster.
 * Tests k-means clustering for color analysis.
 */
@DisplayName("ColorCluster Tests")
public class ColorClusterTest extends BrobotTestBase {
    
    private ColorCluster colorCluster;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        colorCluster = new ColorCluster();
    }
    
    @Nested
    @DisplayName("Cluster Initialization")
    class ClusterInitialization {
        
        @Test
        @DisplayName("Should create empty cluster")
        void shouldCreateEmptyCluster() {
            assertNotNull(colorCluster);
            assertEquals(0, colorCluster.getSize());
            assertTrue(colorCluster.getColors().isEmpty());
        }
        
        @Test
        @DisplayName("Should initialize with k value")
        void shouldInitializeWithK() {
            ColorCluster cluster = new ColorCluster(5);
            
            assertNotNull(cluster);
            assertEquals(5, cluster.getK());
        }
        
        @Test
        @DisplayName("Should initialize with colors")
        void shouldInitializeWithColors() {
            List<ColorInfo> colors = createSampleColors();
            ColorCluster cluster = new ColorCluster(colors);
            
            assertEquals(colors.size(), cluster.getSize());
            assertEquals(colors, cluster.getColors());
        }
    }
    
    @Nested
    @DisplayName("Color Management")
    class ColorManagement {
        
        @Test
        @DisplayName("Should add color to cluster")
        void shouldAddColorToCluster() {
            ColorInfo color = createColorInfo(100, 150, 200);
            
            colorCluster.addColor(color);
            
            assertEquals(1, colorCluster.getSize());
            assertTrue(colorCluster.contains(color));
        }
        
        @Test
        @DisplayName("Should add multiple colors")
        void shouldAddMultipleColors() {
            List<ColorInfo> colors = createSampleColors();
            
            for (ColorInfo color : colors) {
                colorCluster.addColor(color);
            }
            
            assertEquals(colors.size(), colorCluster.getSize());
            for (ColorInfo color : colors) {
                assertTrue(colorCluster.contains(color));
            }
        }
        
        @Test
        @DisplayName("Should remove color from cluster")
        void shouldRemoveColorFromCluster() {
            ColorInfo color = createColorInfo(100, 150, 200);
            colorCluster.addColor(color);
            
            boolean removed = colorCluster.removeColor(color);
            
            assertTrue(removed);
            assertEquals(0, colorCluster.getSize());
            assertFalse(colorCluster.contains(color));
        }
        
        @Test
        @DisplayName("Should clear all colors")
        void shouldClearAllColors() {
            List<ColorInfo> colors = createSampleColors();
            colors.forEach(colorCluster::addColor);
            
            colorCluster.clear();
            
            assertEquals(0, colorCluster.getSize());
            assertTrue(colorCluster.getColors().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Centroid Calculation")
    class CentroidCalculation {
        
        @Test
        @DisplayName("Should calculate centroid of cluster")
        void shouldCalculateCentroid() {
            colorCluster.addColor(createColorInfo(100, 100, 100));
            colorCluster.addColor(createColorInfo(200, 200, 200));
            
            ColorInfo centroid = colorCluster.calculateCentroid();
            
            assertNotNull(centroid);
            assertEquals(150, centroid.getRed());
            assertEquals(150, centroid.getGreen());
            assertEquals(150, centroid.getBlue());
        }
        
        @Test
        @DisplayName("Should handle empty cluster centroid")
        void shouldHandleEmptyClusterCentroid() {
            ColorInfo centroid = colorCluster.calculateCentroid();
            
            assertNotNull(centroid);
            // Empty cluster returns default centroid
            assertEquals(0, centroid.getRed());
            assertEquals(0, centroid.getGreen());
            assertEquals(0, centroid.getBlue());
        }
        
        @Test
        @DisplayName("Should update centroid")
        void shouldUpdateCentroid() {
            colorCluster.addColor(createColorInfo(100, 100, 100));
            colorCluster.addColor(createColorInfo(200, 200, 200));
            
            colorCluster.updateCentroid();
            ColorInfo centroid = colorCluster.getCentroid();
            
            assertNotNull(centroid);
            assertEquals(150, centroid.getRed());
        }
        
        @Test
        @DisplayName("Should recalculate centroid after changes")
        void shouldRecalculateCentroidAfterChanges() {
            colorCluster.addColor(createColorInfo(100, 100, 100));
            colorCluster.updateCentroid();
            ColorInfo firstCentroid = colorCluster.getCentroid();
            
            colorCluster.addColor(createColorInfo(200, 200, 200));
            colorCluster.updateCentroid();
            ColorInfo secondCentroid = colorCluster.getCentroid();
            
            assertNotEquals(firstCentroid, secondCentroid);
        }
    }
    
    @Nested
    @DisplayName("Distance Calculations")
    class DistanceCalculations {
        
        @Test
        @DisplayName("Should calculate distance to color")
        void shouldCalculateDistanceToColor() {
            colorCluster.setCentroid(createColorInfo(128, 128, 128));
            ColorInfo testColor = createColorInfo(100, 100, 100);
            
            double distance = colorCluster.distanceTo(testColor);
            
            assertTrue(distance > 0);
            assertTrue(distance < 100); // Reasonable distance for similar colors
        }
        
        @Test
        @DisplayName("Should find nearest color in cluster")
        void shouldFindNearestColorInCluster() {
            colorCluster.addColor(createColorInfo(100, 100, 100));
            colorCluster.addColor(createColorInfo(200, 200, 200));
            colorCluster.addColor(createColorInfo(150, 150, 150));
            
            ColorInfo target = createColorInfo(140, 140, 140);
            ColorInfo nearest = colorCluster.findNearest(target);
            
            assertNotNull(nearest);
            assertEquals(150, nearest.getRed());
        }
        
        @Test
        @DisplayName("Should calculate average distance")
        void shouldCalculateAverageDistance() {
            colorCluster.addColor(createColorInfo(100, 100, 100));
            colorCluster.addColor(createColorInfo(110, 110, 110));
            colorCluster.addColor(createColorInfo(120, 120, 120));
            colorCluster.updateCentroid();
            
            double avgDistance = colorCluster.getAverageDistance();
            
            assertTrue(avgDistance >= 0);
            assertTrue(avgDistance < 50); // Small cluster, small average distance
        }
    }
    
    @Nested
    @DisplayName("Cluster Statistics")
    class ClusterStatistics {
        
        @Test
        @DisplayName("Should calculate variance")
        void shouldCalculateVariance() {
            colorCluster.addColor(createColorInfo(100, 100, 100));
            colorCluster.addColor(createColorInfo(200, 200, 200));
            colorCluster.updateCentroid();
            
            double variance = colorCluster.getVariance();
            
            assertTrue(variance > 0);
        }
        
        @Test
        @DisplayName("Should calculate standard deviation")
        void shouldCalculateStandardDeviation() {
            colorCluster.addColor(createColorInfo(100, 100, 100));
            colorCluster.addColor(createColorInfo(200, 200, 200));
            colorCluster.updateCentroid();
            
            double stdDev = colorCluster.getStandardDeviation();
            
            assertTrue(stdDev > 0);
            assertEquals(Math.sqrt(colorCluster.getVariance()), stdDev, 0.001);
        }
        
        @Test
        @DisplayName("Should identify dominant color")
        void shouldIdentifyDominantColor() {
            ColorInfo red = createColorInfo(255, 0, 0);
            red.setFrequency(100);
            ColorInfo blue = createColorInfo(0, 0, 255);
            blue.setFrequency(50);
            
            colorCluster.addColor(red);
            colorCluster.addColor(blue);
            
            ColorInfo dominant = colorCluster.getDominantColor();
            
            assertEquals(red, dominant);
        }
        
        @Test
        @DisplayName("Should calculate total frequency")
        void shouldCalculateTotalFrequency() {
            ColorInfo c1 = createColorInfo(100, 100, 100);
            c1.setFrequency(50);
            ColorInfo c2 = createColorInfo(200, 200, 200);
            c2.setFrequency(30);
            
            colorCluster.addColor(c1);
            colorCluster.addColor(c2);
            
            int totalFreq = colorCluster.getTotalFrequency();
            
            assertEquals(80, totalFreq);
        }
    }
    
    @Nested
    @DisplayName("K-Means Operations")
    class KMeansOperations {
        
        @Test
        @DisplayName("Should split cluster")
        void shouldSplitCluster() {
            // Add colors that can be split into two groups
            colorCluster.addColor(createColorInfo(50, 50, 50));
            colorCluster.addColor(createColorInfo(60, 60, 60));
            colorCluster.addColor(createColorInfo(200, 200, 200));
            colorCluster.addColor(createColorInfo(210, 210, 210));
            
            List<ColorCluster> splits = colorCluster.split(2);
            
            assertEquals(2, splits.size());
            assertTrue(splits.get(0).getSize() > 0);
            assertTrue(splits.get(1).getSize() > 0);
        }
        
        @Test
        @DisplayName("Should merge clusters")
        void shouldMergeClusters() {
            ColorCluster cluster1 = new ColorCluster();
            cluster1.addColor(createColorInfo(100, 100, 100));
            cluster1.addColor(createColorInfo(110, 110, 110));
            
            ColorCluster cluster2 = new ColorCluster();
            cluster2.addColor(createColorInfo(200, 200, 200));
            cluster2.addColor(createColorInfo(210, 210, 210));
            
            ColorCluster merged = ColorCluster.merge(cluster1, cluster2);
            
            assertEquals(4, merged.getSize());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {2, 3, 5, 10})
        @DisplayName("Should handle different k values")
        void shouldHandleDifferentKValues(int k) {
            // Add sufficient colors for k-means
            for (int i = 0; i < k * 3; i++) {
                colorCluster.addColor(createColorInfo(
                    (i * 20) % 256,
                    (i * 30) % 256,
                    (i * 40) % 256
                ));
            }
            
            colorCluster.setK(k);
            List<ColorCluster> clusters = colorCluster.performKMeans();
            
            assertTrue(clusters.size() <= k);
            assertTrue(clusters.size() > 0);
        }
    }
    
    @Nested
    @DisplayName("Cluster Comparison")
    class ClusterComparison {
        
        @Test
        @DisplayName("Should compare clusters by size")
        void shouldCompareClustersBySize() {
            ColorCluster smaller = new ColorCluster();
            smaller.addColor(createColorInfo(100, 100, 100));
            
            ColorCluster larger = new ColorCluster();
            larger.addColor(createColorInfo(100, 100, 100));
            larger.addColor(createColorInfo(200, 200, 200));
            
            assertTrue(smaller.compareTo(larger) < 0);
            assertTrue(larger.compareTo(smaller) > 0);
        }
        
        @Test
        @DisplayName("Should identify similar clusters")
        void shouldIdentifySimilarClusters() {
            ColorCluster cluster1 = new ColorCluster();
            cluster1.addColor(createColorInfo(100, 100, 100));
            cluster1.addColor(createColorInfo(110, 110, 110));
            cluster1.updateCentroid();
            
            ColorCluster cluster2 = new ColorCluster();
            cluster2.addColor(createColorInfo(105, 105, 105));
            cluster2.addColor(createColorInfo(115, 115, 115));
            cluster2.updateCentroid();
            
            assertTrue(cluster1.isSimilarTo(cluster2, 20.0));
        }
        
        @Test
        @DisplayName("Should calculate cluster overlap")
        void shouldCalculateClusterOverlap() {
            ColorInfo shared = createColorInfo(100, 100, 100);
            
            ColorCluster cluster1 = new ColorCluster();
            cluster1.addColor(shared);
            cluster1.addColor(createColorInfo(110, 110, 110));
            
            ColorCluster cluster2 = new ColorCluster();
            cluster2.addColor(shared);
            cluster2.addColor(createColorInfo(200, 200, 200));
            
            double overlap = cluster1.overlapWith(cluster2);
            
            assertTrue(overlap > 0);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle single color cluster")
        void shouldHandleSingleColorCluster() {
            ColorInfo single = createColorInfo(128, 128, 128);
            colorCluster.addColor(single);
            
            colorCluster.updateCentroid();
            
            assertEquals(single, colorCluster.getCentroid());
            assertEquals(0.0, colorCluster.getVariance(), 0.001);
        }
        
        @Test
        @DisplayName("Should handle identical colors")
        void shouldHandleIdenticalColors() {
            ColorInfo color = createColorInfo(100, 100, 100);
            
            for (int i = 0; i < 5; i++) {
                colorCluster.addColor(createColorInfo(100, 100, 100));
            }
            
            colorCluster.updateCentroid();
            
            assertEquals(100, colorCluster.getCentroid().getRed());
            assertEquals(0.0, colorCluster.getVariance(), 0.001);
        }
        
        @Test
        @DisplayName("Should handle large clusters")
        void shouldHandleLargeClusters() {
            for (int i = 0; i < 1000; i++) {
                colorCluster.addColor(createColorInfo(
                    i % 256,
                    (i * 2) % 256,
                    (i * 3) % 256
                ));
            }
            
            assertEquals(1000, colorCluster.getSize());
            assertDoesNotThrow(() -> colorCluster.updateCentroid());
            assertDoesNotThrow(() -> colorCluster.getVariance());
        }
    }
    
    // Helper methods
    private ColorInfo createColorInfo(int r, int g, int b) {
        ColorInfo color = new ColorInfo();
        color.setRed(r);
        color.setGreen(g);
        color.setBlue(b);
        return color;
    }
    
    private List<ColorInfo> createSampleColors() {
        List<ColorInfo> colors = new ArrayList<>();
        colors.add(createColorInfo(255, 0, 0));    // Red
        colors.add(createColorInfo(0, 255, 0));    // Green
        colors.add(createColorInfo(0, 0, 255));    // Blue
        colors.add(createColorInfo(255, 255, 0));  // Yellow
        colors.add(createColorInfo(255, 0, 255));  // Magenta
        return colors;
    }
}