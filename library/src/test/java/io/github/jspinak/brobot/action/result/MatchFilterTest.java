package io.github.jspinak.brobot.action.result;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Unit tests for MatchFilter utility class. Tests various filtering operations on match
 * collections.
 */
@DisplayName("MatchFilter Tests")
public class MatchFilterTest extends BrobotTestBase {

    private List<Match> testMatches;
    private Match match1;
    private Match match2;
    private Match match3;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Create test matches with different properties
        StateObjectMetadata stateData1 = new StateObjectMetadata();
        stateData1.setStateObjectId("obj1");
        stateData1.setObjectType(StateObject.Type.IMAGE);
        stateData1.setOwnerStateName("State1");

        match1 =
                new Match.Builder()
                        .setRegion(new Region(0, 0, 100, 100))
                        .setSimScore(0.95)
                        .setName("match1")
                        .setStateObjectData(stateData1)
                        .build();

        StateObjectMetadata stateData2 = new StateObjectMetadata();
        stateData2.setStateObjectId("obj2");
        stateData2.setObjectType(StateObject.Type.IMAGE);
        stateData2.setOwnerStateName("State2");

        match2 =
                new Match.Builder()
                        .setRegion(new Region(100, 100, 100, 100))
                        .setSimScore(0.85)
                        .setName("match2")
                        .setStateObjectData(stateData2)
                        .build();

        StateObjectMetadata stateData3 = new StateObjectMetadata();
        stateData3.setStateObjectId("obj3");
        stateData3.setObjectType(StateObject.Type.REGION);
        stateData3.setOwnerStateName("State1");

        match3 =
                new Match.Builder()
                        .setRegion(new Region(200, 200, 100, 100))
                        .setSimScore(0.75)
                        .setName("match3")
                        .setStateObjectData(stateData3)
                        .build();

