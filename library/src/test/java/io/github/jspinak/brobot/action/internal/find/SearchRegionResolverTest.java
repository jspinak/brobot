package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchRegionResolverTest {

    private SearchRegionResolver searchRegionResolver;
    
    @BeforeEach
    void setUp() {
        searchRegionResolver = new SearchRegionResolver();
    }
    
    @Test
    void testGetRegions_WithStateImage_DefinedFixedRegions() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        StateImage stateImage = new StateImage();
        
        // Add patterns with fixed regions
        Pattern pattern1 = new Pattern();
        pattern1.getSearchRegions().setFixedRegion(new Region(10, 10, 100, 100));
        Pattern pattern2 = new Pattern();
        pattern2.getSearchRegions().setFixedRegion(new Region(200, 200, 50, 50));
        stateImage.addPatterns(pattern1);
        stateImage.addPatterns(pattern2);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(findOptions, stateImage);
        
        // Verify - defined/fixed regions have highest priority
        assertEquals(2, regions.size());
    }
    
    @Test
    void testGetRegions_WithStateImage_ActionOptionsRegions() {
        // Setup - PatternFindOptions does have getSearchRegions()
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.addSearchRegions(new Region(15, 15, 85, 85));
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSearchRegions(searchRegions)
            .build();
        
        StateImage stateImage = new StateImage();
        Pattern pattern = new Pattern();
        // Don't set any search regions on the pattern
        stateImage.addPatterns(pattern);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(findOptions, stateImage);
        
        // Verify - should use action options regions when no fixed regions on patterns
        assertEquals(1, regions.size());
        assertEquals(new Region(15, 15, 85, 85), regions.get(0));
    }
    
    @Test
    void testGetRegions_WithStateImage_ImageRegionsOnly() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        StateImage stateImage = new StateImage();
        
        Pattern pattern1 = new Pattern();
        pattern1.getSearchRegions().addSearchRegions(new Region(0, 0, 100, 100));
        Pattern pattern2 = new Pattern();
        pattern2.getSearchRegions().addSearchRegions(new Region(100, 100, 200, 200));
        stateImage.addPatterns(pattern1);
        stateImage.addPatterns(pattern2);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(findOptions, stateImage);
        
        // Verify
        assertEquals(2, regions.size());
    }
    
    @Test
    void testGetRegions_WithStateImage_DefaultFullScreen() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        StateImage stateImage = new StateImage();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(findOptions, stateImage);
        
        // Verify - should return default full-screen region
        assertEquals(1, regions.size());
        Region defaultRegion = regions.get(0);
        assertEquals(0, defaultRegion.getX());
        assertEquals(0, defaultRegion.getY());
    }
    
    @Test
    void testGetRegions_WithPattern_FixedRegion() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        Pattern pattern = new Pattern();
        pattern.setFixed(true); // Must set fixed=true for fixed region to be used
        
        Region fixedRegion = new Region(25, 25, 75, 75);
        pattern.getSearchRegions().setFixedRegion(fixedRegion);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(findOptions, pattern);
        
        // Verify - fixed region takes precedence
        assertEquals(1, regions.size());
        assertEquals(fixedRegion, regions.get(0));
    }
    
    @Test
    void testGetRegions_WithPattern_ActionOptionsRegions() {
        // Setup - PatternFindOptions does have getSearchRegions()
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.addSearchRegions(new Region(5, 5, 95, 95));
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSearchRegions(searchRegions)
            .build();
        
        Pattern pattern = new Pattern();
        // Don't set any search regions on the pattern itself
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(findOptions, pattern);
        
        // Verify - should use action options regions when pattern has no regions
        assertEquals(1, regions.size());
        assertEquals(new Region(5, 5, 95, 95), regions.get(0));
    }
    
    @Test
    void testGetRegions_WithPattern_PatternRegionsOnly() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        Pattern pattern = new Pattern();
        
        Region patternRegion = new Region(30, 30, 70, 70);
        pattern.getSearchRegions().addSearchRegions(patternRegion);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(findOptions, pattern);
        
        // Verify
        assertEquals(1, regions.size());
        assertEquals(patternRegion, regions.get(0));
    }
    
    @Test
    void testGetRegions_WithPattern_DefaultFullScreen() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        Pattern pattern = new Pattern();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(findOptions, pattern);
        
        // Verify - should return default full-screen region
        assertEquals(1, regions.size());
        Region defaultRegion = regions.get(0);
        assertEquals(0, defaultRegion.getX());
        assertEquals(0, defaultRegion.getY());
    }
    
    @Test
    void testGetRegionsForAllImages_WithObjectCollection_MultipleImages() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        
        StateImage image1 = new StateImage();
        Pattern pattern1 = new Pattern();
        pattern1.getSearchRegions().addSearchRegions(new Region(0, 0, 50, 50));
        image1.addPatterns(pattern1);
        
        StateImage image2 = new StateImage();
        Pattern pattern2 = new Pattern();
        pattern2.getSearchRegions().addSearchRegions(new Region(50, 50, 100, 100));
        image2.addPatterns(pattern2);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(image1, image2)
                .build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages(findOptions, collection);
        
        // Verify - should combine regions from all images
        assertEquals(2, regions.size());
    }
    
    @Test
    void testGetRegionsForAllImages_WithObjectCollection_DirectRegions() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        
        Region region1 = new Region(10, 10, 30, 30);
        Region region2 = new Region(40, 40, 60, 60);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(region1, region2)
                .build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages(findOptions, collection);
        
        // Verify - should return the direct regions
        assertEquals(2, regions.size());
        assertTrue(regions.contains(region1));
        assertTrue(regions.contains(region2));
    }
    
    @Test
    void testGetRegionsForAllImages_WithObjectCollection_Empty() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages(findOptions, emptyCollection);
        
        // Verify - should return default full-screen region
        assertEquals(1, regions.size());
    }
    
    @Test
    void testGetRegions_WithActionConfig_NotPatternFindOptions() {
        // Setup
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        StateImage stateImage = new StateImage();
        
        Pattern pattern = new Pattern();
        pattern.getSearchRegions().addSearchRegions(new Region(20, 20, 80, 80));
        stateImage.addPatterns(pattern);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(clickOptions, stateImage);
        
        // Verify - should still work with other ActionConfig types
        assertEquals(1, regions.size());
    }
    
    @Test
    void testGetRegionsForAllImages_WithMultipleObjectCollections() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        
        ObjectCollection collection1 = new ObjectCollection.Builder()
                .withRegions(new Region(0, 0, 25, 25))
                .build();
                
        ObjectCollection collection2 = new ObjectCollection.Builder()
                .withRegions(new Region(25, 25, 50, 50))
                .build();
        
        // Execute - use varargs
        List<Region> regions = searchRegionResolver.getRegionsForAllImages(findOptions, collection1, collection2);
        
        // Verify - should combine regions from all collections
        assertEquals(2, regions.size());
    }
}