package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.compareImages.FindSimilarImages;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.ALL_WORDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = BrobotTestApplication.class)
class FindSimilarImagesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    MatchesInitializer matchesInitializer;

    @Autowired
    FindSimilarImages findSimilarImages;

    @Autowired
    Action action;

    /**
     * Checks to see if screenshot 1 is found in screenshots 0, 2, 3, 4.
     * It should match with screenshot0.
     */
    @Test
    void shouldMatchScreenshot0() {
        TestData testData = new TestData();

        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.SIMILAR_IMAGES)
                .build();
        ObjectCollection objectCollection1 = new ObjectCollection.Builder()
                .withPatterns(testData.getFloranext1())
                .build();
        ObjectCollection objectCollection2 = new ObjectCollection.Builder()
                .withPatterns(testData.getPatterns(0,2,3,4))
                .build();

        Matches matches = matchesInitializer.init(actionOptions, "find similar screenshots",
                objectCollection1, objectCollection2);
        findSimilarImages.find(matches, List.of(objectCollection1, objectCollection2));
        Optional<Match> bestMatch = matches.getBestMatch();

        assertFalse(bestMatch.isEmpty());
        assertEquals("floranext0", bestMatch.get().getName());
    }

    /**
     * Find images corresponding to words.
     * Do this once and fuse matches that are close together.
     * Do this again without fusing matches.
     * Every match in once set should have a similar match in the other set.
     */
    @Test
    void shouldFindSimilarImages() {
        ObjectCollection screen0 = new ObjectCollection.Builder()
                .withScenes("../screenshots/floranext0")
                .build();
        ActionOptions findAndFuseWords = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ALL_WORDS)
                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                .setMaxFusionDistances(20, 10)
                .build();
        Matches fusedMatches = action.perform(findAndFuseWords, screen0);

        ActionOptions findWordsDontFuse = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ALL_WORDS)
                .setFusionMethod(ActionOptions.MatchFusionMethod.NONE)
                .build();
        Matches notFusedMatches = action.perform(findWordsDontFuse, screen0);

        ActionOptions findSimilar = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.SIMILAR_IMAGES)
                .build();
        ObjectCollection fused = new ObjectCollection.Builder()
                .withMatchObjectsAsStateImages(fusedMatches.getMatchList().toArray(new Match[0]))
                .build();
        ObjectCollection notFused = new ObjectCollection.Builder()
                .withMatchObjectsAsStateImages(notFusedMatches.getMatchList().toArray(new Match[0]))
                .build();
        Matches similarMatches = action.perform(findSimilar, fused, notFused);

        // FIND.SIMILAR_IMAGES returns a match for each image in the 2nd Object Collection
        assertEquals(notFused.getStateImages().size(), similarMatches.size());
    }
}