package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class MatchesTest {

    private Matches matches;
    private Match lowScoreMatch;
    private Match highScoreMatch;
    private final Match m1 = new Match.Builder().setRegion(0,0,10,10).setSimScore(0.8).build();
    private final Match m2 = new Match.Builder().setRegion(100,100,10,10).setSimScore(0.9).build();

    @Test
    void getMedian_shouldReturnAverageRegion() {
        Matches matches = new Matches();
        matches.add(m1, m2);
        Region median = matches.getMedian().orElseThrow();
        assertThat(median.x()).isEqualTo(50);
        assertThat(median.y()).isEqualTo(50);
        assertThat(median.w()).isEqualTo(10);
        assertThat(median.h()).isEqualTo(10);
    }

    @Test
    void getClosestTo_shouldReturnNearestMatch() {
        Matches matches = new Matches();
        matches.add(m1, m2);
        Location nearM1 = new Location(5, 5);
        Match closest = matches.getClosestTo(nearM1).orElseThrow();
        assertThat(closest).isEqualTo(m1);
    }

    @Test
    void keepOnlyConfirmedMatches_shouldFilterMatchList() {
        Matches baseMatches = new Matches();
        baseMatches.add(m1, m2);

        Matches confirmingMatches = new Matches();
        confirmingMatches.add(m2); // Only confirm m2

        baseMatches.keepOnlyConfirmedMatches(confirmingMatches);
        assertThat(baseMatches.getMatchList()).containsExactly(m2);
    }

    @Test
    void getOwnerStateNames_shouldReturnUniqueNames() {
        StateObjectData sod1 = new StateObjectData();
        sod1.setOwnerStateName("StateA");
        StateObjectData sod2 = new StateObjectData();
        sod2.setOwnerStateName("StateB");

        Match matchA = new Match.Builder().setStateObjectData(sod1).build();
        Match matchB = new Match.Builder().setStateObjectData(sod2).build();

        Matches matches = new Matches();
        matches.add(matchA, matchB, matchA); // Add one duplicate

        assertThat(matches.getOwnerStateNames()).containsExactlyInAnyOrder("StateA", "StateB");
    }

    @BeforeEach
    void setUp() {
        matches = new Matches();
        lowScoreMatch = new Match.Builder().setRegion(10, 10, 10, 10).setSimScore(0.8).build();
        highScoreMatch = new Match.Builder().setRegion(20, 20, 10, 10).setSimScore(0.95).build();
    }

    @Test
    void add_shouldIncreaseSize() {
        matches.add(lowScoreMatch);
        assertEquals(1, matches.size());
    }

    @Test
    void getBestMatch_shouldReturnMatchWithHighestScore() {
        matches.add(lowScoreMatch, highScoreMatch);
        Optional<Match> bestMatch = matches.getBestMatch();
        assertTrue(bestMatch.isPresent());
        assertEquals(highScoreMatch, bestMatch.get());
    }

    @Test
    void getMatchRegions_shouldReturnListOfRegions() {
        matches.add(lowScoreMatch, highScoreMatch);
        List<Region> regions = matches.getMatchRegions();
        assertEquals(2, regions.size());
        assertTrue(regions.contains(lowScoreMatch.getRegion()));
        assertTrue(regions.contains(highScoreMatch.getRegion()));
    }

    @Test
    void minus_shouldReturnMatchesNotInOther() {
        Matches otherMatches = new Matches();
        otherMatches.add(lowScoreMatch);

        matches.add(lowScoreMatch, highScoreMatch);

        Matches result = matches.minus(otherMatches);
        assertEquals(1, result.size());
        assertEquals(highScoreMatch, result.getMatchList().getFirst());
    }
}