package io.github.jspinak.brobot.action.basic.find.motion;

import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.sikuli.basics.Settings;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for MotionFindOptions following Brobot testing guidelines.
 * All tests extend BrobotTestBase for proper mock mode and CI/CD compatibility.
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
    
    @Nested
    @DisplayName("Builder and Default Values")
    class BuilderTests {
        
        @Test
        @DisplayName("Should create with default values")
        void shouldCreateWithDefaultValues() {
            MotionFindOptions options = builder.build();
            
            assertNotNull(options);
            assertEquals(FindStrategy.MOTION, options.getFindStrategy());
            assertEquals(300, options.getMaxMovement());
            assertEquals(Settings.MinSimilarity, options.getSimilarity(), 0.001);
            assertEquals(3.0, options.getSearchDuration(), 0.001);
            assertTrue(options.isCaptureImage());
            assertFalse(options.isUseDefinedRegion());
            assertEquals(-1, options.getMaxMatchesToActOn());
            assertNotNull(options.getSearchRegions());
            assertNotNull(options.getMatchAdjustmentOptions());
        }
        
        @Test
        @DisplayName("Should create from existing options")
        void shouldCreateFromExistingOptions() {
            // Create original with custom values
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addSearchRegions(new Region(100, 100, 400, 400));
            
            MotionFindOptions original = new MotionFindOptions.Builder()
                .setMaxMovement(500)
                .setSimilarity(0.85)
                .setSearchDuration(5.0)
                .setCaptureImage(false)
                .setSearchRegions(searchRegions)
                .build();
            
            // Create copy using copy constructor
            MotionFindOptions copy = new MotionFindOptions.Builder(original).build();
            
            assertNotNull(copy);
            assertEquals(500, copy.getMaxMovement());
            assertEquals(0.85, copy.getSimilarity(), 0.001);
            assertEquals(5.0, copy.getSearchDuration(), 0.001);
            assertFalse(copy.isCaptureImage());
            assertNotNull(copy.getSearchRegions());
        }
    }
    
    @Nested
    @DisplayName("MaxMovement Configuration")
    class MaxMovementTests {
        
        @Test
        @DisplayName("Should set custom max movement")
        void shouldSetMaxMovement() {
            MotionFindOptions options = builder
                .setMaxMovement(600)
                .build();
            
            assertEquals(600, options.getMaxMovement());
        }
        
        @Test
        @DisplayName("Should handle edge case values")
        void shouldHandleEdgeCaseValues() {
            // Zero movement
            MotionFindOptions zeroMovement = builder
                .setMaxMovement(0)
                .build();
            assertEquals(0, zeroMovement.getMaxMovement());
            
            // Large movement
            MotionFindOptions largeMovement = builder
                .setMaxMovement(10000)
                .build();
            assertEquals(10000, largeMovement.getMaxMovement());
            
            // Negative movement (edge case - implementation dependent)
            MotionFindOptions negativeMovement = builder
                .setMaxMovement(-100)
                .build();
            assertEquals(-100, negativeMovement.getMaxMovement());
        }
    }
    
    @Nested
    @DisplayName("Inherited BaseFindOptions Features")
    class InheritedFeaturesTests {
        
        @Test
        @DisplayName("Should inherit similarity settings")
        void shouldInheritSimilarity() {
            MotionFindOptions options = builder
                .setSimilarity(0.95)
                .build();
            
            assertEquals(0.95, options.getSimilarity(), 0.001);
        }
        
        @Test
        @DisplayName("Should inherit search duration")
        void shouldInheritSearchDuration() {
            MotionFindOptions options = builder
                .setSearchDuration(10.0)
                .build();
            
            assertEquals(10.0, options.getSearchDuration(), 0.001);
        }
        
        @Test
        @DisplayName("Should inherit capture image setting")
        void shouldInheritCaptureImage() {
            MotionFindOptions options = builder
                .setCaptureImage(false)
                .build();
            
            assertFalse(options.isCaptureImage());
        }
        
        @Test
        @DisplayName("Should inherit search regions")
        void shouldInheritSearchRegions() {
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addSearchRegions(new Region(50, 50, 300, 300));
            
            MotionFindOptions options = builder
                .setSearchRegions(searchRegions)
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
        @DisplayName("Should inherit use defined region setting")
        void shouldInheritUseDefinedRegion() {
            MotionFindOptions options = builder
                .setUseDefinedRegion(true)
                .build();
            
            assertTrue(options.isUseDefinedRegion());
        }
        
        @Test
        @DisplayName("Should inherit match adjustment options")
        void shouldInheritMatchAdjustmentOptions() {
            MatchAdjustmentOptions matchAdjustment = MatchAdjustmentOptions.builder()
                .setTargetPosition(new Position(Positions.Name.MIDDLEMIDDLE))
                .setTargetOffset(new Location(10, 10))
                .setAddW(20)
                .setAddH(20)
                .build();
            
            MotionFindOptions options = builder
                .setMatchAdjustment(matchAdjustment)
                .build();
            
            assertNotNull(options.getMatchAdjustmentOptions());
            assertNotNull(options.getMatchAdjustmentOptions().getTargetPosition());
            assertEquals(0.5, options.getMatchAdjustmentOptions().getTargetPosition().getPercentW(), 0.001);
            assertEquals(0.5, options.getMatchAdjustmentOptions().getTargetPosition().getPercentH(), 0.001);
            assertNotNull(options.getMatchAdjustmentOptions().getTargetOffset());
            assertEquals(10, options.getMatchAdjustmentOptions().getTargetOffset().getX());
            assertEquals(10, options.getMatchAdjustmentOptions().getTargetOffset().getY());
            assertEquals(20, options.getMatchAdjustmentOptions().getAddW());
            assertEquals(20, options.getMatchAdjustmentOptions().getAddH());
        }
    }
    
    @Nested
    @DisplayName("Real-World Use Cases")
    class UseCaseTests {
        
        @Test
        @DisplayName("Should configure for detecting button hover effects")
        void shouldConfigureForButtonHover() {
            SearchRegions buttonArea = new SearchRegions();
            buttonArea.addSearchRegions(new Region(400, 300, 200, 100));
            
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(5)  // Small movement for hover effects
                .setSimilarity(0.98)  // High similarity for subtle changes
                .setSearchDuration(0.5)  // Quick detection
                .setSearchRegions(buttonArea)
                .setCaptureImage(true)  // Capture for debugging
                .build();
            
            assertEquals(5, options.getMaxMovement());
            assertEquals(0.98, options.getSimilarity(), 0.001);
            assertEquals(0.5, options.getSearchDuration(), 0.001);
            assertNotNull(options.getSearchRegions());
            assertTrue(options.isCaptureImage());
        }
        
        @Test
        @DisplayName("Should configure for detecting scrolling content")
        void shouldConfigureForScrolling() {
            SearchRegions fullScreen = new SearchRegions();
            fullScreen.addSearchRegions(new Region(0, 0, 1920, 1080));
            
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(500)  // Large movement for scrolling
                .setSimilarity(0.8)  // Lower similarity for content variations
                .setSearchDuration(2.0)  // Moderate detection time
                .setSearchRegions(fullScreen)
                .setMaxMatchesToActOn(10)  // Track multiple moving elements
                .build();
            
            assertEquals(500, options.getMaxMovement());
            assertEquals(0.8, options.getSimilarity(), 0.001);
            assertEquals(2.0, options.getSearchDuration(), 0.001);
            assertEquals(10, options.getMaxMatchesToActOn());
        }
        
        @Test
        @DisplayName("Should configure for detecting loading animations")
        void shouldConfigureForLoadingAnimation() {
            SearchRegions loadingArea = new SearchRegions();
            loadingArea.addSearchRegions(new Region(860, 440, 200, 200));  // Center area
            
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(50)  // Moderate movement for spinning
                .setSimilarity(0.85)
                .setSearchDuration(1.0)
                .setSearchRegions(loadingArea)
                .setMaxMatchesToActOn(1)  // Only one loading indicator
                .setUseDefinedRegion(false)  // Search for the pattern
                .build();
            
            assertEquals(50, options.getMaxMovement());
            assertEquals(0.85, options.getSimilarity(), 0.001);
            assertEquals(1, options.getMaxMatchesToActOn());
            assertFalse(options.isUseDefinedRegion());
        }
    }
    
    @Nested
    @DisplayName("Builder Fluency and Chaining")
    class BuilderFluencyTests {
        
        @Test
        @DisplayName("Should support fluent builder pattern")
        void shouldSupportFluentBuilder() {
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addSearchRegions(new Region(10, 10, 100, 100));
            
            MatchAdjustmentOptions matchAdjustment = MatchAdjustmentOptions.builder()
                .setTargetPosition(new Position(Positions.Name.TOPLEFT))
                .build();
            
            MotionFindOptions options = new MotionFindOptions.Builder()
                .setMaxMovement(100)
                .setSimilarity(0.9)
                .setSearchDuration(2.5)
                .setCaptureImage(true)
                .setMaxMatchesToActOn(3)
                .setSearchRegions(searchRegions)
                .setUseDefinedRegion(false)
                .setMatchAdjustment(matchAdjustment)
                .build();
            
            // Verify all settings were applied
            assertEquals(100, options.getMaxMovement());
            assertEquals(0.9, options.getSimilarity(), 0.001);
            assertEquals(2.5, options.getSearchDuration(), 0.001);
            assertTrue(options.isCaptureImage());
            assertEquals(3, options.getMaxMatchesToActOn());
            assertNotNull(options.getSearchRegions());
            assertFalse(options.isUseDefinedRegion());
            assertNotNull(options.getMatchAdjustmentOptions());
            assertNotNull(options.getMatchAdjustmentOptions().getTargetPosition());
            assertEquals(0.0, options.getMatchAdjustmentOptions().getTargetPosition().getPercentW(), 0.001);
            assertEquals(0.0, options.getMatchAdjustmentOptions().getTargetPosition().getPercentH(), 0.001);
        }
    }
    
    @Nested
    @DisplayName("Strategy Verification")
    class StrategyTests {
        
        @Test
        @DisplayName("Should always return MOTION strategy")
        void shouldAlwaysReturnMotionStrategy() {
            // Default configuration
            MotionFindOptions defaultOptions = builder.build();
            assertEquals(FindStrategy.MOTION, defaultOptions.getFindStrategy());
            
            // With custom configuration
            MotionFindOptions customOptions = builder
                .setMaxMovement(0)
                .setSimilarity(1.0)
                .build();
            assertEquals(FindStrategy.MOTION, customOptions.getFindStrategy());
            
            // From copy constructor
            MotionFindOptions copiedOptions = new MotionFindOptions.Builder(defaultOptions).build();
            assertEquals(FindStrategy.MOTION, copiedOptions.getFindStrategy());
        }
    }
}