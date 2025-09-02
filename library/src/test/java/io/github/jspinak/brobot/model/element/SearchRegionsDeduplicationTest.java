package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SearchRegionsDeduplicationTest extends BrobotTestBase {
    
    @Test
    public void testRemoveDuplicateRegions() {
        SearchRegions searchRegions = new SearchRegions();
        Region r1 = new Region(0, 540, 960, 540);
        Region r2 = new Region(0, 540, 960, 540); // Duplicate
        Region r3 = new Region(0, 540, 960, 540); // Another duplicate
        
        searchRegions.addSearchRegions(r1, r2, r3);
        List<Region> regions = searchRegions.getRegions(false);
        
        // Should only have one region after deduplication
        assertEquals(1, regions.size(), "Duplicate regions should be removed");
        
        Region result = regions.get(0);
        assertEquals(0, result.x());
        assertEquals(540, result.y());
        assertEquals(960, result.w());
        assertEquals(540, result.h());
    }
    
    @Test
    public void testRemoveNestedRegions() {
        SearchRegions searchRegions = new SearchRegions();
        Region outer = new Region(0, 0, 1000, 1000);
        Region inner = new Region(100, 100, 500, 500); // Contained within outer
        
        searchRegions.addSearchRegions(outer, inner);
        List<Region> regions = searchRegions.getRegions(false);
        
        // Inner region should be removed as it's contained within outer
        assertEquals(1, regions.size(), "Nested region should be removed");
        
        Region result = regions.get(0);
        assertEquals(0, result.x());
        assertEquals(0, result.y());
        assertEquals(1000, result.w());
        assertEquals(1000, result.h());
    }
    
    @Test
    public void testPreserveDifferentRegions() {
        SearchRegions searchRegions = new SearchRegions();
        Region r1 = new Region(0, 0, 100, 100);
        Region r2 = new Region(200, 200, 100, 100);
        Region r3 = new Region(400, 400, 100, 100);
        
        searchRegions.addSearchRegions(r1, r2, r3);
        List<Region> regions = searchRegions.getRegions(false);
        
        // All three regions should be preserved as they don't overlap or contain each other
        assertEquals(3, regions.size(), "Different regions should all be preserved");
    }
    
    @Test
    public void testMixedDuplicatesAndNested() {
        SearchRegions searchRegions = new SearchRegions();
        Region outer = new Region(0, 0, 1000, 1000);
        Region duplicate1 = new Region(0, 0, 1000, 1000); // Duplicate of outer
        Region inner = new Region(100, 100, 500, 500); // Contained within outer
        Region separate = new Region(1100, 1100, 200, 200); // Separate region
        
        searchRegions.addSearchRegions(outer, duplicate1, inner, separate);
        List<Region> regions = searchRegions.getRegions(false);
        
        // Should have outer (no duplicates) and separate (not contained)
        assertEquals(2, regions.size(), "Should have only unique, non-nested regions");
        
        // Verify we have the expected regions
        boolean hasOuter = false;
        boolean hasSeparate = false;
        for (Region r : regions) {
            if (r.x() == 0 && r.y() == 0 && r.w() == 1000 && r.h() == 1000) {
                hasOuter = true;
            }
            if (r.x() == 1100 && r.y() == 1100 && r.w() == 200 && r.h() == 200) {
                hasSeparate = true;
            }
        }
        
        assertTrue(hasOuter, "Should have outer region");
        assertTrue(hasSeparate, "Should have separate region");
    }
    
    @Test
    public void testEmptyRegionsList() {
        SearchRegions searchRegions = new SearchRegions();
        List<Region> regions = searchRegions.getRegions(false);
        
        assertTrue(regions.isEmpty(), "Empty SearchRegions should return empty list");
    }
}