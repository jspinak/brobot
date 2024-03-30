package io.github.jspinak.brobot.test.datatypes.primitives.match;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchesTest {

    List<StateImage> getMatchListAsStateImages() {
        Matches matches = new Matches();
        Match match1 = new Match.Builder()
                .setRegion(new Region(0, 0, 10, 10))
                .setName("match1")
                .build();
        matches.add(match1);
        List<StateImage> stateImageList = matches.getMatchListAsStateImages();
        stateImageList.forEach(System.out::println);
        return stateImageList;
    }

    @Test
    void matchListIsNotEmpty() {
        List<StateImage> stateImages = getMatchListAsStateImages();
        assertFalse(stateImages.isEmpty());
    }

    @Test
    void matchListItemHasName() {
        List<StateImage> stateImages = getMatchListAsStateImages();
        assertFalse(stateImages.get(0).getName().isEmpty());
    }

    @Test
    void matchListItemHasPattern() {
        List<StateImage> stateImages = getMatchListAsStateImages();
        assertFalse(stateImages.get(0).getPatterns().isEmpty());
    }

    @Test
    void stateImageHasDefinedRegion() {
        List<StateImage> stateImages = getMatchListAsStateImages();
        assertTrue(stateImages.get(0).getLargestDefinedFixedRegionOrNewRegion().isDefined());
    }
}