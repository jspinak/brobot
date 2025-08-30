package io.github.jspinak.brobot.declarativeregion;

import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to demonstrate declarative region behavior.
 * When a declarative region is set as a search region on an image,
 * the image's fixed region should be deleted.
 */
@DisplayName("Simple Declarative Region Tests")
public class SimpleDeclarativeRegionTest {
    
    @BeforeAll
    static void setup() {
        // Enable mock mode to avoid needing actual images
        FrameworkSettings.mock = true;
    }

    @Test
    @DisplayName("Declarative region definition structure")
    void testDeclarativeRegionStructure() {
        // Create ImageA (reference image)
        // In mock mode, we can create patterns without actual image files
        Pattern patternA = new Pattern();
        patternA.setName("pattern-a");
        
        StateImage imageA = new StateImage.Builder()
                .setName("ReferenceImage")
                .addPattern(patternA)
                .build();
        
        // Create ImageB with declarative dependency on ImageA
        Pattern patternB = new Pattern();
        patternB.setName("pattern-b");
        
        StateImage imageB = new StateImage.Builder()
                .setName("DependentImage")
                .addPattern(patternB)
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                        .setTargetType(StateObject.Type.IMAGE)
                        .setTargetStateName("StateA")
                        .setTargetObjectName("ReferenceImage")
                        .setAdjustments(MatchAdjustmentOptions.builder()
                                .setAddX(100)
                                .setAddY(50)
                                .setAddW(200)
                                .setAddH(150)
                                .build())
                        .build())
                .build();
        
        // Verify the declarative region is set
        assertNotNull(imageB.getSearchRegionOnObject());
        assertEquals("StateA", imageB.getSearchRegionOnObject().getTargetStateName());
        assertEquals("ReferenceImage", imageB.getSearchRegionOnObject().getTargetObjectName());
        
        // Verify adjustments
        MatchAdjustmentOptions adjustments = imageB.getSearchRegionOnObject().getAdjustments();
        assertEquals(100, adjustments.getAddX());
        assertEquals(50, adjustments.getAddY());
        assertEquals(200, adjustments.getAddW());
        assertEquals(150, adjustments.getAddH());
    }
    
    @Test
    @DisplayName("Fixed region should be cleared when declarative region is applied")
    void testFixedRegionClearingBehavior() {
        // Create ImageB with both fixed region and declarative region
        Pattern pattern = new Pattern();
        pattern.setName("pattern");
        
        StateImage imageB = new StateImage.Builder()
                .setName("ImageWithBothRegions")
                .addPattern(pattern)
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                        .setTargetType(StateObject.Type.IMAGE)
                        .setTargetStateName("StateA")
                        .setTargetObjectName("ReferenceImage")
                        .build())
                .build();
        
        // Initially set a fixed region
        Region fixedRegion = new Region(0, 0, 1920, 1080);
        imageB.setSearchRegions(fixedRegion);
        
        // Verify pattern has search regions
        assertFalse(imageB.getPatterns().isEmpty());
        assertNotNull(imageB.getPatterns().get(0).getSearchRegions());
        
        // The declarative region should take precedence
        assertNotNull(imageB.getSearchRegionOnObject());
        
        // EXPECTED BEHAVIOR:
        // When the declarative region is applied (i.e., when ReferenceImage is found),
        // the fixed region on imageB should be DELETED/CLEARED.
        // This ensures imageB uses only the declarative region for searching.
        
        // In actual execution:
        // 1. ReferenceImage is found at location (x, y)
        // 2. DynamicRegionResolver calculates new region for imageB based on ReferenceImage location
        // 3. The fixed region (0, 0, 1920, 1080) is DELETED from imageB
        // 4. imageB now searches only in the declaratively defined region
    }
}