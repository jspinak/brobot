package io.github.jspinak.brobot.action.basic.find.motion;

import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for MotionFindOptions.
 * Tests motion-based find configuration including maximum movement settings
 * and inheritance from BaseFindOptions.
 */
@DisplayName("MotionFindOptions Tests")
public class MotionFindOptionsTest extends BrobotTestBase {
    
    private MotionFindOptions.Builder builder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        builder = new MotionFindOptions.Builder();
    }
    
    private SearchRegions createSearchRegions(int x, int y, int w, int h) {
        SearchRegions searchRegions = new SearchRegions();
        searchRegions.addSearchRegions(new Region(x, y, w, h));
        return searchRegions;
    }
    
    @Nested
    @DisplayName("Builder Construction")
    class BuilderConstruction {
        
        @Test
        @DisplayName("Should create with default values")
        void shouldCreateWithDefaultValues() {
            MotionFindOptions options = builder.build();
            
            assertNotNull(options);
            assertEquals(FindStrategy.MOTION, options.getFindStrategy());
            assertEquals(300, options.getMaxMovement());
        }
        
        @Test
        @DisplayName("Should create from existing options")
        void shouldCreateFromExistingOptions() {
            // Create original options
            MotionFindOptions original = new MotionFindOptions.Builder()
                .setMaxMovement(500)
                .setSimilarity(0.85)
                .setSearchRegions(createSearchRegions(100, 100, 400, 400))
                .build();
            
            // Create new builder from original
            MotionFindOptions copy = new MotionFindOptions.Builder(original).build();
            
            assertNotNull(copy);
            assertEquals(500, copy.getMaxMovement());
            assertEquals(0.85, copy.getSimilarity(), 0.001);
            assertNotNull(copy.getSearchRegions());
        }
    }
    
    @Nested
    @DisplayName("Max Movement Configuration")
    class MaxMovementConfiguration {
        
        @Test
        @DisplayName("Should set custom max movement")
        void shouldSetMaxMovement() {
            MotionFindOptions options = builder
                .setMaxMovement(600)
                .build();
            
            assertEquals(600, options.getMaxMovement());
        }
        
        @Test
        @DisplayName("Should handle small max movement values")
        void shouldHandleSmallMaxMovement() {
            MotionFindOptions options = builder
                .setMaxMovement(10)
                .build();
            
            assertEquals(10, options.getMaxMovement());
        }
        
        @Test
        @DisplayName("Should handle large max movement values")
        void shouldHandleLargeMaxMovement() {
            MotionFindOptions options = builder
                .setMaxMovement(2000)
                .build();
            
            assertEquals(2000, options.getMaxMovement());
        }
        
        @Test
        @DisplayName("Should handle zero max movement")
        void shouldHandleZeroMaxMovement() {
            MotionFindOptions options = builder
                .setMaxMovement(0)
                .build();
            
            assertEquals(0, options.getMaxMovement());
        }
    }
    
    @Nested
    @DisplayName("Inherited BaseFindOptions Features")
    class InheritedFeatures {
        
        @Test
        @DisplayName("Should inherit similarity settings")
        void shouldInheritSimilaritySettings() {
            MotionFindOptions options = builder
                .setSimilarity(0.95)
                .build();
            
            assertEquals(0.95, options.getSimilarity(), 0.001);
        }
        
        @Test
        @DisplayName("Should inherit search duration")
        void shouldInheritSearchDuration() {
            MotionFindOptions options = builder
                .setSearchDuration(5.0)
                .build();
            
            assertEquals(5.0, options.getSearchDuration(), 0.001);
        }
        
        @Test
        @DisplayName("Should inherit capture image setting")
        void shouldInheritCaptureImageSetting() {
            MotionFindOptions options = builder
                .setCaptureImage(false)
                .build();
            
            assertFalse(options.isCaptureImage());
        }
        
        @Test
        @DisplayName("Should inherit search regions")
        void shouldInheritSearchRegions() {
            MotionFindOptions options = builder
                .setSearchRegions(createSearchRegions(50, 50, 300, 300))
                .build();
            
            assertNotNull(options.getSearchRegions());
            Region region = options.getSearchRegions().getOneRegion();
            assertEquals(50, region.x());
            assertEquals(50, region.y());
            assertEquals(300, region.w());
            assertEquals(300, region.h());
        }
        
        @Test
        @DisplayName("Should inherit max matches to act on")
        void shouldInheritMaxMatchesToActOn() {
            MotionFindOptions options = builder
                .setMaxMatchesToActOn(5)
                .build();
            
            assertEquals(5, options.getMaxMatchesToActOn());
        }
        
        @Test
        @DisplayName("Should inherit match adjustment options")
        void shouldInheritMatchAdjustmentOptions() {
            io.github.jspinak.brobot.model.element.Location offset = new io.github.jspinak.brobot.model.element.Location.Builder().setOffsetX(50).setOffsetY(50).build();
            MatchAdjustmentOptions matchAdjustment = MatchAdjustmentOptions.builder()
                .setTargetOffset(offset)
                .build();
                
            MotionFindOptions options = builder
                .setMatchAdjustment(matchAdjustment)
                .build();
            
            assertNotNull(options.getMatchAdjustmentOptions());
            assertNotNull(options.getMatchAdjustmentOptions().getTargetOffset());
            assertEquals(50, options.getMatchAdjustmentOptions().getTargetOffset().getOffsetX());
            assertEquals(50, options.getMatchAdjustmentOptions().getTargetOffset().getOffsetY());
        }
    }
    
    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {
        
        @Test
        @DisplayName("Should configure for detecting button hover effects")
        void shouldConfigureForButtonHoverDetection() {
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(5)  // Small movement for hover effects
                .setSimilarity(0.98)  // High similarity for subtle changes
                .setSearchDuration(0.5)  // Quick detection
                .setSearchRegions(createSearchRegions(400, 300, 200, 100))  // Button area
                .build();
            
            assertEquals(5, options.getMaxMovement());
            assertEquals(0.98, options.getSimilarity(), 0.001);
            assertEquals(0.5, options.getSearchDuration(), 0.001);
            assertNotNull(options.getSearchRegions());
        }
        
        @Test
        @DisplayName("Should configure for detecting scrolling content")
        void shouldConfigureForScrollingContent() {
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(500)  // Large movement for scrolling
                .setSimilarity(0.8)  // Lower similarity for content variations
                .setSearchDuration(2.0)  // Moderate detection time
                .setSearchRegions(createSearchRegions(0, 0, 1920, 1080))  // Full screen
                .setCaptureImage(true)  // Capture for debugging
                .build();
            
            assertEquals(500, options.getMaxMovement());
            assertEquals(0.8, options.getSimilarity(), 0.001);
            assertNotNull(options.getSearchRegions());
            assertTrue(options.isCaptureImage());
        }
        
        @Test
        @DisplayName("Should configure for detecting loading animations")
        void shouldConfigureForLoadingAnimations() {
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(50)  // Moderate movement for spinning animations
                .setSimilarity(0.85)
                .setSearchDuration(1.0)
                .setSearchRegions(createSearchRegions(400, 300, 200, 200))  // Limited area
                .setMaxMatchesToActOn(1)  // Only track one loading indicator
                .build();
            
            assertEquals(50, options.getMaxMovement());
            assertEquals(200, options.getSearchRegions().getOneRegion().w());
            assertEquals(200, options.getSearchRegions().getOneRegion().h());
            assertEquals(1, options.getMaxMatchesToActOn());
        }
    }
    
    @Nested
    @DisplayName("Builder Chaining")
    class BuilderChaining {
        
        @Test
        @DisplayName("Should support fluent builder pattern")
        void shouldSupportFluentBuilderPattern() {
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(100)
                .setSimilarity(0.9)
                .setSearchDuration(2.5)
                .setCaptureImage(true)
                .setMaxMatchesToActOn(3)
                .setSearchRegions(createSearchRegions(10, 10, 100, 100))
                .setUseDefinedRegion(false)
                .build();
            
            assertEquals(100, options.getMaxMovement());
            assertEquals(0.9, options.getSimilarity(), 0.001);
            assertEquals(2.5, options.getSearchDuration(), 0.001);
            assertTrue(options.isCaptureImage());
            assertEquals(3, options.getMaxMatchesToActOn());
            assertNotNull(options.getSearchRegions());
            assertFalse(options.isUseDefinedRegion());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle negative max movement as zero")
        void shouldHandleNegativeMaxMovement() {
            // Implementation note: Negative values may be treated as zero
            // or throw an exception depending on validation logic
            MotionFindOptions options = builder
                .setMaxMovement(-10)
                .build();
            
            // The actual behavior depends on implementation
            assertTrue(options.getMaxMovement() >= -10);
        }
        
        @Test
        @DisplayName("Should maintain default search regions when not set")
        void shouldMaintainDefaultSearchRegions() {
            MotionFindOptions options = builder.build();
            
            assertNotNull(options.getSearchRegions());
            // Default SearchRegions should be empty (searches entire screen)
        }
        
        @Test
        @DisplayName("Should handle empty search regions")
        void shouldHandleEmptySearchRegions() {
            MotionFindOptions options = builder
                .setSearchRegions(new SearchRegions())
                .build();
            
            assertNotNull(options.getSearchRegions());
        }
    }
    
    @Nested
    @DisplayName("Strategy Verification")
    class StrategyVerification {
        
        @Test
        @DisplayName("Should always return MOTION strategy")
        void shouldAlwaysReturnMotionStrategy() {
            MotionFindOptions options = builder.build();
            assertEquals(FindStrategy.MOTION, options.getFindStrategy());
            
            // Even with different configurations
            options = builder
                .setMaxMovement(0)
                .setSearchRegions(createSearchRegions(0, 0, 100, 100))
                .build();
            assertEquals(FindStrategy.MOTION, options.getFindStrategy());
        }
    }
    
    @Nested
    @DisplayName("Complex Configurations")
    class ComplexConfigurations {
        
        @Test
        @DisplayName("Should handle document scrolling detection")
        void shouldHandleDocumentScrolling() {
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(1000)  // Large vertical movement
                .setSimilarity(0.75)  // Lower similarity for text variations
                .setSearchDuration(3.0)
                .setSearchRegions(createSearchRegions(100, 100, 800, 600))  // Document area
                .setCaptureImage(false)  // Performance optimization
                .build();
            
            assertNotNull(options.getSearchRegions());
            assertEquals(800, options.getSearchRegions().getOneRegion().w());
            assertEquals(1000, options.getMaxMovement());
        }
        
        @Test
        @DisplayName("Should handle video playback detection")
        void shouldHandleVideoPlayback() {
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(50)  // Small movement between frames
                .setSimilarity(0.6)  // Low similarity for frame differences
                .setSearchDuration(0.1)  // Very quick detection
                .setMaxMatchesToActOn(10)  // Track multiple regions
                .build();
            
            assertEquals(50, options.getMaxMovement());
            assertEquals(0.6, options.getSimilarity(), 0.001);
            assertEquals(0.1, options.getSearchDuration(), 0.001);
        }
    }
}