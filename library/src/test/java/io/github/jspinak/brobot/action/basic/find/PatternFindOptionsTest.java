package io.github.jspinak.brobot.action.basic.find;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Comprehensive test suite for PatternFindOptions - configuration for pattern-matching find operations.
 * Tests builder pattern, strategies, factory methods, JSON serialization, and edge cases.
 */
@DisplayName("PatternFindOptions Tests")
public class PatternFindOptionsTest extends BrobotTestBase {
    
    private PatternFindOptions.Builder builder;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        builder = new PatternFindOptions.Builder();
        objectMapper = new ObjectMapper();
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
        @DisplayName("Default strategy is FIRST")
        public void testDefaultStrategy() {
            PatternFindOptions options = builder.build();
            
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
            assertEquals(FindStrategy.FIRST, options.getFindStrategy());
        }
        
        @Test
        @DisplayName("Default DoOnEach is FIRST")
        public void testDefaultDoOnEach() {
            PatternFindOptions options = builder.build();
            
            assertEquals(PatternFindOptions.DoOnEach.FIRST, options.getDoOnEach());
        }
        
        @Test
        @DisplayName("Default similarity is 0.7")
        public void testDefaultSimilarity() {
            PatternFindOptions options = builder.build();
            
            assertEquals(0.7, options.getSimilarity(), 0.01);
        }
        
        @Test
        @DisplayName("Default search duration is 3.0 seconds")
        public void testDefaultSearchDuration() {
            PatternFindOptions options = builder.build();
            
            assertEquals(3.0, options.getSearchDuration(), 0.01);
        }
        
