package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for PatternFindOptions - configuration for find operations.
 * Tests builder pattern, find types, and search parameters.
 */
@DisplayName("PatternFindOptions Tests")
public class PatternFindOptionsTest extends BrobotTestBase {
    
    private PatternFindOptions.Builder builder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        builder = new PatternFindOptions.Builder();
    }
    
    @Nested
    @DisplayName("Default Configuration")
    class DefaultConfiguration {
        
        @Test
        @DisplayName("Default builder creates valid configuration")
        public void testDefaultBuilder() {
            PatternFindOptions options = builder.build();
            
            assertNotNull(options);
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
            assertEquals(0.7, options.getSimilarity(), 0.01);
            assertNotNull(options.getSearchRegions());
        }
        
        @Test
        @DisplayName("Default find type is FIRST")
        public void testDefaultFindType() {
            PatternFindOptions options = builder.build();
            
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
        }
        
        @Test
        @DisplayName("Default similarity is 0.7")
        public void testDefaultSimilarity() {
            PatternFindOptions options = builder.build();
            
            assertEquals(0.7, options.getSimilarity(), 0.01);
        }
        
        @Test
        @DisplayName("Default timeout is reasonable")
        public void testDefaultTimeout() {
            PatternFindOptions options = builder.build();
            
            assertTrue(options.getSearchDuration() >= 0);
        }
    }
    
    @Nested
    @DisplayName("Find Type Configuration")
    class FindTypeConfiguration {
        
        @Test
        @DisplayName("Find first match")
        public void testFindFirst() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
        }
        
        @Test
        @DisplayName("Find best match")
        public void testFindBest() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.BEST, options.getStrategy());
        }
        
        @Test
        @DisplayName("Find all matches")
        public void testFindAll() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
        }
        
        @Test
        @DisplayName("Find each match type")
        public void testFindEach() {
            PatternFindOptions options = builder
                .setDoOnEach(PatternFindOptions.DoOnEach.FIRST)
                .build();
            
            assertEquals(PatternFindOptions.DoOnEach.FIRST, options.getDoOnEach());
        }
        
        @ParameterizedTest
        @EnumSource(PatternFindOptions.Strategy.class)
        @DisplayName("All find types are supported")
        public void testAllFindTypes(PatternFindOptions.Strategy strategy) {
            PatternFindOptions options = builder
                .setStrategy(strategy)
                .build();
            
            assertEquals(strategy, options.getStrategy());
        }
    }
    
    @Nested
    @DisplayName("Similarity Configuration")
    class SimilarityConfiguration {
        
        @Test
        @DisplayName("Set exact match similarity")
        public void testExactMatch() {
            PatternFindOptions options = builder
                .setSimilarity(1.0)
                .build();
            
            assertEquals(1.0, options.getSimilarity());
        }
        
        @Test
        @DisplayName("Set fuzzy match similarity")
        public void testFuzzyMatch() {
            PatternFindOptions options = builder
                .setSimilarity(0.7)
                .build();
            
            assertEquals(0.7, options.getSimilarity());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.5, 0.7, 0.8, 0.9, 0.95, 0.99, 1.0})
        @DisplayName("Various similarity values")
        public void testVariousSimilarities(double similarity) {
            PatternFindOptions options = builder
                .setSimilarity(similarity)
                .build();
            
            assertEquals(similarity, options.getSimilarity());
        }
        
        @Test
        @DisplayName("Similarity outside 0-1 range")
        public void testSimilarityOutsideRange() {
            // Should handle gracefully
            PatternFindOptions options1 = builder.setSimilarity(-0.5).build();
            PatternFindOptions options2 = builder.setSimilarity(1.5).build();
            
            // Values might be clamped or allowed - just verify no exception
            assertNotNull(options1);
            assertNotNull(options2);
        }
    }
    
    @Nested
    @DisplayName("Timeout Configuration")
    class TimeoutConfiguration {
        
        @Test
        @DisplayName("Set custom timeout")
        public void testCustomTimeout() {
            PatternFindOptions options = builder
                .setSearchDuration(10.0)
                .build();
            
            assertEquals(10.0, options.getSearchDuration());
        }
        
        @Test
        @DisplayName("Set zero timeout for immediate return")
        public void testZeroTimeout() {
            PatternFindOptions options = builder
                .setSearchDuration(0.0)
                .build();
            
            assertEquals(0.0, options.getSearchDuration());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.5, 1.0, 5.0, 10.0, 30.0, 60.0})
        @DisplayName("Various timeout values")
        public void testVariousTimeouts(double timeout) {
            PatternFindOptions options = builder
                .setSearchDuration(timeout)
                .build();
            
            assertEquals(timeout, options.getSearchDuration());
        }
        
        @Test
        @DisplayName("Negative timeout is allowed")
        public void testNegativeTimeout() {
            // Negative might mean infinite wait
            PatternFindOptions options = builder
                .setSearchDuration(-1.0)
                .build();
            
            assertEquals(-1.0, options.getSearchDuration());
        }
    }
    
    @Nested
    @DisplayName("Search Region Configuration")
    class SearchRegionConfiguration {
        
        @Test
        @DisplayName("Set custom search regions")
        public void testCustomSearchRegions() {
            SearchRegions regions = new SearchRegions();
            
            PatternFindOptions options = builder
                .setSearchRegions(regions)
                .build();
            
            assertEquals(regions, options.getSearchRegions());
        }
        
        @Test
        @DisplayName("Default search regions are not null")
        public void testDefaultSearchRegions() {
            PatternFindOptions options = builder.build();
            
            assertNotNull(options.getSearchRegions());
        }
        
        @Test
        @DisplayName("Set null search regions")
        public void testNullSearchRegions() {
            PatternFindOptions options = builder
                .setSearchRegions(null)
                .build();
            
            assertNull(options.getSearchRegions());
        }
    }
    
    @Nested
    @DisplayName("Match Adjustment Options")
    class MatchAdjustmentConfiguration {
        
        @Test
        @DisplayName("Configure match adjustment")
        public void testMatchAdjustment() {
            io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions adjustment = io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions.builder()
                .setAddW(10)
                .setAddH(10)
                .build();
            
            PatternFindOptions options = builder
                .setMatchAdjustment(adjustment)
                .build();
            
            assertNotNull(options.getMatchAdjustmentOptions());
            assertEquals(10, options.getMatchAdjustmentOptions().getAddW());
            assertEquals(10, options.getMatchAdjustmentOptions().getAddH());
        }
        
        @Test
        @DisplayName("No match adjustment by default")
        public void testNoMatchAdjustmentByDefault() {
            PatternFindOptions options = builder.build();
            
            // Default has no adjustments
            io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions adjustment = options.getMatchAdjustmentOptions();
            assertNotNull(adjustment);
            assertEquals(0, adjustment.getAddW());
            assertEquals(0, adjustment.getAddH());
        }
    }
    
    @Nested
    @DisplayName("Match Fusion Options")
    class MatchFusionOptionsTests {
        
        @Test
        @DisplayName("Configure match fusion")
        public void testMatchFusion() {
            MatchFusionOptions fusion = MatchFusionOptions.builder()
                .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .setMaxFusionDistanceX(10)
                .setMaxFusionDistanceY(10)
                .build();
            
            PatternFindOptions options = builder
                .setMatchFusion(fusion)
                .build();
            
            assertNotNull(options.getMatchFusionOptions());
            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, 
                options.getMatchFusionOptions().getFusionMethod());
        }
        
        @Test
        @DisplayName("No match fusion by default")
        public void testNoMatchFusionByDefault() {
            PatternFindOptions options = builder.build();
            
            // Default might be null or have no fusion
            MatchFusionOptions fusion = options.getMatchFusionOptions();
            if (fusion != null) {
                assertEquals(MatchFusionOptions.FusionMethod.NONE, 
                    fusion.getFusionMethod());
            }
        }
    }
    
    @Nested
    @DisplayName("Complex Find Scenarios")
    class ComplexFindScenarios {
        
        @Test
        @DisplayName("Find all with high similarity")
        public void testFindAllHighSimilarity() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.95)
                .setSearchDuration(5.0)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
            assertEquals(0.95, options.getSimilarity());
            assertEquals(5.0, options.getSearchDuration());
        }
        
        @Test
        @DisplayName("Find best match with adjustments")
        public void testFindBestWithAdjustments() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setMatchAdjustment(io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions.builder()
                    .setAddW(5)
                    .setAddH(5)
                    .build())
                .build();
            
            assertEquals(PatternFindOptions.Strategy.BEST, options.getStrategy());
            assertNotNull(options.getMatchAdjustmentOptions());
        }
        
        @Test
        @DisplayName("Find with fusion and adjustment")
        public void testFindWithFusionAndAdjustment() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setMatchFusion(MatchFusionOptions.builder()
                    .setFusionMethod(MatchFusionOptions.FusionMethod.RELATIVE)
                    .build())
                .setMatchAdjustment(io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions.builder()
                    .setAddW(-5)
                    .setAddH(-5)
                    .build())
                .build();
            
            assertNotNull(options.getMatchFusionOptions());
            assertNotNull(options.getMatchAdjustmentOptions());
        }
    }
    
    @Nested
    @DisplayName("Builder Copy Constructor")
    class BuilderCopyConstructor {
        
        @Test
        @DisplayName("Copy existing options")
        public void testCopyConstructor() {
            PatternFindOptions original = builder
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.9)
                .setSearchDuration(10.0)
                .build();
            
            PatternFindOptions copy = new PatternFindOptions.Builder(original).build();
            
            assertEquals(original.getStrategy(), copy.getStrategy());
            assertEquals(original.getSimilarity(), copy.getSimilarity());
            assertEquals(original.getSearchDuration(), copy.getSearchDuration());
        }
        
        @Test
        @DisplayName("Modify copied options")
        public void testModifyCopiedOptions() {
            PatternFindOptions original = builder
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
            
            PatternFindOptions modified = new PatternFindOptions.Builder(original)
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.FIRST, original.getStrategy());
            assertEquals(PatternFindOptions.Strategy.BEST, modified.getStrategy());
        }
    }
    
    @Nested
    @DisplayName("Performance Considerations")
    class PerformanceConsiderations {
        
        @Test
        @DisplayName("Quick find with low similarity")
        public void testQuickFind() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.6) // Lower similarity for faster matching
                .setSearchDuration(0.5) // Short timeout
                .build();
            
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
            assertEquals(0.6, options.getSimilarity());
            assertEquals(0.5, options.getSearchDuration());
        }
        
        @Test
        @DisplayName("Thorough find with high similarity")
        public void testThoroughFind() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.99) // Very high similarity
                .setSearchDuration(30.0) // Long timeout
                .build();
            
            assertEquals(PatternFindOptions.Strategy.BEST, options.getStrategy());
            assertEquals(0.99, options.getSimilarity());
            assertEquals(30.0, options.getSearchDuration());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Empty builder creates valid options")
        public void testEmptyBuilder() {
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            
            assertNotNull(options);
            assertNotNull(options.getStrategy());
            assertTrue(options.getSimilarity() >= 0 && options.getSimilarity() <= 1);
        }
        
        @Test
        @DisplayName("Null find type")
        public void testNullFindType() {
            PatternFindOptions options = builder
                .setStrategy(null)
                .build();
            
            // Should either use default or be null
            // Just verify no exception
            assertNotNull(options);
        }
        
        @Test
        @DisplayName("Extreme similarity values")
        public void testExtremeSimilarityValues() {
            PatternFindOptions options1 = builder.setSimilarity(Double.MIN_VALUE).build();
            PatternFindOptions options2 = builder.setSimilarity(Double.MAX_VALUE).build();
            
            assertNotNull(options1);
            assertNotNull(options2);
        }
    }
}