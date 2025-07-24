package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
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
        ActionOptions actionOptions = new ActionOptions();
        StateImage stateImage = new StateImage();
        
        // Add patterns with fixed regions
        Pattern pattern1 = new Pattern();
        pattern1.getSearchRegions().setFixedRegion(new Region(10, 10, 100, 100));
        Pattern pattern2 = new Pattern();
        pattern2.getSearchRegions().setFixedRegion(new Region(200, 200, 50, 50));
        stateImage.addPatterns(pattern1);
        stateImage.addPatterns(pattern2);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions, stateImage);
        
        // Verify - defined/fixed regions have highest priority
        assertEquals(2, regions.size());
    }
    
    @Test
    void testGetRegions_WithStateImage_ActionOptionsRegions() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        Region actionRegion = new Region(50, 50, 150, 150);
        actionOptions.getSearchRegions().addSearchRegions(actionRegion);
        
        StateImage stateImage = new StateImage();
        Pattern pattern = new Pattern();
        pattern.getSearchRegions().addSearchRegions(new Region(0, 0, 200, 200));
        stateImage.addPatterns(pattern);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions, stateImage);
        
        // Verify - action options regions take precedence over image regions
        assertEquals(1, regions.size());
        assertEquals(actionRegion, regions.get(0));
    }
    
    @Test
    void testGetRegions_WithStateImage_ImageRegionsOnly() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        StateImage stateImage = new StateImage();
        
        Pattern pattern1 = new Pattern();
        pattern1.getSearchRegions().addSearchRegions(new Region(0, 0, 100, 100));
        Pattern pattern2 = new Pattern();
        pattern2.getSearchRegions().addSearchRegions(new Region(100, 100, 200, 200));
        stateImage.addPatterns(pattern1);
        stateImage.addPatterns(pattern2);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions, stateImage);
        
        // Verify
        assertEquals(2, regions.size());
    }
    
    @Test
    void testGetRegions_WithStateImage_DefaultFullScreen() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        StateImage stateImage = new StateImage();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions, stateImage);
        
        // Verify - should return default full-screen region
        assertEquals(1, regions.size());
        Region defaultRegion = regions.get(0);
        assertEquals(0, defaultRegion.getX());
        assertEquals(0, defaultRegion.getY());
    }
    
    @Test
    void testGetRegions_WithPattern_FixedRegion() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        Pattern pattern = new Pattern();
        pattern.setFixed(true); // Must set fixed=true for fixed region to be used
        
        Region fixedRegion = new Region(25, 25, 75, 75);
        pattern.getSearchRegions().setFixedRegion(fixedRegion);
        
        Region actionRegion = new Region(0, 0, 50, 50);
        actionOptions.getSearchRegions().addSearchRegions(actionRegion);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions, pattern);
        
        // Verify - fixed region takes precedence
        assertEquals(1, regions.size());
        assertEquals(fixedRegion, regions.get(0));
    }
    
    @Test
    void testGetRegions_WithPattern_ActionOptionsRegions() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        Region actionRegion = new Region(10, 10, 40, 40);
        actionOptions.getSearchRegions().addSearchRegions(actionRegion);
        
        Pattern pattern = new Pattern();
        Region patternRegion = new Region(20, 20, 60, 60);
        pattern.getSearchRegions().addSearchRegions(patternRegion);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions, pattern);
        
        // Verify - action options regions override pattern regions (when no fixed region)
        assertEquals(1, regions.size());
        assertEquals(actionRegion, regions.get(0));
    }
    
    @Test
    void testGetRegions_WithPattern_PatternRegionsOnly() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        Pattern pattern = new Pattern();
        
        Region patternRegion1 = new Region(0, 0, 100, 100);
        Region patternRegion2 = new Region(100, 0, 100, 100);
        pattern.getSearchRegions().addSearchRegions(patternRegion1);
        pattern.getSearchRegions().addSearchRegions(patternRegion2);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions, pattern);
        
        // Verify - regions might get merged if adjacent, so check that we have at least 1
        assertTrue(regions.size() >= 1);
        // The two regions are adjacent, so they might be merged into one large region
        if (regions.size() == 1) {
            // If merged, should be one region covering both original regions
            Region merged = regions.get(0);
            assertEquals(0, merged.getX());
            assertEquals(0, merged.getY());
            assertEquals(200, merged.getW()); // Combined width
            assertEquals(100, merged.getH());
        } else {
            // If not merged, should have both regions
            assertEquals(2, regions.size());
        }
    }
    
    @Test
    void testGetRegions_WithPattern_DefaultFullScreen() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        Pattern pattern = new Pattern();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions, pattern);
        
        // Verify
        assertEquals(1, regions.size());
        assertNotNull(regions.get(0));
    }
    
    @Test
    void testGetRegions_ActionOptionsOnly_WithRegions() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        Region region1 = new Region(0, 0, 50, 50);
        Region region2 = new Region(50, 50, 100, 100);
        actionOptions.getSearchRegions().addSearchRegions(region1);
        actionOptions.getSearchRegions().addSearchRegions(region2);
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions);
        
        // Verify
        assertTrue(regions.size() >= 2);
    }
    
    @Test
    void testGetRegions_ActionOptionsOnly_Empty() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegions(actionOptions);
        
        // Verify - should return default full-screen region
        assertEquals(1, regions.size());
        assertNotNull(regions.get(0));
    }
    
    @Test
    void testGetRegionsForAllImages_ActionOptions_WithDefinedRegions() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        Region actionRegion = new Region(10, 10, 90, 90);
        actionOptions.getSearchRegions().addSearchRegions(actionRegion);
        
        StateImage image1 = new StateImage();
        Pattern p1 = new Pattern();
        p1.getSearchRegions().addSearchRegions(new Region(0, 0, 50, 50));
        image1.addPatterns(p1);
        
        StateImage image2 = new StateImage();
        Pattern p2 = new Pattern();
        p2.getSearchRegions().addSearchRegions(new Region(50, 50, 100, 100));
        image2.addPatterns(p2);
        
        ObjectCollection collection1 = new ObjectCollection.Builder()
                .withImages(image1)
                .build();
        ObjectCollection collection2 = new ObjectCollection.Builder()
                .withImages(image2)
                .build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages(actionOptions, collection1, collection2);
        
        // Verify - action options regions override all image regions
        assertEquals(1, regions.size());
        assertEquals(actionRegion, regions.get(0));
    }
    
    @Test
    void testGetRegionsForAllImages_ActionOptions_NoDefinedRegions() {
        // Setup
        ActionOptions actionOptions = new ActionOptions();
        
        StateImage image1 = new StateImage();
        Pattern p1 = new Pattern();
        p1.getSearchRegions().addSearchRegions(new Region(0, 0, 50, 50));
        image1.addPatterns(p1);
        
        StateImage image2 = new StateImage();
        Pattern p2 = new Pattern();
        p2.getSearchRegions().addSearchRegions(new Region(50, 50, 100, 100));
        p2.getSearchRegions().addSearchRegions(new Region(100, 100, 150, 150));
        image2.addPatterns(p2);
        
        ObjectCollection collection1 = new ObjectCollection.Builder()
                .withImages(image1)
                .build();
        ObjectCollection collection2 = new ObjectCollection.Builder()
                .withImages(image2)
                .build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages(actionOptions, collection1, collection2);
        
        // Verify - should return all regions from all images
        assertTrue(regions.size() >= 3);
    }
    
    @Test
    void testGetRegionsForAllImages_ActionConfig_WithFindOptions() {
        // Setup
        SearchRegions searchRegions = new SearchRegions();
        Region findRegion = new Region(15, 15, 85, 85);
        searchRegions.addSearchRegions(findRegion);
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSearchRegions(searchRegions)
                .build();
        
        StateImage image = new StateImage();
        Pattern p = new Pattern();
        p.getSearchRegions().addSearchRegions(new Region(0, 0, 100, 100));
        image.addPatterns(p);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages((ActionConfig) findOptions, collection);
        
        // Verify - find options regions take precedence
        assertEquals(1, regions.size());
        assertEquals(findRegion, regions.get(0));
    }
    
    @Test
    void testGetRegionsForAllImages_ActionConfig_WithColorFindOptions() {
        // Setup
        SearchRegions searchRegions = new SearchRegions();
        Region colorRegion = new Region(20, 20, 80, 80);
        searchRegions.addSearchRegions(colorRegion);
        ColorFindOptions colorOptions = new ColorFindOptions.Builder()
                .setSearchRegions(searchRegions)
                .build();
        
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages((ActionConfig) colorOptions, collection);
        
        // Verify
        assertEquals(1, regions.size());
        assertEquals(colorRegion, regions.get(0));
    }
    
    @Test
    void testGetRegionsForAllImages_ActionConfig_NonFindConfig() {
        // Setup
        ClickOptions clickOptions = new ClickOptions.Builder()
                .build();
        
        Region imageRegion = new Region(5, 5, 95, 95);
        StateImage image = new StateImage();
        Pattern p = new Pattern();
        p.getSearchRegions().addSearchRegions(imageRegion);
        image.addPatterns(p);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages((ActionConfig) clickOptions, collection);
        
        // Verify - should return image regions
        assertEquals(1, regions.size());
        assertEquals(imageRegion, regions.get(0));
    }
    
    @Test
    void testGetRegionsForAllImages_ActionConfig_EmptyCollections() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .build();
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages((ActionConfig) findOptions, emptyCollection);
        
        // Verify - should return default full-screen region
        assertEquals(1, regions.size());
        assertNotNull(regions.get(0));
    }
    
    @Test
    void testGetRegionsForAllImages_ActionConfig_NullSearchRegions() {
        // Setup
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .build();
        // Don't set search regions (null)
        
        Region imageRegion = new Region(10, 10, 90, 90);
        StateImage image = new StateImage();
        Pattern p = new Pattern();
        p.getSearchRegions().addSearchRegions(imageRegion);
        image.addPatterns(p);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        
        // Execute
        List<Region> regions = searchRegionResolver.getRegionsForAllImages((ActionConfig) findOptions, collection);
        
        // Verify - should fall back to image regions
        assertEquals(1, regions.size());
        assertEquals(imageRegion, regions.get(0));
    }
}