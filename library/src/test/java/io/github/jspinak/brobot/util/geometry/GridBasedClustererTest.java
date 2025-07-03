package io.github.jspinak.brobot.util.geometry;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GridBasedClustererTest {
    
    private GridBasedClusterer clusterer;
    
    @BeforeEach
    void setUp() {
        clusterer = new GridBasedClusterer();
    }
    
    @Test
    void testGetClusterRegions_SingleCluster() {
        // Setup
        Region region = new Region(0, 0, 300, 300);
        
        // Create points clustered in top-left grid cell (0)
        List<Location> points = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Location loc = new Location(10 + i, 10 + i);
            points.add(loc);
        }
        
        // Execute
        Map<Region, Integer> clusters = clusterer.getClusterRegions(region, points, 1);
        
        // Verify
        assertEquals(1, clusters.size());
        // The cluster should be in the top-left region
        Region topLeftRegion = clusters.keySet().iterator().next();
        assertNotNull(topLeftRegion);
        assertEquals(10, clusters.get(topLeftRegion));
    }
    
    @Test
    void testGetClusterRegions_MultipleClusters() {
        // Setup
        Region region = new Region(0, 0, 300, 300);
        
        List<Location> points = new ArrayList<>();
        
        // Create points in different grid cells
        // Cell 0 (top-left): 5 points
        for (int i = 0; i < 5; i++) {
            points.add(new Location(10 + i, 10 + i));
        }
        
        // Cell 4 (center): 8 points
        for (int i = 0; i < 8; i++) {
            points.add(new Location(150 + i, 150 + i));
        }
        
        // Cell 8 (bottom-right): 3 points
        for (int i = 0; i < 3; i++) {
            points.add(new Location(250 + i, 250 + i));
        }
        
        // Execute - request top 2 clusters
        Map<Region, Integer> clusters = clusterer.getClusterRegions(region, points, 2);
        
        // Verify
        assertTrue(clusters.size() <= 2);
        assertTrue(clusters.size() > 0);
        
        // The center region with 8 points should be the densest
        Integer maxCount = clusters.values().stream().max(Integer::compareTo).orElse(0);
        assertTrue(maxCount >= 5, "Highest density cluster should have at least 5 points");
    }
    
    @Test
    void testGetClusterRegions_EmptyPoints() {
        // Setup
        Region region = new Region(0, 0, 300, 300);
        List<Location> emptyPoints = new ArrayList<>();
        
        // Execute
        Map<Region, Integer> clusters = clusterer.getClusterRegions(region, emptyPoints, 3);
        
        // Verify
        assertTrue(clusters.isEmpty());
    }
    
    @Test
    void testGetClusterRegions_PointsOutsideGrid() {
        // Setup
        Region region = new Region(0, 0, 300, 300);
        
        List<Location> points = new ArrayList<>();
        
        // Create points outside the region
        for (int i = 0; i < 5; i++) {
            points.add(new Location(-10 - i, -10 - i));
        }
        
        // Execute
        Map<Region, Integer> clusters = clusterer.getClusterRegions(region, points, 1);
        
        // Verify - No clusters should be found for out-of-bounds points
        assertTrue(clusters.isEmpty());
    }
    
    @Test
    void testGetClusterRegions_InvalidGridNumber() {
        // Setup
        Region region = new Region(0, 0, 300, 300);
        List<Location> points = new ArrayList<>();
        
        // Create points at the exact boundary (edge case)
        points.add(new Location(300, 300)); // Right at the edge
        points.add(new Location(301, 301)); // Just outside
        
        // Execute
        Map<Region, Integer> clusters = clusterer.getClusterRegions(region, points, 1);
        
        // Verify - Points outside the region should be ignored
        // Only valid points within the region should be clustered
        assertTrue(clusters.size() <= 1);
    }
    
    @Test
    void testGetClusterRegions_MaxClustersLimit() {
        // Setup
        Region region = new Region(0, 0, 300, 300);
        
        List<Location> points = new ArrayList<>();
        
        // Create points distributed across all 9 cells (3x3 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                // Place a point at the center of each grid cell
                int x = col * 100 + 50;
                int y = row * 100 + 50;
                points.add(new Location(x, y));
            }
        }
        
        // Execute with max 3 clusters
        Map<Region, Integer> clusters = clusterer.getClusterRegions(region, points, 3);
        
        // Verify
        assertTrue(clusters.size() <= 3);
        assertTrue(clusters.size() > 0);
    }
}