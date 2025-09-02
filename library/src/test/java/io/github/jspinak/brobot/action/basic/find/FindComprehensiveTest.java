package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for the Find action.
 * Tests pattern matching functionality including:
 * - Basic find operations
 * - Multiple pattern matching
 * - Similarity thresholds
 * - Search region constraints
 * - Find options configuration
 */
@DisplayName("Comprehensive Find Action Tests")
public class FindComprehensiveTest extends BrobotTestBase {
    
    private ActionResult actionResult;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        actionResult = new ActionResult();
    }
    
    @Nested
    @DisplayName("Basic Find Operations")
    class BasicFindOperations {
        
        @Test
        @DisplayName("Should create PatternFindOptions with defaults")
        void testDefaultFindOptions() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            
            // Assert
            assertNotNull(options);
            assertEquals(0.7, options.getSimilarity());
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
        }
        
        @Test
        @DisplayName("Should create custom PatternFindOptions")
        void testCustomFindOptions() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(0.9)
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            
            // Assert
            assertEquals(0.9, options.getSimilarity());
            assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
        }
        
        @Test
        @DisplayName("Should create ObjectCollection with images")
        void testObjectCollectionWithImages() {
            // Arrange
            StateImage image1 = new StateImage.Builder()
                .setName("button.png")
                .build();
            
            StateImage image2 = new StateImage.Builder()
                .setName("icon.png")
                .build();
            
            // Act
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(image1, image2)
                .build();
            
            // Assert
            assertNotNull(collection);
            assertEquals(2, collection.getStateImages().size());
        }
    }
    
    @Nested
    @DisplayName("Similarity Threshold Tests")
    class SimilarityThresholdTests {
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.99})
        @DisplayName("Should set various similarity thresholds")
        void testSimilarityThresholds(double similarity) {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(similarity)
                .build();
            
            // Assert
            assertEquals(similarity, options.getSimilarity());
        }
        
        @Test
        @DisplayName("Should handle exact match requirement")
        void testExactMatch() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(1.0)
                .build();
            
            // Assert
            assertEquals(1.0, options.getSimilarity());
        }
        
        @Test
        @DisplayName("Should handle loose matching")
        void testLooseMatching() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(0.5)
                .build();
            
            // Assert
            assertEquals(0.5, options.getSimilarity());
        }
    }
    
    @Nested
    @DisplayName("Multiple Matches")
    class MultipleMatches {
        
        @Test
        @DisplayName("Should find single match by default")
        void testSingleMatch() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            
            // Assert
            assertEquals(PatternFindOptions.Strategy.FIRST, options.getStrategy());
        }
        
        @Test
        @DisplayName("Should enable multiple matches")
        void testMultipleMatchesEnabled() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setMaxMatchesToActOn(10)
                .build();
            
            // Assert
            assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
            assertEquals(10, options.getMaxMatchesToActOn());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 20, 50, 100})
        @DisplayName("Should set various max matches")
        void testMaxMatchesValues(int maxMatches) {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setMaxMatchesToActOn(maxMatches)
                .build();
            
            // Assert
            assertEquals(maxMatches, options.getMaxMatchesToActOn());
        }
    }
    
    @Nested
    @DisplayName("Search Regions")
    class SearchRegionTests {
        
        @Test
        @DisplayName("Should create SearchRegions")
        void testSearchRegionsCreation() {
            // Arrange
            // Use non-adjacent regions to avoid automatic merging
            Region region1 = new Region(0, 0, 400, 300);
            Region region2 = new Region(600, 0, 400, 300);  // Gap of 200 pixels prevents merging
            
            // Act
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addSearchRegions(region1, region2);
            
            // Assert
            assertNotNull(searchRegions);
            assertEquals(2, searchRegions.getRegions().size());
        }
        
        @Test
        @DisplayName("Should set search regions in options")
        void testSearchRegionsInOptions() {
            // Arrange
            Region region = new Region(100, 100, 400, 300);
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addSearchRegions(region);
            
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchRegions(searchRegions)
                .build();
            
            // Assert
            assertNotNull(options.getSearchRegions());
            assertEquals(1, options.getSearchRegions().getRegions().size());
        }
        
        @Test
        @DisplayName("Should use defined region")
        void testUseDefinedRegion() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setUseDefinedRegion(true)
                .build();
            
            // Assert
            assertTrue(options.isUseDefinedRegion());
        }
    }
    
    @Nested
    @DisplayName("StateImage Handling")
    class StateImageHandling {
        
        @Test
        @DisplayName("Should create StateImage with pattern")
        void testStateImageWithPattern() {
            // Act
            StateImage image = new StateImage.Builder()
                .setName("pattern.png")
                .build();
            
            // Assert
            assertNotNull(image);
            assertEquals("pattern.png", image.getName());
        }
        
        @Test
        @DisplayName("Should handle multiple StateImages")
        void testMultipleStateImages() {
            // Arrange
            List<StateImage> images = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                StateImage img = new StateImage.Builder()
                    .setName("image" + i + ".png")
                    .build();
                images.add(img);
            }
            
            // Act
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(images.toArray(new StateImage[0]))
                .build();
            
            // Assert
            assertEquals(5, collection.getStateImages().size());
        }
        
        @Test
        @DisplayName("Should associate StateImage with owner state")
        void testStateImageWithOwner() {
            // Act
            StateImage image = new StateImage.Builder()
                .setName("button.png")
                .setOwnerStateName("MainMenuState")
                .build();
            
            // Assert
            assertEquals("MainMenuState", image.getOwnerStateName());
        }
    }
    
    @Nested
    @DisplayName("Find Options Advanced")
    class FindOptionsAdvanced {
        
        @Test
        @DisplayName("Should capture images during find")
        void testCaptureImage() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setCaptureImage(true)
                .build();
            
            // Assert
            assertTrue(options.isCaptureImage());
        }
        
        @Test
        @DisplayName("Should set wait time for pattern")
        void testWaitTime() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchDuration(5.0)
                .build();
            
            // Assert
            assertEquals(5.0, options.getSearchDuration());
        }
        
        @Test
        @DisplayName("Should combine multiple options")
        void testCombinedOptions() {
            // Arrange
            SearchRegions searchRegions = new SearchRegions();
            searchRegions.addSearchRegions(new Region(0, 0, 800, 600));
            
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setMaxMatchesToActOn(3)
                .setSearchRegions(searchRegions)
                .setUseDefinedRegion(true)
                .setCaptureImage(true)
                .setSearchDuration(2.0)
                .build();
            
            // Assert
            assertEquals(0.85, options.getSimilarity());
            assertEquals(PatternFindOptions.Strategy.ALL, options.getStrategy());
            assertEquals(3, options.getMaxMatchesToActOn());
            assertNotNull(options.getSearchRegions());
            assertTrue(options.isUseDefinedRegion());
            assertTrue(options.isCaptureImage());
            assertEquals(2.0, options.getSearchDuration());
        }
    }
    
    @Nested
    @DisplayName("Match Results")
    class MatchResults {
        
        @Test
        @DisplayName("Should create Match with region")
        void testMatchCreation() {
            // Act
            Match match = new Match();
            match.setRegion(new Region(100, 100, 50, 30));
            match.setScore(0.95);
            
            // Assert
            assertNotNull(match);
            assertEquals(100, match.x());
            assertEquals(100, match.y());
            assertEquals(50, match.w());
            assertEquals(30, match.h());
            assertEquals(0.95, match.getScore());
        }
        
        @Test
        @DisplayName("Should store multiple matches")
        void testMultipleMatchResults() {
            // Arrange
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Match match = new Match();
                match.setRegion(new Region(i * 100, i * 50, 40, 30));
                match.setScore(0.9 - i * 0.05);
                matches.add(match);
            }
            
            // Act
            ActionResult result = new ActionResult();
            result.setMatchList(matches);
            
            // Assert
            assertEquals(3, result.getMatchList().size());
            assertEquals(0.9, result.getMatchList().get(0).getScore());
            assertEquals(0.85, result.getMatchList().get(1).getScore());
            assertEquals(0.8, result.getMatchList().get(2).getScore());
        }
    }
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @ParameterizedTest
        @CsvSource({
            "1, 10",
            "10, 20",
            "50, 50",
            "100, 100"
        })
        @DisplayName("Should handle various pattern counts efficiently")
        void testPatternCountPerformance(int patternCount, int maxMs) {
            // Arrange
            List<StateImage> images = new ArrayList<>();
            for (int i = 0; i < patternCount; i++) {
                StateImage img = new StateImage.Builder()
                    .setName("pattern" + i + ".png")
                    .build();
                images.add(img);
            }
            
            long startTime = System.currentTimeMillis();
            
            // Act
            ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(images.toArray(new StateImage[0]))
                .build();
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setMaxMatchesToActOn(patternCount)
                .build();
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertEquals(patternCount, collection.getStateImages().size());
            assertTrue(duration < maxMs, 
                String.format("Processing %d patterns should take < %dms, was: %dms", 
                    patternCount, maxMs, duration));
        }
        
        @Test
        @DisplayName("Should create options quickly")
        void testOptionsCreationSpeed() {
            long startTime = System.currentTimeMillis();
            
            // Act
            for (int i = 0; i < 1000; i++) {
                PatternFindOptions options = new PatternFindOptions.Builder()
                    .setSimilarity(0.7 + i * 0.0001)
                    .build();
                assertNotNull(options);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertTrue(duration < 100, "Should create 1000 options in < 100ms");
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle empty ObjectCollection")
        void testEmptyCollection() {
            // Act
            ObjectCollection empty = new ObjectCollection.Builder().build();
            
            // Assert
            assertNotNull(empty);
            assertTrue(empty.getStateImages().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null search regions")
        void testNullSearchRegions() {
            // Act
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchRegions(null)
                .build();
            
            // Assert
            assertNull(options.getSearchRegions());
        }
        
        @Test
        @DisplayName("Should handle invalid similarity values")
        void testInvalidSimilarity() {
            // Test boundary values
            PatternFindOptions options1 = new PatternFindOptions.Builder()
                .setSimilarity(0.0)
                .build();
            assertEquals(0.0, options1.getSimilarity());
            
            PatternFindOptions options2 = new PatternFindOptions.Builder()
                .setSimilarity(2.0) // Above 1.0
                .build();
            assertEquals(2.0, options2.getSimilarity()); // No validation in builder
        }
    }
}