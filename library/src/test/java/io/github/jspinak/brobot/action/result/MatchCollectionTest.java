package io.github.jspinak.brobot.action.result;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for MatchCollection - manages collections of Match objects. Tests
 * add/remove operations, sorting, filtering, and statistical analysis.
 */
@DisplayName("MatchCollection Tests")
public class MatchCollectionTest extends BrobotTestBase {

    @Mock private Match mockMatch1;

    @Mock private Match mockMatch2;

    @Mock private Match mockMatch3;

    @Mock private Region mockRegion1;

    @Mock private Region mockRegion2;

    @Mock private Region mockRegion3;

    private MatchCollection collection;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        collection = new MatchCollection();

        // Setup mock matches with different scores and sizes
        when(mockMatch1.getScore()).thenReturn(0.95);
        when(mockMatch2.getScore()).thenReturn(0.85);
        when(mockMatch3.getScore()).thenReturn(0.90);

        when(mockMatch1.size()).thenReturn(1000);
        when(mockMatch2.size()).thenReturn(2000);
        when(mockMatch3.size()).thenReturn(1500);

        when(mockMatch1.getRegion()).thenReturn(mockRegion1);
        when(mockMatch2.getRegion()).thenReturn(mockRegion2);
        when(mockMatch3.getRegion()).thenReturn(mockRegion3);

