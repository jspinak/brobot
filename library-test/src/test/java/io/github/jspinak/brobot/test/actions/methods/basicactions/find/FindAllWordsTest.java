package io.github.jspinak.brobot.test.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.ALL_WORDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = BrobotTestApplication.class)
public class FindAllWordsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    Action action;

    @Test
    void findWordsWithMinSize() {
        ObjectCollection screens = new ObjectCollection.Builder()
                .withScenes(new Pattern("../screenshots/floranext0"))
                .build();
        ActionOptions findAllWords = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ALL_WORDS)
                .setMinArea(25)
                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                .setMaxFusionDistances(20, 10)
                .build();
        Matches wordMatches = action.perform(findAllWords, screens);
        for (int i=0; i<wordMatches.getMatchList().size(); i++) {
            Match match = wordMatches.getMatchList().get(i);
            assertTrue(match.getRegion().size() >= 25);
        }
        assertFalse(wordMatches.isEmpty());
    }
}