        testMatches = new ArrayList<>(Arrays.asList(match1, match2, match3));
    }

    @Nested
    @DisplayName("Filter by State Object")
    class FilterByStateObject {

        @Test
        @DisplayName("Should filter matches by state object ID")
        public void testFilterByStateObjectId() {
            List<Match> filtered = MatchFilter.byStateObject(testMatches, "obj1");

            assertEquals(1, filtered.size());
            assertEquals(match1, filtered.get(0));
        }

        @Test
        @DisplayName("Should return empty list for non-existent object ID")
        public void testFilterByNonExistentObjectId() {
            List<Match> filtered = MatchFilter.byStateObject(testMatches, "nonexistent");

            assertTrue(filtered.isEmpty());
        }

        @Test
        @DisplayName("Should handle null object ID")
        public void testFilterByNullObjectId() {
            List<Match> filtered = MatchFilter.byStateObject(testMatches, null);

            assertTrue(filtered.isEmpty());
        }

        @Test
        @DisplayName("Should handle null match list")
        public void testFilterNullMatchList() {
            List<Match> filtered = MatchFilter.byStateObject(null, "obj1");

            assertNotNull(filtered);
            assertTrue(filtered.isEmpty());
        }
    }

    @Nested
    @DisplayName("Filter by Owner State")
    class FilterByOwnerState {

        @Test
        @DisplayName("Should filter matches by owner state name")
        public void testFilterByOwnerStateName() {
            List<Match> filtered = MatchFilter.byOwnerState(testMatches, "State1");

            assertEquals(2, filtered.size());
            assertTrue(filtered.contains(match1));
            assertTrue(filtered.contains(match3));
        }

        @Test
        @DisplayName("Should return single match for unique state")
        public void testFilterByUniqueState() {
            List<Match> filtered = MatchFilter.byOwnerState(testMatches, "State2");

            assertEquals(1, filtered.size());
            assertEquals(match2, filtered.get(0));
        }

        @Test
        @DisplayName("Should return empty list for non-existent state")
        public void testFilterByNonExistentState() {
            List<Match> filtered = MatchFilter.byOwnerState(testMatches, "NonExistentState");

            assertTrue(filtered.isEmpty());
        }

        @Test
        @DisplayName("Should handle null state name")
        public void testFilterByNullStateName() {
            List<Match> filtered = MatchFilter.byOwnerState(testMatches, null);

            assertTrue(filtered.isEmpty());
        }
    }

    @Nested
    @DisplayName("Filter by Score")
    class FilterByScore {

        @Test
        @DisplayName("Should filter matches by minimum score")
        public void testFilterByMinScore() {
            List<Match> filtered = MatchFilter.byMinScore(testMatches, 0.80);

            assertEquals(2, filtered.size());
            assertTrue(filtered.contains(match1));
            assertTrue(filtered.contains(match2));
        }

        @Test
        @DisplayName("Should include matches exactly at threshold")
        public void testFilterByMinScoreExact() {
            List<Match> filtered = MatchFilter.byMinScore(testMatches, 0.85);

            assertEquals(2, filtered.size());
            assertTrue(filtered.contains(match1));
            assertTrue(filtered.contains(match2));
        }

        @Test
        @DisplayName("Should filter matches by maximum score")
        public void testFilterByMaxScore() {
            List<Match> filtered = MatchFilter.byMaxScore(testMatches, 0.80);

            assertEquals(1, filtered.size());
            assertTrue(filtered.contains(match3));
        }

        @Test
        @DisplayName("Should filter matches within score range")
        public void testFilterByScoreRange() {
            List<Match> filtered = MatchFilter.byScoreRange(testMatches, 0.80, 0.90);

            assertEquals(1, filtered.size());
            assertTrue(filtered.contains(match2));
        }

        @Test
        @DisplayName("Should return all matches when range includes all")
        public void testFilterByScoreRangeAll() {
            List<Match> filtered = MatchFilter.byScoreRange(testMatches, 0.0, 1.0);

            assertEquals(3, filtered.size());
        }

        @Test
        @DisplayName("Should return empty list for invalid range")
        public void testFilterByInvalidScoreRange() {
            List<Match> filtered = MatchFilter.byScoreRange(testMatches, 0.90, 0.80);

            assertTrue(filtered.isEmpty());
        }
    }

    @Nested
    @DisplayName("Filter by Region")
    class FilterByRegion {

        @Test
        @DisplayName("Should filter matches within region")
        public void testFilterWithinRegion() {
            Region containingRegion = new Region(0, 0, 150, 150);
            List<Match> filtered = MatchFilter.withinRegion(testMatches, containingRegion);

            // Only matches fully contained within the region
            // match1 (0,0,100,100) is fully within (0,0,150,150)
            // match2 (100,100,100,100) extends to (200,200) which is outside
            assertEquals(1, filtered.size());
            assertTrue(filtered.contains(match1));
        }

        @Test
        @DisplayName("Should filter overlapping matches")
        public void testFilterOverlapping() {
            Region overlapRegion = new Region(50, 50, 100, 100);
            List<Match> filtered = MatchFilter.overlapping(testMatches, overlapRegion);

            // match1 overlaps, match2 overlaps at corner
            assertEquals(2, filtered.size());
            assertTrue(filtered.contains(match1));
            assertTrue(filtered.contains(match2));
        }

        @Test
        @DisplayName("Should handle null region")
        public void testFilterByNullRegion() {
            List<Match> filtered = MatchFilter.withinRegion(testMatches, null);

            assertTrue(filtered.isEmpty());
        }
    }

    @Nested
    @DisplayName("Filter by Distance")
    class FilterByDistance {

        @Test
        @DisplayName("Should filter matches near location")
        public void testFilterNearLocation() {
            Location center = new Location(100, 100);
            double maxDistance = 100.0;

            List<Match> filtered = MatchFilter.nearLocation(testMatches, center, maxDistance);

            // match1 center is at (50, 50), distance ~70.7
            // match2 center is at (150, 150), distance ~70.7
            // match3 center is at (250, 250), distance ~212
            assertEquals(2, filtered.size());
            assertTrue(filtered.contains(match1));
            assertTrue(filtered.contains(match2));
        }

        @Test
        @DisplayName("Should include matches exactly at distance threshold")
        public void testFilterByExactDistance() {
            Location origin = new Location(0, 0);
            // match1 center is at (50, 50), distance = sqrt(50^2 + 50^2) ≈ 70.7
            double maxDistance = 71.0;

            List<Match> filtered = MatchFilter.nearLocation(testMatches, origin, maxDistance);

            assertEquals(1, filtered.size());
            assertTrue(filtered.contains(match1));
        }

        @Test
        @DisplayName("Should handle null location")
        public void testFilterByNullLocation() {
            List<Match> filtered = MatchFilter.nearLocation(testMatches, null, 100.0);

            assertTrue(filtered.isEmpty());
        }
    }

    @Nested
    @DisplayName("Custom Filters")
    class CustomFilters {

        @Test
        @DisplayName("Should apply custom predicate filter")
        public void testCustomFilter() {
            // Filter for matches with names containing "1"
            List<Match> filtered =
                    MatchFilter.byPredicate(
                            testMatches, m -> m.getName() != null && m.getName().contains("1"));

            assertEquals(1, filtered.size());
            assertTrue(filtered.contains(match1));
        }

        @Test
        @DisplayName("Should chain multiple filters")
        public void testChainedFilters() {
            // First filter by score, then by state
            List<Match> filtered = MatchFilter.byMinScore(testMatches, 0.80);
            filtered = MatchFilter.byOwnerState(filtered, "State1");

            assertEquals(1, filtered.size());
            assertTrue(filtered.contains(match1));
        }

        @Test
        @DisplayName("Should handle empty predicate result")
        public void testEmptyPredicateResult() {
            List<Match> filtered = MatchFilter.byPredicate(testMatches, m -> false);

            assertTrue(filtered.isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty match list")
        public void testEmptyMatchList() {
            List<Match> empty = new ArrayList<>();

            assertTrue(MatchFilter.byMinScore(empty, 0.5).isEmpty());
            assertTrue(MatchFilter.byOwnerState(empty, "State1").isEmpty());
            assertTrue(MatchFilter.byStateObject(empty, "obj1").isEmpty());
        }

        @Test
        @DisplayName("Should handle matches with null properties")
        public void testMatchesWithNullProperties() {
            Match nullMatch = new Match.Builder().setRegion(new Region(0, 0, 10, 10)).build();
            // Don't set owner state or state object data

            List<Match> matches = Arrays.asList(nullMatch);

            List<Match> filtered = MatchFilter.byOwnerState(matches, "State1");
            assertTrue(filtered.isEmpty());

            filtered = MatchFilter.byStateObject(matches, "obj1");
            assertTrue(filtered.isEmpty());
        }

        @Test
        @DisplayName("Should preserve original list immutability")
        public void testImmutability() {
            int originalSize = testMatches.size();

            List<Match> filtered = MatchFilter.byMinScore(testMatches, 0.90);

            assertEquals(originalSize, testMatches.size());
            assertNotSame(testMatches, filtered);
        }
    }

    @Nested
    @DisplayName("Additional Filters")
    class AdditionalFilters {

        @Test
        @DisplayName("Should filter by minimum area")
        public void testFilterByMinArea() {
            // All test matches have 100x100 area = 10000
            List<Match> filtered = MatchFilter.byMinArea(testMatches, 5000);
            assertEquals(3, filtered.size());

            filtered = MatchFilter.byMinArea(testMatches, 15000);
            assertTrue(filtered.isEmpty());
        }

        @Test
        @DisplayName("Should filter by maximum area")
        public void testFilterByMaxArea() {
            List<Match> filtered = MatchFilter.byMaxArea(testMatches, 15000);
            assertEquals(3, filtered.size());

            filtered = MatchFilter.byMaxArea(testMatches, 5000);
            assertTrue(filtered.isEmpty());
        }

        @Test
        @DisplayName("Should limit number of matches")
        public void testLimitMatches() {
            List<Match> filtered = MatchFilter.limit(testMatches, 2);
            assertEquals(2, filtered.size());

            filtered = MatchFilter.limit(testMatches, 5);
            assertEquals(3, filtered.size());

            filtered = MatchFilter.limit(testMatches, 0);
            assertTrue(filtered.isEmpty());
        }

        @Test
        @DisplayName("Should get unique matches by state object")
        public void testUniqueByStateObject() {
            // Add duplicate with same state object
            StateObjectMetadata duplicateData = new StateObjectMetadata();
            duplicateData.setStateObjectId("obj1");
            duplicateData.setObjectType(StateObject.Type.IMAGE);

            Match duplicate =
                    new Match.Builder()
                            .setRegion(new Region(300, 300, 100, 100))
                            .setSimScore(0.99)
                            .setStateObjectData(duplicateData)
                            .build();
            testMatches.add(duplicate);

            List<Match> filtered = MatchFilter.uniqueByStateObject(testMatches);
            // Should have one match per unique state object ID
            assertEquals(3, filtered.size());
        }
    }
}