        when(mockMatch1.x()).thenReturn(100);
        when(mockMatch1.y()).thenReturn(100);
        when(mockMatch2.x()).thenReturn(200);
        when(mockMatch2.y()).thenReturn(200);
        when(mockMatch3.x()).thenReturn(150);
        when(mockMatch3.y()).thenReturn(150);
    }

    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorAndInitialization {

        @Test
        @DisplayName("Default constructor creates empty collection")
        public void testDefaultConstructor() {
            MatchCollection newCollection = new MatchCollection();

            assertNotNull(newCollection.getMatches());
            assertTrue(newCollection.getMatches().isEmpty());
            assertEquals(-1, newCollection.getMaxMatches());
            assertNotNull(newCollection.getInitialMatches());
        }

        @Test
        @DisplayName("Constructor with max matches limit")
        public void testConstructorWithMaxMatches() {
            MatchCollection limitedCollection = new MatchCollection(5);

            assertEquals(5, limitedCollection.getMaxMatches());
            assertTrue(limitedCollection.getMatches().isEmpty());
        }
    }

    @Nested
    @DisplayName("Adding Matches")
    class AddingMatches {

        @Test
        @DisplayName("Add single match")
        public void testAddSingleMatch() {
            collection.add(mockMatch1);

            assertEquals(1, collection.getMatches().size());
            assertTrue(collection.getMatches().contains(mockMatch1));
        }

        @Test
        @DisplayName("Add multiple matches varargs")
        public void testAddMultipleMatchesVarargs() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);

            assertEquals(3, collection.getMatches().size());
            assertTrue(collection.getMatches().contains(mockMatch1));
            assertTrue(collection.getMatches().contains(mockMatch2));
            assertTrue(collection.getMatches().contains(mockMatch3));
        }

        @Test
        @DisplayName("Add null match is ignored")
        public void testAddNullMatch() {
            collection.add(mockMatch1, null, mockMatch2);

            assertEquals(2, collection.getMatches().size());
            assertFalse(collection.getMatches().contains(null));
        }

        @Test
        @DisplayName("Add all from another collection")
        public void testAddAllFromCollection() {
            MatchCollection otherCollection = new MatchCollection();
            otherCollection.add(mockMatch1, mockMatch2);

            collection.add(mockMatch3);
            collection.addAll(otherCollection);

            assertEquals(3, collection.getMatches().size());
            assertTrue(collection.getMatches().contains(mockMatch1));
            assertTrue(collection.getMatches().contains(mockMatch2));
            assertTrue(collection.getMatches().contains(mockMatch3));
        }

        @Test
        @DisplayName("Add all from list")
        public void testAddAllFromList() {
            List<Match> matchList = Arrays.asList(mockMatch1, mockMatch2, mockMatch3);

            collection.addAll(matchList);

            assertEquals(3, collection.getMatches().size());
        }

        @Test
        @DisplayName("Add all handles null collection")
        public void testAddAllNullCollection() {
            collection.add(mockMatch1);
            collection.addAll((MatchCollection) null);

            assertEquals(1, collection.getMatches().size());
        }

        @Test
        @DisplayName("Add all handles null list")
        public void testAddAllNullList() {
            collection.add(mockMatch1);
            collection.addAll((List<Match>) null);

            assertEquals(1, collection.getMatches().size());
        }
    }

    @Nested
    @DisplayName("Max Matches Enforcement")
    class MaxMatchesEnforcement {

        @Test
        @DisplayName("Enforce max matches keeps best scores")
        public void testEnforceMaxMatchesKeepsBest() {
            collection = new MatchCollection(2);

            collection.add(mockMatch1, mockMatch2, mockMatch3);

            assertEquals(2, collection.getMatches().size());
            assertTrue(collection.getMatches().contains(mockMatch1)); // score 0.95
            assertTrue(collection.getMatches().contains(mockMatch3)); // score 0.90
            assertFalse(collection.getMatches().contains(mockMatch2)); // score 0.85 (lowest)
        }

        @Test
        @DisplayName("No limit when maxMatches is negative")
        public void testNoLimitWithNegativeMaxMatches() {
            collection = new MatchCollection(-1);

            for (int i = 0; i < 100; i++) {
                Match match = mock(Match.class);
                when(match.getScore()).thenReturn(Math.random());
                collection.add(match);
            }

            assertEquals(100, collection.getMatches().size());
        }

        @Test
        @DisplayName("Enforce max matches after addAll")
        public void testEnforceMaxMatchesAfterAddAll() {
            collection = new MatchCollection(2);
            List<Match> matchList = Arrays.asList(mockMatch1, mockMatch2, mockMatch3);

            collection.addAll(matchList);

            assertEquals(2, collection.getMatches().size());
            assertTrue(collection.getMatches().contains(mockMatch1));
            assertTrue(collection.getMatches().contains(mockMatch3));
        }
    }

    @Nested
    @DisplayName("Sorting Operations")
    class SortingOperations {

        @BeforeEach
        void setupMatches() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);
        }

        @Test
        @DisplayName("Sort by score ascending")
        public void testSortByScoreAscending() {
            collection.sortByScore();

            List<Match> matches = collection.getMatches();
            assertEquals(mockMatch2, matches.get(0)); // 0.85
            assertEquals(mockMatch3, matches.get(1)); // 0.90
            assertEquals(mockMatch1, matches.get(2)); // 0.95
        }

        @Test
        @DisplayName("Sort by score descending")
        public void testSortByScoreDescending() {
            collection.sortByScoreDescending();

            List<Match> matches = collection.getMatches();
            assertEquals(mockMatch1, matches.get(0)); // 0.95
            assertEquals(mockMatch3, matches.get(1)); // 0.90
            assertEquals(mockMatch2, matches.get(2)); // 0.85
        }

        @Test
        @DisplayName("Sort by size ascending")
        public void testSortBySizeAscending() {
            collection.sortBySize();

            List<Match> matches = collection.getMatches();
            assertEquals(mockMatch1, matches.get(0)); // 1000
            assertEquals(mockMatch3, matches.get(1)); // 1500
            assertEquals(mockMatch2, matches.get(2)); // 2000
        }

        @Test
        @DisplayName("Sort by size descending")
        public void testSortBySizeDescending() {
            collection.sortBySizeDescending();

            List<Match> matches = collection.getMatches();
            assertEquals(mockMatch2, matches.get(0)); // 2000
            assertEquals(mockMatch3, matches.get(1)); // 1500
            assertEquals(mockMatch1, matches.get(2)); // 1000
        }

        @Test
        @DisplayName("Sort using strategy enum")
        public void testSortWithStrategy() {
            collection.sort(MatchCollection.SortStrategy.SCORE_DESCENDING);

            List<Match> matches = collection.getMatches();
            assertEquals(mockMatch1, matches.get(0));
            assertEquals(mockMatch3, matches.get(1));
            assertEquals(mockMatch2, matches.get(2));
        }

        @Test
        @DisplayName("Sort by distance from point")
        public void testSortByDistance() {
            Location targetPoint = new Location(0, 0);

            collection.sortByDistanceFrom(targetPoint);

            List<Match> matches = collection.getMatches();
            // mockMatch1 at (100,100) is closest to (0,0)
            // mockMatch3 at (150,150) is middle
            // mockMatch2 at (200,200) is farthest
            assertEquals(mockMatch1, matches.get(0));
            assertEquals(mockMatch3, matches.get(1));
            assertEquals(mockMatch2, matches.get(2));
        }
    }

    @Nested
    @DisplayName("Filtering Operations")
    class FilteringOperations {

        @BeforeEach
        void setupMatches() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);
        }

        @Test
        @DisplayName("Filter by minimum score")
        public void testFilterByMinScore() {
            MatchCollection filteredColl = collection.filterByMinScore(0.88);
            List<Match> filtered = filteredColl.getMatches();

            assertEquals(2, filtered.size());
            assertTrue(filtered.contains(mockMatch1)); // 0.95
            assertTrue(filtered.contains(mockMatch3)); // 0.90
            assertFalse(filtered.contains(mockMatch2)); // 0.85
        }

        @Test
        @DisplayName("Filter by score range")
        public void testFilterByScoreRange() {
            MatchCollection filteredColl =
                    collection.filter(m -> m.getScore() >= 0.86 && m.getScore() <= 0.92);
            List<Match> filtered = filteredColl.getMatches();

            assertEquals(1, filtered.size());
            assertTrue(filtered.contains(mockMatch3)); // 0.90
        }

        @Test
        @DisplayName("Filter by custom predicate")
        public void testFilterByPredicate() {
            Predicate<Match> largeSizePredicate = match -> match.size() > 1200;

            MatchCollection filteredColl = collection.filter(largeSizePredicate);
            List<Match> filtered = filteredColl.getMatches();

            assertEquals(2, filtered.size());
            assertTrue(filtered.contains(mockMatch2)); // 2000
            assertTrue(filtered.contains(mockMatch3)); // 1500
            assertFalse(filtered.contains(mockMatch1)); // 1000
        }

        @Test
        @DisplayName("Remove matches by predicate")
        public void testRemoveByPredicate() {
            Predicate<Match> lowScorePredicate = match -> match.getScore() < 0.90;

            collection.getMatches().removeIf(lowScorePredicate);

            assertEquals(2, collection.getMatches().size());
            assertTrue(collection.getMatches().contains(mockMatch1)); // 0.95
            assertTrue(collection.getMatches().contains(mockMatch3)); // 0.90
            assertFalse(collection.getMatches().contains(mockMatch2)); // 0.85
        }
    }

    @Nested
    @DisplayName("Initial State Management")
    class InitialStateManagement {

        @Test
        @DisplayName("Capture initial state")
        public void testCaptureInitialState() {
            collection.add(mockMatch1, mockMatch2);

            collection.captureInitialState();
            collection.add(mockMatch3);

            List<Match> initial = collection.getInitialMatches();
            assertEquals(2, initial.size());
            assertTrue(initial.contains(mockMatch1));
            assertTrue(initial.contains(mockMatch2));
            assertFalse(initial.contains(mockMatch3));

            assertEquals(3, collection.getMatches().size());
        }

        @Test
        @DisplayName("Get initial matches returns copy")
        public void testGetInitialMatchesReturnsCopy() {
            collection.add(mockMatch1);
            collection.captureInitialState();

            List<Match> initial1 = collection.getInitialMatches();
            List<Match> initial2 = collection.getInitialMatches();

            assertNotSame(initial1, initial2);
            assertEquals(initial1, initial2);
        }

        @Test
        @DisplayName("Get initial matches when not captured")
        public void testGetInitialMatchesWhenNotCaptured() {
            collection.add(mockMatch1);

            List<Match> initial = collection.getInitialMatches();

            assertNotNull(initial);
            assertTrue(initial.isEmpty());
        }
    }

    @Nested
    @DisplayName("Best Match Selection")
    class BestMatchSelection {

        @BeforeEach
        void setupMatches() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);
        }

        @Test
        @DisplayName("Get best match by score")
        public void testGetBestMatchByScore() {
            Optional<Match> best = collection.getBest();

            assertTrue(best.isPresent());
            assertEquals(mockMatch1, best.get()); // Highest score 0.95
        }

        @Test
        @DisplayName("Get best match from empty collection")
        public void testGetBestMatchEmpty() {
            collection = new MatchCollection();

            Optional<Match> best = collection.getBest();

            assertFalse(best.isPresent());
        }

        @Test
        @DisplayName("Get top N matches")
        public void testGetTopMatches() {
            collection.sortByScoreDescending();
            List<Match> top2 =
                    collection.getMatches().size() > 2
                            ? collection.getMatches().subList(0, 2)
                            : collection.getMatches();

            assertEquals(2, top2.size());
            assertEquals(mockMatch1, top2.get(0)); // 0.95
            assertEquals(mockMatch3, top2.get(1)); // 0.90
        }

        @Test
        @DisplayName("Get top N matches when N exceeds size")
        public void testGetTopMatchesExceedsSize() {
            collection.sortByScoreDescending();
            List<Match> top10 = collection.getMatches();

            assertEquals(3, top10.size());
        }
    }

    @Nested
    @DisplayName("Collection Statistics")
    class CollectionStatistics {

        @BeforeEach
        void setupMatches() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);
        }

        @Test
        @DisplayName("Get average score")
        public void testGetAverageScore() {
            double avgScore =
                    collection.getMatches().stream()
                            .mapToDouble(Match::getScore)
                            .average()
                            .orElse(0.0);

            double expected = (0.95 + 0.85 + 0.90) / 3.0;
            assertEquals(expected, avgScore, 0.001);
        }

        @Test
        @DisplayName("Get average score of empty collection")
        public void testGetAverageScoreEmpty() {
            collection = new MatchCollection();

            double avgScore =
                    collection.getMatches().stream()
                            .mapToDouble(Match::getScore)
                            .average()
                            .orElse(0.0);

            assertEquals(0.0, avgScore);
        }

        @Test
        @DisplayName("Get min and max scores")
        public void testGetMinMaxScores() {
            double minScore =
                    collection.getMatches().stream().mapToDouble(Match::getScore).min().orElse(0.0);
            double maxScore =
                    collection.getMatches().stream().mapToDouble(Match::getScore).max().orElse(0.0);

            assertEquals(0.85, minScore);
            assertEquals(0.95, maxScore);
        }

        @Test
        @DisplayName("Count matches above threshold")
        public void testCountMatchesAboveThreshold() {
            long count = collection.getMatches().stream().filter(m -> m.getScore() > 0.88).count();

            assertEquals(2, count); // 0.95 and 0.90
        }

        @Test
        @DisplayName("Check if collection is empty")
        public void testIsEmpty() {
            assertFalse(collection.isEmpty());

            collection.clear();

            assertTrue(collection.isEmpty());
        }

        @Test
        @DisplayName("Get collection size")
        public void testSize() {
            assertEquals(3, collection.size());

            collection.add(mock(Match.class));

            assertEquals(4, collection.size());
        }
    }

    @Nested
    @DisplayName("Collection Manipulation")
    class CollectionManipulation {

        @Test
        @DisplayName("Clear collection")
        public void testClear() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);

            collection.clear();

            assertTrue(collection.isEmpty());
            assertEquals(0, collection.size());
        }

        @Test
        @DisplayName("Remove specific match")
        public void testRemoveMatch() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);

            boolean removed = collection.getMatches().remove(mockMatch2);

            assertTrue(removed);
            assertEquals(2, collection.size());
            assertFalse(collection.getMatches().contains(mockMatch2));
        }

        @Test
        @DisplayName("Remove non-existent match")
        public void testRemoveNonExistentMatch() {
            collection.add(mockMatch1);

            boolean removed = collection.getMatches().remove(mockMatch2);

            assertFalse(removed);
            assertEquals(1, collection.size());
        }

        @Test
        @DisplayName("Retain only specified matches")
        public void testRetainAll() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);
            List<Match> toRetain = Arrays.asList(mockMatch1, mockMatch3);

            collection.getMatches().retainAll(toRetain);

            assertEquals(2, collection.size());
            assertTrue(collection.getMatches().contains(mockMatch1));
            assertTrue(collection.getMatches().contains(mockMatch3));
            assertFalse(collection.getMatches().contains(mockMatch2));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCasesAndValidation {

        @Test
        @DisplayName("Handle matches with same scores")
        public void testMatchesWithSameScores() {
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);
            Match match3 = mock(Match.class);
            when(match1.getScore()).thenReturn(0.90);
            when(match2.getScore()).thenReturn(0.90);
            when(match3.getScore()).thenReturn(0.90);

            collection.add(match1, match2, match3);
            collection.sortByScoreDescending();

            assertEquals(3, collection.size());
            // All should be present even with same scores
            assertTrue(collection.getMatches().contains(match1));
            assertTrue(collection.getMatches().contains(match2));
            assertTrue(collection.getMatches().contains(match3));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10, 100})
        @DisplayName("Handle various max match limits")
        public void testVariousMaxMatchLimits(int maxMatches) {
            collection = new MatchCollection(maxMatches);

            for (int i = 0; i < 150; i++) {
                Match match = mock(Match.class);
                when(match.getScore()).thenReturn(Math.random());
                collection.add(match);
            }

            if (maxMatches > 0) {
                assertEquals(maxMatches, collection.size());
            } else {
                assertEquals(150, collection.size());
            }
        }

        @Test
        @DisplayName("Collection to array conversion")
        public void testToArray() {
            collection.add(mockMatch1, mockMatch2, mockMatch3);

            Match[] array = collection.getMatches().toArray(new Match[0]);

            assertEquals(3, array.length);
            assertTrue(Arrays.asList(array).contains(mockMatch1));
            assertTrue(Arrays.asList(array).contains(mockMatch2));
            assertTrue(Arrays.asList(array).contains(mockMatch3));
        }
    }
}