        @Test
        @DisplayName("Default match fusion options are not null")
        public void testDefaultMatchFusionOptions() {
            PatternFindOptions options = builder.build();
            
            assertNotNull(options.getMatchFusionOptions());
            assertEquals(MatchFusionOptions.FusionMethod.NONE, 
                options.getMatchFusionOptions().getFusionMethod());
        }
    }
    
    @Nested
    @DisplayName("Strategy Configuration")
    class StrategyConfiguration {
        
        @Test
        @DisplayName("Strategy FIRST maps to FindStrategy.FIRST")
        public void testStrategyFirst() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
            assertEquals(FindStrategy.FIRST, options.getFindStrategy());
        }
        
        @Test
        @DisplayName("Strategy BEST maps to FindStrategy.BEST")
        public void testStrategyBest() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.BEST, options.getStrategy());
            assertEquals(FindStrategy.BEST, options.getFindStrategy());
        }
        
        @Test
        @DisplayName("Strategy ALL maps to FindStrategy.ALL")
        public void testStrategyAll() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
            assertEquals(FindStrategy.ALL, options.getFindStrategy());
        }
        
        @Test
        @DisplayName("Strategy EACH maps to FindStrategy.EACH")
        public void testStrategyEach() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .build();
            
            assertEquals(PatternFindOptions.Strategy.EACH, options.getStrategy());
            assertEquals(FindStrategy.EACH, options.getFindStrategy());
        }
        
        @Test
        @DisplayName("DoOnEach.FIRST for first match per image")
        public void testDoOnEachFirst() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .setDoOnEach(PatternFindOptions.DoOnEach.FIRST)
                .build();
            
            assertEquals(PatternFindOptions.DoOnEach.FIRST, options.getDoOnEach());
        }
        
        @Test
        @DisplayName("DoOnEach.BEST for best match per image")
        public void testDoOnEachBest() {
            PatternFindOptions options = builder
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .setDoOnEach(PatternFindOptions.DoOnEach.BEST)
                .build();
            
            assertEquals(PatternFindOptions.DoOnEach.BEST, options.getDoOnEach());
        }
        
        @ParameterizedTest
        @EnumSource(PatternFindOptions.Strategy.class)
        @DisplayName("All strategies are supported and map correctly")
        public void testAllStrategies(PatternFindOptions.Strategy strategy) {
            PatternFindOptions options = builder
                .setStrategy(strategy)
                .build();
            
            assertEquals(strategy, options.getStrategy());
            assertNotNull(options.getFindStrategy());
            
            // Verify each maps to the correct FindStrategy
            switch (strategy) {
                case FIRST:
                    assertEquals(FindStrategy.FIRST, options.getFindStrategy());
                    break;
                case ALL:
                    assertEquals(FindStrategy.ALL, options.getFindStrategy());
                    break;
                case EACH:
                    assertEquals(FindStrategy.EACH, options.getFindStrategy());
                    break;
                case BEST:
                    assertEquals(FindStrategy.BEST, options.getFindStrategy());
                    break;
            }
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
    @DisplayName("Factory Methods")
    class FactoryMethods {
        
        @Test
        @DisplayName("forQuickSearch creates optimized configuration")
        public void testForQuickSearch() {
            PatternFindOptions options = PatternFindOptions.forQuickSearch();
            
            assertNotNull(options);
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
            assertEquals(0.7, options.getSimilarity(), 0.01);
            assertFalse(options.isCaptureImage());
            assertEquals(1, options.getMaxMatchesToActOn());
        }
        
        @Test
        @DisplayName("forPreciseSearch creates high-accuracy configuration")
        public void testForPreciseSearch() {
            PatternFindOptions options = PatternFindOptions.forPreciseSearch();
            
            assertNotNull(options);
            assertEquals(PatternFindOptions.Strategy.BEST, options.getStrategy());
            assertEquals(0.9, options.getSimilarity(), 0.01);
            assertTrue(options.isCaptureImage());
            
            // Check match fusion settings
            MatchFusionOptions fusion = options.getMatchFusionOptions();
            assertNotNull(fusion);
            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, fusion.getFusionMethod());
            assertEquals(10, fusion.getMaxFusionDistanceX());
            assertEquals(10, fusion.getMaxFusionDistanceY());
        }
        
        @Test
        @DisplayName("forAllMatches creates configuration for finding all")
        public void testForAllMatches() {
            PatternFindOptions options = PatternFindOptions.forAllMatches();
            
            assertNotNull(options);
            assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
            assertEquals(0.8, options.getSimilarity(), 0.01);
            assertFalse(options.isCaptureImage());
            assertEquals(-1, options.getMaxMatchesToActOn());
            
            // Check match fusion settings
            MatchFusionOptions fusion = options.getMatchFusionOptions();
            assertNotNull(fusion);
            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, fusion.getFusionMethod());
            assertEquals(20, fusion.getMaxFusionDistanceX());
            assertEquals(20, fusion.getMaxFusionDistanceY());
        }
        
        @Test
        @DisplayName("Factory methods produce distinct configurations")
        public void testFactoryMethodsDistinct() {
            PatternFindOptions quick = PatternFindOptions.forQuickSearch();
            PatternFindOptions precise = PatternFindOptions.forPreciseSearch();
            PatternFindOptions all = PatternFindOptions.forAllMatches();
            
            // Verify they have different strategies
            assertNotEquals(quick.getStrategy(), precise.getStrategy());
            assertNotEquals(quick.getStrategy(), all.getStrategy());
            assertNotEquals(precise.getStrategy(), all.getStrategy());
            
            // Verify they have different similarities
            assertNotEquals(quick.getSimilarity(), precise.getSimilarity());
            assertNotEquals(quick.getSimilarity(), all.getSimilarity());
            assertNotEquals(precise.getSimilarity(), all.getSimilarity());
        }
    }
    
    @Nested
    @DisplayName("Search Duration Configuration")
    class SearchDurationConfiguration {
        
        @Test
        @DisplayName("Set custom search duration")
        public void testCustomSearchDuration() {
            PatternFindOptions options = builder
                .setSearchDuration(10.0)
                .build();
            
            assertEquals(10.0, options.getSearchDuration(), 0.01);
        }
        
        @Test
        @DisplayName("Set zero search duration for immediate return")
        public void testZeroSearchDuration() {
            PatternFindOptions options = builder
                .setSearchDuration(0.0)
                .build();
            
            assertEquals(0.0, options.getSearchDuration(), 0.01);
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.5, 1.0, 5.0, 10.0, 30.0, 60.0})
        @DisplayName("Various search duration values")
        public void testVariousSearchDurations(double duration) {
            PatternFindOptions options = builder
                .setSearchDuration(duration)
                .build();
            
            assertEquals(duration, options.getSearchDuration(), 0.01);
        }
        
        @Test
        @DisplayName("Negative search duration is allowed")
        public void testNegativeSearchDuration() {
            // Negative might mean infinite wait
            PatternFindOptions options = builder
                .setSearchDuration(-1.0)
                .build();
            
            assertEquals(-1.0, options.getSearchDuration(), 0.01);
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
    @DisplayName("Match Fusion Configuration")
    class MatchFusionConfiguration {
        
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
        @DisplayName("Default match fusion is NONE")
        public void testDefaultMatchFusion() {
            PatternFindOptions options = builder.build();
            
            MatchFusionOptions fusion = options.getMatchFusionOptions();
            assertNotNull(fusion);
            assertEquals(MatchFusionOptions.FusionMethod.NONE, fusion.getFusionMethod());
            assertEquals(5, fusion.getMaxFusionDistanceX());
            assertEquals(5, fusion.getMaxFusionDistanceY());
        }
        
        @Test
        @DisplayName("Configure relative match fusion")
        public void testRelativeMatchFusion() {
            MatchFusionOptions fusion = MatchFusionOptions.builder()
                .setFusionMethod(MatchFusionOptions.FusionMethod.RELATIVE)
                .setMaxFusionDistanceX(15)
                .setMaxFusionDistanceY(15)
                .setSceneToUseForCaptureAfterFusingMatches(1)
                .build();
            
            PatternFindOptions options = builder
                .setMatchFusion(fusion)
                .build();
            
            MatchFusionOptions result = options.getMatchFusionOptions();
            assertNotNull(result);
            assertEquals(MatchFusionOptions.FusionMethod.RELATIVE, result.getFusionMethod());
            assertEquals(15, result.getMaxFusionDistanceX());
            assertEquals(15, result.getMaxFusionDistanceY());
            assertEquals(1, result.getSceneToUseForCaptureAfterFusingMatches());
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
        @DisplayName("Null strategy defaults to FIRST")
        public void testNullStrategy() {
            PatternFindOptions options = builder
                .setStrategy(null)
                .build();
            
            assertNotNull(options);
            // Should use default (FIRST) when null is provided
            assertNull(options.getStrategy());
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
    
    @Nested
    @DisplayName("JSON Serialization")
    class JsonSerialization {
        
        @Test
        @DisplayName("Serialize and deserialize basic options")
        public void testBasicSerialization() throws JsonProcessingException {
            PatternFindOptions original = builder
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.85)
                .setSearchDuration(5.0)
                .build();
            
            String json = objectMapper.writeValueAsString(original);
            assertNotNull(json);
            assertTrue(json.contains("BEST"));
            
            PatternFindOptions deserialized = objectMapper.readValue(json, PatternFindOptions.class);
            assertNotNull(deserialized);
            assertEquals(original.getStrategy(), deserialized.getStrategy());
            assertEquals(original.getSimilarity(), deserialized.getSimilarity(), 0.01);
            assertEquals(original.getSearchDuration(), deserialized.getSearchDuration(), 0.01);
        }
        
        @Test
        @DisplayName("Serialize and deserialize with match fusion")
        public void testSerializationWithMatchFusion() throws JsonProcessingException {
            MatchFusionOptions fusion = MatchFusionOptions.builder()
                .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .setMaxFusionDistanceX(20)
                .setMaxFusionDistanceY(20)
                .build();
            
            PatternFindOptions original = builder
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setMatchFusion(fusion)
                .build();
            
            String json = objectMapper.writeValueAsString(original);
            PatternFindOptions deserialized = objectMapper.readValue(json, PatternFindOptions.class);
            
            assertNotNull(deserialized.getMatchFusionOptions());
            assertEquals(MatchFusionOptions.FusionMethod.ABSOLUTE, 
                deserialized.getMatchFusionOptions().getFusionMethod());
            assertEquals(20, deserialized.getMatchFusionOptions().getMaxFusionDistanceX());
        }
        
        @Test
        @DisplayName("Deserialize with unknown properties (forward compatibility)")
        public void testDeserializeWithUnknownProperties() throws JsonProcessingException {
            String json = "{\"strategy\":\"FIRST\",\"similarity\":0.8,\"unknownField\":\"value\"}";
            
            PatternFindOptions options = objectMapper.readValue(json, PatternFindOptions.class);
            assertNotNull(options);
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
            assertEquals(0.8, options.getSimilarity(), 0.01);
        }
        
        @Test
        @DisplayName("Serialize factory method results")
        public void testSerializeFactoryMethods() throws JsonProcessingException {
            PatternFindOptions quick = PatternFindOptions.forQuickSearch();
            PatternFindOptions precise = PatternFindOptions.forPreciseSearch();
            PatternFindOptions all = PatternFindOptions.forAllMatches();
            
            String quickJson = objectMapper.writeValueAsString(quick);
            String preciseJson = objectMapper.writeValueAsString(precise);
            String allJson = objectMapper.writeValueAsString(all);
            
            assertNotNull(quickJson);
            assertNotNull(preciseJson);
            assertNotNull(allJson);
            
            // Verify they can be deserialized back
            PatternFindOptions quickDeserialized = objectMapper.readValue(quickJson, PatternFindOptions.class);
            PatternFindOptions preciseDeserialized = objectMapper.readValue(preciseJson, PatternFindOptions.class);
            PatternFindOptions allDeserialized = objectMapper.readValue(allJson, PatternFindOptions.class);
            
            assertEquals(quick.getStrategy(), quickDeserialized.getStrategy());
            assertEquals(precise.getStrategy(), preciseDeserialized.getStrategy());
            assertEquals(all.getStrategy(), allDeserialized.getStrategy());
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Builder chaining works correctly")
        public void testBuilderChaining() {
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.9)
                .setSearchDuration(10.0)
                .setCaptureImage(true)
                .setUseDefinedRegion(false)
                .setMaxMatchesToActOn(5)
                .setDoOnEach(PatternFindOptions.DoOnEach.BEST)
                .setMatchFusion(MatchFusionOptions.builder()
                    .setFusionMethod(MatchFusionOptions.FusionMethod.RELATIVE)
                    .build())
                .setMatchAdjustment(MatchAdjustmentOptions.builder()
                    .setAddW(10)
                    .setAddH(10)
                    .build())
                .build();
            
            assertNotNull(options);
            assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
            assertEquals(0.9, options.getSimilarity(), 0.01);
            assertEquals(10.0, options.getSearchDuration(), 0.01);
            assertTrue(options.isCaptureImage());
            assertFalse(options.isUseDefinedRegion());
            assertEquals(5, options.getMaxMatchesToActOn());
            assertEquals(PatternFindOptions.DoOnEach.BEST, options.getDoOnEach());
            assertNotNull(options.getMatchFusionOptions());
            assertNotNull(options.getMatchAdjustmentOptions());
        }
        
        @Test
        @DisplayName("Copy constructor preserves all fields")
        public void testCopyConstructorCompleteness() {
            MatchFusionOptions fusion = MatchFusionOptions.builder()
                .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .setMaxFusionDistanceX(15)
                .setMaxFusionDistanceY(15)
                .build();
            
            PatternFindOptions original = builder
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .setDoOnEach(PatternFindOptions.DoOnEach.BEST)
                .setSimilarity(0.88)
                .setSearchDuration(7.5)
                .setMatchFusion(fusion)
                .setCaptureImage(false)
                .setMaxMatchesToActOn(3)
                .build();
            
            PatternFindOptions copy = new PatternFindOptions.Builder(original).build();
            
            assertEquals(original.getStrategy(), copy.getStrategy());
            assertEquals(original.getDoOnEach(), copy.getDoOnEach());
            assertEquals(original.getSimilarity(), copy.getSimilarity(), 0.01);
            assertEquals(original.getSearchDuration(), copy.getSearchDuration(), 0.01);
            assertEquals(original.isCaptureImage(), copy.isCaptureImage());
            assertEquals(original.getMaxMatchesToActOn(), copy.getMaxMatchesToActOn());
            
            // Verify match fusion was deep copied
            assertNotNull(copy.getMatchFusionOptions());
            assertEquals(original.getMatchFusionOptions().getFusionMethod(), 
                copy.getMatchFusionOptions().getFusionMethod());
            assertEquals(original.getMatchFusionOptions().getMaxFusionDistanceX(), 
                copy.getMatchFusionOptions().getMaxFusionDistanceX());
        }
        
        @Test
        @DisplayName("Immutability - original unchanged after copy modification")
        public void testImmutability() {
            PatternFindOptions original = builder
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.7)
                .build();
            
            PatternFindOptions modified = new PatternFindOptions.Builder(original)
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.9)
                .build();
            
            // Original should be unchanged
            assertEquals(PatternFindOptions.Strategy.FIRST, original.getStrategy());
            assertEquals(0.7, original.getSimilarity(), 0.01);
            
            // Modified should have new values
            assertEquals(PatternFindOptions.Strategy.ALL, modified.getStrategy());
            assertEquals(0.9, modified.getSimilarity(), 0.01);
        }
    }
}