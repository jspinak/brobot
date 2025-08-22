package io.github.jspinak.brobot.action.basic.find.histogram;

import io.github.jspinak.brobot.action.basic.find.FindStrategy;
import io.github.jspinak.brobot.action.basic.find.HSVBinOptions;
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
 * Test suite for HistogramFindOptions following Brobot testing guidelines.
 * All tests extend BrobotTestBase for proper mock mode and CI/CD compatibility.
 */
@DisplayName("HistogramFindOptions Tests")
public class HistogramFindOptionsTest extends BrobotTestBase {
    
    private HistogramFindOptions.Builder builder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        builder = new HistogramFindOptions.Builder();
    }
    
    @Nested
    @DisplayName("Builder and Default Values")
    class BuilderTests {
        
        @Test
        @DisplayName("Should create with default values")
        void shouldCreateWithDefaultValues() {
            HistogramFindOptions options = builder.build();
            
            assertNotNull(options);
            assertEquals(FindStrategy.HISTOGRAM, options.getFindStrategy());
            assertNotNull(options.getBinOptions());
            // Verify default HSVBinOptions values
            assertEquals(12, options.getBinOptions().getHueBins());
            assertEquals(2, options.getBinOptions().getSaturationBins());
            assertEquals(1, options.getBinOptions().getValueBins());
            // Verify inherited defaults from BaseFindOptions
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
            searchRegions.addSearchRegions(new Region(100, 100, 200, 200));
            
            HistogramFindOptions original = new HistogramFindOptions.Builder()
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(180)
                    .saturationBins(256)
                    .valueBins(256))
                .setSimilarity(0.85)
                .setSearchDuration(5.0)
                .setCaptureImage(false)
                .setSearchRegions(searchRegions)
                .build();
            
            // Create copy using copy constructor
            HistogramFindOptions copy = new HistogramFindOptions.Builder(original).build();
            
            assertNotNull(copy);
            assertEquals(180, copy.getBinOptions().getHueBins());
            assertEquals(256, copy.getBinOptions().getSaturationBins());
            assertEquals(256, copy.getBinOptions().getValueBins());
            assertEquals(0.85, copy.getSimilarity(), 0.001);
            assertEquals(5.0, copy.getSearchDuration(), 0.001);
            assertFalse(copy.isCaptureImage());
            assertNotNull(copy.getSearchRegions());
        }
    }
    
    @Nested
    @DisplayName("HSV Bin Configuration")
    class HSVBinTests {
        
        @Test
        @DisplayName("Should set custom HSV bins")
        void shouldSetCustomHSVBins() {
            HistogramFindOptions options = builder
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(90)
                    .saturationBins(128)
                    .valueBins(64))
                .build();
            
            assertEquals(90, options.getBinOptions().getHueBins());
            assertEquals(128, options.getBinOptions().getSaturationBins());
            assertEquals(64, options.getBinOptions().getValueBins());
        }
        
        @Test
        @DisplayName("Should handle minimal bin configuration")
        void shouldHandleMinimalBins() {
            HistogramFindOptions options = builder
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(1)
                    .saturationBins(1)
                    .valueBins(1))
                .build();
            
            assertEquals(1, options.getBinOptions().getHueBins());
            assertEquals(1, options.getBinOptions().getSaturationBins());
            assertEquals(1, options.getBinOptions().getValueBins());
        }
        
        @Test
        @DisplayName("Should handle maximal bin configuration")
        void shouldHandleMaximalBins() {
            HistogramFindOptions options = builder
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(360)  // Maximum hue degrees
                    .saturationBins(256)  // Maximum 8-bit value
                    .valueBins(256))  // Maximum 8-bit value
                .build();
            
            assertEquals(360, options.getBinOptions().getHueBins());
            assertEquals(256, options.getBinOptions().getSaturationBins());
            assertEquals(256, options.getBinOptions().getValueBins());
        }
    }
    
    @Nested
    @DisplayName("Inherited BaseFindOptions Features")
    class InheritedFeaturesTests {
        
        @Test
        @DisplayName("Should inherit similarity settings")
        void shouldInheritSimilarity() {
            HistogramFindOptions options = builder
                .setSimilarity(0.95)
                .build();
            
            assertEquals(0.95, options.getSimilarity(), 0.001);
        }
        
        @Test
        @DisplayName("Should inherit search duration")
        void shouldInheritSearchDuration() {
            HistogramFindOptions options = builder
                .setSearchDuration(10.0)
                .build();
            
            assertEquals(10.0, options.getSearchDuration(), 0.001);
        }
        
        @Test
        @DisplayName("Should inherit capture image setting")
        void shouldInheritCaptureImage() {
            HistogramFindOptions options = builder
                .setCaptureImage(false)
                .build();
            
            assertFalse(options.isCaptureImage());
        }
        
        @Test
        @DisplayName("Should inherit search regions")
        void shouldInheritSearchRegions() {
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addSearchRegions(new Region(50, 50, 300, 300));
            
            HistogramFindOptions options = builder
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
            HistogramFindOptions options = builder
                .setMaxMatchesToActOn(10)
                .build();
            
            assertEquals(10, options.getMaxMatchesToActOn());
        }
        
        @Test
        @DisplayName("Should inherit use defined region setting")
        void shouldInheritUseDefinedRegion() {
            HistogramFindOptions options = builder
                .setUseDefinedRegion(true)
                .build();
            
            assertTrue(options.isUseDefinedRegion());
        }
        
        @Test
        @DisplayName("Should inherit match adjustment options")
        void shouldInheritMatchAdjustmentOptions() {
            MatchAdjustmentOptions matchAdjustment = MatchAdjustmentOptions.builder()
                .setAddW(20)
                .setAddH(20)
                .setAddX(5)
                .setAddY(5)
                .build();
            
            HistogramFindOptions options = builder
                .setMatchAdjustment(matchAdjustment)
                .build();
            
            assertNotNull(options.getMatchAdjustmentOptions());
            assertEquals(20, options.getMatchAdjustmentOptions().getAddW());
            assertEquals(20, options.getMatchAdjustmentOptions().getAddH());
            assertEquals(5, options.getMatchAdjustmentOptions().getAddX());
            assertEquals(5, options.getMatchAdjustmentOptions().getAddY());
        }
    }
    
    @Nested
    @DisplayName("Real-World Use Cases")
    class UseCaseTests {
        
        @Test
        @DisplayName("Should configure for color-based object detection")
        void shouldConfigureForColorBasedDetection() {
            SearchRegions fullScreen = new SearchRegions();
            fullScreen.addSearchRegions(new Region(0, 0, 1920, 1080));
            
            HistogramFindOptions options = new HistogramFindOptions.Builder()
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(36)  // 10-degree hue resolution
                    .saturationBins(8)  // Moderate saturation resolution
                    .valueBins(4))  // Low value resolution
                .setSimilarity(0.85)
                .setSearchDuration(2.0)
                .setSearchRegions(fullScreen)
                .setCaptureImage(true)
                .build();
            
            assertEquals(36, options.getBinOptions().getHueBins());
            assertEquals(0.85, options.getSimilarity(), 0.001);
            assertEquals(2.0, options.getSearchDuration(), 0.001);
            assertNotNull(options.getSearchRegions());
            assertTrue(options.isCaptureImage());
        }
        
        @Test
        @DisplayName("Should configure for grayscale image matching")
        void shouldConfigureForGrayscaleMatching() {
            HistogramFindOptions options = new HistogramFindOptions.Builder()
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(1)  // No hue information
                    .saturationBins(1)  // No saturation information
                    .valueBins(256))  // Full value resolution for grayscale
                .setSimilarity(0.9)
                .setMaxMatchesToActOn(1)
                .setSearchDuration(1.0)
                .build();
            
            assertEquals(1, options.getBinOptions().getHueBins());
            assertEquals(1, options.getBinOptions().getSaturationBins());
            assertEquals(256, options.getBinOptions().getValueBins());
            assertEquals(0.9, options.getSimilarity(), 0.001);
            assertEquals(1, options.getMaxMatchesToActOn());
        }
        
        @Test
        @DisplayName("Should configure for UI element detection by color")
        void shouldConfigureForUIElementDetection() {
            SearchRegions uiArea = new SearchRegions();
            uiArea.addSearchRegions(new Region(100, 100, 800, 600));
            
            HistogramFindOptions options = new HistogramFindOptions.Builder()
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(24)  // Good color discrimination
                    .saturationBins(4)  // Basic saturation levels
                    .valueBins(2))  // Light/dark distinction
                .setSimilarity(0.95)  // High similarity for UI consistency
                .setSearchRegions(uiArea)
                .setCaptureImage(true)  // For debugging
                .setUseDefinedRegion(false)
                .build();
            
            assertEquals(24, options.getBinOptions().getHueBins());
            assertEquals(0.95, options.getSimilarity(), 0.001);
            assertTrue(options.isCaptureImage());
            assertFalse(options.isUseDefinedRegion());
        }
        
        @Test
        @DisplayName("Should configure for skin tone detection")
        void shouldConfigureForSkinToneDetection() {
            HistogramFindOptions options = new HistogramFindOptions.Builder()
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(180)  // High hue resolution for skin tones
                    .saturationBins(32)  // Good saturation discrimination
                    .valueBins(16))  // Moderate value resolution
                .setSimilarity(0.75)  // Allow for skin tone variations
                .setSearchDuration(1.0)
                .setMaxMatchesToActOn(20)  // Multiple skin regions
                .build();
            
            assertEquals(180, options.getBinOptions().getHueBins());
            assertEquals(32, options.getBinOptions().getSaturationBins());
            assertEquals(16, options.getBinOptions().getValueBins());
            assertEquals(0.75, options.getSimilarity(), 0.001);
            assertEquals(20, options.getMaxMatchesToActOn());
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
                .setTargetPosition(new Position(Positions.Name.MIDDLEMIDDLE))
                .build();
            
            HistogramFindOptions options = new HistogramFindOptions.Builder()
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(18)
                    .saturationBins(3)
                    .valueBins(2))
                .setSimilarity(0.88)
                .setSearchDuration(1.5)
                .setCaptureImage(true)
                .setMaxMatchesToActOn(5)
                .setSearchRegions(searchRegions)
                .setUseDefinedRegion(false)
                .setMatchAdjustment(matchAdjustment)
                .build();
            
            // Verify all settings were applied
            assertEquals(18, options.getBinOptions().getHueBins());
            assertEquals(0.88, options.getSimilarity(), 0.001);
            assertEquals(1.5, options.getSearchDuration(), 0.001);
            assertTrue(options.isCaptureImage());
            assertEquals(5, options.getMaxMatchesToActOn());
            assertNotNull(options.getSearchRegions());
            assertFalse(options.isUseDefinedRegion());
            assertNotNull(options.getMatchAdjustmentOptions());
        }
    }
    
    @Nested
    @DisplayName("Strategy Verification")
    class StrategyTests {
        
        @Test
        @DisplayName("Should always return HISTOGRAM strategy")
        void shouldAlwaysReturnHistogramStrategy() {
            // Default configuration
            HistogramFindOptions defaultOptions = builder.build();
            assertEquals(FindStrategy.HISTOGRAM, defaultOptions.getFindStrategy());
            
            // With custom configuration
            HistogramFindOptions customOptions = builder
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(1)
                    .saturationBins(1)
                    .valueBins(1))
                .setSimilarity(1.0)
                .build();
            assertEquals(FindStrategy.HISTOGRAM, customOptions.getFindStrategy());
            
            // From copy constructor
            HistogramFindOptions copiedOptions = new HistogramFindOptions.Builder(defaultOptions).build();
            assertEquals(FindStrategy.HISTOGRAM, copiedOptions.getFindStrategy());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle zero bin values")
        void shouldHandleZeroBinValues() {
            // Zero bins might be invalid, but should not crash
            HistogramFindOptions options = builder
                .setBinOptions(HSVBinOptions.builder()
                    .hueBins(0)
                    .saturationBins(0)
                    .valueBins(0))
                .build();
            
            assertNotNull(options.getBinOptions());
            assertEquals(0, options.getBinOptions().getHueBins());
            assertEquals(0, options.getBinOptions().getSaturationBins());
            assertEquals(0, options.getBinOptions().getValueBins());
        }
        
        @Test
        @DisplayName("Should maintain default search regions when not set")
        void shouldMaintainDefaultSearchRegions() {
            HistogramFindOptions options = builder.build();
            
            assertNotNull(options.getSearchRegions());
            // Default SearchRegions should be empty list (searches entire screen)
            assertTrue(options.getSearchRegions().getRegions().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle empty search regions")
        void shouldHandleEmptySearchRegions() {
            SearchRegions emptyRegions = new SearchRegions();
            
            HistogramFindOptions options = builder
                .setSearchRegions(emptyRegions)
                .build();
            
            assertNotNull(options.getSearchRegions());
            assertTrue(options.getSearchRegions().getRegions().isEmpty());
        }
    }
}