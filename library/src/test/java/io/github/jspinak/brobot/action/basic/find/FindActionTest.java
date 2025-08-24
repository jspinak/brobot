package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Find Action Tests")
public class FindActionTest extends BrobotTestBase {

    @Mock
    private FindStrategyRegistry mockStrategyRegistry;
    
    @Mock
    private FindStrategy mockFindStrategy;
    
    @Mock
    private ImageFinder mockImageFinder;
    
    @Mock
    private FindText mockFindText;
    
    @Mock
    private FindColor mockFindColor;
    
    private Find find;
    private ObjectCollection objectCollection;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        find = new Find(mockStrategyRegistry);
        objectCollection = new ObjectCollection();
    }
    
    @Nested
    @DisplayName("Basic Find Operations")
    class BasicFindOperations {
        
        @Test
        @DisplayName("Should return FIND action type")
        public void testGetActionType() {
            assertEquals(ActionInterface.Type.FIND, find.getActionType());
        }
        
        @Test
        @DisplayName("Should find image pattern")
        public void testFindImage() {
            StateImage stateImage = StateImage.builder()
                .withName("test-image")
                .build();
            objectCollection.addStateImage(stateImage);
            
            Match expectedMatch = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .withScore(0.95)
                .build();
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(expectedMatch)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), any())).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertNotNull(result.getMatch());
            assertEquals(0.95, result.getMatch().getScore());
        }
        
        @Test
        @DisplayName("Should find text")
        public void testFindText() {
            StateString stateString = new StateString.Builder()
                .withString("Hello World")
                .build();
            objectCollection.addStateString(stateString);
            
            Match expectedMatch = new Match.Builder()
                .withRegion(new Region(200, 200, 100, 20))
                .withText("Hello World")
                .build();
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(expectedMatch)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), any())).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals("Hello World", result.getMatch().getText());
        }
        
        @Test
        @DisplayName("Should check region for matches")
        public void testFindInRegion() {
            StateRegion stateRegion = new StateRegion.Builder()
                .withRegion(new Region(0, 0, 500, 500))
                .build();
            StateImage stateImage = StateImage.builder().build();
            
            objectCollection.addStateRegion(stateRegion);
            objectCollection.addStateImage(stateImage);
            
            Match expectedMatch = new Match.Builder()
                .withRegion(new Region(150, 150, 50, 50))
                .withScore(0.88)
                .build();
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(expectedMatch)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), any())).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertTrue(stateRegion.getRegion().contains(expectedMatch.getRegion()));
        }
    }
    
    @Nested
    @DisplayName("Find Options Configuration")
    class FindOptionsConfiguration {
        
        @Test
        @DisplayName("Should use similarity threshold")
        public void testSimilarityThreshold() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .withSimilarity(0.85)
                .build();
            objectCollection.setActionConfig(options);
            
            Match expectedMatch = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .withScore(0.87)
                .build();
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(expectedMatch)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), eq(options))).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertTrue(result.getMatch().getScore() >= 0.85);
        }
        
        @Test
        @DisplayName("Should respect search time limit")
        public void testSearchTimeLimit() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .withSearchTime(2.0)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(false)
                .setDuration(2000.0)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), eq(options))).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertFalse(result.isSuccess());
            assertEquals(2000.0, result.getDuration(), 100.0);
        }
        
        @Test
        @DisplayName("Should find all matches")
        public void testFindAllMatches() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .withFind(PatternFindOptions.Find.ALL)
                .build();
            objectCollection.setActionConfig(options);
            
            List<Match> matches = Arrays.asList(
                new Match.Builder().withRegion(new Region(100, 100, 50, 50)).withScore(0.95).build(),
                new Match.Builder().withRegion(new Region(200, 200, 50, 50)).withScore(0.92).build(),
                new Match.Builder().withRegion(new Region(300, 300, 50, 50)).withScore(0.88).build()
            );
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatchList(matches)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), eq(options))).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals(3, result.getMatchList().size());
        }
        
        @Test
        @DisplayName("Should find best match")
        public void testFindBestMatch() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .withFind(PatternFindOptions.Find.BEST)
                .build();
            objectCollection.setActionConfig(options);
            
            List<Match> matches = Arrays.asList(
                new Match.Builder().withRegion(new Region(100, 100, 50, 50)).withScore(0.85).build(),
                new Match.Builder().withRegion(new Region(200, 200, 50, 50)).withScore(0.95).build(),
                new Match.Builder().withRegion(new Region(300, 300, 50, 50)).withScore(0.90).build()
            );
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(matches.get(1))
                .setMatchList(Collections.singletonList(matches.get(1)))
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), eq(options))).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals(0.95, result.getMatch().getScore());
        }
    }
    
    @Nested
    @DisplayName("Multiple Object Handling")
    class MultipleObjectHandling {
        
        @Test
        @DisplayName("Should find from multiple images")
        public void testFindMultipleImages() {
            StateImage image1 = StateImage.builder().withName("image1").build();
            StateImage image2 = StateImage.builder().withName("image2").build();
            StateImage image3 = StateImage.builder().withName("image3").build();
            
            objectCollection.addStateImage(image1);
            objectCollection.addStateImage(image2);
            objectCollection.addStateImage(image3);
            
            Match expectedMatch = new Match.Builder()
                .withRegion(new Region(200, 200, 50, 50))
                .withScore(0.91)
                .build();
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(expectedMatch)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), any())).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockFindStrategy, atLeastOnce()).find(any(), any());
        }
        
        @Test
        @DisplayName("Should handle mixed object types")
        public void testMixedObjectTypes() {
            StateImage stateImage = StateImage.builder().build();
            StateString stateString = new StateString.Builder()
                .withString("test")
                .build();
            StateRegion stateRegion = new StateRegion.Builder()
                .withRegion(new Region(0, 0, 800, 600))
                .build();
            
            objectCollection.addStateImage(stateImage);
            objectCollection.addStateString(stateString);
            objectCollection.addStateRegion(stateRegion);
            
            Match expectedMatch = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .withScore(0.89)
                .build();
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(expectedMatch)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), any())).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle no matches found")
        public void testNoMatchesFound() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(false)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), any())).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertFalse(result.isSuccess());
            assertNull(result.getMatch());
            assertTrue(result.getMatchList().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle empty object collection")
        public void testEmptyObjectCollection() {
            ActionResult result = find.perform(objectCollection);
            
            assertFalse(result.isSuccess());
            assertNull(result.getMatch());
        }
        
        @Test
        @DisplayName("Should handle null object collection")
        public void testNullObjectCollection() {
            ActionResult result = find.perform(null);
            
            assertFalse(result.isSuccess());
            assertNull(result.getMatch());
        }
        
        @Test
        @DisplayName("Should handle strategy not found")
        public void testStrategyNotFound() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(null);
            
            ActionResult result = find.perform(objectCollection);
            
            assertFalse(result.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Search Regions")
    class SearchRegions {
        
        @Test
        @DisplayName("Should search in specified region")
        public void testSearchInRegion() {
            Region searchRegion = new Region(100, 100, 400, 400);
            StateRegion stateRegion = new StateRegion.Builder()
                .withRegion(searchRegion)
                .build();
            StateImage stateImage = StateImage.builder().build();
            
            objectCollection.addStateRegion(stateRegion);
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .withSearchRegion(searchRegion)
                .build();
            objectCollection.setActionConfig(options);
            
            Match expectedMatch = new Match.Builder()
                .withRegion(new Region(200, 200, 50, 50))
                .withScore(0.93)
                .build();
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(expectedMatch)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), eq(options))).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertTrue(searchRegion.contains(expectedMatch.getRegion()));
        }
        
        @Test
        @DisplayName("Should handle multiple search regions")
        public void testMultipleSearchRegions() {
            StateRegion region1 = new StateRegion.Builder()
                .withRegion(new Region(0, 0, 400, 300))
                .build();
            StateRegion region2 = new StateRegion.Builder()
                .withRegion(new Region(400, 0, 400, 300))
                .build();
            StateImage stateImage = StateImage.builder().build();
            
            objectCollection.addStateRegion(region1);
            objectCollection.addStateRegion(region2);
            objectCollection.addStateImage(stateImage);
            
            List<Match> matches = Arrays.asList(
                new Match.Builder().withRegion(new Region(100, 100, 50, 50)).build(),
                new Match.Builder().withRegion(new Region(500, 100, 50, 50)).build()
            );
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatchList(matches)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), any())).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals(2, result.getMatchList().size());
        }
    }
    
    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {
        
        @ParameterizedTest
        @EnumSource(PatternFindOptions.Strategy.class)
        @DisplayName("Should handle all find strategies")
        public void testAllFindStrategies(PatternFindOptions.Strategy findStrategy) {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .withStrategy(findStrategy)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(new Match.Builder().build())
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), eq(options))).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.7, 0.8, 0.9, 0.95, 1.0})
        @DisplayName("Should handle various similarity thresholds")
        public void testVariousSimilarityThresholds(double similarity) {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .withSimilarity(similarity)
                .build();
            objectCollection.setActionConfig(options);
            
            Match expectedMatch = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .withScore(similarity + 0.01)
                .build();
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(expectedMatch)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), eq(options))).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertTrue(result.getMatch().getScore() >= similarity);
        }
        
        @ParameterizedTest
        @MethodSource("provideSearchTimeAndResults")
        @DisplayName("Should handle various search times and results")
        public void testSearchTimeAndResults(double searchTime, boolean shouldFind, int expectedMatches) {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .withSearchTime(searchTime)
                .build();
            objectCollection.setActionConfig(options);
            
            List<Match> matches = new ArrayList<>();
            for (int i = 0; i < expectedMatches; i++) {
                matches.add(new Match.Builder()
                    .withRegion(new Region(i * 100, i * 100, 50, 50))
                    .withScore(0.9 - i * 0.05)
                    .build());
            }
            
            ActionResult expectedResult = new ActionResult.Builder()
                .setSuccess(shouldFind)
                .setMatchList(matches)
                .build();
            
            when(mockStrategyRegistry.getStrategy(any())).thenReturn(mockFindStrategy);
            when(mockFindStrategy.find(any(), eq(options))).thenReturn(expectedResult);
            
            ActionResult result = find.perform(objectCollection);
            
            assertEquals(shouldFind, result.isSuccess());
            assertEquals(expectedMatches, result.getMatchList().size());
        }
        
        static Stream<Arguments> provideSearchTimeAndResults() {
            return Stream.of(
                Arguments.of(0.5, false, 0),
                Arguments.of(1.0, true, 1),
                Arguments.of(2.0, true, 3),
                Arguments.of(5.0, true, 5),
                Arguments.of(10.0, true, 10)
            );
        }
    }
}