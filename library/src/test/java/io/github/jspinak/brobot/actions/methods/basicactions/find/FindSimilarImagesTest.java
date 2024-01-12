package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Test
    void perform() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.SIMILAR_IMAGES)
                .build();
        ObjectCollection objectCollection1 = new ObjectCollection.Builder()
                .withPatterns(new Pattern("../screenshots/floranext1"))
                .build();
        ObjectCollection objectCollection2 = new ObjectCollection.Builder()
                .withPatterns(
                        new Pattern("../screenshots/floranext0"),
                        new Pattern("../screenshots/floranext1"),
                        new Pattern("../screenshots/floranext2"),
                        new Pattern("../screenshots/floranext3"),
                        new Pattern("../screenshots/floranext4"))
                .build();

        Matches matches = matchesInitializer.init(actionOptions, "find similar screenshots",
                objectCollection1, objectCollection2);
        Optional<Match> bestMatch = matches.getBestMatch();

        assertFalse(bestMatch.isEmpty());
        assertEquals("floranext0", bestMatch.get().getName());
    }
}