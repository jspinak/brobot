package io.github.jspinak.brobot.test.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class WordMatchesTests {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    Action action;

    Matches getWordMatches() {
        TestData testData = new TestData();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withScenes(testData.getFloranext0())
                .build();
        ActionOptions findWordsOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL_WORDS)
                .build();
        Matches matches = action.perform(findWordsOptions, objColl);
        //matches.getMatchList().forEach(System.out::println);
        //matches.getMatchList().forEach(match -> System.out.println(match.getText()));
        return matches;
    }

    @Test
    void findWords() {
        Matches matches = getWordMatches();
        assertFalse(matches.isEmpty());
    }

    @Test
    void matchesHaveMats() {
        Matches matches = getWordMatches();
        assertNotNull(matches.getMatchList().get(0).getMat());
    }

    @Test
    void firstMatchHasText() {
        Matches matches = getWordMatches();
        assertNotEquals(0, matches.size());
        String text = matches.getMatchList().get(0).getText();
        System.out.println("Text = " + text);
        assertNotEquals("", text);
    }
}