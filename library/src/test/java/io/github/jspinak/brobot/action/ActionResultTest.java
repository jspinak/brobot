package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.action.result.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ActionResult.
 * Tests the unified results container for all action executions.
 */
@DisplayName("ActionResult Tests")
public class ActionResultTest extends BrobotTestBase {
    
    private ActionResult actionResult;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        actionResult = new ActionResult();
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create empty ActionResult")
        void shouldCreateEmptyActionResult() {
            ActionResult result = new ActionResult();
            
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.isEmpty());
            assertEquals("", result.getActionDescription());
            assertEquals("", result.getOutputText());
        }
        
        @Test
        @DisplayName("Should create ActionResult with config")
        void shouldCreateActionResultWithConfig() {
            ActionConfig config = mock(ActionConfig.class);
            ActionResult result = new ActionResult(config);
            
            assertNotNull(result);
            assertEquals(config, result.getActionConfig());
        }
    }
    
    @Nested
    @DisplayName("Match Management")
    class MatchManagement {
        
        @Test
        @DisplayName("Should add single match")
        void shouldAddSingleMatch() {
            Match match = createMockMatch(0.95);
            
            actionResult.add(match);
            
            assertEquals(1, actionResult.size());
            assertFalse(actionResult.isEmpty());
            assertTrue(actionResult.getMatchList().contains(match));
        }
        
        @Test
        @DisplayName("Should add multiple matches")
        void shouldAddMultipleMatches() {
            Match match1 = createMockMatch(0.9);
            Match match2 = createMockMatch(0.8);
            Match match3 = createMockMatch(0.7);
            
            actionResult.add(match1, match2, match3);
            
            assertEquals(3, actionResult.size());
            assertEquals(3, actionResult.getMatchList().size());
        }
        
        @Test
        @DisplayName("Should set match list directly")
        void shouldSetMatchListDirectly() {
            List<Match> matches = Arrays.asList(
                createMockMatch(0.9),
                createMockMatch(0.8),
                createMockMatch(0.7)
            );
            
            actionResult.setMatchList(matches);
            
            assertEquals(3, actionResult.size());
            assertEquals(matches.size(), actionResult.getMatchList().size());
        }
        
        @Test
        @DisplayName("Should handle null match list")
        void shouldHandleNullMatchList() {
            actionResult.setMatchList(null);
            
            assertTrue(actionResult.isEmpty());
            assertEquals(0, actionResult.size());
        }
        
        @Test
        @DisplayName("Should track initial matches")
        void shouldTrackInitialMatches() {
            List<Match> initialMatches = Arrays.asList(
                createMockMatch(0.9),
                createMockMatch(0.8)
            );
            
            actionResult.setInitialMatchList(initialMatches);
            
            assertEquals(2, actionResult.getInitialMatchList().size());
        }
    }
    
    @Nested
    @DisplayName("Match Sorting")
    class MatchSorting {
        
        @Test
        @DisplayName("Should sort matches by score ascending")
        void shouldSortMatchesByScoreAscending() {
            actionResult.add(
                createMockMatch(0.8),
                createMockMatch(0.6),
                createMockMatch(0.9),
                createMockMatch(0.7)
            );
            
            actionResult.sortMatchObjects();
            
            List<Match> sorted = actionResult.getMatchList();
            assertTrue(sorted.get(0).getScore() <= sorted.get(1).getScore());
            assertTrue(sorted.get(1).getScore() <= sorted.get(2).getScore());
            assertTrue(sorted.get(2).getScore() <= sorted.get(3).getScore());
        }
        
        @Test
        @DisplayName("Should sort matches by score descending")
        void shouldSortMatchesByScoreDescending() {
            actionResult.add(
                createMockMatch(0.6),
                createMockMatch(0.9),
                createMockMatch(0.7),
                createMockMatch(0.8)
            );
            
            actionResult.sortMatchObjectsDescending();
            
            List<Match> sorted = actionResult.getMatchList();
            assertTrue(sorted.get(0).getScore() >= sorted.get(1).getScore());
            assertTrue(sorted.get(1).getScore() >= sorted.get(2).getScore());
            assertTrue(sorted.get(2).getScore() >= sorted.get(3).getScore());
        }
        
        @Test
        @DisplayName("Should sort matches by size descending")
        void shouldSortMatchesBySizeDescending() {
            actionResult.add(
                createMockMatchWithSize(0.8, 100),
                createMockMatchWithSize(0.7, 200),
                createMockMatchWithSize(0.9, 50),
                createMockMatchWithSize(0.6, 150)
            );
            
            actionResult.sortBySizeDescending();
            
            List<Match> sorted = actionResult.getMatchList();
            assertEquals(4, sorted.size(), "Should have 4 matches");
            // Verify size ordering (largest first) - areas should be 40000, 22500, 10000, 2500
            // Print sizes for debugging
            for (int i = 0; i < sorted.size(); i++) {
                System.out.println("Match " + i + " size: " + sorted.get(i).getRegion().size());
            }
            assertTrue(sorted.get(0).getRegion().size() >= sorted.get(1).getRegion().size(), 
                "First should be >= second: " + sorted.get(0).getRegion().size() + " >= " + sorted.get(1).getRegion().size());
            assertTrue(sorted.get(1).getRegion().size() >= sorted.get(2).getRegion().size(),
                "Second should be >= third: " + sorted.get(1).getRegion().size() + " >= " + sorted.get(2).getRegion().size());
            assertTrue(sorted.get(2).getRegion().size() >= sorted.get(3).getRegion().size(),
                "Third should be >= fourth: " + sorted.get(2).getRegion().size() + " >= " + sorted.get(3).getRegion().size());
        }
    }
    
    @Nested
    @DisplayName("Best Match Operations")
    class BestMatchOperations {
        
        @Test
        @DisplayName("Should find best match")
        void shouldFindBestMatch() {
            Match bestMatch = createMockMatch(0.95);
            actionResult.add(
                createMockMatch(0.7),
                bestMatch,
                createMockMatch(0.8)
            );
            
            Optional<Match> best = actionResult.getBestMatch();
            
            assertTrue(best.isPresent());
            assertEquals(bestMatch, best.get());
            assertEquals(0.95, best.get().getScore());
        }
        
        @Test
        @DisplayName("Should return empty for no matches")
        void shouldReturnEmptyForNoMatches() {
            Optional<Match> best = actionResult.getBestMatch();
            
            assertFalse(best.isPresent());
        }
        
        @Test
        @DisplayName("Should get best location")
        void shouldGetBestLocation() {
            Location expectedLocation = new Location(100, 200);
            Match bestMatch = createMockMatchWithLocation(0.95, expectedLocation);
            
            actionResult.add(
                createMockMatch(0.7),
                bestMatch,
                createMockMatch(0.8)
            );
            
            Optional<Location> bestLocation = actionResult.getBestLocation();
            
            assertTrue(bestLocation.isPresent());
            assertEquals(expectedLocation, bestLocation.get());
        }
        
        @Test
        @DisplayName("Should check best match similarity threshold")
        void shouldCheckBestMatchSimilarityThreshold() {
            actionResult.add(createMockMatch(0.85));
            
            assertTrue(actionResult.bestMatchSimilarityLessThan(0.9));
            assertFalse(actionResult.bestMatchSimilarityLessThan(0.8));
        }
        
        @Test
        @DisplayName("Should return true for empty matches when checking threshold")
        void shouldReturnTrueForEmptyMatchesWhenCheckingThreshold() {
            assertTrue(actionResult.bestMatchSimilarityLessThan(0.5));
        }
    }
    
    @Nested
    @DisplayName("Match Extraction")
    class MatchExtraction {
        
        @Test
        @DisplayName("Should extract regions from matches")
        void shouldExtractRegionsFromMatches() {
            Region r1 = new Region(0, 0, 100, 100);
            Region r2 = new Region(50, 50, 150, 150);
            
            actionResult.add(
                createMockMatchWithRegion(0.8, r1),
                createMockMatchWithRegion(0.9, r2)
            );
            
            List<Region> regions = actionResult.getMatchRegions();
            
            assertEquals(2, regions.size());
            assertTrue(regions.contains(r1));
            assertTrue(regions.contains(r2));
        }
        
        @Test
        @DisplayName("Should extract locations from matches")
        void shouldExtractLocationsFromMatches() {
            Location l1 = new Location(10, 20);
            Location l2 = new Location(30, 40);
            
            actionResult.add(
                createMockMatchWithLocation(0.8, l1),
                createMockMatchWithLocation(0.9, l2)
            );
            
            List<Location> locations = actionResult.getMatchLocations();
            
            assertEquals(2, locations.size());
            assertTrue(locations.contains(l1));
            assertTrue(locations.contains(l2));
        }
    }
    
    @Nested
    @DisplayName("Success and Status")
    class SuccessAndStatus {
        
        @Test
        @DisplayName("Should set and get success status")
        void shouldSetAndGetSuccessStatus() {
            assertFalse(actionResult.isSuccess());
            
            actionResult.setSuccess(true);
            
            assertTrue(actionResult.isSuccess());
        }
        
        @Test
        @DisplayName("Should set and get action description")
        void shouldSetAndGetActionDescription() {
            String description = "Click on button";
            
            actionResult.setActionDescription(description);
            
            assertEquals(description, actionResult.getActionDescription());
        }
        
        @Test
        @DisplayName("Should set and get output text")
        void shouldSetAndGetOutputText() {
            String output = "Action completed successfully";
            
            actionResult.setOutputText(output);
            
            assertEquals(output, actionResult.getOutputText());
        }
    }
    
    @Nested
    @DisplayName("Max Matches Configuration")
    class MaxMatchesConfiguration {
        
        @Test
        @DisplayName("Should set and get max matches")
        void shouldSetAndGetMaxMatches() {
            actionResult.setMaxMatches(5);
            
            assertEquals(5, actionResult.getMaxMatches());
        }
        
        @Test
        @DisplayName("Should allow unlimited matches with -1")
        void shouldAllowUnlimitedMatchesWithNegativeOne() {
            actionResult.setMaxMatches(-1);
            
            assertEquals(-1, actionResult.getMaxMatches());
        }
    }
    
    @Nested
    @DisplayName("Component Integration")
    class ComponentIntegration {
        
        @Test
        @DisplayName("Should have timing data component")
        void shouldHaveTimingDataComponent() {
            assertNotNull(actionResult.getTimingData());
        }
        
        @Test
        @DisplayName("Should have text extraction component")
        void shouldHaveTextExtractionComponent() {
            assertNotNull(actionResult.getTextResult());
        }
        
        @Test
        @DisplayName("Should have state tracker component")
        void shouldHaveStateTrackerComponent() {
            assertNotNull(actionResult.getStateTracker());
        }
        
        @Test
        @DisplayName("Should have region manager component")
        void shouldHaveRegionManagerComponent() {
            assertNotNull(actionResult.getRegionManager());
        }
        
        @Test
        @DisplayName("Should have movement tracker component")
        void shouldHaveMovementTrackerComponent() {
            assertNotNull(actionResult.getMovementTracker());
        }
        
        @Test
        @DisplayName("Should have action analysis component")
        void shouldHaveActionAnalysisComponent() {
            assertNotNull(actionResult.getActionAnalysis());
        }
        
        @Test
        @DisplayName("Should have execution history component")
        void shouldHaveExecutionHistoryComponent() {
            assertNotNull(actionResult.getExecutionHistory());
        }
    }
    
    @Nested
    @DisplayName("Deprecated Methods")
    class DeprecatedMethods {
        
        @Test
        @DisplayName("Should support deprecated sortByMatchScoreDecending")
        @SuppressWarnings("deprecation")
        void shouldSupportDeprecatedSortByMatchScoreDecending() {
            actionResult.add(
                createMockMatch(0.8),
                createMockMatch(0.6),
                createMockMatch(0.9)
            );
            
            actionResult.sortByMatchScoreDecending(); // Uses deprecated method
            
            // Should still work (delegates to sortMatchObjects)
            assertEquals(3, actionResult.size());
        }
        
        @Test
        @DisplayName("Should support deprecated sortBySizeDecending")
        @SuppressWarnings("deprecation")
        void shouldSupportDeprecatedSortBySizeDecending() {
            actionResult.add(
                createMockMatchWithSize(0.8, 100),
                createMockMatchWithSize(0.7, 200)
            );
            
            actionResult.sortBySizeDecending(); // Uses deprecated method
            
            // Should still work (delegates to sortBySizeDescending)
            assertEquals(2, actionResult.size());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty match list operations")
        void shouldHandleEmptyMatchListOperations() {
            assertTrue(actionResult.isEmpty());
            assertEquals(0, actionResult.size());
            assertTrue(actionResult.getMatchList().isEmpty());
            assertTrue(actionResult.getMatchRegions().isEmpty());
            assertTrue(actionResult.getMatchLocations().isEmpty());
            assertFalse(actionResult.getBestMatch().isPresent());
            assertFalse(actionResult.getBestLocation().isPresent());
        }
        
        @Test
        @DisplayName("Should handle null in add operation")
        void shouldHandleNullInAddOperation() {
            Match validMatch = createMockMatch(0.8);
            
            // Add with null should handle gracefully
            actionResult.add(validMatch, null);
            
            assertEquals(1, actionResult.size()); // Only valid match added
        }
        
        @Test
        @DisplayName("Should handle matches with identical scores")
        void shouldHandleMatchesWithIdenticalScores() {
            actionResult.add(
                createMockMatch(0.8),
                createMockMatch(0.8),
                createMockMatch(0.8)
            );
            
            actionResult.sortMatchObjects();
            
            assertEquals(3, actionResult.size());
            // All have same score, order may vary but should not crash
        }
    }
    
    // Helper methods for creating test data
    
    private Match createMockMatch(double score) {
        Match match = mock(Match.class);
        when(match.getScore()).thenReturn(score);
        when(match.getRegion()).thenReturn(new Region(0, 0, 50, 50));
        when(match.getTarget()).thenReturn(new Location(25, 25));
        return match;
    }
    
    private Match createMockMatchWithSize(double score, int size) {
        Match match = mock(Match.class);
        Region region = new Region(0, 0, size, size);
        when(match.getScore()).thenReturn(score);
        when(match.getRegion()).thenReturn(region);
        when(match.getTarget()).thenReturn(new Location(size/2, size/2));
        when(match.size()).thenReturn(size * size); // Mock the size() method for sorting
        return match;
    }
    
    private Match createMockMatchWithRegion(double score, Region region) {
        Match match = mock(Match.class);
        when(match.getScore()).thenReturn(score);
        when(match.getRegion()).thenReturn(region);
        // Region doesn't have getCenter(), use middle position
        Location center = new Location(region.x() + region.w()/2, region.y() + region.h()/2);
        when(match.getTarget()).thenReturn(center);
        return match;
    }
    
    private Match createMockMatchWithLocation(double score, Location location) {
        Match match = mock(Match.class);
        when(match.getScore()).thenReturn(score);
        when(match.getTarget()).thenReturn(location);
        when(match.getRegion()).thenReturn(new Region(location.getX()-25, location.getY()-25, 50, 50));
        return match;
    }
}