package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MatchesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    List<StateImage> getMatchListAsStateImages() {
        ActionResult matches = new ActionResult();
        Match match1 = new Match.Builder()
                .setRegion(new Region(0, 0, 10, 10))
                .setName("topLeft")
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