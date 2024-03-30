package io.github.jspinak.brobot.test.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.compareImages.FindSimilarImages;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

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

    @Test
    void perform() {
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
}